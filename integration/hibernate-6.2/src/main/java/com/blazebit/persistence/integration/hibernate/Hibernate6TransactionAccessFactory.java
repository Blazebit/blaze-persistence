/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.integration.hibernate;

import com.blazebit.apt.service.ServiceProvider;
import com.blazebit.persistence.view.spi.TransactionAccess;
import com.blazebit.persistence.view.spi.TransactionAccessFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.transaction.jta.platform.spi.JtaPlatform;
import org.hibernate.service.ServiceRegistry;

import jakarta.persistence.EntityManager;

/**
 * @author Moritz Becker
 * @since 1.6.7
 */
@ServiceProvider(TransactionAccessFactory.class)
public class Hibernate6TransactionAccessFactory implements TransactionAccessFactory {
    @Override
    public TransactionAccess createTransactionAccess(EntityManager entityManager) {
        try {
            return new Hibernate6EntityTransactionSynchronizationStrategy(entityManager.getTransaction(), entityManager);
        } catch (IllegalStateException ex) {
            JtaPlatform jtaPlatform = getHibernateJtaPlatform(entityManager);
            return new Hibernate6JtaPlatformTransactionSynchronizationStrategy(jtaPlatform);
        }
    }

    @Override
    public int getPriority() {
        return 100;
    }

    private static JtaPlatform getHibernateJtaPlatform(EntityManager em) {
        Session hibernateSession = em.unwrap(Session.class);
        SessionFactory hibernateSessionFactory = hibernateSession.getSessionFactory();
        ServiceRegistry hibernateServiceRegistry = ((SessionFactoryImplementor) hibernateSessionFactory).getServiceRegistry();
        return hibernateServiceRegistry.getService(JtaPlatform.class);
    }
}
