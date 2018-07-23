package net.zhome.home.util;

import java.util.logging.Level;
import java.util.logging.Logger;


public class ZLogger extends Logger {
    private ZLogger(Class c) {
        super(c.getName(), null);
    }

    public static ZLogger getLogger(Class c) {
        return new ZLogger(c);
    }

    public void error(String msg) {
        this.log(Level.SEVERE, msg);
    }

    public void error(String msg, Throwable t) {
        this.log(Level.SEVERE, msg, t);
    }

    public void warn(String msg) {
        this.log(Level.WARNING, msg);
    }

    public void warn(String msg, Throwable t) {
        this.log(Level.WARNING, msg, t);
    }
}
