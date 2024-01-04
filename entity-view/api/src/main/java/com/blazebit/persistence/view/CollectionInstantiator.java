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

import java.util.Collection;

/**
 * An instantiator for normal, recording and JPA collections for an entity view attribute.
 *
 * @param <C> The collection type
 * @param <R> The recording container type
 * @author Christian Beikov
 * @since 1.5.0
 */
public interface CollectionInstantiator<C extends Collection<?>, R extends Collection<?> & RecordingContainer<? extends C>> {

    /**
     * Creates a plain collection.
     *
     * @param size The size estimate
     * @return the collection
     */
    public C createCollection(int size);

    /**
     * Creates a recording collection.
     *
     * @param size The size estimate
     * @return the recording collection
     */
    public R createRecordingCollection(int size);
}