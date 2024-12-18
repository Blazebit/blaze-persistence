/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria.impl.support;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.Predicate;

/**
 * An interface necessary to be able to compile against JPA 2.0 but let users use JPA 2.1 APIs.
 *
 * @param <Z> the source type of the join
 * @param <E> the element type of the target <code>List</code>
 * @author Christian Beikov
 * @since 1.3.0
 */
public interface ListJoinSupport<Z, E> extends ListJoin<Z, E> {

    ListJoin<Z, E> on(Expression<Boolean> restriction);

    ListJoin<Z, E> on(Predicate... restrictions);

}
