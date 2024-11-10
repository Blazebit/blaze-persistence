/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

/**
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
@Entity(name = "IndexedNode")
public class IndexedNode {

    @Id
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY)
    private Root parent;
    @Column(name = "list_index")
    private Integer index;

    public IndexedNode() {
    }

    public IndexedNode(Integer id) {
        this.id = id;
    }

    public IndexedNode(Integer id, Root parent) {
        this.id = id;
        this.parent = parent;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Root getParent() {
        return parent;
    }

    public void setParent(Root parent) {
        this.parent = parent;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }
}
