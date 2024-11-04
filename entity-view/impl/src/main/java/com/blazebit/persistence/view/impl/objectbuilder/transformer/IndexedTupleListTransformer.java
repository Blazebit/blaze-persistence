/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.objectbuilder.transformer;

import com.blazebit.persistence.view.impl.objectbuilder.ContainerAccumulator;
import com.blazebit.persistence.view.impl.objectbuilder.TupleId;
import com.blazebit.persistence.view.impl.objectbuilder.TupleIndexValue;
import com.blazebit.persistence.view.impl.objectbuilder.TupleReuse;
import com.blazebit.persistence.view.spi.type.TypeConverter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class IndexedTupleListTransformer extends TupleListTransformer {

    private final int[] parentIdPositions;
    private final int valueStartIndex;
    private final int valueOffset;
    private final ContainerAccumulator<Object> containerAccumulator;
    private final boolean dirtyTracking;
    private final TypeConverter<Object, Object> keyConverter;
    private final TypeConverter<Object, Object> valueConverter;

    public IndexedTupleListTransformer(int[] parentIdPositions, int startIndex, int valueStartIndex, ContainerAccumulator<?> containerAccumulator, boolean dirtyTracking, TypeConverter<Object, Object> keyConverter, TypeConverter<Object, Object> valueConverter) {
        super(startIndex);
        this.parentIdPositions = parentIdPositions;
        this.valueStartIndex = valueStartIndex;
        this.containerAccumulator = (ContainerAccumulator<Object>) containerAccumulator;
        this.dirtyTracking = dirtyTracking;
        this.keyConverter = keyConverter;
        this.valueConverter = valueConverter;
        this.valueOffset = valueStartIndex - startIndex;
    }

    @Override
    public int getConsumableIndex() {
        return valueStartIndex;
    }

    @Override
    public List<Object[]> transform(List<Object[]> tuples) {
        Map<TupleId, TupleIndexValue> tupleIndex = new HashMap<TupleId, TupleIndexValue>(tuples.size());
        Iterator<Object[]> tupleListIter = tuples.iterator();

        while (tupleListIter.hasNext()) {
            Object[] tuple = tupleListIter.next();
            TupleId id = new TupleId(parentIdPositions, tuple);
            // Skip constructing the collection and removing tuples when the parent is empty i.e. null
            if (!id.isEmpty()) {
                TupleIndexValue tupleIndexValue = tupleIndex.get(id);

                // At startIndex we have the index/key of the list/map
                // At valueStartIndex is the actual element that should be put into the collection
                if (tupleIndexValue == null) {
                    Object collection = containerAccumulator.createContainer(dirtyTracking, 0);
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
        }

        return tuples;
    }

    private void add(Object collection, Object key, Object value) {
        if (keyConverter != null) {
            key = keyConverter.convertToViewType(key);
        }
        if (key != null) {
            if (valueConverter != null) {
                value = valueConverter.convertToViewType(value);
            }
            containerAccumulator.add(collection, key, value, dirtyTracking);
        }
    }

}
