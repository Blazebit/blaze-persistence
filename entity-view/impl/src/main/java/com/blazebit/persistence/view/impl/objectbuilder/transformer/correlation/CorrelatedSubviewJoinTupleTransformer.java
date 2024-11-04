/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation;

import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.view.impl.objectbuilder.TupleReuse;
import com.blazebit.persistence.view.impl.objectbuilder.ViewTypeObjectBuilderTemplate;
import com.blazebit.persistence.view.impl.objectbuilder.transformator.UpdatableViewMap;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.TupleTransformer;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CorrelatedSubviewJoinTupleTransformer implements TupleTransformer {

    private final ViewTypeObjectBuilderTemplate<Object[]> template;
    private final int consumeStartIndex;
    private final int consumeEndIndex;
    private final ObjectBuilder<Object[]> objectBuilder;

    public CorrelatedSubviewJoinTupleTransformer(ViewTypeObjectBuilderTemplate<Object[]> template, ObjectBuilder<Object[]> objectBuilder) {
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
