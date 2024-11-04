/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
