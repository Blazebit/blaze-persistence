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

import java.util.List;

import javax.persistence.TypedQuery;

import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.Queryable;
import com.blazebit.persistence.spi.DbmsStatementType;

/**
 *
 * @param <T> The query result type
 * @param <X> The concrete builder type
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public abstract class AbstractQueryBuilder<T, X extends Queryable<T, X>, Z, W, FinalSetReturn extends BaseFinalSetOperationBuilderImpl<T, ?, ?>> extends AbstractCommonQueryBuilder<T, X, Z, W, FinalSetReturn> implements Queryable<T, X> {

    /**
     * Create flat copy of builder
     *
     * @param builder
     */
    protected AbstractQueryBuilder(AbstractQueryBuilder<T, ? extends FullQueryBuilder<T, ?>, ?, ?, ?> builder) {
        super(builder);
    }

    public AbstractQueryBuilder(MainQuery mainQuery, boolean isMainQuery, Class<T> clazz, String alias, FinalSetReturn finalSetOperationBuilder) {
        super(mainQuery, null, isMainQuery, DbmsStatementType.SELECT, clazz, alias, finalSetOperationBuilder);
    }

    @Override
    public TypedQuery<T> getQuery() {
        return getTypedQuery();
    }

    @Override
    public List<T> getResultList() {
        return getQuery().getResultList();
    }

    @Override
    public T getSingleResult() {
        return getQuery().getSingleResult();
    }

}
