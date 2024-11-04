/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

import java.util.List;

/**
 * An object build provides the select clause expressions that should be used by a {@link FullQueryBuilder} and provides methods for
 * transforming tuples into the target type <code>T</code>.
 *
 * @param <T> The type that this builder produces
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface ObjectBuilder<T> {

    /**
     * Applies the select items necessary for this object builder to work.
     *
     * @param selectBuilder The selectBuilder on which to apply the selects
     * @param <X> The type of the select builder
     */
    // TODO: Create a special subtype "ObjectBuilderTarget" that extends SelectBuilder<X> & FetchBuilder<X>
    public <X extends SelectBuilder<X>> void applySelects(X selectBuilder);

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
