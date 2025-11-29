/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite;

import static org.junit.Assert.assertEquals;

import jakarta.persistence.Tuple;

import org.junit.Test;

import com.blazebit.persistence.CaseWhenAndThenBuilder;
import com.blazebit.persistence.CaseWhenOrThenBuilder;
import com.blazebit.persistence.CaseWhenStarterBuilder;
import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.impl.BuilderChainingException;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class CaseWhenTest extends AbstractCoreTest {

    @Test
    public void testSelectGeneralCaseWhenWithAlias() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");

        criteria.selectCase("myAlias")
                .when("d.name").eqExpression("'v'").thenExpression("2")
                .whenAnd()
                .and("d.name").eqExpression("'v'")
                .and("d.name").eqExpression("'i'")
                .thenExpression("1")
                .whenAnd()
                .and("d.name").eqExpression("'v'")
                .and("d.name").eqExpression("'i'")
                .thenExpression("1")
                .whenOr()
                .or("d.name").eqExpression("'v'")
                .or("d.name").eqExpression("'i'")
                .thenExpression("1")
                .whenOr()
                .and()
                .and("d.name").eqExpression("'v'")
                .and("d.name").eqExpression("'i'")
                .endAnd()
                .and()
                .and("d.name").eqExpression("'v'")
                .and("d.name").eqExpression("'i'")
                .endAnd()
                .thenExpression("2")
                .otherwiseExpression("0");

        String expected = "SELECT CASE "
                + "WHEN d.name = 'v' THEN 2 "
                + "WHEN d.name = 'v' AND d.name = 'i' THEN 1 "
                + "WHEN d.name = 'v' AND d.name = 'i' THEN 1 "
                + "WHEN d.name = 'v' OR d.name = 'i' THEN 1 "
                + "WHEN (d.name = 'v' AND d.name = 'i') OR (d.name = 'v' AND d.name = 'i') THEN 2 "
                + "ELSE 0 END AS myAlias FROM Document d";
        assertEquals(expected, criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testSelectGeneralCaseWhenWithoutAlias() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");

        criteria.selectCase()
                .when("d.name").eqExpression("'v'").thenExpression("2")
                .otherwiseExpression("0");

        String expected = "SELECT CASE "
                + "WHEN d.name = 'v' THEN 2 "
                + "ELSE 0 END FROM Document d";
        assertEquals(expected, criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testGeneralCaseWhenNoAndClauses() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");

        verifyException(criteria.selectCase().whenAnd(), IllegalStateException.class, r -> r.thenExpression("d.name"));
    }

    @Test
    public void testGeneralCaseWhenNoOrClauses() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");

        verifyException(criteria.selectCase().whenOr(), IllegalStateException.class, r -> r.thenExpression("d.name"));
    }

    @Test
    public void testGeneralCaseWhenOrThenBuilderNotEnded() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        CaseWhenStarterBuilder<?> caseWhenBuilder = criteria.selectCase();
        caseWhenBuilder.whenOr().or("x").ltExpression("y").thenExpression("2").whenOr();

        verifyException(caseWhenBuilder, BuilderChainingException.class, r -> r.whenAnd());
        verifyException(caseWhenBuilder, BuilderChainingException.class, r -> r.whenOr());
        verifyException(caseWhenBuilder, BuilderChainingException.class, r -> r.when("test"));
        verifyException(caseWhenBuilder, BuilderChainingException.class, r -> r.whenExists());
        verifyException(caseWhenBuilder, BuilderChainingException.class, r -> r.whenNotExists());
        verifyException(caseWhenBuilder, BuilderChainingException.class, r -> r.whenSubquery());
        verifyException(caseWhenBuilder, BuilderChainingException.class, r -> r.whenSubquery("test", "test"));
        verifyException(criteria, BuilderChainingException.class, r -> r.getQueryString());
    }

    @Test
    public void testGeneralCaseWhenAndThenBuilderNotEnded() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        CaseWhenStarterBuilder<?> caseWhenBuilder = criteria.selectCase();
        caseWhenBuilder.whenAnd().and("x").ltExpression("y").thenExpression("2").whenAnd();

        verifyException(caseWhenBuilder, BuilderChainingException.class, r -> r.whenAnd());
        verifyException(caseWhenBuilder, BuilderChainingException.class, r -> r.whenOr());
        verifyException(caseWhenBuilder, BuilderChainingException.class, r -> r.when("test"));
        verifyException(caseWhenBuilder, BuilderChainingException.class, r -> r.whenExists());
        verifyException(caseWhenBuilder, BuilderChainingException.class, r -> r.whenNotExists());
        verifyException(caseWhenBuilder, BuilderChainingException.class, r -> r.whenSubquery());
        verifyException(caseWhenBuilder, BuilderChainingException.class, r -> r.whenSubquery("test", "test"));
        verifyException(criteria, BuilderChainingException.class, r -> r.getQueryString());
    }

    @Test
    public void testGeneralCaseWhenAndBuilderNotEnded() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        CaseWhenOrThenBuilder<?> caseWhenOrThenBuilder = criteria.selectCase().whenOr();
        caseWhenOrThenBuilder.and();

        verifyException(caseWhenOrThenBuilder, BuilderChainingException.class, r -> r.and());
        verifyException(caseWhenOrThenBuilder, BuilderChainingException.class, r -> r.or("test"));
        verifyException(caseWhenOrThenBuilder, BuilderChainingException.class, r -> r.orExists());
        verifyException(caseWhenOrThenBuilder, BuilderChainingException.class, r -> r.orNotExists());
        verifyException(caseWhenOrThenBuilder, BuilderChainingException.class, r -> r.orSubquery());
        verifyException(caseWhenOrThenBuilder, BuilderChainingException.class, r -> r.orSubquery("test", "test"));
        verifyException(caseWhenOrThenBuilder, BuilderChainingException.class, r -> r.thenExpression("2"));
        verifyException(criteria, BuilderChainingException.class, r -> r.getQueryString());
    }

    @Test
    public void testGeneralCaseWhenOrBuilderNotEnded() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        CaseWhenAndThenBuilder<?> caseWhenAndThenBuilder = criteria.selectCase().whenAnd();
        caseWhenAndThenBuilder.or();

        verifyException(caseWhenAndThenBuilder, BuilderChainingException.class, r -> r.or());
        verifyException(caseWhenAndThenBuilder, BuilderChainingException.class, r -> r.and("test"));
        verifyException(caseWhenAndThenBuilder, BuilderChainingException.class, r -> r.andExists());
        verifyException(caseWhenAndThenBuilder, BuilderChainingException.class, r -> r.andNotExists());
        verifyException(caseWhenAndThenBuilder, BuilderChainingException.class, r -> r.andSubquery());
        verifyException(caseWhenAndThenBuilder, BuilderChainingException.class, r -> r.andSubquery("test", "test"));
        verifyException(caseWhenAndThenBuilder, BuilderChainingException.class, r -> r.thenExpression("2"));
        verifyException(criteria, BuilderChainingException.class, r -> r.getQueryString());
    }

    @Test
    public void testCaseWhenSubqueryBuilderNotEnded() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.selectCase().whenExists().from(Person.class, "p");

        verifyException(criteria, BuilderChainingException.class, r -> r.getQueryString());
    }

    @Test
    public void testSimpleCaseWhenBuilderNotEnded() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.selectSimpleCase("d.name");

        verifyException(criteria, BuilderChainingException.class, r -> r.getQueryString());
    }

    @Test
    public void testSimpleCaseWhen() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.selectSimpleCase("d.name")
                .when("'v'", "2")
                .when("'i'", "1")
                .otherwise("0");

        String expected = "SELECT CASE d.name "
                + "WHEN 'v' THEN 2 "
                + "WHEN 'i' THEN 1 "
                + "ELSE 0 END FROM Document d";

        assertEquals(expected, criteria.getQueryString());
    }

    @Test
    public void testCaseWhenArithmeticExpression() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.selectSimpleCase("d.name")
                .when("'v'", "d.age + 2")
                .when("'i'", "d.age + 1")
                .otherwise("d.age + 0");

        String expected = "SELECT CASE d.name "
                + "WHEN 'v' THEN (d.age + 2) "
                + "WHEN 'i' THEN (d.age + 1) "
                + "ELSE (d.age + 0) END " +
                "FROM Document d";

        assertEquals(expected, criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testSelectCaseWhenSizeAsSubexpression() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class).from(Document.class, "d")
                .selectCase()
                    .when("SIZE(d.contacts)").gtExpression("2").thenExpression("2")
                    .otherwiseExpression("0")
                .where("d.partners.name").like().expression("'%onny'").noEscape();

        String expected = "SELECT CASE WHEN " + countDistinct("KEY(contacts_1)") + " > 2 THEN 2 ELSE 0 END " +
                "FROM Document d LEFT JOIN d.contacts contacts_1 " +
                "LEFT JOIN d.partners partners_1 " +
                "WHERE partners_1.name LIKE '%onny' GROUP BY d.id";
        assertEquals(expected, criteria.getQueryString());
        criteria.getResultList();
    }
    
    @Test
    public void testThenParameterValue(){
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Document.class, "d")
                .selectCase()
                    .when("SIZE(d.contacts)").gtExpression("2")
                    .then(1)
                    .otherwise(0)
                .where("d.partners.name").like().expression("'%onny'").noEscape();

        String expected = "SELECT CASE WHEN " + countDistinct("KEY(contacts_1)") + " > 2 THEN 1 ELSE 0 END " +
                "FROM Document d " +
                "LEFT JOIN d.contacts contacts_1 " +
                "LEFT JOIN d.partners partners_1 " +
                "WHERE partners_1.name LIKE '%onny' " +
                "GROUP BY d.id";
        assertEquals(expected, criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testImplicitGroupByCase() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Document.class, "d")
                .selectCase()
                .when("version").eqExpression("1").thenExpression("'first'")
                .otherwiseExpression("'later'")
                .select("COUNT(id)");

        String expected = "SELECT CASE WHEN d.version = 1 THEN 'first' ELSE 'later' END, COUNT(d.id) FROM Document d GROUP BY CASE WHEN d.version = 1 THEN 'first' ELSE 'later' END";
        assertEquals(expected, criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testImplicitGroupByConstantifiedCase1() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Document.class, "d")
                .selectCase()
                .when("version").eqExpression("1").then("first")
                .otherwiseExpression("'later'")
                .select("COUNT(id)");

        String expected = "SELECT CASE WHEN d.version = 1 THEN 'first' ELSE 'later' END, COUNT(d.id) FROM Document d GROUP BY CASE WHEN d.version = 1 THEN 'first' ELSE 'later' END";
        assertEquals(expected, criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testImplicitGroupByConstantifiedCase2() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Document.class, "d")
                .selectCase()
                .when("version").eqExpression("1").thenExpression("'first'")
                .otherwise("later")
                .select("COUNT(id)");

        String expected = "SELECT CASE WHEN d.version = 1 THEN 'first' ELSE 'later' END, COUNT(d.id) FROM Document d GROUP BY CASE WHEN d.version = 1 THEN 'first' ELSE 'later' END";
        assertEquals(expected, criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testCaseWhenSizeThenAttribute(){
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Document.class, "d")
                .selectCase()
                .when("SIZE(d.contacts)").gtExpression("2")
                .thenExpression("d.name")
                .otherwiseExpression("''")
                .where("d.partners.name").like().expression("'%onny'").noEscape();

        String expected = "SELECT CASE WHEN " + countDistinct("KEY(contacts_1)") + " > 2 THEN d.name ELSE '' END " +
                "FROM Document d LEFT JOIN d.contacts contacts_1 " +
                "LEFT JOIN d.partners partners_1 " +
                "WHERE partners_1.name LIKE '%onny' " +
                "GROUP BY d.id, d.name";
        assertEquals(expected, criteria.getQueryString());
        criteria.getResultList();
    }
}
