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

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "legacy_order_pos_def")
public class LegacyOrderPositionDefault implements Serializable {

    private LegacyOrderPositionDefaultId id;
    private LegacyOrderPosition orderPosition;
    private String value;

    public LegacyOrderPositionDefault() {
    }

    public LegacyOrderPositionDefault(LegacyOrderPositionDefaultId id) {
        this.id = id;
    }

    @EmbeddedId
    public LegacyOrderPositionDefaultId getId() {
        return id;
    }

    public void setId(LegacyOrderPositionDefaultId id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
        @JoinColumn(
                name = "def_order_id",
                referencedColumnName = "pos_order_id",
                insertable = false,
                updatable = false,
                nullable = false
        ),
        @JoinColumn(
                name = "def_position",
                referencedColumnName = "pos_position",
                insertable = false,
                updatable = false,
                nullable = false
        )
    })
    public LegacyOrderPosition getOrderPosition() {
        return orderPosition;
    }

    public void setOrderPosition(LegacyOrderPosition orderPosition) {
        this.orderPosition = orderPosition;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof LegacyOrderPositionDefault)) {
            return false;
        }

        LegacyOrderPositionDefault that = (LegacyOrderPositionDefault) o;

        return getId() != null ? getId().equals(that.getId()) : that.getId() == null;
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }
}
