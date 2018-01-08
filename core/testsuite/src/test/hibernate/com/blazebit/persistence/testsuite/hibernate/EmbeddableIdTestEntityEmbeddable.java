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

package com.blazebit.persistence.testsuite.hibernate;

import com.blazebit.persistence.testsuite.entity.IntIdEntity;
import com.blazebit.persistence.testsuite.entity.NameObject;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Embeddable
public class EmbeddableIdTestEntityEmbeddable implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private EmbeddableIdTestEntity manyToOne;
    private Set<EmbeddableIdTestEntity> oneToMany = new HashSet<EmbeddableIdTestEntity>(0);
    private Map<String, NameObject> elementCollection = new HashMap<String, NameObject>(0);
    private Map<String, IntIdEntity> manyToMany = new HashMap<String, IntIdEntity>(0);
    private EmbeddableIdTestEntityNestedEmbeddable nestedEmbeddable = new EmbeddableIdTestEntityNestedEmbeddable();

    @JoinColumns({
            @JoinColumn(name = "many_to_one_key", referencedColumnName = "test_key"),
            @JoinColumn(name = "many_to_one_int_id_entity_id", referencedColumnName = "int_id_entity_id"),
            @JoinColumn(name = "many_to_one_value", referencedColumnName = "some_value")
    })
    @ManyToOne(fetch = FetchType.LAZY)
    public EmbeddableIdTestEntity getManyToOne() {
        return manyToOne;
    }
    
    public void setManyToOne(EmbeddableIdTestEntity manyToOne) {
        this.manyToOne = manyToOne;
    }

    @OneToMany(mappedBy = "embeddable.manyToOne")
    public Set<EmbeddableIdTestEntity> getOneToMany() {
        return oneToMany;
    }

    public void setOneToMany(Set<EmbeddableIdTestEntity> oneToMany) {
        this.oneToMany = oneToMany;
    }

    // Fixed size because mysql has size limitations
    @ElementCollection
    @CollectionTable(name = "emb_id_tst_ent_elem_coll",
            joinColumns = {
                    @JoinColumn(name = "elem_coll_parent_key", referencedColumnName = "test_key"),
                    @JoinColumn(name = "elem_coll_parent_int_id_ent_id", referencedColumnName = "int_id_entity_id"),
                    @JoinColumn(name = "elem_coll_parent_value", referencedColumnName = "some_value")
            }
    )
    @MapKeyColumn(name = "elem_coll_key", nullable = false, length = 20)
    public Map<String, NameObject> getElementCollection() {
        return elementCollection;
    }
    
    public void setElementCollection(Map<String, NameObject> elementCollection) {
        this.elementCollection = elementCollection;
    }

    @ManyToMany
    @CollectionTable(name = "emb_id_tst_ent_many_to_many",
            joinColumns = {
                    @JoinColumn(name = "many_many_parent_key", referencedColumnName = "test_key"),
                    @JoinColumn(name = "many_many_parent_int_id_ent_id", referencedColumnName = "int_id_entity_id"),
                    @JoinColumn(name = "many_many_parent_value", referencedColumnName = "some_value")
            }
    )
    @MapKeyColumn(name = "many_many_key", nullable = false, length = 20)
    public Map<String, IntIdEntity> getManyToMany() {
        return manyToMany;
    }

    public void setManyToMany(Map<String, IntIdEntity> manyToMany) {
        this.manyToMany = manyToMany;
    }

    @Embedded
    public EmbeddableIdTestEntityNestedEmbeddable getNestedEmbeddable() {
        return nestedEmbeddable;
    }

    public void setNestedEmbeddable(EmbeddableIdTestEntityNestedEmbeddable nestedEmbeddable) {
        this.nestedEmbeddable = nestedEmbeddable;
    }

}
