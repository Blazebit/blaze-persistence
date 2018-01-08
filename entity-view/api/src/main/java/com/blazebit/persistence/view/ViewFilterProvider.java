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

package com.blazebit.persistence.view;

import com.blazebit.persistence.WhereBuilder;

/**
 * A view filter provider is an object that applies restrictions on a {@link WhereBuilder}.
 * 
 * View filter providers must have a no-arg constructor if they are used in conjunction with {@link ViewFilter}.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public abstract class ViewFilterProvider {

    /**
     * Applies restrictions on the given where builder.
     *
     * @param <T>                 The actual type of the where builder
     * @param whereBuilder        The where builder on which the restrictions should be applied
     * @return The where builder after applying restrictions
     */
    public abstract <T extends WhereBuilder<T>> T apply(T whereBuilder);
}
