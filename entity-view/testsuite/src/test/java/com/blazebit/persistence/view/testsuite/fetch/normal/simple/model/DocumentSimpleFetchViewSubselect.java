/*
 * Copyright 2014 - 2018 Blazebit.
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

package com.blazebit.persistence.view.testsuite.fetch.normal.simple.model;

import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.testsuite.fetch.normal.model.DocumentFetchView;
import com.blazebit.persistence.view.testsuite.fetch.normal.model.SimpleDocumentFetchView;
import com.blazebit.persistence.view.testsuite.fetch.normal.model.SimplePersonFetchSubView;

import java.util.List;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@EntityView(Document.class)
public interface DocumentSimpleFetchViewSubselect extends DocumentFetchView {

    public List<String> getStrings();

    @Mapping(value = "owner.id", fetch = FetchStrategy.SUBSELECT)
    public Long getCorrelatedOwnerId();

    @Mapping(value = "owner", fetch = FetchStrategy.SUBSELECT)
    public Person getCorrelatedOwner();

    @Mapping(value = "owner", fetch = FetchStrategy.SUBSELECT)
    public SimplePersonFetchSubView getCorrelatedOwnerView();

    @Mapping(value = "owner.id", fetch = FetchStrategy.SUBSELECT)
    public Set<Long> getCorrelatedOwnerIdList();

    @Mapping(value = "owner", fetch = FetchStrategy.SUBSELECT)
    public Set<Person> getCorrelatedOwnerList();

    @Mapping(value = "owner", fetch = FetchStrategy.SUBSELECT)
    public Set<SimplePersonFetchSubView> getCorrelatedOwnerViewList();

    @Mapping(value = "this.id", fetch = FetchStrategy.SUBSELECT)
    public Long getThisCorrelatedId();

    @Mapping(value = "this", fetch = FetchStrategy.SUBSELECT)
    public Document getThisCorrelatedEntity();

    @Mapping(value = "this", fetch = FetchStrategy.SUBSELECT)
    public SimpleDocumentFetchView getThisCorrelatedView();

    @Mapping(value = "this.id", fetch = FetchStrategy.SUBSELECT)
    public Set<Long> getThisCorrelatedIdList();

    @Mapping(value = "this", fetch = FetchStrategy.SUBSELECT)
    public Set<Document> getThisCorrelatedEntityList();

    @Mapping(value = "this", fetch = FetchStrategy.SUBSELECT)
    public Set<SimpleDocumentFetchView> getThisCorrelatedViewList();

    @Mapping(value = "partners.id", fetch = FetchStrategy.SUBSELECT)
    public Set<Long> getPartnerIdList();

    @Mapping(value = "partners", fetch = FetchStrategy.SUBSELECT)
    public Set<Person> getPartnerList();

    @Mapping(value = "partners", fetch = FetchStrategy.SUBSELECT)
    public Set<SimplePersonFetchSubView> getPartnerViewList();

}
