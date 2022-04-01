/*
 * Copyright 2014 - 2022 Blazebit.
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
