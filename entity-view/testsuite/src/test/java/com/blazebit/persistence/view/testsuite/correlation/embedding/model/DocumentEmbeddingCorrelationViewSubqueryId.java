/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.correlation.embedding.model;

import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.MappingCorrelatedSimple;

import java.util.Set;

/**
 * Use the id of the association instead of the association directly.
 * This was important because of HHH-2772 but isn't anymore because we implemented automatic rewriting with #341.
 * We still keep this around to catch possible regressions.
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
@EntityView(Document.class)
public interface DocumentEmbeddingCorrelationViewSubqueryId extends DocumentEmbeddingCorrelationView {

    @MappingCorrelatedSimple(correlationBasis = "owner.id", correlationResult = "id", correlated = Document.class, correlationExpression = "owner.id NOT IN correlationKey AND id NOT IN EMBEDDING_VIEW(id)", fetch = FetchStrategy.SELECT)
    public Set<Long> getOwnerRelatedDocumentIds();

    @MappingCorrelatedSimple(correlationBasis = "owner.id", correlated = Document.class, correlationExpression = "owner.id NOT IN correlationKey AND id NOT IN EMBEDDING_VIEW(id)", fetch = FetchStrategy.SELECT)
    public Set<Document> getOwnerRelatedDocuments();

    @MappingCorrelatedSimple(correlationBasis = "owner.id", correlated = Document.class, correlationExpression = "owner.id NOT IN correlationKey AND id NOT IN EMBEDDING_VIEW(id)", fetch = FetchStrategy.SELECT)
    public Set<SimpleDocumentEmbeddingCorrelatedView> getOwnerRelatedDocumentViews();

}
