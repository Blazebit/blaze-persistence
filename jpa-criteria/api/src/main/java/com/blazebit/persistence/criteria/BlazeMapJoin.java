/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.MapJoin;
import jakarta.persistence.criteria.Predicate;

/**
 * An extended version of {@link MapJoin}.
 *
 * @param <Z> the source type of the join
 * @param <K> the key type of the target <code>Map</code>
 * @param <V> the element type of the target <code>Map</code>
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface BlazeMapJoin<Z, K, V> extends MapJoin<Z, K, V>, BlazeJoin<Z, V> {

    /**
     * Like {@link BlazeJoin#treatAs} but returns the subtype {@link BlazeMapJoin} instead.
     *
     * @param type type to be downcast to
     * @param <T>  The target treat type
     * @return The treated join object
     */
    <T extends V> BlazeMapJoin<Z, K, T> treatAs(Class<T> type);

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
    BlazeMapJoin<Z, K, V> on(Expression<Boolean> restriction);

    /**
     * Modify the join to restrict the result according to the
     * specified ON condition. Replaces the previous ON condition,
     * if any.
     * Return the join object
     *
     * @param restrictions zero or more restriction predicates
     * @return the modified join object
     */
    BlazeMapJoin<Z, K, V> on(Predicate... restrictions);

}
