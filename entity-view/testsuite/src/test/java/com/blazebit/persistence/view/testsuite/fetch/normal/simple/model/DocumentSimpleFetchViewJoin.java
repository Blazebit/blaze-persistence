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
public interface DocumentSimpleFetchViewJoin extends DocumentFetchView {

    @Mapping(value = "owner.id", fetch = FetchStrategy.JOIN)
    public Long getCorrelatedOwnerId();

    @Mapping(value = "owner", fetch = FetchStrategy.JOIN)
    public Person getCorrelatedOwner();

    @Mapping(value = "owner", fetch = FetchStrategy.JOIN)
    public SimplePersonFetchSubView getCorrelatedOwnerView();

    @Mapping(value = "owner.id", fetch = FetchStrategy.JOIN)
    public Set<Long> getCorrelatedOwnerIdList();

    @Mapping(value = "owner", fetch = FetchStrategy.JOIN)
    public Set<Person> getCorrelatedOwnerList();

    @Mapping(value = "owner", fetch = FetchStrategy.JOIN)
    public Set<SimplePersonFetchSubView> getCorrelatedOwnerViewList();

    @Mapping(value = "this.id", fetch = FetchStrategy.JOIN)
    public Long getThisCorrelatedId();

    @Mapping(value = "this", fetch = FetchStrategy.JOIN)
    public Document getThisCorrelatedEntity();

    @Mapping(value = "this", fetch = FetchStrategy.JOIN)
    public SimpleDocumentFetchView getThisCorrelatedView();

    @Mapping(value = "this.id", fetch = FetchStrategy.JOIN)
    public Set<Long> getThisCorrelatedIdList();

    @Mapping(value = "this", fetch = FetchStrategy.JOIN)
    public Set<Document> getThisCorrelatedEntityList();

    @Mapping(value = "this", fetch = FetchStrategy.JOIN)
    public Set<SimpleDocumentFetchView> getThisCorrelatedViewList();

    @Mapping(value = "partners.id", fetch = FetchStrategy.JOIN)
    public Set<Long> getPartnerIdList();

    // NOTE: We use subselect here since we would otherwise get a configuration error. Join fetching the same plural attribute multiple times doesn't work yet

    @Mapping(value = "partners", fetch = FetchStrategy.SUBSELECT)
    public Set<Person> getPartnerList();

    @Mapping(value = "partners", fetch = FetchStrategy.SUBSELECT)
    public Set<SimplePersonFetchSubView> getPartnerViewList();

}
