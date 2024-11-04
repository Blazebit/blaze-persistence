/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.correlation.subselectsubset.model;

import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.AttributeFilter;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.MappingCorrelatedSimple;
import com.blazebit.persistence.view.filter.ContainsIgnoreCaseFilter;

import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.4.1
 */
@EntityView(Document.class)
public interface DocumentSubselectElementCollectionView {

    @IdMapping
    public Long getId();

    @Mapping("lower(name)")
    @AttributeFilter(ContainsIgnoreCaseFilter.class)
    public String getName();

    @Mapping(fetch = FetchStrategy.SUBSELECT)
    public Set<NameObjectView> getNames();

}
