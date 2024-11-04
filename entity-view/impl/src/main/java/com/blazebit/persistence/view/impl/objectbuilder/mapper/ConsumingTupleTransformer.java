/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.objectbuilder.mapper;

import com.blazebit.persistence.ParameterHolder;
import com.blazebit.persistence.view.impl.EntityViewConfiguration;
import com.blazebit.persistence.view.impl.objectbuilder.TupleReuse;
import com.blazebit.persistence.view.impl.objectbuilder.transformator.UpdatableViewMap;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.TupleTransformer;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.TupleTransformerFactory;

import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
public class ConsumingTupleTransformer implements TupleTransformerFactory, TupleTransformer {

    private final int[] consumableIndexArray;

    public ConsumingTupleTransformer(int[] consumableIndexArray) {
        this.consumableIndexArray = consumableIndexArray;
    }

    @Override
    public int getConsumeStartIndex() {
        return -1;
    }

    @Override
    public int getConsumeEndIndex() {
        return -1;
    }

    @Override
    public TupleTransformer create(ParameterHolder<?> parameterHolder, Map<String, Object> optionalParameters, EntityViewConfiguration entityViewConfiguration) {
        return this;
    }

    @Override
    public Object[] transform(Object[] tuple, UpdatableViewMap updatableViewMap) {
        for (int i = 0; i < consumableIndexArray.length; i++) {
            tuple[consumableIndexArray[i]] = TupleReuse.CONSUMED;
        }
        return tuple;
    }
}
