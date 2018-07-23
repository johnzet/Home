package net.zhome.home.persistence.model;

import javax.persistence.*;

//@Entity
@MappedSuperclass
//@Cacheable
//@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@PersistenceUnit(name = "housePersistenceUnit")
public class AbstractEntity {
//    @SuppressWarnings("unused")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;


//    @SuppressWarnings("unused")
//    @Version
//    private Long version;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof AbstractEntity && this.id.equals((((AbstractEntity) obj).id));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        return prime * id.intValue();
    }

}