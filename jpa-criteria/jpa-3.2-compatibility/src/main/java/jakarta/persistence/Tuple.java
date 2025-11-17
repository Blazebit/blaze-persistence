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

import java.util.List;

/**
 * Interface for extracting the elements of a query result tuple.
 *
 * @see TupleElement
 *
 * @since 2.0
 */
public interface Tuple {

    /**
     * Get the value of the specified tuple element.
     * @param tupleElement  tuple element
     * @return value of tuple element
     * @throws IllegalArgumentException if tuple element
     *         does not correspond to an element in the
     *         query result tuple
     */
    <X> X get(TupleElement<X> tupleElement);

    /**
     * Get the value of the tuple element to which the
     * specified alias has been assigned.
     * @param alias  alias assigned to tuple element
     * @param type of the tuple element
     * @return value of the tuple element
     * @throws IllegalArgumentException if alias
     *         does not correspond to an element in the
     *         query result tuple or element cannot be
     *         assigned to the specified type
     */
    <X> X get(String alias, Class<X> type); 

    /**
     * Get the value of the tuple element to which the
     * specified alias has been assigned.
     * @param alias  alias assigned to tuple element
     * @return value of the tuple element
     * @throws IllegalArgumentException if alias
      *        does not correspond to an element in the
     *         query result tuple
     */
    Object get(String alias); 

    /**
     * Get the value of the element at the specified
     * position in the result tuple. The first position
     * is 0.
     * @param i  position in result tuple
     * @param type  type of the tuple element
     * @return value of the tuple element
     * @throws IllegalArgumentException if i exceeds
     *         length of result tuple  or element cannot
     *         be assigned to the specified type
     */
    <X> X get(int i, Class<X> type);

    /**
     * Get the value of the element at the specified
     * position in the result tuple. The first position
     * is 0.
     * @param i  position in result tuple
     * @return value of the tuple element
     * @throws IllegalArgumentException if i exceeds
     *         length of result tuple
     */
    Object get(int i);

    /**
     * Return the values of the result tuple elements as
     * an array.
     * @return tuple element values
     */
    Object[] toArray();

    /**
     * Return the tuple elements.
     * @return tuple elements
     */
    List<TupleElement<?>> getElements();
}
