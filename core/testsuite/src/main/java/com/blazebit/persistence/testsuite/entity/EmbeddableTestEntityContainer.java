/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "emb_tst_ent_cont")
public class EmbeddableTestEntityContainer implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private Set<EmbeddableTestEntity> embeddableTestEntities = new HashSet<EmbeddableTestEntity>();
    
    @Id
    @Column(name = "id")
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    
    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "emb_tst_ent_cont_entities",
        joinColumns = @JoinColumn(name = "tst_ent_cont_id", referencedColumnName = "id"),
        inverseJoinColumns = {
            @JoinColumn(name = "tst_ent_key", referencedColumnName = "test_key"),
            @JoinColumn(name = "tst_ent_value", referencedColumnName = "test_value")
        }
    )
    public Set<EmbeddableTestEntity> getEmbeddableTestEntities() {
        return embeddableTestEntities;
    }
    public void setEmbeddableTestEntities(Set<EmbeddableTestEntity> embeddableTestEntities) {
        this.embeddableTestEntities = embeddableTestEntities;
    }
    
}
