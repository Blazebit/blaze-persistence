/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.collections.subview.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.MappingCorrelatedSimple;
import com.blazebit.persistence.view.testsuite.collections.entity.simple.DocumentForCollections;
import com.blazebit.persistence.view.testsuite.collections.entity.simple.PersonForCollections;

import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
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
