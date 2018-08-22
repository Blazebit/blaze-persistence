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

package com.blazebit.persistence.view.testsuite.entity;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class LegacyOrderPositionDefaultId implements Serializable {

    private Long orderId;
    private Integer position;
    private Integer supplierId;

    public LegacyOrderPositionDefaultId() {
    }

    public LegacyOrderPositionDefaultId(Long orderId, Integer position) {
        this.orderId = orderId;
        this.position = position;
    }

    public LegacyOrderPositionDefaultId(LegacyOrderPositionId positionId, Integer supplierId) {
        this.orderId = positionId.getOrderId();
        this.position = positionId.getPositionId();
        this.supplierId = supplierId;
    }

    public LegacyOrderPositionDefaultId(Long orderId, Integer position, Integer supplierId) {
        this.orderId = orderId;
        this.position = position;
        this.supplierId = supplierId;
    }

    @Column(name = "def_order_id")
    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    @Column(name = "def_position")
    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    @Column(name = "def_supplier_id")
    public Integer getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Integer supplierId) {
        this.supplierId = supplierId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof LegacyOrderPositionDefaultId)) {
            return false;
        }

        LegacyOrderPositionDefaultId that = (LegacyOrderPositionDefaultId) o;

        if (orderId != null ? !orderId.equals(that.orderId) : that.orderId != null) {
            return false;
        }
        if (position != null ? !position.equals(that.position) : that.position != null) {
            return false;
        }
        return supplierId != null ? supplierId.equals(that.supplierId) : that.supplierId == null;
    }

    @Override
    public int hashCode() {
        int result = orderId != null ? orderId.hashCode() : 0;
        result = 31 * result + (position != null ? position.hashCode() : 0);
        result = 31 * result + (supplierId != null ? supplierId.hashCode() : 0);
        return result;
    }
}
