/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.hibernate;

import com.blazebit.persistence.integration.hibernate.base.HibernateJpaProvider;
import com.blazebit.persistence.integration.hibernate.base.function.AbstractHibernateEntityManagerFactoryIntegrator;
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
public class Hibernate7EntityManagerFactoryIntegrator extends AbstractHibernateEntityManagerFactoryIntegrator {

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
