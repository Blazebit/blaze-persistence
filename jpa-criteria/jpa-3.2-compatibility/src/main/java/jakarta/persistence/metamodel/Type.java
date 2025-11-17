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

package jakarta.persistence.metamodel;

/**
 * An instance of the type {@code Type} represents a persistent
 * object or attribute type.
 *
 * @param <X>  The type of the represented object or attribute
 *
 * @since 2.0
 */
public interface Type<X> {

    enum PersistenceType {

        /** Entity class */
        ENTITY,

        /** Embeddable class */
        EMBEDDABLE,

        /** Mapped superclass */
        MAPPED_SUPERCLASS,

        /** Basic type */
        BASIC
    }

    /**
     * Return the persistence type.
     * @return persistence type
     */	
    PersistenceType getPersistenceType();

    /**
     * Return the represented Java type.
     * @return Java type
     */
    Class<X> getJavaType();
}
