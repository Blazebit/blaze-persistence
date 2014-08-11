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
import com.blazebit.persistence.entity.Person;
import static org.junit.Assert.assertEquals;
import static com.googlecode.catchexception.CatchException.*;
import org.junit.Test;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public class LessTest extends AbstractCoreTest {

    @Test
    public void testLt() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.where("d.age").lt(20L);

        assertEquals("SELECT d FROM Document d WHERE d.age < :param_0", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testLtNull() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        verifyException(criteria.where("d.age"), NullPointerException.class).lt(null);
    }

    @Test
    public void testLtExpression() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.where("d.age").ltExpression("d.owner.name");

        assertEquals("SELECT d FROM Document d JOIN d.owner owner WHERE d.age < owner.name", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testLtExpressionNull() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        verifyException(criteria.where("d.age"), NullPointerException.class).ltExpression(null);
    }

    @Test
    public void testLe() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.where("d.age").le(20L);

        assertEquals("SELECT d FROM Document d WHERE d.age <= :param_0", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testLeNull() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        verifyException(criteria.where("d.age"), NullPointerException.class).le(null);
    }

    @Test
    public void testLeExpression() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.where("d.age").leExpression("d.owner.name");

        assertEquals("SELECT d FROM Document d JOIN d.owner owner WHERE d.age <= owner.name", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testLeExpressionNull() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        verifyException(criteria.where("d.age"), NullPointerException.class).leExpression(null);
    }

    @Test
    public void testLeAll() {
        CriteriaBuilder<Document> crit = cbf.from(em, Document.class, "d");
        crit.where("name").le().all().from(Person.class, "p").select("id").where("name").eqExpression("d.name").end();
        String expected = "SELECT d FROM Document d WHERE d.name <= ALL(SELECT p.id FROM Person p WHERE p.name = d.name)";

        assertEquals(expected, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testLeAny() {
        CriteriaBuilder<Document> crit = cbf.from(em, Document.class, "d");
        crit.where("name").le().any().from(Person.class, "p").select("id").where("name").eqExpression("d.name").end();
        String expected = "SELECT d FROM Document d WHERE d.name <= ANY(SELECT p.id FROM Person p WHERE p.name = d.name)";

        assertEquals(expected, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testLeOne() {
        CriteriaBuilder<Document> crit = cbf.from(em, Document.class, "d");
        crit.where("name").le().from(Person.class, "p").select("id").where("name").eqExpression("d.name").end();
        String expected = "SELECT d FROM Document d WHERE d.name <= (SELECT p.id FROM Person p WHERE p.name = d.name)";

        assertEquals(expected, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testLtAll() {
        CriteriaBuilder<Document> crit = cbf.from(em, Document.class, "d");
        crit.where("name").lt().all().from(Person.class, "p").select("id").where("name").eqExpression("d.name").end();
        String expected = "SELECT d FROM Document d WHERE d.name < ALL(SELECT p.id FROM Person p WHERE p.name = d.name)";

        assertEquals(expected, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testLtAny() {
        CriteriaBuilder<Document> crit = cbf.from(em, Document.class, "d");
        crit.where("name").lt().any().from(Person.class, "p").select("id").where("name").eqExpression("d.name").end();
        String expected = "SELECT d FROM Document d WHERE d.name < ANY(SELECT p.id FROM Person p WHERE p.name = d.name)";

        assertEquals(expected, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testLtOne() {
        CriteriaBuilder<Document> crit = cbf.from(em, Document.class, "d");
        crit.where("name").lt().from(Person.class, "p").select("id").where("name").eqExpression("d.name").end();
        String expected = "SELECT d FROM Document d WHERE d.name < (SELECT p.id FROM Person p WHERE p.name = d.name)";

        assertEquals(expected, crit.getQueryString());
        crit.getResultList();
    }
}
