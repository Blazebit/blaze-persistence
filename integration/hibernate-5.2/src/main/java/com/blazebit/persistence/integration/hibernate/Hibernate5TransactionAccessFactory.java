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
import org.hibernate.engine.transaction.jta.platform.internal.NoJtaPlatform;
import org.hibernate.engine.transaction.jta.platform.spi.JtaPlatform;
import org.hibernate.service.ServiceRegistry;

import javax.persistence.EntityManager;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
@ServiceProvider(TransactionAccessFactory.class)
public class Hibernate5TransactionAccessFactory implements TransactionAccessFactory {
    @Override
    public TransactionAccess createTransactionAccess(EntityManager entityManager) {
        JtaPlatform jtaPlatform = getHibernate5JtaPlatformPresent(entityManager);
        if (jtaPlatform == null || NoJtaPlatform.class == jtaPlatform.getClass()) {
            return new Hibernate5EntityTransactionSynchronizationStrategy(entityManager);
        } else {
            return new Hibernate5JtaPlatformTransactionSynchronizationStrategy(jtaPlatform);
        }
    }

    @Override
    public int getPriority() {
        return 100;
    }

    private static JtaPlatform getHibernate5JtaPlatformPresent(EntityManager em) {
        Session hibernateSession = em.unwrap(Session.class);
        SessionFactory hibernateSessionFactory = hibernateSession.getSessionFactory();
        ServiceRegistry hibernateServiceRegistry = ((SessionFactoryImplementor) hibernateSessionFactory).getServiceRegistry();
        return hibernateServiceRegistry.getService(JtaPlatform.class);
    }
}
