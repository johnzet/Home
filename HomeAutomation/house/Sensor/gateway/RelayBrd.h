/*

Devantech RLY08 Relay Board

I2C register set

register    Read                Write
0           Software version    Command register
1           Relay states        Relay states

Commands for I2C

decimal hex     command

100     0x64    All relays on
101     0x65    Turn relay 1 on
102     0x66    Turn relay 2 on
103     0x67    Turn relay 3 on
104     0x68    Turn relay 4 on
105     0x69    Turn relay 5 on
106     0x6A    Turn relay 6 on
107     0x6B    Turn relay 7 on
108     0x6C    Turn relay 8 on
110     0x6E    All relays off
111     0x6F    Turn relay 1 off
112     0x70    Turn relay 2 off
113     0x71    Turn relay 3 off
114     0x72    Turn relay 4 off
115     0x73    Turn relay 5 off
116     0x74    Turn relay 6 off
117     0x75    Turn relay 7 off
118     0x76    Turn relay 8 off




*/


#ifndef RelayBrd_H
#define RelayBrd_H

#include <inttypes.h>

class RelayBrdClass
{
  private:

  public:
	uint8_t readState(uint8_t address);
	void setState(uint8_t address, uint8_t state);
	void changeAddress(uint8_t newAddress);
};

extern RelayBrdClass RelayBrd;

#endif
