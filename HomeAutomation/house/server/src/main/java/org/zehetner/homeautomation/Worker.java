package org.zehetner.homeautomation;

/**
 * Created by IntelliJ IDEA.
 * User: johnzet
 * Date: 1/29/12
 * Time: 10:05 PM
 * To change this template use File | HvacSettings | File Templates.
 */
public interface Worker {
    void start();

    void junitSetLoopDelay(final long delayMs);

    void stop();
}
