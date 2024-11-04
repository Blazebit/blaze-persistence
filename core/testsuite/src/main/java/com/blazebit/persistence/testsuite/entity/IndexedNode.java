/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

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
