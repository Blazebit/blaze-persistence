/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.correlation.embedding.model;

import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.view.IdMapping;

import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
public interface DocumentEmbeddingCorrelationView {

    @IdMapping
    public Long getId();

    public String getName();

    public Set<Long> getOwnerRelatedDocumentIds();

    public Set<Document> getOwnerRelatedDocuments();

    public Set<SimpleDocumentEmbeddingCorrelatedView> getOwnerRelatedDocumentViews();

}
