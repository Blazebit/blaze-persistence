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
import com.blazebit.persistence.view.impl.collection.RecordingList;
import com.blazebit.persistence.view.spi.type.TypeConverter;

import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class IndexedListTupleListTransformer extends AbstractIndexedTupleListTransformer<List<Object>, Integer> {

    private final CollectionInstantiator collectionInstantiator;
    private final boolean dirtyTracking;

    public IndexedListTupleListTransformer(int[] parentIdPositions, int startIndex, CollectionInstantiator collectionInstantiator, boolean dirtyTracking, TypeConverter<Object, Object> valueConverter) {
        super(parentIdPositions, startIndex, startIndex + 1, null, valueConverter);
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
    protected void addToCollection(List<Object> collection, Integer index, Object value) {
        final List<Object> list;
        if (dirtyTracking) {
            list = ((RecordingList<Object>) collection).getDelegate();
        } else {
            list = collection;
        }
        if (index < list.size()) {
            list.set(index, value);
        } else if (index > list.size()) {
            for (int i = index - list.size(); i <= index; i++) {
                list.add(null);
            }
            
            list.add(index, value);
        } else {
            list.add(index, value);
        }
    }

}