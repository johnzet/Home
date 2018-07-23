package net.zhome.home.persistence.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(name = "sample",
        indexes = {@Index(name = "i_time_ms",  columnList="time_ms", unique = false),
                   @Index(name = "i_sensor_id", columnList="sensor_id", unique = false)})
public class Sample extends AbstractEntity /*implements Serializable*/ {

    public Sample() {
    }

    public Sample(final long sensor_id, final long time, final float value) {
        this.sensorId = sensor_id;
        this.timeMs = time;
        this.sensedValue = value;
    }

    @Column(nullable = false, name = "time_ms")
    private Long timeMs;

    @Column(nullable = false, name = "sensor_id")
    private Long sensorId;

    @Column(name = "sensed_value")
    private Float sensedValue;

    public Long getTimeMs() {
        return timeMs;
    }

    public void setTimeMs(Long timeMs) {
        this.timeMs = timeMs;
    }

    public Long getSensorId() {
        return sensorId;
    }

    public void setSensorId(Long sensorId) {
        this.sensorId = sensorId;
    }

    public Float getSensedValue() {
        return sensedValue;
    }

    public void setSensedValue(Float sensedValue) {
        this.sensedValue = sensedValue;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Sample other = (Sample) obj;
        return sensorId.equals(other.sensorId) && timeMs.equals(other.timeMs);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        return prime * (timeMs.intValue()) ^ (sensorId.intValue() & 0x0000ffff) << 16;
    }


    @Override
    public String toString() {
        return "Sample [sensorId=" + sensorId + "]" + " = " + sensedValue;
    }

}