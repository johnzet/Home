package org.zehetner.homeautomation.health;

import com.rapplogic.xbee.api.PacketListener;
import com.rapplogic.xbee.api.XBeeAddress64;
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.api.zigbee.ZNetRxResponse;
import com.rapplogic.xbee.util.ByteUtils;
import org.apache.log4j.Logger;
import org.zehetner.homeautomation.Utils;
import org.zehetner.homeautomation.common.XbeeCommandName;
import org.zehetner.homeautomation.xbee.XBeeDevice;

import java.util.Scanner;
import java.util.regex.MatchResult;

/**
 * Created by IntelliJ IDEA.
 * User: johnz
 * Date: 10/27/12
 * Time: 10:38 AM
 */
public class HealthMonitor implements PacketListener {
    private static final Logger LOG = Logger.getLogger(HealthMonitor.class.getName());

    private int gatewaySensorErrors = 0;
    private int gatewayCommErrors = 0;
    private int gatewayTimeoutErrors = 0;
    private int gatewayWdtResetCount = 0;
    private int thermostat1SensorErrors = 0;
    private int thermostat1CommErrors = 0;
    private int thermostat1TimeoutErrors = 0;
    private int thermostat1WdtResetCount = 0;
    private int thermostat2SensorErrors = 0;
    private int thermostat2CommErrors = 0;
    private int thermostat2TimeoutErrors = 0;
    private int thermostat2WdtResetCount = 0;

    @Override
    public void processResponse(final XBeeResponse response) {
        try {
            if (response instanceof ZNetRxResponse) {
                final ZNetRxResponse zNetRxResponse = (ZNetRxResponse)response;
                final String rxData = ByteUtils.toString(zNetRxResponse.getData());
                final XBeeAddress64 address64 = zNetRxResponse.getRemoteAddress64();

                if (rxData.startsWith(XbeeCommandName.ErrorCounts.name())) {
                    if (Utils.get64BitAddress(XBeeDevice.GATEWAY).equals(address64)) {
                        LOG.info("Gateway responded with error counts: " + rxData);
                        final int[] counts = this.scanCounts(rxData);
                        this.gatewaySensorErrors = counts[0];
                        this.gatewayCommErrors = counts[1];
                        this.gatewayTimeoutErrors = counts[2];
                        this.gatewayWdtResetCount = counts[3];
                    } else if (Utils.get64BitAddress(XBeeDevice.THERMOSTAT_1).equals(address64)) {
                        LOG.info("Thermostat 1 responded with error counts: " + rxData);
                        final int[] counts = this.scanCounts(rxData);
                        this.thermostat1SensorErrors = counts[0];
                        this.thermostat1CommErrors = counts[1];
                        this.thermostat1TimeoutErrors = counts[2];
                        this.thermostat1WdtResetCount = counts[3];
                    } else if (Utils.get64BitAddress(XBeeDevice.THERMOSTAT_2).equals(address64)) {
                        LOG.info("Thermostat 2 responded with error counts: " + rxData);
                        final int[] counts = this.scanCounts(rxData);
                        this.thermostat2SensorErrors = counts[0];
                        this.thermostat2CommErrors = counts[1];
                        this.thermostat2TimeoutErrors = counts[2];
                        this.thermostat2WdtResetCount = counts[3];
                    }
                }
            }
        }
        catch (Throwable t) {
            LOG.warn("Exception retrieving reported error counts", t);
        }
        detectProblems();
    }

    private int[] scanCounts(final String str) {
        // format: "ErrorCounts SENSOR %i COMM %i TIMEOUT %i WDT %i"
        final Scanner scanner = new Scanner(str);
        scanner.findInLine("ErrorCounts SENSOR (\\d+) COMM (\\d+) TIMEOUT (\\d+) WDT (\\d+)");
        final MatchResult result = scanner.match();
        final int[] counts = {-1,-1,-1,-1};
        for (int i=1; i<=result.groupCount(); i++) {
            try {
                counts[i-1] = Integer.parseInt(result.group(i));
            } catch (NumberFormatException e) {
                LOG.warn("Unexpected token: " + result.group(i), e);
            }
        }
        scanner.close();
        return counts;
    }

    public int getGatewaySensorErrors() {
        return this.gatewaySensorErrors;
    }

    public int getGatewayCommErrors() {
        return this.gatewayCommErrors;
    }

    public int getGatewayTimeoutErrors() {
        return this.gatewayTimeoutErrors;
    }

    public int getGatewayWdtResetCount() {
        return this.gatewayWdtResetCount;
    }

    public int getThermostat1SensorErrors() {
        return this.thermostat1SensorErrors;
    }

    public int getThermostat1CommErrors() {
        return this.thermostat1CommErrors;
    }

    public int getThermostat1TimeoutErrors() {
        return this.thermostat1TimeoutErrors;
    }

    public int getThermostat1WdtResetCount() {
        return this.thermostat1WdtResetCount;
    }

    public int getThermostat2SensorErrors() {
        return thermostat2SensorErrors;
    }

    public int getThermostat2CommErrors() {
        return thermostat2CommErrors;
    }

    public int getThermostat2TimeoutErrors() {
        return thermostat2TimeoutErrors;
    }

    public int getThermostat2WdtResetCount() {
        return thermostat2WdtResetCount;
    }

    public boolean isProblem() {
        return false;
    }

    /**
     * Get the LCD text (20 chars or less) describing the current problem.  Return empty string if all is OK.
     * @return short description of the problem
     */
    public String getShortProblem() {
        return "";
    }
}
