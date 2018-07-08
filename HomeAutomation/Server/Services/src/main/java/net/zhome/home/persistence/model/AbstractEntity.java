package net.zhome.home.persistence.model;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

//@Entity
@MappedSuperclass
//@Cacheable
//@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class AbstractEntity {
//    @SuppressWarnings("unused")
    @Id
    @GeneratedValue
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