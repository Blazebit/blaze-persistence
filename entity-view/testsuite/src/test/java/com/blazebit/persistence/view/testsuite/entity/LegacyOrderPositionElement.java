/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.entity;

import com.blazebit.persistence.testsuite.entity.LongSequenceEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
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
