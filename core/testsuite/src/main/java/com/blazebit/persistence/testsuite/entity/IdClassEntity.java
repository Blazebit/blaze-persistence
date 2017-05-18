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

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
@Entity
@Table(name = "id_class_entity")
@IdClass(IdClassEntityId.class)
public class IdClassEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer key1;
    private String key2;
    private Integer value;
    private Set<IdClassEntity> children = new HashSet<>();

    public IdClassEntity() {
    }

    public IdClassEntity(Integer key1, String key2, Integer value) {
        this.key1 = key1;
        this.key2 = key2;
        this.value = value;
    }

    @Id
    @Column(name = "key1", nullable = false)
    public Integer getKey1() {
        return key1;
    }

    public void setKey1(Integer key1) {
        this.key1 = key1;
    }

    @Id
    @Column(name = "key2", nullable = false, length = 40)
    public String getKey2() {
        return key2;
    }

    public void setKey2(String key2) {
        this.key2 = key2;
    }

    @Basic(optional = false)
    @Column(name = "value", nullable = false)
    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    @ManyToMany
    @JoinTable(name = "id_class_entity_children", joinColumns = {
            @JoinColumn(name = "child_key1", nullable = false, referencedColumnName = "key1"),
            @JoinColumn(name = "child_key2", nullable = false, referencedColumnName = "key2")
    }, inverseJoinColumns = {
            @JoinColumn(name = "parent_key1", nullable = false, referencedColumnName = "key1"),
            @JoinColumn(name = "parent_key2", nullable = false, referencedColumnName = "key2")
    })
    public Set<IdClassEntity> getChildren() {
        return children;
    }

    public void setChildren(Set<IdClassEntity> children) {
        this.children = children;
    }
}
