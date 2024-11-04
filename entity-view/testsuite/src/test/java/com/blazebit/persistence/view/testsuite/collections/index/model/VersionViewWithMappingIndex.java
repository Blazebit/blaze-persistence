/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.collections.index.model;

import com.blazebit.persistence.testsuite.entity.Version;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;

import java.util.Comparator;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
@EntityView(Version.class)
public interface VersionViewWithMappingIndex {

    class DefaultComparator implements Comparator<VersionViewWithMappingIndex> {
        @Override
        public int compare(VersionViewWithMappingIndex o1, VersionViewWithMappingIndex o2) {
            return o1.getId().compareTo(o2.getId());
        }
    }
    
    @IdMapping
    public Long getId();

}
