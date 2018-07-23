package net.zhome.home.persistence.repository;

import net.zhome.home.persistence.model.SensorHost;

import javax.ejb.Stateless;
import javax.inject.Named;
import java.util.List;

@Stateless
@Named
public class SensorHostRepository extends AbstractRepository<SensorHost> {

    SensorHostRepository() {
        super(SensorHost.class);
    }

    public List<SensorHost> findByActive(boolean active){
        return em.createQuery("Select sh from SensorHost sh where sh.active = " + active).getResultList();
    }
}
