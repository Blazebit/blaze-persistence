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

package com.blazebit.persistence.view.impl.collection;

import com.blazebit.persistence.view.CollectionInstantiator;

import java.util.Collection;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface CollectionInstantiatorImplementor<C extends Collection<?>, R extends RecordingCollection<? extends C, ?>> extends CollectionInstantiator<C, R> {

    /**
     * Returns whether the collection type allows duplicates.
     *
     * @return whether the collection type allows duplicates
     */
    public boolean allowsDuplicates();

    /**
     * Returns whether the collection type is indexed.
     *
     * @return whether the collection type is indexed
     */
    public boolean isIndexed();

    /**
     * Returns whether the collection requires a post construct call.
     *
     * @return whether the collection requires a post construct call
     */
    public boolean requiresPostConstruct();

    /**
     * Invokes the post construct action on the collection.
     *
     * @param collection The collection
     */
    public void postConstruct(Collection<?> collection);

    /**
     * Creates a collection for the JPA model.
     *
     * @param size The size estimate
     * @return the collection
     */
    public Collection<?> createJpaCollection(int size);
}
