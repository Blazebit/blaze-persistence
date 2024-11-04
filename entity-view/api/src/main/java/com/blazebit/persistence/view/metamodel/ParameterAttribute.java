/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.metamodel;

/**
 * Represents an attribute of a view type specified by a constructor parameter.
 *
 * @param <X> The type of the declaring entity view
 * @param <Y> The type of attribute
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface ParameterAttribute<X, Y> extends Attribute<X, Y> {

    /**
     * Returns the index of the parameter within the constructor.
     *
     * @return The index of the parameter within the constructor
     */
    public int getIndex();

    /**
     * Returns the declaring constructor.
     *
     * @return The declaring constructor
     */
    public MappingConstructor<X> getDeclaringConstructor();

    /**
     * Returns whether the parameter is a "self" parameter i.e. annotated with {@link com.blazebit.persistence.view.Self}.
     *
     * @return Whether the parameter is a self parameter
     * @since 1.5.0
     */
    public boolean isSelfParameter();
}
