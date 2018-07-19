/*
 * Copyright 2014 - 2018 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blazebit.persistence.view.testsuite.entity;

import com.blazebit.persistence.testsuite.entity.IntIdEntity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;

@Entity
@Table(name = "emb_tst_ent")
public class EmbeddableTestEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private EmbeddableTestEntityId id = new EmbeddableTestEntityId();
    private EmbeddableTestEntityEmbeddable embeddable = new EmbeddableTestEntityEmbeddable();
    private Set<EmbeddableTestEntitySimpleEmbeddable> embeddableSet = new HashSet<EmbeddableTestEntitySimpleEmbeddable>(0);
    private Map<String, EmbeddableTestEntitySimpleEmbeddable> embeddableMap = new HashMap<String, EmbeddableTestEntitySimpleEmbeddable>(0);

    public EmbeddableTestEntity() {
    }
    public EmbeddableTestEntity(IntIdEntity intIdEntity, String key) {
        id.setIntIdEntity(intIdEntity);
        id.setKey(key);
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
    @CollectionTable(name = "emb_tst_ent_emb_set", joinColumns = {
            @JoinColumn(name = "emb_tst_ent_key", referencedColumnName = "test_key"),
            @JoinColumn(name = "emb_tst_ent_int_id_ent", referencedColumnName = "int_id_ent")
    })
    public Set<EmbeddableTestEntitySimpleEmbeddable> getEmbeddableSet() {
        return embeddableSet;
    }
    
    public void setEmbeddableSet(Set<EmbeddableTestEntitySimpleEmbeddable> embeddableSet) {
        this.embeddableSet = embeddableSet;
    }

    @ElementCollection
    @MapKeyColumn(name = "emb_tst_ent_emb_map_key", nullable = false, length = 20)
    @CollectionTable(name = "emb_tst_ent_emb_map", joinColumns = {
            @JoinColumn(name = "emb_tst_ent_key", referencedColumnName = "test_key"),
            @JoinColumn(name = "emb_tst_ent_int_id_ent", referencedColumnName = "int_id_ent")
    })
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
