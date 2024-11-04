/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria.impl.support;

import javax.persistence.criteria.CollectionJoin;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

/**
 * An interface necessary to be able to compile against JPA 2.0 but let users use JPA 2.1 APIs.
 *
 * @param <Z> the source type of the join
 * @param <E> the element type of the target <code>Collection</code>
 * @author Christian Beikov
 * @since 1.3.0
 */
public interface CollectionJoinSupport<Z, E> extends CollectionJoin<Z, E> {

    public CollectionJoin<Z, E> on(Expression<Boolean> restriction);

    public CollectionJoin<Z, E> on(Predicate... restrictions);
}
