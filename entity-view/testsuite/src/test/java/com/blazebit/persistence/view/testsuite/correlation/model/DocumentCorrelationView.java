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

package com.blazebit.persistence.view.testsuite.correlation.model;

import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;

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

}
