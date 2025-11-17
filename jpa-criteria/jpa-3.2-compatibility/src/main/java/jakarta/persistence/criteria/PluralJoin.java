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

import jakarta.persistence.metamodel.PluralAttribute;

/**
 * The {@code PluralJoin} interface defines functionality
 * that is common to joins to all collection types.  It is
 * not intended to be used directly in query construction.
 *
 * @param <Z> the source type
 * @param <C> the collection type
 * @param <E> the element type of the collection 
 *
 * @since 2.0
 */
public interface PluralJoin<Z, C, E> extends Join<Z, E> {

    /**
     * Return the metamodel representation for the collection-valued 
     * attribute corresponding to the join.
     * @return metamodel collection-valued attribute corresponding
     *         to the target of the join
     */
    PluralAttribute<? super Z, C, E> getModel();
}
