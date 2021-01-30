package org.zehetner.homeautomation.mock;

import org.zehetner.homeautomation.sprinklers.SprinklerHttpComm;
import org.zehetner.homeautomation.sprinklers.Zone;

/**
 * Created by IntelliJ IDEA.
 * User: johnz
 * Date: 3/12/2016
 * Time: 12:58 PM
 */
public class MockSprinklerHttpComm  extends SprinklerHttpComm {
    private int currentZone = 0;

    @Override
    public String executeGet(final String targetURL, final String postRequestData) {
        if (targetURL.contains("/getall")) {
            return "{state: " + this.currentZone + '}';
        }
        if (targetURL.contains("/on?zone")) {
            final int index = targetURL.indexOf("/on?zone");
            this.currentZone = Integer.parseInt(targetURL.substring(index+9, index+10));
        }
        return "";
    }

    public void junitSetActiveZone(Zone zone) {
        this.currentZone = zone.getPhysicalRelayNumber();
    }

    public Zone junitGetActiveZone() {
        return Zone.getZone(currentZone);
    }
}
