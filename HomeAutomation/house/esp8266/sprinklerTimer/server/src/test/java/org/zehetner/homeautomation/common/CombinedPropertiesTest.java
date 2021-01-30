package org.zehetner.homeautomation.common;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Created by IntelliJ IDEA.
 * User: johnzet
 * Date: 1/28/12
 * Time: 7:17 PM
 */
public class CombinedPropertiesTest {

    @Test
    public void testLoadSystemProperties() {
        final CombinedProperties properties = CombinedProperties.getSingleton();

        assertNotNull(properties.getSystemProperty(CombinedProperties.SPRINKLERS_HOST_NAME));
        assertNotNull(Double.parseDouble(properties.getSystemProperty(CombinedProperties.MECHANICAL_DELAY)) > 0.1);
    }

    @Test
    public void testLoadModifySaveLoadUserProperties() {
        final String testKey = "anyKey";
        final String testValue = "anyValue";
        final CombinedProperties properties = CombinedProperties.getSingleton();

        assertNull(properties.getUserProperty(testKey));

        properties.setUserProperty(testKey, testValue);
        assertEquals(testValue, properties.getUserProperty(testKey));
        properties.saveUserProperties();

        properties.junitReload();
        assertEquals(testValue, properties.getUserProperty(testKey));

        // cleanup
        properties.setUserProperty(testKey, null);
        properties.saveUserProperties();
    }
}
