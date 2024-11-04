/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.correlation.subselectsubset.model;

import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.MappingCorrelatedSimple;

import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
@EntityView(Document.class)
public interface DocumentEmbeddingViewSubselectWithoutIdView {

    public String getName();

    @MappingCorrelatedSimple(correlated = Person.class, correlationBasis = "this", correlationExpression = "this = EMBEDDING_VIEW(owner)", fetch = FetchStrategy.SUBSELECT)
    public SimplePersonSubView getOwner();

}
