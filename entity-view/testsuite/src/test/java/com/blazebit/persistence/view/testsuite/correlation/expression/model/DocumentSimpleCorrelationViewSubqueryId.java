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

package com.blazebit.persistence.view.testsuite.correlation.expression.model;

import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.MappingCorrelatedSimple;
import com.blazebit.persistence.view.testsuite.correlation.model.DocumentCorrelationView;
import com.blazebit.persistence.view.testsuite.correlation.model.SimpleDocumentCorrelatedView;
import com.blazebit.persistence.view.testsuite.correlation.model.SimplePersonCorrelatedSubView;

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
public interface DocumentSimpleCorrelationViewSubqueryId extends DocumentCorrelationView {

    @MappingCorrelatedSimple(correlationBasis = "COALESCE(owner.id, 1)", correlationResult = "id", correlated = Person.class, correlationExpression = "id IN correlationKey", fetch = FetchStrategy.SELECT)
    public Long getCorrelatedOwnerId();

    @MappingCorrelatedSimple(correlationBasis = "COALESCE(owner.id, 1)", correlated = Person.class, correlationExpression = "id IN correlationKey", fetch = FetchStrategy.SELECT)
    public Person getCorrelatedOwner();

    @MappingCorrelatedSimple(correlationBasis = "COALESCE(owner.id, 1)", correlated = Person.class, correlationExpression = "id IN correlationKey", fetch = FetchStrategy.SELECT)
    public SimplePersonCorrelatedSubView getCorrelatedOwnerView();

    @MappingCorrelatedSimple(correlationBasis = "COALESCE(owner.id, 1)", correlationResult = "id", correlated = Person.class, correlationExpression = "id IN correlationKey", fetch = FetchStrategy.SELECT)
    public Set<Long> getCorrelatedOwnerIdList();

    @MappingCorrelatedSimple(correlationBasis = "COALESCE(owner.id, 1)", correlated = Person.class, correlationExpression = "id IN correlationKey", fetch = FetchStrategy.SELECT)
    public Set<Person> getCorrelatedOwnerList();

    @MappingCorrelatedSimple(correlationBasis = "COALESCE(owner.id, 1)", correlated = Person.class, correlationExpression = "id IN correlationKey", fetch = FetchStrategy.SELECT)
    public Set<SimplePersonCorrelatedSubView> getCorrelatedOwnerViewList();

    @MappingCorrelatedSimple(correlationBasis = "COALESCE(owner.id, 1)", correlationResult = "id", correlated = Document.class, correlationExpression = "owner.id IN correlationKey AND id NOT IN VIEW_ROOT(id)", fetch = FetchStrategy.SELECT)
    public Set<Long> getOwnerRelatedDocumentIds();

    @MappingCorrelatedSimple(correlationBasis = "COALESCE(owner.id, 1)", correlated = Document.class, correlationExpression = "owner.id IN correlationKey AND id NOT IN VIEW_ROOT(id)", fetch = FetchStrategy.SELECT)
    public Set<Document> getOwnerRelatedDocuments();

    @MappingCorrelatedSimple(correlationBasis = "COALESCE(owner.id, 1)", correlated = Document.class, correlationExpression = "owner.id IN correlationKey AND id NOT IN VIEW_ROOT(id)", fetch = FetchStrategy.SELECT)
    public Set<SimpleDocumentCorrelatedView> getOwnerRelatedDocumentViews();

    @MappingCorrelatedSimple(correlationBasis = "COALESCE(owner.id, 1)", correlationResult = "id", correlated = Document.class, correlationExpression = "owner.id IN correlationKey", fetch = FetchStrategy.SELECT)
    public Set<Long> getOwnerOnlyRelatedDocumentIds();

    @MappingCorrelatedSimple(correlationBasis = "COALESCE(owner.id, 1)", correlated = Document.class, correlationExpression = "owner.id IN correlationKey", fetch = FetchStrategy.SELECT)
    public Set<Document> getOwnerOnlyRelatedDocuments();

    @MappingCorrelatedSimple(correlationBasis = "COALESCE(owner.id, 1)", correlated = Document.class, correlationExpression = "owner.id IN correlationKey", fetch = FetchStrategy.SELECT)
    public Set<SimpleDocumentCorrelatedView> getOwnerOnlyRelatedDocumentViews();

    @MappingCorrelatedSimple(correlationBasis = "this", correlationResult = "id", correlated = Document.class, correlationExpression = "this IN correlationKey", fetch = FetchStrategy.SELECT)
    public Long getThisCorrelatedId();

    @MappingCorrelatedSimple(correlationBasis = "this", correlated = Document.class, correlationExpression = "this IN correlationKey", fetch = FetchStrategy.SELECT)
    public Document getThisCorrelatedEntity();

    @MappingCorrelatedSimple(correlationBasis = "this", correlated = Document.class, correlationExpression = "this IN correlationKey", fetch = FetchStrategy.SELECT)
    public SimpleDocumentCorrelatedView getThisCorrelatedView();

    @MappingCorrelatedSimple(correlationBasis = "this", correlationResult = "id", correlated = Document.class, correlationExpression = "this IN correlationKey", fetch = FetchStrategy.SELECT)
    public Set<Long> getThisCorrelatedIdList();

    @MappingCorrelatedSimple(correlationBasis = "this", correlated = Document.class, correlationExpression = "this IN correlationKey", fetch = FetchStrategy.SELECT)
    public Set<Document> getThisCorrelatedEntityList();

    @MappingCorrelatedSimple(correlationBasis = "this", correlated = Document.class, correlationExpression = "this IN correlationKey", fetch = FetchStrategy.SELECT)
    public Set<SimpleDocumentCorrelatedView> getThisCorrelatedViewList();

}
