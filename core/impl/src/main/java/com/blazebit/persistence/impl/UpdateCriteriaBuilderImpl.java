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

package com.blazebit.persistence.impl;

import com.blazebit.persistence.UpdateCriteriaBuilder;

/**
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @since 1.1.0
 */
public class UpdateCriteriaBuilderImpl<T> extends BaseUpdateCriteriaBuilderImpl<T, UpdateCriteriaBuilder<T>, Void> implements UpdateCriteriaBuilder<T> {

    public UpdateCriteriaBuilderImpl(MainQuery mainQuery, Class<T> clazz, String alias) {
        super(mainQuery, null, true, clazz, alias, null, null, null, null);
    }

    @Override
    AbstractCommonQueryBuilder<T, UpdateCriteriaBuilder<T>, AbstractCommonQueryBuilder<?, ?, ?, ?, ?>, AbstractCommonQueryBuilder<?, ?, ?, ?, ?>, BaseFinalSetOperationBuilderImpl<T, ?, ?>> copy(QueryContext queryContext) {
        throw new UnsupportedOperationException("This should only be used on CTEs!");
    }
}
