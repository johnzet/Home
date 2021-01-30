package org.zehetner.homeautomation.hvac;

import com.rapplogic.xbee.api.PacketListener;
import com.rapplogic.xbee.api.XBeeAddress64;
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.api.zigbee.ZNetRxResponse;
import com.rapplogic.xbee.util.ByteUtils;
import org.apache.log4j.Logger;
import org.zehetner.homeautomation.Worker;
import org.zehetner.homeautomation.common.CombinedProperties;
import org.zehetner.homeautomation.common.Manager;
import org.zehetner.homeautomation.common.XbeeCommandName;
import org.zehetner.homeautomation.xbee.Transceiver;
import org.zehetner.homeautomation.xbee.XBeeTransceiver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Created by IntelliJ IDEA.
 * User: johnz
 * Date: 12/21/11
 * Time: 11:17 PM
 */
public class HvacMechanical implements Worker, PacketListener {
    private static final Logger LOG = Logger.getLogger(HvacMechanical.class.getName());

    public static final String HEAT_MIN_TIME_ON = "heatMinTimeOn";
    public static final String HEAT_MIN_TIME_OFF = "heatMinTimeOff";
    public static final String COOL_MIN_TIME_ON = "coolMinTimeOn";
    public static final String COOL_MIN_TIME_OFF = "coolMinTimeOff";

    private Date lastHeatTimeOn = null;
    private Date lastHeatTimeOff = null;
    private Date lastCoolTimeOn = null;
    private Date lastCoolTimeOff = null;
    private long heatMinTimeOn = 0L;
    private long heatMinTimeOff = 0L;
    private long coolMinTimeOn = 0L;
    private long coolMinTimeOff = 0L;

    private final Map<Equipment, HvacMechanical.HvacEquipment> hvacEquipments = Collections.synchronizedMap(new EnumMap<Equipment, HvacMechanical.HvacEquipment>(Equipment.class));
    private final Map<Equipment, HvacMechanical.HvacEquipment> proposedChanges = Collections.synchronizedMap(new EnumMap<Equipment, HvacMechanical.HvacEquipment>(Equipment.class));

    private Date lastProposedChangeTime = new Date(0L);
    private volatile Thread delayThread = null;
    private boolean isRelayStateConsistent = true;

    private long junitLoopDelay = 0L;

    private Transceiver transceiver = null;

    public synchronized void setTransceiver(final Transceiver transceiverArg) {
        this.transceiver = transceiverArg;
    }

    public long getTimeOn(final Equipment eqt) {
        final long now = Manager.getDateNow().getTime();
        if (eqt == null || eqt == Equipment.ALL_OFF) {
            return 0L;
        }
        if (eqt == Equipment.COOL_1 || eqt == Equipment.COOL_2) {
            return this.isOn(eqt) ? now - this.lastCoolTimeOff.getTime() : 0L;
        }
        if (eqt == Equipment.HEAT_1 || eqt == Equipment.HEAT_2) {
            return this.isOn(eqt)? now - this.lastHeatTimeOff.getTime() : 0L;
        }
        return 0L;
    }

    @Override
    public void start() {
        init();
        this.delayThread = new Thread(new HvacMechanical.Delay());
        this.delayThread.setName(HvacMechanical.class.getName());
        this.delayThread.start();
        LOG.info("Started HVAC Mechanical thread");
    }

    @Override
    public void stop() {
        if (this.delayThread != null) {
            this.delayThread.interrupt();
            LOG.info("Stopped HVAC Mechanical thread");
        }
    }

    public synchronized void init() {
        if (! this.hvacEquipments.isEmpty()) {
            return;
        }
        final CombinedProperties properties = Manager.getSingleton().getProperties();

        this.heatMinTimeOn = Long.parseLong(properties.getSystemProperty(HvacMechanical.HEAT_MIN_TIME_ON));
        this.heatMinTimeOff = Long.parseLong(properties.getSystemProperty(HvacMechanical.HEAT_MIN_TIME_OFF));
        this.coolMinTimeOn = Long.parseLong(properties.getSystemProperty(HvacMechanical.COOL_MIN_TIME_ON));
        this.coolMinTimeOff = Long.parseLong(properties.getSystemProperty(HvacMechanical.COOL_MIN_TIME_OFF));

        for (final Equipment e : Equipment.values()) {
            this.hvacEquipments.put(e, new HvacMechanical.HvacEquipment(e));
        }

        LOG.info("Inited HVAC Mechanical");
    }

    public static long getMechanicalDelay() {
        final CombinedProperties properties = Manager.getSingleton().getProperties();
        return Long.parseLong(properties.getSystemProperty(CombinedProperties.MECHANICAL_DELAY));
    }

    private boolean isTimePassed(final Date lastEventTime, final long minPauseTime) {
        return lastEventTime == null || (Manager.getDateNow().getTime() - lastEventTime.getTime()) > minPauseTime;
    }

    boolean isOn(final Equipment equipment) {
        return this.hvacEquipments.get(equipment).isOn();
    }

    synchronized void setOnByProposal(final Equipment equipment, final boolean state) {

        if (isChanged(equipment, state)) {
            setLastProposedChangeTime(Manager.getDateNow());
            final HvacMechanical.HvacEquipment hvacEqt = new HvacMechanical.HvacEquipment(equipment);
            hvacEqt.setOn(state);
            this.proposedChanges.put(equipment, hvacEqt);
            LOG.info("Set " + hvacEqt.getEquipment().getName() + ' ' + state + " in HvacMechanical");
        }

    }

    synchronized boolean isOnByProposal(final Equipment equipment) {
        HvacEquipment hvacEqt = this.proposedChanges.get(equipment);
        if (hvacEqt != null) {
            return hvacEqt.isOn();
        }
        return isOn(equipment);
    }

    private synchronized boolean isChanged(final Equipment equipment, final boolean state) {
        final boolean currentState = isOn(equipment);
        if (this.proposedChanges.isEmpty()) {
            return currentState != state;
        }

        final HvacMechanical.HvacEquipment lastProposedChange = this.proposedChanges.get(equipment);
        if (lastProposedChange == null) {
            return currentState != state;
        }
        return lastProposedChange.isOn() != state;
    }

    protected synchronized void updateRelayBoard() {
        if (this.proposedChanges.isEmpty() && this.isRelayStateConsistent) {
            return;
        }

        final Iterator<HvacMechanical.HvacEquipment> iterator = this.proposedChanges.values().iterator();
        final List<Equipment> removeList = new ArrayList<Equipment>(3);
        while (iterator.hasNext()) {
            final HvacMechanical.HvacEquipment newEquipmentState = iterator.next();
            LOG.debug("updateRelayBoard() - proposedChanges contains " + newEquipmentState.toString());
            if (hasEnoughTimePassed(newEquipmentState)) {
                LOG.debug("updateRelayBoard() - proposedChanges contains " + newEquipmentState.toString()
                    + " enough time has passed - proceeding to set relay state.");


                this.hvacEquipments.get(newEquipmentState.getEquipment()).setOn(newEquipmentState.isOn());

                removeList.add(newEquipmentState.getEquipment());
            }
        }
        for (final Equipment eqt : removeList) {
            this.proposedChanges.remove(eqt);
        }

        int stateByte = 0;
        for (final HvacMechanical.HvacEquipment hvacEqt : this.hvacEquipments.values()) {
            final int relayNumber = hvacEqt.getEquipment().getPhysicalRelayNumber();
            final boolean relayState = hvacEqt.isOn();

            if (relayState && relayNumber > 0) {
                stateByte += 1 << (relayNumber-1);
            }
        }

        sendRelayStateRequest(stateByte);
    }

    private synchronized void recordLastActivationTimes() {
        if (this.isOn(Equipment.HEAT_1) || this.isOn(Equipment.HEAT_2)) {
            this.lastHeatTimeOn = Manager.getDateNow();
        } else {
            this.lastHeatTimeOff = Manager.getDateNow();
        }

        if (this.isOn(Equipment.COOL_1) || this.isOn(Equipment.COOL_2)) {
            this.lastCoolTimeOn = Manager.getDateNow();
        } else {
            this.lastCoolTimeOff = Manager.getDateNow();
        }
    }

    private boolean hasEnoughTimePassed(final HvacMechanical.HvacEquipment givenState) {
        final boolean currentState = this.isOn(givenState.getEquipment());
        final Equipment equipment = givenState.getEquipment();
        final boolean newState = givenState.isOn();
        if (newState == currentState) {
            return true;
        }
        final long delay = getMechanicalDelay();
        final Date delayedDate = new Date(Manager.getDateNow().getTime() - delay - 1L);
        if (equipment == Equipment.HEAT_1 || equipment == Equipment.HEAT_2) {
            final boolean heat1IsOn = this.isOn(Equipment.HEAT_1);
            final boolean upshift = (newState && equipment == Equipment.HEAT_2 && heat1IsOn);
            if (newState) {
                return isTimePassed((heat1IsOn? delayedDate : this.lastHeatTimeOn), (upshift? delay : this.heatMinTimeOff));
            } else {
                return isTimePassed(this.lastHeatTimeOff, this.heatMinTimeOn);
            }
        } else if (equipment == Equipment.COOL_1 || equipment == Equipment.COOL_2) {
            final boolean cool1IsOn = this.isOn(Equipment.COOL_1);
            final boolean upshift = (newState && equipment == Equipment.COOL_2 && this.isOn(Equipment.COOL_1));
            if (newState) {
                return isTimePassed((cool1IsOn? delayedDate : this.lastCoolTimeOn), (upshift? delay : this.coolMinTimeOff));
            } else {
                return isTimePassed(this.lastCoolTimeOff, this.coolMinTimeOn);
            }
        } else {
            return true;
        }
    }

    protected Date getLastProposedChangeTime() {
        return new Date(this.lastProposedChangeTime.getTime());
    }

    public void setLastProposedChangeTime(final Date lastProposedChangeTimeArg) {
        this.lastProposedChangeTime = new Date(lastProposedChangeTimeArg.getTime());
    }

    private synchronized void sendRelayStateRequest(final int stateByte) {
        final CombinedProperties properties = Manager.getSingleton().getProperties();
        final XBeeAddress64 address64 = new XBeeAddress64(properties.getSystemProperty(XBeeTransceiver.GATEWAY_XBEE_ADDRESS_PROP));
        final String payload = XbeeCommandName.HvacRelayState + " " + Integer.toString(stateByte);
        this.transceiver.sendRequest(address64, payload);
        LOG.info("Setting Hvac relay state to " + ByteUtils.toBase16(stateByte));
    }

    @Override
    public synchronized void processResponse(final XBeeResponse response) {
        try {
            if (response instanceof ZNetRxResponse) {
                final ZNetRxResponse zNetRxResponse = (ZNetRxResponse)response;
                final String rxData = ByteUtils.toString(zNetRxResponse.getData());
                if (rxData.startsWith(XbeeCommandName.HvacRelayState.name())) {
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

    private boolean checkRelayStateConsistency(final int relayState) {
        for (final HvacMechanical.HvacEquipment eqt : this.hvacEquipments.values()) {
            final int relayNumber = (eqt.getEquipment().getPhysicalRelayNumber());
            final boolean receivedState = (relayState & 0x00FF & (0x01 << (relayNumber-1))) > 0;
            if (receivedState !=  eqt.isOn()) {
                LOG.warn("relay state is inconsistent");
                return false;
            }
        }
        return true;
    }


    @Override
    public void junitSetLoopDelay(final long delayMs) {
        this.junitLoopDelay = delayMs;
    }

    private class Delay implements Runnable {

        @Override
        public void run() {
            boolean keepLooping = true;
            do {
                try {
                    LOG.debug("At the top of the HvacMechanical.Delay loop.");
                    recordLastActivationTimes();
                    final long timeNow = Manager.getDateNow().getTime();
                    final long timeLastChange = getLastProposedChangeTime().getTime();
                    if ((timeNow - timeLastChange) > getMechanicalDelay()) {
                        updateRelayBoard();
                    }

                    Thread.sleep((HvacMechanical.this.junitLoopDelay > 0)? HvacMechanical.this.junitLoopDelay : 10000L);
                } catch (InterruptedException e) {
                    LOG.info("HvacMechanical thread normal exit");
                    keepLooping = false;
                } catch (Throwable t) {
                    LOG.warn("HvacMechanical thread exception caught: ", t);
                }
            } while(keepLooping);
        }

    }

    private static class HvacEquipment {
        private final Equipment equipment;
        private boolean on = false;

        protected HvacEquipment(final Equipment equipmentArg) {
            this.equipment = equipmentArg;
        }

        protected Equipment getEquipment() {
            return this.equipment;
        }

        protected boolean isOn() {
            return this.on;
        }

        protected void setOn(final boolean onArg) {
            this.on = onArg;
        }

        @Override
        public boolean equals(final Object obj) {
            if (! (obj instanceof HvacMechanical.HvacEquipment)) {
                throw new IllegalStateException("expected type HvacMechanical.HvacEquipment");
            }
            return this.equipment == ((HvacMechanical.HvacEquipment)obj).getEquipment();
        }

        @Override
        public String toString() {
            return this.equipment.toString() + " on=" + this.isOn();
        }

        @Override
        public int hashCode() {
            final int hash = this.equipment.hashCode();
            return ((this.isOn())? hash * 2 : hash);
        }
    }
}
