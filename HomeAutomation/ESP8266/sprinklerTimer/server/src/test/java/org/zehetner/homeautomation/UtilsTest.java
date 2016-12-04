package org.zehetner.homeautomation;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: johnzet
 * Date: 2/4/12
 * Time: 11:01 PM
 * To change this template use File | HvacSettings | File Templates.
 */
public class UtilsTest {
    @Test
    public void testIsEmpty() throws Exception {
        assertTrue(Utils.isEmpty(null));
        assertTrue(Utils.isEmpty(""));
        assertTrue(Utils.isEmpty(" "));
        assertFalse(Utils.isEmpty("not empty"));
    }
}
