/*
 * Copyright 2014 - 2023 Blazebit.
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
