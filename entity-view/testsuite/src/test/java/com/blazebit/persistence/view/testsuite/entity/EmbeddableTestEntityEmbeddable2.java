/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.entity;

import com.blazebit.persistence.testsuite.entity.IntIdEntity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;

@Embeddable
public class EmbeddableTestEntityEmbeddable2 implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private String name;
    private EmbeddableTestEntity2 manyToOne;
    private Set<EmbeddableTestEntity2> oneToMany = new HashSet<EmbeddableTestEntity2>(0);
    private Map<String, IntIdEntity> elementCollection = new HashMap<String, IntIdEntity>(0);

    public EmbeddableTestEntityEmbeddable2() {
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
    public EmbeddableTestEntity2 getManyToOne() {
        return manyToOne;
    }
    
    public void setManyToOne(EmbeddableTestEntity2 manyToOne) {
        this.manyToOne = manyToOne;
    }

    @OneToMany(mappedBy = "embeddable.manyToOne")
    public Set<EmbeddableTestEntity2> getOneToMany() {
        return oneToMany;
    }

    public void setOneToMany(Set<EmbeddableTestEntity2> oneToMany) {
        this.oneToMany = oneToMany;
    }

    // Fixed size because mysql has size limitations
    @OneToMany
    @MapKeyColumn(name = "emb_ts_ent_elem_coll_key", nullable = false, length = 20)
    @JoinTable(name = "emb_ts_ent2_elem_coll", joinColumns = {
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
