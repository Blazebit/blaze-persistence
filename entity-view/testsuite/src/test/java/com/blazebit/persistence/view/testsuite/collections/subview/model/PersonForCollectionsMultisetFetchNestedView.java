/*
 * Copyright 2014 - 2022 Blazebit.
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

package com.blazebit.persistence.view.testsuite.collections.subview.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.MappingCorrelatedSimple;
import com.blazebit.persistence.view.UpdatableEntityView;
import com.blazebit.persistence.view.testsuite.collections.entity.simple.DocumentForCollections;
import com.blazebit.persistence.view.testsuite.collections.entity.simple.PersonForCollections;

import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
@UpdatableEntityView
@EntityView(PersonForCollections.class)
public interface PersonForCollectionsMultisetFetchNestedView {

    @IdMapping
    public Long getId();

    public String getName();

    @Mapping(fetch = FetchStrategy.MULTISET)
    public SubviewSimpleDocumentMultisetFetchView getPartnerDocument();

    @Mapping(fetch = FetchStrategy.MULTISET)
    public Set<SubviewDocumentMultisetFetchView> getOwnedDocuments();

    @MappingCorrelatedSimple(correlated = DocumentForCollections.class, fetch = FetchStrategy.MULTISET, correlationBasis = "this", correlationExpression = "id = EMBEDDING_VIEW(partnerDocument.id)")
    public SubviewSimpleDocumentMultisetFetchView getCorrelatedPartnerDocument();

    @MappingCorrelatedSimple(correlated = DocumentForCollections.class, fetch = FetchStrategy.MULTISET, correlationBasis = "this", correlationExpression = "owner.id = EMBEDDING_VIEW(id)")
    public Set<SubviewSimpleDocumentMultisetFetchView> getCorrelatedOwnedDocuments();
}
