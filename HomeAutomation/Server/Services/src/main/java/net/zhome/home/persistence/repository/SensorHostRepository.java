package net.zhome.home.persistence.repository;

import net.zhome.home.persistence.model.SensorHost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SensorHostRepository extends JpaRepository<SensorHost, Long> {

    List<SensorHost> findByActive(boolean active);

}
