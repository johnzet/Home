package net.zhome.home.persistence.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

@Entity
@Table(name = "sensor",
        indexes = {@Index(name = "i_sensor_type", columnList="sensor_type", unique = false),
                @Index(name = "i_property_name", columnList="property_name", unique = false),
        })
public class Sensor extends AbstractEntity {

    public Sensor() {
        super();
    }


    @Column(nullable = false, name = "sensor_type")
    private SensorType sensorType;

    @Column(nullable = false, name = "short_name")
    private String shortName;

    @Column(nullable = false, name = "long_name")
    private String longName;

    @Column(nullable = false, name = "property_name")
    private String propertyName;

    @Column(nullable = false, name = "meas_unit")
    private Unit unit;

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(nullable = false, name = "sensor_host")
//    @Column(nullable = false, name = "sensor_host")
    @JsonIgnore
    private SensorHost sensorHost;

    public SensorHost getSensorHost() {
        return sensorHost;
    }

    public void setSensorHost(SensorHost sensorHost) {
        this.sensorHost = sensorHost;
        this.sensorHost.getSensors().add(this);
    }

    public SensorType getSensorType() {
        return sensorType;
    }

    public void setSensorType(SensorType sensorType) {
        this.sensorType = sensorType;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getLongName() {
        return longName;
    }

    public void setLongName(String longName) {
        this.longName = longName;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Sensor other = (Sensor) obj;

        return propertyName.equals(other.propertyName) && sensorHost.equals(other.sensorHost);
    }

    @Override
    public String toString() {
        return "Sensor " + longName;
    }

    public enum SensorType {SENSOR, ACTIVATION}

    public enum Unit {NULL, DEG_C, PCT, HPA, ON}

}