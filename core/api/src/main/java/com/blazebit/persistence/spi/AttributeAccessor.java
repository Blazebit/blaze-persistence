/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spi;

/**
 * A class to access the attribute of an entity.
 *
 * @param <X> The entity type
 * @param <Y> The attribute type
 * @author Christian Beikov
 * @since 1.4.1
 */
public interface AttributeAccessor<X, Y>  {

    /**
     * Returns the attribute value of the given entity.
     *
     * @param entity The entity
     * @return the attribute value
     */
    public Y get(X entity);

    /**
     * Returns the attribute value of the given entity or null if the entity is null.
     *
     * @param entity The entity
     * @return the attribute value or null if the entity is null
     */
    public Y getNullSafe(X entity);

    /**
     * Sets the attribute to the given value on the given entity.
     *
     * @param entity The entity
     * @param value the attribute value
     */
    public void set(X entity, Y value);
}
