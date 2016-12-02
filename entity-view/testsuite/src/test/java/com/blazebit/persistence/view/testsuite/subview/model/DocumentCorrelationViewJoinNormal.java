/*
 * Copyright 2014 - 2016 Blazebit.
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

package com.blazebit.persistence.view.testsuite.subview.model;

import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.MappingCorrelated;
import com.blazebit.persistence.view.testsuite.entity.Document;

import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@EntityView(Document.class)
public interface DocumentCorrelationViewJoinNormal extends DocumentCorrelationView {

    @MappingCorrelated(correlationBasis = "owner", correlationResult = "correlatedDocumentForId.id", correlator = OwnerRelatedCorrelationIdProviderNormal.class, fetch = FetchStrategy.JOIN)
    public Set<Long> getOwnerRelatedDocumentIds();

    @MappingCorrelated(correlationBasis = "owner", correlationResult = "correlatedDocumentForSubview", correlator = OwnerRelatedCorrelationProviderNormal.class, fetch = FetchStrategy.JOIN)
    public Set<DocumentRelatedView> getOwnerRelatedDocuments();

    @MappingCorrelated(correlationBasis = "owner", correlationResult = "correlatedDocumentOnlyForId.id", correlator = OwnerOnlyRelatedCorrelationIdProviderNormal.class, fetch = FetchStrategy.JOIN)
    public Set<Long> getOwnerOnlyRelatedDocumentIds();

    @MappingCorrelated(correlationBasis = "owner", correlationResult = "correlatedDocumentOnlyForSubview", correlator = OwnerOnlyRelatedCorrelationProviderNormal.class, fetch = FetchStrategy.JOIN)
    public Set<DocumentRelatedView> getOwnerOnlyRelatedDocuments();

}
