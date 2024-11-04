/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.entity;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
@Entity
@Table(name = "ORDER_POS")
public class OrderPosition implements Serializable {
    private static final long serialVersionUID = 1L;

    private OrderPositionId id;
    private Order order;
    private OrderPositionHead head;
    private Set<OrderPositionElement> elements = new HashSet<>(0);

    @EmbeddedId
    public OrderPositionId getId() {
        return id;
    }

    public void setId(OrderPositionId id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(insertable = false, updatable = false, nullable = false)
    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(
            name = "orderId",
            referencedColumnName = "head_order_id",
            insertable = false,
            updatable = false),
        @JoinColumn(
            name = "position",
            referencedColumnName = "head_position",
            insertable = false,
            updatable = false)})
    public OrderPositionHead getHead() {
        return head;
    }

    public void setHead(OrderPositionHead head) {
        this.head = head;
    }

    @OneToMany
    @JoinColumns({
        @JoinColumn(
            name = "elem_order_id",
            referencedColumnName = "orderId",
            updatable = false),
        @JoinColumn(
            name = "elem_position",
            referencedColumnName = "position",
            updatable = false)
    })
    public Set<OrderPositionElement> getElements() {
        return elements;
    }

    public void setElements(Set<OrderPositionElement> elements) {
        this.elements = elements;
    }
}
