package org.zehetner.homeautomation.sprinklers;


import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.zehetner.homeautomation.Worker;
import org.zehetner.homeautomation.common.Manager;
import org.zehetner.homeautomation.stateengine.Program;
import org.zehetner.homeautomation.stateengine.ProgramSet;
import org.zehetner.homeautomation.stateengine.SprinklerProgram;

public class SprinklerStateEngine extends Thread implements Worker {
    private static final Logger LOG = Logger.getLogger(SprinklerStateEngine.class.getName());
    private long CALCULATION_INTERVAL = 5000L;
    protected final SprinklerMechanical sprinklerMechanical;

    private Zone onDemandActivatedZone = Zone.ALL_OFF;
    private DateTime onDemandStartTime = null;
    private static final Duration ON_DEMAND_MAX_ON_TIME = new Duration(20 * 60 * 1000);  // 20 minutes
    private boolean threadRunEnabled = true;

    public SprinklerStateEngine(final SprinklerMechanical sprinklerMechanicalArg) {
        this.sprinklerMechanical = sprinklerMechanicalArg;
    }

    public void setCalculationInterval(final long interval) {
        this.CALCULATION_INTERVAL = interval;
    }

    public void calculateState() {
        LOG.debug("At the top of SprinklerStateEngine.calculateState()");

        Zone winningZone = Zone.ALL_OFF;
        if (this.onDemandActivatedZone != Zone.ALL_OFF) {
            if (this.onDemandStartTime == null) this.onDemandStartTime = new DateTime().minusYears(1);
            Duration runTime = new Duration(this.onDemandStartTime.toInstant(), Manager.getDateNow().toInstant());
            if (runTime.isLongerThan(ON_DEMAND_MAX_ON_TIME)) {
                this.onDemandActivatedZone = Zone.ALL_OFF;
            }
            winningZone = this.onDemandActivatedZone;
        } else {

            final ProgramSet programSet = Manager.getSingleton().getProgramSet();
            for (final Program program : programSet.getPrograms()) {
                if (program instanceof SprinklerProgram) {
                    final SprinklerProgram sprinklerProgram = (SprinklerProgram)program;
                    final Zone zone = sprinklerProgram.getActiveZone();
                    if (zone != Zone.ALL_OFF) {
                        winningZone = zone;
                    } else {
                        if (this.sprinklerMechanical.getActiveZone() != Zone.ALL_OFF) {
                            ((SprinklerProgram) program).setRecentActivity();
                        }
                    }
                }
            }
        }
        if (winningZone != this.sprinklerMechanical.getActiveZone()) {
            LOG.info("SprinklerStateEngine.calculateState() starting zone: " + winningZone);
        } else {
            LOG.debug("SprinklerStateEngine.calculateState() starting zone: " + winningZone);
        }
        this.sprinklerMechanical.setZoneOn(winningZone); // last one wins
	}

    public void junitSetLoopDelay(final long delayMs) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void junitSetRunEnabled(final boolean enabled) {
        this.threadRunEnabled = enabled;
    }


    /*
    * This should be called through the UI to set sprinkler state temporarily
     */
    public void setOnDemandZoneState(final Zone zone) {
        LOG.info("Activating zone " + zone);

        this.onDemandActivatedZone = zone;
        this.onDemandStartTime = Manager.getDateNow();
    }

    public void junitClearOnDemandZoneState() {
        this.onDemandActivatedZone = Zone.ALL_OFF;
        this.onDemandStartTime = Manager.getDateNow().minusYears(1);
    }

    /*
    * This is called by junit tests
     */
    public Zone getOnDemandZoneState() {
        return this.onDemandActivatedZone;
    }

    public void run() {
        boolean keepLooping = true;
        int divisor = 0;
        LOG.info("Starting the SprinklerStateEngine thread");
        do {
            LOG.debug("At the top of the SprinklerStateEngine loop.");
            try {
                if (this.threadRunEnabled) {
                    if (divisor++ > 10) {
                        LOG.debug("Checking relay state consistency.");
                        this.sprinklerMechanical.checkRelayStateConsistency();
                        divisor = 0;
                    }
                    calculateState();
                }
                Thread.sleep(CALCULATION_INTERVAL);
            } catch (InterruptedException e) {
                LOG.info("SprinklerStateEngine thread normal exit");
                keepLooping = false;
            } catch (Throwable t) {
                LOG.warn("Unknown exception in SprinklerStateEngine: " + t.toString());
            }
        } while (keepLooping);
        LOG.info("SprinklerStateEngine thread done normal exit");
    }
}
