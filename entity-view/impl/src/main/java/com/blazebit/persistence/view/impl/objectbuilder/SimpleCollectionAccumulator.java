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

package com.blazebit.persistence.view.impl.objectbuilder;

import com.blazebit.persistence.view.impl.collection.PluralObjectFactory;

import java.util.Collection;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public class SimpleCollectionAccumulator implements ContainerAccumulator<Collection<Object>>  {

    private final PluralObjectFactory<Collection<Object>> pluralObjectFactory;

    public SimpleCollectionAccumulator(PluralObjectFactory<? extends Collection<?>> pluralObjectFactory) {
        this.pluralObjectFactory = (PluralObjectFactory<Collection<Object>>) pluralObjectFactory;
    }

    @Override
    public Collection<Object> createContainer(boolean recording, int size) {
        return pluralObjectFactory.createCollection(size);
    }

    @Override
    public void add(Collection<Object> container, Object indexObject, Object value, boolean recording) {
        container.add(value);
    }

    @Override
    public void addAll(Collection<Object> container, Collection<Object> collection, boolean recording) {
        container.addAll(collection);
    }

    @Override
    public boolean requiresPostConstruct() {
        return false;
    }

    @Override
    public void postConstruct(Collection<Object> collection) {
    }
}
