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

package com.blazebit.persistence.view.impl.collection;

import com.blazebit.persistence.view.impl.entity.MapViewToEntityMapper;
import com.blazebit.persistence.view.impl.update.UpdateContext;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface MapAction<T extends Map<?, ?>> {

    public void doAction(T map, UpdateContext context, MapViewToEntityMapper mapper, CollectionRemoveListener keyRemoveListener, CollectionRemoveListener valueRemoveListener);

    public Collection<Object> getAddedKeys();

    public Collection<Object> getRemovedKeys();

    public Collection<Object> getAddedElements();

    public Collection<Object> getRemovedElements();

    public Collection<Object> getAddedKeys(T collection);

    public Collection<Object> getRemovedKeys(T collection);

    public Collection<Object> getAddedElements(T collection);

    public Collection<Object> getRemovedElements(T collection);

    public MapAction<T> replaceObject(Object oldKey, Object oldValue, Object newKey, Object newValue);

    public void addAction(List<MapAction<T>> actions, Collection<Object> addedKeys, Collection<Object> removedKeys, Collection<Object> addedElements, Collection<Object> removedElements);
}
