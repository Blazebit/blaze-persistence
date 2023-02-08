/*
 * Copyright 2014 - 2023 Blazebit.
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

package com.blazebit.persistence.view.testsuite.entity;

import com.blazebit.persistence.testsuite.entity.LongSequenceEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
@Entity
@Table(name = "legacy_order_pos_elem")
public class LegacyOrderPositionElement extends LongSequenceEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private Long orderId;
    private Integer position;
    private String text;

    public LegacyOrderPositionElement() {
    }

    public LegacyOrderPositionElement(Long orderId, Integer position, String text) {
        this.orderId = orderId;
        this.position = position;
        this.text = text;
    }

    @Column(name = "elem_order_id")
    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    @Column(name = "elem_position")
    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
