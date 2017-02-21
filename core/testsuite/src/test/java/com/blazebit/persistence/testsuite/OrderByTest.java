/*
 * Copyright 2014 - 2017 Blazebit.
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

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Test;

import com.blazebit.persistence.Criteria;
import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.impl.ConfigurationProperties;
import com.blazebit.persistence.impl.expression.SyntaxErrorException;
import com.blazebit.persistence.spi.CriteriaBuilderConfiguration;
import com.blazebit.persistence.testsuite.entity.Document;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public class OrderByTest extends AbstractCoreTest {

    @Test
    public void testOrderByAscNullsFirst() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.orderBy("d.age", true, true);

        assertEquals("SELECT d FROM Document d ORDER BY " + renderNullPrecedence("d.age", "ASC", "FIRST"), criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testOrderByAscNullsLast() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.orderBy("d.age", true, false);

        assertEquals("SELECT d FROM Document d ORDER BY " + renderNullPrecedence("d.age", "ASC", "LAST"), criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testOrderByDescNullsFirst() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.orderBy("d.age", false, true);

        assertEquals("SELECT d FROM Document d ORDER BY " + renderNullPrecedence("d.age", "DESC", "FIRST"), criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testOrderByDescNullsLast() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.orderBy("d.age", false, false);

        assertEquals("SELECT d FROM Document d ORDER BY " + renderNullPrecedence("d.age", "DESC", "LAST"), criteria.getQueryString());
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
    public void testOrderByFunctionCompatibleMode(){
        CriteriaBuilderConfiguration config = Criteria.getDefault();
        config = configure(config);
        config.setProperty(ConfigurationProperties.COMPATIBLE_MODE, "true");
        cbf = config.createCriteriaBuilderFactory(em.getEntityManagerFactory());
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        try {
            criteria.orderByAsc("SIZE(d.partners)");
            Assert.fail("Expected SyntaxErrorException");
        } catch (SyntaxErrorException ex) {
        }
    }
    
    @Test
    public void testOrderByFunctionExpression(){
        PaginatedCriteriaBuilder<String> criteria = cbf.create(em, String.class)
                .from(Document.class, "d")
                .select("COALESCE(d.owner.name, 'a')", "asd")
                .orderByAsc("asd")
                .orderByAsc("id")
                .page(0, 1);
        String expectedQuery = "SELECT d.id, COALESCE(owner_1.name,'a') AS asd FROM Document d "
                + "JOIN d.owner owner_1 "
                + "GROUP BY " + groupBy("d.id", renderNullPrecedenceGroupBy(groupByPathExpressions("COALESCE(owner_1.name,'a')", "owner_1.name")), renderNullPrecedenceGroupBy("d.id"))
                + " ORDER BY " + renderNullPrecedence("asd", "COALESCE(owner_1.name,'a')", "ASC", "LAST") + ", " + renderNullPrecedence("d.id", "ASC", "LAST");
        assertEquals(expectedQuery, criteria.getPageIdQueryString());
    }

    @Test
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
    public void testOrderByFunctionExperimental(){
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.orderByDesc("FUNCTION('zero',FUNCTION('zero',d.id,FUNCTION('zero',FUNCTION('zero',:colors))),1)");
        assertEquals("SELECT d FROM Document d ORDER BY " + renderNullPrecedence(function("zero", function("zero", "d.id", function("zero", function("zero", ":colors"))), "1"), "DESC", "LAST"), criteria.getQueryString());
    }
    
    @Test
    public void testOrderBySize(){
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.select("d.id").orderByAsc("SIZE(d.partners)");
        
        final String expected = "SELECT d.id FROM Document d LEFT JOIN d.partners partners_1 GROUP BY d.id ORDER BY " + renderNullPrecedence(function("COUNT_TUPLE", "partners_1.id"), "ASC", "LAST");
        assertEquals(expected, criteria.getQueryString());
        criteria.getResultList();
    }
    
    @Test
    public void testOrderBySizeMultiple(){
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.select("d.id").orderByAsc("SIZE(d.partners)").orderByDesc("SIZE(d.versions)");
        
        final String expected = "SELECT d.id FROM Document d LEFT JOIN d.partners partners_1 LEFT JOIN d.versions versions_1 GROUP BY d.id ORDER BY " + renderNullPrecedence(function("COUNT_TUPLE", "'DISTINCT'", "partners_1.id"), "ASC", "LAST") + ", " + renderNullPrecedence(function("COUNT_TUPLE", "'DISTINCT'", "versions_1.id"), "DESC", "LAST");
        assertEquals(expected, criteria.getQueryString());
        criteria.getResultList();
    }
}
