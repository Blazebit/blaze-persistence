/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria.impl.support;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.MapJoin;
import jakarta.persistence.criteria.Predicate;

/**
 * An interface necessary to be able to compile against JPA 2.0 but let users use JPA 2.1 APIs.
 *
 * @param <Z> the source type of the join
 * @param <K> the key type of the target <code>Map</code>
 * @param <V> the element type of the target <code>Map</code>
 * @author Christian Beikov
 * @since 1.3.0
 */
public interface MapJoinSupport<Z, K, V> extends MapJoin<Z, K, V> {

    MapJoin<Z, K, V> on(Expression<Boolean> restriction);

    MapJoin<Z, K, V> on(Predicate... restrictions);

}
