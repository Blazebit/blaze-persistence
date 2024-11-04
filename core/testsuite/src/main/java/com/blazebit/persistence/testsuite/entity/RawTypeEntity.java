/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.MapKeyClass;
import javax.persistence.MapKeyColumn;
import javax.persistence.MapKeyJoinColumn;
import javax.persistence.OneToMany;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@Entity
public class RawTypeEntity {

    @Id
    private Integer id;

    // Raw types are on purpose
    @OneToMany(targetEntity = RawTypeEntity.class)
    @JoinTable(name = "schema_list")
    private List list;

    // Raw types are on purpose
    @OneToMany(targetEntity = RawTypeEntity.class)
    @JoinTable(name = "schema_set")
    private Set set;

    // Raw types are on purpose
    @OneToMany(targetEntity = RawTypeEntity.class)
    @MapKeyClass(Integer.class)
    @JoinTable(name = "schema_map_1")
    @MapKeyColumn(nullable = false)
    private Map map;

    // Raw types are on purpose
    @OneToMany(targetEntity = RawTypeEntity.class)
    @MapKeyClass(RawTypeEntity.class)
    @JoinTable(name = "schema_map_2")
    @MapKeyJoinColumn(nullable = false)
    private Map map2;

}
