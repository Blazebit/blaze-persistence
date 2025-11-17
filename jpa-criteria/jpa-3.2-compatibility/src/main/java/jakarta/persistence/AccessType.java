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

package jakarta.persistence;

/**
 * Used with the {@link Access} annotation to specify an access
 * type to be applied to an entity class, mapped superclass, or
 * embeddable class, or to a specific attribute of such a class.
 * 
 * @see Access
 *
 * @since 2.0
 */
public enum AccessType {

    /**
     * Field-based access is used.
     */
    FIELD,

    /**
     * Property-based access is used, that is, state is accessed
     * via getter and setter methods.
     */
    PROPERTY
}
