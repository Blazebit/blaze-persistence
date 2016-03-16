package com.blazebit.persistence.testsuite.entity;

import java.io.Serializable;

import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

@Entity
public class EmbeddableTestEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private EmbeddableTestEntityId id;
    private EmbeddableTestEntityEmbeddable embeddable = new EmbeddableTestEntityEmbeddable();

    public EmbeddableTestEntity() {
    }

    @EmbeddedId
    public EmbeddableTestEntityId getId() {
        return id;
    }

    public void setId(EmbeddableTestEntityId id) {
        this.id = id;
    }
    
    @Embedded
    public EmbeddableTestEntityEmbeddable getEmbeddable() {
        return embeddable;
    }
    
    public void setEmbeddable(EmbeddableTestEntityEmbeddable embeddable) {
        this.embeddable = embeddable;
    }

}
