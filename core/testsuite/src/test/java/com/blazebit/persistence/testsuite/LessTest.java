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
public class LessTest extends AbstractCoreTest {

    @Test
    public void testLt() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.where("d.age").lt(20L);

        assertEquals("SELECT d FROM Document d WHERE d.age < :param_0", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testLtNull() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        verifyException(criteria.where("d.age"), NullPointerException.class).lt(null);
    }

    @Test
    public void testLtExpression() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.where("d.age").ltExpression("d.owner.age");

        assertEquals("SELECT d FROM Document d JOIN d.owner owner_1 WHERE d.age < owner_1.age", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testLtExpressionNull() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        verifyException(criteria.where("d.age"), NullPointerException.class).ltExpression(null);
    }

    @Test
    public void testLe() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.where("d.age").le(20L);

        assertEquals("SELECT d FROM Document d WHERE d.age <= :param_0", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testLeNull() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        verifyException(criteria.where("d.age"), NullPointerException.class).le(null);
    }

    @Test
    public void testLeExpression() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.where("d.age").leExpression("d.owner.age");

        assertEquals("SELECT d FROM Document d JOIN d.owner owner_1 WHERE d.age <= owner_1.age", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testLeExpressionNull() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        verifyException(criteria.where("d.age"), NullPointerException.class).leExpression(null);
    }

    @Test
    public void testLeAll() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.where("id").le().all().from(Person.class, "p").select("id").where("name").eqExpression("d.name").end();
        String expected = "SELECT d FROM Document d WHERE d.id <= ALL(SELECT p.id FROM Person p WHERE p.name = d.name)";

        assertEquals(expected, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testLeAny() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.where("id").le().any().from(Person.class, "p").select("id").where("name").eqExpression("d.name").end();
        String expected = "SELECT d FROM Document d WHERE d.id <= ANY(SELECT p.id FROM Person p WHERE p.name = d.name)";

        assertEquals(expected, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testLeOne() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.where("id").le().from(Person.class, "p").select("id").where("name").eqExpression("d.name").end();
        String expected = "SELECT d FROM Document d WHERE d.id <= (SELECT p.id FROM Person p WHERE p.name = d.name)";

        assertEquals(expected, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testLtAll() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.where("id").lt().all().from(Person.class, "p").select("id").where("name").eqExpression("d.name").end();
        String expected = "SELECT d FROM Document d WHERE d.id < ALL(SELECT p.id FROM Person p WHERE p.name = d.name)";

        assertEquals(expected, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testLtAny() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.where("id").lt().any().from(Person.class, "p").select("id").where("name").eqExpression("d.name").end();
        String expected = "SELECT d FROM Document d WHERE d.id < ANY(SELECT p.id FROM Person p WHERE p.name = d.name)";

        assertEquals(expected, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testLtOne() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.where("id").lt().from(Person.class, "p").select("id").where("name").eqExpression("d.name").end();
        String expected = "SELECT d FROM Document d WHERE d.id < (SELECT p.id FROM Person p WHERE p.name = d.name)";

        assertEquals(expected, crit.getQueryString());
        crit.getResultList();
    }
    
    @Test
    public void testLtSubqueryWithSurroundingExpression() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.where("age").lt("alias", "1 + alias").from(Person.class, "p").select("COUNT(id)").end();     
        
        assertEquals("SELECT d FROM Document d WHERE d.age < 1 + (SELECT COUNT(p.id) FROM Person p)", crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testLtMultipleSubqueryWithSurroundingExpression() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.where("age").lt("alias", "alias * alias").from(Person.class, "p").select("COUNT(id)").end();     
        
        assertEquals("SELECT d FROM Document d WHERE d.age < (SELECT COUNT(p.id) FROM Person p) * (SELECT COUNT(p.id) FROM Person p)", crit.getQueryString());
        crit.getResultList();
    }
    
    @Test
    public void testLeSubqueryWithSurroundingExpression() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.where("age").le("alias", "1 + alias").from(Person.class, "p").select("COUNT(id)").end();     
        
        assertEquals("SELECT d FROM Document d WHERE d.age <= 1 + (SELECT COUNT(p.id) FROM Person p)", crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testLeMultipleSubqueryWithSurroundingExpression() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.where("age").le("alias", "alias * alias").from(Person.class, "p").select("COUNT(id)").end();     
        
        assertEquals("SELECT d FROM Document d WHERE d.age <= (SELECT COUNT(p.id) FROM Person p) * (SELECT COUNT(p.id) FROM Person p)", crit.getQueryString());
        crit.getResultList();
    }
 
}
