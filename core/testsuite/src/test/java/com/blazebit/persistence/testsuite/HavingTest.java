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
import org.junit.experimental.categories.Category;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.impl.BuilderChainingException;
import com.blazebit.persistence.impl.ConfigurationProperties;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMySQL;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class HavingTest extends AbstractCoreTest {

    @Test
    public void testHaving() {
        CriteriaBuilder<Long> criteria = cbf.create(em, Long.class).from(Document.class, "d").select("COUNT(d.id)");
        criteria.groupBy("d.owner")
            .having("d.age").gt(0L);
        assertEquals("SELECT COUNT(d.id) FROM Document d JOIN d.owner owner_1 GROUP BY owner_1, d.age HAVING d.age > :param_0", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    @Category(NoMySQL.class)
    public void testHavingPropertyExpression() {
        CriteriaBuilder<Long> criteria = cbf.create(em, Long.class).from(Document.class, "d").select("COUNT(d.id)");
        criteria.groupBy("d.owner")
            .having("d.age + 1").gt(0L);

        assertEquals("SELECT COUNT(d.id) FROM Document d JOIN d.owner owner_1 GROUP BY owner_1, " + groupByPathExpressions("d.age + 1", "d.age") + " HAVING d.age + 1 > :param_0", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testHavingPath() {
        CriteriaBuilder<Long> criteria = cbf.create(em, Long.class).from(Document.class, "d").select("COUNT(d.id)");
        criteria.groupBy("d.owner")
            .having("MIN(SQRT(d.partners.age))").gt(0d);

        assertEquals(
            "SELECT COUNT(d.id) FROM Document d JOIN d.owner owner_1 LEFT JOIN d.partners partners_1 GROUP BY owner_1 HAVING MIN(SQRT(partners_1.age)) > :param_0",
            criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testHavingPathExpression() {
        CriteriaBuilder<Long> criteria = cbf.create(em, Long.class).from(Document.class, "d").select("COUNT(d.id)");
        criteria.groupBy("d.owner")
            .having("MIN(SQRT(d.partners.age)) + 1").gt(0d);

        assertEquals(
            "SELECT COUNT(d.id) FROM Document d JOIN d.owner owner_1 LEFT JOIN d.partners partners_1 GROUP BY owner_1 HAVING MIN(SQRT(partners_1.age)) + 1 > :param_0",
            criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testHavingAnd() {
        CriteriaBuilder<Long> criteria = cbf.create(em, Long.class).from(Document.class, "d").select("COUNT(d.id)");
        criteria.groupBy("d.owner")
            .having("MIN(SQRT(d.partners.age))").gt(0d)
            .having("d.owner.name").like().value("http://%").noEscape();

        assertEquals(
            "SELECT COUNT(d.id) FROM Document d JOIN d.owner owner_1 LEFT JOIN d.partners partners_1 GROUP BY owner_1, owner_1.name HAVING MIN(SQRT(partners_1.age)) > :param_0 AND owner_1.name LIKE :param_1",
            criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testHavingOr() {
        CriteriaBuilder<Long> criteria = cbf.create(em, Long.class).from(Document.class, "d").select("COUNT(d.id)");
        criteria.groupBy("d.owner")
            .havingOr()
                .having("MIN(SQRT(d.partners.age))").gt(0d)
                .having("d.owner.name").like().value("http://%").noEscape()
            .endOr();

        assertEquals(
            "SELECT COUNT(d.id) FROM Document d JOIN d.owner owner_1 LEFT JOIN d.partners partners_1 GROUP BY owner_1, owner_1.name HAVING MIN(SQRT(partners_1.age)) > :param_0 OR owner_1.name LIKE :param_1",
            criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testHavingOrAnd() {
        CriteriaBuilder<Long> criteria = cbf.create(em, Long.class).from(Document.class, "d").select("COUNT(d.id)");
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
            "SELECT COUNT(d.id) FROM Document d JOIN d.owner owner_1 LEFT JOIN d.partners partners_1 GROUP BY owner_1, owner_1.name, owner_1.age HAVING (MIN(SQRT(partners_1.age)) > :param_0 AND owner_1.name LIKE :param_1) OR (owner_1.age < :param_2 AND owner_1.name LIKE :param_3)",
            criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testHavingAndOr() {
        CriteriaBuilder<Long> criteria = cbf.create(em, Long.class).from(Document.class, "d").select("COUNT(d.id)");
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
            "SELECT COUNT(d.id) FROM Document d JOIN d.owner owner_1 LEFT JOIN d.partners partners_1 GROUP BY owner_1, owner_1.name, owner_1.age HAVING (MIN(SQRT(partners_1.age)) > :param_0 OR owner_1.name LIKE :param_1) AND (owner_1.age < :param_2 OR owner_1.name LIKE :param_3)",
            criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testHavingOrSingleClause() {
        CriteriaBuilder<Long> criteria = cbf.create(em, Long.class).from(Document.class, "d").select("COUNT(d.id)");
        criteria.groupBy("d.owner")
            .havingOr()
                .having("MIN(SQRT(d.partners.age))").gt(0d)
            .endOr();

        assertEquals(
            "SELECT COUNT(d.id) FROM Document d JOIN d.owner owner_1 LEFT JOIN d.partners partners_1 GROUP BY owner_1 HAVING MIN(SQRT(partners_1.age)) > :param_0",
            criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testHavingOrHavingAndSingleClause() {
        CriteriaBuilder<Long> criteria = cbf.create(em, Long.class).from(Document.class, "d").select("COUNT(d.id)");
        criteria.groupBy("d.owner")
            .havingOr()
                .havingAnd()
                    .having("MIN(SQRT(d.partners.age))").gt(0d)
                .endAnd()
            .endOr();

        assertEquals(
            "SELECT COUNT(d.id) FROM Document d JOIN d.owner owner_1 LEFT JOIN d.partners partners_1 GROUP BY owner_1 HAVING MIN(SQRT(partners_1.age)) > :param_0",
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
        CriteriaBuilder<Long> criteria = cbf.create(em, Long.class).from(Document.class, "d").select("COUNT(d.id)");
        criteria.groupBy("name")
            .havingExists()
                .from(Person.class, "p")
                .select("id")
                .where("name").eqExpression("d.name")
            .end();
        String expected = "SELECT COUNT(d.id) FROM Document d GROUP BY d.name HAVING EXISTS (SELECT p.id FROM Person p WHERE p.name = d.name)";

        assertEquals(expected, criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testHavingNotExists() {
        CriteriaBuilder<Long> criteria = cbf.create(em, Long.class).from(Document.class, "d").select("COUNT(d.id)");
        criteria.groupBy("name")
            .havingNotExists()
                .from(Person.class, "p")
                .select("id")
                .where("name").eqExpression("d.name")
            .end();
        String expected = "SELECT COUNT(d.id) FROM Document d GROUP BY d.name HAVING NOT EXISTS (SELECT p.id FROM Person p WHERE p.name = d.name)";

        assertEquals(expected, criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testHavingNotExists2() {
        CriteriaBuilder<Long> criteria = cbf.create(em, Long.class).from(Document.class, "d").select("COUNT(d.id)");
        criteria.groupBy("name")
            .having("d.name").eq("test")
            .havingNotExists()
                .from(Person.class, "p")
                .select("id")
                .where("name").eqExpression("d.name")
            .end();
        String expected = "SELECT COUNT(d.id) FROM Document d GROUP BY d.name HAVING d.name = :param_0 AND NOT EXISTS (SELECT p.id FROM Person p WHERE p.name = d.name)";

        assertEquals(expected, criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testHavingExistsAndBuilder() {
        CriteriaBuilder<Long> criteria = cbf.create(em, Long.class).from(Document.class, "d").select("COUNT(d.id)");
        criteria.groupBy("name")
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
        String expected = "SELECT COUNT(d.id) FROM Document d GROUP BY d.name HAVING d.name = :param_0 AND (EXISTS (SELECT p.id FROM Person p WHERE p.name = d.name))";

        assertEquals(expected, criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testHavingNotExistsAndBuilder() {
        CriteriaBuilder<Long> criteria = cbf.create(em, Long.class).from(Document.class, "d").select("COUNT(d.id)");
        criteria.groupBy("name")
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
        String expected = "SELECT COUNT(d.id) FROM Document d GROUP BY d.name HAVING d.name = :param_0 AND (NOT EXISTS (SELECT p.id FROM Person p WHERE p.name = d.name))";

        assertEquals(expected, criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testHavingExistsOrBuilder() {
        CriteriaBuilder<Long> criteria = cbf.create(em, Long.class).from(Document.class, "d").select("COUNT(d.id)");
        criteria.groupBy("name")
            .havingOr()
                .having("d.name").eq("test")
                .havingExists()
                    .from(Person.class, "p")
                    .select("id")
                    .where("name").eqExpression("d.name")
                .end()
            .endOr();
        String expected = "SELECT COUNT(d.id) FROM Document d GROUP BY d.name HAVING d.name = :param_0 OR EXISTS (SELECT p.id FROM Person p WHERE p.name = d.name)";

        assertEquals(expected, criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testHavingNotExistsOrBuilder() {
        CriteriaBuilder<Long> criteria = cbf.create(em, Long.class).from(Document.class, "d").select("COUNT(d.id)");
        criteria.groupBy("name")
            .havingOr()
                .having("d.name").eq("test")
                .havingNotExists()
                    .from(Person.class, "p")
                    .select("id")
                    .where("name").eqExpression("d.name")
                .end()
            .endOr();
        String expected = "SELECT COUNT(d.id) FROM Document d GROUP BY d.name HAVING d.name = :param_0 OR NOT EXISTS (SELECT p.id FROM Person p WHERE p.name = d.name)";

        assertEquals(expected, criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testHavingLeftSubquery() {
        CriteriaBuilder<Long> criteria = cbf.create(em, Long.class).from(Document.class, "d").select("COUNT(d.id)");
        criteria.groupBy("id")
            .havingSubquery()
                .from(Person.class, "p")
                .select("id")
                .where("name").eqExpression("d.name")
            .end().eqExpression("id");
        String expected = "SELECT COUNT(d.id) FROM Document d GROUP BY d.id, d.name HAVING (SELECT p.id FROM Person p WHERE p.name = d.name) = d.id";

        assertEquals(expected, criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testHavingLeftSubqueryAndBuilder() {
        CriteriaBuilder<Long> criteria = cbf.create(em, Long.class).from(Document.class, "d").select("COUNT(d.id)");
        criteria.groupBy("name")
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
        String expected = "SELECT COUNT(d.id) FROM Document d" + singleValuedAssociationIdJoin("d.owner", "owner_1", false) + " GROUP BY d.name, " + singleValuedAssociationIdPath("d.owner.id", "owner_1") + " HAVING d.name = :param_0 AND ((SELECT p.id FROM Person p WHERE p.name = d.name) = " + singleValuedAssociationIdPath("d.owner.id", "owner_1") + ")";

        assertEquals(expected, criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testHavingLeftSubqueryOrBuilder() {
        CriteriaBuilder<Long> criteria = cbf.create(em, Long.class).from(Document.class, "d").select("COUNT(d.id)");
        criteria.groupBy("name")
            .havingOr()
                .having("d.name").eq("test")
                .havingSubquery()
                    .from(Person.class, "p")
                    .select("id").where("name").eqExpression("d.name")
                .end().eqExpression("d.owner.id")
            .endOr();
        String expected = "SELECT COUNT(d.id) FROM Document d" + singleValuedAssociationIdJoin("d.owner", "owner_1", false) + " GROUP BY d.name, " + singleValuedAssociationIdPath("d.owner.id", "owner_1") + " HAVING d.name = :param_0 OR (SELECT p.id FROM Person p WHERE p.name = d.name) = " + singleValuedAssociationIdPath("d.owner.id", "owner_1");

        assertEquals(expected, criteria.getQueryString());
        criteria.getResultList();
    }
    
    @Test
    public void testHavingSubqueryWithSurroundingExpression() {
        CriteriaBuilder<Long> criteria = cbf.create(em, Long.class).from(Document.class, "d").select("COUNT(d.id)");
        criteria.groupBy("name")
            .havingSubquery("alias", "ABS(alias)")
                .from(Person.class, "p")
                .select("id")
                .where("name").eqExpression("d.name")
            .end().eqExpression("d.owner.id");
        String expected = "SELECT COUNT(d.id) FROM Document d" + singleValuedAssociationIdJoin("d.owner", "owner_1", false) + " GROUP BY d.name, " + singleValuedAssociationIdPath("d.owner.id", "owner_1") + " HAVING ABS((SELECT p.id FROM Person p WHERE p.name = d.name)) = "  + singleValuedAssociationIdPath("d.owner.id", "owner_1");
        
        assertEquals(expected, criteria.getQueryString());
        criteria.getResultList(); 
    }
    

    @Test
    public void testWhereMultipleSubqueryWithSurroundingExpression() {
        CriteriaBuilder<Long> criteria = cbf.create(em, Long.class).from(Document.class, "d").select("COUNT(d.id)");
        criteria.groupBy("name")
            .havingSubquery("alias", "alias * alias")
                .from(Person.class, "p")
                .select("COUNT(id)")
                .where("name").eqExpression("d.name")
            .end().eqExpression("d.owner.id");
        String expected = "SELECT COUNT(d.id) FROM Document d" + singleValuedAssociationIdJoin("d.owner", "owner_1", false) + " GROUP BY d.name, " + singleValuedAssociationIdPath("d.owner.id", "owner_1") + " HAVING (SELECT COUNT(p.id) FROM Person p WHERE p.name = d.name) * (SELECT COUNT(p.id) FROM Person p WHERE p.name = d.name) = " + singleValuedAssociationIdPath("d.owner.id", "owner_1");
        
        assertEquals(expected, criteria.getQueryString());
        criteria.getResultList();
    }
    
    @Test
    public void testHavingAndSubqueryWithSurroundingExpression() {
        CriteriaBuilder<Long> criteria = cbf.create(em, Long.class).from(Document.class, "d").select("COUNT(d.id)");
        criteria.groupBy("name")
            .having("d.name").eq("test")
            .havingOr()
                .havingAnd()
                    .havingSubquery("alias", "ABS(alias)")
                        .from(Person.class, "p")
                        .select("id")
                        .where("name").eqExpression("d.name")
                    .end().eqExpression("d.owner.id")
                .endAnd()
            .endOr();        
        String expected = "SELECT COUNT(d.id) FROM Document d" + singleValuedAssociationIdJoin("d.owner", "owner_1", false) + " GROUP BY d.name, " + singleValuedAssociationIdPath("d.owner.id", "owner_1") + " HAVING d.name = :param_0 AND (ABS((SELECT p.id FROM Person p WHERE p.name = d.name)) = " + singleValuedAssociationIdPath("d.owner.id", "owner_1") + ")";
        
        assertEquals(expected, criteria.getQueryString());
        criteria.getResultList(); 
    }
    
    @Test
    public void testHavingAndMultipleSubqueryWithSurroundingExpression() {
        CriteriaBuilder<Long> criteria = cbf.create(em, Long.class).from(Document.class, "d").select("COUNT(d.id)");
        criteria.groupBy("name")
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
        String expected = "SELECT COUNT(d.id) FROM Document d" + singleValuedAssociationIdJoin("d.owner", "owner_1", false) + " GROUP BY d.name, " + singleValuedAssociationIdPath("d.owner.id", "owner_1") + " HAVING d.name = :param_0 AND ((SELECT p.id FROM Person p WHERE p.name = d.name) * (SELECT p.id FROM Person p WHERE p.name = d.name) = " + singleValuedAssociationIdPath("d.owner.id", "owner_1") + ")";
        
        assertEquals(expected, criteria.getQueryString());
        criteria.getResultList(); 
    }
    
    @Test
    public void testHavingOrSubqueryWithSurroundingExpression() {
        CriteriaBuilder<Long> criteria = cbf.create(em, Long.class).from(Document.class, "d").select("COUNT(d.id)");
        criteria.groupBy("name")
            .havingOr()
                .having("d.name").eq("test")
                .havingSubquery("alias", "ABS(alias)")
                    .from(Person.class, "p")
                    .select("id")
                    .where("name").eqExpression("d.name")
                .end().eqExpression("d.owner.id")
            .endOr();        
        String expected = "SELECT COUNT(d.id) FROM Document d" + singleValuedAssociationIdJoin("d.owner", "owner_1", false) + " GROUP BY d.name, " + singleValuedAssociationIdPath("d.owner.id", "owner_1") + " HAVING d.name = :param_0 OR ABS((SELECT p.id FROM Person p WHERE p.name = d.name)) = " + singleValuedAssociationIdPath("d.owner.id", "owner_1");
        
        assertEquals(expected, criteria.getQueryString());
        criteria.getResultList(); 
    }
    
    @Test
    public void testHavingOrMultipleSubqueryWithSurroundingExpression() {
        CriteriaBuilder<Long> criteria = cbf.create(em, Long.class).from(Document.class, "d").select("COUNT(d.id)");
        criteria.groupBy("name")
            .havingOr()
                .having("d.name").eq("test")
                .havingSubquery("alias", "alias * alias")
                    .from(Person.class, "p")
                    .select("id")
                    .where("name").eqExpression("d.name")
                .end().eqExpression("d.owner.id")
            .endOr();
        String expected = "SELECT COUNT(d.id) FROM Document d" + singleValuedAssociationIdJoin("d.owner", "owner_1", false) + " GROUP BY d.name, " + singleValuedAssociationIdPath("d.owner.id", "owner_1") + " HAVING d.name = :param_0 OR (SELECT p.id FROM Person p WHERE p.name = d.name) * (SELECT p.id FROM Person p WHERE p.name = d.name) = " + singleValuedAssociationIdPath("d.owner.id", "owner_1");
        
        assertEquals(expected, criteria.getQueryString());
        criteria.getResultList(); 
    }
    
    /* having case tests */
    
    @Test
    public void testHavingCase1() {
        CriteriaBuilder<Long> criteria = cbf.create(em, Long.class).from(Document.class, "d").select("COUNT(versions.id)");
        criteria.groupBy("d.id").havingCase().when("d.id").geExpression("d.age").thenExpression("2").otherwiseExpression("1").eqExpression("d.idx");
        String expected = "SELECT COUNT(versions_1.id) FROM Document d LEFT JOIN d.versions versions_1 " +
                "GROUP BY d.id, " + groupByPathExpressions("CASE WHEN d.id >= d.age THEN 2 ELSE 1 END", "d.age") + ", d.idx " +
                "HAVING CASE WHEN d.id >= d.age THEN 2 ELSE 1 END = d.idx";
        assertEquals(expected, criteria.getQueryString());
        criteria.getResultList(); 
    }
    
    @Test
    public void testHavingCase2() {
        CriteriaBuilder<Long> criteria = cbf.create(em, Long.class).from(Document.class, "d").select("COUNT(versions.id)");
        criteria.setProperty(ConfigurationProperties.IMPLICIT_GROUP_BY_FROM_HAVING, "false");
        criteria.groupBy("d.id").havingCase().when("d.id").geExpression("d.age").thenExpression("2").otherwiseExpression("1").eqExpression("d.idx");
        
        String expected = "SELECT COUNT(versions_1.id) FROM Document d LEFT JOIN d.versions versions_1 GROUP BY d.id HAVING CASE WHEN d.id >= d.age THEN 2 ELSE 1 END = d.idx";
        assertEquals(expected, criteria.getQueryString());
        // Being able to omit functional dependent columns does not work for e.g. DB2, MySQL, MSSQL, Oracle etc.
        // Therefore we don't execute this query
        // criteria.getResultList();
    }
    
    @Test
    public void testHavingCaseBuilderNotEnded() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.groupBy("d.id").havingCase();
        verifyBuilderChainingException(criteria);
    }
    
    @Test
    public void testHavingSimpleCaseBuilderNotEnded() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.groupBy("d.id").havingCase();
        verifyBuilderChainingException(criteria);
    }
    
    @Test
    public void testHavingSimpleCase() {
        CriteriaBuilder<Long> criteria = cbf.create(em, Long.class).from(Document.class, "d").select("COUNT(versions.id)");
        criteria.groupBy("d.id").havingSimpleCase("d.id").when("1", "d.age").otherwise("d.idx").eqExpression("d.idx");
        String expected = "SELECT COUNT(versions_1.id) FROM Document d LEFT JOIN d.versions versions_1 " +
                "GROUP BY d.id, " + groupByPathExpressions("CASE d.id WHEN 1 THEN d.age ELSE d.idx END", "d.age") + ", d.idx " +
                "HAVING CASE d.id WHEN 1 THEN d.age ELSE d.idx END = d.idx";
        assertEquals(expected, criteria.getQueryString());
        criteria.getResultList(); 
    }
    
    @Test
    public void testHavingAndCase() {
        CriteriaBuilder<Long> criteria = cbf.create(em, Long.class).from(Document.class, "d").select("COUNT(versions.id)");
        criteria.groupBy("d.id").havingOr().havingAnd().havingCase()
                .whenAnd().and("d.id").eqExpression("d.age").and("d.age").ltExpression("4").thenExpression("2")
                .when("d.id").eqExpression("4").thenExpression("4").otherwiseExpression("3").eqExpression("2").endAnd().endOr();
        String expected = "SELECT COUNT(versions_1.id) FROM Document d LEFT JOIN d.versions versions_1 " +
                "GROUP BY d.id, " + groupByPathExpressions("CASE WHEN d.id = d.age AND d.age < 4 THEN 2 WHEN d.id = 4 THEN 4 ELSE 3 END", "d.age") + " " +
                "HAVING CASE WHEN d.id = d.age AND d.age < 4 THEN 2 WHEN d.id = 4 THEN 4 ELSE 3 END = 2";
        assertEquals(expected, criteria.getQueryString());
        criteria.getResultList(); 
    }
    
    @Test
    public void testHavingAndSimpleCase() {
        CriteriaBuilder<Long> criteria = cbf.create(em, Long.class).from(Document.class, "d").select("COUNT(versions.id)");
        criteria.groupBy("d.id").havingOr().havingAnd().havingSimpleCase("d.id")
                .when("d.age", "2")
                .when("4", "4").otherwise("3").eqExpression("2").endAnd().endOr();
        String expected = "SELECT COUNT(versions_1.id) FROM Document d LEFT JOIN d.versions versions_1 " +
                "GROUP BY d.id, " + groupByPathExpressions("CASE d.id WHEN d.age THEN 2 WHEN 4 THEN 4 ELSE 3 END", "d.age") + " " +
                "HAVING CASE d.id WHEN d.age THEN 2 WHEN 4 THEN 4 ELSE 3 END = 2";
        assertEquals(expected, criteria.getQueryString());
        criteria.getResultList(); 
    }
    
    @Test
    public void testHavingOrCase() {
        CriteriaBuilder<Long> criteria = cbf.create(em, Long.class).from(Document.class, "d").select("COUNT(versions.id)");
        criteria.groupBy("d.id").havingOr().havingCase()
                .whenAnd().and("d.id").eqExpression("d.age").and("d.age").ltExpression("4").thenExpression("2")
                .when("d.id").eqExpression("4").thenExpression("4").otherwiseExpression("3").eqExpression("2").endOr();
        String expected = "SELECT COUNT(versions_1.id) FROM Document d LEFT JOIN d.versions versions_1 " +
                "GROUP BY d.id, " + groupByPathExpressions("CASE WHEN d.id = d.age AND d.age < 4 THEN 2 WHEN d.id = 4 THEN 4 ELSE 3 END", "d.age") + " " +
                "HAVING CASE WHEN d.id = d.age AND d.age < 4 THEN 2 WHEN d.id = 4 THEN 4 ELSE 3 END = 2";
        assertEquals(expected, criteria.getQueryString());
        criteria.getResultList(); 
    }
    
    @Test
    public void testHavingOrSimpleCase() {
        CriteriaBuilder<Long> criteria = cbf.create(em, Long.class).from(Document.class, "d").select("COUNT(versions.id)");
        criteria.groupBy("d.id").havingOr().havingSimpleCase("d.id")
                .when("d.age", "2")
                .when("4", "4").otherwise("3").eqExpression("2").endOr();
        String expected = "SELECT COUNT(versions_1.id) FROM Document d LEFT JOIN d.versions versions_1 " +
                "GROUP BY d.id, " + groupByPathExpressions("CASE d.id WHEN d.age THEN 2 WHEN 4 THEN 4 ELSE 3 END", "d.age") + " " +
                "HAVING CASE d.id WHEN d.age THEN 2 WHEN 4 THEN 4 ELSE 3 END = 2";
        assertEquals(expected, criteria.getQueryString());
        criteria.getResultList(); 
    }
    
    @Test
    public void testHavingSize(){
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.select("d.id").groupBy("d.id").having("SIZE(d.partners)").gtExpression("1");
        
        final String expected = "SELECT d.id FROM Document d LEFT JOIN d.partners partners_1 GROUP BY d.id HAVING " + function("COUNT_TUPLE", "partners_1.id")+ " > 1";
        assertEquals(expected, criteria.getQueryString());
        criteria.getResultList();
    }
    
    @Test
    public void testHavingSizeMultiple(){
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.select("d.id").groupBy("d.id").having("SIZE(d.partners)").gtExpression("1").having("SIZE(d.versions)").gtExpression("2");
        
        final String expected = "SELECT d.id FROM Document d LEFT JOIN d.partners partners_1 LEFT JOIN d.versions versions_1 GROUP BY d.id HAVING " + function("COUNT_TUPLE" , "'DISTINCT'", "partners_1.id") + " > 1 AND " + function("COUNT_TUPLE", "'DISTINCT'", "versions_1.id") + " > 2";
        assertEquals(expected, criteria.getQueryString());
        criteria.getResultList();
    }
    
    private void verifyBuilderChainingException(CriteriaBuilder<Document> crit){
        verifyException(crit, BuilderChainingException.class).havingCase();
        verifyException(crit, BuilderChainingException.class).havingSimpleCase("d.id");
        verifyException(crit, BuilderChainingException.class).having("d.id");
        verifyException(crit, BuilderChainingException.class).getQueryString();
    }
}
