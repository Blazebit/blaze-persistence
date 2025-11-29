/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.SetJoin;

/**
 * An extended version of {@link SetJoin}.
 *
 * @param <Z> the source type of the join
 * @param <E> the element type of the target <code>Set</code>
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface BlazeSetJoin<Z, E> extends SetJoin<Z, E>, BlazeJoin<Z, E> {

    /**
     * Like {@link BlazeJoin#treatAs} but returns the subtype {@link BlazeSetJoin} instead.
     *
     * @param type type to be downcast to
     * @param <T>  The target treat type
     * @return The treated join object
     */
    <T extends E> BlazeSetJoin<Z, T> treatAs(Class<T> type);

    /* Compatibility for JPA 2.1 */

    /**
     * Modify the join to restrict the result according to the
     * specified ON condition. Replaces the previous ON condition,
     * if any.
     * Return the join object
     *
     * @param restriction a simple or compound boolean expression
     * @return the modified join object
     */
    BlazeSetJoin<Z, E> on(Expression<Boolean> restriction);

    /**
     * Modify the join to restrict the result according to the
     * specified ON condition. Replaces the previous ON condition,
     * if any.
     * Return the join object
     *
     * @param restrictions zero or more restriction predicates
     * @return the modified join object
     */
    BlazeSetJoin<Z, E> on(Predicate... restrictions);

}
