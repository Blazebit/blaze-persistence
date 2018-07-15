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

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;

@Embeddable
public class EmbeddableTestEntityEmbeddable implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private String name;
    private EmbeddableTestEntity manyToOne;
    private Set<EmbeddableTestEntity> oneToMany = new HashSet<EmbeddableTestEntity>(0);
    private Map<String, IntIdEntity> elementCollection = new HashMap<String, IntIdEntity>(0);

    public EmbeddableTestEntityEmbeddable() {
    }

    @Column(name = "emb_tst_ent_name")
    public String getName() {
        return name;
    }

    
    public void setName(String name) {
        this.name = name;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "emb_tst_ent_key", referencedColumnName = "test_key"),
            @JoinColumn(name = "emb_tst_ent_int_id_ent", referencedColumnName = "int_id_ent")
    })
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
    @OneToMany
    @MapKeyColumn(name = "emb_ts_ent_elem_coll_key", nullable = false, length = 20)
    @JoinTable(name = "emb_ts_ent_elem_coll", joinColumns = {
            @JoinColumn(name = "emb_tst_ent_key", referencedColumnName = "test_key"),
            @JoinColumn(name = "emb_tst_ent_int_id_ent", referencedColumnName = "int_id_ent")
    }, inverseJoinColumns = @JoinColumn(name = "int_id_ent"))
    public Map<String, IntIdEntity> getElementCollection() {
        return elementCollection;
    }
    
    public void setElementCollection(Map<String, IntIdEntity> elementCollection) {
        this.elementCollection = elementCollection;
    }

}
