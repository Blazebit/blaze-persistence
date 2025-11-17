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
 * Represents an attribute of a Java type.
 *
 * @param <X> The represented type that contains the attribute
 * @param <Y> The type of the represented attribute
 *
 * @since 2.0
 */
public interface Attribute<X, Y> {

	enum PersistentAttributeType {
	    
	     /** Many-to-one association */
	     MANY_TO_ONE, 

     	 /** One-to-one association */
	     ONE_TO_ONE, 
	     
	     /** Basic attribute */
	     BASIC, 

	     /** Embeddable class attribute */
	     EMBEDDED,

	     /** Many-to-many association */
	     MANY_TO_MANY, 

	     /** One-to-many association */
	     ONE_TO_MANY, 

	     /** Element collection */
	     ELEMENT_COLLECTION
	}

    /**
     * Return the name of the attribute.
     * @return name
     */
    String getName();

    /**
     * Return the persistent attribute type for the attribute.
     * @return persistent attribute type
     */
    PersistentAttributeType getPersistentAttributeType();

    /**
     * Return the managed type representing the type in which 
     * the attribute was declared.
     * @return declaring type
     */
    ManagedType<X> getDeclaringType();

    /**
     * Return the Java type of the represented attribute.
     * @return Java type
     */
    Class<Y> getJavaType();

    /**
     * Return the {@link java.lang.reflect.Member} for the
	 *  represented attribute.
     * @return corresponding {@link java.lang.reflect.Member}
     */
    java.lang.reflect.Member getJavaMember();

    /**
     * Is the attribute an association.
     * @return boolean indicating whether the attribute 
     *         corresponds to an association
     */
    boolean isAssociation();

    /**
     * Is the attribute collection-valued (represents a
	 * {@code Collection}, {@code Set}, {@code List}, or
	 * {@code Map}).
     * @return boolean indicating whether the attribute is 
     *         collection-valued
     */
    boolean isCollection();
}
