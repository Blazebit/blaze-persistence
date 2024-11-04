/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.correlation.embedding.model;

import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;

import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
@EntityView(Document.class)
public interface SimpleDocumentEmbeddingCorrelatedView {
    
    @IdMapping
    public Long getId();

    public String getName();

    @Mapping("EMBEDDING_VIEW(owner)")
    public SimplePersonEmbeddingCorrelatedSubView getOwner();

    public Set<SimplePersonEmbeddingCorrelatedSubView> getPartners();

}
