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

package com.blazebit.persistence.view.impl.objectbuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
        Map<ParentId, TupleIndexValue> tupleIndex = new HashMap<ParentId, TupleIndexValue>(tuples.size());
        List<Object[]> resultList = new ArrayList<Object[]>(tuples.size());
        
        for (Object[] tuple : tuples) {
            ParentId id = new ParentId(parentIdPositions, tuple);
            TupleIndexValue tupleIndexValue = tupleIndex.get(id);
            
            if (tupleIndexValue == null) {
                tupleIndexValue = new TupleIndexValue(tuple, startIndex + 1);
                Object collection = createCollection();
                addToCollection(collection, tuple[startIndex]);
                tuple[startIndex] = collection;
                tupleIndex.put(id, tupleIndexValue);
                resultList.add(tuple);
            } else if (tupleIndexValue.addRestTuple(tuple, startIndex + 1)) {
                Object collection = tupleIndexValue.getTuple()[startIndex];
                Object old = tuple[startIndex];
                addToCollection(collection, old);
                tuple[startIndex] = collection;
                resultList.add(tuple);
            } else {
                addToCollection(tupleIndexValue.getTuple()[startIndex], tuple[startIndex]);
            }
        }
        
        return resultList;
    }
    
    private Object createCollection() {
        return new HashSet<Object>();
    }
    
    private void addToCollection(Object collection, Object value) {
        if (value != null) {
            Set<Object> set = (Set<Object>) collection;
            set.add(value);
        }
    }
    
}
