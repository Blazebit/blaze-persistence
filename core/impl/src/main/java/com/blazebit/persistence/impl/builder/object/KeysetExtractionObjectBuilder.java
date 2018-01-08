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

package com.blazebit.persistence.impl.builder.object;

import java.io.Serializable;
import java.util.List;

import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.SelectBuilder;
import com.blazebit.persistence.impl.keyset.KeysetMode;
import com.blazebit.persistence.impl.keyset.KeysetPaginationHelper;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class KeysetExtractionObjectBuilder<T> implements ObjectBuilder<T> {

    private final int keysetSize;
    private final KeysetMode keysetMode;
    private Object[] first;
    private Object[] last;

    public KeysetExtractionObjectBuilder(int keysetSize, KeysetMode keysetMode) {
        this.keysetSize = keysetSize;
        this.keysetMode = keysetMode;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T build(Object[] tuple) {
        Object[] newTuple = new Object[tuple.length - keysetSize];
        System.arraycopy(tuple, 0, newTuple, 0, newTuple.length);

        if (keysetMode == KeysetMode.PREVIOUS) {
            if (first == null) {
                first = tuple;
                last = tuple;
            } else {
                first = tuple;
            }
        } else {
            if (first == null) {
                first = tuple;
                last = tuple;
            } else {
                last = tuple;
            }
        }

        return (T) newTuple;
    }

    public Serializable[] getLowest() {
        if (first == null) {
            return null;
        }

        return KeysetPaginationHelper.extractKey(first, first.length - keysetSize);
    }

    public Serializable[] getHighest() {
        if (last == null) {
            return null;
        }

        return KeysetPaginationHelper.extractKey(last, last.length - keysetSize);
    }

    @Override
    public List<T> buildList(List<T> list) {
        return list;
    }

    @Override
    public <X extends SelectBuilder<X>> void applySelects(X queryBuilder) {
    }

}
