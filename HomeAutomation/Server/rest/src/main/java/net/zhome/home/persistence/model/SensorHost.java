package net.zhome.home.persistence.model;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "sensorhost")
public class SensorHost extends AbstractEntity {

    public SensorHost() {
        super();
    }

    @Column(nullable = true, name = "location")
    private String location;

    @Column(nullable = true, name = "description")
    private String description;


    @Column(nullable = false, name = "data_url")
    private String dataUrl;

    @Column(nullable = true, name = "home_url")
    private String homeUrl;

    @Column(nullable = false, name = "active")
    private Boolean active;

    @Column(nullable = false, name = "interval_s")
    private Long intervalS;

    @OneToMany(mappedBy = "sensorHost", fetch=FetchType.EAGER, cascade = CascadeType.ALL)
    private List<Sensor> sensors = new ArrayList<>();

    public List<Sensor> getSensors() {
        return sensors;
    }

    public void setSensors(List<Sensor> sensors) {
        this.sensors = sensors;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDataUrl() {
        return dataUrl;
    }

    public void setDataUrl(String dataUrl) {
        this.dataUrl = dataUrl;
    }

    public String getHomeUrl() {
        return homeUrl;
    }

    public void setHomeUrl(String homeUrl) {
        this.homeUrl = homeUrl;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Long getIntervalS() {
        return intervalS;
    }

    public void setIntervalS(Long intervalS) {
        this.intervalS = intervalS;
    }

    @Override
    public String toString() {
        return "SensorHost " + location;
    }
}