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

package com.blazebit.persistence.testsuite;

import static com.googlecode.catchexception.CatchException.verifyException;
import static org.junit.Assert.assertEquals;

import javax.persistence.Tuple;

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

        verifyException(criteria.selectCase().whenAnd(), IllegalStateException.class).thenExpression("d.name");
    }

    @Test
    public void testGeneralCaseWhenNoOrClauses() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");

        verifyException(criteria.selectCase().whenOr(), IllegalStateException.class).thenExpression("d.name");
    }

    @Test
    public void testGeneralCaseWhenOrThenBuilderNotEnded() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        CaseWhenStarterBuilder<?> caseWhenBuilder = criteria.selectCase();
        caseWhenBuilder.whenOr().or("x").ltExpression("y").thenExpression("2").whenOr();

        verifyException(caseWhenBuilder, BuilderChainingException.class).whenAnd();
        verifyException(caseWhenBuilder, BuilderChainingException.class).whenOr();
        verifyException(caseWhenBuilder, BuilderChainingException.class).when("test");
        verifyException(caseWhenBuilder, BuilderChainingException.class).whenExists();
        verifyException(caseWhenBuilder, BuilderChainingException.class).whenNotExists();
        verifyException(caseWhenBuilder, BuilderChainingException.class).whenSubquery();
        verifyException(caseWhenBuilder, BuilderChainingException.class).whenSubquery("test", "test");
        verifyException(criteria, BuilderChainingException.class).getQueryString();
    }

    @Test
    public void testGeneralCaseWhenAndThenBuilderNotEnded() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        CaseWhenStarterBuilder<?> caseWhenBuilder = criteria.selectCase();
        caseWhenBuilder.whenAnd().and("x").ltExpression("y").thenExpression("2").whenAnd();

        verifyException(caseWhenBuilder, BuilderChainingException.class).whenAnd();
        verifyException(caseWhenBuilder, BuilderChainingException.class).whenOr();
        verifyException(caseWhenBuilder, BuilderChainingException.class).when("test");
        verifyException(caseWhenBuilder, BuilderChainingException.class).whenExists();
        verifyException(caseWhenBuilder, BuilderChainingException.class).whenNotExists();
        verifyException(caseWhenBuilder, BuilderChainingException.class).whenSubquery();
        verifyException(caseWhenBuilder, BuilderChainingException.class).whenSubquery("test", "test");
        verifyException(criteria, BuilderChainingException.class).getQueryString();
    }

    @Test
    public void testGeneralCaseWhenAndBuilderNotEnded() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        CaseWhenOrThenBuilder<?> caseWhenOrThenBuilder = criteria.selectCase().whenOr();
        caseWhenOrThenBuilder.and();

        verifyException(caseWhenOrThenBuilder, BuilderChainingException.class).and();
        verifyException(caseWhenOrThenBuilder, BuilderChainingException.class).or("test");
        verifyException(caseWhenOrThenBuilder, BuilderChainingException.class).orExists();
        verifyException(caseWhenOrThenBuilder, BuilderChainingException.class).orNotExists();
        verifyException(caseWhenOrThenBuilder, BuilderChainingException.class).orSubquery();
        verifyException(caseWhenOrThenBuilder, BuilderChainingException.class).orSubquery("test", "test");
        verifyException(caseWhenOrThenBuilder, BuilderChainingException.class).thenExpression("2");
        verifyException(criteria, BuilderChainingException.class).getQueryString();
    }

    @Test
    public void testGeneralCaseWhenOrBuilderNotEnded() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        CaseWhenAndThenBuilder<?> caseWhenAndThenBuilder = criteria.selectCase().whenAnd();
        caseWhenAndThenBuilder.or();

        verifyException(caseWhenAndThenBuilder, BuilderChainingException.class).or();
        verifyException(caseWhenAndThenBuilder, BuilderChainingException.class).and("test");
        verifyException(caseWhenAndThenBuilder, BuilderChainingException.class).andExists();
        verifyException(caseWhenAndThenBuilder, BuilderChainingException.class).andNotExists();
        verifyException(caseWhenAndThenBuilder, BuilderChainingException.class).andSubquery();
        verifyException(caseWhenAndThenBuilder, BuilderChainingException.class).andSubquery("test", "test");
        verifyException(caseWhenAndThenBuilder, BuilderChainingException.class).thenExpression("2");
        verifyException(criteria, BuilderChainingException.class).getQueryString();
    }

    @Test
    public void testCaseWhenSubqueryBuilderNotEnded() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.selectCase().whenExists().from(Person.class, "p");

        verifyException(criteria, BuilderChainingException.class).getQueryString();
    }

    @Test
    public void testSimpleCaseWhenBuilderNotEnded() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.selectSimpleCase("d.name");

        verifyException(criteria, BuilderChainingException.class).getQueryString();
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

        String expected = "SELECT CASE WHEN " + function("COUNT_TUPLE", "'DISTINCT'", "KEY(contacts_1)") + " > 2 THEN 2 ELSE 0 END " +
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

        String expected = "SELECT CASE WHEN " + function("COUNT_TUPLE", "'DISTINCT'", "KEY(contacts_1)") + " > 2 THEN 1 ELSE 0 END " +
                "FROM Document d " +
                "LEFT JOIN d.contacts contacts_1 " +
                "LEFT JOIN d.partners partners_1 " +
                "WHERE partners_1.name LIKE '%onny' " +
                "GROUP BY d.id";
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

        String expected = "SELECT CASE WHEN " + function("COUNT_TUPLE", "'DISTINCT'", "KEY(contacts_1)") + " > 2 THEN d.name ELSE '' END " +
                "FROM Document d LEFT JOIN d.contacts contacts_1 " +
                "LEFT JOIN d.partners partners_1 " +
                "WHERE partners_1.name LIKE '%onny' " +
                "GROUP BY d.id, d.name";
        assertEquals(expected, criteria.getQueryString());
        criteria.getResultList();
    }
}
