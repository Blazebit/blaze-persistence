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
@Entity(name = "IndexedNode2")
public class IndexedNode2 {

    @Id
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY)
    private Root2 parent;
    @Column(name = "list_index")
    private Integer index;
}
