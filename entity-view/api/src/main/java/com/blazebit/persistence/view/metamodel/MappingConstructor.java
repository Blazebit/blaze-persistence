/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.metamodel;

import java.lang.reflect.Constructor;
import java.util.List;

/**
 * Represents a constructor of a view type.
 *
 * @param <X> The type of the declaring entity view
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface MappingConstructor<X> {

    /**
     * Returns the name of the constructor.
     *
     * @return The name of the constructor
     */
    public String getName();

    /**
     * Returns the declaring managed view type.
     *
     * @return The declaring managed view type
     */
    public ManagedViewType<X> getDeclaringType();

    /**
     * Returns the java constructor for this mapping constructor.
     *
     * @return The java constructor for this mapping constructor
     */
    public Constructor<X> getJavaConstructor();

    /**
     * Returns the parameter attributes of this mapping constructor.
     *
     * @return The parameter attributes of this mapping constructor
     */
    public List<ParameterAttribute<? super X, ?>> getParameterAttributes();

    /**
     * Returns the parameter attribute of this mapping constructor at the given index if it exists, otherwise null.
     *
     * @param index The index at which the parameter is located
     * @return The parameter attribute of this mapping constructor at the given index if it exists, otherwise null.
     */
    public ParameterAttribute<? super X, ?> getParameterAttribute(int index);
}
