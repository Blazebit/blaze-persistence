/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.fetch.subview.model;

import com.blazebit.persistence.testsuite.entity.Document;
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
public interface PersonSelectSubview {

    @IdMapping
    public Long getId();

    @Mapping(fetch = FetchStrategy.SELECT)
    public Set<Document> getOwnedDocuments();

}
