package net.zhome.home.persistence.repository;

import net.zhome.home.persistence.model.Sample;

import javax.ejb.Stateless;
import javax.inject.Named;
import java.util.List;

@Stateless
@Named
public class SampleRepository extends AbstractRepository<Sample> {

    SampleRepository() {
        super(Sample.class);
    }

    public List<Sample> findBySensorId(Long sensorId){
        return em.createQuery("select sa from Sample sa where sa.sensorId = " + sensorId).getResultList();

    };

    public List<Sample> findBySensorIdOrderByTimeMsAsc(Long sensorId){return null;};

    public List<Sample> findByGreatestTimeMs(){return null;};

    public List<Sample> findBySensorIdAndTimeMsGreaterThanEqual(Long sensorId, Long timeMs){return null;};

    public List<Sample> findBySensorIdAndTimeMsGreaterThanEqualOrderByTimeMsAsc(Long sensorId, Long startTimeMs){
        String query = "select sa from Sample sa where sa.sensorId = " + sensorId + " and sa.timeMs >= " + startTimeMs + " order by sa.timeMs asc";
        return em.createQuery(query).getResultList();
    }

    public List<Sample> findBySensorIdAndTimeMsGreaterThanEqualAndTimeMsLessThanEqualOrderByTimeMsAsc(Long sensorId, Long startTimeMs, Long endTimeMs){return null;};

}
