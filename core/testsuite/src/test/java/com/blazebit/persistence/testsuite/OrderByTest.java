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

import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.entity.Person;
import com.googlecode.catchexception.CatchException;
import org.junit.Assert;
import org.junit.Test;

import com.blazebit.persistence.Criteria;
import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.impl.ConfigurationProperties;
import com.blazebit.persistence.parser.expression.SyntaxErrorException;
import com.blazebit.persistence.spi.CriteriaBuilderConfiguration;
import com.blazebit.persistence.testsuite.entity.Document;
import org.junit.experimental.categories.Category;

import javax.persistence.Tuple;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class OrderByTest extends AbstractCoreTest {

    @Test
    public void testOrderByAscNullsFirst() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.orderBy("d.someValue", true, true);

        assertEquals("SELECT d FROM Document d ORDER BY " + renderNullPrecedence("d.someValue", "ASC", "FIRST"), criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testOrderByAscNullsFirstOmitted() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.orderBy("d.age", true, true);

        assertEquals("SELECT d FROM Document d ORDER BY d.age ASC", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testOrderByAscNullsLast() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.orderBy("d.someValue", true, false);

        assertEquals("SELECT d FROM Document d ORDER BY " + renderNullPrecedence("d.someValue", "ASC", "LAST"), criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testOrderByAscNullsLastOmitted() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.orderBy("d.age", true, false);

        assertEquals("SELECT d FROM Document d ORDER BY d.age ASC", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testOrderByDescNullsFirst() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.orderBy("d.someValue", false, true);

        assertEquals("SELECT d FROM Document d ORDER BY " + renderNullPrecedence("d.someValue", "DESC", "FIRST"), criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testOrderByDescNullsFirstOmitted() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.orderBy("d.age", false, true);

        assertEquals("SELECT d FROM Document d ORDER BY d.age DESC", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testOrderByDescNullsLast() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.orderBy("d.someValue", false, false);

        assertEquals("SELECT d FROM Document d ORDER BY " + renderNullPrecedence("d.someValue", "DESC", "LAST"), criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testOrderByDescNullsLastOmitted() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.orderBy("d.age", false, false);

        assertEquals("SELECT d FROM Document d ORDER BY d.age DESC", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testOrderByNested() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.orderBy("d.versions.document.age", false, false);

        assertEquals(
            "SELECT d FROM Document d LEFT JOIN d.versions versions_1 LEFT JOIN versions_1.document document_1 ORDER BY " + renderNullPrecedence("document_1.age", "DESC", "LAST"),
            criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testOrderByMultiple() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.orderBy("d.partners.ownedDocuments.age", false, false).orderBy("d.partners.partnerDocument.age", true, true);

        assertEquals(
            "SELECT d FROM Document d LEFT JOIN d.partners partners_1 LEFT JOIN partners_1.ownedDocuments ownedDocuments_1 LEFT JOIN partners_1.partnerDocument partnerDocument_1 ORDER BY " + renderNullPrecedence("ownedDocuments_1.age", "DESC", "LAST") + ", " + renderNullPrecedence("partnerDocument_1.age", "ASC", "FIRST"),
            criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testOrderByNullAlias() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        try {
            criteria.orderBy(null, false, false);
            Assert.fail("Expected NullPointerException");
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void testOrderByEmptyAlias() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        try {
            criteria.orderBy("", false, false);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }
    
    @Test
    public void testOrderByFunctionCompatibleMode() {
        CriteriaBuilderConfiguration config = Criteria.getDefault();
        config = configure(config);
        config.setProperty(ConfigurationProperties.COMPATIBLE_MODE, "true");
        cbf = config.createCriteriaBuilderFactory(em.getEntityManagerFactory());
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        verifyException(criteria, SyntaxErrorException.class).orderByAsc("SIZE(d.partners)");
    }
    
    @Test
    public void testOrderByFunctionExpression() {
        PaginatedCriteriaBuilder<String> criteria = cbf.create(em, String.class)
                .from(Document.class, "d")
                .select("UPPER(d.owner.name)", "asd")
                .orderByAsc("asd")
                .orderByAsc("id")
                .page(0, 1);
        String expectedQuery = "SELECT UPPER(owner_1.name) AS asd FROM Document d"
                + " JOIN d.owner owner_1"
                + " ORDER BY asd ASC, d.id ASC";
        assertEquals(expectedQuery, criteria.getQueryString());
    }

    @Test
    // TODO: Report datanucleus issue
    @Category({ NoDatanucleus.class })
    public void testOrderByAliasedCaseWhen() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");

        criteria.select("CASE WHEN (nameObject.primaryName IS NULL OR LENGTH(TRIM(nameObject.primaryName)) = 0) THEN name ELSE nameObject.primaryName END", "abc");
        criteria.orderByAsc("abc");

        String caseWhen = "CASE WHEN d.nameObject.primaryName IS NULL OR LENGTH(TRIM(BOTH FROM d.nameObject.primaryName)) = 0 THEN d.name ELSE d.nameObject.primaryName END";
        String expected = "SELECT " + caseWhen + " AS abc " +
                "FROM Document d " +
                "ORDER BY " + renderNullPrecedence("abc", caseWhen, "ASC", "LAST");
        assertEquals(expected, criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testOrderByConcatParameter() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.orderByAsc("CONCAT(:prefix, name)");
        assertEquals("SELECT d FROM Document d ORDER BY " + renderNullPrecedence("CONCAT(:prefix,d.name)", "ASC", "LAST"), criteria.getQueryString());
        criteria.setParameter("prefix", "test").getResultList();
    }
    
    @Test
    public void testOrderByFunctionExperimental() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.orderByDesc("FUNCTION('zero',FUNCTION('zero',d.id,FUNCTION('zero',FUNCTION('zero',:colors))),1)");
        assertEquals("SELECT d FROM Document d ORDER BY " + renderNullPrecedence(function("zero", function("zero", "d.id", function("zero", function("zero", ":colors"))), "1"), "DESC", "LAST"), criteria.getQueryString());
    }
    
    @Test
    public void testOrderBySize() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.select("d.id").orderByAsc("SIZE(d.partners)");
        
        final String expected = "SELECT d.id FROM Document d LEFT JOIN d.partners partners_1 GROUP BY d.id ORDER BY " + renderNullPrecedence(function("COUNT_TUPLE", "partners_1.id"), "ASC", "LAST");
        assertEquals(expected, criteria.getQueryString());
        criteria.getResultList();
    }
    
    @Test
    public void testOrderBySizeMultiple() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.select("d.id").orderByAsc("SIZE(d.partners)").orderByDesc("SIZE(d.versions)");
        
        final String expected = "SELECT d.id FROM Document d LEFT JOIN d.partners partners_1 LEFT JOIN d.versions versions_1 GROUP BY d.id ORDER BY " + renderNullPrecedence(function("COUNT_TUPLE", "'DISTINCT'", "partners_1.id"), "ASC", "LAST") + ", " + renderNullPrecedence(function("COUNT_TUPLE", "'DISTINCT'", "versions_1.id"), "DESC", "LAST");
        assertEquals(expected, criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testOrderByAliasedSubqueryWithEmulatedNullPrecedence() {
        // DB2 does not support correlated subqueries in the ORDER BY clause
        // This test is to ensure, we don't copy the correlated subquery into the order by if we can prove the expression is not nullable
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class);
        criteria.from(Document.class, "d");
        criteria.select("d.id");
        criteria.selectSubquery("childCount")
                .from(Person.class, "p")
                .select("COUNT(*)")
                .where("p.partnerDocument").eqExpression("d")
        .end();
        criteria.orderByAsc("childCount", true);

        String subquery = "(SELECT " + countStar() + " FROM Person p WHERE p.partnerDocument = d)";
        final String expected = "SELECT d.id, " + subquery + " AS childCount FROM Document d" +
                " ORDER BY childCount ASC";
        assertEquals(expected, criteria.getQueryString());
        criteria.getResultList();
    }
}
