package com.blazebit.persistence.testsuite;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.base.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.category.NoDatanucleus4;
import com.blazebit.persistence.testsuite.entity.*;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.EntityTransaction;
import javax.persistence.Tuple;
import java.util.List;

/**
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public class SizeTransformationTest extends AbstractCoreTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{
                Document.class,
                Version.class,
                Person.class,
                Workflow.class,
                IntIdEntity.class
        };
    }

    // TODO: create datanucleus issue
    @Category(NoDatanucleus.class)
    @Test
    public void testSizeToCountTransformationWithElementCollectionIndexed1() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Workflow.class, "w")
                .select("SIZE(w.localized)");
        String expectedQuery = "SELECT " + function("COUNT_TUPLE", "w.id", "KEY(localized_1)") + " FROM Workflow w LEFT JOIN w.localized localized_1 GROUP BY w.id";
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
        String expectedQuery = "SELECT " + function("COUNT_TUPLE", "'DISTINCT'", "w.id", "KEY(localized_1)") + " FROM Workflow w LEFT JOIN w.localized localized_1 " +
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
        String expectedQuery = "SELECT COUNT(supportedLocales_1) FROM Workflow w LEFT JOIN w.supportedLocales supportedLocales_1 GROUP BY w.id";
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
        String expectedQuery = "SELECT COUNT(DISTINCT supportedLocales_1) FROM Workflow w LEFT JOIN w.localized localized " +
                "LEFT JOIN w.supportedLocales supportedLocales_1 GROUP BY w.id";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }

    @Test
    public void testSizeToCountTransformationWithList1() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("SIZE(d.people)");
        String expectedQuery = "SELECT " + function("COUNT_TUPLE", "d.id", "INDEX(people_1)") + " FROM Document d LEFT JOIN d.people people_1 GROUP BY d.id";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }

    /**
     * involves two collections and thus requires the usage of distinct
     */
    @Test
    public void testSizeToCountTransformationWithList2() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("SIZE(d.people)")
                .select("d.partners");
        String expectedQuery = "SELECT " + function("COUNT_TUPLE", "'DISTINCT'", "d.id", "INDEX(people_1)") + ", partners_1 FROM Document d " +
                "LEFT JOIN d.partners partners_1 " +
                "LEFT JOIN d.people people_1 " +
                "GROUP BY d.id, partners_1.age, partners_1.id, partners_1.name, partners_1.partnerDocument";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }

    @Test
    public void testSizeToCountTransformationWithMap1() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("SIZE(d.contacts)");
        String expectedQuery = "SELECT " + function("COUNT_TUPLE", "d.id", "KEY(contacts_1)") + " FROM Document d LEFT JOIN d.contacts contacts_1 GROUP BY d.id";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }

    @Test
    public void testSizeToCountTransformationWithMap2() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("SIZE(d.contacts)")
                .select("d.partners");
        String expectedQuery = "SELECT " + function("COUNT_TUPLE", "'DISTINCT'", "d.id", "KEY(contacts_1)") + ", partners_1 FROM Document d " +
                "LEFT JOIN d.contacts contacts_1 " +
                "LEFT JOIN d.partners partners_1 " +
                "GROUP BY d.id, partners_1.age, partners_1.id, partners_1.name, partners_1.partnerDocument";
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
        String expectedQuery = "SELECT (SELECT " + countStar() + " FROM Document document LEFT JOIN document.peopleCollectionBag peopleCollectionBag WHERE document = d) FROM Document d";
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
        String expectedQuery = "SELECT (SELECT " + countStar() + " FROM Document document LEFT JOIN document.peopleListBag peopleListBag WHERE document = d) FROM Document d";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }

    @Ignore
    @Test
    public void testSizeToCountTransformationMultiLevel() {
        EntityTransaction tx = em.getTransaction();
        Document doc1;
        Document doc2;
        Person o1;
        Person o2;
        try {
            tx.begin();
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

            em.flush();
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw new RuntimeException(e);
        }

        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Person.class, "p")
                .leftJoin("p.ownedDocuments", "ownedDocument")
                .select("p.id")
                .select("ownedDocument.id")
                .select("SIZE(ownedDocument.versions)")
                .select("SIZE(p.ownedDocuments)")
                .orderByAsc("p.id")
                .orderByAsc("ownedDocument.id");

        String expectedQuery = "SELECT p.id, (SELECT " + countStar() + " FROM p.ownedDocuments), (SELECT " + countStar() + " FROM ownedDocument.versions) FROM Person p LEFT JOIN p.ownedDocuments ownedDocument";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
        List<Tuple> result = cb.getResultList();
        Assert.assertEquals(3, result.size());

        Assert.assertArrayEquals(new Object[] { o1.getId(), doc1.getId(), 1, 2 }, result.get(0).toArray());
        Assert.assertArrayEquals(new Object[] { o1.getId(), doc2.getId(), 2, 2 }, result.get(1).toArray());
        Assert.assertArrayEquals(new Object[] { o2.getId(), null, 0, 0 }, result.get(2).toArray());
    }

}
