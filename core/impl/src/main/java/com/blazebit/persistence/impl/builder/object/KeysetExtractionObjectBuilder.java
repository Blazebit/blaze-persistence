/*
 * Copyright 2014 - 2020 Blazebit.
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.SelectBuilder;
import com.blazebit.persistence.impl.keyset.KeysetMode;
import com.blazebit.persistence.impl.keyset.KeysetPaginationHelper;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class KeysetExtractionObjectBuilder<T> implements ObjectBuilder<T> {

    private final int[] keysetToSelectIndexMapping;
    private final int keysetSuffix;
    private final KeysetMode keysetMode;
    private final boolean unwrap;
    private Object[] first;
    private Object[] last;
    private ArrayList<Object[]> keysets;

    public KeysetExtractionObjectBuilder(int[] keysetToSelectIndexMapping, KeysetMode keysetMode, boolean unwrap, boolean extractAll) {
        this.keysetToSelectIndexMapping = keysetToSelectIndexMapping;
        this.keysetMode = keysetMode;
        this.unwrap = unwrap;
        if (extractAll) {
            this.keysets = new ArrayList<>();
        }
        int suffix = 0;
        for (int i = 0; i < keysetToSelectIndexMapping.length; i++) {
            if (keysetToSelectIndexMapping[i] == -1) {
                suffix++;
            }
        }
        this.keysetSuffix = suffix;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T build(Object[] tuple) {
        if (keysetMode == KeysetMode.PREVIOUS) {
            if (keysets != null) {
                if (keysets.isEmpty()) {
                    keysets.add(tuple);
                } else if (!equals(keysets.get(0), tuple)) {
                    keysets.add(0, tuple);
                }
            }
            if (first == null) {
                first = tuple;
                last = tuple;
            } else {
                first = tuple;
            }
        } else {
            if (keysets != null) {
                if (keysets.isEmpty() || !equals(keysets.get(keysets.size() - 1), tuple)) {
                    keysets.add(tuple);
                }
            }
            if (first == null) {
                first = tuple;
                last = tuple;
            } else {
                last = tuple;
            }
        }

        if (unwrap) {
            return (T) tuple[0];
        } else {
            Object[] newTuple = new Object[tuple.length - keysetSuffix];
            System.arraycopy(tuple, 0, newTuple, 0, newTuple.length);
            return (T) newTuple;
        }
    }

    private boolean equals(Object[] existing, Object[] tuple) {
        for (int index : keysetToSelectIndexMapping) {
            if (index == -1 || !Objects.equals(existing[index], tuple[index])) {
                return false;
            }
        }
        return true;
    }

    public Serializable[] getLowest() {
        if (first == null) {
            return null;
        }

        return KeysetPaginationHelper.extractKey(first, keysetToSelectIndexMapping, keysetSuffix);
    }

    public Serializable[] getHighest() {
        if (last == null) {
            return null;
        }

        return KeysetPaginationHelper.extractKey(last, keysetToSelectIndexMapping, keysetSuffix);
    }

    public Serializable[][] getKeysets() {
        if (keysets == null) {
            return null;
        }

        Serializable[][] keysetArray = new Serializable[keysets.size()][];
        for (int i = 0; i < keysetArray.length; i++) {
            keysetArray[i] = KeysetPaginationHelper.extractKey(keysets.get(i), keysetToSelectIndexMapping, keysetSuffix);
        }

        return keysetArray;
    }

    @Override
    public List<T> buildList(List<T> list) {
        return list;
    }

    @Override
    public <X extends SelectBuilder<X>> void applySelects(X queryBuilder) {
    }

}
