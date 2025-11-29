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
 * The {@code TupleElement} interface defines an element that is
 * returned in a query result tuple.
 * 
 * @param <X> the type of the element
 *
 * @see Tuple
 *
 * @since 2.0
 */
public interface TupleElement<X> {
    
    /**
     * Return the Java type of the tuple element.
     * @return the Java type of the tuple element
     */
    Class<? extends X> getJavaType();

    /**
     * Return the alias assigned to the tuple element or null, 
     * if no alias has been assigned.
     * @return alias
     */
    String getAlias();
}
