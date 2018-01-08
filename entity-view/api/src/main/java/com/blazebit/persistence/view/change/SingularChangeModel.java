/*
 * Copyright 2014 - 2018 Blazebit.
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

package com.blazebit.persistence.view.change;

import com.blazebit.persistence.view.metamodel.MapAttribute;
import com.blazebit.persistence.view.metamodel.PluralAttribute;
import com.blazebit.persistence.view.metamodel.SingularAttribute;

import java.util.List;

/**
 * An interface for accessing the dirty state of an object.
 *
 * @param <E> The element type represented by the change model
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface SingularChangeModel<E> extends ChangeModel<E> {

    /**
     * Returns the change models of the dirty nested attributes.
     *
     * @return The dirty change models
     * @throws IllegalStateException if invoked on a change model that corresponds to a basic attribute
     */
    public List<ChangeModel<?>> getDirtyChanges();

    /**
     * Returns the change model for the attribute.
     *
     * @param attributePath The name of the attribute or path to sub-attribute
     * @param <X> The element type of the target attribute path
     * @return The change model for the attribute
     * @throws IllegalStateException if invoked on a change model that corresponds to a basic attribute
     * @throws IllegalArgumentException if attribute of the given name does not otherwise exist
     */
    public <X> ChangeModel<X> get(String attributePath);

    /**
     * Returns the change model for the attribute.
     *
     * @param attributePath The name of the attribute or path to sub-attribute
     * @param <X> The element type of the target attribute path
     * @return The change model for the attribute
     * @throws IllegalStateException if invoked on a change model that corresponds to a basic attribute
     * @throws IllegalArgumentException if attribute of the given name does not otherwise exist
     */
    public <X> List<? extends ChangeModel<X>> getAll(String attributePath);

    /**
     * Returns the change model for the attribute.
     *
     * @param attribute The singular attribute
     * @param <X> The element type of the singular attribute
     * @return The change model for the attribute
     */
    public <X> SingularChangeModel<X> get(SingularAttribute<E, X> attribute);

    /**
     * Returns the change model for the attribute.
     *
     * @param attribute The collection attribute
     * @param <C> The container type of the plural attribute
     * @param <V> The value type of the plural attribute
     * @return The change model for the attribute
     */
    public <V, C extends java.util.Collection<V>> PluralChangeModel<C, V> get(PluralAttribute<E, C, V> attribute);

    /**
     * Returns the change model for the attribute.
     *
     * @param attribute The map attribute
     * @param <K> The key type of the map attribute
     * @param <V> The value type of the map attribute
     * @return The change model for the attribute
     */
    public <K, V> MapChangeModel<K, V> get(MapAttribute<E, K, V> attribute);

    /**
     * Returns whether the change model for the attribute is dirty.
     * Essentially, this is a more efficient short-hand for <code>get(attributePath).isDirty()</code>.
     *
     * @param attributePath The name of the attribute or path to sub-attribute
     * @return True if the change model for the attribute path is dirty, false otherwise
     * @throws IllegalStateException if invoked on a change model that corresponds to a basic attribute
     * @throws IllegalArgumentException if attribute of the given name does not otherwise exist
     */
    public boolean isDirty(String attributePath);

    /**
     * Returns whether the target attribute path was changed by updating or mutating it,
     * but still has the same <i>identity</i> regarding parent objects it is contained in.
     * If any of the parent objects as denoted by the attribute path are updated i.e. the identity was changed,
     * this returns <code>true</code>. Mutations or updates to the target object also cause <code>true</code> to be returned.
     * In all other cases, this method returns <code>false</code>.
     *
     * @param attributePath The name of the attribute or path to sub-attribute
     * @return True if the attribute was changed, false otherwise
     * @throws IllegalStateException if invoked on a change model that corresponds to a basic attribute
     * @throws IllegalArgumentException if attribute of the given name does not otherwise exist
     */
    public boolean isChanged(String attributePath);
}
