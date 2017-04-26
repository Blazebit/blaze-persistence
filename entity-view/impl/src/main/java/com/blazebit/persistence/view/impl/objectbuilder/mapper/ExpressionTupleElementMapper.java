/*
 * Copyright 2014 - 2017 Blazebit.
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

import java.util.Map;

import com.blazebit.persistence.CommonQueryBuilder;
import com.blazebit.persistence.FetchBuilder;
import com.blazebit.persistence.SelectBuilder;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class ExpressionTupleElementMapper implements TupleElementMapper {

    private static final String[] EMPTY = new String[0];

    protected final String expression;
    protected final String[] fetches;

    public ExpressionTupleElementMapper(String expression) {
        this.expression = expression;
        this.fetches = EMPTY;
    }

    public ExpressionTupleElementMapper(String expression, String[] fetches) {
        this.expression = expression;
        this.fetches = fetches;
    }

    @Override
    public void applyMapping(SelectBuilder<?> queryBuilder, CommonQueryBuilder<?> parameterSource, Map<String, Object> optionalParameters) {
        queryBuilder.select(expression);
        if (fetches.length != 0) {
            final FetchBuilder<?> fetchBuilder = (FetchBuilder<?>) queryBuilder;
            for (int i = 0; i < fetches.length; i++) {
                fetchBuilder.fetch(fetches[i]);
            }
        }
    }

}
