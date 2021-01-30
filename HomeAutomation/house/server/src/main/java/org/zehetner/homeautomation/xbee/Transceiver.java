package org.zehetner.homeautomation.xbee;

import com.rapplogic.xbee.api.PacketListener;
import com.rapplogic.xbee.api.XBeeAddress64;

/**
 * Created by IntelliJ IDEA.
 * User: johnzet
 * Date: 2/4/12
 * Time: 3:00 PM
 */
public interface Transceiver {

    void sendRequest(XBeeAddress64 address64, String payload);

    void addPacketListener(PacketListener packetListener);

    void initTransceiver();
}
