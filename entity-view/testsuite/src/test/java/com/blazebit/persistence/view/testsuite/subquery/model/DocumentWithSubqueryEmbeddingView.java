/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.subquery.model;

import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.MappingSubquery;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
@EntityView(Document.class)
public interface DocumentWithSubqueryEmbeddingView {
    
    @IdMapping
    public Long getId();

    public String getName();

    @MappingSubquery(TestEmbeddingViewSubqueryProvider.class)
    public Long getContactCount();
}
