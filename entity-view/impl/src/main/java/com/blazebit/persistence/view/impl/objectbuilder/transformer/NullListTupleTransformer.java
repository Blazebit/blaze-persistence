/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.objectbuilder.transformer;

import java.util.List;

import com.blazebit.persistence.view.impl.objectbuilder.TupleReuse;

/**
 *
 * @author Christian Beikov
 * @since 1.6.8
 */
public class NullListTupleTransformer extends TupleListTransformer {

    private final int valueStartIndex;

    public NullListTupleTransformer(int startIndex, int valueStartIndex) {
        super(startIndex);
        this.valueStartIndex = valueStartIndex;
    }

    @Override
    public int getConsumableIndex() {
        return valueStartIndex;
    }

    @Override
    public List<Object[]> transform(List<Object[]> tuples) {
        for (Object[] tuple : tuples) {
            tuple[startIndex] = null;
            for (int i = startIndex + 1; i <= valueStartIndex; i++) {
                tuple[valueStartIndex] = TupleReuse.CONSUMED;
            }
        }
        return tuples;
    }
}
