/*
 * Copyright 2014 Blazebit.
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

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public abstract class AbstractIndexedTupleListTransformer<C, K> extends TupleListTransformer {

    private final int[] parentIdPositions;

    public AbstractIndexedTupleListTransformer(int[] parentIdPositions, int startIndex) {
        super(startIndex);
        this.parentIdPositions = parentIdPositions;
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

            if (tupleIndexValue == null) {
                Object key = tuple[startIndex];
                tuple[startIndex] = createCollection();
                add(tuple[startIndex], key, tuple[startIndex + 1]);
                tuple[startIndex + 1] = TupleReuse.CONSUMED;
                tupleIndexValue = new TupleIndexValue(tuple, startIndex + 2);
                tupleIndex.put(id, tupleIndexValue);
            } else if (tupleIndexValue.addRestTuple(tuple, startIndex + 2)) {
                Object key = tuple[startIndex];
                tuple[startIndex] = tupleIndexValue.getTuple()[startIndex];
                add(tuple[startIndex], key, tuple[startIndex + 1]);
                tuple[startIndex + 1] = TupleReuse.CONSUMED;
            } else {
                Object key = tuple[startIndex];
                add(tupleIndexValue.getTuple()[startIndex], key, tuple[startIndex + 1]);
                tuple[startIndex + 1] = TupleReuse.CONSUMED;
                tupleListIter.remove();
            }
        }

        return tuples;
    }

    protected abstract Object createCollection();

    @SuppressWarnings("unchecked")
    protected void add(Object collection, Object key, Object value) {
        if (key != null) {
            addToCollection((C) collection, (K) key, value);
        }
    }

    protected abstract void addToCollection(C collection, K key, Object value);

}
