/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.fetch.normal.model;

import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.AttributeFilter;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.filter.ContainsFilter;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@EntityView(Person.class)
public interface SimplePersonFetchSubView {
    
    @IdMapping
    public Long getId();

    @Mapping("UPPER(name)")
    @AttributeFilter(ContainsFilter.class)
    public String getName();
}
