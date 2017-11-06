/*
 * Copyright 2014 - 2017 Blazebit.
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
 * @param <C> The plural container type of represented by the change model
 * @param <V> The element type of the container represented by the change model
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface PluralChangeModel<C, V> extends ChangeModel<C> {

    /**
     * Returns the change models of all added, removed or mutated elements.
     *
     * @return The dirty change models
     */
    public List<SingularChangeModel<V>> getElementChanges();

    /**
     * Returns the change models of all added elements.
     *
     * @return The dirty change models
     */
    public List<SingularChangeModel<V>> getAddedElements();

    /**
     * Returns the change models of all removed elements.
     *
     * @return The dirty change models
     */
    public List<SingularChangeModel<V>> getRemovedElements();

    /**
     * Returns the change models of all mutated elements.
     *
     * @return The dirty change models
     */
    public List<SingularChangeModel<V>> getMutatedElements();

    /**
     * Returns the change models for the elements attribute.
     *
     * @param attributePath The name of the attribute or path to sub-attribute
     * @param <X> The element type of the singular attribute
     * @return The change model for the attribute
     * @throws IllegalStateException if invoked on a change model that corresponds to a basic attribute
     * @throws IllegalArgumentException if attribute of the given name does not otherwise exist
     */
    public <X> List<? extends ChangeModel<X>> get(String attributePath);

    /**
     * Returns the change models for the elements attribute.
     *
     * @param attribute The singular attribute
     * @param <X> The element type of the singular attribute
     * @return The change model for the attribute
     */
    public <X> List<SingularChangeModel<X>> get(SingularAttribute<V, X> attribute);

    /**
     * Returns the change models for the elements attribute.
     *
     * @param attribute The collection attribute
     * @param <C> The container type of the plural attribute
     * @param <E> The value type of the plural attribute
     * @return The change model for the attribute
     */
    public <E, C extends java.util.Collection<E>> List<PluralChangeModel<C, E>> get(PluralAttribute<V, C, E> attribute);

    /**
     * Returns the change models for the elements attribute.
     *
     * @param attribute The map attribute
     * @param <K> The key type of the map attribute
     * @param <E> The value type of the map attribute
     * @return The change model for the attribute
     */
    public <K, E> List<MapChangeModel<K, E>> get(MapAttribute<V, K, E> attribute);

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
     * This method always returns <code>true</code> when the collection was altered i.e. objects removed or added.
     * In all other cases, this method returns <code>false</code>.
     *
     * @param attributePath The name of the attribute or path to sub-attribute
     * @return True if the attribute was changed, false otherwise
     * @throws IllegalStateException if invoked on a change model that corresponds to a basic attribute
     * @throws IllegalArgumentException if attribute of the given name does not otherwise exist
     */
    public boolean isChanged(String attributePath);
}
