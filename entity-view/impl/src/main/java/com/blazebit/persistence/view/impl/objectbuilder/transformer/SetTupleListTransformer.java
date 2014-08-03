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

import com.blazebit.persistence.view.impl.objectbuilder.TupleId;
import com.blazebit.persistence.view.impl.objectbuilder.TupleIndexValue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian
 */
public class SetTupleListTransformer extends TupleListTransformer {
    
    private final int[] parentIdPositions;

    public SetTupleListTransformer(int[] parentIdPositions, int startIndex) {
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
                tupleIndexValue = new TupleIndexValue(tuple, startIndex + 1);
                Object collection = createCollection();
                add(collection, tuple[startIndex]);
                tuple[startIndex] = collection;
                tupleIndex.put(id, tupleIndexValue);
            } else if (tupleIndexValue.addRestTuple(tuple, startIndex + 1)) {
                Object collection = tupleIndexValue.getTuple()[startIndex];
                Object old = tuple[startIndex];
                add(collection, old);
                tuple[startIndex] = collection;
            } else {
                add(tupleIndexValue.getTuple()[startIndex], tuple[startIndex]);
                tupleListIter.remove();
            }
        }
        
        return tuples;
    }
    
    private Object createCollection() {
        return new HashSet<Object>();
    }
    
    private void add(Object collection, Object value) {
        if (value != null) {
            addToCollection((Set<Object>) collection, value);
        }
    }
    
    private void addToCollection(Set<Object> set, Object value) {
        set.add(value);
    }
    
}
