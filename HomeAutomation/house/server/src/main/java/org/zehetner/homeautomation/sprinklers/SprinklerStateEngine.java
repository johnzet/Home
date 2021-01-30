package org.zehetner.homeautomation.sprinklers;


import org.apache.log4j.Logger;
import org.zehetner.homeautomation.Worker;
import org.zehetner.homeautomation.common.Manager;
import org.zehetner.homeautomation.stateengine.Program;
import org.zehetner.homeautomation.stateengine.ProgramSet;
import org.zehetner.homeautomation.stateengine.SprinklerProgram;

import java.util.Date;

public class SprinklerStateEngine implements Worker {
    private static final Logger LOG = Logger.getLogger(SprinklerStateEngine.class.getName());
    private static final long CALCULATION_INTERVAL = 5000L;
    private Thread stateEngineThread = null;
    private final SprinklerMechanical sprinklerMechanical;

    private Zone onDemandActivatedZone = Zone.ALL_OFF;
    private Date onDemandStartTime = null;
    private static final long ON_DEMAND_MAX_ON_TIME = 10 * 60 * 1000;  // 10 minutes

    public SprinklerStateEngine(final SprinklerMechanical sprinklerMechanicalArg) {
        this.sprinklerMechanical = sprinklerMechanicalArg;
    }

    public void calculateState() {
        LOG.debug("At the top of Sprinkler calculateState.");

        this.sprinklerMechanical.fixRelayStateConsistency();

        if (this.onDemandActivatedZone != Zone.ALL_OFF) {
            if (Manager.getDateNow().getTime() - this.onDemandStartTime.getTime() > ON_DEMAND_MAX_ON_TIME) {
                this.onDemandActivatedZone = Zone.ALL_OFF;
            }
            this.sprinklerMechanical.setZoneOn(this.onDemandActivatedZone);
        } else {

            final ProgramSet programSet = Manager.getSingleton().getProgramSet();
            Zone winningZone = Zone.ALL_OFF;
            for (final Program program : programSet.getPrograms()) {
                if (program instanceof SprinklerProgram) {
                    final SprinklerProgram sprinklerProgram = (SprinklerProgram)program;
                    final Zone zone = sprinklerProgram.getActiveZone();
                    if (zone != Zone.ALL_OFF) {
                        winningZone = zone;
                        sprinklerProgram.setRecentActivity();
                    }
                }
            }
            this.sprinklerMechanical.setZoneOn(winningZone); // last one wins
        }
	}

    @Override
    public void start() {
        this.stateEngineThread = new Thread(new SprinklerStateEngine.Engine());
        this.stateEngineThread.start();
        LOG.info("Started Sprinkler State Engine thread");
    }

    @Override
    public void junitSetLoopDelay(final long delayMs) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void stop() {
        if (this.stateEngineThread != null) {
            this.stateEngineThread.interrupt();
        }
        LOG.info("Stopped Sprinkler State Engine thread");
    }

    /*
    * This should be called through the UI to set sprinkler state temporarily
     */
    public void setOnDemandZoneState(final Zone zone) {
        this.onDemandActivatedZone = zone;
        this.onDemandStartTime = Manager.getDateNow();
    }

    public boolean isZoneOn(final Zone zone) {
        return (this.sprinklerMechanical.isZoneOn(zone));
    }

    private class Engine implements Runnable {
        @Override
        public void run() {
            boolean keepLooping = true;
            do {
                LOG.debug("At the top of the SprinklerStateEngine.Engine loop.");
                calculateState();
                try {
                    Thread.sleep(CALCULATION_INTERVAL);
                } catch (InterruptedException e) {
                    LOG.info("SprinklerStateEngine thread normal exit");
                    keepLooping = false;
                } catch (Throwable t) {
                    LOG.warn("SprinklerStateEngine thread exception caught: ", t);
                }
            } while (keepLooping);
        }
    }
}
