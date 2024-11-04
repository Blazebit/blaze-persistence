/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.correlation.subselectsubset.model;

import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;

import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
@EntityView(Person.class)
public interface PersonSubselectView {
    
    @IdMapping
    public Long getId();

    public String getName();

    @Mapping(value = "ownedDocuments.owner", fetch = FetchStrategy.SUBSELECT)
    public Set<SimplePersonSubView> getOwnedDocuments();
}
