package org.zehetner.homeautomation;

/**
 * Created by IntelliJ IDEA.
 * User: johnzet
 * Date: 1/29/12
 * Time: 10:05 PM
 * To change this template use File | HvacSettings | File Templates.
 */
public interface Worker extends Runnable {
    void junitSetLoopDelay(final long delayMs);
    void junitSetRunEnabled(boolean enabled);
}
