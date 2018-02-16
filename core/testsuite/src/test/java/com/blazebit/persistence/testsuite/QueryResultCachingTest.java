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

import com.blazebit.persistence.testsuite.entity.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.TypedQuery;
import java.util.Properties;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class QueryResultCachingTest extends AbstractCoreTest {

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

    // NOTE: not sure why, but this fails for Datanucleus/PostgreSQL combination on TravisCI only...
    @Test
    public void queryResultCachingTest() {
        TypedQuery<String> query = cbf.create(em, String.class)
                .from(Document.class)
                .select("name")
                .where("id").in(1L)
                .setCacheable(true)
                .getQuery();

        clearQueries();
        // Iterate through the results so that DataNucleus properly fills the cache...
        for (String s : query.getResultList()) {
            s.length();
        }
        assertQueryCount(1);
        query.getResultList();
        assertQueryCount(1);
    }
}
