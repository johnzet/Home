package org.zehetner.homeautomation.mock;

import com.rapplogic.xbee.api.PacketListener;
import com.rapplogic.xbee.api.XBeeAddress64;
import org.zehetner.homeautomation.xbee.Transceiver;

/**
 * Created by IntelliJ IDEA.
 * User: johnzet
 * Date: 2/4/12
 * Time: 3:13 PM
 * To change this template use File | HvacSettings | File Templates.
 */
public class MockTransceiver implements Transceiver {

    private String payload;

    public String getPayload() {
        return payload;
    }

    @Override
    public void sendRequest(final XBeeAddress64 addr64, final String payloadArg) {
        this.payload = payloadArg;
    }

    @Override
    public void addPacketListener(final PacketListener packetListener) {
        //To change body of implemented methods use File | HvacSettings | File Templates.
    }

    @Override
    public void initTransceiver() {
        //To change body of implemented methods use File | HvacSettings | File Templates.
    }
}
