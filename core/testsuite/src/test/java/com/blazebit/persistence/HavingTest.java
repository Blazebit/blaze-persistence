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
import static com.googlecode.catchexception.CatchException.verifyException;
import java.util.Calendar;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public class HavingTest extends AbstractCoreTest {

    @Test
    public void testHaving() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.groupBy("d.owner")
            .having("d.age").gt(0L);
        assertEquals("SELECT d FROM Document d JOIN d.owner owner_1 GROUP BY owner_1 HAVING d.age > :param_0", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testHavingPropertyExpression() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.groupBy("d.owner")
            .having("d.age + 1").gt(0L);

        assertEquals("SELECT d FROM Document d JOIN d.owner owner_1 GROUP BY owner_1 HAVING d.age + 1 > :param_0", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testHavingPath() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.groupBy("d.owner")
            .having("LENGTH(d.partners.name)").gt(0);

        assertEquals(
            "SELECT d FROM Document d JOIN d.owner owner_1 LEFT JOIN d.partners partners_1 GROUP BY owner_1 HAVING LENGTH(partners_1.name) > :param_0",
            criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testHavingPathExpression() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.groupBy("d.owner")
            .having("LENGTH(d.partners.name) + 1").gt(0);

        assertEquals(
            "SELECT d FROM Document d JOIN d.owner owner_1 LEFT JOIN d.partners partners_1 GROUP BY owner_1 HAVING LENGTH(partners_1.name) + 1 > :param_0",
            criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testHavingAnd() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.groupBy("d.owner")
            .having("LENGTH(d.partners.name)").gt(0)
            .having("d.versions.url").like("http://%");

        assertEquals(
            "SELECT d FROM Document d JOIN d.owner owner_1 LEFT JOIN d.partners partners_1 LEFT JOIN d.versions versions_1 GROUP BY owner_1 HAVING LENGTH(partners_1.name) > :param_0 AND versions_1.url LIKE :param_1",
            criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testHavingOr() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.groupBy("d.owner")
            .havingOr()
                .having("LENGTH(d.partners.name)").gt(0)
                .having("d.versions.url").like("http://%")
            .endOr();

        assertEquals(
            "SELECT d FROM Document d JOIN d.owner owner_1 LEFT JOIN d.partners partners_1 LEFT JOIN d.versions versions_1 GROUP BY owner_1 HAVING LENGTH(partners_1.name) > :param_0 OR versions_1.url LIKE :param_1",
            criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testHavingOrAnd() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.groupBy("d.owner")
            .havingOr()
                .havingAnd()
                    .having("LENGTH(d.partners.name)").gt(0)
                    .having("d.versions.url").like("http://%")
                .endAnd()
                .havingAnd()
                    .having("d.versions.date").lt(Calendar.getInstance())
                    .having("d.versions.url").like("ftp://%")
                .endAnd()
            .endOr();

        assertEquals(
            "SELECT d FROM Document d JOIN d.owner owner_1 LEFT JOIN d.partners partners_1 LEFT JOIN d.versions versions_1 GROUP BY owner_1 HAVING (LENGTH(partners_1.name) > :param_0 AND versions_1.url LIKE :param_1) OR (versions_1.date < :param_2 AND versions_1.url LIKE :param_3)",
            criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testHavingAndOr() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.groupBy("d.owner")
            .havingOr()
                .having("LENGTH(d.partners.name)").gt(0)
                .having("d.versions.url").like("http://%")
            .endOr()
            .havingOr()
                .having("d.versions.date").lt(Calendar.getInstance())
                .having("d.versions.url").like("ftp://%")
            .endOr();

        assertEquals(
            "SELECT d FROM Document d JOIN d.owner owner_1 LEFT JOIN d.partners partners_1 LEFT JOIN d.versions versions_1 GROUP BY owner_1 HAVING (LENGTH(partners_1.name) > :param_0 OR versions_1.url LIKE :param_1) AND (versions_1.date < :param_2 OR versions_1.url LIKE :param_3)",
            criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testHavingOrSingleClause() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.groupBy("d.owner")
            .havingOr()
                .having("LENGTH(d.partners.name)").gt(0)
            .endOr();

        assertEquals(
            "SELECT d FROM Document d JOIN d.owner owner_1 LEFT JOIN d.partners partners_1 GROUP BY owner_1 HAVING LENGTH(partners_1.name) > :param_0",
            criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testHavingOrHavingAndSingleClause() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.groupBy("d.owner")
            .havingOr()
                .havingAnd()
                    .having("LENGTH(d.partners.name)").gt(0)
                .endAnd()
            .endOr();

        assertEquals(
            "SELECT d FROM Document d JOIN d.owner owner_1 LEFT JOIN d.partners partners_1 GROUP BY owner_1 HAVING LENGTH(partners_1.name) > :param_0",
            criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testHavingWithoutGroupBy() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        verifyException(criteria, IllegalStateException.class).having("d.partners.name");
    }

    @Test
    public void testHavingNull() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        verifyException(criteria.groupBy("d.owner"), NullPointerException.class).having(null);
    }

    @Test
    public void testHavingExists() {
        CriteriaBuilder<Document> crit = cbf.from(em, Document.class, "d");
        crit.groupBy("name")
            .havingExists()
                .from(Person.class, "p")
                .select("id")
                .where("name").eqExpression("d.name")
            .end();
        String expected = "SELECT d FROM Document d GROUP BY d.name HAVING EXISTS (SELECT p.id FROM Person p WHERE p.name = d.name)";

        assertEquals(expected, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testHavingNotExists() {
        CriteriaBuilder<Document> crit = cbf.from(em, Document.class, "d");
        crit.groupBy("name")
            .havingNotExists()
                .from(Person.class, "p")
                .select("id")
                .where("name").eqExpression("d.name")
            .end();
        String expected = "SELECT d FROM Document d GROUP BY d.name HAVING NOT EXISTS (SELECT p.id FROM Person p WHERE p.name = d.name)";

        assertEquals(expected, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testHavingNotExists2() {
        CriteriaBuilder<Document> crit = cbf.from(em, Document.class, "d");
        crit.groupBy("name")
            .having("d.name").eq("test")
            .havingNotExists()
                .from(Person.class, "p")
                .select("id")
                .where("name").eqExpression("d.name")
            .end();
        String expected = "SELECT d FROM Document d GROUP BY d.name HAVING d.name = :param_0 AND NOT EXISTS (SELECT p.id FROM Person p WHERE p.name = d.name)";

        assertEquals(expected, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testHavingExistsAndBuilder() {
        CriteriaBuilder<Document> crit = cbf.from(em, Document.class, "d");
        crit.groupBy("name")
            .having("d.name").eq("test")
            .havingOr()
                .havingAnd()
                    .havingExists()
                        .from(Person.class, "p")
                        .select("id")
                        .where("name").eqExpression("d.name")
                    .end()
                .endAnd()
            .endOr();
        String expected = "SELECT d FROM Document d GROUP BY d.name HAVING d.name = :param_0 AND (EXISTS (SELECT p.id FROM Person p WHERE p.name = d.name))";

        assertEquals(expected, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testHavingNotExistsAndBuilder() {
        CriteriaBuilder<Document> crit = cbf.from(em, Document.class, "d");
        crit.groupBy("name")
            .having("d.name").eq("test")
            .havingOr()
                .havingAnd()
                    .havingNotExists()
                        .from(Person.class, "p")
                        .select("id")
                        .where("name").eqExpression("d.name")
                    .end()
                .endAnd()
            .endOr();
        String expected = "SELECT d FROM Document d GROUP BY d.name HAVING d.name = :param_0 AND (NOT EXISTS (SELECT p.id FROM Person p WHERE p.name = d.name))";

        assertEquals(expected, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testHavingExistsOrBuilder() {
        CriteriaBuilder<Document> crit = cbf.from(em, Document.class, "d");
        crit.groupBy("name")
            .havingOr()
                .having("d.name").eq("test")
                .havingExists()
                    .from(Person.class, "p")
                    .select("id")
                    .where("name").eqExpression("d.name")
                .end()
            .endOr();
        String expected = "SELECT d FROM Document d GROUP BY d.name HAVING d.name = :param_0 OR EXISTS (SELECT p.id FROM Person p WHERE p.name = d.name)";

        assertEquals(expected, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testHavingNotExistsOrBuilder() {
        CriteriaBuilder<Document> crit = cbf.from(em, Document.class, "d");
        crit.groupBy("name")
            .havingOr()
                .having("d.name").eq("test")
                .havingNotExists()
                    .from(Person.class, "p")
                    .select("id")
                    .where("name").eqExpression("d.name")
                .end()
            .endOr();
        String expected = "SELECT d FROM Document d GROUP BY d.name HAVING d.name = :param_0 OR NOT EXISTS (SELECT p.id FROM Person p WHERE p.name = d.name)";

        assertEquals(expected, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testHavingLeftSubquery() {
        CriteriaBuilder<Document> crit = cbf.from(em, Document.class, "d");
        crit.groupBy("id")
            .havingSubquery()
                .from(Person.class, "p")
                .select("id")
                .where("name").eqExpression("d.name")
            .end().eqExpression("id");
        String expected = "SELECT d FROM Document d GROUP BY d.id HAVING (SELECT p.id FROM Person p WHERE p.name = d.name) = d.id";

        assertEquals(expected, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testHavingLeftSubqueryAndBuilder() {
        CriteriaBuilder<Document> crit = cbf.from(em, Document.class, "d");
        crit.groupBy("name")
            .having("d.name").eq("test")
            .havingOr()
                .havingAnd()
                    .havingSubquery()
                        .from(Person.class, "p")
                        .select("id")
                        .where("name").eqExpression("d.name")
                    .end().eqExpression("d.owner.id")
                .endAnd()
            .endOr();
        String expected = "SELECT d FROM Document d JOIN d.owner owner_1 GROUP BY d.name HAVING d.name = :param_0 AND ((SELECT p.id FROM Person p WHERE p.name = d.name) = owner_1.id)";

        assertEquals(expected, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testHavingLeftSubqueryOrBuilder() {
        CriteriaBuilder<Document> crit = cbf.from(em, Document.class, "d");
        crit.groupBy("name")
            .havingOr()
                .having("d.name").eq("test")
                .havingSubquery()
                    .from(Person.class, "p")
                    .select("id").where("name").eqExpression("d.name")
                .end().eqExpression("d.owner.id")
            .endOr();
        String expected = "SELECT d FROM Document d JOIN d.owner owner_1 GROUP BY d.name HAVING d.name = :param_0 OR (SELECT p.id FROM Person p WHERE p.name = d.name) = owner_1.id";

        assertEquals(expected, crit.getQueryString());
        crit.getResultList();
    }
    
    @Test
    public void testHavingSubqueryWithSurroundingExpression() {
        CriteriaBuilder<Document> crit = cbf.from(em, Document.class, "d");
        crit.groupBy("name")
            .havingSubquery("alias", "SUM(alias)")
                .from(Person.class, "p")
                .select("id")
                .where("name").eqExpression("d.name")
            .end().eqExpression("d.owner.id");
        String expected = "SELECT d FROM Document d JOIN d.owner owner_1 GROUP BY d.name HAVING SUM((SELECT p.id FROM Person p WHERE p.name = d.name)) = owner_1.id";
        
        assertEquals(expected, crit.getQueryString());
//        TODO: restore as soon as hibernate supports this
//        cb.getResultList(); 
    }
    

    @Test
    public void testWhereMultipleSubqueryWithSurroundingExpression() {
        CriteriaBuilder<Document> crit = cbf.from(em, Document.class, "d");
        crit.groupBy("name")
            .havingSubquery("alias", "alias * alias")
                .from(Person.class, "p")
                .select("COUNT(id)")
                .where("name").eqExpression("d.name")
            .end().eqExpression("d.owner.id");
        String expected = "SELECT d FROM Document d JOIN d.owner owner_1 GROUP BY d.name HAVING (SELECT COUNT(p.id) FROM Person p WHERE p.name = d.name) * (SELECT COUNT(p.id) FROM Person p WHERE p.name = d.name) = owner_1.id";
        
        assertEquals(expected, crit.getQueryString());
        crit.getResultList();
    }
    
    @Test
    public void testHavingAndSubqueryWithSurroundingExpression() {
        CriteriaBuilder<Document> crit = cbf.from(em, Document.class, "d");
        crit.groupBy("name")
            .having("d.name").eq("test")
            .havingOr()
                .havingAnd()
                    .havingSubquery("alias", "SUM(alias)")
                        .from(Person.class, "p")
                        .select("id")
                        .where("name").eqExpression("d.name")
                    .end().eqExpression("d.owner.id")
                .endAnd()
            .endOr();        
        String expected = "SELECT d FROM Document d JOIN d.owner owner_1 GROUP BY d.name HAVING d.name = :param_0 AND (SUM((SELECT p.id FROM Person p WHERE p.name = d.name)) = owner_1.id)";
        
        assertEquals(expected, crit.getQueryString());
//        TODO: restore as soon as hibernate supports this
//        cb.getResultList(); 
    }
    
    @Test
    public void testHavingAndMultipleSubqueryWithSurroundingExpression() {
        CriteriaBuilder<Document> crit = cbf.from(em, Document.class, "d");
        crit.groupBy("name")
            .having("d.name").eq("test")
            .havingOr()
                .havingAnd()
                    .havingSubquery("alias", "alias * alias")
                        .from(Person.class, "p")
                        .select("id")
                        .where("name").eqExpression("d.name")
                    .end().eqExpression("d.owner.id")
                .endAnd()
            .endOr();        
        String expected = "SELECT d FROM Document d JOIN d.owner owner_1 GROUP BY d.name HAVING d.name = :param_0 AND ((SELECT p.id FROM Person p WHERE p.name = d.name) * (SELECT p.id FROM Person p WHERE p.name = d.name) = owner_1.id)";
        
        assertEquals(expected, crit.getQueryString());
//        TODO: restore as soon as hibernate supports this
//        cb.getResultList(); 
    }
    
    @Test
    public void testHavingOrSubqueryWithSurroundingExpression() {
        CriteriaBuilder<Document> crit = cbf.from(em, Document.class, "d");
        crit.groupBy("name")
            .havingOr()
                .having("d.name").eq("test")
                .havingSubquery("alias", "SUM(alias)")
                    .from(Person.class, "p")
                    .select("id")
                    .where("name").eqExpression("d.name")
                .end().eqExpression("d.owner.id")
            .endOr();        
        String expected = "SELECT d FROM Document d JOIN d.owner owner_1 GROUP BY d.name HAVING d.name = :param_0 OR SUM((SELECT p.id FROM Person p WHERE p.name = d.name)) = owner_1.id";
        
        assertEquals(expected, crit.getQueryString());
//        TODO: restore as soon as hibernate supports this
//        cb.getResultList(); 
    }
    
    @Test
    public void testHavingOrMultipleSubqueryWithSurroundingExpression() {
        CriteriaBuilder<Document> crit = cbf.from(em, Document.class, "d");
        crit.groupBy("name")
            .havingOr()
                .having("d.name").eq("test")
                .havingSubquery("alias", "alias * alias")
                    .from(Person.class, "p")
                    .select("id")
                    .where("name").eqExpression("d.name")
                .end().eqExpression("d.owner.id")
            .endOr();        
        String expected = "SELECT d FROM Document d JOIN d.owner owner_1 GROUP BY d.name HAVING d.name = :param_0 OR (SELECT p.id FROM Person p WHERE p.name = d.name) * (SELECT p.id FROM Person p WHERE p.name = d.name) = owner_1.id";
        
        assertEquals(expected, crit.getQueryString());
//        TODO: restore as soon as hibernate supports this
//        cb.getResultList(); 
    }
}
