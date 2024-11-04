/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.filter.basic.model;

import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.view.AttributeFilter;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.filter.GreaterOrEqualFilter;

/**
 * @author Christian Beikov
 * @since 1.6.8
 */
@EntityView(Document.class)
public interface AggregateFilterDocumentView {
    @IdMapping
    Long getAge();

    @AttributeFilter(GreaterOrEqualFilter.class)
    @Mapping("count(*)")
    String getCount();
}
