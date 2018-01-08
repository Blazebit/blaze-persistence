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
 * Use the association directly. This wasn't possible with Hibernate because of HHH-2772 but is now because we implemented automatic rewriting with #341.
 * We still keep this around to catch possible regressions.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@EntityView(Document.class)
public interface DocumentSimpleCorrelationViewSubselectNormal extends DocumentCorrelationView {

    @MappingCorrelatedSimple(correlationBasis = "owner", correlationResult = "id", correlated = Person.class, correlationExpression = "this IN correlationKey", fetch = FetchStrategy.SUBSELECT)
    public Long getCorrelatedOwnerId();

    @MappingCorrelatedSimple(correlationBasis = "owner", correlated = Person.class, correlationExpression = "this IN correlationKey", fetch = FetchStrategy.SUBSELECT)
    public Person getCorrelatedOwner();

    @MappingCorrelatedSimple(correlationBasis = "owner", correlated = Person.class, correlationExpression = "this IN correlationKey", fetch = FetchStrategy.SUBSELECT)
    public SimplePersonCorrelatedSubView getCorrelatedOwnerView();

    @MappingCorrelatedSimple(correlationBasis = "owner", correlationResult = "id", correlated = Person.class, correlationExpression = "this IN correlationKey", fetch = FetchStrategy.SUBSELECT)
    public Set<Long> getCorrelatedOwnerIdList();

    @MappingCorrelatedSimple(correlationBasis = "owner", correlated = Person.class, correlationExpression = "this IN correlationKey", fetch = FetchStrategy.SUBSELECT)
    public Set<Person> getCorrelatedOwnerList();

    @MappingCorrelatedSimple(correlationBasis = "owner", correlated = Person.class, correlationExpression = "this IN correlationKey", fetch = FetchStrategy.SUBSELECT)
    public Set<SimplePersonCorrelatedSubView> getCorrelatedOwnerViewList();

    @MappingCorrelatedSimple(correlationBasis = "owner", correlationResult = "id", correlated = Document.class, correlationExpression = "owner IN correlationKey AND id NOT IN VIEW_ROOT(id)", fetch = FetchStrategy.SUBSELECT)
    public Set<Long> getOwnerRelatedDocumentIds();

    @MappingCorrelatedSimple(correlationBasis = "owner", correlated = Document.class, correlationExpression = "owner IN correlationKey AND id NOT IN VIEW_ROOT(id)", fetch = FetchStrategy.SUBSELECT)
    public Set<Document> getOwnerRelatedDocuments();

    @MappingCorrelatedSimple(correlationBasis = "owner", correlated = Document.class, correlationExpression = "owner IN correlationKey AND id NOT IN VIEW_ROOT(id)", fetch = FetchStrategy.SUBSELECT)
    public Set<SimpleDocumentCorrelatedView> getOwnerRelatedDocumentViews();

    @MappingCorrelatedSimple(correlationBasis = "owner", correlationResult = "id", correlated = Document.class, correlationExpression = "owner IN correlationKey", fetch = FetchStrategy.SUBSELECT)
    public Set<Long> getOwnerOnlyRelatedDocumentIds();

    @MappingCorrelatedSimple(correlationBasis = "owner", correlated = Document.class, correlationExpression = "owner IN correlationKey", fetch = FetchStrategy.SUBSELECT)
    public Set<Document> getOwnerOnlyRelatedDocuments();

    @MappingCorrelatedSimple(correlationBasis = "owner", correlated = Document.class, correlationExpression = "owner IN correlationKey", fetch = FetchStrategy.SUBSELECT)
    public Set<SimpleDocumentCorrelatedView> getOwnerOnlyRelatedDocumentViews();

    @MappingCorrelatedSimple(correlationBasis = "this", correlated = Document.class, correlationExpression = "this IN correlationKey", correlationResult = "id", fetch = FetchStrategy.SUBSELECT)
    public Long getThisCorrelatedId();

    @MappingCorrelatedSimple(correlationBasis = "this", correlated = Document.class, correlationExpression = "this IN correlationKey", fetch = FetchStrategy.SUBSELECT)
    public Document getThisCorrelatedEntity();

    @MappingCorrelatedSimple(correlationBasis = "this", correlated = Document.class, correlationExpression = "this IN correlationKey", fetch = FetchStrategy.SUBSELECT)
    public SimpleDocumentCorrelatedView getThisCorrelatedView();

    @MappingCorrelatedSimple(correlationBasis = "this", correlated = Document.class, correlationExpression = "this IN correlationKey", correlationResult = "id", fetch = FetchStrategy.SUBSELECT)
    public Set<Long> getThisCorrelatedIdList();

    @MappingCorrelatedSimple(correlationBasis = "this", correlated = Document.class, correlationExpression = "this IN correlationKey", fetch = FetchStrategy.SUBSELECT)
    public Set<Document> getThisCorrelatedEntityList();

    @MappingCorrelatedSimple(correlationBasis = "this", correlated = Document.class, correlationExpression = "this IN correlationKey", fetch = FetchStrategy.SUBSELECT)
    public Set<SimpleDocumentCorrelatedView> getThisCorrelatedViewList();

}
