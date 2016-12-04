package org.zehetner.homeautomation.sprinklers;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: johnz
 * Date: 5/20/12
 * Time: 5:22 PM
 */
public class ZoneTest {

    @Test
    public void testAllOffOption() {
        assertEquals(Zone.ALL_OFF, Zone.valueOf("ALL_OFF"));
    }

    @Test
    public void testPhysicalRelayNumber() {
        assertEquals(8L, (long)Zone.ZONE_8.getPhysicalRelayNumber());
    }

    @Test
    public void testGetZone() {
        assertEquals(Zone.ALL_OFF, Zone.getZone(0));
        assertEquals(Zone.ZONE_1, Zone.getZone(1));
        assertEquals(Zone.ZONE_8, Zone.getZone(8));
    }
}
