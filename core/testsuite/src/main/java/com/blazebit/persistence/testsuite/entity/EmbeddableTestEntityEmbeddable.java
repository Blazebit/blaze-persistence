package com.blazebit.persistence.testsuite.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;

@Embeddable
public class EmbeddableTestEntityEmbeddable implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private EmbeddableTestEntity manyToOne;
    private Set<EmbeddableTestEntity> oneToMany = new HashSet<EmbeddableTestEntity>(0);
    private Map<String, NameObject> elementCollection = new HashMap<String, NameObject>(0);
    private Map<String, IntIdEntity> manyToMany = new HashMap<String, IntIdEntity>(0);
    private EmbeddableTestEntityNestedEmbeddable nestedEmbeddable = new EmbeddableTestEntityNestedEmbeddable();

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

    // Fixed size because mysql has size limitations
    @ElementCollection
    @MapKeyColumn(nullable = false, length = 20)
    public Map<String, NameObject> getElementCollection() {
        return elementCollection;
    }
    
    public void setElementCollection(Map<String, NameObject> elementCollection) {
        this.elementCollection = elementCollection;
    }

    @ManyToMany
    @MapKeyColumn(nullable = false, length = 20)
    public Map<String, IntIdEntity> getManyToMany() {
        return manyToMany;
    }

    public void setManyToMany(Map<String, IntIdEntity> manyToMany) {
        this.manyToMany = manyToMany;
    }

    @Embedded
    public EmbeddableTestEntityNestedEmbeddable getNestedEmbeddable() {
        return nestedEmbeddable;
    }

    public void setNestedEmbeddable(EmbeddableTestEntityNestedEmbeddable nestedEmbeddable) {
        this.nestedEmbeddable = nestedEmbeddable;
    }

}
