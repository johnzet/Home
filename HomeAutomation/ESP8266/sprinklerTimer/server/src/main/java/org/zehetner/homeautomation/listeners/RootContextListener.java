package org.zehetner.homeautomation.listeners;
/**
 * Created by IntelliJ IDEA.
 * User: johnzet
 * Date: 1/29/12
 * Time: 10:27 AM
 */

import org.zehetner.homeautomation.common.Manager;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

public class RootContextListener implements ServletContextListener,
        HttpSessionListener, HttpSessionAttributeListener {


    // Public constructor is required by servlet spec
    public RootContextListener() {
    }

    // -------------------------------------------------------
    // ServletContextListener implementation
    // -------------------------------------------------------
    public void contextInitialized(final ServletContextEvent sce) {
        /* This method is called when the servlet context is
           initialized(when the Web application is deployed).
           You can initialize servlet context related data here.
        */

        final Manager manager = Manager.getSingleton();

        manager.getProgramSet().loadPrograms();

        manager.startThreads();
    }

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
    public void sessionCreated(final HttpSessionEvent se) {
        /* Session is created. */
    }

    public void sessionDestroyed(final HttpSessionEvent se) {
        /* Session is destroyed. */
    }

    // -------------------------------------------------------
    // HttpSessionAttributeListener implementation
    // -------------------------------------------------------

    public void attributeAdded(final HttpSessionBindingEvent event) {
        /* This method is called when an attribute
           is added to a session.
        */
    }

    public void attributeRemoved(final HttpSessionBindingEvent event) {
        /* This method is called when an attribute
           is removed from a session.
        */
    }

    public void attributeReplaced(final HttpSessionBindingEvent event) {
        /* This method is invoked when an attibute
           is replaced in a session.
        */
    }
}
