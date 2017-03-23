/*
 * Copyright 2014 - 2017 Blazebit.
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

package com.blazebit.persistence.view.testsuite.correlation.general.model;

import com.blazebit.persistence.view.*;
import com.blazebit.persistence.view.testsuite.entity.Document;
import com.blazebit.persistence.view.testsuite.subview.model.DocumentRelatedView;

import java.util.Set;

/**
 * Use the id of the association instead of the association directly.
 * This was important because of HHH-2772 but isn't anymore because we implemented automatic rewriting with #341.
 * We still keep this around to catch possible regressions.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@EntityView(Document.class)
public interface DocumentCorrelationViewSubqueryId extends DocumentCorrelationView {

    @MappingCorrelated(correlationBasis = "owner.id", correlationResult = "id", correlator = OwnerRelatedCorrelationIdProviderId.class, fetch = FetchStrategy.SELECT)
    public Set<Long> getOwnerRelatedDocumentIds();

    @MappingCorrelated(correlationBasis = "owner.id", correlator = OwnerRelatedCorrelationProviderId.class, fetch = FetchStrategy.SELECT)
    public Set<DocumentRelatedView> getOwnerRelatedDocuments();

    @MappingCorrelated(correlationBasis = "owner.id", correlationResult = "id", correlator = OwnerOnlyRelatedCorrelationIdProviderId.class, fetch = FetchStrategy.SELECT)
    public Set<Long> getOwnerOnlyRelatedDocumentIds();

    @MappingCorrelated(correlationBasis = "owner.id", correlator = OwnerOnlyRelatedCorrelationProviderId.class, fetch = FetchStrategy.SELECT)
    public Set<DocumentRelatedView> getOwnerOnlyRelatedDocuments();

}
