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
package com.blazebit.persistence.view.impl;

import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.QueryBuilder;
import com.blazebit.persistence.spi.ObjectBuilderFactory;

/**
 *
 * @author cpbec
 */
public class ObjectBuilderFactoryImpl<X> implements ObjectBuilderFactory<X> {

    private final ViewTypeObjectBuilderTemplate<X> template;

    public ObjectBuilderFactoryImpl(ViewTypeObjectBuilderTemplate<X> template) {
        this.template = template;
    }

    @Override
    public ObjectBuilder<X> createObjectBuilder(QueryBuilder<X, ?> queryBuilder) {
        return new ViewTypeObjectBuilderImpl<X>(template, queryBuilder);
    }
}
