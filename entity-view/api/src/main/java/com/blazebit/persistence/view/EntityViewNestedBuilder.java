/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view;

/**
 * A builder for creating nested entity views.
 *
 * @param <ViewType> The entity view type that is built
 * @param <ResultType> The type to return when this builder finishes
 * @param <BuilderType> The entity view builder type
 * @author Christian Beikov
 * @since 1.5.0
 */
public interface EntityViewNestedBuilder<ViewType, ResultType, BuilderType extends EntityViewNestedBuilder<ViewType, ResultType, BuilderType>> extends EntityViewBuilderBase<ViewType, BuilderType> {
    /**
     * Finishes this builder, associates the built object with the parent object and returns the next builder.
     *
     * @return The next builder
     */
    ResultType build();
}