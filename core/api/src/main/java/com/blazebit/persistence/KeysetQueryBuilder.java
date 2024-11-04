/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

import java.io.Serializable;

/**
 * A base interface for builders that support keyset filtering.
 *
 * @param <X> The concrete builder type
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface KeysetQueryBuilder<X extends KeysetQueryBuilder<X>> {

    /**
     * Uses the keyset which the keyset builder constructed to filter out rows that come after the keyset.
     * Based on the order by expressions, the keyset builder should receive reference values for every used expression.
     * The constructed keyset will be filtered out so this is like a "lower than" filter.
     * 
     * @return The keyset builder for specifing the keyset
     */
    public KeysetBuilder<X> beforeKeyset();

    /**
     * Like {@link FullQueryBuilder#beforeKeyset()} but maps the reference values by position instead of by expression.
     * The order of the reference values has to match the order of the order by expressions.
     * 
     * @param values The reference values
     * @return The query builder for chaining calls
     */
    public X beforeKeyset(Serializable... values);

    /**
     * Like {@link FullQueryBuilder#beforeKeyset(java.io.Serializable...)} but uses the given keyset as reference values.
     * The order of the tuple values has to match the order of the order by expressions.
     * 
     * @param keyset The reference keyset
     * @return The query builder for chaining calls
     */
    public X beforeKeyset(Keyset keyset);

    /**
     * Uses the keyset which the keyset builder constructed to filter out rows that come before the keyset.
     * Based on the order by expressions, the keyset builder should receive reference values for every used expression.
     * The constructed keyset will be filtered out so this is like a "greater than" filter.
     * 
     * @return The keyset builder for specifing the keyset
     */
    public KeysetBuilder<X> afterKeyset();

    /**
     * Like {@link FullQueryBuilder#afterKeyset()} but maps the reference values by position instead of by expression.
     * The order of the reference values has to match the order of the order by expressions.
     * 
     * @param values The reference values
     * @return The query builder for chaining calls
     */
    public X afterKeyset(Serializable... values);

    /**
     * Like {@link FullQueryBuilder#afterKeyset(java.io.Serializable...)} but uses the given keyset as reference values.
     * The order of the tuple values has to match the order of the order by expressions.
     * 
     * @param keyset The reference keyset
     * @return The query builder for chaining calls
     */
    public X afterKeyset(Keyset keyset);
}
