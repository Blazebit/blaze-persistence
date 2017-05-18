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

import static com.googlecode.catchexception.CatchException.caughtException;
import static com.googlecode.catchexception.CatchException.verifyException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;

import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate42;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate43;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate50;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate51;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOpenJPA;
import com.blazebit.persistence.testsuite.entity.DocumentForEntityKeyMaps;
import com.blazebit.persistence.testsuite.entity.PersonForEntityKeyMaps;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.JoinType;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDB2;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class JoinTest extends AbstractCoreTest {

    final String defaultDocumentAlias = "document";

    @Override
    protected Class<?>[] getEntityClasses() {
        return concat(super.getEntityClasses(), new Class<?>[] {
                DocumentForEntityKeyMaps.class,
                PersonForEntityKeyMaps.class
        });
    }

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
    @Category(NoEclipselink.class)
    // Eclipselink does not support right joins
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
    @Category(NoEclipselink.class)
    // Eclipselink does not support right joins
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
    @Category(NoEclipselink.class)
    // Eclipselink does not support right joins
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
    @Category(NoEclipselink.class)
    // Eclipselink does not support right joins
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
    @Category(NoEclipselink.class)
    // Eclipselink does not support right joins
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
    @Category(NoEclipselink.class)
    // Eclipselink does not support right joins
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
        crit.join("d.versions", "versions", JoinType.LEFT, true);
        verifyException(crit, IllegalStateException.class).getQueryString();
        String message = caughtException().getMessage();
        assertTrue(message.contains("Missing fetch owners: [d]"));
    }
    
    @Test
    public void testFetchJoinCheck2() {
        CriteriaBuilder<Tuple> crit = cbf.create(em, Tuple.class).from(Document.class, "d")
            .select("name");
        crit.fetch("d.versions");
        verifyException(crit, IllegalStateException.class).getQueryString();
        String message = caughtException().getMessage();
        assertTrue(message.contains("Missing fetch owners: [d]"));
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
    public void selectWithFetchSimpleRelation() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "a");
        crit.select("owner");
        crit.fetch("owner.partnerDocument.owner");

        assertEquals("SELECT owner_1 FROM Document a JOIN a.owner owner_1 LEFT JOIN FETCH owner_1.partnerDocument partnerDocument_1 LEFT JOIN FETCH partnerDocument_1.owner owner_2", crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void selectWithFetchMultipleRelations() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "a");
        crit.select("owner");
        crit.fetch("owner.partnerDocument.owner", "owner.ownedDocuments");

        assertEquals("SELECT owner_1 FROM Document a JOIN a.owner owner_1 LEFT JOIN FETCH owner_1.ownedDocuments ownedDocuments_1 LEFT JOIN FETCH owner_1.partnerDocument partnerDocument_1 LEFT JOIN FETCH partnerDocument_1.owner owner_2", crit.getQueryString());
        crit.getResultList();
    }

    @Test
    // Older Hibernate versions don't like fetch joining an element collection at all: https://hibernate.atlassian.net/browse/HHH-11140
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoHibernate51.class })
    public void selectWithFetchElementCollectionOnly() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "a");
        crit.select("owner");
        crit.fetch("owner.localized");

        assertEquals("SELECT owner_1 FROM Document a JOIN a.owner owner_1 LEFT JOIN FETCH owner_1.localized localized_1", crit.getQueryString());
        crit.getResultList();
    }

    @Test
    // But fetching the element collection together with other properties is still problematic
    @Category({ NoHibernate.class })
    public void selectWithFetchElementCollectionAndOtherRelations() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "a");
        crit.select("owner");
        crit.fetch("owner.partnerDocument.owner", "owner.localized");

        assertEquals("SELECT owner_1 FROM Document a JOIN a.owner owner_1 LEFT JOIN FETCH owner_1.localized localized_1 LEFT JOIN FETCH owner_1.partnerDocument partnerDocument_1 LEFT JOIN FETCH partnerDocument_1.owner owner_2", crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void selectWithFetchNonExistingSubRelation() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "a");
        crit.select("owner");
        verifyException(crit, IllegalArgumentException.class).fetch("owner.intIdEntity");
    }

    @Test
    public void selectNonFetchOwnerWithFetching() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "a");
        crit.select("owner.id");
        crit.fetch("owner");
        verifyException(crit, IllegalStateException.class).getQueryString();
        String message = caughtException().getMessage();
        assertTrue(message.contains("Missing fetch owners: [a]"));
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
        IllegalStateException e = caughtException();
        assertTrue(e.getMessage().contains("Cyclic"));
    }
    
    @Test
    public void testMultipleDependenciesOnJoinDoesNotThrowCyclicJoinException() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d")
                .innerJoinOn("d.owner", "o1").on("o1.id").eqExpression("d.id").end()
                .innerJoinOn("d.partners", "p2")
                    .onOr()
                        .on("p2.id").eqExpression("o1.id")
                        .on("p2.id").eqExpression("d.id")
                    .endOr()
                .end()
                .innerJoinOn("d.people", "p1").on("p1.id").eqExpression("p2.id").end();

        crit.getQueryString();
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
        
        assertEquals("SELECT d FROM Document d LEFT JOIN FETCH d.contacts c WHERE d.id IN :ids ORDER BY d.id ASC", crit.getQueryString());
    }
    
    // NOTE: DB2 9.7 which is what we've got on Travis CI does not support subqueries in the on clause. See http://www-01.ibm.com/support/knowledgecenter/SSEPGG_9.7.0/com.ibm.db2.luw.messages.sql.doc/doc/msql00338n.html?cp=SSEPGG_9.7.0
    @Test
    @Category({ NoDB2.class })
    public void testSizeInOnClause() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d")
            .leftJoinOn("d.partners", "p").on("SIZE(d.versions)").gtExpression("2").end();
        
        final String expected = "SELECT d FROM Document d LEFT JOIN d.partners p"
                + onClause("(SELECT " + countStar() + " FROM d.versions version) > 2");
        assertEquals(expected, crit.getQueryString());
        crit.getResultList();
    }
    
    @Test
    public void testSizeNoExplicitJoinReusal() {
        // Given
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
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
            }
        });

        // When
        CriteriaBuilder<Long> crit = cbf.create(em, Long.class)
            .from(Document.class, "d")
            .leftJoin("d.partners", "partner")
            .select("SIZE(d.partners)")
            .where("partner.name").eqExpression("'Joe'");
    
        // Then
        final String expected = "SELECT " + function("COUNT_TUPLE", "'DISTINCT'", "partners_1.id") + " FROM Document d LEFT JOIN d.partners partner LEFT JOIN d.partners partners_1 WHERE partner.name = 'Joe' GROUP BY d.id";
        assertEquals(expected, crit.getQueryString());
        List<Long> results = crit.getResultList();
        assertEquals(1, results.size());
        assertEquals(Long.valueOf(2l), results.get(0));
    }

    @Test
    // Only hibernate supports single valued association id expressions without needing to join
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testEntityJoinEmulationReverseJoinDependencyBug() {
        // this test is only relevant if entity join emulation is performed
        org.junit.Assume.assumeTrue(!jpaProvider.supportsEntityJoin());
        CriteriaBuilder<Long> crit = cbf.create(em, Long.class)
                .from(Document.class, "d")
                .innerJoinOn(Person.class, "p").on("p.partnerDocument.id").eqExpression("d.id").end()
                .innerJoinOn("p.favoriteDocuments", "favoriteDocument").on("favoriteDocument.idx").eqExpression("p.id").end()
                .select("p.name");

        final String expected = "SELECT p.name FROM Document d, Person p JOIN p.favoriteDocuments favoriteDocument"
                + onClause("favoriteDocument.idx = p.id")
                + " WHERE p.partnerDocument.id = d.id";
        assertEquals(expected, crit.getQueryString());
    }

    @Test
    public void testJoinMapKey(){
        CriteriaBuilder<DocumentForEntityKeyMaps> crit = cbf.create(em, DocumentForEntityKeyMaps.class, "d")
                .leftJoin("contactDocuments", "contact")
                .leftJoin("KEY(contact)", "person")
                .select("person.id");
        // Assert that the key join is not rendered through when using normal joins
        final String expected = "SELECT KEY(contact).id FROM DocumentForEntityKeyMaps d LEFT JOIN d.contactDocuments contact";
        assertEquals(expected, crit.getQueryString());
    }

    @Test
    public void testFetchJoinMapKey(){
        CriteriaBuilder<DocumentForEntityKeyMaps> crit = cbf.create(em, DocumentForEntityKeyMaps.class, "d")
                .leftJoinFetch("contactDocuments", "contact")
                .leftJoinFetch("KEY(contact)", "person");
        // Assert that the key join is only rendered through if fetching is used
        final String expected = "SELECT d FROM DocumentForEntityKeyMaps d LEFT JOIN FETCH d.contactDocuments contact LEFT JOIN FETCH KEY(contact) person";
        assertEquals(expected, crit.getQueryString());
    }

    @Test
    public void testJoinMapKeyRelation(){
        CriteriaBuilder<DocumentForEntityKeyMaps> crit = cbf.create(em, DocumentForEntityKeyMaps.class, "d")
                .leftJoin("contactDocuments", "contact")
                .leftJoin("KEY(contact).someDocument", "someDoc")
                .select("someDoc.id");
        // Assert that a join on a key's relation is rendered through
        final String expected = "SELECT someDoc.id FROM DocumentForEntityKeyMaps d LEFT JOIN d.contactDocuments contact LEFT JOIN KEY(contact).someDocument someDoc";
        assertEquals(expected, crit.getQueryString());
    }

    @Test
    public void testJoinMapKeyRelationOnAlias(){
        CriteriaBuilder<DocumentForEntityKeyMaps> crit = cbf.create(em, DocumentForEntityKeyMaps.class, "d")
                .leftJoin("contactDocuments", "contact")
                .leftJoin("KEY(contact)", "person")
                .leftJoin("person.someDocument", "someDoc")
                .select("someDoc.id");
        // Assert that a join on a key's relation is rendered through, but not the key join itself
        final String expected = "SELECT someDoc.id FROM DocumentForEntityKeyMaps d LEFT JOIN d.contactDocuments contact LEFT JOIN KEY(contact).someDocument someDoc";
        assertEquals(expected, crit.getQueryString());
    }

    @Test
    public void testImplicitJoinAttributeWithEqualJoinBaseAlias(){
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "name")
                .select("name.name");
        final String expected = "SELECT name.name FROM Document name";
        assertEquals(expected, crit.getQueryString());
    }
}
