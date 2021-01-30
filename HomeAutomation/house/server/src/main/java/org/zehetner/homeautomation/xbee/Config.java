package org.zehetner.homeautomation.xbee;

import gnu.io.CommPortIdentifier;
import org.apache.log4j.Logger;
import org.zehetner.homeautomation.Utils;
import org.zehetner.homeautomation.common.CombinedProperties;
import org.zehetner.homeautomation.common.Manager;

import java.util.Enumeration;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created by IntelliJ IDEA.
 * User: johnz
 * Date: 12/19/11
 * Time: 2:05 PM
 */
public class Config {
    private static final Logger LOG = Logger.getLogger(Config.class.getName());

    private Config() {
    }

    public static String getComPort() {
        final String systemComPort = Manager.getSingleton().getProperties().getSystemProperty(CombinedProperties.PROP_COM_PORT);
        LOG.info("System com port property = " + systemComPort);

        if (! Utils.isEmpty(systemComPort)  && ! "auto".equals(systemComPort)) {
            LOG.info("Selected com port " + systemComPort);
            return systemComPort;
        }
        final SortedSet<String> ports = getComPorts();
        if (!ports.isEmpty()) {
            LOG.info("Selected com port " + ports.last());
            return ports.last();
        }
        throw new IllegalStateException("Couldn't get a COM port for the XBee radio.");
    }

    /** Ask the Java Communications API * what ports it thinks it has.
     * @return a sorted list of the com ports*/
    public static SortedSet<String> getComPorts() {
        final SortedSet<String> ports = new TreeSet<String>();
        // get list of ports available on this particular computer,
        // by calling static method in CommPortIdentifier.
        final Enumeration<CommPortIdentifier> pList = CommPortIdentifier.getPortIdentifiers();

        // Process the list.
        while (pList.hasMoreElements()) {
            final CommPortIdentifier cpi = pList.nextElement();
            final StringBuilder sb = new StringBuilder(50);
            sb.append("Port ").append(cpi.getName()).append(' ');
            if (cpi.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                sb.append("is a Serial Port.");
                ports.add(cpi.getName());
            } else if (cpi.getPortType() == CommPortIdentifier.PORT_PARALLEL) {
                sb.append("is a Parallel Port.");
            } else {
                sb.append("is an Unknown Port: ").append(cpi.getName());
            }
            LOG.info(sb.toString());
        }
        return ports;
    }
}
