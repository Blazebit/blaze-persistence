/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite;

import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMySQL;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOpenJPA;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.IntIdEntity;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.entity.RecursiveEntity;
import com.blazebit.persistence.testsuite.entity.TestCTE;
import com.blazebit.persistence.testsuite.entity.Version;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.TypedQuery;
import java.util.Properties;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.3.1
 */
public class CTEQueryResultCachingTest extends AbstractCoreTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[] {
                Document.class,
                Person.class,
                IntIdEntity.class,
                Version.class,
                RecursiveEntity.class,
                TestCTE.class
        };
    }

    @Override
    protected Properties applyProperties(Properties properties) {
        properties = super.applyProperties(properties);
        properties.setProperty("hibernate.cache.use_query_cache", "true");
        properties.setProperty("hibernate.cache.region.factory_class", "org.hibernate.testing.cache.CachingRegionFactory");
        return properties;
    }

    @Before
    public final void setUp() {
        enableQueryCollecting();
    }

    @After
    public final void tearDown() {
        disableQueryCollecting();
    }

    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoMySQL.class })
    public void cteQueryResultCachingTest() {
        TypedQuery<TestCTE> query = cbf.create(em, TestCTE.class).with(TestCTE.class)
                .from(RecursiveEntity.class, "e")
                .bind("id").select("e.id")
                .bind("name").select("NULL")
                .bind("level").select("1")
                .end()
                .setCacheable(true)
                .getQuery();

        clearQueries();
        query.getResultList();
        assertQueryCount(1);
        query.getResultList();
        assertQueryCount(1);
    }
}
