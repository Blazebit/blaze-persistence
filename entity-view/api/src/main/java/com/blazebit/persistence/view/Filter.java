package com.blazebit.persistence.view;

import com.blazebit.persistence.RestrictionBuilder;

/**
 * A filter is an object that applies a restriction on a {@link RestrictionBuilder}.
 *
 * Filters must have a constructor that accepts either of the following parameter types
 * <ul>
 * <li>none</li>
 * <li>{@linkplain Class}</li>
 * <li>{@linkplain Object}</li>
 * <li>{@linkplain Class}, {@linkplain Object}</li>
 * </ul>
 *
 * @author Christian Beikov
 * @since 1.0
 */
public interface Filter {

    /**
     * Applies a restriction on the given restriction builder.
     *
     * @param <T>                The actual type of the restriction builder
     * @param restrictionBuilder The restriction builder
     * @return The object which the restriction builder returns on a terminal operation
     */
    public <T> T apply(RestrictionBuilder<T> restrictionBuilder);
}
