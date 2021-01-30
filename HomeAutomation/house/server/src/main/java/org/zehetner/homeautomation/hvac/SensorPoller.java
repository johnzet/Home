package org.zehetner.homeautomation.hvac;

import com.rapplogic.xbee.api.PacketListener;
import com.rapplogic.xbee.api.XBeeAddress64;
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.api.zigbee.ZNetRxResponse;
import com.rapplogic.xbee.util.ByteUtils;
import org.apache.log4j.Logger;
import org.zehetner.homeautomation.Utils;
import org.zehetner.homeautomation.common.CombinedProperties;
import org.zehetner.homeautomation.common.Manager;
import org.zehetner.homeautomation.common.XbeeCommandName;
import org.zehetner.homeautomation.health.HealthMonitor;
import org.zehetner.homeautomation.xbee.Transceiver;
import org.zehetner.homeautomation.xbee.XBeeDevice;
import org.zehetner.homeautomation.xbee.XBeeTransceiver;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.MessageFormat;

/**
 * Created by IntelliJ IDEA.
 * User: johnz
 * Date: 12/19/11
 * Time: 1:29 PM
 */
public class SensorPoller implements PacketListener {
    private static final Logger LOG = Logger.getLogger(SensorPoller.class);
    public static final String TEMPERATURE_C_KEY = "TEMPERATURE_C";
    public static final String HUMIDITY_KEY = "HUMIDITY";
    public static final String BAROMETER_KEY = "BAROMETER_KPA";
    public static final String BAT_PERCENT_KEY = "BAT_PCT";
    public static final String UP = "UP";
    public static final String DOWN = "DOWN";
    private Transceiver transceiver = null;

    public SensorPoller() {
    }

    public void setTransceiver(final Transceiver transceiverArg) {
        this.transceiver = transceiverArg;
    }

    private void parseDataResponse(final XBeeAddress64 address64, final String responseText) {
        final Temperature temperatureC = getTemperatureFromResponse(responseText);
        final double humidity = getHumidityFromResponse(responseText);
        final double batPct = getTerm1BatPctFromResponse(responseText);
        final Sensors sensors = Manager.getSingleton().getSensors();

        if (Utils.get64BitAddress(XBeeDevice.GATEWAY).equals(address64)) {
            final double barometer = getBarometerFromResponse(responseText);
            sensors.setOutdoorTemperature(temperatureC);
            sensors.setOutdoorHumidity(humidity);
            sensors.setBarometer(Sensors.kPaToinHg(barometer));
        } else if (Utils.get64BitAddress(XBeeDevice.THERMOSTAT_1).equals(address64)) {
            sensors.setIndoorTemperature(temperatureC);
            sensors.setIndoorHumidity(humidity);
            sensors.setThermostat1BatPercent(batPct);
        } else {
            LOG.warn("unknown device: " + address64);
        }
    }

    private void parseTempChangeResponse(final XBeeAddress64 address64, final String responseText) {
        if (Utils.get64BitAddress(XBeeDevice.THERMOSTAT_1).equals(address64)) {
            final HvacSettings hvacSettings = Manager.getSingleton().getHvacSystem().getHvacStateEngine().getHvacSettings();
            final Temperature temp = hvacSettings.getHoldTemperature();
            final int change = Integer.parseInt(responseText);
            hvacSettings.setHoldTemperature(temp.incrementInFahrenheit(change));
        }
    }

    private Temperature getTemperatureFromResponse(final String responseText) {
        final String[] pairs = responseText.split("\\|");
        for (final String pair : pairs) {
            final String[] sides = pair.split(" ");
            if (TEMPERATURE_C_KEY.equals(sides[0])) {
                final double cTemp = Double.parseDouble(sides[1]);
                return new CelsiusTemperature(cTemp);
            }
        }
        return new CelsiusTemperature(Double.NaN);
    }

    private double getHumidityFromResponse(final String responseText) {
        final String[] pairs = responseText.split("\\|");
        for (final String pair : pairs) {
            final String[] sides = pair.split(" ");
            if (HUMIDITY_KEY.equals(sides[0])) {
                return Double.parseDouble(sides[1]);
            }
        }
        return Double.NaN;
    }

    private double getBarometerFromResponse(final String responseText) {
        final String[] pairs = responseText.split("\\|");
        for (final String pair : pairs) {
            final String[] sides = pair.split(" ");
            if (BAROMETER_KEY.equals(sides[0])) {
                return Double.parseDouble(sides[1]);
            }
        }
        return Double.NaN;
    }

    private double getTerm1BatPctFromResponse(final String responseText) {
        final String[] pairs = responseText.split("\\|");
        for (final String pair : pairs) {
            final String[] sides = pair.split(" ");
            if (BAT_PERCENT_KEY.equals(sides[0])) {
                return Double.parseDouble(sides[1]);
            }
        }
        return Double.NaN;
    }

    private void sendDisplayText(final XBeeAddress64 address64) {
        final Sensors sensors = Manager.getSingleton().getSensors();
        if (Utils.get64BitAddress(XBeeDevice.GATEWAY).equals(address64)) {
            final String text = MessageFormat.format(XbeeCommandName.LcdText.name()
                    + ' '
                    + "In {0}\337F {1}\045|"
                    + "Out {2}\337F {3}\045|"
                    + "{4}inHg",
                    this.decimalFormat(sensors.getIndoorTemperature().getFahrenheitTemperature(), "##.0"),
                    this.decimalFormat(sensors.getIndoorHumidity(), "##.0"),
                    this.decimalFormat(sensors.getOutdoorTemperature().getFahrenheitTemperature(), "##.0"),
                    this.decimalFormat(sensors.getOutdoorHumidity(), "##.0"),
                    this.decimalFormat(sensors.getBarometer(), "##.00"));
            LOG.info("Sending LCD data to Gateway: " + text);
            this.transceiver.sendRequest(address64, text);
        } else if (Utils.get64BitAddress(XBeeDevice.THERMOSTAT_1).equals(address64) || Utils.get64BitAddress(XBeeDevice.THERMOSTAT_2).equals(address64)) {
            final HvacSettings hvacSettings = Manager.getSingleton().getHvacSystem().getHvacStateEngine().getHvacSettings();
            final HvacStateEngine hvacStateEngine = Manager.getSingleton().getHvacSystem().getHvacStateEngine();
            final HealthMonitor healthMonitor = Manager.getSingleton().getHealthMonitor();
            final String message = MessageFormat.format("{0}  {1}\337F  {2}",
                    Mode.toString(hvacSettings.getMode()),
                    this.decimalFormat(hvacSettings.getHoldTemperature().getFahrenheitTemperature(), "##"),
                    ((hvacSettings.isFanOn())? "Fan On" : "Fan Off"));

            final String activity;
            if (healthMonitor.isProblem()) {
                activity = healthMonitor.getShortProblem();
            } else if (hvacStateEngine.isOn(Equipment.HEAT_2)) {
                activity = "Stage 2 Heat is On";
            } else if (hvacStateEngine.isOn(Equipment.HEAT_1)) {
                activity = "Stage 1 Heat is On";
            } else if (hvacStateEngine.isOn(Equipment.COOL_2)) {
                activity = "Stage 2 A/C is On";
            } else if (hvacStateEngine.isOn(Equipment.COOL_1)) {
                activity = "Stage 1 A/C is On";
            } else if (hvacStateEngine.isOn(Equipment.FAN)) {
                activity = "Fan Only is On";
            } else {
                activity = "Idle";
            }

            final String command = ((sensors.getThermostat1BatPercent() < 10.0)?
                    XbeeCommandName.LcdTextBeep.name() : XbeeCommandName.LcdText.name());
            final String text = MessageFormat.format(command
                    + ' '
                    + "In {0}\337F {1}\045     {4}\045|"
                    + "Out {2}\337F {3}\045|"
                    + "{5} |"
                    + "{6}",
                    this.decimalFormat(sensors.getIndoorTemperature().getFahrenheitTemperature(), "##"),
                    this.decimalFormat(sensors.getIndoorHumidity(), "##"),
                    this.decimalFormat(sensors.getOutdoorTemperature().getFahrenheitTemperature(), "##"),
                    this.decimalFormat(sensors.getOutdoorHumidity(), "##"),
                    this.decimalFormat(sensors.getThermostat1BatPercent(), "###"),
                    message,
                    activity);
            LOG.info("Sending LCD data to thermostat" + address64 + " : " + text);
            this.transceiver.sendRequest(address64, text);
        } else {
            LOG.warn("unknown device: " + address64);
        }

    }

    private String decimalFormat(final double number, final String pattern) {
        final DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        //symbols.setNaN("NaN");
        symbols.setNaN("---");
        final DecimalFormat decimalFormat = new DecimalFormat(pattern, symbols);
        return decimalFormat.format(number);
    }

    @Override
    public void processResponse(final XBeeResponse response) {
        try {
            if (response instanceof ZNetRxResponse) {
                final ZNetRxResponse zNetRxResponse = (ZNetRxResponse)response;
                final String rxData = ByteUtils.toString(zNetRxResponse.getData());
                final XBeeAddress64 address64 = zNetRxResponse.getRemoteAddress64();
                if (address64 != null) {
                    if (rxData.startsWith(XbeeCommandName.SensorData.name())) {
                        LOG.info("Received sensor data: " + getXbeeNameFromAddress(address64) + " - " + rxData);
                        parseDataResponse(address64, rxData.substring(XbeeCommandName.SensorData.name().length() + 1));
                        sendDisplayText(address64);
                    } else if (rxData.startsWith(XbeeCommandName.TemperatureChange.name())) {
                        LOG.info("A temperature change was requested: " + getXbeeNameFromAddress(address64) + " - " + rxData);
                        parseTempChangeResponse(address64, rxData.substring(XbeeCommandName.TemperatureChange.name().length() + 1));
                    }
                }
            }
        }
        catch (Throwable t) {
            LOG.warn("Exception retrieving sensor data", t);
        }
    }

    private String getXbeeNameFromAddress(final XBeeAddress64 address64) {
        final CombinedProperties properties = Manager.getSingleton().getProperties();

        if (address64 == null) {
            return "";
        }
        if (address64.equals(Utils.get64BitAddress(XBeeDevice.GATEWAY))) {
            return properties.getSystemProperty(XBeeTransceiver.GATEWAY_XBEE_NAME_PROP);
        }
        if (address64.equals(Utils.get64BitAddress(XBeeDevice.THERMOSTAT_1))) {
            return properties.getSystemProperty(XBeeTransceiver.THERMOSTAT_1_XBEE_NAME_PROP);
        }
        if (address64.equals(Utils.get64BitAddress(XBeeDevice.THERMOSTAT_2))) {
            return properties.getSystemProperty(XBeeTransceiver.THERMOSTAT_2_XBEE_NAME_PROP);
        }
        return "";

    }
}
