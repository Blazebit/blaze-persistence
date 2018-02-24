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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.blazebit.persistence.view.impl.objectbuilder.TupleId;
import com.blazebit.persistence.view.impl.objectbuilder.TupleIndexValue;
import com.blazebit.persistence.view.impl.objectbuilder.TupleReuse;
import com.blazebit.persistence.view.spi.type.TypeConverter;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public abstract class AbstractIndexedTupleListTransformer<C, K> extends TupleListTransformer {

    private final int[] parentIdPositions;
    private final int valueStartIndex;
    private final int valueOffset;
    private final TypeConverter<Object, Object> keyConverter;
    private final TypeConverter<Object, Object> valueConverter;

    public AbstractIndexedTupleListTransformer(int[] parentIdPositions, int startIndex, int valueStartIndex, TypeConverter<Object, Object> keyConverter, TypeConverter<Object, Object> valueConverter) {
        super(startIndex);
        this.parentIdPositions = parentIdPositions;
        this.valueStartIndex = valueStartIndex;
        this.keyConverter = keyConverter;
        this.valueConverter = valueConverter;
        this.valueOffset = valueStartIndex - startIndex;
    }

    @Override
    public List<Object[]> transform(List<Object[]> tuples) {
        Map<TupleId, TupleIndexValue> tupleIndex = new HashMap<TupleId, TupleIndexValue>(tuples.size());
        // Implementation detail: the tuple list is a LinkedList
        Iterator<Object[]> tupleListIter = tuples.iterator();

        while (tupleListIter.hasNext()) {
            Object[] tuple = tupleListIter.next();
            TupleId id = new TupleId(parentIdPositions, tuple);
            TupleIndexValue tupleIndexValue = tupleIndex.get(id);

            // At startIndex we have the index/key of the list/map
            // At valueStartIndex is the actual element that should be put into the collection
            if (tupleIndexValue == null) {
                Object collection = createCollection();
                tupleIndexValue = new TupleIndexValue(collection, tuple, startIndex, valueOffset + 1);
                Object key = tuple[startIndex];
                add(collection, key, tuple[valueStartIndex]);
                tuple[startIndex] = collection;
                tuple[valueStartIndex] = TupleReuse.CONSUMED;
                tupleIndex.put(id, tupleIndexValue);
            } else if (tupleIndexValue.addRestTuple(tuple, startIndex, valueOffset + 1)) {
                Object collection = tupleIndexValue.getTupleValue();
                Object key = tuple[startIndex];
                add(collection, key, tuple[valueStartIndex]);
                tuple[startIndex] = collection;
                tuple[valueStartIndex] = TupleReuse.CONSUMED;
                // Check if the tuple after the offset is contained
                if (tupleIndexValue.containsRestTuple(tuple, startIndex, valueOffset + 1)) {
                    tupleListIter.remove();
                }
            } else {
                Object key = tuple[startIndex];
                add(tupleIndexValue.getTupleValue(), key, tuple[valueStartIndex]);
                tuple[valueStartIndex] = TupleReuse.CONSUMED;
                tupleListIter.remove();
            }
        }

        return tuples;
    }

    protected abstract Object createCollection();

    @SuppressWarnings("unchecked")
    protected void add(Object collection, Object key, Object value) {
        if (keyConverter != null) {
            key = keyConverter.convertToViewType(key);
        }
        if (key != null) {
            if (valueConverter != null) {
                value = valueConverter.convertToViewType(value);
            }
            addToCollection((C) collection, (K) key, value);
        }
    }

    protected abstract void addToCollection(C collection, K key, Object value);

}
