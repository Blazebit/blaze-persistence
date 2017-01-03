/*
 * Copyright 2014 - 2017 Blazebit.
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

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;
import javax.persistence.EntityManager;
import javax.transaction.TransactionSynchronizationRegistry;

public class TransactionHelper {

    private TransactionHelper() {
    }

    public static TransactionSynchronizationStrategy getSynchronizationStrategy(EntityManager em) {
        TransactionSynchronizationRegistry synchronizationRegistry;

        try {
            synchronizationRegistry = (TransactionSynchronizationRegistry) new InitialContext().lookup("java:comp/TransactionSynchronizationRegistry");
            if (synchronizationRegistry != null) {
                return new JtaTransactionSynchronizationStrategy(synchronizationRegistry);
            }
        } catch (NoInitialContextException e) {
            // Maybe in Java SE environment
            synchronizationRegistry = null;
        } catch (NamingException e) {
            throw new IllegalArgumentException("Could not access transaction synchronization registry!", e);
        }

        try {
            String version = Class.forName("org.hibernate.Session")
                    .getPackage().getImplementationVersion();
            String[] versionParts = version.split("\\.");
            int major = Integer.parseInt(versionParts[0]);

            if (major >= 5) {
                return new Hibernate5TransactionSynchronizationStrategy(em);
            } else {
                return new Hibernate4TransactionSynchronizationStrategy(em);
            }
        } catch (ClassNotFoundException ex) {
            throw new IllegalArgumentException("Unsupported jpa provider!", ex);
        }
    }
}
