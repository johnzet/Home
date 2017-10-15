package net.zhome.home.persistence.repository;

import net.zhome.home.persistence.model.Sample;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SampleRepository extends CrudRepository<Sample, Long>, SampleRepositoryCustom {

    List<Sample> findBySensorId(Long sensorId);

    List<Sample> findBySensorIdAndTimeMsGreaterThanEqualOrderByTimeMsAsc(Long sensorId, Long timeMs);

    List<Sample> findBySensorIdAndTimeMsGreaterThanEqualAndTimeMsLessThanEqualOrderByTimeMsAsc(Long sensorId, Long startTime, Long endTime);

    List<Sample> findBySensorIdOrderByTimeMsAsc(Long sensorId);
}
