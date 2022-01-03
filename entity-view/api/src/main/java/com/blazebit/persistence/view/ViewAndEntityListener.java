/*
 * Copyright 2014 - 2022 Blazebit.
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
 * A listener for getting a callback.
 *
 * @param <T> The view type
 * @param <E> The entity type
 * @author Christian Beikov
 * @since 1.4.0
 */
public interface ViewAndEntityListener<T, E> {

    /**
     * A callback that is invoked for a view and entity.
     *
     * @param view The view that is about to be persisted
     * @param entity The entity object that is about to be persisted
     */
    public void call(T view, E entity);
}
