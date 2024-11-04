/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.cache.model;

import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.MappingCorrelatedSimple;

@EntityView(Document.class)
public interface DocumentCorrelatingOwner1View {

    @IdMapping
    Long getId();

    String getName();

    @MappingCorrelatedSimple(
            correlated = Person.class,
            correlationBasis = "this",
            correlationExpression = "id = EMBEDDING_VIEW(owner.id)",
            correlationResult = "friend",
            fetch = FetchStrategy.SELECT)
    PersonView getOwner();
}
