package org.zehetner.homeautomation.common;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: johnzet
 * Date: 1/28/12
 * Time: 6:28 PM
 * To change this template use File | HvacSettings | File Templates.
 */
public class CombinedProperties {
    private static final Logger LOG = Logger.getLogger(CombinedProperties.class.getName());
    private static final String SYSTEM_PROPS_FILE_NAME = "systemProperties.properties";
    private static final String USER_PROPS_FILE_NAME = "userProperties.properties";
    private static final String PROGRAMS_FILE_NAME = "programs.xml";

    public static final String MECHANICAL_DELAY = "mechanicalDelay";
    public static final String SPRINKLERS_HOST_NAME = "sprinklersHostName";

    private Properties systemProperties = null;
    private Properties userProperties = null;
    private static CombinedProperties singleton = null;
    private static final String PROPERTY_FILE_PATH = "org.zehetner.propertyFileLocation";

    private CombinedProperties() {
    }

    public static CombinedProperties getSingleton() {
        if (singleton == null) {
            singleton = new CombinedProperties();
            singleton.load();
        }
        return singleton;
    }

    public static String getProgramSetXmlFileName() {
        final String path = System.getProperty(PROPERTY_FILE_PATH, ".");

        return path + File.separatorChar + PROGRAMS_FILE_NAME;
    }

    private void load() {
        this.systemProperties = new Properties();
        final String path = System.getProperty(PROPERTY_FILE_PATH, ".");
        try {
            final FileInputStream in = new FileInputStream(path + File.separatorChar + SYSTEM_PROPS_FILE_NAME);
            this.systemProperties.load(in);
            in.close();
            LOG.info("Loaded system properties");
        } catch (FileNotFoundException e) {
            LOG.error("Couldn't find system properties file", e);
        } catch (IOException e) {
            LOG.error("Couldn't get system properties", e);
        }

        this.userProperties = new Properties();
        try {
            final FileInputStream in = new FileInputStream(path + File.separatorChar + USER_PROPS_FILE_NAME);
            this.userProperties.load(in);
            in.close();
            LOG.info("Loaded user properties");
        } catch (FileNotFoundException e) {
            LOG.error("Couldn't find user properties file", e);
        } catch (IOException e) {
            LOG.error("Couldn't get user properties", e);
        }
    }

    public void junitReload() {
        load();
    }

    public String getSystemProperty(final String key) {
        return this.systemProperties.getProperty(key);
    }

    public void setUserProperty(final String key, final String value) {
        if (value == null) {
            this.userProperties.remove(key);
        } else {
            this.userProperties.setProperty(key, value);
        }
    }

    public String getUserProperty(final String key) {
        return this.userProperties.getProperty(key);
    }

    public void saveUserProperties() {
        final String path = System.getProperty(PROPERTY_FILE_PATH, ".");

        try {
            final FileOutputStream out = new FileOutputStream(path + File.separatorChar + USER_PROPS_FILE_NAME);
            this.userProperties.store(out, "---User Properties---");
            out.close();
            LOG.info("Saved user properties");
        } catch (FileNotFoundException e) {
            LOG.error("Couldn't find user properties file", e);
        } catch (IOException e) {
            LOG.error("Couldn't save to user properties file", e);
        }
    }
}
