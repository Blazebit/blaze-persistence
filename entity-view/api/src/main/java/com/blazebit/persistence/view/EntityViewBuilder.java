/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view;

/**
 * A builder for defining flush related configuration.
 *
 * @param <ViewType> The entity view type that is built
 * @author Christian Beikov
 * @since 1.5.0
 */
public interface EntityViewBuilder<ViewType> extends EntityViewBuilderBase<ViewType, EntityViewBuilder<ViewType>> {

    /**
     * Builds the entity view and returns it.
     *
     * @return The built entity view
     */
    ViewType build();
}