/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.objectbuilder.transformer;

import com.blazebit.persistence.view.impl.objectbuilder.TupleReuse;
import com.blazebit.persistence.view.impl.objectbuilder.ViewTypeObjectBuilderTemplate;
import com.blazebit.persistence.view.impl.objectbuilder.transformator.UpdatableViewMap;

/**
 *
 * @author Christian Beikov
 * @since 1.6.2
 */
public class NullTupleTransformer implements TupleTransformer {

    private final int consumeStartIndex;
    private final int consumeEndIndex;

    public NullTupleTransformer(ViewTypeObjectBuilderTemplate<Object[]> template, int startIndex) {
        this(startIndex, startIndex + template.getMappers().length);
    }

    private NullTupleTransformer(int startIndex, int endIndex) {
        this.consumeStartIndex = startIndex + 1;
        this.consumeEndIndex = endIndex;
    }

    /**
     * A multiset fetched subview only consists of a single tuple index. Therefore, the specification of only the
     * {@code startIndex} is sufficient.
     */
    public static NullTupleTransformer forMultiset(int startIndex) {
        return new NullTupleTransformer(startIndex, startIndex + 1);
    }

    @Override
    public int getConsumeStartIndex() {
        return consumeStartIndex;
    }

    @Override
    public int getConsumeEndIndex() {
        return consumeEndIndex;
    }

    @Override
    public Object[] transform(Object[] tuple, UpdatableViewMap updatableViewMap) {
        tuple[consumeStartIndex - 1] = null;
        for (int i = consumeStartIndex; i < consumeEndIndex; i++) {
            tuple[i] = TupleReuse.CONSUMED;
        }
        return tuple;
    }

}
