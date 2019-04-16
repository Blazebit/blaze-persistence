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

import com.blazebit.reflection.ReflectionUtils;

import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;
import javax.persistence.EntityManager;
import javax.transaction.TransactionSynchronizationRegistry;
import java.lang.reflect.InvocationTargetException;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class TransactionHelper {

    private static final String[] NAMES = {
        "java:comp/TransactionSynchronizationRegistry", // Java EE Standard name
        "java:/TransactionSynchronizationRegistry", // Local providers
        "java:comp/env/TransactionSynchronizationRegistry", // Tomcat
    };

    private TransactionHelper() {
    }

    public static TransactionSynchronizationStrategy getSynchronizationStrategy(EntityManager em) {
        InitialContext context = null;

        try {
            context = new InitialContext();
        } catch (NamingException e) {
            // Maybe in Java SE environment
        }

        if (context != null) {
            for (String name : NAMES) {
                try {
                    TransactionSynchronizationRegistry synchronizationRegistry = (TransactionSynchronizationRegistry) context.lookup(name);
                    if (synchronizationRegistry != null) {
                        return new JtaTransactionSynchronizationStrategy(synchronizationRegistry);
                    }
                } catch (NoInitialContextException | NameNotFoundException e) {
                    // Maybe in Java SE environment
                } catch (NamingException e) {
                    throw new IllegalArgumentException("Could not access transaction synchronization registry!", e);
                }
            }
        }

        try {
            String version = Class.forName("org.hibernate.Session")
                    .getPackage().getImplementationVersion();
            String[] versionParts = version.split("\\.");
            int major = Integer.parseInt(versionParts[0]);

            if (major >= 5) {
                Object jtaPlatform = getHibernate5JtaPlatformPresent(em);
                if (jtaPlatform == null || jtaPlatform.getClass() == Class.forName("org.hibernate.engine.transaction.jta.platform.internal.NoJtaPlatform")) {
                    return new Hibernate5EntityTransactionSynchronizationStrategy(em);
                } else {
                    return new Hibernate5JtaPlatformTransactionSynchronizationStrategy(jtaPlatform);
                }
            } else {
                return new Hibernate4TransactionSynchronizationStrategy(em);
            }
        } catch (ClassNotFoundException ex) {
            throw new IllegalArgumentException("Unsupported jpa provider!", ex);
        }
    }

    private static Object getHibernate5JtaPlatformPresent(EntityManager em) {
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
