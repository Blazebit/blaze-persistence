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

package com.blazebit.persistence.view.metamodel;

import com.blazebit.persistence.spi.ServiceProvider;
import com.blazebit.persistence.view.CollectionInstantiator;
import com.blazebit.persistence.view.RecordingContainer;

import java.util.List;

/**
 * Instances of the type {@linkplain ListAttribute} represent {@linkplain java.util.List}-valued attributes.
 *
 * @param <X> The type of the declaring entity view
 * @param <E> The element type of the represented List
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface ListAttribute<X, E> extends PluralAttribute<X, List<E>, E> {

    /**
     * Returns the collection instantiator for this attribute.
     *
     * @param <R> The recording collection type
     * @return The collection instantiator
     * @since 1.5.0
     */
    public <R extends List<E> & RecordingContainer<? extends List<E>>> CollectionInstantiator<List<E>, R> getCollectionInstantiator();

    /**
     * Returns the index mapping of the attribute.
     *
     * @return The index mapping of the attribute
     * @since 1.5.0
     */
    public String getIndexMapping();

    /**
     * Renders the index mapping for the given parent expression to the given string builder.
     *
     * @param parent The parent expression
     * @param serviceProvider The service provider
     * @param sb The string builder
     * @since 1.5.0
     */
    public void renderIndexMapping(String parent, ServiceProvider serviceProvider, StringBuilder sb);
}
