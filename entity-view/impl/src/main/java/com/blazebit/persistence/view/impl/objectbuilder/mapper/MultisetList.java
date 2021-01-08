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

package com.blazebit.persistence.view.impl.objectbuilder.mapper;

import java.util.AbstractList;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public final class MultisetList extends AbstractList<Object[]> {

    private final int index;
    private final List<Object[]> source;
    private final int[] sizes;
    private int size;
    private int lastIndex = -1;
    private int lastSizeOffset;
    private List<Object[]> lastList;

    public MultisetList(int index, List<Object[]> source) {
        this.index = index;
        this.source = source;
        this.sizes = new int[source.size()];
    }

    public void addSize(int index, int size) {
        this.size += size;
        sizes[index] = (index == 0 ? 0 : sizes[index - 1]) + size;
    }

    @Override
    public Object[] get(int index) {
        resolveTupleIndex(index);
        List<Object[]> list = lastList;
        if (list == null) {
            return null;
        }
        return list.get(index - lastSizeOffset);
    }

    @Override
    public Object[] set(int index, Object[] element) {
        resolveTupleIndex(index);
        List<Object[]> list = lastList;
        if (list == null) {
            return null;
        }
        return list.set(index - lastSizeOffset, element);
    }

    @Override
    public Object[] remove(int index) {
        int tupleIndex = resolveTupleIndex(index);
        List<Object[]> list = lastList;
        if (list == null) {
            return null;
        }
        if (tupleIndex == -1) {
            tupleIndex = 0;
            for (; tupleIndex < sizes.length; tupleIndex++) {
                if (index < sizes[tupleIndex] && !((List<?>) source.get(tupleIndex)[this.index]).isEmpty()) {
                    break;
                }
            }
        }
        Object[] old = list.remove(index - lastSizeOffset);
        for (int i = tupleIndex; i < sizes.length; i++) {
            sizes[i]--;
        }
        size--;
        lastIndex = -1;
        return old;
    }

    private int resolveTupleIndex(int index) {
        if (lastIndex + lastSizeOffset == index) {
            return -1;
        }
        int tupleIndex = 0;
        int lastSizeOffset = 0;
        for (; tupleIndex < sizes.length; tupleIndex++) {
            if (index < sizes[tupleIndex] && !((List<?>) source.get(tupleIndex)[this.index]).isEmpty()) {
                break;
            } else {
                lastSizeOffset = sizes[tupleIndex];
            }
        }
        this.lastSizeOffset = lastSizeOffset;
        this.lastIndex = index - lastSizeOffset;
        this.lastList = (List<Object[]>) source.get(tupleIndex)[this.index];
        return tupleIndex;
    }

    @Override
    public int size() {
        return size;
    }
}
