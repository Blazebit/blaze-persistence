/*
 * Copyright 2014 Blazebit.
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

import com.blazebit.persistence.view.CorrelationStrategy;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.MappingCorrelated;
import com.blazebit.persistence.view.testsuite.entity.Document;

import java.util.List;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@EntityView(Document.class)
public interface DocumentCorrelationViewJoin extends DocumentCorrelationView {

    // TODO: need to use owner.id instead of owner because of HHH-2772
    @MappingCorrelated(correlationBasis = "owner.id", correlationResult = "correlatedDocumentForId.id", correlator = OwnerRelatedCorrelationIdProvider.class, strategy = CorrelationStrategy.JOIN)
    public Set<Long> getOwnerRelatedDocumentIds();

    // TODO: need to use owner.id instead of owner because of HHH-2772
    @MappingCorrelated(correlationBasis = "owner.id", correlationResult = "correlatedDocumentForSubview", correlator = OwnerRelatedCorrelationProvider.class, strategy = CorrelationStrategy.JOIN)
    public Set<DocumentRelatedView> getOwnerRelatedDocuments();

}
