package net.zhome.home.persistence.repository;

import net.zhome.home.persistence.model.Sample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SampleRepository extends JpaRepository<Sample, Long> {

    List<Sample> findBySensorId(Long sensorId);

    List<Sample> findBySensorIdAndTimeMsGreaterThanEqual(Long sensorId, Long timeMs);

}
