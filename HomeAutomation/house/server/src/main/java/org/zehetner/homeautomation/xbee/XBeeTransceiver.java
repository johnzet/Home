package org.zehetner.homeautomation.xbee;

import com.rapplogic.xbee.api.PacketListener;
import com.rapplogic.xbee.api.XBee;
import com.rapplogic.xbee.api.XBeeAddress64;
import com.rapplogic.xbee.api.XBeeException;
import com.rapplogic.xbee.api.zigbee.ZNetTxRequest;
import com.rapplogic.xbee.util.ByteUtils;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * To run this example you need to have at least two ZNet XBees powered up and configured to the same PAN ID (ATID) in API mode (2).
 * This software requires the XBee to be configured in API mode; if your ZNet radios are flashed with the transparent (AT) firmware,
 * you will need to re-flash with API firmware to run this software.
 *
 * I use the Digi X-CTU software to configure my XBee's, but if you don't have Windows (X-CTU only works on Windows), you can still use the configureCoordinator and
 * configureEndDevice methods in ZNetApiAtTest.java.
 *
 * There are a few chicken and egg situations where you need to know some basic configuration before you can connect to the XBee.  This
 * includes the baud rate and the API mode.  The default baud rate is 9600 and if you ever change it, you will want to remember the setting.
 * If you can't connect at 9600 and you don't know the baud rate, try all possibilities until it works.  Same with the API Mode: if you click
 * Test/Query in X-CTU, try changing the API mode until it succeeds, then write it down somewhere for next time.
 *
 * Here's my setup configuration (assumes factory configuration):
 *
 * COORDINATOR common:
 *
 * - Reset to factory settings:
 * ATRE
 * - Put in API mode 2
 * ATAP 2
 * - Set PAN id to arbitrary value
 * ATID 1AAA
 * - Set the Node Identifier (give it a meaningful name)
 * ATNI COORDINATOR
 * - Save common
 * ATWR
 * - reboot
 * ATFR
 *
 * The XBee network will assign the network 16-bit MY address.  The coordinator MY address is always 0
 *
 * X-CTU tells me my SH Address is 00 13 a2 00 and SL is 40 0a 3e 02
 *
 * END DEVICE common:
 *
 * - Reset to factory settings:
 * ATRE
 * - Put in API mode 2
 * ATAP 2
 * - Set PAN id to arbitrary value
 * ATID 1AAA
 * - Set the Node Identifier (give it a meaningful name)
 * ATNI END_DEVICE_1
 * - Save common
 * ATWR
 * - reboot
 * ATFR
 *
 * Only one XBee needs to be connected to the computer (serial-usb); the other may be remote, but can also be connected to the computer.
 * I use the XBee Explorer from SparkFun to connect my XBees to my computer as it makes it incredibly easy -- just drop in the XBee.
 *
 * For this example, I use my XBee COORDINATOR as my "sender" (runs this class) and the END DEVICE as my "receiver" XBee.
 * You could alternatively use your END DEVICE as the sender -- it doesn't matter because any XBee, either configured as a COORDINATOR
 * or END DEVICE, can both send and receive.
 *
 * How to find the COM port:
 *
 * Java is nice in that it runs on many platforms.  I use mac/windows and linux (server) and the com port is different on all three.
 * On the mac it appears as /dev/tty.usbserial-A6005v5M on my machine.  I just plug in each XBee one at a time and check the /dev dir
 * to match the XBee to the device name: ls -l /dev/tty.u (hit tab twice to see all entries)
 *
 * On Windows you can simply select Start->My Computer->Manage, select Device Manager and expand "Ports"
 *
 * For Linux I'm not exactly sure just yet although I found mine by trial and error to be /dev/ttyUSB0  I think it could easily be different
 * for other distros.
 *
 * To run, simply right-click on the class, in the left pane, and select Run As->Java Application.  Eclipse will let you run multiple
 * processes in one IDE, but there is only one console and it will switch between the two processes as it is updated.
 *
 * If you are running the sender and receiver in the same eclipse, remember to hit the terminate button twice to kill both
 * or you won't be able to start it again.  If this situation occurs, simply restart eclipse.
 *
 * @author andrew
 */
public class XBeeTransceiver implements Transceiver {

    private static final Logger LOG = Logger.getLogger(XBeeTransceiver.class);
    private final XBee xbee = new XBee();
    public static final String GATEWAY_XBEE_ADDRESS_PROP = "gatewayXbeeAddress";
    public static final String THERMOSTAT_1_XBEE_ADDRESS_PROP = "thermostat1XbeeAddress";
    public static final String THERMOSTAT_2_XBEE_ADDRESS_PROP = "thermostat2XbeeAddress";
    public static final String GATEWAY_XBEE_NAME_PROP = "gatewayXbeeName";
    public static final String THERMOSTAT_1_XBEE_NAME_PROP = "thermostat1XbeeName";
    public static final String THERMOSTAT_2_XBEE_NAME_PROP = "thermostat2XbeeName";

    public static final String HVAC_RELAY_BOARD_COMMAND = "SET_HVAC_RELAYS";
    public static final String SPRINKLER_RELAY_BOARD_COMMAND = "SET_SPRINKLER_RELAYS";

    @Override
    public void initTransceiver() {
        synchronized (XBeeTransceiver.class) {
            if (! this.isOpen()) {
                try {
                    final String comPort = Config.getComPort();
                    this.open(comPort);
                } catch (XBeeException e) {
                    LOG.fatal("Failed to open XBee connection", e);
                }
            }
        }
    }

    private void open(final String comPort) throws XBeeException {
        synchronized (XBeeTransceiver.class) {
            this.xbee.open(comPort, 38400);
        }
    }

    public boolean isOpen() {
        synchronized (XBeeTransceiver.class) {
            return this.xbee.isConnected();
        }
    }

    @Override
    public  void sendRequest(final XBeeAddress64 address64, final String payload) {
        synchronized (XBeeTransceiver.class) {
            final ZNetTxRequest request = new ZNetTxRequest(address64, ByteUtils.stringToIntArray(payload));

            // update frame id for this request
            request.setFrameId(this.xbee.getNextFrameId());

            LOG.info("sending tx " + request);

            LOG.info("request packet bytes count " + request.getXBeePacket().getByteArray().length);
            LOG.info("request packet bytes (base 16) " + ByteUtils.toBase16(request.getXBeePacket().getByteArray()));

            try {
                this.xbee.sendRequest(request);
            } catch (IOException e) {
                LOG.warn("exception sending xbee request: " + request.toString(), e);
            }
        }
    }

    @Override
    public void addPacketListener(final PacketListener packetListener) {
        synchronized (XBeeTransceiver.class) {
            this.xbee.addPacketListener(packetListener);
        }
    }
}

