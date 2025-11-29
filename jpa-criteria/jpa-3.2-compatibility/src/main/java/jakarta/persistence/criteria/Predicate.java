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

import java.util.List;

/**
 * The type of a simple or compound predicate: a conjunction or
 * disjunction of restrictions.
 * A simple predicate is considered to be a conjunction with a
 * single conjunct.
 *
 * @since 2.0
 */
public interface Predicate extends Expression<Boolean> {

        enum BooleanOperator {
              AND, OR
        }
	
    /**
     * Return the boolean operator for the predicate.
     * If the predicate is simple, this is {@code AND}.
     * @return boolean operator for the predicate
     */
    BooleanOperator getOperator();
    
    /**
     * Whether the predicate has been created from another
     * predicate by applying {@link Predicate#not()}
     * or by calling {@link CriteriaBuilder#not}.
     * @return boolean indicating if the predicate is 
     *                 a negated predicate
     */
    boolean isNegated();

    /**
     * Return the top-level conjuncts or disjuncts of the
     * predicate. Returns empty list if there are no top-level
     * conjuncts or disjuncts of the predicate.
     * Modifications to the list do not affect the query.
     * @return list of boolean expressions forming the predicate
     */
    List<Expression<Boolean>> getExpressions();

    /**
     * Create a negation of the predicate.
     * @return negated predicate 
     */
    Predicate not();

}
