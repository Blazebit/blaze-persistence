/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.collections.subview.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.UpdatableEntityView;
import com.blazebit.persistence.view.testsuite.collections.entity.simple.PersonForCollections;

import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
@EntityView(PersonForCollections.class)
@UpdatableEntityView
public interface SubviewPersonForCollectionsSelectFetchView {
    
    @IdMapping("this")
    public Id getId();

    public String getName();

    public Set<SubviewPersonForCollectionsView> getSomeCollection();

    @EntityView(PersonForCollections.class)
    interface Id {
        Long getId();
        @Mapping("CASE WHEN id IS NOT NULL THEN 'test' END")
        String getTest();
    }

}
