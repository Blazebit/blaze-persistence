/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class LegacyOrderPositionId implements Serializable {

    private Long orderId;
    private Integer positionId;

    public LegacyOrderPositionId() {
    }

    public LegacyOrderPositionId(Long orderId, Integer positionId) {
        this.orderId = orderId;
        this.positionId = positionId;
    }

    @Column(name = "pos_order_id")
    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    @Column(name = "pos_position")
    public Integer getPositionId() {
        return positionId;
    }

    public void setPositionId(Integer positionId) {
        this.positionId = positionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof LegacyOrderPositionId)) {
            return false;
        }

        LegacyOrderPositionId that = (LegacyOrderPositionId) o;

        if (orderId != null ? !orderId.equals(that.orderId) : that.orderId != null) {
            return false;
        }
        return positionId != null ? positionId.equals(that.positionId) : that.positionId == null;
    }

    @Override
    public int hashCode() {
        int result = orderId != null ? orderId.hashCode() : 0;
        result = 31 * result + (positionId != null ? positionId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "LegacyOrderPositionId{" +
                "orderId=" + orderId +
                ", positionId=" + positionId +
                '}';
    }
}
