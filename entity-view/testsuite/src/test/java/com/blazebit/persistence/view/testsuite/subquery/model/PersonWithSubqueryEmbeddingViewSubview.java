/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.subquery.model;

import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
@EntityView(Person.class)
public interface PersonWithSubqueryEmbeddingViewSubview {
    
    @IdMapping
    public Long getId();

    public String getName();

    // DataNucleus shortens on PostgreSQL only the select item alias: https://github.com/datanucleus/datanucleus-core/issues/300
    @Mapping("partnerDocument")
    public DocumentWithSubqueryEmbeddingView getPDocument();
}
