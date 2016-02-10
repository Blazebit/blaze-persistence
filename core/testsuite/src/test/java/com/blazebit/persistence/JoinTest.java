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

import static com.googlecode.catchexception.CatchException.verifyException;
import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.persistence.EntityTransaction;
import javax.persistence.Tuple;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.blazebit.persistence.entity.Document;
import com.blazebit.persistence.entity.Person;
import com.blazebit.persistence.testsuite.base.category.NoDB2;
import com.blazebit.persistence.testsuite.base.category.NoDatanucleus;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public class JoinTest extends AbstractCoreTest {

    final String defaultDocumentAlias = "document";

    @Test
    public void testGenerics() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d").where("owner.name").eq("test");
        assertEquals("SELECT d FROM Document d JOIN d.owner owner_1 WHERE owner_1.name = :param_0", criteria.getQueryString());
        criteria.getResultList();
    }
    
    @Test
    public void testDefaultAlias() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class);
        assertEquals("SELECT " + defaultDocumentAlias + " FROM Document " + defaultDocumentAlias, criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testRightJoinFetch() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.rightJoinFetch("owner", "o");
        criteria.rightJoinFetch("versions", "v");
        criteria.where("o.age").eq(0L);

        assertEquals("SELECT d FROM Document d RIGHT JOIN FETCH d.owner o RIGHT JOIN FETCH d.versions v WHERE o.age = :param_0",
                     criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testRightJoin() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.rightJoin("owner", "o");
        criteria.rightJoin("versions", "v");

        assertEquals("SELECT d FROM Document d RIGHT JOIN d.owner o RIGHT JOIN d.versions v", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testLeftJoinFetch() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.leftJoinFetch("owner", "o");
        criteria.leftJoinFetch("versions", "v");

        assertEquals("SELECT d FROM Document d LEFT JOIN FETCH d.owner o LEFT JOIN FETCH d.versions v", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testLeftJoin() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.leftJoin("owner", "o");
        criteria.leftJoin("versions", "v");

        assertEquals("SELECT d FROM Document d LEFT JOIN d.owner o LEFT JOIN d.versions v", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testInnerJoinFetch() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.innerJoinFetch("owner", "o");
        criteria.innerJoinFetch("versions", "v");

        assertEquals("SELECT d FROM Document d JOIN FETCH d.owner o JOIN FETCH d.versions v", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testInnerJoin() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.innerJoin("owner", "o");
        criteria.innerJoin("versions", "v");

        assertEquals("SELECT d FROM Document d JOIN d.owner o JOIN d.versions v", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testJoinMethodEquivalences() {
        final String qInnerJoin = cbf.create(em, Document.class, "d").join("owner", "o", JoinType.INNER, false).getQueryString();
        final String qInnerJoinFetch = cbf.create(em, Document.class, "d").join("owner", "o", JoinType.INNER, true)
            .getQueryString();
        final String qLeftJoin = cbf.create(em, Document.class, "d").join("owner", "o", JoinType.LEFT, false).getQueryString();
        final String qLeftJoinFetch = cbf.create(em, Document.class, "d").join("owner", "o", JoinType.LEFT, true).getQueryString();
        final String qRightJoin = cbf.create(em, Document.class, "d").join("owner", "o", JoinType.RIGHT, false).getQueryString();
        final String qRightJoinFetch = cbf.create(em, Document.class, "d").join("owner", "o", JoinType.RIGHT, true)
            .getQueryString();

        assertEquals(cbf.create(em, Document.class, "d").innerJoin("owner", "o").getQueryString(),
                     qInnerJoin);
        assertEquals(cbf.create(em, Document.class, "d").innerJoinFetch("owner", "o").getQueryString(),
                     qInnerJoinFetch);
        assertEquals(cbf.create(em, Document.class, "d").rightJoin("owner", "o").getQueryString(),
                     qRightJoin);
        assertEquals(cbf.create(em, Document.class, "d").rightJoinFetch("owner", "o").getQueryString(),
                     qRightJoinFetch);
        assertEquals(cbf.create(em, Document.class, "d").leftJoin("owner", "o").getQueryString(),
                     qLeftJoin);
        assertEquals(cbf.create(em, Document.class, "d").leftJoinFetch("owner", "o").getQueryString(),
                     qLeftJoinFetch);
    }

    @Test
    public void testNestedLeftJoinBeforeRightJoin() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.joinDefault("owner.ownedDocuments.versions", "cont", JoinType.LEFT, false);
        criteria.joinDefault("owner.ownedDocuments.versions.document", "contDoc", JoinType.RIGHT, true);
        criteria.joinDefault("owner", "o", JoinType.INNER, true);

        assertEquals(
            "SELECT d FROM Document d JOIN FETCH d.owner o LEFT JOIN FETCH o.ownedDocuments ownedDocuments_1 LEFT JOIN FETCH ownedDocuments_1.versions cont RIGHT JOIN FETCH cont.document contDoc",
            criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testNestedRightJoinBeforeLeftJoin() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.joinDefault("owner.ownedDocuments.versions", "cont", JoinType.RIGHT, false);
        criteria.joinDefault("owner.ownedDocuments.versions.document", "contDoc", JoinType.LEFT, true);
        criteria.joinDefault("owner", "o", JoinType.INNER, true);

        assertEquals(
            "SELECT d FROM Document d JOIN FETCH d.owner o LEFT JOIN FETCH o.ownedDocuments ownedDocuments_1 RIGHT JOIN FETCH ownedDocuments_1.versions cont LEFT JOIN FETCH cont.document contDoc",
            criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testNestedLeftJoinAfterRightJoin() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.joinDefault("owner.ownedDocuments.versions.document", "contDoc", JoinType.RIGHT, true);
        criteria.joinDefault("owner.ownedDocuments.versions", "cont", JoinType.LEFT, false);
        criteria.joinDefault("owner", "o", JoinType.INNER, true);

        assertEquals(
            "SELECT d FROM Document d JOIN FETCH d.owner o LEFT JOIN FETCH o.ownedDocuments ownedDocuments_1 LEFT JOIN FETCH ownedDocuments_1.versions cont RIGHT JOIN FETCH cont.document contDoc",
            criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testNestedRightJoinAfterLeftJoin() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.joinDefault("owner.ownedDocuments.versions.document", "contDoc", JoinType.LEFT, true);
        criteria.joinDefault("owner.ownedDocuments.versions", "cont", JoinType.RIGHT, false);
        criteria.joinDefault("owner", "o", JoinType.INNER, true);

        assertEquals(
            "SELECT d FROM Document d JOIN FETCH d.owner o LEFT JOIN FETCH o.ownedDocuments ownedDocuments_1 RIGHT JOIN FETCH ownedDocuments_1.versions cont LEFT JOIN FETCH cont.document contDoc",
            criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testConstructorClassNull() {
        verifyException(cbf, NullPointerException.class).create(em, null, "d");
    }

    @Test
    public void testConstructorEntityManagerNull() {
        verifyException(cbf, NullPointerException.class).create(null, Document.class, "d");
    }

    @Test
    public void testJoinNullPath() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class);
        verifyException(criteria, NullPointerException.class).join(null, "o", JoinType.LEFT, true);
    }

    @Test
    public void testJoinNullAlias() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class);
        verifyException(criteria, NullPointerException.class).join("owner", null, JoinType.LEFT, true);
    }

    @Test
    public void testJoinNullJoinType() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class);
        verifyException(criteria, NullPointerException.class).join("owner", "o", null, true);
    }

    @Test
    public void testJoinEmptyAlias() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class);
        verifyException(criteria, IllegalArgumentException.class).join("owner", "", JoinType.LEFT, true);
    }

    @Test
    public void testUnresolvedAlias1() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.where("z.c.x").eq(0).leftJoin("d.partners", "p");

        verifyException(criteria, IllegalArgumentException.class).getQueryString();
    }

    @Test
    public void testUnresolvedAlias2() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "a");
        criteria.where("z").eq(0);

        verifyException(criteria, IllegalArgumentException.class).getQueryString();
    }

    @Test
    public void testUnresolvedAliasInOrderBy() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "a");
        criteria.orderByAsc("z");

        verifyException(criteria, IllegalArgumentException.class).getQueryString();
    }

    @Test
    public void testImplicitRootRelativeAlias() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "a");
        criteria.where("versions.document.age").eq(0L).leftJoin("a.partners", "p");

        assertEquals(
            "SELECT a FROM Document a LEFT JOIN a.partners p LEFT JOIN a.versions versions_1 LEFT JOIN versions_1.document document_1 WHERE document_1.age = :param_0",
            criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testCallOrderInvariance() {
        CriteriaBuilder<Document> criteria1 = cbf.create(em, Document.class, "a");
        CriteriaBuilder<Document> criteria2 = cbf.create(em, Document.class, "a");

        criteria1.where("p.ownedDocuments.age").eq(0L).leftJoin("a.partners", "p");
        criteria2.leftJoin("a.partners", "p").where("p.ownedDocuments.age").eq(0L);

        assertEquals(
            "SELECT a FROM Document a LEFT JOIN a.partners p LEFT JOIN p.ownedDocuments ownedDocuments_1 WHERE ownedDocuments_1.age = :param_0",
            criteria1.getQueryString());
        assertEquals(
            "SELECT a FROM Document a LEFT JOIN a.partners p LEFT JOIN p.ownedDocuments ownedDocuments_1 WHERE ownedDocuments_1.age = :param_0",
            criteria2.getQueryString());
        criteria1.getResultList();
        criteria2.getResultList();
    }

    @Test
    public void testFetchJoinCheck1() {
        CriteriaBuilder<Tuple> crit = cbf.create(em, Tuple.class).from(Document.class, "d")
            .select("name");
        verifyException(crit, IllegalStateException.class).join("d.versions", "versions", JoinType.LEFT, true);
    }
    
    @Test
    public void testFetchJoinCheck2() {
        CriteriaBuilder<Tuple> crit = cbf.create(em, Tuple.class).from(Document.class, "d")
            .select("name");
        verifyException(crit, IllegalStateException.class).fetch("d.versions");
    }

    @Test
    public void testModelAwareJoin() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "a");
        crit.select("owner.name");

        assertEquals("SELECT owner_1.name FROM Document a JOIN a.owner owner_1", crit.getQueryString());
        crit.getResultList();
    }
    
    @Test
    public void testFetch() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "a");
        crit.fetch("owner.name");

        assertEquals("SELECT a FROM Document a JOIN FETCH a.owner owner_1", crit.getQueryString());
        crit.getResultList();
    }
    
    @Test
    public void testFetchAmbiguousImplicitAlias() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "a");
        crit.fetch("owner.partnerDocument.owner");

        assertEquals("SELECT a FROM Document a JOIN FETCH a.owner owner_1 LEFT JOIN FETCH owner_1.partnerDocument partnerDocument_1 LEFT JOIN FETCH partnerDocument_1.owner owner_2", crit.getQueryString());
        crit.getResultList();
    }
    
    @Test
    public void testImplicitJoinNodeReuse() {
        CriteriaBuilder<String> crit = cbf.create(em, String.class);
        crit.from(Document.class, "d");
        crit.select("d.intIdEntity.name");
        crit.where("d.intIdEntity").isNotNull();

        assertEquals("SELECT intIdEntity_1.name FROM Document d LEFT JOIN d.intIdEntity intIdEntity_1 WHERE intIdEntity_1 IS NOT NULL", crit.getQueryString());
        crit.getResultList();
    }
    
    @Test
    public void testCyclicJoinDependencyDetection(){
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d")
            .leftJoinOn("owner", "o1").on("o1.name").eqExpression("o2.name").end()
            .leftJoinOn("owner", "o2").on("o2.name").eqExpression("o1.name").end();
        verifyException(crit, IllegalStateException.class).getQueryString();
    }
    
    @Test
    public void testLeftJoinChildRelationsOnLeftJoin(){
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d")
            .leftJoin("partners", "p").where("p.partnerDocument.owner.name").eqExpression("'John'");
        assertEquals("SELECT d FROM Document d LEFT JOIN d.partners p LEFT JOIN p.partnerDocument partnerDocument_1 LEFT JOIN partnerDocument_1.owner owner_1 WHERE owner_1.name = 'John'", crit.getQueryString());
    }
    
    @Test
    public void testPaginatedJoinFetch(){
        PaginatedCriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d")
                .leftJoinFetch("contacts", "c")
                .orderByAsc("d.id")
                .page(0, 10);
        
        assertEquals("SELECT d FROM Document d LEFT JOIN FETCH d.contacts c WHERE d.id IN :ids ORDER BY " + renderNullPrecedence("d.id", "ASC", "LAST"), crit.getQueryString());
    }
    
    // NOTE: DB2 9.7 which is what we've got on Travis CI does not support subqueries in the on clause. See http://www-01.ibm.com/support/knowledgecenter/SSEPGG_9.7.0/com.ibm.db2.luw.messages.sql.doc/doc/msql00338n.html?cp=SSEPGG_9.7.0
    // TODO: Datanucleus does not seem to support subqueries in the on clause, the query parser complains about the FROM keyword
    @Test
    @Category({ NoDB2.class, NoDatanucleus.class })
    public void testSizeInOnClause() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d")
            .leftJoinOn("d.partners", "p").on("SIZE(d.versions)").gtExpression("2").end();
        
        final String expected = "SELECT d FROM Document d LEFT JOIN d.partners p " + ON_CLAUSE + " (SELECT COUNT(versions) FROM Document document LEFT JOIN document.versions versions WHERE document = d) > 2";
        assertEquals(expected, crit.getQueryString());
        crit.getResultList();
    }
    
    @Test
    public void testSizeNoExplicitJoinReusal() {
        // Given
        EntityTransaction tx = em.getTransaction();
        try{
            tx.begin();
            Document d = new Document("D1");

            Person p1 = new Person("Joe");
            Person p2 = new Person("Fred");
            p2.setPartnerDocument(d);
            d.setOwner(p1);
            
            em.persist(p1);
            em.persist(d);
            em.persist(p2);
            
            p1.setPartnerDocument(d);
            em.merge(p1);
            
            tx.commit();
        } catch(Throwable t) {
            t.printStackTrace();
            tx.rollback();
        }
        
        // When
        CriteriaBuilder<Long> crit = cbf.create(em, Long.class)
            .from(Document.class, "d")
            .leftJoin("d.partners", "partner")
            .select("SIZE(d.partners)")
            .where("partner.name").eqExpression("'Joe'");
    
        // Then
        final String expected = "SELECT COUNT(DISTINCT partners_1) FROM Document d LEFT JOIN d.partners partner LEFT JOIN d.partners partners_1 WHERE partner.name = 'Joe' GROUP BY d.id";
        assertEquals(expected, crit.getQueryString());
        List<Long> results = crit.getResultList();
        assertEquals(1, results.size());
        assertEquals(Long.valueOf(2l), results.get(0));
    }
}
