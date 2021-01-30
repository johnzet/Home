package org.zehetner.homeautomation.hvac;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: johnz
 * Date: 5/20/12
 * Time: 5:26 PM
 */
public class EquipmentTest {

    @Test
    public void testAllOffOption() {
        assertEquals(Equipment.ALL_OFF, Equipment.valueOf("ALL_OFF"));
    }

    @Test
    public void testPhysicalRelayNumber() {
        assertEquals(6L, (long)Equipment.HUMIDIFIER.getPhysicalRelayNumber());
    }
}
