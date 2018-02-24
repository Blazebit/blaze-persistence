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

import java.util.Calendar;

import org.junit.Ignore;
import org.junit.Test;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.impl.BuilderChainingException;
import com.blazebit.persistence.testsuite.AbstractCoreTest;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class WhereTest extends AbstractCoreTest {

    @Test
    public void testWhereProperty() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.where("d.age").ge(25L);

        assertEquals("SELECT d FROM Document d WHERE d.age >= :param_0", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testWherePropertyExpression() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.where("d.age + 1").ge(25L);

        assertEquals("SELECT d FROM Document d WHERE d.age + 1 >= :param_0", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testWherePath() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.where("d.partners.age").gt(0L);

        assertEquals("SELECT d FROM Document d LEFT JOIN d.partners partners_1 WHERE partners_1.age > :param_0", criteria
                     .getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testWherePathExpression() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.where("d.owner.ownedDocuments.age + 1").ge(25L);

        assertEquals(
            "SELECT d FROM Document d JOIN d.owner owner_1 LEFT JOIN owner_1.ownedDocuments ownedDocuments_1 WHERE ownedDocuments_1.age + 1 >= :param_0",
            criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testWhereAnd() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.where("d.partners.age").gt(0L).where("d.versions.url").like().value("http://%").noEscape();

        assertEquals(
            "SELECT d FROM Document d LEFT JOIN d.partners partners_1 LEFT JOIN d.versions versions_1 WHERE partners_1.age > :param_0 AND versions_1.url LIKE :param_1",
            criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testWhereOr() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.whereOr().where("d.partners.age").gt(0L).where("d.versions.url").like().value("http://%").noEscape().endOr();

        assertEquals(
            "SELECT d FROM Document d LEFT JOIN d.partners partners_1 LEFT JOIN d.versions versions_1 WHERE partners_1.age > :param_0 OR versions_1.url LIKE :param_1",
            criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testWhereOrAnd() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria
            .whereOr()
                .whereAnd()
                    .where("d.partners.age").gt(0L)
                    .where("d.versions.url").like().value("http://%").noEscape()
                .endAnd()
                .whereAnd()
                    .where("d.versions.date").lt(Calendar.getInstance())
                    .where("d.versions.url").like().value("ftp://%").noEscape()
                .endAnd()
            .endOr();
        assertEquals(
            "SELECT d FROM Document d LEFT JOIN d.partners partners_1 LEFT JOIN d.versions versions_1 WHERE (partners_1.age > :param_0 AND versions_1.url LIKE :param_1) OR (versions_1.date < :param_2 AND versions_1.url LIKE :param_3)",
            criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testWhereAndOr() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria
            .whereOr()
                .where("d.partners.age").gt(0L)
                .where("d.versions.url").like().value("http://%").noEscape()
            .endOr()
            .whereOr()
                .where("d.versions.date").lt(Calendar.getInstance())
                .where("d.versions.url").like().value("ftp://%").noEscape()
            .endOr();

        assertEquals(
            "SELECT d FROM Document d LEFT JOIN d.partners partners_1 LEFT JOIN d.versions versions_1 WHERE (partners_1.age > :param_0 OR versions_1.url LIKE :param_1) AND (versions_1.date < :param_2 OR versions_1.url LIKE :param_3)",
            criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testWhereOrSingleClause() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.whereOr().where("d.partners.age").gt(0L).endOr();

        assertEquals("SELECT d FROM Document d LEFT JOIN d.partners partners_1 WHERE partners_1.age > :param_0", criteria
                     .getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testWhereOrWhereAndSingleClause() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.whereOr().whereAnd().where("d.versions.date").gt(Calendar.getInstance()).endAnd().endOr();

        assertEquals("SELECT d FROM Document d LEFT JOIN d.versions versions_1 WHERE versions_1.date > :param_0", criteria
                     .getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testWhereNull() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        verifyException(criteria, NullPointerException.class).where(null);
    }

    @Test
    public void testWhereEmpty() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        verifyException(criteria, IllegalArgumentException.class).where("");
    }

    @Test
    public void testWhereNotClosed() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.where("d.age");
        verifyException(criteria, BuilderChainingException.class).where("d.owner");
    }

    @Test
    public void testWhereOrNotClosed() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.whereOr().where("d.partners.age").gt(0L);
        verifyException(criteria, BuilderChainingException.class).where("d.partners.name");
    }

    @Test
    public void testWhereAndNotClosed() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.whereOr().whereAnd().where("d.partners.age").gt(0L);
        verifyException(criteria, BuilderChainingException.class).where("d.partners.name");
    }

    @Test
    public void testWhereExists() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.whereExists().from(Person.class, "p").select("id").where("name").eqExpression("d.name").end();
        String expected = "SELECT d FROM Document d WHERE EXISTS (SELECT p.id FROM Person p WHERE p.name = d.name)";

        assertEquals(expected, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testWhereNotExists() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.whereNotExists().from(Person.class, "p").select("id").where("name").eqExpression("d.name").end();
        String expected = "SELECT d FROM Document d WHERE NOT EXISTS (SELECT p.id FROM Person p WHERE p.name = d.name)";

        assertEquals(expected, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testWhereExistsAndBuilder() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.where("d.name").eq("test").whereOr().whereAnd().whereExists().from(Person.class, "p").select("id").where("name")
            .eqExpression("d.name").end().endAnd().endOr();
        String expected = "SELECT d FROM Document d WHERE d.name = :param_0 AND (EXISTS (SELECT p.id FROM Person p WHERE p.name = d.name))";

        assertEquals(expected, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testWhereNotExistsAndBuilder() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.where("d.name").eq("test").whereOr().whereAnd().whereNotExists().from(Person.class, "p").select("id").where("name")
            .eqExpression("d.name").end().endAnd().endOr();
        String expected = "SELECT d FROM Document d WHERE d.name = :param_0 AND (NOT EXISTS (SELECT p.id FROM Person p WHERE p.name = d.name))";

        assertEquals(expected, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testWhereExistsOrBuilder() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.whereOr().where("d.name").eq("test").whereExists().from(Person.class, "p").select("id").where("name").eqExpression(
            "d.name").end().endOr();
        String expected = "SELECT d FROM Document d WHERE d.name = :param_0 OR EXISTS (SELECT p.id FROM Person p WHERE p.name = d.name)";

        assertEquals(expected, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testWhereNotExistsOrBuilder() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.whereOr().where("d.name").eq("test").whereNotExists().from(Person.class, "p").select("id").where("name")
            .eqExpression("d.name").end().endOr();
        String expected = "SELECT d FROM Document d WHERE d.name = :param_0 OR NOT EXISTS (SELECT p.id FROM Person p WHERE p.name = d.name)";

        assertEquals(expected, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testWhereLeftSubquery() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.whereSubquery().from(Person.class, "p").select("id").where("name").eqExpression("d.name").end().eqExpression("id");
        String expected = "SELECT d FROM Document d WHERE (SELECT p.id FROM Person p WHERE p.name = d.name) = d.id";

        assertEquals(expected, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testWhereLeftSubqueryAndBuilder() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.where("d.name").eq("test").whereOr().whereAnd().whereSubquery().from(Person.class, "p").select("id").where("name")
            .eqExpression("d.name").end().eqExpression("d.owner.id").endAnd().endOr();
        String expected = "SELECT d FROM Document d" + singleValuedAssociationIdJoin("d.owner", "owner_1", false) + " WHERE d.name = :param_0 AND ((SELECT p.id FROM Person p WHERE p.name = d.name) = " + singleValuedAssociationIdPath("d.owner.id", "owner_1") + ")";

        assertEquals(expected, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testWhereLeftSubqueryOrBuilder() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.whereOr().where("d.name").eq("test").whereSubquery().from(Person.class, "p").select("id").where("name")
            .eqExpression("d.name").end().eqExpression("d.owner.id").endOr();
        String expected = "SELECT d FROM Document d" + singleValuedAssociationIdJoin("d.owner", "owner_1", false) + " WHERE d.name = :param_0 OR (SELECT p.id FROM Person p WHERE p.name = d.name) = " + singleValuedAssociationIdPath("d.owner.id", "owner_1");

        assertEquals(expected, crit.getQueryString());
        crit.getResultList();
    }
    
    @Test
    public void testWhereSubqueryWithSurroundingExpression() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.whereSubquery("alias", "1 + alias").from(Person.class, "p").select("COUNT(id)").end().eq(2L);     
        
        assertEquals("SELECT d FROM Document d WHERE 1 + (SELECT COUNT(p.id) FROM Person p) = :param_0", crit.getQueryString());
        crit.getResultList();
    }
    

    @Test
    public void testWhereMultipleSubqueryWithSurroundingExpression() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.whereSubquery("alias", "alias * alias").from(Person.class, "p").select("COUNT(id)").end().eq(2L);     
        
        assertEquals("SELECT d FROM Document d WHERE (SELECT COUNT(p.id) FROM Person p) * (SELECT COUNT(p.id) FROM Person p) = :param_0", crit.getQueryString());
        crit.getResultList();
    }
    
    @Test
    public void testWhereAndSubqueryWithSurroundingExpression() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.where("d.name").eq("test").whereOr().whereAnd().whereSubquery("alias", "SQRT(alias)").from(Person.class, "p").select("id").where("name")
            .eqExpression("d.name").end().eqExpression("d.owner.id").endAnd().endOr();
        String expected = "SELECT d FROM Document d" + singleValuedAssociationIdJoin("d.owner", "owner_1", false) + " WHERE d.name = :param_0 AND (SQRT((SELECT p.id FROM Person p WHERE p.name = d.name)) = " + singleValuedAssociationIdPath("d.owner.id", "owner_1") + ")";

        assertEquals(expected, crit.getQueryString());
        crit.getResultList(); 
    }
    
    @Test
    public void testWhereAndMultipleSubqueryWithSurroundingExpression() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.where("d.name").eq("test").whereOr().whereAnd().whereSubquery("alias", "alias * alias").from(Person.class, "p").select("id").where("name")
            .eqExpression("d.name").end().eqExpression("d.owner.id").endAnd().endOr();
        String expected = "SELECT d FROM Document d" + singleValuedAssociationIdJoin("d.owner", "owner_1", false) + " WHERE d.name = :param_0 AND ((SELECT p.id FROM Person p WHERE p.name = d.name) * (SELECT p.id FROM Person p WHERE p.name = d.name) = " + singleValuedAssociationIdPath("d.owner.id", "owner_1")+ ")";

        assertEquals(expected, crit.getQueryString());
        crit.getResultList(); 
    }
    
    @Test
    public void testWhereOrSubqueryWithSurroundingExpression() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.whereOr().where("d.name").eq("test").whereSubquery("alias", "SQRT(alias)").from(Person.class, "p").select("id").where("name")
            .eqExpression("d.name").end().eqExpression("d.owner.id").endOr();
        String expected = "SELECT d FROM Document d" + singleValuedAssociationIdJoin("d.owner", "owner_1", false) + " WHERE d.name = :param_0 OR SQRT((SELECT p.id FROM Person p WHERE p.name = d.name)) = " + singleValuedAssociationIdPath("d.owner.id", "owner_1");

        assertEquals(expected, crit.getQueryString());
        crit.getResultList(); 
    }
    
    @Test
    public void testWhereOrMultipleSubqueryWithSurroundingExpression() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.whereOr().where("d.name").eq("test").whereSubquery("alias", "alias * alias").from(Person.class, "p").select("id").where("name")
            .eqExpression("d.name").end().eqExpression("d.owner.id").endOr();
        String expected = "SELECT d FROM Document d" + singleValuedAssociationIdJoin("d.owner", "owner_1", false) + " WHERE d.name = :param_0 OR (SELECT p.id FROM Person p WHERE p.name = d.name) * (SELECT p.id FROM Person p WHERE p.name = d.name) = " + singleValuedAssociationIdPath("d.owner.id", "owner_1");

        assertEquals(expected, crit.getQueryString());
        crit.getResultList(); 
    }
    
    @Test
    public void testWhereCase() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.whereCase().when("d.id").geExpression("d.age").thenExpression("2").otherwiseExpression("1").eqExpression("d.idx");
        String expected = "SELECT d FROM Document d WHERE CASE WHEN d.id >= d.age THEN 2 ELSE 1 END = d.idx";
        assertEquals(expected, crit.getQueryString());
        crit.getResultList(); 
    }
    
    @Test
    public void testWhereCaseBuilderNotEnded() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.whereCase();
        verifyBuilderChainingException(crit);
    }
    
    @Test
    public void testWhereSimpleCaseBuilderNotEnded() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.whereCase();
        verifyBuilderChainingException(crit);
    }
    
    @Test
    public void testWhereSimpleCase() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.whereSimpleCase("d.id").when("1", "d.age").otherwise("d.idx").eqExpression("d.idx");
        String expected = "SELECT d FROM Document d WHERE CASE d.id WHEN 1 THEN d.age ELSE d.idx END = d.idx";
        assertEquals(expected, crit.getQueryString());
        crit.getResultList(); 
    }
    
    @Test
    public void testWhereAndCase() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.whereOr().whereAnd().whereCase()
                .whenAnd().and("d.id").eqExpression("d.age").and("d.age").ltExpression("4").thenExpression("2")
                .when("d.id").eqExpression("4").thenExpression("4").otherwiseExpression("3").eqExpression("2").endAnd().endOr();
        String expected = "SELECT d FROM Document d WHERE CASE WHEN d.id = d.age AND d.age < 4 THEN 2 WHEN d.id = 4 THEN 4 ELSE 3 END = 2";
        assertEquals(expected, crit.getQueryString());
        crit.getResultList(); 
    }
    
    @Test
    public void testWhereAndSimpleCase() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.whereOr().whereAnd().whereSimpleCase("d.id")
                .when("d.age", "2")
                .when("4", "4").otherwise("3").eqExpression("2").endAnd().endOr();
        String expected = "SELECT d FROM Document d WHERE CASE d.id WHEN d.age THEN 2 WHEN 4 THEN 4 ELSE 3 END = 2";
        assertEquals(expected, crit.getQueryString());
        crit.getResultList(); 
    }
    
    @Test
    public void testWhereOrCase() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.whereOr().whereCase()
                .whenAnd().and("d.id").eqExpression("d.age").and("d.age").ltExpression("4").thenExpression("2")
                .when("d.id").eqExpression("4").thenExpression("4").otherwiseExpression("3").eqExpression("2").endOr();
        String expected = "SELECT d FROM Document d WHERE CASE WHEN d.id = d.age AND d.age < 4 THEN 2 WHEN d.id = 4 THEN 4 ELSE 3 END = 2";
        assertEquals(expected, crit.getQueryString());
        crit.getResultList(); 
    }
    
    @Test
    public void testWhereOrSimpleCase() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.whereOr().whereSimpleCase("d.id")
                .when("d.age", "2")
                .when("4", "4").otherwise("3").eqExpression("2").endOr();
        String expected = "SELECT d FROM Document d WHERE CASE d.id WHEN d.age THEN 2 WHEN 4 THEN 4 ELSE 3 END = 2";
        assertEquals(expected, crit.getQueryString());
        crit.getResultList(); 
    }
    
    @Test
    public void testWhereRightSideCase() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.where("d.id").lt().caseWhen("d.age").eqExpression("3").thenExpression("4").otherwiseExpression("1");
        String expected = "SELECT d FROM Document d WHERE d.id < CASE WHEN d.age = 3 THEN 4 ELSE 1 END";
        assertEquals(expected, crit.getQueryString());
        crit.getResultList(); 
    }
    
    @Test
    public void testWhereRightSideCaseAnd() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.where("d.id").lt().caseWhenAnd().and("d.id").eqExpression("3").and("d.age").ltExpression("3").thenExpression("4").otherwiseExpression("1");
        String expected = "SELECT d FROM Document d WHERE d.id < CASE WHEN d.id = 3 AND d.age < 3 THEN 4 ELSE 1 END";
        assertEquals(expected, crit.getQueryString());
        crit.getResultList(); 
    }
    
    @Test
    public void testWhereRightSideCaseOr() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.where("d.id").lt().caseWhenOr().or("d.id").eqExpression("3").or("d.age").ltExpression("3").thenExpression("4").otherwiseExpression("1");
        String expected = "SELECT d FROM Document d WHERE d.id < CASE WHEN d.id = 3 OR d.age < 3 THEN 4 ELSE 1 END";
        assertEquals(expected, crit.getQueryString());
        crit.getResultList(); 
    }
    
    @Test
    public void testWhereRightSideCaseExists() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.where("d.id").lt().caseWhenExists().from(Person.class).end().thenExpression("4").otherwiseExpression("1");
        String expected = "SELECT d FROM Document d WHERE d.id < CASE WHEN EXISTS (SELECT 1 FROM Person person) THEN 4 ELSE 1 END";
        assertEquals(expected, crit.getQueryString());
        crit.getResultList(); 
    }
    
    @Test
    public void testWhereRightSideCaseNotExists() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.where("d.id").lt().caseWhenNotExists().from(Person.class).end().thenExpression("4").otherwiseExpression("1");
        String expected = "SELECT d FROM Document d WHERE d.id < CASE WHEN NOT EXISTS (SELECT 1 FROM Person person) THEN 4 ELSE 1 END";
        assertEquals(expected, crit.getQueryString());
        crit.getResultList(); 
    }
    
    @Test
    public void testWhereRightSideCaseSubquery() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.where("d.id").lt().caseWhenSubquery().from(Person.class).select("COUNT(person.id)").end().geExpression("4").thenExpression("4").otherwiseExpression("1");
        String expected = "SELECT d FROM Document d WHERE d.id < CASE WHEN (SELECT COUNT(person.id) FROM Person person) >= 4 THEN 4 ELSE 1 END";
        assertEquals(expected, crit.getQueryString());
        crit.getResultList(); 
    }
    
    @Test
    public void testWhereRightSideCaseSubqueryExpression() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.where("d.id").lt().caseWhenSubquery("s", "s+1").from(Person.class).select("COUNT(person.id)").end().geExpression("4").thenExpression("4").otherwiseExpression("1");
        String expected = "SELECT d FROM Document d WHERE d.id < CASE WHEN (SELECT COUNT(person.id) FROM Person person) + 1 >= 4 THEN 4 ELSE 1 END";
        assertEquals(expected, crit.getQueryString());
        crit.getResultList(); 
    }
    
    @Test
    public void testWhereRightSideSimpleCase() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.where("d.id").lt().simpleCase("d.id").when("2", "1").when("4", "12").otherwise("10");
        String expected = "SELECT d FROM Document d WHERE d.id < CASE d.id WHEN 2 THEN 1 WHEN 4 THEN 12 ELSE 10 END";
        assertEquals(expected, crit.getQueryString());
        crit.getResultList(); 
    }

    // TODO: #188
    @Ignore("#188")
    @Test
    public void testWhereSizeSingle() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.select("d.id").select("d.name")
            .where("SIZE(d.versions)").gtExpression("2");
        
        final String expected = "SELECT d.id, d.name FROM Document d LEFT JOIN d.versions versions_1 GROUP BY d.id, d.name HAVING COUNT(versions_1) > 2";
        assertEquals(expected, crit.getQueryString());
    }

    // TODO: #188
    @Ignore("#188")
    @Test
    public void testWhereSizeMultiple() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.select("d.id").select("d.name")
            .where("SIZE(d.versions)").gtExpression("2")
            .where("SIZE(d.partners)").ltExpression("1");
        
        final String expected = "SELECT d.id, d.name FROM Document d LEFT JOIN d.partners partners_1 LEFT JOIN d.versions versions_1 GROUP BY d.id, d.name HAVING COUNT(DISTINCT versions_1) > 2 AND COUNT(DISTINCT partners_1) < 1";
        assertEquals(expected, crit.getQueryString());
    }
    
    private void verifyBuilderChainingException(CriteriaBuilder<Document> crit){
        verifyException(crit, BuilderChainingException.class).whereCase();
        verifyException(crit, BuilderChainingException.class).whereSimpleCase("d.id");
        verifyException(crit, BuilderChainingException.class).where("d.id");
        verifyException(crit, BuilderChainingException.class).getQueryString();
    }
}
