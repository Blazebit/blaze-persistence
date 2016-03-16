package com.blazebit.persistence.testsuite.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Embeddable;
import javax.persistence.OneToMany;

@Embeddable
public class EmbeddableTestEntityNestedEmbeddable implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Set<EmbeddableTestEntity> nestedOneToMany = new HashSet<EmbeddableTestEntity>();

    @OneToMany
    public Set<EmbeddableTestEntity> getNestedOneToMany() {
        return nestedOneToMany;
    }
    
    public void setNestedOneToMany(Set<EmbeddableTestEntity> nestedOneToMany) {
        this.nestedOneToMany = nestedOneToMany;
    }

}
