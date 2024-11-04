/*
 * Copyright (c) 2008, 2023 Oracle and/or its affiliates. All rights reserved.
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
//     Linda DeMichiel - 2.1
//     Linda DeMichiel - 2.0

package jakarta.persistence.spi;

/**
 * Specifies whether entity managers created by the
 * {@link jakarta.persistence.EntityManagerFactory}
 * are JTA or resource-local entity managers.
 *
 * @since 1.0
 *
 * @deprecated replaced by
 * {@link jakarta.persistence.PersistenceUnitTransactionType}
 */
@Deprecated(since = "3.2", forRemoval = true)
public enum PersistenceUnitTransactionType {

    /** JTA entity managers are created. */
    JTA,
	
    /** Resource-local entity managers are created. */
    RESOURCE_LOCAL
}
