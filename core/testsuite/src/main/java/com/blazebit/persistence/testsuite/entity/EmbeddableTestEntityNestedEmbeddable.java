/*
 * Copyright 2014 - 2023 Blazebit.
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

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
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
