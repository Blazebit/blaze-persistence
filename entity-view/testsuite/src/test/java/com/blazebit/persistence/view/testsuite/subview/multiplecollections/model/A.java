/*
 * Copyright 2014 - 2020 Blazebit.
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

package com.blazebit.persistence.view.testsuite.subview.multiplecollections.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author Moritz Becker
 * @since 1.4.0
 */
@Entity
public class A {
    @Id
    @GeneratedValue
    private Long id;
    @ManyToMany
    private Set<B> bSet = new HashSet<>(0);
    @ManyToMany
    private Set<C> cSet = new HashSet<>(0);

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<B> getbSet() {
        return bSet;
    }

    public void setbSet(Set<B> bSet) {
        this.bSet = bSet;
    }

    public Set<C> getcSet() {
        return cSet;
    }

    public void setcSet(Set<C> cSet) {
        this.cSet = cSet;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof A)) return false;
        A a = (A) o;
        // Null-ids are never equal
        return id != null && a.id != null && Objects.equals(id, a.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
