/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.fetch.normal.model;

import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.IdMapping;

import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface DocumentFetchView {

    @IdMapping
    public Long getId();

    public String getName();

    public Long getCorrelatedOwnerId();

    public Person getCorrelatedOwner();

    public SimplePersonFetchSubView getCorrelatedOwnerView();

    public Set<Long> getCorrelatedOwnerIdList();

    public Set<Person> getCorrelatedOwnerList();

    public Set<SimplePersonFetchSubView> getCorrelatedOwnerViewList();

    public Long getThisCorrelatedId();

    public Document getThisCorrelatedEntity();

    public SimpleDocumentFetchView getThisCorrelatedView();

    public Set<Long> getThisCorrelatedIdList();

    public Set<Document> getThisCorrelatedEntityList();

    public Set<SimpleDocumentFetchView> getThisCorrelatedViewList();

    public Set<Long> getPartnerIdList();

    public Set<Person> getPartnerList();

    public Set<SimplePersonFetchSubView> getPartnerViewList();

}
