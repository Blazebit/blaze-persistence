/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.correlation.general.model;

import com.blazebit.persistence.testsuite.entity.Version;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.MappingCorrelated;
import com.blazebit.persistence.view.MappingCorrelatedSimple;
import com.blazebit.persistence.view.testsuite.correlation.model.DocumentCorrelationView;
import com.blazebit.persistence.view.testsuite.correlation.model.SimpleDocumentCorrelatedView;
import com.blazebit.persistence.view.testsuite.correlation.model.SimplePersonCorrelatedSubView;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.testsuite.correlation.model.SimpleVersionCorrelatedView;

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
public interface DocumentCorrelationViewSubselectId extends DocumentCorrelationView {

    @MappingCorrelated(correlationBasis = "owner.id", correlationResult = "id", correlator = OwnerCorrelationProviderId.class, fetch = FetchStrategy.SUBSELECT)
    public Long getCorrelatedOwnerId();

    @MappingCorrelated(correlationBasis = "owner.id", correlator = OwnerCorrelationProviderId.class, fetch = FetchStrategy.SUBSELECT)
    public Person getCorrelatedOwner();

    @MappingCorrelated(correlationBasis = "owner.id", correlator = OwnerCorrelationProviderId.class, fetch = FetchStrategy.SUBSELECT)
    public SimplePersonCorrelatedSubView getCorrelatedOwnerView();

    @MappingCorrelated(correlationBasis = "owner.id", correlationResult = "id", correlator = OwnerCorrelationProviderId.class, fetch = FetchStrategy.SUBSELECT)
    public Set<Long> getCorrelatedOwnerIdList();

    @MappingCorrelated(correlationBasis = "owner.id", correlator = OwnerCorrelationProviderId.class, fetch = FetchStrategy.SUBSELECT)
    public Set<Person> getCorrelatedOwnerList();

    @MappingCorrelated(correlationBasis = "owner.id", correlator = OwnerCorrelationProviderId.class, fetch = FetchStrategy.SUBSELECT)
    public Set<SimplePersonCorrelatedSubView> getCorrelatedOwnerViewList();

    @MappingCorrelated(correlationBasis = "owner.id", correlationResult = "id", correlator = OwnerRelatedCorrelationProviderId.class, fetch = FetchStrategy.SUBSELECT)
    public Set<Long> getOwnerRelatedDocumentIds();

    @MappingCorrelated(correlationBasis = "owner.id", correlator = OwnerRelatedCorrelationProviderId.class, fetch = FetchStrategy.SUBSELECT)
    public Set<Document> getOwnerRelatedDocuments();

    @MappingCorrelated(correlationBasis = "owner.id", correlator = OwnerRelatedCorrelationProviderId.class, fetch = FetchStrategy.SUBSELECT)
    public Set<SimpleDocumentCorrelatedView> getOwnerRelatedDocumentViews();

    @MappingCorrelated(correlationBasis = "owner.id", correlationResult = "id", correlator = OwnerOnlyRelatedCorrelationProviderId.class, fetch = FetchStrategy.SUBSELECT)
    public Set<Long> getOwnerOnlyRelatedDocumentIds();

    @MappingCorrelated(correlationBasis = "owner.id", correlator = OwnerOnlyRelatedCorrelationProviderId.class, fetch = FetchStrategy.SUBSELECT)
    public Set<Document> getOwnerOnlyRelatedDocuments();

    @MappingCorrelated(correlationBasis = "owner.id", correlator = OwnerOnlyRelatedCorrelationProviderId.class, fetch = FetchStrategy.SUBSELECT)
    public Set<SimpleDocumentCorrelatedView> getOwnerOnlyRelatedDocumentViews();

    @MappingCorrelated(correlationBasis = "this", correlator = ThisCorrelationProviderNormal.class, correlationResult = "id", fetch = FetchStrategy.SUBSELECT)
    public Long getThisCorrelatedId();

    @MappingCorrelated(correlationBasis = "this", correlator = ThisCorrelationProviderNormal.class, fetch = FetchStrategy.SUBSELECT)
    public Document getThisCorrelatedEntity();

    @MappingCorrelated(correlationBasis = "this", correlator = ThisCorrelationProviderNormal.class, fetch = FetchStrategy.SUBSELECT)
    public SimpleDocumentCorrelatedView getThisCorrelatedView();

    @MappingCorrelated(correlationBasis = "this", correlator = ThisCorrelationProviderNormal.class, correlationResult = "id", fetch = FetchStrategy.SUBSELECT)
    public Set<Long> getThisCorrelatedIdList();

    @MappingCorrelated(correlationBasis = "this", correlator = ThisCorrelationProviderNormal.class, fetch = FetchStrategy.SUBSELECT)
    public Set<Document> getThisCorrelatedEntityList();

    @MappingCorrelated(correlationBasis = "this", correlator = ThisCorrelationProviderNormal.class, fetch = FetchStrategy.SUBSELECT)
    public Set<SimpleDocumentCorrelatedView> getThisCorrelatedViewList();

    @MappingCorrelated(correlationBasis = "this", correlationResult = "versions.id", correlator = ThisCorrelationProviderNormal.class, fetch = FetchStrategy.SUBSELECT)
    public Set<Long> getThisCorrelatedEmptyIdList();

    @MappingCorrelated(correlationBasis = "this", correlationResult = "versions", correlator = ThisCorrelationProviderNormal.class, fetch = FetchStrategy.SUBSELECT)
    public Set<Version> getThisCorrelatedEmptyEntityList();

    @MappingCorrelated(correlationBasis = "this", correlationResult = "versions", correlator = ThisCorrelationProviderNormal.class, fetch = FetchStrategy.SUBSELECT)
    public Set<SimpleVersionCorrelatedView> getThisCorrelatedEmptyViewList();

}
