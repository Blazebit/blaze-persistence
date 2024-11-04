/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.tx;

import com.blazebit.persistence.view.spi.TransactionAccess;
import com.blazebit.persistence.view.spi.TransactionAccessFactory;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;
import javax.transaction.UserTransaction;

/**
 * @author Christian Beikov
 * @since 1.4.0
 */
public class JtaResources implements TransactionAccessFactory {

    private static final String DEFAULT_USER_TRANSACTION_NAME = "java:comp/UserTransaction";
    private static final String[] TRANSACTION_MANAGER_NAMES = {
        "java:comp/TransactionManager",
        "java:appserver/TransactionManager",
        "java:pm/TransactionManager",
        "java:/TransactionManager"
    };
    private static final String[] TRANSACTION_SYNCHRONIZATION_REGISTRY_NAMES = {
        "java:comp/TransactionSynchronizationRegistry", // Java EE Standard name
        "java:/TransactionSynchronizationRegistry", // Local providers
        "java:comp/env/TransactionSynchronizationRegistry", // Tomcat
    };

    private final TransactionManager transactionManager;
    private final TransactionSynchronizationRegistry transactionSynchronizationRegistry;

    public JtaResources(TransactionManager transactionManager, TransactionSynchronizationRegistry transactionSynchronizationRegistry) {
        this.transactionManager = transactionManager;
        this.transactionSynchronizationRegistry = transactionSynchronizationRegistry;
    }

    public static JtaResources getInstance() {
        InitialContext context = null;

        try {
            context = new InitialContext();
        } catch (NamingException e) {
            // Maybe in Java SE environment
            return null;
        }

        UserTransaction ut = null;
        try {
            ut = (UserTransaction) context.lookup(DEFAULT_USER_TRANSACTION_NAME);
        } catch (NamingException ex) {
        }

        TransactionManager tm = null;
        if (ut instanceof TransactionManager) {
            tm = (TransactionManager) ut;
        }

        for (String jndiName : TRANSACTION_MANAGER_NAMES) {
            try {
                tm = (TransactionManager) context.lookup(jndiName);
                break;
            } catch (NamingException ex) {
            }
        }

        TransactionSynchronizationRegistry tsr = null;
        if (ut instanceof TransactionSynchronizationRegistry) {
            tsr = (TransactionSynchronizationRegistry) ut;
        } else if (tm instanceof TransactionSynchronizationRegistry) {
            tsr = (TransactionSynchronizationRegistry) tm;
        }
        if (tsr == null) {
            for (String name : TRANSACTION_SYNCHRONIZATION_REGISTRY_NAMES) {
                try {
                    tsr = (TransactionSynchronizationRegistry) context.lookup(name);
                    break;
                } catch (NamingException ex) {
                }
            }
        }

        if (tm == null || tsr == null) {
            return null;
        }

        return new JtaResources(tm, tsr);
    }

    @Override
    public TransactionAccess createTransactionAccess(EntityManager entityManager) {
        return new JtaTransactionSynchronizationStrategy(this);
    }

    @Override
    public int getPriority() {
        throw new UnsupportedOperationException();
    }

    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    public TransactionSynchronizationRegistry getTransactionSynchronizationRegistry() {
        return transactionSynchronizationRegistry;
    }
}
