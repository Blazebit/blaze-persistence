/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.spi;

/**
 * Mapping of an entity view constructor parameter attribute.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface EntityViewParameterMapping extends EntityViewAttributeMapping {

    /**
     * Returns the mapping of the constructor declaring this attribute.
     *
     * @return The declaring constructor mapping
     */
    public EntityViewConstructorMapping getDeclaringConstructor();

    /**
     * Returns the 0-based index of the parameter represented by this parameter mapping.
     *
     * @return The 0-based parameter index
     */
    public int getIndex();
    
}
