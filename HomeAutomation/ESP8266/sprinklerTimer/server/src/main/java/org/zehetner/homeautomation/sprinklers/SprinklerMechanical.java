package org.zehetner.homeautomation.sprinklers;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.zehetner.homeautomation.common.CombinedProperties;
import org.zehetner.homeautomation.common.Manager;

/**
 * Created by IntelliJ IDEA.
 * User: johnz
 * Date: 12/21/11
 * Time: 11:17 PM
 */
public class SprinklerMechanical {
    private static final Logger LOG = Logger.getLogger(SprinklerMechanical.class.getName());
    private SprinklerHttpComm comm = new SprinklerHttpComm();
    private Zone activeZone = Zone.ALL_OFF;

    public void setSprinklerHttpComm(final SprinklerHttpComm mockComm) {
        this.comm = mockComm;
    }

    public SprinklerHttpComm getSprinklerHttpComm() {
        return this.comm;
    }

    public Zone getActiveZone() {
        return this.activeZone;
    }

    void setZoneOn(final Zone zone) {
        if (this.activeZone == zone) {
            return;
        }
        LOG.info("Set " + zone.getName() + " true in SprinklerMechanical");
        this.activeZone = zone;

        updateRelayBoard();
    }

    protected void updateRelayBoard() {
        sendRelayStateRequest(this.activeZone.getPhysicalRelayNumber());
    }

    private void sendRelayStateRequest(final int relayNumber) {
        final CombinedProperties properties = Manager.getSingleton().getProperties();
        final String hostName = properties.getSystemProperty(CombinedProperties.SPRINKLERS_HOST_NAME);
        final String targetUrl = "HTTP://" + hostName + "/on?zone=" + relayNumber;
        this.comm.executeGet(targetUrl, null);
        LOG.info("Setting Sprinkler relay state to " + relayNumber);
    }

    private Zone getRelayState() {
        int relayState = 0x00;
        try {
            final CombinedProperties properties = Manager.getSingleton().getProperties();
            final String hoseName = properties.getSystemProperty(CombinedProperties.SPRINKLERS_HOST_NAME);
            final String targetUrl = "HTTP://" + hoseName + "/getall";
            final String jsonResponseStr = this.comm.executeGet(targetUrl, null);
            final JSONObject json = new JSONObject(jsonResponseStr);
            int s = json.getInt("state");
            LOG.debug("****** Relay state bit field received = " + s);


            for (int pos = 0; pos < 8; pos++) {
                if (s == (Math.pow(2, pos))) {
                    relayState = pos+1;
                }
            }
            LOG.debug("****** Returning Relay state zone = " + relayState);
        } catch (JSONException e) {
            LOG.error("JSON parse exception:  " + e.getMessage());
        } catch (Throwable t) {
            LOG.warn("Unknown exception while asking relays for their state: " + t.toString());
        }

        LOG.info("Received sprinkler relay state " + relayState);
        return Zone.values()[relayState];
    }

    public void checkRelayStateConsistency() {
        LOG.debug("Checking relay state");
        final Zone actualActiveZone = getRelayState();
        if (this.activeZone != actualActiveZone) {
            LOG.warn("Relay state is inconsistent - trying to fix...");
            sendRelayStateRequest(this.activeZone.ordinal());
        }
    }

    boolean isZoneOn(final Zone zone) {
        return (this.activeZone == zone);
    }
}
