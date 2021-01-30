package org.zehetner.homeautomation;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: johnz
 * Date: 5/23/12
 * Time: 6:25 PM
 */
public class TimeTest {
    @Test
    public void testTime() {
        final Time t = new Time(13, 14, 15);
        assertEquals(13, t.getHours());
        assertEquals(14, t.getMinutes());
        assertEquals(15, t.getSeconds());
        assertEquals(15+14*60+13*3600, t.getSecondsSinceMidnight());
    }
}
