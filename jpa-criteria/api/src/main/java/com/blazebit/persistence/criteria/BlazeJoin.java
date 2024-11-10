/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;

/**
 * An extended version of {@link Join}.
 *
 * @param <Z> the source type of the join
 * @param <X> the target type of the join
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface BlazeJoin<Z, X> extends Fetch<Z, X>, Join<Z, X>, BlazeFrom<Z, X> {

    /**
     * Treats this from object as the given subtype. This will not cause a treat join but return a wrapper,
     * that can be used for further joins.
     *
     * @param type type to be downcast to
     * @param <T>  The target treat type
     * @return The treated join object
     */
    <T extends X> BlazeJoin<Z, T> treatAs(Class<T> type);

    /**
     * Fetches this join.
     *
     * @return this join instance
     */
    BlazeJoin<Z, X> fetch();

    /**
     * Whether this join is marked to also be fetched.
     *
     * @return true if it should be fetched, false otherwise
     */
    boolean isFetch();

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
    BlazeJoin<Z, X> on(Expression<Boolean> restriction);

    /**
     * Modify the join to restrict the result according to the
     * specified ON condition. Replaces the previous ON condition,
     * if any.
     * Return the join object
     *
     * @param restrictions zero or more restriction predicates
     * @return the modified join object
     */
    BlazeJoin<Z, X> on(Predicate... restrictions);

    /**
     * Return the predicate that corresponds to the ON
     * restriction(s) on the join, or null if no ON condition
     * has been specified.
     *
     * @return the ON restriction predicate
     */
    Predicate getOn();

}
