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

package com.blazebit.persistence.view.impl.objectbuilder;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
public class ConstrainedTupleList extends AbstractList<Object[]> {

    private final int classMappingIndex;
    private final int[] subtypeIndexes;
    private final List<Object[]> tupleList;

    public ConstrainedTupleList(int classMappingIndex, int[] subtypeIndexes, List<Object[]> tupleList) {
        this.classMappingIndex = classMappingIndex;
        this.subtypeIndexes = subtypeIndexes;
        this.tupleList = tupleList;
    }

    @Override
    public Object[] get(int index) {
        int count = 0;
        for (int i = 0; i < tupleList.size(); i++) {
            if (Arrays.binarySearch(subtypeIndexes, ((Number) tupleList.get(i)[classMappingIndex]).intValue()) >= 0) {
                if (count == index) {
                    return tupleList.get(i);
                }
                count++;
            }
        }
        throw new NoSuchElementException();
    }

    @Override
    public Object[] set(int index, Object[] element) {
        int count = 0;
        for (int i = 0; i < tupleList.size(); i++) {
            if (Arrays.binarySearch(subtypeIndexes, ((Number) tupleList.get(i)[classMappingIndex]).intValue()) >= 0) {
                if (count == index) {
                    return tupleList.set(i, element);
                }
                count++;
            }
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public Object[] remove(int index) {
        int count = 0;
        for (int i = 0; i < tupleList.size(); i++) {
            if (Arrays.binarySearch(subtypeIndexes, ((Number) tupleList.get(i)[classMappingIndex]).intValue()) >= 0) {
                if (count == index) {
                    return tupleList.remove(i);
                }
                count++;
            }
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public int size() {
        int count = 0;
        for (int i = 0; i < tupleList.size(); i++) {
            if (Arrays.binarySearch(subtypeIndexes, ((Number) tupleList.get(i)[classMappingIndex]).intValue()) >= 0) {
                count++;
            }
        }
        return count;
    }
}