package com.blazebit.persistence.view.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.MapKeyColumn;

@Entity
public class EmbeddableTestEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private EmbeddableTestEntityId id = new EmbeddableTestEntityId();
    private EmbeddableTestEntityEmbeddable embeddable = new EmbeddableTestEntityEmbeddable();
    private Set<EmbeddableTestEntitySimpleEmbeddable> embeddableSet = new HashSet<EmbeddableTestEntitySimpleEmbeddable>(0);
    private Map<String, EmbeddableTestEntitySimpleEmbeddable> embeddableMap = new HashMap<String, EmbeddableTestEntitySimpleEmbeddable>(0);

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

    @ElementCollection
    public Set<EmbeddableTestEntitySimpleEmbeddable> getEmbeddableSet() {
        return embeddableSet;
    }
    
    public void setEmbeddableSet(Set<EmbeddableTestEntitySimpleEmbeddable> embeddableSet) {
        this.embeddableSet = embeddableSet;
    }

    @ElementCollection
    @MapKeyColumn(nullable = false, length = 20)
    public Map<String, EmbeddableTestEntitySimpleEmbeddable> getEmbeddableMap() {
        return embeddableMap;
    }
    
    public void setEmbeddableMap(Map<String, EmbeddableTestEntitySimpleEmbeddable> embeddableMap) {
        this.embeddableMap = embeddableMap;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof EmbeddableTestEntity))
            return false;
        EmbeddableTestEntity other = (EmbeddableTestEntity) obj;
        if (getId() == null) {
            if (other.getId() != null)
                return false;
        } else if (!getId().equals(other.getId()))
            return false;
        return true;
    }
    
}
