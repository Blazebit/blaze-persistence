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
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
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
    private Set<LegacyOrderPositionDefault> defaults = new HashSet<>();

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
