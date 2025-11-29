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

import jakarta.persistence.metamodel.EntityType;

import java.util.Set;

/**
 * The {@code CommonAbstractCriteria} interface defines functionality
 * that is common to both top-level criteria queries and subqueries as 
 * well as to update and delete criteria operations.
 * It is not intended to be used directly in query construction.
 *
 * <p> Note that criteria queries and criteria update and delete operations
 * are typed differently.
 * Criteria queries are typed according to the query result type.
 * Update and delete operations are typed according to the target of the
 * update or delete.
 *
 * @since 2.1
 */
public interface CommonAbstractCriteria {

    /**
     * Create a subquery of the query. 
     * @param type  the subquery result type
     * @return subquery 
     */
    <U> Subquery<U> subquery(Class<U> type);

    /**
     * Create a subquery of the query.
     * @param type  the subquery result type
     * @return subquery
     */
    <U> Subquery<U> subquery(EntityType<U> type);

    /**
     * Return the predicate that corresponds to the where clause
     * restriction(s), or null if no restrictions have been
     * specified.
     * @return where clause predicate
     */
    Predicate getRestriction();

    /**
     * Return the parameters of the query.  Returns empty set if
     * there are no parameters.
     * Modifications to the set do not affect the query.
     * @return the query parameters
     */
    Set<ParameterExpression<?>> getParameters();
}
