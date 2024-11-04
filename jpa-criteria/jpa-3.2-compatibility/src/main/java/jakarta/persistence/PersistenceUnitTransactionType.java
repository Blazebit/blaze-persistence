/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0,
 * or the Eclipse Distribution License v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause
 */

// Contributors:
//     Gavin King      - 3.2
package jakarta.persistence;

/**
 * Enumerates the possible approaches to transaction management in Jakarta
 * Persistence. An {@link EntityManager} may be a JTA entity manager, where
 * transaction management is done via JTA, or it may be a resource-local
 * entity manager, where transaction management is performed via the
 * {@link EntityTransaction} interface.
 *
 * @since 3.2
 */
public enum PersistenceUnitTransactionType {
    /**
     * Transaction management via JTA.
     */
    JTA,
    /**
     * Resource-local transaction management.
     */
    RESOURCE_LOCAL
}
