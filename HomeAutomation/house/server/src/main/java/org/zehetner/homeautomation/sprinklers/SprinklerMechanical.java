package org.zehetner.homeautomation.sprinklers;

import com.rapplogic.xbee.api.PacketListener;
import com.rapplogic.xbee.api.XBeeAddress64;
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.api.zigbee.ZNetRxResponse;
import com.rapplogic.xbee.util.ByteUtils;
import org.apache.log4j.Logger;
import org.zehetner.homeautomation.common.CombinedProperties;
import org.zehetner.homeautomation.common.Manager;
import org.zehetner.homeautomation.common.XbeeCommandName;
import org.zehetner.homeautomation.xbee.Transceiver;
import org.zehetner.homeautomation.xbee.XBeeTransceiver;

/**
 * Created by IntelliJ IDEA.
 * User: johnz
 * Date: 12/21/11
 * Time: 11:17 PM
 */
public class SprinklerMechanical implements PacketListener {
    private static final Logger LOG = Logger.getLogger(SprinklerMechanical.class.getName());

    private Zone activeZone = Zone.ALL_OFF;

    private Transceiver transceiver = null;
    private boolean isRelayStateConsistent = true;

    public void setTransceiver(final Transceiver transceiverArg) {
        this.transceiver = transceiverArg;
    }

    public Zone getActiveZone() {
        return this.activeZone;
    }

    void setZoneOn(final Zone zone) {
        if (this.isRelayStateConsistent && this.activeZone == zone) {
            return;
        }
        LOG.info("Set " + zone.getName() + " true in SprinklerMechanical");
        this.activeZone = zone;

        updateRelayBoard();

        this.isRelayStateConsistent = true;
    }

    protected void updateRelayBoard() {
        sendRelayStateRequest(getRelayStateByte());
    }

    private int getRelayStateByte() {
        return (Zone.ALL_OFF == this.activeZone)?
                0x0
              : (0x1 << (this.activeZone.getPhysicalRelayNumber() - 1));
    }

    private void sendRelayStateRequest(final int stateByte) {
        final CombinedProperties properties = Manager.getSingleton().getProperties();
        final XBeeAddress64 address64 = new XBeeAddress64(properties.getSystemProperty(XBeeTransceiver.GATEWAY_XBEE_ADDRESS_PROP));
        final String payload = XbeeCommandName.SprinklerRelayState + " " + Integer.toString(stateByte);
        this.transceiver.sendRequest(address64, payload);
        LOG.info("Setting Sprinkler relay state to " + ByteUtils.toBase16(stateByte));
    }



    @Override
    public void processResponse(final XBeeResponse response) {
        try {
            if (response instanceof ZNetRxResponse) {
                final ZNetRxResponse zNetRxResponse = (ZNetRxResponse)response;
                final String rxData = ByteUtils.toString(zNetRxResponse.getData());
                if (rxData.startsWith(XbeeCommandName.SprinklerRelayState.name())) {
                    LOG.info("Gateway responded with relay state " + rxData);
                    final int relayState = Integer.parseInt(rxData.split(" ")[1]);
                    this.isRelayStateConsistent = checkRelayStateConsistency(relayState) ;
                    LOG.info("Is Gateway's relay state consistent: " + this.isRelayStateConsistent);
                }
            }
        }
        catch (Throwable t) {
            LOG.warn("Exception retrieving hvac relay state", t);
        }
    }

    public void fixRelayStateConsistency() {
        if (! isRelayStateCorrect()) {
            setZoneOn(this.activeZone);
        }
    }

    private boolean isRelayStateCorrect() {
        if (!this.isRelayStateConsistent) {
            return false;
        }
        return isZoneOn(this.activeZone);
    }

    private boolean checkRelayStateConsistency(final int receivedRelayState) {
        final int expectedRelayState = getRelayStateByte();
        if (expectedRelayState != receivedRelayState) {
            LOG.warn("relay state is inconsistent");
            return false;
        }
        return true;
    }

    boolean isZoneOn(final Zone zone) {
        return (this.activeZone == zone);
    }
}
