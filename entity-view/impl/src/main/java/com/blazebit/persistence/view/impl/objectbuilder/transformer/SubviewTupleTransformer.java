/*
 * Copyright 2014 Blazebit.
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
import com.blazebit.persistence.QueryBuilder;
import com.blazebit.persistence.view.impl.objectbuilder.ViewTypeObjectBuilderTemplate;

/**
 *
 * @author Christian
 */
public class SubviewTupleTransformer extends TupleTransformer {

    private final ViewTypeObjectBuilderTemplate<Object[]> template;
    private ObjectBuilder<Object[]> objectBuilder;

    public SubviewTupleTransformer(ViewTypeObjectBuilderTemplate<Object[]> template) {
        this.template = template;
    }

    @Override
    public TupleTransformer init(QueryBuilder<?, ?> queryBuilder) {
        this.objectBuilder = template.createObjectBuilder(queryBuilder, true);
        return this;
    }

    @Override
    public Object[] transform(Object[] tuple) {
        return objectBuilder.build(tuple);
    }

}
