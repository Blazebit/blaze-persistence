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
 * Use the association directly. This wasn't possible with Hibernate because of HHH-2772 but is now because we implemented automatic rewriting with #341.
 * We still keep this around to catch possible regressions.
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
@EntityView(Document.class)
public interface DocumentEmbeddingCorrelationViewSubselectNormal extends DocumentEmbeddingCorrelationView {

    @MappingCorrelatedSimple(correlationBasis = "owner", correlationResult = "id", correlated = Document.class, correlationExpression = "owner NOT IN correlationKey AND id NOT IN EMBEDDING_VIEW(id)", fetch = FetchStrategy.SUBSELECT)
    public Set<Long> getOwnerRelatedDocumentIds();

    @MappingCorrelatedSimple(correlationBasis = "owner", correlated = Document.class, correlationExpression = "owner NOT IN correlationKey AND id NOT IN EMBEDDING_VIEW(id)", fetch = FetchStrategy.SUBSELECT)
    public Set<Document> getOwnerRelatedDocuments();

    @MappingCorrelatedSimple(correlationBasis = "owner", correlated = Document.class, correlationExpression = "owner NOT IN correlationKey AND id NOT IN EMBEDDING_VIEW(id)", fetch = FetchStrategy.SUBSELECT)
    public Set<SimpleDocumentEmbeddingCorrelatedView> getOwnerRelatedDocumentViews();

}
