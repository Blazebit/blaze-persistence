/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.subquery.model;

import com.blazebit.persistence.view.AttributeFilter;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.MappingSubquery;
import com.blazebit.persistence.view.filter.GreaterThanFilter;
import com.blazebit.persistence.testsuite.entity.Document;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@EntityView(Document.class)
public interface DocumentWithSubquery {
    
    @IdMapping
    public Long getId();

    public String getName();

    @AttributeFilter(GreaterThanFilter.class)
    @MappingSubquery(TestSubqueryProvider.class)
    public Long getContactCount();
}
