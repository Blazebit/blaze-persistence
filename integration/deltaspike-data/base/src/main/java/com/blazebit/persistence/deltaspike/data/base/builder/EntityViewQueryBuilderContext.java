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

package com.blazebit.persistence.deltaspike.data.base.builder;

import com.blazebit.persistence.deltaspike.data.base.builder.part.OrderByQueryAttribute;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation is similar to {@link org.apache.deltaspike.data.impl.builder.QueryBuilderContext} but was modified to
 * work with entity views.
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public class EntityViewQueryBuilderContext {

    private final StringBuilder whereExpressionBuilder = new StringBuilder();
    private final List<OrderByQueryAttribute> orderByAttributes = new ArrayList<>();
    private int counter = 1;

    public EntityViewQueryBuilderContext reset() {
        counter = 1;
        return this;
    }

    public int increment() {
        return counter++;
    }

    public StringBuilder getWhereExpressionBuilder() {
        return whereExpressionBuilder;
    }

    public List<OrderByQueryAttribute> getOrderByAttributes() {
        return orderByAttributes;
    }
}
