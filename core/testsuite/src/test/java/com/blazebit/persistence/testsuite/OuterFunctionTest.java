/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.AbstractCoreTest;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class OuterFunctionTest extends AbstractCoreTest {

    @Test
    public void testOuter1() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.whereSubquery().from(Person.class, "p").select("id").where("OUTER(owner.name)").eqExpression("OUTER(name)").end()
            .eqExpression("partners.id");
        String expected = "SELECT d FROM Document d JOIN d.owner owner_1 LEFT JOIN d.partners partners_1 WHERE (SELECT p.id FROM Person p WHERE owner_1.name = d.name) = partners_1.id";
        String actual = crit.getQueryString();

        assertEquals(expected, actual);
        assertEquals(actual, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testOuter2() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.whereSubquery().from(Person.class, "p").select("id").where("OUTER(id)").eqExpression("id").end().eqExpression(
            "partners.id");
        String expected = "SELECT d FROM Document d LEFT JOIN d.partners partners_1 WHERE (SELECT p.id FROM Person p WHERE d.id = p.id) = partners_1.id";
        String actual = crit.getQueryString();

        assertEquals(expected, actual);
        assertEquals(actual, crit.getQueryString());
        crit.getResultList();
    }
    
    @Test
    public void testComplexOuterExpression() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.whereSubquery().from(Person.class, "p").select("id").where("OUTER(id) + LENGTH(OUTER(name))").eqExpression("id").end().eqExpression(
            "partners.id");
        String expected = "SELECT d FROM Document d LEFT JOIN d.partners partners_1 WHERE (SELECT p.id FROM Person p WHERE d.id + LENGTH(d.name) = p.id) = partners_1.id";
        String actual = crit.getQueryString();

        assertEquals(expected, actual);
        assertEquals(actual, crit.getQueryString());
        crit.getResultList();
    }
}
