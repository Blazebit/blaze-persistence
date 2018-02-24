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
public class GreaterTest extends AbstractCoreTest {

    @Test
    public void testGt() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.where("d.age").gt(20L);

        assertEquals("SELECT d FROM Document d WHERE d.age > :param_0", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testGtNull() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        verifyException(criteria.where("d.age"), NullPointerException.class).gt(null);
    }

    @Test
    public void testGtExpression() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.where("d.age").gtExpression("d.owner.age");

        assertEquals("SELECT d FROM Document d JOIN d.owner owner_1 WHERE d.age > owner_1.age", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testGtExpressionNull() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        verifyException(criteria.where("d.age"), NullPointerException.class).gtExpression(null);
    }

    @Test
    public void testGe() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.where("d.age").ge(20L);

        assertEquals("SELECT d FROM Document d WHERE d.age >= :param_0", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testGeNull() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        verifyException(criteria.where("d.age"), NullPointerException.class).ge(null);
    }

    @Test
    public void testGeExpression() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.where("d.age").geExpression("d.owner.age");

        assertEquals("SELECT d FROM Document d JOIN d.owner owner_1 WHERE d.age >= owner_1.age", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testGeExpressionNull() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        verifyException(criteria.where("d.age"), NullPointerException.class).geExpression(null);
    }

    @Test
    public void testGeAll() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.where("id").ge().all().from(Person.class, "p").select("id").where("name").eqExpression("d.name").end();
        String expected = "SELECT d FROM Document d WHERE d.id >= ALL(SELECT p.id FROM Person p WHERE p.name = d.name)";

        assertEquals(expected, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testGeAny() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.where("id").ge().any().from(Person.class, "p").select("id").where("name").eqExpression("d.name").end();
        String expected = "SELECT d FROM Document d WHERE d.id >= ANY(SELECT p.id FROM Person p WHERE p.name = d.name)";

        assertEquals(expected, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testGeOne() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.where("id").ge().from(Person.class, "p").select("id").where("name").eqExpression("d.name").end();
        String expected = "SELECT d FROM Document d WHERE d.id >= (SELECT p.id FROM Person p WHERE p.name = d.name)";

        assertEquals(expected, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testGtAll() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.where("id").gt().all().from(Person.class, "p").select("id").where("name").eqExpression("d.name").end();
        String expected = "SELECT d FROM Document d WHERE d.id > ALL(SELECT p.id FROM Person p WHERE p.name = d.name)";

        assertEquals(expected, crit.getQueryString());
    }

    @Test
    public void testGtAny() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.where("id").gt().any().from(Person.class, "p").select("id").where("name").eqExpression("d.name").end();
        String expected = "SELECT d FROM Document d WHERE d.id > ANY(SELECT p.id FROM Person p WHERE p.name = d.name)";

        assertEquals(expected, crit.getQueryString());
    }

    @Test
    public void testGtOne() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.where("id").gt().from(Person.class, "p").select("id").where("name").eqExpression("d.name").end();
        String expected = "SELECT d FROM Document d WHERE d.id > (SELECT p.id FROM Person p WHERE p.name = d.name)";

        assertEquals(expected, crit.getQueryString());
    }
    
    @Test
    public void testGtSubqueryWithSurroundingExpression() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.where("age").gt("alias", "1 + alias").from(Person.class, "p").select("COUNT(id)").end();     
        
        assertEquals("SELECT d FROM Document d WHERE d.age > 1 + (SELECT COUNT(p.id) FROM Person p)", crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testGtMultipleSubqueryWithSurroundingExpression() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.where("age").gt("alias", "alias * alias").from(Person.class, "p").select("COUNT(id)").end();     
        
        assertEquals("SELECT d FROM Document d WHERE d.age > (SELECT COUNT(p.id) FROM Person p) * (SELECT COUNT(p.id) FROM Person p)", crit.getQueryString());
        crit.getResultList();
    }
    
    @Test
    public void testGeSubqueryWithSurroundingExpression() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.where("age").ge("alias", "1 + alias").from(Person.class, "p").select("COUNT(id)").end();     
        
        assertEquals("SELECT d FROM Document d WHERE d.age >= 1 + (SELECT COUNT(p.id) FROM Person p)", crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testGeMultipleSubqueryWithSurroundingExpression() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.where("age").ge("alias", "alias * alias").from(Person.class, "p").select("COUNT(id)").end();     
        
        assertEquals("SELECT d FROM Document d WHERE d.age >= (SELECT COUNT(p.id) FROM Person p) * (SELECT COUNT(p.id) FROM Person p)", crit.getQueryString());
        crit.getResultList();
    }
}
