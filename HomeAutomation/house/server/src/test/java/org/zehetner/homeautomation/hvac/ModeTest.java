package org.zehetner.homeautomation.hvac;

import junit.framework.TestCase;
import org.junit.Test;

/**
 * Created by IntelliJ IDEA.
 * User: johnz
 * Date: 12/20/11
 * Time: 11:47 PM
 */
public class ModeTest extends TestCase {
    @Test
    public void testValues() {
        assertEquals(Mode.OFF, Mode.valueOf("OFF"));
        assertEquals(Mode.HEAT, Mode.valueOf("HEAT"));
        assertEquals(Mode.COOL, Mode.valueOf("COOL"));
        assertEquals(3, Mode.values().length);
    }
}

