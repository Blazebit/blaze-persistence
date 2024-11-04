/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.entity;

import com.blazebit.persistence.testsuite.entity.LongSequenceEntity;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "legacy_order")
public class LegacyOrder extends LongSequenceEntity {

    private Set<LegacyOrderPosition> positions = new HashSet<>();

    @OneToMany(mappedBy = "order")
    public Set<LegacyOrderPosition> getPositions() {
        return positions;
    }

    public void setPositions(Set<LegacyOrderPosition> positions) {
        this.positions = positions;
    }
}
