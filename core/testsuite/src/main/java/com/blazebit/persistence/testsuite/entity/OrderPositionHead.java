/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.entity;

import java.io.Serializable;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
@Entity
@Table(name = "order_pos_head")
public class OrderPositionHead implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private OrderPositionHeadId id;
    private Integer number;

    // Note that DataNucleus requires this override to be directly on the id
    @EmbeddedId
    @AttributeOverrides({
        @AttributeOverride(name = "orderId", column = @Column(name = "head_order_id")),
        @AttributeOverride(name = "position", column = @Column(name = "head_position"))
    })
    public OrderPositionHeadId getId() {
        return id;
    }

    public void setId(OrderPositionHeadId id) {
        this.id = id;
    }

    @Column(name = "order_nr")
    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }
}
