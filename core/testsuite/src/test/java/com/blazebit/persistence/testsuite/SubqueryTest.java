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
import static org.junit.Assert.assertTrue;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;

import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlMacro;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOpenJPA;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.junit.Test;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.impl.BuilderChainingException;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.entity.Version;
import org.junit.experimental.categories.Category;

import java.util.List;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class SubqueryTest extends AbstractCoreTest {

    @Override
    public void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                Document doc1 = new Document("doc1");
                Document doc2 = new Document("Doc2");
                Document doc3 = new Document("doC3");
                Document doc4 = new Document("dOc4");
                Document doc5 = new Document("DOC5");
                Document doc6 = new Document("bdoc");
                Document doc7 = new Document("adoc");

                Person o1 = new Person("Karl1");
                Person o2 = new Person("Karl2");
                o1.getLocalized().put(1, "abra kadabra");
                o2.getLocalized().put(1, "ass");

                doc1.setOwner(o1);
                doc2.setOwner(o1);
                doc3.setOwner(o1);
                doc4.setOwner(o2);
                doc5.setOwner(o2);
                doc6.setOwner(o2);
                doc7.setOwner(o2);

                doc1.getContacts().put(1, o1);
                doc1.getContacts().put(2, o2);

                em.persist(o1);
                em.persist(o2);

                em.persist(doc1);
                em.persist(doc2);
                em.persist(doc3);
                em.persist(doc4);
                em.persist(doc5);
                em.persist(doc6);
                em.persist(doc7);
            }
        });
    }

    @Test
    public void testNotEndedBuilder() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.selectSubquery("subquery", "MAX(subquery)");
        
        verifyException(crit, BuilderChainingException.class).getResultList();
    }

    @Test
    public void testRootAliasInSubquery() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.where("id").in().from(Person.class).select("id").leftJoin("ownedDocuments", "subDoc").where("subDoc").eqExpression("d").end().getQueryString();
        String expected = "SELECT d FROM Document d WHERE d.id IN (SELECT person.id FROM Person person LEFT JOIN person.ownedDocuments subDoc WHERE "
                + "subDoc = d)";

        assertEquals(expected, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testWrongAliasUsageSubquery() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.where("id").notIn()
                .from(Document.class, "d2")
                .select("d2.versions.document.owner.id")
                .where("id").notEqExpression("d.id")
                .end();

        String expected = "SELECT d FROM Document d WHERE d.id NOT IN "
                + "(SELECT " + singleValuedAssociationIdPath("document_1.owner.id", "owner_1") + " FROM Document d2 LEFT JOIN d2.versions versions_1 LEFT JOIN versions_1.document document_1" + singleValuedAssociationIdJoin("document_1.owner", "owner_1", true) + " WHERE d2.id <> d.id)";

        assertEquals(expected, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testAmbiguousSelectAliases() {
        // we decided that this should not throw an exception
        // - we first check for existing aliases and if none exist we check if an implicit root alias is possible
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.select("name", "name").where("id").in().from(Person.class).select("id").where("name").eqExpression("name").end()
                .getQueryString();
        String expected = "SELECT d.name AS name FROM Document d WHERE d.id IN "
                + "(SELECT person.id FROM Person person WHERE d.name = d.name)";

        assertEquals(expected, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testSelectAliases() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.select("name", "n").where("id")
                .in().from(Person.class).select("id").where("d.name").eqExpression("name")
                .end()
                .getQueryString();
        String expected = "SELECT d.name AS n FROM Document d WHERE d.id IN "
                + "(SELECT person.id FROM Person person WHERE d.name = person.name)";

        assertEquals(expected, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testMultipleSubqueriesWithSelectAliases() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.select("name", "n")
                .where("id")
                .in().from(Person.class).select("id").where("name").eqExpression("d.name").end()
                .where("id")
                .notIn().from(Person.class).select("id").where("d.name").like().expression("name").noEscape().end()
                .orderByAsc("n")
                .getQueryString();
        String expected = "SELECT d.name AS n FROM Document d WHERE d.id IN "
                + "(SELECT person.id FROM Person person WHERE person.name = d.name) AND d.id NOT IN "
                + "(SELECT person.id FROM Person person WHERE d.name LIKE person.name) "
                + "ORDER BY n ASC";

        assertEquals(expected, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testMultipleSubqueriesWithJoinAliases() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.select("name", "n")
                .leftJoin("versions", "v")
                .where("id")
                .in().from(Person.class, "p").select("id").where("d.age").eqExpression("SIZE(p.ownedDocuments)").end()
                .where("id")
                .notIn().from(Person.class).select("id").where("d.age").ltExpression("SIZE(partnerDocument.versions)").end()
                .getQueryString();

        String expected = "SELECT d.name AS n FROM Document d LEFT JOIN d.versions v WHERE d.id IN "
                + "(SELECT p.id FROM Person p WHERE d.age = SIZE(p.ownedDocuments)) AND d.id NOT IN "
                + "(SELECT person.id FROM Person person LEFT JOIN person.partnerDocument partnerDocument_1 "
                + "WHERE d.age < SIZE(partnerDocument_1.versions))";

        assertEquals(expected, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testImplicitAliasPostfixing() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.select("name", "n")
                .where("SIZE(d.versions)").lt(5)
                .where("id")
                .in().from(Document.class, "document").select("id").where("SIZE(document.versions)").eqExpression(
                        "SIZE(document.partners)").end()
                .getQueryString();

        String expected = "SELECT d.name AS n FROM Document d WHERE SIZE(d.versions) < :param_0 AND d.id IN "
                + "(SELECT document.id FROM Document document "
                + "WHERE SIZE(document.versions) = SIZE(document.partners))";

        assertEquals(expected, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testImplicitRootAliasPostfixing() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "document");
        crit.select("name", "n")
                .leftJoinDefault("versions", "v")
                .where("SIZE(document.versions)").lt(5)
                .where("id")
                .in().from(Document.class).select("id").where("name").eq("name1").end()
                .getQueryString();

        String expected = "SELECT document.name AS n FROM Document document LEFT JOIN document.versions v WHERE SIZE(document.versions) < :param_0 AND document.id IN "
                + "(SELECT document_1.id FROM Document document_1 WHERE document_1.name = :param_1)";

        assertEquals(expected, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testSubqueryWithoutSelect() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d")
                .where("owner").in()
                .from(Person.class)
                .where("partnerDocument").eqExpression("d")
                .end();
        String expectedQuery = "SELECT d FROM Document d WHERE d.owner IN (SELECT person FROM Person person WHERE person.partnerDocument = d)";
        assertEquals(expectedQuery, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testSubqueryCorrelatesSimple() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d")
                .where("owner").in()
                .from("d.people")
                .where("partnerDocument").eqExpression("d")
                .end();
        String expectedQuery = "SELECT d FROM Document d WHERE d.owner IN (SELECT person FROM d.people person WHERE person.partnerDocument = d)";
        assertEquals(expectedQuery, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    // Test for #421
    public void testSubqueryCorrelatesOuter() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d")
                .where("owner").in()
                .from("OUTER(people)")
                .where("partnerDocument").eqExpression("d")
                .end();
        String expectedQuery = "SELECT d FROM Document d WHERE d.owner IN (SELECT person FROM d.people person WHERE person.partnerDocument = d)";
        assertEquals(expectedQuery, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    // Test for #421
    public void testSubqueryCorrelatesMacro() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.registerMacro("test", new JpqlMacro() {
            @Override
            public void render(FunctionRenderContext context) {
                context.addChunk("d.");
                context.addArgument(0);
            }
        });
        crit.where("owner").in()
                .from("TEST(people)")
                .where("partnerDocument").eqExpression("d")
                .end();
        String expectedQuery = "SELECT d FROM Document d WHERE d.owner IN (SELECT person FROM d.people person WHERE person.partnerDocument = d)";
        assertEquals(expectedQuery, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testSubqueryCorrelatesOverRelation() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d")
                .whereExists()
                    .from("d.owner.ownedDocuments", "dSub")
                    .where("dSub").notEqExpression("d")
                .end();
        String expectedQuery = "SELECT d FROM Document d JOIN d.owner owner_1 WHERE EXISTS (SELECT 1 FROM owner_1.ownedDocuments dSub WHERE dSub <> d)";
        assertEquals(expectedQuery, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    // TODO: Report datanucleus issue
    @Category({ NoDatanucleus.class })
    public void testMultipleCorrelationsWithJoins() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d")
                .where("owner").in()
                .from("d.people", "person")
                .from("d.partners", "partner")
                .from("d.contacts", "contact")
                .select("contact")
                .leftJoin("contact.partnerDocument", "contactDoc")
                .leftJoin("partner.partnerDocument", "partnerDoc")
                .leftJoin("person.partnerDocument", "personDoc")
                .where("contactDoc").eqExpression("d")
                .end();
        String expectedQuery = "SELECT d FROM Document d WHERE d.owner IN (SELECT contact " +
                "FROM d.people person " +
                "LEFT JOIN person.partnerDocument personDoc, " +
                "d.partners partner " +
                "LEFT JOIN partner.partnerDocument partnerDoc, " +
                "d.contacts contact " +
                "LEFT JOIN contact.partnerDocument contactDoc " +
                "WHERE contactDoc = d)";
        assertEquals(expectedQuery, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testMultipleSubqueriesWithParameters() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.select("name", "n")
                .where("id")
                .in().from(Person.class).select("id").where("d.name").eq("name").end()
                .where("id")
                .notIn().from(Person.class).select("id").where("d.name").like().value("test").noEscape().end()
                .where("SIZE(d.versions)").lt(5)
                .getQueryString();
        String expected = "SELECT d.name AS n FROM Document d WHERE d.id IN (SELECT person.id FROM Person person "
                + "WHERE d.name = :param_0) AND d.id NOT IN (SELECT person.id FROM Person person WHERE d.name LIKE :param_1) AND SIZE(d.versions) < :param_2";

        assertEquals(expected, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testSubqueryCollectionAccessUsesJoin() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.leftJoin("d.partners.localized", "l").whereSubquery()
                .from(Person.class, "p").select("name").where("LENGTH(l)").gt(1).end()
                .like().value("%dld").noEscape();
        String expectedQuery = "SELECT d FROM Document d LEFT JOIN d.partners partners_1 LEFT JOIN partners_1.localized l WHERE (SELECT p.name FROM Person p "
                + "WHERE LENGTH(" + joinAliasValue("l") + ") > :param_0) LIKE :param_1";
        assertEquals(expectedQuery, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testSubqueryCollectionAccessAddsJoin() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.whereSubquery()
                .from(Person.class, "p").select("name").where("LENGTH(d.partners.localized[1])").gt(1).end()
                .like().value("%dld").noEscape();
        String expectedQuery = "SELECT d FROM Document d LEFT JOIN d.partners partners_1 LEFT JOIN partners_1.localized localized_1_1"
                + onClause("KEY(localized_1_1) = 1")
                + " WHERE (SELECT p.name FROM Person p WHERE LENGTH(" + joinAliasValue("localized_1_1") + ") > :param_0) LIKE :param_1";
        assertEquals(expectedQuery, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testSubqueryUsesOuterJoin() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .leftJoin("d.contacts", "c")
                .select("id")
                .selectSubquery("alias", "ABS(alias)", "localizedCount")
                .from(Person.class, "p")
                .select("COUNT(p.localized)")
                .where("p.id").eqExpression("c.id")
                .end()
                .groupBy("id")
                .orderByAsc("localizedCount");
        
        String expectedSubQuery = "ABS((SELECT COUNT(" + joinAliasValue("localized_1") + ") FROM Person p LEFT JOIN p.localized localized_1 WHERE p.id = " + joinAliasValue("c", "id") + "))";
        String expectedQuery = "SELECT d.id, " + expectedSubQuery + " AS localizedCount "
                + "FROM Document d LEFT JOIN d.contacts c GROUP BY d.id, " + joinAliasValue("c", "id") + " ORDER BY localizedCount ASC";
        assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList(); 
    }

    @Test
    public void testSubqueryAddsJoin() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("id")
                .selectSubquery("alias", "ABS(alias)", "localizedCount")
                .from(Person.class, "p")
                .select("COUNT(p.localized)")
                .where("p.id").eqExpression("d.contacts.id")
                .end()
                .groupBy("id")
                .orderByAsc("localizedCount");

        String expectedSubQuery = "ABS((SELECT COUNT(" + joinAliasValue("localized_1") + ") FROM Person p LEFT JOIN p.localized localized_1 WHERE p.id = " + joinAliasValue("contacts_1", "id") + "))";
        String expectedQuery = "SELECT d.id, " + expectedSubQuery + " AS localizedCount "
                + "FROM Document d LEFT JOIN d.contacts contacts_1 GROUP BY d.id, " + joinAliasValue("contacts_1", "id") + " ORDER BY localizedCount ASC";
        assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }

    @Test
    public void testSubqueryCollectionAccess() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.whereSubquery()
                .from(Person.class, "p").select("name").where("LENGTH(d.partners.localized[1])").gt(1).end()
                .like().value("%dld").noEscape();

        String expectedQuery = "SELECT d FROM Document d LEFT JOIN d.partners partners_1 LEFT JOIN partners_1.localized localized_1_1"
                + onClause("KEY(localized_1_1) = 1")
                + " WHERE (SELECT p.name FROM Person p WHERE LENGTH(" + joinAliasValue("localized_1_1") + ") > :param_0) LIKE :param_1";
        assertEquals(expectedQuery, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testMultipleJoinPathSubqueryCollectionAccess() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.leftJoin("d.partners.localized", "l").whereSubquery()
                .from(Person.class, "p").select("name").where("LENGTH(d.partners.localized[1])").gt(1).end()
                .like().value("%dld").noEscape();

        String expectedQuery = "SELECT d FROM Document d LEFT JOIN d.partners partners_1 LEFT JOIN partners_1.localized l "
                + "LEFT JOIN partners_1.localized localized_1_1"
                + onClause("KEY(localized_1_1) = 1") +
                " WHERE (SELECT p.name FROM Person p "
                + "WHERE LENGTH(" + joinAliasValue("localized_1_1") + ") > :param_0) LIKE :param_1";
        assertEquals(expectedQuery, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testJoinElementCollectionsOnCorrelatedInverseAssociations() {
        CriteriaBuilder<Integer> crit = cbf.create(em, Integer.class)
                .from(Document.class, "d")
                .select("1");
        crit.whereExists()
                .from("d.partners", "p")
                .where("LENGTH(localized[1])").gt(1)
        .end();

        String expectedQuery = "SELECT 1 FROM Document d"
                + " WHERE EXISTS (SELECT 1 FROM Person p LEFT JOIN p.localized localized_1_1"
                + onClause("KEY(localized_1_1) = 1")
                + " WHERE p.partnerDocument.id = d.id AND LENGTH(" + joinAliasValue("localized_1_1") + ") > :param_0)";
        assertEquals(expectedQuery, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testInvalidSubqueryOrderByCollectionAccess() {
        PaginatedCriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .leftJoin("d.contacts", "c")
                .selectSubquery("localizedCount")
                .from(Person.class, "p")
                .select("COUNT(p.localized)")
                .where("p.id").eqExpression("c.id")
                .end()
                .orderByAsc("localizedCount")
                .orderByAsc("id")
                .page(0, 1);
        // In a paginated query access to outer collections is disallowed in the order by clause
        verifyException(cb, IllegalStateException.class).getPageIdQueryString();
    }

    @Test
    public void testInvalidSubqueryOrderByCollectionAccessNewJoin() {
        PaginatedCriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .selectSubquery("localizedCount")
                .from(Person.class, "p")
                .select("COUNT(p.localized)")
                .where("p.id").eqExpression("d.contacts.id")
                .end()
                .orderByAsc("localizedCount")
                .orderByAsc("id")
                .page(0, 1);
        // In a paginated query access to outer collections is disallowed in the order by clause
        verifyException(cb, IllegalStateException.class).getPageIdQueryString();
    }

    @Test
    public void testEquallyAliasedSingleValuedAssociationSelectInSubqueryAsInParentQuery() {
        PaginatedCriteriaBuilder<Document> pcb = cbf.create(em, Document.class)
                .where("owner.id").eq(1L)
                .where("id").notIn()
                    .from(Document.class, "c2")
                    .select("versions.document.id") // document is the same alias as the root entity alias
                    .where("id").eq(1L)
                .end()
                .orderByAsc("id")
                .page(0, 10);
        
        String expectedCountQuery = "SELECT " + countPaginated("document.id", false) + " FROM Document document" + singleValuedAssociationIdJoin("document.owner", "owner_1", false) + " WHERE " + singleValuedAssociationIdPath("document.owner.id", "owner_1") + " = :param_0 AND document.id NOT IN (SELECT " + singleValuedAssociationIdPath("versions_1.document.id", "document_1") + " FROM Document c2 LEFT JOIN c2.versions versions_1" + singleValuedAssociationIdJoin("versions_1.document", "document_1", true) + " WHERE c2.id = :param_1)";
        String expectedObjectQuery = "SELECT document FROM Document document" + singleValuedAssociationIdJoin("document.owner", "owner_1", false) + " WHERE " + singleValuedAssociationIdPath("document.owner.id", "owner_1") + " = :param_0 AND document.id NOT IN (SELECT " + singleValuedAssociationIdPath("versions_1.document.id", "document_1") + " FROM Document c2 LEFT JOIN c2.versions versions_1" + singleValuedAssociationIdJoin("versions_1.document", "document_1", true) + " WHERE c2.id = :param_1) ORDER BY document.id ASC";
        assertEquals(expectedCountQuery, pcb.getPageCountQueryString());
        assertEquals(expectedObjectQuery, pcb.getQueryString());
        pcb.getResultList();
    }
    
    @Test
    public void testRequirementForFullyQualifyingSubqueryAlias() {
        PaginatedCriteriaBuilder<Document> pcb = cbf.create(em, Document.class)
                .selectSubquery()
                    .from(Version.class)
                    .select("COUNT(id)")
                    .where("version.document.id").eqExpression("OUTER(id)") // we have to fully qualify version.document.id
                .end().orderByAsc("id").page(0, 10);
        
        String expectedCountQuery = "SELECT " + countPaginated("document.id", false) + " FROM Document document";
        String expectedObjectQuery = "SELECT (SELECT COUNT(version.id) FROM Version version" + singleValuedAssociationIdJoin("version.document", "document_1", true) + " WHERE " + singleValuedAssociationIdPath("version.document.id", "document_1") + " = document.id) FROM Document document ORDER BY document.id ASC";
        assertEquals(expectedCountQuery, pcb.getPageCountQueryString());
        assertEquals(expectedObjectQuery, pcb.getQueryString());
        pcb.getResultList();
    }

    @Test
    // NOTE: Datanucleus has a bug here: https://github.com/datanucleus/datanucleus-core/issues/173
    @Category({ NoDatanucleus.class })
    public void testMultiLevelSubqueryAliasVisibility() {
        final CriteriaBuilder<Long> cb = cbf.create(em, Long.class)
                .from(Document.class, "d")
                .select("d.id")
                .where("d.id").in()
                    .from(Document.class, "d1")
                    .select("d1.id")
                    .whereSubquery()
                        .from(Document.class, "d2")
                        .select("COUNT(*)")
                        .where("d2.parent.id").eqExpression("d.id")
                    .end().gtExpression("0")
                .end();

        final String expectedQuery = "SELECT d.id FROM Document d WHERE d.id IN (" +
                "SELECT d1.id FROM Document d1 WHERE (" +
                    "SELECT " + countStar() + " FROM Document d2" + singleValuedAssociationIdJoin("d2.parent", "parent_1", true) + " " +
                "WHERE " + singleValuedAssociationIdPath("d2.parent.id", "parent_1") + " = d.id" +
                ") > 0)";
        assertEquals(expectedQuery, cb.getQueryString());
        final List<Long> results = cb.getResultList();
        assertTrue(results.isEmpty());
    }

    @Test
    // Test for issue #504
    @Category({ NoDatanucleus.class })
    public void testMultiLevelSubqueryImplicitAliasCollision() {
        final CriteriaBuilder<Long> cb = cbf.create(em, Long.class)
                .from(Document.class)
                .select("id")
                .where("id").in()
                    .from(Document.class)
                    .select("id")
                    .where("id").in()
                        .from(Document.class)
                        .select("id")
                        .where("idx").gt(2)
                    .end()
                .end();

        final String expectedQuery = "SELECT document.id FROM Document document WHERE document.id IN (" +
                "SELECT document_1.id FROM Document document_1 WHERE document_1.id IN (" +
                    "SELECT document_2.id FROM Document document_2 WHERE document_2.idx > :param_0" +
                "))";
        assertEquals(expectedQuery, cb.getQueryString());
        final List<Long> results = cb.getResultList();
        assertTrue(results.isEmpty());
    }
}
