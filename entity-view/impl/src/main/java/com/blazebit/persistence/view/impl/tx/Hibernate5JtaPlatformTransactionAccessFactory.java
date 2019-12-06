/*
 * Copyright 2014 - 2019 Blazebit.
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

package com.blazebit.persistence.view.impl.tx;

import com.blazebit.persistence.view.spi.TransactionAccess;
import com.blazebit.persistence.view.spi.TransactionAccessFactory;
import com.blazebit.reflection.ReflectionUtils;

import javax.persistence.EntityManager;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Christian Beikov
 * @since 1.4.0
 */
public class Hibernate5JtaPlatformTransactionAccessFactory implements TransactionAccessFactory {

    @Override
    public TransactionAccess createTransactionAccess(EntityManager entityManager) {
        return new Hibernate5JtaPlatformTransactionSynchronizationStrategy(getHibernate5JtaPlatformPresent(entityManager));
    }

    public static Object getHibernate5JtaPlatformPresent(EntityManager em) {
        try {
            Object hibernateSession = em.unwrap(Class.forName("org.hibernate.Session"));
            Object hibernateSessionFactory = ReflectionUtils.getMethod(hibernateSession.getClass(), "getSessionFactory").invoke(hibernateSession);
            Object hibernateServiceRegistry = ReflectionUtils.getMethod(hibernateSessionFactory.getClass(), "getServiceRegistry").invoke(hibernateSessionFactory);
            return ReflectionUtils.getMethod(hibernateServiceRegistry.getClass(), "getService", Class.class).invoke(hibernateServiceRegistry, Class.forName("org.hibernate.engine.transaction.jta.platform.spi.JtaPlatform"));
        } catch (ClassNotFoundException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException("Unexpected error when attempting to retrieve the Hibernate 5 JTA platform!", e);
        }
    }
}