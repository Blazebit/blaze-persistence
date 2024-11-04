/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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