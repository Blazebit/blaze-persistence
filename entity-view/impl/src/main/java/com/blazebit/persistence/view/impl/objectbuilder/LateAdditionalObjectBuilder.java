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

package com.blazebit.persistence.view.impl.objectbuilder;

import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.SelectBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
public class LateAdditionalObjectBuilder implements ObjectBuilder<Object[]> {

    private final ObjectBuilder<Object[]> objectBuilder;
    private final ObjectBuilder<Object[]> additionalBuilder;

    public LateAdditionalObjectBuilder(ObjectBuilder<Object[]> objectBuilder, ObjectBuilder<Object[]> additionalBuilder) {
        this.objectBuilder = objectBuilder;
        this.additionalBuilder = additionalBuilder;
    }

    @Override
    public <X extends SelectBuilder<X>> void applySelects(X queryBuilder) {
        additionalBuilder.applySelects(queryBuilder);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object[] build(Object[] tuple) {
        return tuple;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Object[]> buildList(List<Object[]> list) {
        List<Object[]> objects = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            objects.add(objectBuilder.build(list.get(i)));
        }

        objects = objectBuilder.buildList(objects);

        for (int j = 0; j < objects.size(); j++) {
            objects.set(j, additionalBuilder.build(objects.get(j)));
        }
        return additionalBuilder.buildList(objects);
    }
}
