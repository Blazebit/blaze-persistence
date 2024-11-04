/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.subview.treat.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.Table;

/**
 * @author Moritz Becker
 * @since 1.4.0
 */
@Entity
@Table(name = "base_container_item")
@Inheritance
public abstract class BaseContainerItem {
    @Id
    private Long id;

    public BaseContainerItem() {
    }

    public BaseContainerItem(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
