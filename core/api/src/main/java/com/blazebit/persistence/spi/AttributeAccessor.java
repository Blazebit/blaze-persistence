/*
 * Copyright 2014 - 2021 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
