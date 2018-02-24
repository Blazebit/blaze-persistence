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

package com.blazebit.persistence.view.impl.objectbuilder.transformer;

import com.blazebit.persistence.view.impl.collection.CollectionInstantiator;
import com.blazebit.persistence.view.impl.collection.RecordingCollection;
import com.blazebit.persistence.view.spi.type.TypeConverter;

import java.util.Collection;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class CollectionTupleListTransformer extends AbstractNonIndexedTupleListTransformer<Collection<Object>> {

    private final CollectionInstantiator collectionInstantiator;
    private final boolean dirtyTracking;

    public CollectionTupleListTransformer(int[] parentIdPositions, int startIndex, CollectionInstantiator collectionInstantiator, boolean dirtyTracking, TypeConverter<Object, Object> elementConverter) {
        super(parentIdPositions, startIndex, elementConverter);
        this.collectionInstantiator = collectionInstantiator;
        this.dirtyTracking = dirtyTracking;
    }
    
    @Override
    protected Object createCollection() {
        if (dirtyTracking) {
            return collectionInstantiator.createRecordingCollection(0);
        } else {
            return collectionInstantiator.createCollection(0);
        }
    }

    @Override
    protected void addToCollection(Collection<Object> collection, Object value) {
        if (dirtyTracking) {
            ((RecordingCollection<?, Object>) collection).getDelegate().add(value);
        } else {
            collection.add(value);
        }
    }

}
