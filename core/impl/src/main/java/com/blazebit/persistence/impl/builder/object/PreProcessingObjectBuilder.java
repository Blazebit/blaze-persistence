/*
 * Copyright 2014 - 2020 Blazebit.
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

package com.blazebit.persistence.impl.builder.object;

import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.SelectBuilder;

import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public class PreProcessingObjectBuilder<T> implements ObjectBuilder<T> {

    private final ObjectBuilder<Object[]> preProcessor;
    private final ObjectBuilder<T> objectBuilder;

    public PreProcessingObjectBuilder(ObjectBuilder<Object[]> preProcessor, ObjectBuilder<T> objectBuilder) {
        this.preProcessor = preProcessor;
        this.objectBuilder = objectBuilder;
    }

    @Override
    public <X extends SelectBuilder<X>> void applySelects(X selectBuilder) {
        objectBuilder.applySelects(selectBuilder);
    }

    @Override
    public T build(Object[] tuple) {
        return objectBuilder.build(preProcessor.build(tuple));
    }

    @Override
    public List<T> buildList(List<T> list) {
        return objectBuilder.buildList(list);
    }
}
