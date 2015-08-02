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
import com.blazebit.persistence.impl.BuilderChainingException;
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
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.groupBy("d.owner")
            .having("d.age").gt(0L);
        assertEquals("SELECT d FROM Document d JOIN d.owner owner_1 GROUP BY owner_1 HAVING d.age > :param_0", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testHavingPropertyExpression() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.groupBy("d.owner")
            .having("d.age + 1").gt(0L);

        assertEquals("SELECT d FROM Document d JOIN d.owner owner_1 GROUP BY owner_1 HAVING d.age + 1 > :param_0", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testHavingPath() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.groupBy("d.owner")
            .having("MIN(SQRT(d.partners.age))").gt(0d);

        assertEquals(
            "SELECT d FROM Document d JOIN d.owner owner_1 LEFT JOIN d.partners partners_1 GROUP BY owner_1 HAVING MIN(SQRT(partners_1.age)) > :param_0",
            criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testHavingPathExpression() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.groupBy("d.owner")
            .having("MIN(SQRT(d.partners.age)) + 1").gt(0d);

        assertEquals(
            "SELECT d FROM Document d JOIN d.owner owner_1 LEFT JOIN d.partners partners_1 GROUP BY owner_1 HAVING MIN(SQRT(partners_1.age)) + 1 > :param_0",
            criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testHavingAnd() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.groupBy("d.owner")
            .having("MIN(SQRT(d.partners.age))").gt(0d)
            .having("d.owner.name").like().value("http://%").noEscape();

        assertEquals(
            "SELECT d FROM Document d JOIN d.owner owner_1 LEFT JOIN d.partners partners_1 GROUP BY owner_1, owner_1.name HAVING MIN(SQRT(partners_1.age)) > :param_0 AND owner_1.name LIKE :param_1",
            criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testHavingOr() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.groupBy("d.owner")
            .havingOr()
                .having("MIN(SQRT(d.partners.age))").gt(0d)
                .having("d.owner.name").like().value("http://%").noEscape()
            .endOr();

        assertEquals(
            "SELECT d FROM Document d JOIN d.owner owner_1 LEFT JOIN d.partners partners_1 GROUP BY owner_1, owner_1.name HAVING MIN(SQRT(partners_1.age)) > :param_0 OR owner_1.name LIKE :param_1",
            criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testHavingOrAnd() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.groupBy("d.owner")
            .havingOr()
                .havingAnd()
                    .having("MIN(SQRT(d.partners.age))").gt(0d)
                    .having("d.owner.name").like().value("http://%").noEscape()
                .endAnd()
                .havingAnd()
                    .having("d.owner.age").lt(10L)
                    .having("d.owner.name").like().value("ftp://%").noEscape()
                .endAnd()
            .endOr();

        assertEquals(
            "SELECT d FROM Document d JOIN d.owner owner_1 LEFT JOIN d.partners partners_1 GROUP BY owner_1, owner_1.name, owner_1.age HAVING (MIN(SQRT(partners_1.age)) > :param_0 AND owner_1.name LIKE :param_1) OR (owner_1.age < :param_2 AND owner_1.name LIKE :param_3)",
            criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testHavingAndOr() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.groupBy("d.owner")
            .havingOr()
                .having("MIN(SQRT(d.partners.age))").gt(0d)
                .having("d.owner.name").like().value("http://%").noEscape()
            .endOr()
            .havingOr()
                .having("d.owner.age").lt(10L)
                .having("d.owner.name").like().value("ftp://%").noEscape()
            .endOr();

        assertEquals(
            "SELECT d FROM Document d JOIN d.owner owner_1 LEFT JOIN d.partners partners_1 GROUP BY owner_1, owner_1.name, owner_1.age HAVING (MIN(SQRT(partners_1.age)) > :param_0 OR owner_1.name LIKE :param_1) AND (owner_1.age < :param_2 OR owner_1.name LIKE :param_3)",
            criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testHavingOrSingleClause() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.groupBy("d.owner")
            .havingOr()
                .having("MIN(SQRT(d.partners.age))").gt(0d)
            .endOr();

        assertEquals(
            "SELECT d FROM Document d JOIN d.owner owner_1 LEFT JOIN d.partners partners_1 GROUP BY owner_1 HAVING MIN(SQRT(partners_1.age)) > :param_0",
            criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testHavingOrHavingAndSingleClause() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.groupBy("d.owner")
            .havingOr()
                .havingAnd()
                    .having("MIN(SQRT(d.partners.age))").gt(0d)
                .endAnd()
            .endOr();

        assertEquals(
            "SELECT d FROM Document d JOIN d.owner owner_1 LEFT JOIN d.partners partners_1 GROUP BY owner_1 HAVING MIN(SQRT(partners_1.age)) > :param_0",
            criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testHavingWithoutGroupBy() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        verifyException(criteria, IllegalStateException.class).having("d.partners.name");
    }

    @Test
    public void testHavingNull() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        verifyException(criteria.groupBy("d.owner"), NullPointerException.class).having(null);
    }

    @Test
    public void testHavingExists() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
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
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
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
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
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
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
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
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
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
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
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
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
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
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
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
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
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
        String expected = "SELECT d FROM Document d GROUP BY d.name HAVING d.name = :param_0 AND ((SELECT p.id FROM Person p WHERE p.name = d.name) = d.owner.id)";

        assertEquals(expected, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testHavingLeftSubqueryOrBuilder() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.groupBy("name")
            .havingOr()
                .having("d.name").eq("test")
                .havingSubquery()
                    .from(Person.class, "p")
                    .select("id").where("name").eqExpression("d.name")
                .end().eqExpression("d.owner.id")
            .endOr();
        String expected = "SELECT d FROM Document d GROUP BY d.name HAVING d.name = :param_0 OR (SELECT p.id FROM Person p WHERE p.name = d.name) = d.owner.id";

        assertEquals(expected, crit.getQueryString());
        crit.getResultList();
    }
    
    @Test
    public void testHavingSubqueryWithSurroundingExpression() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.groupBy("name")
            .havingSubquery("alias", "SUM(alias)")
                .from(Person.class, "p")
                .select("id")
                .where("name").eqExpression("d.name")
            .end().eqExpression("d.owner.id");
        String expected = "SELECT d FROM Document d GROUP BY d.name, d.owner.id HAVING SUM((SELECT p.id FROM Person p WHERE p.name = d.name)) = d.owner.id";
        
        assertEquals(expected, crit.getQueryString());
//        TODO: restore as soon as hibernate supports this
//        cb.getResultList(); 
    }
    

    @Test
    public void testWhereMultipleSubqueryWithSurroundingExpression() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.groupBy("name")
            .havingSubquery("alias", "alias * alias")
                .from(Person.class, "p")
                .select("COUNT(id)")
                .where("name").eqExpression("d.name")
            .end().eqExpression("d.owner.id");
        String expected = "SELECT d FROM Document d GROUP BY d.name HAVING (SELECT COUNT(p.id) FROM Person p WHERE p.name = d.name) * (SELECT COUNT(p.id) FROM Person p WHERE p.name = d.name) = d.owner.id";
        
        assertEquals(expected, crit.getQueryString());
        crit.getResultList();
    }
    
    @Test
    public void testHavingAndSubqueryWithSurroundingExpression() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
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
        String expected = "SELECT d FROM Document d GROUP BY d.name, d.owner.id HAVING d.name = :param_0 AND (SUM((SELECT p.id FROM Person p WHERE p.name = d.name)) = d.owner.id)";
        
        assertEquals(expected, crit.getQueryString());
//        TODO: restore as soon as hibernate supports this
//        cb.getResultList(); 
    }
    
    @Test
    public void testHavingAndMultipleSubqueryWithSurroundingExpression() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
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
        String expected = "SELECT d FROM Document d GROUP BY d.name HAVING d.name = :param_0 AND ((SELECT p.id FROM Person p WHERE p.name = d.name) * (SELECT p.id FROM Person p WHERE p.name = d.name) = d.owner.id)";
        
        assertEquals(expected, crit.getQueryString());
//        TODO: restore as soon as hibernate supports this
//        cb.getResultList(); 
    }
    
    @Test
    public void testHavingOrSubqueryWithSurroundingExpression() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.groupBy("name")
            .havingOr()
                .having("d.name").eq("test")
                .havingSubquery("alias", "SUM(alias)")
                    .from(Person.class, "p")
                    .select("id")
                    .where("name").eqExpression("d.name")
                .end().eqExpression("d.owner.id")
            .endOr();        
        String expected = "SELECT d FROM Document d GROUP BY d.name, d.owner.id HAVING d.name = :param_0 OR SUM((SELECT p.id FROM Person p WHERE p.name = d.name)) = d.owner.id";
        
        assertEquals(expected, crit.getQueryString());
//        TODO: restore as soon as hibernate supports this
//        cb.getResultList(); 
    }
    
    @Test
    public void testHavingOrMultipleSubqueryWithSurroundingExpression() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.groupBy("name")
            .havingOr()
                .having("d.name").eq("test")
                .havingSubquery("alias", "alias * alias")
                    .from(Person.class, "p")
                    .select("id")
                    .where("name").eqExpression("d.name")
                .end().eqExpression("d.owner.id")
            .endOr();
        String expected = "SELECT d FROM Document d GROUP BY d.name HAVING d.name = :param_0 OR (SELECT p.id FROM Person p WHERE p.name = d.name) * (SELECT p.id FROM Person p WHERE p.name = d.name) = d.owner.id";
        
        assertEquals(expected, crit.getQueryString());
//        TODO: restore as soon as hibernate supports this
//        cb.getResultList(); 
    }
    
    /* having case tests */
    
    @Test
    public void testHavingCase() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.groupBy("d.id").havingCase().when("d.id").geExpression("d.age").thenExpression("2").otherwiseExpression("1").eqExpression("d.idx");
        String expected = "SELECT d FROM Document d GROUP BY d.id HAVING CASE WHEN d.id >= d.age THEN 2 ELSE 1 END = d.idx";
        assertEquals(expected, crit.getQueryString());
        crit.getResultList(); 
    }
    
    @Test
    public void testHavingCaseBuilderNotEnded() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.groupBy("d.id").havingCase();
        verifyBuilderChainingException(crit);
    }
    
    @Test
    public void testHavingSimpleCaseBuilderNotEnded() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.groupBy("d.id").havingCase();
        verifyBuilderChainingException(crit);
    }
    
    @Test
    public void testHavingSimpleCase() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.groupBy("d.id").havingSimpleCase("d.id").when("1", "d.age").otherwise("d.idx").eqExpression("d.idx");
        String expected = "SELECT d FROM Document d GROUP BY d.id HAVING CASE d.id WHEN 1 THEN d.age ELSE d.idx END = d.idx";
        assertEquals(expected, crit.getQueryString());
        crit.getResultList(); 
    }
    
    @Test
    public void testHavingAndCase() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.groupBy("d.id").havingOr().havingAnd().havingCase()
                .whenAnd().and("d.id").eqExpression("d.age").and("d.age").ltExpression("4").thenExpression("2")
                .when("d.id").eqExpression("4").thenExpression("4").otherwiseExpression("3").eqExpression("2").endAnd().endOr();
        String expected = "SELECT d FROM Document d GROUP BY d.id HAVING CASE WHEN d.id = d.age AND d.age < 4 THEN 2 WHEN d.id = 4 THEN 4 ELSE 3 END = 2";
        assertEquals(expected, crit.getQueryString());
        crit.getResultList(); 
    }
    
    @Test
    public void testHavingAndSimpleCase() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.groupBy("d.id").havingOr().havingAnd().havingSimpleCase("d.id")
                .when("d.age", "2")
                .when("4", "4").otherwise("3").eqExpression("2").endAnd().endOr();
        String expected = "SELECT d FROM Document d GROUP BY d.id HAVING CASE d.id WHEN d.age THEN 2 WHEN 4 THEN 4 ELSE 3 END = 2";
        assertEquals(expected, crit.getQueryString());
        crit.getResultList(); 
    }
    
    @Test
    public void testHavingOrCase() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.groupBy("d.id").havingOr().havingCase()
                .whenAnd().and("d.id").eqExpression("d.age").and("d.age").ltExpression("4").thenExpression("2")
                .when("d.id").eqExpression("4").thenExpression("4").otherwiseExpression("3").eqExpression("2").endOr();
        String expected = "SELECT d FROM Document d GROUP BY d.id HAVING CASE WHEN d.id = d.age AND d.age < 4 THEN 2 WHEN d.id = 4 THEN 4 ELSE 3 END = 2";
        assertEquals(expected, crit.getQueryString());
        crit.getResultList(); 
    }
    
    @Test
    public void testHavingOrSimpleCase() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.groupBy("d.id").havingOr().havingSimpleCase("d.id")
                .when("d.age", "2")
                .when("4", "4").otherwise("3").eqExpression("2").endOr();
        String expected = "SELECT d FROM Document d GROUP BY d.id HAVING CASE d.id WHEN d.age THEN 2 WHEN 4 THEN 4 ELSE 3 END = 2";
        assertEquals(expected, crit.getQueryString());
        crit.getResultList(); 
    }
    
    private void verifyBuilderChainingException(CriteriaBuilder<Document> crit){
        verifyException(crit, BuilderChainingException.class).havingCase();
        verifyException(crit, BuilderChainingException.class).havingSimpleCase("d.id");
        verifyException(crit, BuilderChainingException.class).having("d.id");
        verifyException(crit, BuilderChainingException.class).getQueryString();
    }
}
