/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.subview.model;

import com.blazebit.persistence.view.AttributeFilter;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.MappingParameter;
import com.blazebit.persistence.view.filter.ContainsFilter;
import com.blazebit.persistence.testsuite.entity.Person;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@EntityView(Person.class)
public interface PersonSubViewFiltered {
    
    @IdMapping
    public Long getId();

    @AttributeFilter(ContainsFilter.class)
    public String getName();

    @MappingParameter("contactPersonNumber")
    public Integer getContactPersonNumber();
}
