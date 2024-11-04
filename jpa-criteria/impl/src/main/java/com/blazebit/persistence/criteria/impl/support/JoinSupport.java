/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria.impl.support;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;

/**
 * An interface necessary to be able to compile against JPA 2.0 but let users use JPA 2.1 APIs.
 *
 * @param <Z> the source type of the join
 * @param <X> the target type of the join
 * @author Christian Beikov
 * @since 1.3.0
 */
public interface JoinSupport<Z, X> extends Join<Z, X> {

    Join<Z, X> on(Expression<Boolean> restriction);

    Join<Z, X> on(Predicate... restrictions);

}
