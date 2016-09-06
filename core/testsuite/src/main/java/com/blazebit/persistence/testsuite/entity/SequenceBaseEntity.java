package com.blazebit.persistence.testsuite.entity;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@MappedSuperclass
public class SequenceBaseEntity<I extends Serializable> implements Serializable {

    private I id;

    @Id
    @GeneratedValue
    public I getId() {
        return id;
    }

    public void setId(I id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SequenceBaseEntity)) return false;
        SequenceBaseEntity<?> that = (SequenceBaseEntity<?>) o;
        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
