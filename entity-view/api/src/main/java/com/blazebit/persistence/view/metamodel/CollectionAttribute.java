/*
 * Copyright 2014 - 2020 Blazebit.
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

package com.blazebit.persistence.view.metamodel;

import com.blazebit.persistence.view.CollectionInstantiator;
import com.blazebit.persistence.view.RecordingContainer;

import java.util.Collection;

/**
 * Instances of the type {@linkplain CollectionAttribute} represent {@linkplain java.util.Collection}-valued attributes.
 *
 * @param <X> The type of the declaring entity view
 * @param <E> The element type of the represented Collection
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface CollectionAttribute<X, E> extends PluralAttribute<X, Collection<E>, E> {

    /**
     * Returns the collection instantiator for this attribute.
     *
     * @param <R> The recording collection type
     * @return The collection instantiator
     * @since 1.5.0
     */
    public <R extends Collection<E> & RecordingContainer<? extends Collection<E>>> CollectionInstantiator<Collection<E>, R> getCollectionInstantiator();
}
