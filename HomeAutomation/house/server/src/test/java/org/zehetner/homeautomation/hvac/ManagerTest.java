package org.zehetner.homeautomation.hvac;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.zehetner.homeautomation.Worker;
import org.zehetner.homeautomation.common.Manager;

/**
 * Created by IntelliJ IDEA.
 * User: johnz
 * Date: 12/26/11
 * Time: 7:09 PM
 */
public class ManagerTest extends TestCase {
    private Manager manager;

    @Override
    @Before
    public void setUp() {
        manager = Manager.getSingleton();
    }

    @Test
    public void testGetSingleton() {
        assertTrue(Manager.getSingleton() instanceof Manager);
    }

    @Test
    public void testGetMechanical() {
        assertTrue(this.manager.getHvacSystem().getHvacStateEngine().getHvacMechanical() instanceof Worker);
    }

    @Test
    public void testGetSensors() {
        assertTrue(this.manager.getSensors() instanceof Sensors);
    }

    @Test
    public void testGetSettings() {
        assertTrue(this.manager.getHvacSystem().getHvacStateEngine().getHvacSettings() instanceof HvacSettings);
    }

    @Test
    public void testGetStateEngine() {
        assertTrue(this.manager.getHvacSystem().getHvacStateEngine() instanceof HvacStateEngine);
    }

    @Test
    public void testGetSensorPoller() {
        assertTrue(this.manager.getSensorPoller() instanceof SensorPoller);
    }

    @Test
    public void testGetDataLogger() {
        assertTrue(this.manager.getDataLogger() instanceof DataLogger);
    }
}
