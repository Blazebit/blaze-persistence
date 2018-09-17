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

package com.blazebit.persistence.view.testsuite.collections.multiple.model;

import com.blazebit.persistence.testsuite.entity.IdClassEntity;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.testsuite.collections.entity.simple.DocumentForCollections;

import java.util.Comparator;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
@EntityView(IdClassEntity.class)
public interface IdClassEntityIdView {
    
    @IdMapping("this")
    public Id getId();

    @EntityView(IdClassEntity.class)
    interface Id {
        Integer getKey1();
        String getKey2();
    }

    static class AscendingOrderComparator implements Comparator<IdClassEntityIdView> {
        @Override
        public int compare(IdClassEntityIdView o1, IdClassEntityIdView o2) {
            int cmp = o1.getId().getKey1().compareTo(o2.getId().getKey1());
            if (cmp == 0) {
                return o1.getId().getKey2().compareTo(o2.getId().getKey2());
            }
            return cmp;
        }
    }

    static class DescendingOrderComparator implements Comparator<IdClassEntityIdView> {
        @Override
        public int compare(IdClassEntityIdView o1, IdClassEntityIdView o2) {
            int cmp = o2.getId().getKey1().compareTo(o1.getId().getKey1());
            if (cmp == 0) {
                return o2.getId().getKey2().compareTo(o1.getId().getKey2());
            }
            return cmp;
        }
    }
}
