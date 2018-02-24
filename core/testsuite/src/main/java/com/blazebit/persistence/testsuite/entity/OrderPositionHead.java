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

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

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
