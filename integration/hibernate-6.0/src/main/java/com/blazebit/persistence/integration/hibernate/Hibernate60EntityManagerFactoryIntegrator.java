/*
 * Copyright 2014 - 2023 Blazebit.
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

package com.blazebit.persistence.integration.hibernate;

import com.blazebit.apt.service.ServiceProvider;
import com.blazebit.persistence.integration.hibernate.base.HibernateJpaProvider;
import com.blazebit.persistence.integration.hibernate.base.function.AbstractHibernateEntityManagerFactoryIntegrator;
import com.blazebit.persistence.spi.EntityManagerFactoryIntegrator;
import com.blazebit.persistence.spi.JpaProvider;
import com.blazebit.persistence.spi.JpaProviderFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnitUtil;
import org.hibernate.Session;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;

/**
 *
 * @author Christian Beikov
 * @since 1.6.7
 */
@ServiceProvider(EntityManagerFactoryIntegrator.class)
public class Hibernate60EntityManagerFactoryIntegrator extends AbstractHibernateEntityManagerFactoryIntegrator {

    @Override
    public String getDbms(EntityManagerFactory entityManagerFactory) {
        if (entityManagerFactory == null) {
            return null;
        }

        return getDbmsName(entityManagerFactory.unwrap(SessionFactoryImplementor.class).getJdbcServices().getDialect());
    }

    private String getDbms(EntityManager entityManager) {
        if (entityManager == null) {
            return null;
        }
        Session s = entityManager.unwrap(Session.class);
        Dialect dialect = getDialect(s);
        return getDbmsName(dialect);
    }

    @Override
    public JpaProviderFactory getJpaProviderFactory(final EntityManagerFactory entityManagerFactory) {
        return new JpaProviderFactory() {
            @Override
            public JpaProvider createJpaProvider(EntityManager em) {
                SessionFactoryImplementor factory = null;
                PersistenceUnitUtil persistenceUnitUtil = entityManagerFactory == null ? null : entityManagerFactory.getPersistenceUnitUtil();
                if (persistenceUnitUtil == null && em != null) {
                    persistenceUnitUtil = em.getEntityManagerFactory().getPersistenceUnitUtil();
                }
                if (em == null) {
                    if (entityManagerFactory instanceof SessionFactoryImplementor) {
                        factory = (SessionFactoryImplementor) entityManagerFactory;
                    }
                    if (factory == null && entityManagerFactory != null) {
                        factory = entityManagerFactory.unwrap(SessionFactoryImplementor.class);
                    }
                    if (factory != null) {
                        return new HibernateJpaProvider(persistenceUnitUtil, getDbmsName(factory.getJdbcServices().getDialect()), factory.getMappingMetamodel());
                    }
                }
                return new HibernateJpaProvider(persistenceUnitUtil, getDbms(em), em == null ? null : em.unwrap(SessionImplementor.class).getFactory().getMappingMetamodel());
            }
        };
    }
}
