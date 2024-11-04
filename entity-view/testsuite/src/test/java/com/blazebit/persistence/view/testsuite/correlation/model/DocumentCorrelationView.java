/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.correlation.model;

import com.blazebit.persistence.testsuite.entity.Version;
import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.MappingCorrelatedSimple;

import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface DocumentCorrelationView {

    @IdMapping
    public Long getId();

    public String getName();

    public Long getCorrelatedOwnerId();

    public Person getCorrelatedOwner();

    public SimplePersonCorrelatedSubView getCorrelatedOwnerView();

    public Set<Long> getCorrelatedOwnerIdList();

    public Set<Person> getCorrelatedOwnerList();

    public Set<SimplePersonCorrelatedSubView> getCorrelatedOwnerViewList();

    public Set<Long> getOwnerRelatedDocumentIds();

    public Set<Document> getOwnerRelatedDocuments();

    public Set<SimpleDocumentCorrelatedView> getOwnerRelatedDocumentViews();

    public Set<Long> getOwnerOnlyRelatedDocumentIds();

    public Set<Document> getOwnerOnlyRelatedDocuments();

    public Set<SimpleDocumentCorrelatedView> getOwnerOnlyRelatedDocumentViews();

    public Long getThisCorrelatedId();

    public Document getThisCorrelatedEntity();

    public SimpleDocumentCorrelatedView getThisCorrelatedView();

    public Set<Long> getThisCorrelatedIdList();

    public Set<Document> getThisCorrelatedEntityList();

    public Set<SimpleDocumentCorrelatedView> getThisCorrelatedViewList();

    public Set<Long> getThisCorrelatedEmptyIdList();

    public Set<Version> getThisCorrelatedEmptyEntityList();

    public Set<SimpleVersionCorrelatedView> getThisCorrelatedEmptyViewList();

}
