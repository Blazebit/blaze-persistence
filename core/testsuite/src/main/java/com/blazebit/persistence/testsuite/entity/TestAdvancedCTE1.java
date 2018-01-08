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

import com.blazebit.persistence.CTE;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.io.Serializable;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@Entity
@CTE
public class TestAdvancedCTE1 implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private TestCTEEmbeddable embeddable;
    private Integer level;
    private RecursiveEntity parent;
    private Long parentId;

    // overrides
    private String name;

    @Id
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Embedded
    public TestCTEEmbeddable getEmbeddable() {
        return embeddable;
    }

    public void setEmbeddable(TestCTEEmbeddable embeddable) {
        this.embeddable = embeddable;
    }

    @Column(name = "nesting_level")
    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    public RecursiveEntity getParent() {
        return parent;
    }
    
    public void setParent(RecursiveEntity owner) {
        this.parent = owner;
    }

    @Column(name = "parent_id", insertable = false, updatable = false)
    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long ownerId) {
        this.parentId = ownerId;
    }

    @Column(name = "name", insertable = false, updatable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
