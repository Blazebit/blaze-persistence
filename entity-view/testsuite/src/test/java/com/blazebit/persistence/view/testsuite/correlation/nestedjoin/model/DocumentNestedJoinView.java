/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.correlation.nestedjoin.model;

import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.MappingCorrelatedSimple;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
@EntityView(Document.class)
public interface DocumentNestedJoinView {

    @IdMapping
    public Long getId();

    @MappingCorrelatedSimple(
            correlationBasis = "COALESCE(owner.id, 1)",
            correlated = Person.class,
            correlationExpression = "id IN correlationKey",
            correlationResult = "friend",
            fetch = FetchStrategy.JOIN
    )
    public PersonNestedJoinSubView getCorrelatedOwnerFriendView();

}
