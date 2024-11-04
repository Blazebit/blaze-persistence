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
        this.consumeStartIndex = startIndex + 1;
        this.consumeEndIndex = startIndex + template.getMappers().length;
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
