package net.zhome.home.persistence.repository;

import net.zhome.home.persistence.model.Sensor;

import javax.ejb.Stateless;
import javax.inject.Named;


@Stateless
@Named
public class SensorRepository extends AbstractRepository<Sensor> {

    SensorRepository() {
        super(Sensor.class);
    }

}
