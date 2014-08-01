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
import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian
 */
public abstract class IndexedTupleListTransformer extends TupleListTransformer {
    
    private final int[] parentIdPositions;

    public IndexedTupleListTransformer(int[] parentIdPositions, int startIndex) {
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
                Object[] newTuple = new Object[tuple.length - 1];
                System.arraycopy(tuple, 0, newTuple, 0, startIndex);
                newTuple[startIndex] = createCollection();
                int nextIndex = startIndex + 2;
                if (nextIndex < tuple.length) {
                    System.arraycopy(tuple, nextIndex, newTuple, startIndex + 1, newTuple.length - (nextIndex - 1));
                }
                tupleIndexValue = new TupleIndexValue(newTuple, startIndex + 1);
                tupleIndex.put(id, tupleIndexValue);
                resultList.add(newTuple);
            } else if (tupleIndexValue.addRestTuple(tuple, startIndex + 2)) {
                Object[] newTuple = new Object[tuple.length - 1];
                System.arraycopy(tuple, 0, newTuple, 0, startIndex);
                newTuple[startIndex] = tupleIndexValue.getTuple()[startIndex];
                int nextIndex = startIndex + 2;
                if (nextIndex < tuple.length) {
                    System.arraycopy(tuple, nextIndex, newTuple, startIndex + 1, newTuple.length - (nextIndex - 1));
                }
                resultList.add(newTuple);
            }
            
            Object key = tuple[startIndex];
            
            if (key != null) {
                addToCollection(tupleIndexValue.getTuple()[startIndex], key, tuple[startIndex + 1]);
            }
        }
        
        return resultList;
    }
    
    protected abstract Object createCollection();
    
    protected abstract void addToCollection(Object collection, Object key, Object value);
    
}
