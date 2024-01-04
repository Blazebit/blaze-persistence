/*
 * Copyright 2014 - 2024 Blazebit.
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

package com.blazebit.persistence.view;

import com.blazebit.persistence.view.metamodel.CollectionAttribute;
import com.blazebit.persistence.view.metamodel.ListAttribute;
import com.blazebit.persistence.view.metamodel.MapAttribute;
import com.blazebit.persistence.view.metamodel.PluralAttribute;
import com.blazebit.persistence.view.metamodel.SetAttribute;
import com.blazebit.persistence.view.metamodel.SingularAttribute;

/**
 * A builder for creating an entity view.
 *
 * @param <ViewType> The entity view type that is built
 * @param <BuilderType> The entity view builder type
 * @author Christian Beikov
 * @since 1.5.0
 */
public interface EntityViewBuilderBase<ViewType, BuilderType extends EntityViewBuilderBase<ViewType, BuilderType>> {

    /**
     * Sets the given value for the given attribute.
     *
     * @param attribute The attribute name
     * @param value     The value
     * @return this builder for chaining
     */
    BuilderType with(String attribute, Object value);

    /**
     * Sets the given value for the given constructor parameter.
     *
     * @param parameterIndex The constructor parameter index
     * @param value          The value
     * @return this builder for chaining
     */
    BuilderType with(int parameterIndex, Object value);

    /**
     * Sets the given value for the attribute.
     *
     * @param attribute The singular attribute
     * @param value     The value
     * @param <E>       The attribute type
     * @return this builder for chaining
     */
    <E> BuilderType with(SingularAttribute<ViewType, E> attribute, E value);

    /**
     * Sets the given value for the attribute.
     *
     * @param attribute The singular attribute
     * @param value     The value
     * @param <C>       The attribute type
     * @return this builder for chaining
     */
    <C> BuilderType with(PluralAttribute<ViewType, C, ?> attribute, C value);

    /**
     * Returns the value for the given attribute.
     *
     * @param attribute The attribute name
     * @param <E>       The attribute type
     * @return the value for the attribute
     */
    <E> E get(String attribute);

    /**
     * Returns the value for the given constructor parameter.
     *
     * @param parameterIndex The constructor parameter index
     * @param <E>            The attribute type
     * @return the value for the constructor parameter
     */
    <E> E get(int parameterIndex);

    /**
     * Returns the value for the given attribute.
     *
     * @param attribute The attribute
     * @param <E>       The attribute type
     * @return The value
     */
    <E> E get(SingularAttribute<ViewType, E> attribute);

    /**
     * Returns the value for the given attribute.
     *
     * @param attribute The attribute
     * @param <C>       The attribute type
     * @return The value
     */
    <C> C get(PluralAttribute<ViewType, C, ?> attribute);

    /**
     * Adds the given value as element to the Collection, Set or List attribute for the given attribute name.
     *
     * @param attribute The attribute name
     * @param value     The value
     * @return this builder for chaining
     */
    BuilderType withElement(String attribute, Object value);

    /**
     * Adds the given value as element to the Collection, Set or List attribute for the given constructor parameter index.
     *
     * @param parameterIndex The constructor parameter index
     * @param value          The value
     * @return this builder for chaining
     */
    BuilderType withElement(int parameterIndex, Object value);

    /**
     * Adds the given value as element at the given index to the List attribute for the given attribute name.
     *
     * @param attribute The attribute name
     * @param index     The index at which to set the given value
     * @param value     The value
     * @return this builder for chaining
     */
    BuilderType withListElement(String attribute, int index, Object value);

    /**
     * Adds the given value as element at the given index to the List attribute for the given constructor parameter index.
     *
     * @param parameterIndex The constructor parameter index
     * @param index          The index at which to set the given value
     * @param value          The value
     * @return this builder for chaining
     */
    BuilderType withListElement(int parameterIndex, int index, Object value);

    /**
     * Adds the given key and value to the Map attribute for the given attribute name.
     *
     * @param attribute The attribute name
     * @param key       The key
     * @param value     The value
     * @return this builder for chaining
     */
    BuilderType withEntry(String attribute, Object key, Object value);

    /**
     * Adds the given key and value to the Map attribute for the given constructor parameter index.
     *
     * @param parameterIndex The constructor parameter index
     * @param key            The key
     * @param value          The value
     * @return this builder for chaining
     */
    BuilderType withEntry(int parameterIndex, Object key, Object value);

    /**
     * Adds the given value as element to the Collection for the given attribute.
     *
     * @param attribute The attribute
     * @param value     The value
     * @param <E>       The element type
     * @return this builder for chaining
     */
    <E> BuilderType withElement(CollectionAttribute<ViewType, E> attribute, E value);

    /**
     * Adds the given value as element to the Set for the given attribute.
     *
     * @param attribute The attribute
     * @param value     The value
     * @param <E>       The element type
     * @return this builder for chaining
     */
    <E> BuilderType withElement(SetAttribute<ViewType, E> attribute, E value);

    /**
     * Adds the given value as element to the List for the given attribute.
     *
     * @param attribute The attribute
     * @param value     The value
     * @param <E>       The element type
     * @return this builder for chaining
     */
    <E> BuilderType withElement(ListAttribute<ViewType, E> attribute, E value);

    /**
     * Sets the given value as element on the List at the given index for the given attribute.
     *
     * @param attribute The attribute
     * @param index     The index at which to set the given value
     * @param value     The value
     * @param <E>       The element type
     * @return this builder for chaining
     */
    <E> BuilderType withListElement(ListAttribute<ViewType, E> attribute, int index, E value);

    /**
     * Sets the given value as element on the Map for the given key for the given attribute.
     *
     * @param attribute The attribute
     * @param key       The key for which to set the given value
     * @param value     The value
     * @param <K>       The key type
     * @param <V>       The element type
     * @return this builder for chaining
     */
    <K, V> BuilderType withEntry(MapAttribute<ViewType, K, V> attribute, K key, V value);

    /**
     * Starts and returns a nested entity view builder for the singular attribute for the given attribute name.
     *
     * @param attribute The attribute name
     * @param <E>       The attribute type
     * @return the nested builder
     */
    <E> EntityViewNestedBuilder<E, ? extends BuilderType, ?> withSingularBuilder(String attribute);

    /**
     * Starts and returns a nested entity view builder for the singular attribute for the given constructor parameter index.
     *
     * @param parameterIndex The constructor parameter index
     * @param <E>            The attribute type
     * @return the nested builder
     */
    <E> EntityViewNestedBuilder<E, ? extends BuilderType, ?> withSingularBuilder(int parameterIndex);

    /**
     * Starts and returns a nested entity view builder for the Collection, Set or List attribute for the given attribute name.
     *
     * @param attribute The attribute name
     * @param <E>       The element type
     * @return the nested builder
     */
    <E> EntityViewNestedBuilder<E, ? extends BuilderType, ?> withCollectionBuilder(String attribute);

    /**
     * Starts and returns a nested entity view builder for the Collection, Set or List attribute for the given constructor parameter index.
     *
     * @param parameterIndex The constructor parameter index
     * @param <E>            The element type
     * @return the nested builder
     */
    <E> EntityViewNestedBuilder<E, ? extends BuilderType, ?> withCollectionBuilder(int parameterIndex);

    /**
     * Starts and returns a nested entity view builder for the List attribute for the given attribute name.
     *
     * @param attribute The attribute name
     * @param <E>       The element type
     * @return the nested builder
     */
    <E> EntityViewNestedBuilder<E, ? extends BuilderType, ?> withListBuilder(String attribute);

    /**
     * Starts and returns a nested entity view builder for the List attribute for the given constructor parameter index.
     *
     * @param parameterIndex The constructor parameter index
     * @param <E>            The element type
     * @return the nested builder
     */
    <E> EntityViewNestedBuilder<E, ? extends BuilderType, ?> withListBuilder(int parameterIndex);

    /**
     * Starts and returns a nested entity view builder for the List attribute for the given attribute name.
     * After the nested builder is finished, the built object is added to the list at the given index.
     *
     * @param attribute The attribute name
     * @param index     The index in the list at which to set the element
     * @param <E>       The element type
     * @return the nested builder
     */
    <E> EntityViewNestedBuilder<E, ? extends BuilderType, ?> withListBuilder(String attribute, int index);

    /**
     * Starts and returns a nested entity view builder for the List attribute for the given constructor parameter index.
     * After the nested builder is finished, the built object is added to the list at the given index.
     *
     * @param parameterIndex The constructor parameter index
     * @param index          The index in the list at which to set the element
     * @param <E>            The element type
     * @return the nested builder
     */
    <E> EntityViewNestedBuilder<E, ? extends BuilderType, ?> withListBuilder(int parameterIndex, int index);

    /**
     * Starts and returns a nested entity view builder for the Set attribute for the given attribute name.
     *
     * @param attribute The attribute name
     * @param <E>       The element type
     * @return the nested builder
     */
    <E> EntityViewNestedBuilder<E, ? extends BuilderType, ?> withSetBuilder(String attribute);

    /**
     * Starts and returns a nested entity view builder for the Set attribute for the given constructor parameter index.
     *
     * @param parameterIndex The constructor parameter index
     * @param <E>            The element type
     * @return the nested builder
     */
    <E> EntityViewNestedBuilder<E, ? extends BuilderType, ?> withSetBuilder(int parameterIndex);

    /**
     * Starts and returns a nested entity view builder for the Map attribute for the given attribute name.
     * After the nested builder is finished, the built object is put into the map with the given key.
     *
     * @param attribute The attribute name
     * @param key       The map key for which to set the element for
     * @param <V>       The element type
     * @return the nested builder
     */
    <V> EntityViewNestedBuilder<V, ? extends BuilderType, ?> withMapBuilder(String attribute, Object key);

    /**
     * Starts and returns a nested entity view builder for the Map attribute for the given constructor parameter index.
     * After the nested builder is finished, the built object is put into the map with the given key.
     *
     * @param parameterIndex The constructor parameter index
     * @param key            The map key for which to set the element for
     * @param <V>            The element type
     * @return the nested builder
     */
    <V> EntityViewNestedBuilder<V, ? extends BuilderType, ?> withMapBuilder(int parameterIndex, Object key);

    /**
     * Starts and returns a nested entity view builder for the Map attribute for the given attribute name.
     * The returned builder is for the key subview. The builder returned by that, is for the value subview.
     *
     * @param attribute The attribute name
     * @param <K>       The key type
     * @param <V>       The element type
     * @return the nested builder
     */
    <K, V> EntityViewNestedBuilder<K, ? extends EntityViewNestedBuilder<V, ? extends BuilderType, ?>, ?> withMapBuilder(String attribute);

    /**
     * Starts and returns a nested entity view builder for the Map attribute for the given constructor parameter index.
     * The returned builder is for the key subview. The builder returned by that, is for the value subview.
     *
     * @param parameterIndex The constructor parameter index
     * @param <K>            The key type
     * @param <V>            The element type
     * @return the nested builder
     */
    <K, V> EntityViewNestedBuilder<K, ? extends EntityViewNestedBuilder<V, ? extends BuilderType, ?>, ?> withMapBuilder(int parameterIndex);

    /**
     * Starts and returns a nested entity view builder for the given singular attribute.
     *
     * @param attribute The attribute
     * @param <E>       The attribute type
     * @return the nested builder
     */
    <E> EntityViewNestedBuilder<E, ? extends BuilderType, ?> withBuilder(SingularAttribute<ViewType, E> attribute);

    /**
     * Starts and returns a nested entity view builder for the given Collection attribute.
     *
     * @param attribute The attribute
     * @param <E>       The element type
     * @return the nested builder
     */
    <E> EntityViewNestedBuilder<E, ? extends BuilderType, ?> withBuilder(CollectionAttribute<ViewType, E> attribute);

    /**
     * Starts and returns a nested entity view builder for the given List attribute.
     *
     * @param attribute The attribute
     * @param <E>       The element type
     * @return the nested builder
     */
    <E> EntityViewNestedBuilder<E, ? extends BuilderType, ?> withBuilder(ListAttribute<ViewType, E> attribute);

    /**
     * Starts and returns a nested entity view builder for the given List attribute.
     * After the nested builder is finished, the built object is added to the list at the given index.
     *
     * @param attribute The attribute
     * @param index     The index in the list at which to set the element
     * @param <E>       The element type
     * @return the nested builder
     */
    <E> EntityViewNestedBuilder<E, ? extends BuilderType, ?> withBuilder(ListAttribute<ViewType, E> attribute, int index);

    /**
     * Starts and returns a nested entity view builder for the Set attribute.
     *
     * @param attribute The attribute
     * @param <E>       The element type
     * @return the nested builder
     */
    <E> EntityViewNestedBuilder<E, ? extends BuilderType, ?> withBuilder(SetAttribute<ViewType, E> attribute);

    /**
     * Starts and returns a nested entity view builder for the given Map attribute.
     * After the nested builder is finished, the built object is put into the map with the given key.
     *
     * @param attribute The attribute
     * @param key       The map key for which to set the element for
     * @param <K>       The key type
     * @param <V>       The element type
     * @return the nested builder
     */
    <K, V> EntityViewNestedBuilder<V, ? extends BuilderType, ?> withBuilder(MapAttribute<ViewType, K, V> attribute, K key);

    /**
     * Starts and returns a nested entity view builder for the given Map attribute.
     * The returned builder is for the key subview. The builder returned by that, is for the value subview.
     *
     * @param attribute The attribute
     * @param <K>       The key type
     * @param <V>       The element type
     * @return the nested builder
     */
    <K, V> EntityViewNestedBuilder<K, ? extends EntityViewNestedBuilder<V, ? extends BuilderType, ?>, ?> withBuilder(MapAttribute<ViewType, K, V> attribute);
}
