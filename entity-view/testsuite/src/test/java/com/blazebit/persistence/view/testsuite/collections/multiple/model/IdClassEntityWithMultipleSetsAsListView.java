/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.collections.multiple.model;

import com.blazebit.persistence.testsuite.entity.IdClassEntity;
import com.blazebit.persistence.view.CollectionMapping;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.testsuite.collections.entity.simple.DocumentForCollections;

import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
@EntityView(IdClassEntity.class)
public interface IdClassEntityWithMultipleSetsAsListView extends IdClassEntityIdView {
    
    public Integer getValue();

    @CollectionMapping(comparator = AscendingOrderComparator.class)
    public List<IdClassEntityIdView> getChildren();

    @CollectionMapping(comparator = DescendingOrderComparator.class)
    public List<IdClassEntityIdView> getChildren2();
}
