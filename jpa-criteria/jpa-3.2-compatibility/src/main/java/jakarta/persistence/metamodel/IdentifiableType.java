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

import java.util.Set;

/**
 * An instance of the type {@code IdentifiableType} represents an
 * entity or mapped superclass type.
 *
 * @param <X> The represented entity or mapped superclass type.
 *
 * @since 2.0
 *
 */
public interface IdentifiableType<X> extends ManagedType<X> {
	
    /**
     * Return the attribute that corresponds to the id attribute of 
     * the entity or mapped superclass.
     * @param type  the type of the represented id attribute
     * @return id attribute
     * @throws IllegalArgumentException if id attribute of the given
     *         type is not present in the identifiable type or if
     *         the identifiable type has an id class
     */
    <Y> SingularAttribute<? super X, Y> getId(Class<Y> type);

    /**
     * Return the attribute that corresponds to the id attribute 
     * declared by the entity or mapped superclass.
     * @param type  the type of the represented declared 
     *              id attribute
     * @return declared id attribute
     * @throws IllegalArgumentException if id attribute of the given
     *         type is not declared in the identifiable type or if
     *         the identifiable type has an id class
     */
    <Y> SingularAttribute<X, Y> getDeclaredId(Class<Y> type);

    /**
     * Return the attribute that corresponds to the version 
     * attribute of the entity or mapped superclass.
     * @param type  the type of the represented version attribute
     * @return version attribute
     * @throws IllegalArgumentException if version attribute of the 
     * 	        given type is not present in the identifiable type
     */
    <Y> SingularAttribute<? super X, Y> getVersion(Class<Y> type);

    /**
     * Return the attribute that corresponds to the version 
     * attribute declared by the entity or mapped superclass.
     * @param type  the type of the represented declared version 
     *              attribute
     * @return declared version attribute
     * @throws IllegalArgumentException if version attribute of the 
     *         type is not declared in the identifiable type
     */
    <Y> SingularAttribute<X, Y> getDeclaredVersion(Class<Y> type);
	
    /**
     * Return the identifiable type that corresponds to the most
     * specific mapped superclass or entity extended by the entity 
     * or mapped superclass. 
     * @return supertype of identifiable type or null if no 
     *         such supertype
     */
    IdentifiableType<? super X> getSupertype();

    /**
     * Whether the identifiable type has a single id attribute.
     * Returns true for a simple id or embedded id; returns false
     * for an idclass.
     * @return boolean indicating whether the identifiable
     *         type has a single id attribute
     */
    boolean hasSingleIdAttribute();

    /**
     * Whether the identifiable type has a version attribute.
     * @return boolean indicating whether the identifiable
     *         type has a version attribute
     */
    boolean hasVersionAttribute();

    /**
     *  Return the attributes corresponding to the id class of the
     *  identifiable type.
     *  @return id attributes
     *  @throws IllegalArgumentException if the identifiable type
     *          does not have an id class
     */
     Set<SingularAttribute<? super X, ?>> getIdClassAttributes();

    /**
     * Return the type that represents the type of the id.
     * @return type of id
     */
    Type<?> getIdType();
}
