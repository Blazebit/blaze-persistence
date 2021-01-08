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

package com.blazebit.persistence.view.impl.objectbuilder.transformer;

import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.blazebit.persistence.view.impl.collection.CollectionInstantiatorImplementor;
import com.blazebit.persistence.view.impl.collection.RecordingCollection;
import com.blazebit.persistence.view.impl.objectbuilder.TupleId;
import com.blazebit.persistence.view.impl.objectbuilder.TupleIndexValue;
import com.blazebit.persistence.view.spi.type.TypeConverter;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class NonIndexedTupleListTransformer extends TupleListTransformer {

    private final int[] parentIdPositions;
    private final CollectionInstantiatorImplementor<?, ?> collectionInstantiator;
    private final boolean dirtyTracking;
    private final TypeConverter<Object, Object> elementConverter;

    public NonIndexedTupleListTransformer(int[] parentIdPositions, int startIndex, CollectionInstantiatorImplementor<?, ?> collectionInstantiator, boolean dirtyTracking, TypeConverter<Object, Object> elementConverter) {
        super(startIndex);
        this.parentIdPositions = parentIdPositions;
        this.collectionInstantiator = collectionInstantiator;
        this.dirtyTracking = dirtyTracking;
        this.elementConverter = elementConverter;
    }

    @Override
    public int getConsumableIndex() {
        return -1;
    }

    @Override
    public List<Object[]> transform(List<Object[]> tuples) {
        Map<TupleId, TupleIndexValue> tupleIndex = new HashMap<>(tuples.size());
        Iterator<Object[]> tupleListIter = tuples.iterator();

        while (tupleListIter.hasNext()) {
            Object[] tuple = tupleListIter.next();
            TupleId id = new TupleId(parentIdPositions, tuple);
            // Skip constructing the collection and removing tuples when the parent is empty i.e. null
            if (!id.isEmpty()) {
                TupleIndexValue tupleIndexValue = tupleIndex.get(id);

                if (tupleIndexValue == null) {
                    Object collection = createCollection();
                    tupleIndexValue = new TupleIndexValue(collection, tuple, startIndex, 1);
                    add(collection, tuple[startIndex]);
                    tuple[startIndex] = collection;
                    tupleIndex.put(id, tupleIndexValue);
                } else if (tupleIndexValue.addRestTuple(tuple, startIndex, 1)) {
                    Object collection = tupleIndexValue.getTupleValue();
                    add(collection, tuple[startIndex]);
                    tuple[startIndex] = collection;
                    // Check if the tuple after the offset is contained
                    if (tupleIndexValue.containsRestTuple(tuple, startIndex, 1)) {
                        tupleListIter.remove();
                    }
                } else {
                    add(tupleIndexValue.getTupleValue(), tuple[startIndex]);
                    tupleListIter.remove();
                }
            }
        }
        if (collectionInstantiator.requiresPostConstruct()) {
            IdentityHashMap<Collection<?>, Boolean> handledCollections = new IdentityHashMap<>(tuples.size());
            for (Object[] tuple : tuples) {
                Collection<Object> collection = (Collection<Object>) tuple[startIndex];
                if (handledCollections.put(collection, Boolean.TRUE) == null) {
                    collectionInstantiator.postConstruct(collection);
                }
            }
        }

        return tuples;
    }

    protected Object createCollection() {
        if (dirtyTracking) {
            return collectionInstantiator.createRecordingCollection(0);
        } else {
            return collectionInstantiator.createCollection(0);
        }
    }

    @SuppressWarnings("unchecked")
    protected void add(Object collection, Object value) {
        if (elementConverter != null) {
            value = elementConverter.convertToViewType(value);
        }
        if (value != null) {
            if (dirtyTracking) {
                ((RecordingCollection<?, Object>) collection).getDelegate().add(value);
            } else {
                ((Collection<Object>) collection).add(value);
            }
        }
    }

}
