package org.zehetner.homeautomation;

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
}
