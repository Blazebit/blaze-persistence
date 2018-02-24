/*
 * Copyright 2014 - 2018 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blazebit.persistence.testsuite.entity;

import java.io.Serializable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 *
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
    
    @EmbeddedId
    public OrderPositionId getId() {
        return id;
    }

    public void setId(OrderPositionId id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.LAZY)
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
                    updatable = false) })
    public OrderPositionHead getHead() {
        return head;
    }

    public void setHead(OrderPositionHead head) {
        this.head = head;
    }

}
