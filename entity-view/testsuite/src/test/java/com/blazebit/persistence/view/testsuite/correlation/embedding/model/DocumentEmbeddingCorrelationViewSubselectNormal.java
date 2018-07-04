/*
 * Copyright 2014 - 2018 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
