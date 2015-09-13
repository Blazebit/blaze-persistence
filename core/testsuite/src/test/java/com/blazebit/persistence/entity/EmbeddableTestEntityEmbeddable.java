package com.blazebit.persistence.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;

@Embeddable
public class EmbeddableTestEntityEmbeddable implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private EmbeddableTestEntity manyToOne;
    private Set<EmbeddableTestEntity> oneToMany = new HashSet<EmbeddableTestEntity>(0);
    private Map<String, IntIdEntity> elementCollection = new HashMap<String, IntIdEntity>(0);

    @ManyToOne(fetch = FetchType.LAZY)
    public EmbeddableTestEntity getManyToOne() {
        return manyToOne;
    }
    
    public void setManyToOne(EmbeddableTestEntity manyToOne) {
        this.manyToOne = manyToOne;
    }

    @OneToMany(mappedBy = "embeddable.manyToOne")
    public Set<EmbeddableTestEntity> getOneToMany() {
        return oneToMany;
    }

    public void setOneToMany(Set<EmbeddableTestEntity> oneToMany) {
        this.oneToMany = oneToMany;
    }

    @ElementCollection
    @MapKeyColumn(nullable = false)
    public Map<String, IntIdEntity> getElementCollection() {
        return elementCollection;
    }
    
    public void setElementCollection(Map<String, IntIdEntity> elementCollection) {
        this.elementCollection = elementCollection;
    }

}
