package com.blazebit.persistence.testsuite;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Workflow;
import org.junit.Assert;
import org.junit.Test;

import javax.persistence.Tuple;

/**
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public class SizeTransformationTest extends AbstractCoreTest {

    @Test
    public void testSizeToCountTransformationWithElementCollectionIndexed1() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Workflow.class, "w")
                .select("SIZE(w.localized)");
        String expectedQuery = "SELECT " + function("count_tuple", "w.id", "KEY(localized_1)") + " FROM Workflow w LEFT JOIN w.localized localized_1 GROUP BY w.id";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }

    @Test
    public void testSizeToCountTransformationWithElementCollectionIndexed2() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Workflow.class, "w")
                .select("SIZE(w.localized)")
                .leftJoin("w.supportedLocales", "supportedLocales_1");
        String expectedQuery = "SELECT " + function("count_tuple", "'DISTINCT'", "w.id", "KEY(localized_1)") + " FROM Workflow w LEFT JOIN w.localized localized_1 " +
                "LEFT JOIN w.supportedLocales supportedLocales_1 GROUP BY w.id";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }

    @Test
    public void testSizeToCountTransformationWithElementCollectionBasic1() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Workflow.class, "w")
                .select("SIZE(w.supportedLocales)");
        String expectedQuery = "SELECT " + function("count_tuple", "w.id", "supportedLocales_1") + " FROM Workflow w LEFT JOIN w.supportedLocales supportedLocales_1 GROUP BY w.id";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }

    @Test
    public void testSizeToCountTransformationWithElementCollectionBasic2() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Workflow.class, "w")
                .select("SIZE(w.supportedLocales)")
                .leftJoin("w.localized", "localized");
        String expectedQuery = "SELECT " + function("count_tuple", "'DISTINCT'", "w.id", "supportedLocales_1") + " FROM Workflow w LEFT JOIN w.supportedLocales supportedLocales_1 " +
                "LEFT JOIN w.localized localized GROUP BY w.id";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }

    @Test
    public void testSizeToCountTransformationWithList1() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("SIZE(d.people)");
        String expectedQuery = "SELECT " + function("count_tuple", "d.id", "INDEX(people_1)") + " FROM Document d LEFT JOIN d.people people_1 GROUP BY d.id";
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
        String expectedQuery = "SELECT " + function("count_tuple", "'DISTINCT'", "d.id", "INDEX(people_1)") + ", partners_1 FROM Document d " +
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
        String expectedQuery = "SELECT " + function("count_tuple", "d.id", "KEY(contacts_1)") + " FROM Document d LEFT JOIN d.contacts contacts_1 GROUP BY d.id";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }

    @Test
    public void testSizeToCountTransformationWithMap2() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("SIZE(d.contacts)")
                .select("d.partners");
        String expectedQuery = "SELECT " + function("count_tuple", "'DISTINCT'", "d.id", "KEY(contacts_1)") + ", partners_1 FROM Document d " +
                "LEFT JOIN d.contacts contacts_1 " +
                "LEFT JOIN d.partners partners_1 " +
                "GROUP BY d.id, partners_1.age, partners_1.id, partners_1.name, partners_1.partnerDocument";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }

    @Test
    public void testSizeToCountTransformationWithCollectionBag() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("SIZE(d.peopleCollectionBag)");
        String expectedQuery = "SELECT (SELECT COUNT(peopleCollectionBag) FROM Document document LEFT JOIN document.peopleCollectionBag peopleCollectionBag WHERE document = d) FROM Document d";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }

    @Test
    public void testSizeToCountTransformationWithListBag() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("SIZE(d.peopleListBag)");
        String expectedQuery = "SELECT (SELECT COUNT(peopleListBag) FROM Document document LEFT JOIN document.peopleListBag peopleListBag WHERE document = d) FROM Document d";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }

}
