/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.spi;

import java.lang.reflect.Constructor;
import java.util.List;

/**
 * Mapping of an entity view constructor.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface EntityViewConstructorMapping {

    /**
     * Returns the mapping of the view declaring this constructor.
     *
     * @return The declaring view mapping
     */
    public EntityViewMapping getDeclaringView();

    /**
     * Returns the name of the view constructor.
     *
     * @return The view constructor name
     */
    public String getName();

    /**
     * Returns the constructor object of the declaring view java type represented by this mapping.
     *
     * @return The constructor represented by this mapping
     */
    public Constructor<?> getConstructor();

    /**
     * Returns the parameter mappings of this constructor mapping.
     *
     * @return The parameter mappings of this constructor mapping
     */
    List<EntityViewParameterMapping> getParameters();
}
