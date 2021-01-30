package org.zehetner.homeautomation.listeners;
/**
 * Created by IntelliJ IDEA.
 * User: johnzet
 * Date: 1/29/12
 * Time: 10:27 AM
 */

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.zehetner.homeautomation.common.CombinedProperties;
import org.zehetner.homeautomation.common.Manager;
import org.zehetner.homeautomation.hvac.HvacMechanical;
import org.zehetner.homeautomation.hvac.HvacStateEngine;
import org.zehetner.homeautomation.xbee.Transceiver;
import org.zehetner.homeautomation.xbee.XBeeTransceiver;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.io.File;
import java.io.IOException;

public class RootContextListener implements ServletContextListener,
        HttpSessionListener, HttpSessionAttributeListener {
    private static final Logger LOG = Logger.getLogger(RootContextListener.class.getName());


    // Public constructor is required by servlet spec
    public RootContextListener() {
    }

    // -------------------------------------------------------
    // ServletContextListener implementation
    // -------------------------------------------------------
    @Override
    public void contextInitialized(final ServletContextEvent sce) {
        /* This method is called when the servlet context is
           initialized(when the Web application is deployed).
           You can initialize servlet context related data here.
        */

        final Manager manager = Manager.getSingleton();
        final Transceiver transceiver = new XBeeTransceiver();
        transceiver.initTransceiver();

        final HvacStateEngine hvacStateEngine = Manager.getSingleton().getHvacSystem().getHvacStateEngine();
        final HvacMechanical hvacMechanical = hvacStateEngine.getHvacMechanical();

        hvacMechanical.setTransceiver(transceiver);
        manager.getSprinklerMechanical().setTransceiver(transceiver);
        manager.getSensorPoller().setTransceiver(transceiver);

        final String xmlStr;
        try {
            xmlStr = FileUtils.readFileToString(new File(CombinedProperties.getProgramSetXmlFileName()));
            manager.getProgramSet().loadFromXml(xmlStr);
        } catch (IOException e) {
            LOG.warn("Couldn't load program set file", e);
        }

        transceiver.addPacketListener(hvacMechanical);
        transceiver.addPacketListener(manager.getSprinklerMechanical());
        transceiver.addPacketListener(manager.getSensorPoller());
        transceiver.addPacketListener(manager.getHealthMonitor());

        manager.startThreads();
    }

    @Override
    public void contextDestroyed(final ServletContextEvent sce) {
        /* This method is invoked when the Servlet Context
           (the Web application) is undeployed or
           Application Server shuts down.
        */
        Manager.getSingleton().destroy();
    }

    // -------------------------------------------------------
    // HttpSessionListener implementation
    // -------------------------------------------------------
    @Override
    public void sessionCreated(final HttpSessionEvent se) {
        /* Session is created. */
    }

    @Override
    public void sessionDestroyed(final HttpSessionEvent se) {
        /* Session is destroyed. */
    }

    // -------------------------------------------------------
    // HttpSessionAttributeListener implementation
    // -------------------------------------------------------

    @Override
    public void attributeAdded(final HttpSessionBindingEvent event) {
        /* This method is called when an attribute
           is added to a session.
        */
    }

    @Override
    public void attributeRemoved(final HttpSessionBindingEvent event) {
        /* This method is called when an attribute
           is removed from a session.
        */
    }

    @Override
    public void attributeReplaced(final HttpSessionBindingEvent event) {
        /* This method is invoked when an attibute
           is replaced in a session.
        */
    }
}
