/*
 * Copyright 2014 Blazebit.
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
package com.blazebit.persistence;

import com.blazebit.persistence.entity.Document;
import com.blazebit.persistence.impl.expression.SyntaxErrorException;
import static com.googlecode.catchexception.CatchException.verifyException;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

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

        assertEquals("SELECT d FROM Document d ORDER BY d.age ASC NULLS FIRST", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testOrderByAscNullsLast() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.orderBy("d.age", true, false);

        assertEquals("SELECT d FROM Document d ORDER BY d.age ASC NULLS LAST", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testOrderByDescNullsFirst() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.orderBy("d.age", false, true);

        assertEquals("SELECT d FROM Document d ORDER BY d.age DESC NULLS FIRST", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testOrderByDescNullsLast() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.orderBy("d.age", false, false);

        assertEquals("SELECT d FROM Document d ORDER BY d.age DESC NULLS LAST", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testOrderByNested() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.orderBy("d.versions.document.age", false, false);

        assertEquals(
            "SELECT d FROM Document d LEFT JOIN d.versions versions_1 LEFT JOIN versions_1.document document_1 ORDER BY document_1.age DESC NULLS LAST",
            criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testOrderByMultiple() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.orderBy("d.partners.ownedDocuments.age", false, false).orderBy("d.partners.partnerDocument.age", true, true);

        assertEquals(
            "SELECT d FROM Document d LEFT JOIN d.partners partners_1 LEFT JOIN partners_1.ownedDocuments ownedDocuments_1 LEFT JOIN partners_1.partnerDocument partnerDocument_1 ORDER BY ownedDocuments_1.age DESC NULLS LAST, partnerDocument_1.age ASC NULLS FIRST",
            criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testOrderByNullAlias() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        verifyException(criteria, NullPointerException.class).orderBy(null, false, false);
    }

    @Test
    public void testOrderByEmptyAlias() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        verifyException(criteria, IllegalArgumentException.class).orderBy("", false, false);
    }
    
    @Test
    public void testOrderByFunction(){
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        verifyException(criteria, SyntaxErrorException.class).orderByAsc("SIZE(d.partners)");
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
                + "GROUP BY d.id, COALESCE(owner_1.name,'a'), d.id "
                + "ORDER BY asd ASC NULLS LAST, d.id ASC NULLS LAST";
        assertEquals(expectedQuery, criteria.getPageIdQueryString());
    }
}
