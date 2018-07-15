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

package com.blazebit.persistence.integration.hibernate;

import com.blazebit.apt.service.ServiceProvider;
import com.blazebit.persistence.integration.hibernate.base.HibernateJpaProvider;
import com.blazebit.persistence.integration.hibernate.base.function.AbstractHibernateEntityManagerFactoryIntegrator;
import com.blazebit.persistence.spi.EntityManagerFactoryIntegrator;
import com.blazebit.persistence.spi.JpaProvider;
import com.blazebit.persistence.spi.JpaProviderFactory;
import org.hibernate.Session;
import org.hibernate.dialect.Dialect;
import org.hibernate.ejb.HibernateEntityManagerFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.entity.EntityPersister;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnitUtil;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@ServiceProvider(EntityManagerFactoryIntegrator.class)
public class Hibernate42EntityManagerFactoryIntegrator extends AbstractHibernateEntityManagerFactoryIntegrator {

    @Override
    public String getDbms(EntityManagerFactory entityManagerFactory) {
        if (entityManagerFactory == null) {
            return null;
        }

        EntityManager em = null;

        try {
            em = entityManagerFactory.createEntityManager();
            return getDbms(em);
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    private String getDbms(EntityManager entityManager) {
        if (entityManager == null) {
            return null;
        }
        Session s = entityManager.unwrap(Session.class);
        Dialect dialect = getDialect(s);
        return getDbmsName(dialect);
    }

    private Map<String, CollectionPersister> getCollectionPersisters(EntityManager em) {
        if (em == null) {
            return null;
        }

        return em.unwrap(SessionImplementor.class).getFactory().getCollectionPersisters();
    }

    private Map<String, EntityPersister> getEntityPersisters(EntityManager em) {
        if (em == null) {
            return null;
        }

        return em.unwrap(SessionImplementor.class).getFactory().getEntityPersisters();
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
                    } else if (entityManagerFactory instanceof HibernateEntityManagerFactory) {
                        factory = (SessionFactoryImplementor) ((HibernateEntityManagerFactory) entityManagerFactory).getSessionFactory();
                    }
                    if (entityManagerFactory instanceof HibernateEntityManagerFactory) {
                        return new HibernateJpaProvider(persistenceUnitUtil, getDbmsName(factory.getDialect()), factory.getEntityPersisters(), factory.getCollectionPersisters(), MAJOR, MINOR, FIX);
                    }
                }
                return new HibernateJpaProvider(persistenceUnitUtil, getDbms(em), getEntityPersisters(em), getCollectionPersisters(em), MAJOR, MINOR, FIX);
            }
        };
    }
}
