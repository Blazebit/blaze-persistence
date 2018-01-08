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

package com.blazebit.persistence.view.testsuite.correlation.simple.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.MappingCorrelatedSimple;
import com.blazebit.persistence.view.testsuite.correlation.model.DocumentCorrelationView;
import com.blazebit.persistence.view.testsuite.correlation.model.SimpleDocumentCorrelatedView;
import com.blazebit.persistence.view.testsuite.correlation.model.SimplePersonCorrelatedSubView;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;

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
public interface DocumentSimpleCorrelationViewJoinId extends DocumentCorrelationView {

    @MappingCorrelatedSimple(correlationBasis = "owner.id", correlationResult = "id", correlated = Person.class, correlationExpression = "id IN correlationKey", fetch = FetchStrategy.JOIN)
    public Long getCorrelatedOwnerId();

    @MappingCorrelatedSimple(correlationBasis = "owner.id", correlated = Person.class, correlationExpression = "id IN correlationKey", fetch = FetchStrategy.JOIN)
    public Person getCorrelatedOwner();

    @MappingCorrelatedSimple(correlationBasis = "owner.id", correlated = Person.class, correlationExpression = "id IN correlationKey", fetch = FetchStrategy.JOIN)
    public SimplePersonCorrelatedSubView getCorrelatedOwnerView();

    @MappingCorrelatedSimple(correlationBasis = "owner.id", correlationResult = "id", correlated = Person.class, correlationExpression = "id IN correlationKey", fetch = FetchStrategy.JOIN)
    public Set<Long> getCorrelatedOwnerIdList();

    @MappingCorrelatedSimple(correlationBasis = "owner.id", correlated = Person.class, correlationExpression = "id IN correlationKey", fetch = FetchStrategy.JOIN)
    public Set<Person> getCorrelatedOwnerList();

    @MappingCorrelatedSimple(correlationBasis = "owner.id", correlated = Person.class, correlationExpression = "id IN correlationKey", fetch = FetchStrategy.JOIN)
    public Set<SimplePersonCorrelatedSubView> getCorrelatedOwnerViewList();

    @MappingCorrelatedSimple(correlationBasis = "owner.id", correlationResult = "id", correlated = Document.class, correlationExpression = "owner.id IN correlationKey AND id NOT IN VIEW_ROOT(id)", fetch = FetchStrategy.JOIN)
    public Set<Long> getOwnerRelatedDocumentIds();

    @MappingCorrelatedSimple(correlationBasis = "owner.id", correlated = Document.class, correlationExpression = "owner.id IN correlationKey AND id NOT IN VIEW_ROOT(id)", fetch = FetchStrategy.JOIN)
    public Set<Document> getOwnerRelatedDocuments();

    @MappingCorrelatedSimple(correlationBasis = "owner.id", correlated = Document.class, correlationExpression = "owner.id IN correlationKey AND id NOT IN VIEW_ROOT(id)", fetch = FetchStrategy.JOIN)
    public Set<SimpleDocumentCorrelatedView> getOwnerRelatedDocumentViews();

    @MappingCorrelatedSimple(correlationBasis = "owner.id", correlationResult = "id", correlated = Document.class, correlationExpression = "owner.id IN correlationKey", fetch = FetchStrategy.JOIN)
    public Set<Long> getOwnerOnlyRelatedDocumentIds();

    @MappingCorrelatedSimple(correlationBasis = "owner.id", correlated = Document.class, correlationExpression = "owner.id IN correlationKey", fetch = FetchStrategy.JOIN)
    public Set<Document> getOwnerOnlyRelatedDocuments();

    @MappingCorrelatedSimple(correlationBasis = "owner.id", correlated = Document.class, correlationExpression = "owner.id IN correlationKey", fetch = FetchStrategy.JOIN)
    public Set<SimpleDocumentCorrelatedView> getOwnerOnlyRelatedDocumentViews();

    @MappingCorrelatedSimple(correlationBasis = "this", correlationResult = "id", correlated = Document.class, correlationExpression = "this IN correlationKey", fetch = FetchStrategy.JOIN)
    public Long getThisCorrelatedId();

    @MappingCorrelatedSimple(correlationBasis = "this", correlated = Document.class, correlationExpression = "this IN correlationKey", fetch = FetchStrategy.JOIN)
    public Document getThisCorrelatedEntity();

    @MappingCorrelatedSimple(correlationBasis = "this", correlated = Document.class, correlationExpression = "this IN correlationKey", fetch = FetchStrategy.JOIN)
    public SimpleDocumentCorrelatedView getThisCorrelatedView();

    @MappingCorrelatedSimple(correlationBasis = "this", correlationResult = "id", correlated = Document.class, correlationExpression = "this IN correlationKey", fetch = FetchStrategy.JOIN)
    public Set<Long> getThisCorrelatedIdList();

    @MappingCorrelatedSimple(correlationBasis = "this", correlated = Document.class, correlationExpression = "this IN correlationKey", fetch = FetchStrategy.JOIN)
    public Set<Document> getThisCorrelatedEntityList();

    @MappingCorrelatedSimple(correlationBasis = "this", correlated = Document.class, correlationExpression = "this IN correlationKey", fetch = FetchStrategy.JOIN)
    public Set<SimpleDocumentCorrelatedView> getThisCorrelatedViewList();

}
