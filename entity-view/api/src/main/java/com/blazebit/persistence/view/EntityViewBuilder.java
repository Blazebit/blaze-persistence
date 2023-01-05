/*
 * Copyright 2014 - 2023 Blazebit.
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