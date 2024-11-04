/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.metamodel;

import java.util.Set;

/**
 * Provides access to the metamodel of the entity views.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface ViewMetamodel {

    /**
     * Returns the metamodel entity view type representing the entity view specified by the given class or null.
     *
     * @param <X>   The type of the given entity view class
     * @param clazz The entity view class for which the view type should be returned
     * @return The metamodel entity view type or null
     */
    public <X> ViewType<X> view(Class<X> clazz);

    /**
     * Returns the metamodel entity view type representing the entity view specified by the given class or throws an exception.
     *
     * @param <X>   The type of the given entity view class
     * @param clazz The entity view class for which the view type should be returned
     * @return The metamodel entity view type
     * @throws IllegalArgumentException if the class is not a view type
     * @since 1.5.0
     */
    public <X> ViewType<X> viewOrError(Class<X> clazz);

    /**
     * Returns the metamodel entity views.
     *
     * @return The metamodel entity views
     */
    public Set<ViewType<?>> getViews();

    /**
     * Returns the metamodel managed entity view type representing the managed entity view specified by the given class or null.
     *
     * @param <X>   The type of the given entity view class
     * @param clazz The entity view class for which the view type should be returned
     * @return The metamodel entity view type or null
     */
    public <X> ManagedViewType<X> managedView(Class<X> clazz);

    /**
     * Returns the metamodel managed entity view type representing the managed entity view specified by the given class or throws an exception.
     *
     * @param <X>   The type of the given entity view class
     * @param clazz The entity view class for which the view type should be returned
     * @return The metamodel entity view type
     * @throws IllegalArgumentException if the class is not a view type
     * @since 1.5.0
     */
    public <X> ManagedViewType<X> managedViewOrError(Class<X> clazz);

    /**
     * Returns the metamodel managed entity views.
     *
     * @return The metamodel managed entity views
     */
    public Set<ManagedViewType<?>> getManagedViews();

    /**
     * Returns the metamodel embeddable entity view type representing the embeddable entity view specified by the given class or null.
     *
     * @param <X>   The type of the given entity view class
     * @param clazz The entity view class for which the view type should be returned
     * @return The metamodel entity view type or null
     */
    public <X> FlatViewType<X> flatView(Class<X> clazz);

    /**
     * Returns the metamodel embeddable entity view type representing the embeddable entity view specified by the given class or throws an exception.
     *
     * @param <X>   The type of the given entity view class
     * @param clazz The entity view class for which the view type should be returned
     * @return The metamodel entity view type
     * @throws IllegalArgumentException if the class is not a view type
     * @since 1.5.0
     */
    public <X> FlatViewType<X> flatViewOrError(Class<X> clazz);

    /**
     * Returns the metamodel embeddableentity views.
     *
     * @return The metamodel embeddableentity views
     */
    public Set<FlatViewType<?>> getFlatViews();
}
