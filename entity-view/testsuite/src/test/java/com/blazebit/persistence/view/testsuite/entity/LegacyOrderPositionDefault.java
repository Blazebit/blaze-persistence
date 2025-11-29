/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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

    @Column(name = "val")
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
