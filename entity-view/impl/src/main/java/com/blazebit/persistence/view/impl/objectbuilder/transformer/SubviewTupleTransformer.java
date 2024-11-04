/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.objectbuilder.transformer;

import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.view.impl.objectbuilder.TupleReuse;
import com.blazebit.persistence.view.impl.objectbuilder.ViewTypeObjectBuilderTemplate;
import com.blazebit.persistence.view.impl.objectbuilder.transformator.UpdatableViewMap;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class SubviewTupleTransformer implements TupleTransformer {

    private final ViewTypeObjectBuilderTemplate<Object[]> template;
    private final int consumeStartIndex;
    private final int consumeEndIndex;
    private final ObjectBuilder<Object[]> objectBuilder;

    public SubviewTupleTransformer(ViewTypeObjectBuilderTemplate<Object[]> template, ObjectBuilder<Object[]> objectBuilder) {
        this.template = template;
        this.consumeStartIndex = template.getTupleOffset() + 1;
        this.consumeEndIndex = template.getTupleOffset() + template.getMappers().length;
        this.objectBuilder = objectBuilder;
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
        tuple[template.getTupleOffset()] = objectBuilder.build(tuple);
        for (int i = consumeStartIndex; i < consumeEndIndex; i++) {
            tuple[i] = TupleReuse.CONSUMED;
        }
        return tuple;
    }

}
