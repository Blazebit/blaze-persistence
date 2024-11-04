/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.correlation.simple.model;

import com.blazebit.persistence.testsuite.entity.Version;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.MappingCorrelatedSimple;
import com.blazebit.persistence.view.testsuite.correlation.model.DocumentCorrelationView;
import com.blazebit.persistence.view.testsuite.correlation.model.SimpleDocumentCorrelatedView;
import com.blazebit.persistence.view.testsuite.correlation.model.SimplePersonCorrelatedSubView;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.testsuite.correlation.model.SimpleVersionCorrelatedView;

import java.util.Set;

/**
 * Use the association directly. This wasn't possible with Hibernate because of HHH-2772 but is now because we implemented automatic rewriting with #341.
 * We still keep this around to catch possible regressions.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@EntityView(Document.class)
public interface DocumentSimpleCorrelationViewSubqueryNormal extends DocumentCorrelationView {

    @MappingCorrelatedSimple(correlationBasis = "owner", correlationResult = "id", correlated = Person.class, correlationExpression = "this IN correlationKey", fetch = FetchStrategy.SELECT)
    public Long getCorrelatedOwnerId();

    @MappingCorrelatedSimple(correlationBasis = "owner", correlated = Person.class, correlationExpression = "this IN correlationKey", fetch = FetchStrategy.SELECT)
    public Person getCorrelatedOwner();

    @MappingCorrelatedSimple(correlationBasis = "owner", correlated = Person.class, correlationExpression = "this IN correlationKey", fetch = FetchStrategy.SELECT)
    public SimplePersonCorrelatedSubView getCorrelatedOwnerView();

    @MappingCorrelatedSimple(correlationBasis = "owner", correlationResult = "id", correlated = Person.class, correlationExpression = "this IN correlationKey", fetch = FetchStrategy.SELECT)
    public Set<Long> getCorrelatedOwnerIdList();

    @MappingCorrelatedSimple(correlationBasis = "owner", correlated = Person.class, correlationExpression = "this IN correlationKey", fetch = FetchStrategy.SELECT)
    public Set<Person> getCorrelatedOwnerList();

    @MappingCorrelatedSimple(correlationBasis = "owner", correlated = Person.class, correlationExpression = "this IN correlationKey", fetch = FetchStrategy.SELECT)
    public Set<SimplePersonCorrelatedSubView> getCorrelatedOwnerViewList();

    @MappingCorrelatedSimple(correlationBasis = "owner", correlationResult = "id", correlated = Document.class, correlationExpression = "owner IN correlationKey AND id NOT IN VIEW_ROOT(id)", fetch = FetchStrategy.SELECT)
    public Set<Long> getOwnerRelatedDocumentIds();

    @MappingCorrelatedSimple(correlationBasis = "owner", correlated = Document.class, correlationExpression = "owner IN correlationKey AND id NOT IN VIEW_ROOT(id)", fetch = FetchStrategy.SELECT)
    public Set<Document> getOwnerRelatedDocuments();

    @MappingCorrelatedSimple(correlationBasis = "owner", correlated = Document.class, correlationExpression = "owner IN correlationKey AND id NOT IN VIEW_ROOT(id)", fetch = FetchStrategy.SELECT)
    public Set<SimpleDocumentCorrelatedView> getOwnerRelatedDocumentViews();

    @MappingCorrelatedSimple(correlationBasis = "owner", correlationResult = "id", correlated = Document.class, correlationExpression = "owner IN correlationKey", fetch = FetchStrategy.SELECT)
    public Set<Long> getOwnerOnlyRelatedDocumentIds();

    @MappingCorrelatedSimple(correlationBasis = "owner", correlated = Document.class, correlationExpression = "owner IN correlationKey", fetch = FetchStrategy.SELECT)
    public Set<Document> getOwnerOnlyRelatedDocuments();

    @MappingCorrelatedSimple(correlationBasis = "owner", correlated = Document.class, correlationExpression = "owner IN correlationKey", fetch = FetchStrategy.SELECT)
    public Set<SimpleDocumentCorrelatedView> getOwnerOnlyRelatedDocumentViews();

    @MappingCorrelatedSimple(correlationBasis = "this", correlated = Document.class, correlationExpression = "this IN correlationKey", correlationResult = "id", fetch = FetchStrategy.SELECT)
    public Long getThisCorrelatedId();

    @MappingCorrelatedSimple(correlationBasis = "this", correlated = Document.class, correlationExpression = "this IN correlationKey", fetch = FetchStrategy.SELECT)
    public Document getThisCorrelatedEntity();

    @MappingCorrelatedSimple(correlationBasis = "this", correlated = Document.class, correlationExpression = "this IN correlationKey", fetch = FetchStrategy.SELECT)
    public SimpleDocumentCorrelatedView getThisCorrelatedView();

    @MappingCorrelatedSimple(correlationBasis = "this", correlated = Document.class, correlationExpression = "this IN correlationKey", correlationResult = "id", fetch = FetchStrategy.SELECT)
    public Set<Long> getThisCorrelatedIdList();

    @MappingCorrelatedSimple(correlationBasis = "this", correlated = Document.class, correlationExpression = "this IN correlationKey", fetch = FetchStrategy.SELECT)
    public Set<Document> getThisCorrelatedEntityList();

    @MappingCorrelatedSimple(correlationBasis = "this", correlated = Document.class, correlationExpression = "this IN correlationKey", fetch = FetchStrategy.SELECT)
    public Set<SimpleDocumentCorrelatedView> getThisCorrelatedViewList();

    @MappingCorrelatedSimple(correlationBasis = "this", correlationResult = "versions.id", correlated = Document.class, correlationExpression = "this IN correlationKey", fetch = FetchStrategy.SELECT)
    public Set<Long> getThisCorrelatedEmptyIdList();

    @MappingCorrelatedSimple(correlationBasis = "this", correlationResult = "versions", correlated = Document.class, correlationExpression = "this IN correlationKey", fetch = FetchStrategy.SELECT)
    public Set<Version> getThisCorrelatedEmptyEntityList();

    @MappingCorrelatedSimple(correlationBasis = "this", correlationResult = "versions", correlated = Document.class, correlationExpression = "this IN correlationKey", fetch = FetchStrategy.SELECT)
    public Set<SimpleVersionCorrelatedView> getThisCorrelatedEmptyViewList();

}
