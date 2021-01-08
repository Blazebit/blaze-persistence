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

package com.blazebit.persistence.view;

/**
 * A builder for creating nested entity views.
 *
 * @param <T> The entity view type that is built
 * @param <X> The entity view builder type to return
 * @author Christian Beikov
 * @since 1.5.0
 */
public interface EntityViewNestedBuilder<T, X> extends EntityViewBuilderBase<T, EntityViewNestedBuilder<T, X>> {
    /**
     * Finishes this builder, associates the built object with the parent object and returns the next builder.
     *
     * @return The next builder
     */
    X build();
}