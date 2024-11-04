/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite;

import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOpenJPA;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Properties;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class QueryResultCachingTest extends AbstractCoreTest {

    private Document d;

    @Override
    protected Properties applyProperties(Properties properties) {
        properties = super.applyProperties(properties);
        properties.setProperty("hibernate.cache.use_query_cache", "true");
        properties.setProperty("hibernate.cache.region.factory_class", "org.hibernate.testing.cache.CachingRegionFactory");
        return properties;
    }

    @Before
    public final void setUp() {
        d = new Document("a", new Person("a"));
        em.persist(d);
        em.flush();
        em.clear();
        em.getTransaction().commit();
        em.close();
        em = emf.createEntityManager();
        em.getTransaction().begin();
        enableQueryCollecting();
    }

    @After
    public final void tearDown() {
        disableQueryCollecting();
        Document document = em.find(Document.class, d.getId());
        em.remove(document);
        em.remove(document.getOwner());
        em.flush();
    }

    // Apparently, only Hibernate is able to cache scalar queries...
    @Test
    @Category({ NoEclipselink.class, NoDatanucleus.class, NoOpenJPA.class })
    public void queryResultCachingTest() {
        TypedQuery<String> query = cbf.create(em, String.class)
                .from(Document.class)
                .select("name")
                .where("id").in(d.getId())
                .setCacheable(true)
                .getQuery();

        // Iterate through the results so that DataNucleus properly fills the cache...
        for (String s : query.getResultList()) {
            s.length();
        }
        assertQueryCount(1);
        query.getResultList();
        assertQueryCount(1);
    }

    // Apparently, only Hibernate is able to cache scalar queries...
    @Test
    @Category({ NoEclipselink.class, NoDatanucleus.class, NoOpenJPA.class })
    public void queryResultCachingWithObjectBuilderTest() {
        TypedQuery<TestObject> query = cbf.create(em, String.class)
                .from(Document.class)
                .selectNew(TestObject.class)
                    .with("name")
                .end()
                .where("id").in(d.getId())
                .setCacheable(true)
                .getQuery();

        clearQueries();
        // Iterate through the results so that DataNucleus properly fills the cache...
        for (TestObject testObject : query.getResultList()) {

        }

        assertQueryCount(1);
        query.getResultList();
        assertQueryCount(1);
    }

    public static class TestObject {
        private final String name;

        public TestObject(String name) {
            this.name = name;
        }
    }
}
