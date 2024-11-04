/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.fetch.normal.simple.model;

import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.testsuite.fetch.normal.model.DocumentFetchView;
import com.blazebit.persistence.view.testsuite.fetch.normal.model.SimpleDocumentFetchView;
import com.blazebit.persistence.view.testsuite.fetch.normal.model.SimplePersonFetchSubView;

import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@EntityView(Document.class)
public interface DocumentSimpleFetchViewSubquery extends DocumentFetchView {

    @Mapping(value = "owner.id", fetch = FetchStrategy.SELECT)
    public Long getCorrelatedOwnerId();

    @Mapping(value = "owner", fetch = FetchStrategy.SELECT)
    public Person getCorrelatedOwner();

    @Mapping(value = "owner", fetch = FetchStrategy.SELECT)
    public SimplePersonFetchSubView getCorrelatedOwnerView();

    @Mapping(value = "owner.id", fetch = FetchStrategy.SELECT)
    public Set<Long> getCorrelatedOwnerIdList();

    @Mapping(value = "owner", fetch = FetchStrategy.SELECT)
    public Set<Person> getCorrelatedOwnerList();

    @Mapping(value = "owner", fetch = FetchStrategy.SELECT)
    public Set<SimplePersonFetchSubView> getCorrelatedOwnerViewList();

    @Mapping(value = "this.id", fetch = FetchStrategy.SELECT)
    public Long getThisCorrelatedId();

    @Mapping(value = "this", fetch = FetchStrategy.SELECT)
    public Document getThisCorrelatedEntity();

    @Mapping(value = "this", fetch = FetchStrategy.SELECT)
    public SimpleDocumentFetchView getThisCorrelatedView();

    @Mapping(value = "this.id", fetch = FetchStrategy.SELECT)
    public Set<Long> getThisCorrelatedIdList();

    @Mapping(value = "this", fetch = FetchStrategy.SELECT)
    public Set<Document> getThisCorrelatedEntityList();

    @Mapping(value = "this", fetch = FetchStrategy.SELECT)
    public Set<SimpleDocumentFetchView> getThisCorrelatedViewList();

    @Mapping(value = "partners.id", fetch = FetchStrategy.SELECT)
    public Set<Long> getPartnerIdList();

    @Mapping(value = "partners", fetch = FetchStrategy.SELECT)
    public Set<Person> getPartnerList();

    @Mapping(value = "partners", fetch = FetchStrategy.SELECT)
    public Set<SimplePersonFetchSubView> getPartnerViewList();

}
