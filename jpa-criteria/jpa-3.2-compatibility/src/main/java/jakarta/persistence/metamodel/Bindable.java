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

import jakarta.persistence.criteria.Path;

/**
 * An instances of the type {@code Bindable} represents an object
 * or attribute type that can be bound into a {@link Path Path}.
 *
 * @param <T> The type of the represented object or attribute
 *
 * @since 2.0
 *
 */
public interface Bindable<T> {
    
    enum BindableType {
        /**
         * Single-valued attribute type.
         *
         * @see SingularAttribute
         */
        SINGULAR_ATTRIBUTE, 

        /**
         * Multivalued attribute type, that is, a collection.
         *
         * @see PluralAttribute
         */
        PLURAL_ATTRIBUTE, 

        /**
         * Entity type.
         *
         * @see EntityType
         */
        ENTITY_TYPE
    }

    /**
     * Return the bindable type of the represented object.
     * @return bindable type
     */	
    BindableType getBindableType();

    /**
     * Return the Java type of the represented object.
     * If the bindable type of the object is {@code PLURAL_ATTRIBUTE},
     * the Java element type is returned. If the bindable type is
     * {@code SINGULAR_ATTRIBUTE} or {@code ENTITY_TYPE}, 
     * the Java type of the
     * represented entity or attribute is returned.
     * @return Java type
     */
    Class<T> getBindableJavaType();
}
