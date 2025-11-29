/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.subview.treat.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * @author Moritz Becker
 * @since 1.4.0
 */
@Entity
@Table(name = "container")
public class Container {
    @ManyToOne
    private BaseContainerItem item;

    @Id
    private Long id;

    public Container() {
    }

    public Container(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public BaseContainerItem getItem() {
        return item;
    }

    public void setItem(BaseContainerItem item) {
        this.item = item;
    }
}
