/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
@Entity(name = "Root2")
public class Root2 {

    @Id
    private Integer id;
    private String name;

    @OneToMany(mappedBy = "parent")
    @OrderColumn(name = "list_index")
    private List<IndexedNode2> indexedNodesMappedBy;
    @OneToMany(mappedBy = "parent")
    @MapKeyColumn(name = "map_key", length = 10)
    private Map<String, KeyedNode2> keyedNodesMappedBy;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<IndexedNode2> getIndexedNodesMappedBy() {
        return indexedNodesMappedBy;
    }

    public void setIndexedNodesMappedBy(List<IndexedNode2> indexedNodesMappedBy) {
        this.indexedNodesMappedBy = indexedNodesMappedBy;
    }

    public Map<String, KeyedNode2> getKeyedNodesMappedBy() {
        return keyedNodesMappedBy;
    }

    public void setKeyedNodesMappedBy(Map<String, KeyedNode2> keyedNodesMappedBy) {
        this.keyedNodesMappedBy = keyedNodesMappedBy;
    }
}
