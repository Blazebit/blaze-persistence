/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
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

    @OneToMany(mappedBy = "parent")
    @OrderColumn(name = "list_index")
    private List<IndexedNode2> indexedNodesMappedBy;
    @OneToMany(mappedBy = "parent")
    @MapKeyColumn(name = "map_key", length = 10)
    private Map<String, KeyedNode2> keyedNodesMappedBy;

}
