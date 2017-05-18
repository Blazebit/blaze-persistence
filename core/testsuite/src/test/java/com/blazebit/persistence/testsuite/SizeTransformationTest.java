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

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.impl.ConfigurationProperties;
import com.blazebit.persistence.impl.function.subquery.SubqueryFunction;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus4;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMSSQL;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.entity.Version;
import com.blazebit.persistence.testsuite.entity.Workflow;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public class SizeTransformationTest extends AbstractCoreTest {

    // TODO: create datanucleus issue
    @Category(NoDatanucleus.class)
    @Test
    public void testSizeToCountTransformationWithElementCollectionIndexed1() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Workflow.class, "w")
                .select("SIZE(w.localized)");
        String expectedQuery = "SELECT " + function("COUNT_TUPLE", "KEY(localized_1)") + " FROM Workflow w LEFT JOIN w.localized localized_1 GROUP BY w.id";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }

    // TODO: create datanucleus issue
    @Category(NoDatanucleus.class)
    @Test
    public void testSizeToCountTransformationWithElementCollectionIndexed2() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Workflow.class, "w")
                .select("SIZE(w.localized)")
                .leftJoin("w.supportedLocales", "supportedLocales_1");
        String expectedQuery = "SELECT " + function("COUNT_TUPLE", "'DISTINCT'", "KEY(localized_1)") + " FROM Workflow w LEFT JOIN w.localized localized_1 " +
                "LEFT JOIN w.supportedLocales supportedLocales_1 GROUP BY w.id";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }

    // TODO: create datanucleus issue
    @Category(NoDatanucleus.class)
    @Test
    public void testSizeToCountTransformationWithElementCollectionBasic1() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Workflow.class, "w")
                .select("SIZE(w.supportedLocales)");
        String expectedQuery = "SELECT " + function("COUNT_TUPLE", "supportedLocales_1") + " FROM Workflow w LEFT JOIN w.supportedLocales supportedLocales_1 GROUP BY w.id";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }

    // TODO: create datanucleus issue
    @Category(NoDatanucleus.class)
    @Test
    public void testSizeToCountTransformationWithElementCollectionBasic2() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Workflow.class, "w")
                .select("SIZE(w.supportedLocales)")
                .leftJoin("w.localized", "localized");
        String expectedQuery = "SELECT " + function("COUNT_TUPLE", "'DISTINCT'", "supportedLocales_1") + " FROM Workflow w LEFT JOIN w.localized localized " +
                "LEFT JOIN w.supportedLocales supportedLocales_1 GROUP BY w.id";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }

    @Test
    public void testSizeToCountTransformationMultipleSelect() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("d.name")
                .select("SIZE(d.people)");
        String expectedQuery = "SELECT d.name, " + function("COUNT_TUPLE", "INDEX(people_1)") + " " +
                "FROM Document d " +
                "LEFT JOIN d.people people_1 " +
                "GROUP BY d.id, d.name";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }

    @Test
    public void testSizeToCountTransformationWithList1() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("SIZE(d.people)");
        String expectedQuery = "SELECT " + function("COUNT_TUPLE", "INDEX(people_1)") + " FROM Document d LEFT JOIN d.people people_1 GROUP BY d.id";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }

    @Test
    public void testSizeToCountTransformationWithList2() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("SIZE(d.people)")
                .select("d.partners");
        String expectedQuery = "SELECT " + function("COUNT_TUPLE", "'DISTINCT'", "INDEX(people_1)") + ", partners_1 FROM Document d " +
                "LEFT JOIN d.partners partners_1 " +
                "LEFT JOIN d.people people_1 " +
                "GROUP BY d.id, partners_1.age, partners_1.defaultLanguage, partners_1.friend, partners_1.id, partners_1.name, partners_1.nameObject, partners_1.partnerDocument";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }

    @Test
    public void testSizeToCountTransformationWithMap1() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("SIZE(d.contacts)");
        String expectedQuery = "SELECT " + function("COUNT_TUPLE", "KEY(contacts_1)") + " FROM Document d LEFT JOIN d.contacts contacts_1 GROUP BY d.id";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }

    @Test
    public void testSizeToCountTransformationWithMap2() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("SIZE(d.contacts)")
                .select("d.partners");
        String expectedQuery = "SELECT " + function("COUNT_TUPLE", "'DISTINCT'", "KEY(contacts_1)") + ", partners_1 FROM Document d " +
                "LEFT JOIN d.contacts contacts_1 " +
                "LEFT JOIN d.partners partners_1 " +
                "GROUP BY d.id, partners_1.age, partners_1.defaultLanguage, partners_1.friend, partners_1.id, partners_1.name, partners_1.nameObject, partners_1.partnerDocument";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }

    // TODO: create datanucleus issue
    // fails with datanucleus-4
    @Category(NoDatanucleus4.class)
    @Test
    public void testSizeToCountTransformationWithCollectionBag() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("SIZE(d.peopleCollectionBag)");
        String expectedQuery = "SELECT (SELECT " + countStar() + " FROM d.peopleCollectionBag person) FROM Document d";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }

    // TODO: create datanucleus issue
    // fails with datanucleus-5
    @Category(NoDatanucleus.class)
    @Test
    public void testSizeToCountTransformationWithListBag() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("SIZE(d.peopleListBag)");
        String expectedQuery = "SELECT (SELECT " + countStar() + " FROM d.peopleListBag person) FROM Document d";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }

    @Test
    public void testDuplicateSizeExpressionsInSelect() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Person.class, "p")
                .select("SIZE(p.ownedDocuments)")
                .select("SIZE(p.ownedDocuments)");

        String expectedQuery = "SELECT " + function("COUNT_TUPLE", "ownedDocuments_1.id") + ", " + function("COUNT_TUPLE", "ownedDocuments_1.id") + " FROM Person p LEFT JOIN p.ownedDocuments ownedDocuments_1 GROUP BY p.id";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }

    static class Holder {
        Document doc1;
        Document doc2;
        Person o1;
        Person o2;
    }

    @Test
    public void testSizeToCountTransformationMultiLevel() {
        final Holder holder = new Holder();
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                Document doc1;
                Document doc2;
                Person o1;
                Person o2;

                doc1 = new Document("doc1");
                doc2 = new Document("doc2");

                o1 = new Person("Karl1");
                o2 = new Person("Karl2");
                o1.getLocalized().put(1, "abra kadabra");
                o2.getLocalized().put(1, "ass");

                Version v1 = new Version();
                v1.setDocument(doc1);

                Version v2 = new Version();
                v2.setDocument(doc2);
                Version v3 = new Version();
                v3.setDocument(doc2);

                doc1.setOwner(o1);
                doc2.setOwner(o1);

                doc1.getContacts().put(1, o1);
                doc1.getContacts().put(2, o2);

                em.persist(o1);
                em.persist(o2);

                em.persist(doc1);
                em.persist(doc2);

                em.persist(v1);
                em.persist(v2);
                em.persist(v3);

                holder.doc1 = doc1;
                holder.doc2 = doc2;
                holder.o1 = o1;
                holder.o2 = o2;
            }
        });

        Document doc1 = holder.doc1;
        Document doc2 = holder.doc2;
        Person o1 = holder.o1;
        Person o2 = holder.o2;

        CriteriaBuilder<Tuple> cb = cbf.create(this.em, Tuple.class).from(Person.class, "p")
                .leftJoin("p.ownedDocuments", "ownedDocument")
                .select("p.id")
                .select("ownedDocument.id")
                .select("SIZE(ownedDocument.versions)")
                .select("SIZE(p.ownedDocuments)")
                .orderByAsc("p.id")
                .orderByAsc("ownedDocument.id");

        String expectedQuery = "SELECT p.id, ownedDocument.id, " + function("COUNT_TUPLE", "'DISTINCT'", "versions_1.id") + ", (SELECT " + countStar() + " FROM p.ownedDocuments document) FROM Person p LEFT JOIN p.ownedDocuments ownedDocument LEFT JOIN ownedDocument.versions versions_1 GROUP BY " + groupBy("ownedDocument.id", "p.id", renderNullPrecedenceGroupBy("p.id"), renderNullPrecedenceGroupBy("ownedDocument.id")) +
                " ORDER BY p.id ASC, " + renderNullPrecedence("ownedDocument.id", "ASC", "LAST");
        Assert.assertEquals(expectedQuery, cb.getQueryString());
        List<Tuple> result = cb.getResultList();
        Assert.assertEquals(3, result.size());

        Assert.assertArrayEquals(new Object[] { o1.getId(), doc1.getId(), 1l, 2l }, result.get(0).toArray());
        Assert.assertArrayEquals(new Object[] { o1.getId(), doc2.getId(), 2l, 2l }, result.get(1).toArray());
        Assert.assertArrayEquals(new Object[] { o2.getId(), null, 0l, 0l }, result.get(2).toArray());
    }

    @Test
    public void testSizeToCountTransformationMultiLevel2() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Person.class, "p")
                .leftJoin("p.ownedDocuments", "ownedDocument")
                .select("p.id")
                .select("SIZE(ownedDocument.versions)")
                .select("SIZE(p.ownedDocuments)")
                .orderByAsc("p.id");

        String expectedQuery = "SELECT p.id, " + function("COUNT_TUPLE", "'DISTINCT'", "versions_1.id") + ", (SELECT " + countStar() + " FROM p.ownedDocuments document) FROM Person p LEFT JOIN p.ownedDocuments ownedDocument LEFT JOIN ownedDocument.versions versions_1 GROUP BY " + groupBy("ownedDocument.id", "p.id", renderNullPrecedenceGroupBy("p.id")) +
                " ORDER BY p.id ASC";

        Assert.assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }

    @Test
    public void testSizeToCountTransformationSubqueryCorrelationGroupBy() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Person.class, "p")
                .leftJoin("p.ownedDocuments", "ownedDocument")
                .leftJoin("ownedDocument.partners", "partner")
                .select("p.id")
                .select("SIZE(ownedDocument.partners)")
                .select("SIZE(partner.favoriteDocuments)")
                .orderByAsc("p.id");

        String expectedQuery = "SELECT p.id, (SELECT " + countStar() + " FROM ownedDocument.partners person), " + function("COUNT_TUPLE", "'DISTINCT'", "favoriteDocuments_1.id") + " FROM Person p " +
                "LEFT JOIN p.ownedDocuments ownedDocument " +
                "LEFT JOIN ownedDocument.partners partner " +
                "LEFT JOIN partner.favoriteDocuments favoriteDocuments_1 " +
                "GROUP BY " + groupBy("partner.id", "p.id", renderNullPrecedenceGroupBy("p.id"), "ownedDocument.id") +
                " ORDER BY p.id ASC";

        Assert.assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }

    @Test
    public void testSizeToCountTransformationMultiBranches() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Person.class, "p")
                .leftJoin("p.ownedDocuments", "ownedDocument")
                .leftJoin("p.favoriteDocuments", "favoriteDocument")
                .select("p.id")
                .select("ownedDocument.id")
                .select("favoriteDocument.id")
                .select("SIZE(ownedDocument.versions)")
                .select("SIZE(favoriteDocument.versions)")
                .select("SIZE(p.ownedDocuments)")
                .orderByAsc("p.id")
                .orderByAsc("ownedDocument.id")
                .orderByAsc("favoriteDocument.id");

        String expectedQuery = "SELECT p.id, ownedDocument.id, favoriteDocument.id, (SELECT " + countStar() + " FROM ownedDocument.versions version), (SELECT " + countStar() + " FROM favoriteDocument.versions version), (SELECT " + countStar() + " FROM p.ownedDocuments document) FROM Person p LEFT JOIN p.favoriteDocuments favoriteDocument LEFT JOIN p.ownedDocuments ownedDocument " +
                "ORDER BY p.id ASC, " + renderNullPrecedence("ownedDocument.id", "ASC", "LAST") + ", " + renderNullPrecedence("favoriteDocument.id", "ASC", "LAST");
        Assert.assertEquals(expectedQuery, cb.getQueryString());
    }

    @Test
    public void testSizeTransformationInArithmeticExpression() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Person.class, "p")
                .select("SIZE(p.ownedDocuments) + 1");

        String expectedQuery = "SELECT " + function("COUNT_TUPLE", "ownedDocuments_1.id") + " + 1 FROM Person p LEFT JOIN p.ownedDocuments ownedDocuments_1 GROUP BY p.id";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }

    @Test
    public void testSizeTransformationWithLateJoin() {
        CriteriaBuilder<Long> cb = cbf.create(em, Long.class).from(Person.class, "p")
                .select("p.ownedDocuments.id", "ownedDocumentId")
                .select("SIZE(p.ownedDocuments)")
                .orderByAsc("ownedDocumentId");

        String expectedQuery = "SELECT ownedDocuments_1.id AS ownedDocumentId, (SELECT " + countStar() + " FROM p.ownedDocuments document) FROM Person p LEFT JOIN p.ownedDocuments ownedDocuments_1 ORDER BY " + renderNullPrecedence("ownedDocumentId", "ownedDocuments_1.id", "ASC", "LAST");
        Assert.assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }

    @Test
    public void testSizeTransformationMultiLevelCorrelatedFromClause() {
        CriteriaBuilder<Long> cb = cbf.create(em, Long.class)
                        .setProperty(ConfigurationProperties.SIZE_TO_COUNT_TRANSFORMATION, "false")
                        .from(Person.class, "p")
                        .select("SIZE(p.ownedDocuments.partners.favoriteDocuments)");

        String expectedQuery = "SELECT (SELECT " + countStar() + " FROM partners_1.favoriteDocuments document) FROM Person p " +
                "LEFT JOIN p.ownedDocuments ownedDocuments_1 " +
                "LEFT JOIN ownedDocuments_1.partners partners_1";
            Assert.assertEquals(expectedQuery, cb.getQueryString());
            cb.getResultList();
    }

    // from issue #513
    @Test
    @Category({ NoMSSQL.class })
    public void testSumSize() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                Person p1 = new Person("p1");
                Person p2 = new Person("p2");
                Person p3 = new Person("p3");
                Person p4 = new Person("p4");
                Document d1 = new Document("d1");
                Document d2 = new Document("d2");
                d1.setOwner(p1);
                d2.setOwner(p3);
                em.persist(p1);
                em.persist(p2);
                em.persist(p3);
                em.persist(p4);
                d1.getPeople().add(p1);
                d1.getPeople().add(p2);
                d2.getPeople().add(p3);
                d2.getPeople().add(p4);
                em.persist(d1);
                em.persist(d2);
            }
        });

        CriteriaBuilder<Number> cb = cbf.create(em, Number.class)
                .from(Document.class, "d")
                .select("SUM(SIZE(d.people))");

        String expected = "SELECT SUM(" + function(SubqueryFunction.FUNCTION_NAME, "(SELECT " + countStar() + " FROM d.people person)") + ") FROM Document d";
        assertEquals(expected, cb.getQueryString());
        assertEquals(4L, cb.getResultList().get(0).longValue());
    }
}
