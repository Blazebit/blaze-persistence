/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.correlation.expression.model;

import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.entity.Version;
import com.blazebit.persistence.view.AttributeFilter;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.MappingCorrelatedSimple;
import com.blazebit.persistence.view.filter.EqualFilter;
import com.blazebit.persistence.view.testsuite.correlation.model.DocumentCorrelationView;
import com.blazebit.persistence.view.testsuite.correlation.model.SimpleDocumentCorrelatedView;
import com.blazebit.persistence.view.testsuite.correlation.model.SimplePersonCorrelatedSubView;
import com.blazebit.persistence.view.testsuite.correlation.model.SimpleVersionCorrelatedView;

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

    @AttributeFilter(EqualFilter.class)
    @MappingCorrelatedSimple(correlationBasis = "COALESCE(owner.id, 1)", correlationResult = "id", correlated = Person.class, correlationExpression = "id IN correlationKey", fetch = FetchStrategy.JOIN)
    public Long getCorrelatedOwnerId();

    @MappingCorrelatedSimple(correlationBasis = "COALESCE(owner.id, 1)", correlated = Person.class, correlationExpression = "id IN correlationKey", fetch = FetchStrategy.JOIN)
    public Person getCorrelatedOwner();

    @MappingCorrelatedSimple(correlationBasis = "COALESCE(owner.id, 1)", correlated = Person.class, correlationExpression = "id IN correlationKey", fetch = FetchStrategy.JOIN)
    public SimplePersonCorrelatedSubView getCorrelatedOwnerView();

    @AttributeFilter(EqualFilter.class)
    @MappingCorrelatedSimple(correlationBasis = "COALESCE(owner.id, 1)", correlationResult = "id", correlated = Person.class, correlationExpression = "id IN correlationKey", fetch = FetchStrategy.JOIN)
    public Set<Long> getCorrelatedOwnerIdList();

    @MappingCorrelatedSimple(correlationBasis = "COALESCE(owner.id, 1)", correlated = Person.class, correlationExpression = "id IN correlationKey", fetch = FetchStrategy.JOIN)
    public Set<Person> getCorrelatedOwnerList();

    @MappingCorrelatedSimple(correlationBasis = "COALESCE(owner.id, 1)", correlated = Person.class, correlationExpression = "id IN correlationKey", fetch = FetchStrategy.JOIN)
    public Set<SimplePersonCorrelatedSubView> getCorrelatedOwnerViewList();

    @MappingCorrelatedSimple(correlationBasis = "COALESCE(owner.id, 1)", correlationResult = "id", correlated = Document.class, correlationExpression = "owner.id IN correlationKey AND id NOT IN VIEW_ROOT(id)", fetch = FetchStrategy.JOIN)
    public Set<Long> getOwnerRelatedDocumentIds();

    @MappingCorrelatedSimple(correlationBasis = "COALESCE(owner.id, 1)", correlated = Document.class, correlationExpression = "owner.id IN correlationKey AND id NOT IN VIEW_ROOT(id)", fetch = FetchStrategy.JOIN)
    public Set<Document> getOwnerRelatedDocuments();

    @MappingCorrelatedSimple(correlationBasis = "COALESCE(owner.id, 1)", correlated = Document.class, correlationExpression = "owner.id IN correlationKey AND id NOT IN VIEW_ROOT(id)", fetch = FetchStrategy.JOIN)
    public Set<SimpleDocumentCorrelatedView> getOwnerRelatedDocumentViews();

    @MappingCorrelatedSimple(correlationBasis = "COALESCE(owner.id, 1)", correlationResult = "id", correlated = Document.class, correlationExpression = "owner.id IN correlationKey", fetch = FetchStrategy.JOIN)
    public Set<Long> getOwnerOnlyRelatedDocumentIds();

    @MappingCorrelatedSimple(correlationBasis = "COALESCE(owner.id, 1)", correlated = Document.class, correlationExpression = "owner.id IN correlationKey", fetch = FetchStrategy.JOIN)
    public Set<Document> getOwnerOnlyRelatedDocuments();

    @MappingCorrelatedSimple(correlationBasis = "COALESCE(owner.id, 1)", correlated = Document.class, correlationExpression = "owner.id IN correlationKey", fetch = FetchStrategy.JOIN)
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

    @MappingCorrelatedSimple(correlationBasis = "this", correlationResult = "versions.id", correlated = Document.class, correlationExpression = "this IN correlationKey", fetch = FetchStrategy.JOIN)
    public Set<Long> getThisCorrelatedEmptyIdList();

    @MappingCorrelatedSimple(correlationBasis = "this", correlationResult = "versions", correlated = Document.class, correlationExpression = "this IN correlationKey", fetch = FetchStrategy.JOIN)
    public Set<Version> getThisCorrelatedEmptyEntityList();

    @MappingCorrelatedSimple(correlationBasis = "this", correlationResult = "versions", correlated = Document.class, correlationExpression = "this IN correlationKey", fetch = FetchStrategy.JOIN)
    public Set<SimpleVersionCorrelatedView> getThisCorrelatedEmptyViewList();

}
