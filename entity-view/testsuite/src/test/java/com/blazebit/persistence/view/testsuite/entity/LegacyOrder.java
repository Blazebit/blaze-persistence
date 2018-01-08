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
