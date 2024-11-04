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
@Entity(name = "KeyedNode")
public class KeyedNode {

    @Id
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY)
    private Root parent;
    @Column(name = "map_key", length = 10)
    private String key;

    public KeyedNode() {
    }

    public KeyedNode(Integer id) {
        this.id = id;
    }

    public KeyedNode(Integer id, Root parent) {
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

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
