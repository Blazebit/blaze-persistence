/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

import java.util.List;

/**
 * An object build provides the returning bindings that should be used by a {@link ReturningModificationCriteriaBuilder} and provides methods for
 * transforming tuples into the target type <code>T</code>.
 *
 * @param <T> The type that this builder produces
 * @author Christian Beikov
 * @since 1.1.0
 */
public interface ReturningObjectBuilder<T> {

    /**
     * Applies the returning bindings necessary for this object builder to work.
     *
     * @param returningBuilder The returningBuilder on which to apply the returning bindings
     */
    public void applyReturning(SimpleReturningBuilder returningBuilder);

    /**
     * Builds an object of the target type <code>T</code> from the given tuple.
     *
     * @param tuple The result tuple
     * @return The target object
     */
    public T build(Object[] tuple);

    /**
     * Transforms the given list and returns the result.
     *
     * @param list The list to be transformed
     * @return The resulting list
     */
    public List<T> buildList(List<T> list);
}
