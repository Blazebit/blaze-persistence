/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.AbstractCoreTest;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Document_;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.entity.Person_;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class SubqueryTest extends AbstractCoreTest {

    @Test
    public void correlateSelectJoin() {
        BlazeCriteriaQuery<Long> cq = BlazeCriteria.get(cbf, Long.class);
        BlazeCriteriaBuilder cb = cq.getCriteriaBuilder();
        BlazeRoot<Document> root = cq.from(Document.class, "document");
        BlazeSubquery<Person> subquery = cq.subquery(Person.class);

        BlazeRoot<Document> correlatedRoot = subquery.correlate(root);
        BlazeJoin<Document, Person> correlatedJoin = correlatedRoot.join(Document_.people, "subPerson");

        subquery.select(correlatedJoin);
        cq.where(cb.exists(subquery));
        cq.select(root.get(Document_.id));

        CriteriaBuilder<?> criteriaBuilder = cq.createCriteriaBuilder(em);

        assertEquals("SELECT document.id FROM Document document WHERE EXISTS (SELECT subPerson FROM " + correlationPath(Document.class, "document.people", "subPerson", "id = document.id") + ")", criteriaBuilder.getQueryString());
        assertEquals(1, subquery.getCorrelatedJoins().size());
        assertEquals(root, correlatedRoot.getCorrelationParent());
        assertTrue(correlatedRoot.isCorrelated());
        assertFalse(root.isCorrelated());
    }

    @Test
    public void correlateSelectLiteralWhereGroupByHaving() {
        BlazeCriteriaQuery<Long> cq = BlazeCriteria.get(cbf, Long.class);
        BlazeCriteriaBuilder cb = cq.getCriteriaBuilder();
        BlazeRoot<Document> root = cq.from(Document.class, "document");
        BlazeSubquery<Integer> subquery = cq.subquery(Integer.class);

        BlazeRoot<Document> correlatedRoot = subquery.correlate(root);
        BlazeJoin<Document, Person> correlatedJoin = correlatedRoot.join(Document_.owner, "subOwner");

        subquery.select(cb.literal(1));
        subquery.where(cb.equal(correlatedJoin.get(Person_.id), 0));
        subquery.groupBy(correlatedJoin.get(Person_.age));
        subquery.having(cb.greaterThan(cb.count(correlatedJoin.get(Person_.id)), 2L));
        cq.where(cb.exists(subquery));
        cq.select(root.get(Document_.id));

        CriteriaBuilder<?> criteriaBuilder = cq.createCriteriaBuilder(em);
        assertEquals("SELECT document.id FROM Document document WHERE EXISTS (SELECT 1 FROM document.owner subOwner WHERE subOwner.id = :generated_param_0 GROUP BY subOwner.age HAVING COUNT(subOwner.id) > :generated_param_1)", criteriaBuilder.getQueryString());
        assertEquals(1, subquery.getCorrelatedJoins().size());
        assertEquals(root, correlatedRoot.getCorrelationParent());
        assertTrue(correlatedRoot.isCorrelated());
        assertFalse(root.isCorrelated());
    }

    @Test
    public void correlateSelectRootWithJoinInSubquery() {
        BlazeCriteriaQuery<Long> cq = BlazeCriteria.get(cbf, Long.class);
        BlazeCriteriaBuilder cb = cq.getCriteriaBuilder();
        BlazeRoot<Document> root = cq.from(Document.class, "document");
        BlazeJoin<Document, Person> people = root.join(Document_.people, "person");
        BlazeSubquery<Person> subquery = cq.subquery(Person.class);

        BlazeJoin<Document, Person> correlatedRoot = subquery.correlate(people);
        BlazeJoin<Person, Document> correlatedJoin = correlatedRoot.join(Person_.ownedDocuments, "subDoc");

        subquery.select(correlatedRoot);
        subquery.where(cb.greaterThan(correlatedJoin.get(Document_.age), 1L));
        cq.where(cb.exists(subquery));
        cq.select(root.get(Document_.id));

        CriteriaBuilder<?> criteriaBuilder = cq.createCriteriaBuilder(em);

        assertEquals("SELECT document.id FROM Document document JOIN document.people person WHERE EXISTS (SELECT person FROM " + correlationPath("person.ownedDocuments", Document.class, "subDoc", "owner.id = person.id AND subDoc.age > :generated_param_0", " WHERE subDoc.age > :generated_param_0") + ")", criteriaBuilder.getQueryString());
        assertEquals(1, subquery.getCorrelatedJoins().size());
        assertEquals(people, correlatedRoot.getCorrelationParent());
        assertTrue(correlatedRoot.isCorrelated());
        assertFalse(root.isCorrelated());
    }

    @Test
    public void correlateWithoutSelect() {
        BlazeCriteriaQuery<Long> cq = BlazeCriteria.get(cbf, Long.class);
        BlazeCriteriaBuilder cb = cq.getCriteriaBuilder();
        BlazeRoot<Document> root = cq.from(Document.class, "document");
        BlazeSubquery<Person> subquery = cq.subquery(Person.class);

        BlazeRoot<Document> correlatedRoot = subquery.correlate(root);
        BlazeJoin<Document, Person> correlatedJoin = correlatedRoot.join(Document_.people, "subPerson");

        cq.where(cb.exists(subquery));
        cq.select(root.get(Document_.id));

        CriteriaBuilder<?> criteriaBuilder = cq.createCriteriaBuilder(em);
        assertEquals("SELECT document.id FROM Document document WHERE EXISTS (SELECT subPerson FROM " + correlationPath(Document.class, "document.people", "subPerson", "id = document.id") + ")", criteriaBuilder.getQueryString());
        assertEquals(1, subquery.getCorrelatedJoins().size());
        assertEquals(root, correlatedRoot.getCorrelationParent());
        assertTrue(correlatedRoot.isCorrelated());
        assertFalse(root.isCorrelated());
    }

    @Test
    public void uncorrelatedQuantor() {
        BlazeCriteriaQuery<Long> cq = BlazeCriteria.get(cbf, Long.class);
        BlazeCriteriaBuilder cb = cq.getCriteriaBuilder();
        BlazeRoot<Document> root = cq.from(Document.class, "document");
        BlazeSubquery<Person> subquery = cq.subquery(Person.class);

        BlazeRoot<Person> subFrom = subquery.from(Person.class, "subPerson");

        cq.where(cb.equal(root.get(Document_.owner), cb.all(subquery)));
        cq.select(root.get(Document_.id));

        CriteriaBuilder<?> criteriaBuilder = cq.createCriteriaBuilder(em);
        assertEquals("SELECT document.id FROM Document document WHERE document.owner = ALL(SELECT subPerson FROM Person subPerson)", criteriaBuilder.getQueryString());
        assertEquals(0, subquery.getCorrelatedJoins().size());
        verifyException(subFrom, IllegalStateException.class, r -> r.getCorrelationParent());
        assertFalse(subFrom.isCorrelated());
        assertFalse(root.isCorrelated());
    }

    @Test
    public void implicitCorrelation() {
        BlazeCriteriaQuery<Long> cq = BlazeCriteria.get(cbf, Long.class);
        BlazeCriteriaBuilder cb = cq.getCriteriaBuilder();
        BlazeRoot<Document> root = cq.from(Document.class, "document");
        BlazeSubquery<Person> subquery = cq.subquery(Person.class);

        BlazeRoot<Person> subFrom = subquery.from(Person.class, "subPerson");

        subquery.where(cb.equal(subFrom.get(Person_.age), root.get(Document_.age)));
        cq.where(cb.exists(subquery));
        cq.select(root.get(Document_.id));

        CriteriaBuilder<?> criteriaBuilder = cq.createCriteriaBuilder(em);
        assertEquals("SELECT document.id FROM Document document WHERE EXISTS (SELECT subPerson FROM Person subPerson WHERE subPerson.age = document.age)", criteriaBuilder.getQueryString());
        // TODO: not quite sure what the outcome should be
        assertEquals(0, subquery.getCorrelatedJoins().size());
        verifyException(subFrom, IllegalStateException.class, r -> r.getCorrelationParent());
        assertFalse(subFrom.isCorrelated());
        assertFalse(root.isCorrelated());
    }
    
}
