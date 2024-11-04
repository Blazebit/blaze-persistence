/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.correlation.joinable.model;

import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.AttributeFilter;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.MappingCorrelatedSimple;
import com.blazebit.persistence.view.filter.EqualFilter;
import com.blazebit.persistence.view.testsuite.correlation.model.DocumentCorrelationView;
import com.blazebit.persistence.view.testsuite.correlation.model.SimpleDocumentCorrelatedView;
import com.blazebit.persistence.view.testsuite.correlation.model.SimplePersonCorrelatedSubView;

import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.1
 */
@EntityView(Document.class)
public interface DocumentJoinableCorrelationView {

    @IdMapping
    public Long getId();

    @MappingCorrelatedSimple(
            correlationBasis = "COALESCE(owner.id, 1)",
            correlated = Person.class,
            correlationExpression = "id IN correlationKey",
            correlationResult = "friend",
            fetch = FetchStrategy.JOIN
    )
    public SimplePersonCorrelatedSubView getCorrelatedOwnerFriendView();

}
