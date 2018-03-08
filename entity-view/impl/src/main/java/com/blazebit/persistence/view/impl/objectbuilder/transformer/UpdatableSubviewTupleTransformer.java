/*
 * Copyright 2014 - 2018 Blazebit.
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

import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.view.impl.objectbuilder.TupleReuse;
import com.blazebit.persistence.view.impl.objectbuilder.ViewTypeObjectBuilderTemplate;
import com.blazebit.persistence.view.impl.objectbuilder.transformator.UpdatableViewMap;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class UpdatableSubviewTupleTransformer implements TupleTransformer {

    private final ViewTypeObjectBuilderTemplate<Object[]> template;
    private final int consumeStartIndex;
    private final int consumeEndIndex;
    private final ObjectBuilder<Object[]> objectBuilder;

    public UpdatableSubviewTupleTransformer(ViewTypeObjectBuilderTemplate<Object[]> template, ObjectBuilder<Object[]> objectBuilder) {
        this.template = template;
        this.consumeStartIndex = template.getTupleOffset() + 1;
        this.consumeEndIndex = template.getTupleOffset() + template.getMappers().length;
        this.objectBuilder = objectBuilder;
    }

    @Override
    public Object[] transform(Object[] tuple, UpdatableViewMap updatableViewMap) {
        Object id = tuple[template.getTupleOffset()];
        if (id != null) {
            UpdatableViewMap.UpdatableViewKey key = new UpdatableViewMap.UpdatableViewKey(template.getViewClass(), id);
            Object o = updatableViewMap.get(key);
            if (o != null) {
                tuple[template.getTupleOffset()] = o;
            } else {
                o = objectBuilder.build(tuple);
                updatableViewMap.put(key, o);
                tuple[template.getTupleOffset()] = o;
            }
        }
        for (int i = consumeStartIndex; i < consumeEndIndex; i++) {
            tuple[i] = TupleReuse.CONSUMED;
        }
        return tuple;
    }

}
