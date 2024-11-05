/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite;

import java.util.List;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.impl.BuilderChainingException;
import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlMacro;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.entity.Version;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
        
        verifyException(crit, BuilderChainingException.class, r -> r.getResultList());
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
        String expectedQuery = "SELECT d FROM Document d WHERE d.owner IN (SELECT people FROM " + correlationPath(Document.class, "d.people", "people", "id = d.id AND", " WHERE") + " people.partnerDocument = d)";
        assertEquals(expectedQuery, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testSubqueryImplicitCorrelate() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d")
                .whereExists()
                    .from(Person.class, "p")
                    .where("d.owner.friend.name").isNotNull()
                    .where("d.owner.defaultLanguage").isNotNull()
                .end();
        String expectedQuery = "SELECT d FROM Document d WHERE EXISTS (SELECT 1 FROM Person p, Document d_owner_base JOIN d_owner_base.owner owner_1 LEFT JOIN owner_1.friend friend_1 WHERE d.id = d_owner_base.id AND friend_1.name IS NOT NULL AND owner_1.defaultLanguage IS NOT NULL)";
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
        String peopleCorrelation = correlationPath(Document.class, "d.people", "people", "id = d.id");
        String where = " WHERE ";
        String peopleCorrelationWhere = "";
        int idx;
        if ((idx = peopleCorrelation.indexOf(where)) != -1) {
            peopleCorrelationWhere = peopleCorrelation.substring(idx + where.length());
            peopleCorrelation = peopleCorrelation.substring(0, idx);
        }
        String expectedQuery = "SELECT d FROM Document d WHERE d.owner IN (SELECT people FROM " + peopleCorrelation + " WHERE ";
        if (!peopleCorrelationWhere.isEmpty()) {
            expectedQuery += peopleCorrelationWhere + " AND ";
        }
        expectedQuery += "people.partnerDocument = d)";
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
        String peopleCorrelation = correlationPath(Document.class, "d.people", "people", "id = d.id");
        String where = " WHERE ";
        String peopleCorrelationWhere = "";
        int idx;
        if ((idx = peopleCorrelation.indexOf(where)) != -1) {
            peopleCorrelationWhere = peopleCorrelation.substring(idx + where.length());
            peopleCorrelation = peopleCorrelation.substring(0, idx);
        }
        String expectedQuery = "SELECT d FROM Document d WHERE d.owner IN (SELECT people FROM " + peopleCorrelation + " WHERE ";
        if (!peopleCorrelationWhere.isEmpty()) {
            expectedQuery += peopleCorrelationWhere + " AND ";
        }
        expectedQuery += "people.partnerDocument = d)";
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
        String expectedQuery = "SELECT d FROM Document d WHERE EXISTS (SELECT 1 FROM d.owner dSub_base JOIN dSub_base.ownedDocuments dSub WHERE dSub <> d)";
        assertEquals(expectedQuery, crit.getQueryString());
        crit.getResultList();
    }

    // This test compares ids
    @Test
    @Category({ NoEclipselink.class })
    public void testSubqueryCorrelatesArrayExpression() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d")
                .whereExists()
                    .from("Document[_ MEMBER OF d.owner.ownedDocuments AND LENGTH(d.owner.name) > 0]", "dSub")
                    .where("dSub").notEqExpression("d")
                .end();
        String expectedQuery = "SELECT d FROM Document d WHERE EXISTS (SELECT 1 FROM Document dSub, Document d_owner_base JOIN d_owner_base.owner owner_1 WHERE dSub MEMBER OF owner_1.ownedDocuments AND LENGTH(owner_1.name) > 0 AND d.id = d_owner_base.id AND dSub <> d)";
        assertEquals(expectedQuery, crit.getQueryString());
        crit.getResultList();
    }

    // This test compares entities directly
    @Test
    @Category({ NoHibernate.class })
    public void testSubqueryCorrelatesArrayExpressionEntityEqual() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d")
                .whereExists()
                .from("Document[_ MEMBER OF d.owner.ownedDocuments AND LENGTH(d.owner.name) > 0]", "dSub")
                .where("dSub").notEqExpression("d")
                .end();
        String expectedQuery = "SELECT d FROM Document d WHERE EXISTS (SELECT 1 FROM Document dSub, Document d_owner_base JOIN d_owner_base.owner owner_1 WHERE dSub MEMBER OF owner_1.ownedDocuments AND LENGTH(owner_1.name) > 0 AND d = d_owner_base AND dSub <> d)";
        assertEquals(expectedQuery, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testMultipleCorrelationsWithJoins() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d")
                .where("owner").in()
                .from("d.people", "person")
                .leftJoin("person.partnerDocument", "personDoc")
                .from("d.partners", "partner")
                .leftJoin("partner.partnerDocument", "partnerDoc")
                .from("d.contacts", "contact")
                .leftJoin("contact.partnerDocument", "contactDoc")
                .select("contact")
                .where("contactDoc").eqExpression("d")
                .end();
        String peopleCorrelation = correlationPath(Document.class, "d.people", "person", "id = d.id");
        String partnersCorrelation = correlationPath("d.partners", Person.class, "partner", "partnerDocument.id = d.id");
        String where = " WHERE ";
        String peopleCorrelationWhere = "";
        String partnersCorrelationWhere = "";
        int idx;
        if ((idx = peopleCorrelation.indexOf(where)) != -1) {
            peopleCorrelationWhere = peopleCorrelation.substring(idx + where.length());
            peopleCorrelation = peopleCorrelation.substring(0, idx);
        }
        if ((idx = partnersCorrelation.indexOf(where)) != -1) {
            partnersCorrelationWhere = partnersCorrelation.substring(idx + where.length());
            partnersCorrelation = partnersCorrelation.substring(0, idx);
        }
        String expectedQuery = "SELECT d FROM Document d WHERE d.owner IN (SELECT " + joinAliasValue("contact") + " " +
                "FROM " + peopleCorrelation +" LEFT JOIN person.partnerDocument personDoc, " +
                partnersCorrelation + " LEFT JOIN partner.partnerDocument partnerDoc, " +
                "d.contacts contact " +
                "LEFT JOIN contact.partnerDocument contactDoc " +
                "WHERE ";
        if (!peopleCorrelationWhere.isEmpty()) {
            expectedQuery += peopleCorrelationWhere + " AND ";
        }
        if (!partnersCorrelationWhere.isEmpty()) {
            expectedQuery += partnersCorrelationWhere + " AND ";
        }
        expectedQuery += "contactDoc = d)";
        assertEquals(expectedQuery, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testReorderExplicitJoins() {
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
        String peopleCorrelation = correlationPath(Document.class, "d.people", "person", "id = d.id");
        String partnersCorrelation = correlationPath("d.partners", Person.class, "partner", "partnerDocument.id = d.id");
        String where = " WHERE ";
        String peopleCorrelationWhere = "";
        String partnersCorrelationWhere = "";
        int idx;
        if ((idx = peopleCorrelation.indexOf(where)) != -1) {
            peopleCorrelationWhere = peopleCorrelation.substring(idx + where.length());
            peopleCorrelation = peopleCorrelation.substring(0, idx);
        }
        if ((idx = partnersCorrelation.indexOf(where)) != -1) {
            partnersCorrelationWhere = partnersCorrelation.substring(idx + where.length());
            partnersCorrelation = partnersCorrelation.substring(0, idx);
        }
        String expectedQuery = "SELECT d FROM Document d WHERE d.owner IN (SELECT " + joinAliasValue("contact") + " " +
            "FROM " + peopleCorrelation +" LEFT JOIN person.partnerDocument personDoc, " +
            partnersCorrelation + " LEFT JOIN partner.partnerDocument partnerDoc, " +
            "d.contacts contact " +
            "LEFT JOIN contact.partnerDocument contactDoc " +
            "WHERE ";
        if (!peopleCorrelationWhere.isEmpty()) {
            expectedQuery += peopleCorrelationWhere + " AND ";
        }
        if (!partnersCorrelationWhere.isEmpty()) {
            expectedQuery += partnersCorrelationWhere + " AND ";
        }
        expectedQuery += "contactDoc = d)";
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
                .from(Person.class, "p")
                .select("name")
                .where("LENGTH(d.partners.localized[1])").gt(1)
                .end()
            .like().value("%dld").noEscape();
        String expectedQuery = "SELECT d FROM Document d"
                + " WHERE (SELECT p.name FROM Person p, Document d_partners_base " +
                "LEFT JOIN d_partners_base.partners partners_1 " +
                "LEFT JOIN partners_1.localized localized_1_1" + onClause("KEY(localized_1_1) = 1") +
                " WHERE d.id = d_partners_base.id AND LENGTH("+ joinAliasValue("localized_1_1") + ") > :param_0) LIKE :param_1";
        assertEquals(expectedQuery, crit.getQueryString());
        crit.getResultList();
    }

    // NOTE: Hibernate ORM doesn't detect that it has to use the join alias column
    @Test
    @Category({ NoHibernate.class })
    public void testSubqueryUsesOuterJoin() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class)
                .from(Document.class, "d")
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
                + "FROM Document d LEFT JOIN d.contacts c GROUP BY d.id, " + groupBy(joinAliasValue("c", "id"), renderNullPrecedenceGroupBy(joinAliasValue("c", "id"), "ASC", "LAST")) + " ORDER BY localizedCount ASC";
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

        String expectedSubQuery = "ABS((SELECT COUNT(" + joinAliasValue("localized_1") + ") FROM Person p LEFT JOIN p.localized localized_1, Document d_contacts_base LEFT JOIN d_contacts_base.contacts contacts_1 WHERE d.id = d_contacts_base.id AND p.id = " + joinAliasValue("contacts_1", "id") + "))";
        String expectedQuery = "SELECT d.id, " + expectedSubQuery + " AS localizedCount "
                + "FROM Document d GROUP BY d.id ORDER BY localizedCount ASC";
        assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }

    @Test
    public void testSubqueryCollectionAccess() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.whereSubquery()
                .from(Person.class, "p")
                .select("name")
                .where("LENGTH(d.partners.localized[1])").gt(1)
                .end()
            .like().value("%dld").noEscape();

        String expectedQuery = "SELECT d FROM Document d"
                + " WHERE (SELECT p.name FROM Person p, Document d_partners_base " +
                "LEFT JOIN d_partners_base.partners partners_1 " +
                "LEFT JOIN partners_1.localized localized_1_1" + onClause("KEY(localized_1_1) = 1") +
                " WHERE d.id = d_partners_base.id AND LENGTH("+ joinAliasValue("localized_1_1") + ") > :param_0) LIKE :param_1";
        assertEquals(expectedQuery, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testMultipleJoinPathSubqueryCollectionAccess() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.leftJoin("d.partners.localized", "l")
                .whereSubquery()
                    .from(Person.class, "p")
                    .select("name")
                    .where("LENGTH(d.partners.localized[1])").gt(1)
                .end()
                .like().value("%dld").noEscape();

        String expectedQuery = "SELECT d FROM Document d "
                + "LEFT JOIN d.partners partners_1 "
                + "LEFT JOIN partners_1.localized l "
                + "WHERE (SELECT p.name FROM Person p, Person d_partners_localized_base LEFT JOIN d_partners_localized_base.localized localized_1_1"
                + onClause("KEY(localized_1_1) = 1")
                + " WHERE partners_1.id = d_partners_localized_base.id AND LENGTH("+ joinAliasValue("localized_1_1") + ") > :param_0) LIKE :param_1";
        assertEquals(expectedQuery, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    // A special test for Hibernate that needs a different query to be generated to properly work
    @Category({ NoEclipselink.class })
    public void testJoinElementCollectionsOnCorrelatedInverseAssociations() {
        CriteriaBuilder<Integer> crit = cbf.create(em, Integer.class)
                .from(Document.class, "d")
                .select("1");
        crit.whereExists()
                .from("d.partners", "p")
                .where("LENGTH(localized[1])").gt(1)
        .end();

        String p = correlationPath("d.partners", Person.class, "p", "partnerDocument.id = d.id");
        String wherePart = " WHERE ";
        int whereIndex = p.indexOf(wherePart);
        if (whereIndex != -1) {
            wherePart += p.substring(whereIndex + wherePart.length()) + " AND ";
            p = p.substring(0, whereIndex);
        }
        wherePart += "LENGTH("+ joinAliasValue("localized_1_1") + ") > :param_0";
        String expectedQuery = "SELECT 1 FROM Document d"
                + " WHERE EXISTS (SELECT 1 FROM " + p + " LEFT JOIN p.localized localized_1_1"
                + onClause("KEY(localized_1_1) = 1")
                + wherePart + ")";
        assertEquals(expectedQuery, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testInvalidSubqueryOrderByCollectionAccess() {
        PaginatedCriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class)
                .from(Document.class, "d")
                .leftJoin("d.contacts", "c")
                .selectSubquery("localizedCount")
                    .from(Person.class, "p")
                    .select("COUNT(p.localized)")
                    .where("p.id").eqExpression("c.id")
                .end()
                .orderByAsc("localizedCount")
                .orderByAsc("id")
                .page(0, 1)
                .withInlineIdQuery(false);
        // In a paginated query access to outer collections is disallowed in the order by clause
        verifyException(cb, IllegalStateException.class, r -> r.getPageIdQueryString());
    }

    @Test
    public void testInvalidSubqueryOrderByCollectionAccessNewJoin() {
        PaginatedCriteriaBuilder<Tuple> pcb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .selectSubquery("localizedCount")
                    .from(Person.class, "p")
                    .select("COUNT(p.localized)")
                    .where("p.id").eqExpression("d.contacts.id")
                .end()
                .orderByAsc("localizedCount")
                .orderByAsc("id")
                .page(0, 1)
                .withInlineIdQuery(false);

        String expectedCountQuery = "SELECT " + countPaginated("d.id", false) + " FROM Document d";
        String subquery = "SELECT COUNT(" + joinAliasValue("localized_1") + ") FROM Person p LEFT JOIN p.localized localized_1, Document d_contacts_base LEFT JOIN d_contacts_base.contacts contacts_1 WHERE d.id = d_contacts_base.id AND p.id = " + joinAliasValue("contacts_1","id");
        String expectedObjectQuery = "SELECT (" + subquery + ") AS localizedCount FROM Document d ORDER BY localizedCount ASC, d.id ASC";
        pcb.getResultList();
        assertEquals(expectedCountQuery, pcb.getPageCountQueryString());
        assertEquals(expectedObjectQuery, pcb.withInlineCountQuery(false).getQueryString());
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
        pcb.getResultList();
        assertEquals(expectedCountQuery, pcb.getPageCountQueryString());
        assertEquals(expectedObjectQuery, pcb.withInlineCountQuery(false).getQueryString());
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
        pcb.getResultList();
        assertEquals(expectedCountQuery, pcb.getPageCountQueryString());
        assertEquals(expectedObjectQuery, pcb.withInlineCountQuery(false).getQueryString());
    }

    @Test
    public void testPaginationWithOuterAttributeInSubquery() {
        CriteriaBuilder<Document> cb = cbf.create(em, Document.class)
                .whereExists()
                    .from(Person.class)
                    .where("id").eqExpression("owner.id")
                .end();

        // It is disallowed to refer to implicitly refer to attributes of the outer query root
        verifyException(cb, IllegalArgumentException.class, r -> r.getQueryString());
    }

    @Test
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

    // Test for #2031
    @Test
    public void testExistingCorrelatedImplicitJoinInSubquery() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class);
        cb.from(Document.class, "doc");
        cb.selectSubquery()
                .from(Person.class, "p")
                // It's important to create the JoinTreeNode for partnerDocument through this select
                .select("p.partnerDocument.name")
                .whereExists()
                    .from( "p.ownedDocuments", "d" )
                    .where( "p.partnerDocument.nameObject.primaryName" ).eq("test")
                .end()
                .end();

        String subquery;
        if (jpaProvider.needsCorrelationPredicateWhenCorrelatingWithWhereClause()) {
            subquery = "Document d, Document p_partnerDocument_nameObject_base WHERE d.owner.id = p.id AND partnerDocument_1.id = p_partnerDocument_nameObject_base.id AND p_partnerDocument_nameObject_base.";
        } else {
            subquery = "p.ownedDocuments d, Document p_partnerDocument_nameObject_base WHERE partnerDocument_1.id = p_partnerDocument_nameObject_base.id AND p_partnerDocument_nameObject_base.";
        }

        final String expectedQuery = "SELECT (SELECT partnerDocument_1.name " +
                "FROM Person p LEFT JOIN p.partnerDocument partnerDocument_1 " +
                "WHERE EXISTS (SELECT 1 FROM " + subquery + "nameObject.primaryName = :param_0)) FROM Document doc";
        assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }
}
