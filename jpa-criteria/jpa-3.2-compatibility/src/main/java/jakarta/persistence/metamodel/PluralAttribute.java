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
 * Instances of the type {@code PluralAttribute} represent 
 * persistent collection-valued attributes.
 *
 * @param <X> The type the represented collection belongs to
 * @param <C> The type of the represented collection
 * @param <E> The element type of the represented collection
 *
 * @since 2.0
 */
public interface PluralAttribute<X, C, E> 
		extends Attribute<X, C>, Bindable<E> {
	
	enum CollectionType {

	    /** Collection-valued attribute */
	    COLLECTION, 

	    /** Set-valued attribute */
	    SET, 

	    /** List-valued attribute */
	    LIST, 

	    /** Map-valued attribute */
	    MAP
	}
		
    /**
     * Return the collection type.
     * @return collection type
     */
    CollectionType getCollectionType();

    /**
     * Return the type representing the element type of the 
     * collection.
     * @return element type
     */
    Type<E> getElementType();
}
