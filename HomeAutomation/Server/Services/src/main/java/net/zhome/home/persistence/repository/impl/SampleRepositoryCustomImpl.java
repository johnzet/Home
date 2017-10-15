package net.zhome.home.persistence.repository.impl;

import net.zhome.home.persistence.repository.SampleRepositoryCustom;
import net.zhome.home.persistence.model.Sample;
import net.zhome.home.persistence.model.Sensor;
import net.zhome.home.persistence.repository.SensorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by John Zehetner on 10/14/17.
 */

public class SampleRepositoryCustomImpl extends SimpleJpaRepository<Sample, Long> implements SampleRepositoryCustom {

    private SensorRepository sensorRepository;

    @Autowired
    public SampleRepositoryCustomImpl(JpaEntityInformation<Sample, ?> entityInformation, EntityManager entityManager,
                                      SensorRepository sensorRepository) {
        super(entityInformation, entityManager);
    }

    @Override
    public List<Sample> findCurrentSamples() {
        List<Sample> samples = new ArrayList<>();

//        //@Query("SELECT s FROM sample s WHERE s.sensorId = ?1 and max(s.timeMs)")
//        this.getQuery()
//        long now = new Date().getTime();
//        for (Sensor sensor : sensorRepository.findAll()) {
//            Sample sample = this.findLatestSample(sensor.getId());
//            if ((now - sample.getTimeMs()) < 1000 * 3600) {
//                samples.add(sample);
//            }
//        }
        return samples;
    }


}
