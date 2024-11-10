/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.hibernate.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Embeddable
public class EmbeddableIdTestEntityNestedEmbeddable implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Set<EmbeddableIdTestEntity> nestedOneToMany = new HashSet<EmbeddableIdTestEntity>();

    @OneToMany
    @JoinTable(name = "emb_id_tst_ent_nested_one_many",
            joinColumns = {
                    @JoinColumn(name = "nstd_one_many_parent_key", referencedColumnName = "test_key"),
                    @JoinColumn(name = "nstd_one_many_parent_int_id", referencedColumnName = "int_id_entity_id"),
                    @JoinColumn(name = "nstd_one_many_parent_value", referencedColumnName = "some_value")
            }, inverseJoinColumns = {
                    @JoinColumn(name = "nstd_one_many_target_key", referencedColumnName = "test_key"),
                    @JoinColumn(name = "nstd_one_many_target_int_id", referencedColumnName = "int_id_entity_id"),
                    @JoinColumn(name = "nstd_one_many_target_value", referencedColumnName = "some_value")
            }
    )
    public Set<EmbeddableIdTestEntity> getNestedOneToMany() {
        return nestedOneToMany;
    }
    
    public void setNestedOneToMany(Set<EmbeddableIdTestEntity> nestedOneToMany) {
        this.nestedOneToMany = nestedOneToMany;
    }

}
