/*
    1-24-09
    Copyright Spark Fun Electronics© 2009
    Nathan Seidle
	Heavily modified by Mark Zachmann

    Wireless bootloader for the ATmega168 and XBee Series 2 modules

	This is a small (~1K) serial bootloader designed to be a robust solution for remote reset and wireless
	booloading. It's not extremely fast, but is very hardy.

	This bootloader accepts a pure binary stream (not an intel hex file format). All file parsing is done on the
	base side (usually a beefy computer with lots of extra processing ability).

	Things I learned from testing:

	XBee Series 2.5 ships with CTS enabled! That's on purpose and necessary. They ignore serial when they're doing RF stuff.
	I think the chips are low bandwidth.

	To get a Series 2.5 link to work, you must configure the device on XBee Explorer as Coordinator,
	and the device in your funnel board as the end device.
	Both devices should be set up in API mode with Escape chars and set to the same PAN ID.

	Series 2 module settings:
	Baud: 9600
	CTS flow control (default)
	No change to packetization timeout (default = 3?)

	To start the transfer the remote coordinator toggles the XBee DIO3 line to reset the AVR. Leave it as
	Input or Disabled so the user can click the button and not short stuff...

	On restart the AVR will blink the LED for a second and then wait for 10 seconds for a bootload request from the coordinator.
	It takes a few seconds to join the PAN anyway.If nothing comes in the 10 seconds then the remote unit jumps to the beginning
	of the regular program code.

	Data Format:
	The bootload packet transmit request to the funnel is:

		PRxxddddddddddddddddddddddddd

		bytes	2	Program command (PR)
		bytes	2	Start address (low byte first)
		bytes   n	Data

	On complete the coordinator sends two bytes

		EN

	The packetization adds checksum and count so we don't.

	After each packet is received the XBee sends back an OK to tell the coordinator it's ready for another packet. This
	gets rid of any timing dependencies in the code.

	Wireless:
	38 seconds to load 14500 code words (most of the space) at 38400 / 8MHz (internal osc)
	38 seconds to load 14500 code words (most of the space) at 19200 / 8MHz (internal osc)
	Wired:
	11 seconds to load 14500 code words (most of the space) at 19200 / 8MHz (internal osc)
	so you see, there is no benefit to a higher baud rate. The XBee protocol is the bottleneck

	Oh, and if you happen to be using an XBee with a UFL antenna connector (and don't have a UFL antenna sitting around)
	you can convert it to a wire antenna simply by soldering in a short wire into the XBee. It may not be the best,
	but it works.

*/

// a packet: 7E 00 15 90 00 7D 33 xx 00 40 3C xx 7D 33 00 00 01 30 32 3A 33 34 35 36 37 38 C1
// unescaped: 7E 00 15 90 00 13 xx 00 40 3C xx 13 00 00 01 30 32 3A 33 34 35 36 37 38 C1
// byte 7E-header
// word 00.15 length
// byte 90-api
// -- doc says byte 00 frameid but this is wrong I think
// x64 00 13 xx 00 40 3C xx 13 -xbee address
// x16 00 00 xbee 16 address (not used)
// byte 1 - 1==acknowledged, 2==broadcast
// bytes 30 32... data
// byte C1 checksum
// i guess this is right - msz
// #define F_CPU 16000000UL

#if defined AtMega2560 && !(defined AtMega2560_3V3)
#define F_CPU 16000000
#else
#define F_CPU 8000000
#endif

#include <inttypes.h>
#include <avr/io.h>
#include <util/delay.h>
#include <avr/boot.h>
#include <avr/interrupt.h>
#include <avr/pgmspace.h>
#include <avr/wdt.h>

#if defined AtMega2560
#define LED      PINB7
#define UBRRxH UBRR1H
#define UBRRxL UBRR1L
#define UCSRxA UCSR1A
#define UCSRxB UCSR1B
#define UCSRxC UCSR1C
#define UDREx UDRE1
#define UDRx UDR1
#define RXENx RXEN1
#define TXENx TXEN1
#define RXCx RXC1
#define TXCx TXC1
#define UCSZx0 UCSZ10
#define UCSZx1 UCSZ11
#else
#define LED      PINB5
#define UBRRxH UBRR0H
#define UBRRxL UBRR0L
#define UCSRxA UCSR0A
#define UCSRxB UCSR0B
#define UCSRxC UCSR0C
#define UDREx UDRE0
#define UDRx UDR0
#define RXENx RXEN0
#define TXENx TXEN0
#define RXCx RXC0
#define TXCx TXC0
#define UCSZx0 UCSZ00
#define UCSZx1 UCSZ01
#endif


//Define baud rate
#define BAUD 38400ul //Works with internal osc
#define MYUBRR (((((F_CPU * 10) / (16L * BAUD)) + 5) / 10) - 1)

//Here we calculate the wait period inside getch(). Too few cycles and the XBee may not be able to send the character in time. Too long and your sketch will take a long time to boot after powerup.
#define MAX_CHARACTER_WAIT	15 //10 works. 20 works. 5 throws all sorts of retries, but will work.
#define MAX_WAIT_IN_CYCLES ( ((MAX_CHARACTER_WAIT * 8) * F_CPU) / BAUD )


#undef HANDSHAKE
//I have found that flow control is not really needed with this implementation of wireless bootloading.
// I agree with Nate but it never hurts so I added an ifdef.
//Adding flow control for wireless support
#define USE_HANDSHAKE 0
#if USE_HANDSHAKE
#define sbi(port_name, pin_number)   (port_name |= 1<<pin_number)
#define cbi(port_name, pin_number)   (port_name &= (uint8_t)~(1<<pin_number))
#else
#define sbi(port_name, pin_number)
#define cbi(port_name, pin_number)
#endif
#define CTS		2 //This is an input from the XBee. If low, XBee is busy - wait.
#define RTS		3 //This is an output to the XBee. If we're busy, pull line low to tell XBee not to transmit characters into ATmega's UART

#define TRUE	0
#define FALSE	1

//Status LED
#define LED_DDR  DDRB
#define LED_PORT PORTB

#define ESCAPE_CHAR	 0x7d			// since we're escaped, here is the XBee escape char
#define MSG_OFFSET	 12				// location in the packet of the start of MSG data
#define APIID_OFFSET 0				// location in the packet of the API id
#define XBADDR_OFFSET 1				// offset to 64 bit address so we can reply
#define XBADDR_DEST	  5				// offset into packet of 64 bit address
#define FRAMEID_OFFSET 1			// the incoming frame id
#define FRAMEID_DEST 19				// in the dest data
#define OK_LENGTH	21				// length of ok packet

//Function prototypes
void SendPacket(void);
void putch(char);
char getch(void);
void flash_led(uint8_t);
void onboard_program_write(uint32_t page, uint8_t *buf);

//Variables
uint8_t page_content[SPM_PAGESIZE];				// this comes first so it's at a page boundary
uint8_t incoming_page_data[256];
uint16_t page_content_length;
uint16_t page_length;
uint8_t bIsEscaped;
uint8_t retransmit_flag = FALSE;
uint8_t bPacketRFAck[OK_LENGTH] = { 0x7e, 0, (OK_LENGTH-4), 0x10, 0, 1, 2, 3, 4, 5, 6, 7, 8, 0xff, 0xfe, 0, 0, 'O', 'K', 0x01, 0xcc };	// 75, msb,lsb, apiid, frameid, xb64, xb16, maxhops, options, data, inframeid, checks
uint32_t page_address;

//******************************************************************************************
#if defined(__AVR_ATmega1280__) || defined(__AVR_ATmega2560__) || (defined AtMega2560)
void 	main_start()
{
	EIND = 0;
	SPCR = 0;
//*	May 23,	2010	<MLS> Modified app_start because it was not working on the 2560.
//*	Thanks to Peter Knight on the arduino.cc forum for this little trick
//*	http://www.arduino.cc/cgi-bin/yabb2/YaBB.pl?num=1271415071
    asm volatile(
		"clr	r30		\n\t"				
		"clr	r31		\n\t"				
		"eijmp	\n\t"				
		);
}

#else
void (*main_start)(void) = 0x0000;

#endif


// -------------------------------------------------------------------------------
//		Main
// -------------------------------------------------------------------------------
int main(void)
{
	uint8_t mcusr = MCUSR;
	GPIOR0 = mcusr;  // Save MCUSR so that main() can get to it
    MCUSR = 0;
	
	// disable watchdog timer
	wdt_reset();
	wdt_disable();
	
	//set LED pin as output
	LED_DDR |= (1<<LED);

	// Proceed with the bootloader only on external reset.
    if ((mcusr & 1<<EXTRF) == 0) {
	    main_start();
	}
			
	uint8_t check_sum = 0;
	uint16_t i;
	uint32_t count = 0;
	page_content_length = 0;
	
    UBRRxH = MYUBRR >> 8;
    UBRRxL = MYUBRR;
    UCSRxA = 0x00;
    UCSRxB = (1<<RXENx)|(1<<TXENx);
	UCSRxC = (1<<UCSZx1)|(1<<UCSZx0);

	//sbi(PORTD, RTS); //Tell XBee to hold serial characters, we are busy doing other things
	flash_led(3);	// signal we booted
	//cbi(PORTD, RTS); //Tell XBee it is now okay to send us serial characters

	while((UCSRxA & (1<<RXCx)) == 0)
	{
		count++;
		if (count > 30000) {
			main_start();
		} else {
			_delay_ms(.1);
		}
	}

	bIsEscaped = 0;
	page_address = 0;

	while(1)
	{
RESTART:

		while(1) // Wait for the computer to initiate transfer
		{
			if (getch() == 0x7E) 	// start of packet?
				break;				 //This is the "gimme the next chunk" command
			if (retransmit_flag == TRUE)
				goto RESTART;
		}
		getch();							// the msb of the length is always 0 for now
		if (retransmit_flag == TRUE)
			goto RESTART;
        page_length = getch(); 				// Get the length of this block
		if (retransmit_flag == TRUE)
			goto RESTART;
		if ( page_length == ESCAPE_CHAR)	// length may be escaped
			page_length = 0x20 ^ getch();

		if (retransmit_flag == TRUE)
			goto RESTART;

		// now read the data
		bIsEscaped = 0;
		for(i = 0 ; i < page_length ; i++) //Read the program data
		{
			if ( bIsEscaped)
			{
            	incoming_page_data[i] = 0x20 ^ getch();
				bIsEscaped = 0;
			}
			else
			{
            	incoming_page_data[i] = getch();
				if ( incoming_page_data[i] == ESCAPE_CHAR)
				{
					i--;
					bIsEscaped = 1;
				}
			}
			if (retransmit_flag == TRUE)
				goto RESTART;
		}
		if ( 0x90 != incoming_page_data[APIID_OFFSET])
			goto RESTART;			// not a packet we care about

		// now we should get the checksum
		check_sum = getch();
		if (retransmit_flag == TRUE)
			goto RESTART;
		if ( check_sum == ESCAPE_CHAR)
			check_sum = 0x20 ^ getch();
		if (retransmit_flag == TRUE)
			goto RESTART;

		// ------------- Output Packet setup
		// to prep the ACK we need to set the XB address and the checksum
		for( i=0; i<8; i++)
		{
			bPacketRFAck[i+XBADDR_DEST] = incoming_page_data[i+XBADDR_OFFSET];			// copy the xb address
		}
		bPacketRFAck[ FRAMEID_DEST] = incoming_page_data[FRAMEID_OFFSET];
		// get the checksum of the packet
		int iCheck = 0;
		for( i=3; i<(OK_LENGTH-1); i++)
		{
			iCheck = iCheck + bPacketRFAck[i];
		}
		iCheck = 0xff - iCheck;
		bPacketRFAck[OK_LENGTH-1] = iCheck;				// now the ACK is created


		// ------------- Sending
        //Calculate the checksum
		for(i = 0 ; i < page_length ; i++)
            check_sum = check_sum + incoming_page_data[i];

        if(check_sum == 0xff) 						// If we have a good transmission, put it in ink
		{
			if ( 'P' == incoming_page_data[MSG_OFFSET] && 'R' == incoming_page_data[MSG_OFFSET+1])
			{
				if ( page_content_length == 0)		// no content yet
					page_address = *(uint16_t *)&incoming_page_data[MSG_OFFSET+2];	// stored low byte first in the data
				int j;
				for( j=(4+MSG_OFFSET); j<page_length; j++)
				{
					page_content[page_content_length++] = incoming_page_data[j];
					if ( page_content_length == SPM_PAGESIZE)
					{
						flash_led(1);			// flash once to indicate write operation
			            onboard_program_write(page_address, page_content);
						page_content_length = 0;
						page_address += SPM_PAGESIZE;
					}
				}
				SendPacket();
			}
			else if ( 'E' == incoming_page_data[MSG_OFFSET] && 'N' == incoming_page_data[MSG_OFFSET+1])
			{
				if ( page_content_length > 0)
				{
						flash_led(1);			// flash once to indicate receipt
			            onboard_program_write(page_address, page_content);
						page_content_length = 0;
						page_address += SPM_PAGESIZE;
				}
				SendPacket();
				// the reboot will crash the send packet so wait a bit...
				_delay_ms(100);
				flash_led(2);
				main_start();			// we're done so start the program
			}
			else
			{
				flash_led(3);		// bad Command indicator
				_delay_ms(250);
				flash_led(3);		// bad Command indicator
			}
		}
		else
		{
			flash_led(2);		// bad Checksum indicator
			_delay_ms(250);
			flash_led(2);		// bad Checksum indicator
		}
	}
}

// -------------------------------------------------------------------------------
//		SendPacket
//			Send the OK packet to the caller
// -------------------------------------------------------------------------------
void SendPacket()
{
	uint8_t i;
	uint8_t iData;
	putch( bPacketRFAck[0]);			// send the leader
	for( i=1; i<OK_LENGTH; i++)
	{
		iData = bPacketRFAck[i];
		if ( iData == 0x11 || iData == 0x13 || iData == 0x7d || iData==0x7e)
		{
			putch( 0x7d);
			putch( 0x20 ^ iData);
		}
		else
			putch( iData);
	}
}

// -------------------------------------------------------------------------------
//		putch
//			Write a character to the serial port
// -------------------------------------------------------------------------------
void putch(char ch)
{
	//Adding flow control - xbee testing
#if HANDSHAKE
	while( (PIND & (1<<CTS)) != 0); //Don't send anything to the XBee, it is thinking
#endif

	while ((UCSRxA & (1<<UDREx)) == 0);
		UDRx = ch;
}

// -------------------------------------------------------------------------------
//		getch
//			Get a character from the serial port
// -------------------------------------------------------------------------------
char getch(void)
{
	retransmit_flag = FALSE;

	//Adding flow control - xbee testing
	//cbi(PORTD, RTS); //Tell XBee it is now okay to send us serial characters

	uint32_t count = 0;
	while((UCSRxA & (1<<RXCx)) == 0)
	{
		count++;
		if (count > MAX_WAIT_IN_CYCLES) //
		{
			retransmit_flag = TRUE;
			break;
		}
	}

	//Adding flow control - xbee testing
	//sbi(PORTD, RTS); //Tell XBee to hold serial characters, we are busy doing other things

	return UDRx;
}

// -------------------------------------------------------------------------------
//		flash_led
//			Blink the led on/off <count> times
// -------------------------------------------------------------------------------
void flash_led(uint8_t count)
{
	uint8_t i;

	for (i = 0; i < count; i++)
	{
		LED_PORT |= (1<<LED);
		_delay_ms(50);
		LED_PORT &= ~(1<<LED);
		_delay_ms(50);
	}
}

// -------------------------------------------------------------------------------
//		onboard_program_write
//			Write a page to the flash
// -------------------------------------------------------------------------------
void onboard_program_write(uint32_t page, uint8_t *buf)
{

	// Disable interrupts.
	uint8_t sreg = SREG;
	cli();
	
	EECR &= 1<<EEPE;


	boot_page_erase_safe(page);

	uint16_t i;
	for (i=0; i<SPM_PAGESIZE; i+=2)
	{
		// Set up little-endian word.
		uint16_t w = (uint8_t)*buf++;
		w += (*buf++) << 8;
		boot_page_fill_safe(page + i, w);
	}

	boot_page_write_safe(page);     // Store buffer in flash page.
    boot_rww_enable_safe();


	// Re-enable interrupts (if they were ever enabled).
	SREG = sreg;
}
