/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.io.Serializable;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "legacy_order_pos")
public class LegacyOrderPosition implements Serializable {

    private LegacyOrderPositionId id;
    private LegacyOrder order;
    private String articleNumber;
    private Calendar creationDate;
    private LegacyOrderPositionEmbeddable embeddable = new LegacyOrderPositionEmbeddable();
    private Set<LegacyOrderPositionDefault> defaults = new HashSet<>();
    private Set<LegacyOrderPositionElement> elems = new HashSet<>();

    public LegacyOrderPosition() {
    }

    public LegacyOrderPosition(LegacyOrderPositionId id) {
        this.id = id;
    }

    @EmbeddedId
    public LegacyOrderPositionId getId() {
        return id;
    }

    public void setId(LegacyOrderPositionId id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "pos_order_id",
            insertable = false,
            updatable = false,
            nullable = false
    )
    public LegacyOrder getOrder() {
        return order;
    }

    public void setOrder(LegacyOrder order) {
        this.order = order;
    }

    @Column(nullable = false)
    public String getArticleNumber() {
        return articleNumber;
    }

    public void setArticleNumber(String articleNumber) {
        this.articleNumber = articleNumber;
    }

    @OneToMany(mappedBy = "orderPosition")
    public Set<LegacyOrderPositionDefault> getDefaults() {
        return defaults;
    }

    public void setDefaults(Set<LegacyOrderPositionDefault> defaults) {
        this.defaults = defaults;
    }

    @Temporal(TemporalType.TIMESTAMP)
    public Calendar getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Calendar creationDate) {
        this.creationDate = creationDate;
    }

    @Embedded
    public LegacyOrderPositionEmbeddable getEmbeddable() {
        return embeddable;
    }

    public void setEmbeddable(LegacyOrderPositionEmbeddable embeddable) {
        this.embeddable = embeddable;
    }

    @OneToMany
    @JoinColumns({
            @JoinColumn(
                    name = "elem_order_id",
                    referencedColumnName = "pos_order_id",
                    updatable = false),
            @JoinColumn(
                    name = "elem_position",
                    referencedColumnName = "pos_position",
                    updatable = false)
    })
    public Set<LegacyOrderPositionElement> getElems() {
        return elems;
    }

    public void setElems(Set<LegacyOrderPositionElement> elems) {
        this.elems = elems;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof LegacyOrderPosition)) {
            return false;
        }

        LegacyOrderPosition that = (LegacyOrderPosition) o;

        return getId() != null ? getId().equals(that.getId()) : that.getId() == null;
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }
}
