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

package com.blazebit.persistence.testsuite.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;

@Embeddable
public class EmbeddableTestEntityEmbeddable implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private EmbeddableTestEntity manyToOne;
    private Set<EmbeddableTestEntity> oneToMany = new HashSet<EmbeddableTestEntity>(0);
    private Set<EmbeddableTestEntity> oneToMany2 = new HashSet<EmbeddableTestEntity>(0);
    private Map<String, NameObject> elementCollection = new HashMap<String, NameObject>(0);
    private Map<String, IntIdEntity> manyToMany = new HashMap<String, IntIdEntity>(0);
    private EmbeddableTestEntityNestedEmbeddable nestedEmbeddable = new EmbeddableTestEntityNestedEmbeddable();
    // Can't initialize collection because of Hibernate bug!?
    private Map<String, String> elementCollection2;// = new HashMap<String, String>(0);

    @JoinColumns({
            @JoinColumn(name = "many_to_one_key", referencedColumnName = "test_key"),
            @JoinColumn(name = "many_to_one_value", referencedColumnName = "test_value")
    })
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

    @OneToMany
    @JoinTable(name = "emb_tst_ent_one_to_many",
            joinColumns = {
                    @JoinColumn(name = "one_many_parent_key", referencedColumnName = "test_key"),
                    @JoinColumn(name = "one_many_parent_value", referencedColumnName = "test_value")
            }, inverseJoinColumns = {
                @JoinColumn(name = "one_many_element_key", referencedColumnName = "test_key"),
                @JoinColumn(name = "one_many_element_value", referencedColumnName = "test_value")
            }
    )
    public Set<EmbeddableTestEntity> getOneToMany2() {
        return oneToMany2;
    }

    public void setOneToMany2(Set<EmbeddableTestEntity> oneToMany2) {
        this.oneToMany2 = oneToMany2;
    }

    // Fixed size because mysql has size limitations
    @ElementCollection
    @CollectionTable(name = "emb_tst_ent_elem_coll",
        joinColumns = {
            @JoinColumn(name = "elem_coll_parent_key", referencedColumnName = "test_key"),
            @JoinColumn(name = "elem_coll_parent_value", referencedColumnName = "test_value")
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
    @JoinTable(name = "emb_tst_ent_many_to_many",
            joinColumns = {
                    @JoinColumn(name = "many_many_parent_key", referencedColumnName = "test_key"),
                    @JoinColumn(name = "many_many_parent_value", referencedColumnName = "test_value")
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
    public EmbeddableTestEntityNestedEmbeddable getNestedEmbeddable() {
        return nestedEmbeddable;
    }

    public void setNestedEmbeddable(EmbeddableTestEntityNestedEmbeddable nestedEmbeddable) {
        this.nestedEmbeddable = nestedEmbeddable;
    }

    @ElementCollection
    @MapKeyColumn(name = "emb_ts_ent_elem_coll2_key", nullable = false, length = 20)
    @CollectionTable(name = "emb_ts_ent_elem_coll2",
            joinColumns = {
                    @JoinColumn(name = "elem_coll2_parent_key", referencedColumnName = "test_key"),
                    @JoinColumn(name = "elem_coll2_parent_value", referencedColumnName = "test_value")
            }
    )
    public Map<String, String> getElementCollection2() {
        return elementCollection2;
    }

    public void setElementCollection2(Map<String, String> elementCollections) {
        this.elementCollection2 = elementCollection2;
    }

}
