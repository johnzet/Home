package net.zhome.home.persistence.repository;

import net.zhome.home.persistence.model.AbstractEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

/**
 * Created by John Zehetner on 7/22/18.
 */

public class AbstractRepository<T extends AbstractEntity> {

    @PersistenceContext
    EntityManager em;

    private Class<T> type;

    AbstractRepository(Class<T> c) {
        this.type = c;
    }

    public Long count() {
        return (Long)em.createQuery("select count(*) from " + type.getSimpleName()).getSingleResult();
    }

    public List<T> findAll() {
        return em.createQuery("select t from " + type.getSimpleName() + " t").getResultList();
    }

    public T findById(Long id) {
        return em.find(type, id);
    }

    public void save(T entity){
        if (entity.getId() == null) {
            em.persist(entity);
            em.flush();
        } else {
            em.merge(entity);
            em.refresh(entity);
            em.flush();
        }
    }

    public void saveAll(List<T> entities) {
        entities.forEach(this::save);
    }

    public void delete(Long id){
        T entity = em.find(type, id);
        delete(entity);
    }

    public void delete(T entity) {
        if (entity != null) {
            em.remove(entity);
            em.flush();
        }
    }

    public void deleteAll() {
        List<T> entities = findAll();
        entities.forEach(this::delete);
    }
}
