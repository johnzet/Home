package org.zehetner.homeautomation.xbee;

import com.rapplogic.xbee.api.*;
import com.rapplogic.xbee.api.zigbee.ZNetRxResponse;
import com.rapplogic.xbee.api.zigbee.ZNetTxRequest;
import com.rapplogic.xbee.util.ByteUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;

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
 * COORDINATOR config:
 *
 * - Reset to factory settings:
 * ATRE
 * - Put in API mode 2
 * ATAP 2
 * - Set PAN id to arbitrary value
 * ATID 1AAA
 * - Set the Node Identifier (give it a meaningful name)
 * ATNI COORDINATOR
 * - Save config
 * ATWR
 * - reboot
 * ATFR
 *
 * The XBee network will assign the network 16-bit MY address.  The coordinator MY address is always 0
 *
 * X-CTU tells me my SH Address is 00 13 a2 00 and SL is 40 0a 3e 02
 *
 * END DEVICE config:
 *
 * - Reset to factory settings:
 * ATRE
 * - Put in API mode 2
 * ATAP 2
 * - Set PAN id to arbitrary value
 * ATID 1AAA
 * - Set the Node Identifier (give it a meaningful name)
 * ATNI END_DEVICE_1
 * - Save config
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
public class XBeeTransceiver {

    private final static Logger LOG = Logger.getLogger(XBeeTransceiver.class);
    private XBee xbee = new XBee();

    public XBeeTransceiver() throws XBeeException {
    }

    public void open(final String comPort) throws XBeeException {
        xbee.open(comPort, 38400);
    }

    public void close() {
        xbee.close();
    }

    public void sendRequest(XBeeAddress64 addr64, int[] payload) throws XBeeException {

        ZNetTxRequest request = new ZNetTxRequest(addr64, payload);

        // update frame id for this request
        request.setFrameId(xbee.getNextFrameId());

        try {
            /*ZNetTxStatusResponse response = (ZNetTxStatusResponse)*/ xbee.sendSynchronous(request, 1000);

        } catch (XBeeTimeoutException e) {
            LOG.warn("request timed out");
        }
    }

    public String receiveResponseText() {
        for (int i=0; i<20; i++) {
            XBeeResponse response;
            try {
                response = xbee.getResponse(1000);
            } catch (XBeeException e) {
//                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                continue;
            }

            if (response.getApiId() == ApiId.ZNET_RX_RESPONSE) {
                if (response instanceof ErrorResponse) {
                    LOG.warn("error occurred getting xbee response: " + ((ErrorResponse) response).getErrorMsg(), ((ErrorResponse) response).getException());
                    continue;
                }
                // we received a packet from ZNetSenderTest.java
                ZNetRxResponse rx = (ZNetRxResponse) response;
                return ByteUtils.toString(rx.getData());
            }
        }
        return "";
    }

    public boolean receiveAtResponse() {
        for (int i=0; i<20; i++) {
            XBeeResponse response;
            try {
                response = xbee.getResponse(1000);
            } catch (XBeeException e) {
//                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                LOG.warn("Exception during remote AT command response", e);
                continue;
            }

            if (response.getApiId() == ApiId.REMOTE_AT_RESPONSE) {
                LOG.info("Got remote AT command response");
                return true;
            }
        }
        return false;
    }

    public boolean waitAckResponse() throws XBeeException {

        for (int i=0; i<20; i++) {
            XBeeResponse response = xbee.getResponse(1000);

            if (response.getApiId() == ApiId.ZNET_RX_RESPONSE) {
                if (response instanceof ErrorResponse) {
                    LOG.warn("error occurred getting xbee response: " + ((ErrorResponse) response).getErrorMsg(), ((ErrorResponse) response).getException());
                    return false;
                }
                // we received a packet from ZNetSenderTest.java
                ZNetRxResponse rx = (ZNetRxResponse) response;
                int[] resp = rx.getData();
                if (resp.length >= 2 && resp[0] == 'O' && resp[1] == 'K') {
                    return true;
                }
            }
        }
        return false;
    }

    public void resetTarget(XBeeAddress64 addr64) throws XBeeException, InterruptedException {
//        Thread.sleep(10000);
        RemoteAtRequest request = new RemoteAtRequest(addr64, "D3", new int[] {5});
        request.setFrameId(xbee.getNextFrameId());
        RemoteAtResponse response = (RemoteAtResponse) xbee.sendSynchronous(request, 20000);

        this.receiveAtResponse();
        if (response.isOk()) {
            // success
        } else {
            String msg = "target reset has failed";
            System.err.println(msg);
            LOG.warn(msg);
            return;
        }

        Thread.sleep(1000);

        request = new RemoteAtRequest(addr64, "D3", new int[] {4});
        request.setFrameId(xbee.getNextFrameId());
        response = (RemoteAtResponse) xbee.sendSynchronous(request, 20000);

        this.receiveAtResponse();
        if (response.isOk()) {
            // success
        } else {
            String msg = "target reset has failed";
            System.err.println(msg);
            LOG.warn(msg);
            return;
        }

        Thread.sleep(200);

        request = new RemoteAtRequest(addr64, "D3", new int[] {5});
        request.setFrameId(xbee.getNextFrameId());
        response = (RemoteAtResponse) xbee.sendSynchronous(request, 20000);

        this.receiveAtResponse();
        if (response.isOk()) {
            // success
            LOG.info("target has been reset");
            String msg = "target has been reset";
            System.out.println(msg);
        } else {
            String msg = "target reset has failed";
            System.err.println(msg);
            LOG.warn(msg);
        }

        Thread.sleep(1000);
    }

    public void printNodeIdentifiers() throws XBeeException, IOException {
        AtCommand cmd = new AtCommand("ND");
        cmd.setFrameId(xbee.getNextFrameId());
        xbee.sendSynchronous(cmd, 1000);

        boolean breakLoop = false;
        do {
            try {
                XBeeResponse response = xbee.getResponse(30000);
                if (response instanceof AtCommandResponse) {
                    printDiscoveredNode(response.getProcessedPacketBytes());
                }
            }
            catch (XBeeTimeoutException e) {
                breakLoop = true;
            }
        } while (! breakLoop);
    }

    private void printDiscoveredNode(int[] bytes) {
        final ArrayList niList = new ArrayList<Integer>();
        int index = 17;
        do {
            niList.add(bytes[index++]);
        } while (index < bytes.length && bytes[index] != 0);
        final int[] niArray = new int[niList.size()];
        for (int i=0; i<niList.size(); i++) {
            niArray[i] = (Integer)niList.get(i);
        }

        final String ni = ByteUtils.toString(niArray);

        System.out.printf("%02X%02X%02X%02X %02X%02X%02X%02X  %s\n", bytes[9], bytes[10], bytes[11], bytes[12], bytes[13], bytes[14], bytes[15], bytes[16], ni);
    }
}

