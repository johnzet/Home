package org.zehetner.homeautomation.stateengine;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: johnz
 * Date: 5/19/12
 * Time: 3:09 PM
 */
public class DailyAction extends Action {
    public Date startTime = null;

    public Date getStartTime() {
        return this.startTime;
    }

    public void setStartTime(final Date startTimeArg) {
        this.startTime = startTimeArg;
    }

}
