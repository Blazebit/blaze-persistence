/*
 * Copyright 2014 Blazebit.
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
package com.blazebit.testsuite.base;

import com.blazebit.persistence.Criteria;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.spi.CriteriaBuilderConfiguration;
import java.util.Properties;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.junit.After;
import org.junit.Before;

/**
 * 
 * @author Christian
 */
public abstract class AbstractPersistenceTest {

    protected EntityManager em;
    protected CriteriaBuilderFactory cbf;

    @Before
    public void init() {
        Properties properties = new Properties();
        properties.put("eclipselink.metadata-source", "com.blazebit.testsuite.base.EntityClassesMetadataSource");
        properties.put("eclipselink.metadata-source.entity-classes", getEntityClasses());

        EntityManagerFactory factory = Persistence.createEntityManagerFactory("TestsuiteBase", properties);
        em = factory.createEntityManager();

        CriteriaBuilderConfiguration config = Criteria.getDefault();
        cbf = config.createCriteriaBuilderFactory();
    }

    @After
    public void destruct() {
        em.getEntityManagerFactory().close();
    }

    protected abstract Class<?>[] getEntityClasses();
}
