package com.blazebit.persistence.testsuite.hibernate;

import javax.persistence.Embeddable;
import javax.persistence.OneToMany;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Embeddable
public class EmbeddableIdTestEntityNestedEmbeddable implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Set<EmbeddableIdTestEntity> nestedOneToMany = new HashSet<EmbeddableIdTestEntity>();

    @OneToMany
    public Set<EmbeddableIdTestEntity> getNestedOneToMany() {
        return nestedOneToMany;
    }
    
    public void setNestedOneToMany(Set<EmbeddableIdTestEntity> nestedOneToMany) {
        this.nestedOneToMany = nestedOneToMany;
    }

}
