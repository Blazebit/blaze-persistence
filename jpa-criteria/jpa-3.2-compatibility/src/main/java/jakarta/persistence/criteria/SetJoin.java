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

package jakarta.persistence.criteria;

import java.util.Set;
import jakarta.persistence.metamodel.SetAttribute;

/**
 * The {@code SetJoin} interface is the type of the result of
 * joining to a collection over an association or element 
 * collection that has been specified as a {@link java.util.Set}.
 *
 * @param <Z> the source type of the join
 * @param <E> the element type of the target {@code Set}
 *
 * @since 2.0
 */
public interface SetJoin<Z, E> extends PluralJoin<Z, Set<E>, E> {

    /**
     * Modify the join to restrict the result according to the
     * specified ON condition and return the join object.  
     * Replaces the previous ON condition, if any.
     * @param restriction  a simple or compound boolean expression
     * @return the modified join object
     * @since 2.1
     */
    SetJoin<Z, E> on(Expression<Boolean> restriction);

    /**
     * Modify the join to restrict the result according to the
     * specified ON condition and return the join object.  
     * Replaces the previous ON condition, if any.
     * @param restrictions  zero or more restriction predicates
     * @return the modified join object
     * @since 2.1
     */
    SetJoin<Z, E> on(Predicate... restrictions);

    /**
     * Return the metamodel representation for the set attribute.
     * @return metamodel type representing the {@code Set} that is
     *         the target of the join
     */
    SetAttribute<? super Z, E> getModel();
}
