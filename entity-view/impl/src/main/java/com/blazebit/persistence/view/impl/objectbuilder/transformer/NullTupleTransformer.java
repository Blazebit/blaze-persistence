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
