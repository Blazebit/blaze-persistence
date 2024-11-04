/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

import java.util.List;
import java.util.stream.Stream;

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
        return getTypedQuery(null, null);
    }

    @Override
    public List<T> getResultList() {
        return getQuery().getResultList();
    }

    @Override
    public T getSingleResult() {
        return getQuery().getSingleResult();
    }

    @Override
    public Stream<T> getResultStream() {
        return getQuery().getResultStream();
    }

}
