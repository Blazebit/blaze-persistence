package com.blazebit.persistence.testsuite;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.base.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.category.NoDatanucleus4;
import com.blazebit.persistence.testsuite.entity.Document;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.Tuple;

/**
 * Created
 * by Moritz Becker (moritz.becker@gmx.at)
 * on 16.09.2016.
 */
public class SizeTransformationTest extends AbstractCoreTest {

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

}
