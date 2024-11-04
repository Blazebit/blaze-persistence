/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria.impl.support;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.SetJoin;

/**
 * An interface necessary to be able to compile against JPA 2.0 but let users use JPA 2.1 APIs.
 *
 * @param <Z> the source type of the join
 * @param <E> the element type of the target <code>Set</code>
 * @author Christian Beikov
 * @since 1.3.0
 */
public interface SetJoinSupport<Z, E> extends SetJoin<Z, E> {

    SetJoin<Z, E> on(Expression<Boolean> restriction);

    SetJoin<Z, E> on(Predicate... restrictions);

}
