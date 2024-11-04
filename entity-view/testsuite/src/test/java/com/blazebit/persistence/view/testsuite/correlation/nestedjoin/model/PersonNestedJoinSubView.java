/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.correlation.nestedjoin.model;

import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.AttributeFilter;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.MappingCorrelatedSimple;
import com.blazebit.persistence.view.filter.ContainsFilter;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
@EntityView(Person.class)
public interface PersonNestedJoinSubView {
    
    @IdMapping
    public Long getId();

    @Mapping("UPPER(name)")
    @AttributeFilter(ContainsFilter.class)
    public String getName();

    @MappingCorrelatedSimple(
            correlationBasis = "COALESCE(partnerDocument.id, 1)",
            correlated = Document.class,
            correlationExpression = "id IN correlationKey",
            fetch = FetchStrategy.JOIN
    )
    public SimpleDocumentView getPartnerDocument();
}
