package org.zehetner.homeautomation.stateengine;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.zehetner.homeautomation.sprinklers.Zone;

/**
 * Created by IntelliJ IDEA.
 * User: johnz
 * Date: 5/19/12
 * Time: 6:40 PM
 */
@XStreamAlias("SprinklerAction")
public class SprinklerAction extends Action {
    private final int durationMinutes;
    private final Zone zone;

    public SprinklerAction(final int durationMinutesArg, final Zone zoneArg) {
        this.durationMinutes = durationMinutesArg;
        this.zone = zoneArg;
    }

    public int getDurationMinutes() {
        return this.durationMinutes;
    }

    public Zone getZone() {
        return this.zone;
    }
}
