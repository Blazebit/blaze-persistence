/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.builder.object;

import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.SelectBuilder;
import com.blazebit.persistence.impl.keyset.KeysetMode;

import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class DelegatingKeysetExtractionObjectBuilder<T> extends KeysetExtractionObjectBuilder<T> {

    private final ObjectBuilder<T> objectBuilder;

    public DelegatingKeysetExtractionObjectBuilder(ObjectBuilder<T> objectBuilder, int[] keysetToSelectIndexMapping, KeysetMode keysetMode, int pageSize, int highestOffset, boolean extractAll, boolean extractCount) {
        super(keysetToSelectIndexMapping, keysetMode, pageSize, highestOffset, false, extractAll, extractCount);
        this.objectBuilder = objectBuilder;
    }

    @Override
    public T build(Object[] tuple) {
        return objectBuilder.build((Object[]) super.build(tuple));
    }

    @Override
    public List<T> buildList(List<T> list) {
        return objectBuilder.buildList(list);
    }

    @Override
    public <X extends SelectBuilder<X>> void applySelects(X selectBuilder) {
        objectBuilder.applySelects(selectBuilder);
    }

}
