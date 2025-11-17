/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Embeddable
public class EmbeddableTestEntityNestedEmbeddable implements Serializable {
    private static final long serialVersionUID = 1L;

    private Set<EmbeddableTestEntity> nestedOneToMany = new HashSet<EmbeddableTestEntity>();

    @OneToMany
    @JoinTable(name = "emb_tst_ent_nested_one_many",
        joinColumns = {
            @JoinColumn(name = "nstd_one_many_parent_key", referencedColumnName = "test_key"),
            @JoinColumn(name = "nstd_one_many_parent_value", referencedColumnName = "test_value")
        },
        inverseJoinColumns = {
            @JoinColumn(name = "nstd_one_many_target_key", referencedColumnName = "test_key"),
            @JoinColumn(name = "nstd_one_many_target_value", referencedColumnName = "test_value")
        }
    )
    public Set<EmbeddableTestEntity> getNestedOneToMany() {
        return nestedOneToMany;
    }

    public void setNestedOneToMany(Set<EmbeddableTestEntity> nestedOneToMany) {
        this.nestedOneToMany = nestedOneToMany;
    }

}
