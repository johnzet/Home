package org.zehetner.homeautomation;

import com.rapplogic.xbee.api.XBeeAddress64;
import org.zehetner.homeautomation.common.CombinedProperties;
import org.zehetner.homeautomation.xbee.XBeeDevice;

import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: johnz
 * Date: 12/21/11
 * Time: 2:42 PM
 */
public class Utils {
    private static final Logger LOG = Logger.getLogger(Utils.class.getName());

    private Utils() {
    }

    public static boolean isEmpty(final String s) {
        if (s == null) {
            return true;
        }
        return s.trim().isEmpty();
    }

    public static XBeeAddress64 get64BitAddress(final XBeeDevice device) {
        final CombinedProperties props = CombinedProperties.getSingleton();
        return new XBeeAddress64(props.getSystemProperty(device.getKey()));
    }
}
