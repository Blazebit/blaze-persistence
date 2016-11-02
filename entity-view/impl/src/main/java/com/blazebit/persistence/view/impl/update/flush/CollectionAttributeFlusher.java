/*
 * Copyright 2014 - 2016 Blazebit.
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

package com.blazebit.persistence.view.impl.update.flush;

import java.util.Collection;

import javax.persistence.Query;

import com.blazebit.persistence.view.impl.collection.RecordingCollection;
import com.blazebit.reflection.PropertyPathExpression;

public class CollectionAttributeFlusher<E, V extends Collection<?>> implements DirtyAttributeFlusher<E, V> {

    private final PropertyPathExpression<E, Collection<?>> propertyPath;
    
    @SuppressWarnings("unchecked")
    public CollectionAttributeFlusher(PropertyPathExpression<E, ? extends Collection<?>> propertyPath) {
        this.propertyPath = (PropertyPathExpression<E, Collection<?>>) propertyPath;
    }

    @Override
    public boolean supportsQueryFlush() {
        return false;
    }

    @Override
    public void flushQuery(Query query, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void flushEntity(E entity, V value) {
        if (value instanceof RecordingCollection<?, ?>) {
            ((RecordingCollection<Collection<?>, ?>) value).replay(propertyPath.getValue(entity));
        } else {
            propertyPath.setValue(entity, value);
        }
    }
}
