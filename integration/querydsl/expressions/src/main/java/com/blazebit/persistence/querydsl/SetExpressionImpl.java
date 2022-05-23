/*
 * Copyright 2014 - 2022 Blazebit.
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

package com.blazebit.persistence.querydsl;

import com.blazebit.persistence.KeysetPage;
import com.blazebit.persistence.PagedList;
import com.mysema.commons.lang.CloseableIterator;
import com.querydsl.core.NonUniqueResultException;
import com.querydsl.core.QueryMetadata;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Visitor;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Default implementation for {@link SetExpression}. Analog to {@link com.querydsl.sql.UnionImpl}.
 *
 * @param <T> result type
 * @param <Q> concrete query type
 * @author Jan-Willem Gmelig Meyling
 * @since 1.5.0
 */
public class SetExpressionImpl<T, Q extends AbstractBlazeJPAQuery<T, Q>> implements SetExpression<T> {

    private static final long serialVersionUID = -1415380406067515250L;

    private final AbstractBlazeJPAQuery<T, ?> query;

    public SetExpressionImpl(AbstractBlazeJPAQuery<T, ?> query) {
        this.query = query;
    }

    @Override
    public PagedList<T> fetchPage(int firstResult, int maxResults) {
        return query.fetchPage(firstResult, maxResults);
    }

    @Override
    public PagedList<T> fetchPage(KeysetPage keysetPage, int firstResult, int maxResults) {
        return query.fetchPage(keysetPage, firstResult, maxResults);
    }

    @Override
    public List<T> fetch() {
        return query.fetch();
    }

    @Override
    public T fetchFirst() {
        return query.fetchFirst();
    }

    @Override
    public T fetchOne() throws NonUniqueResultException {
        return query.fetchOne();
    }

    @Override
    public CloseableIterator<T> iterate() {
        return query.iterate();
    }

    @Override
    public QueryResults<T> fetchResults() {
        return query.fetchResults();
    }

    @Override
    public long fetchCount() {
        return query.fetchCount();
    }

    @Override
    public QueryMetadata getMetadata() {
        return query.getMetadata();
    }

    @Nullable
    @Override
    public <R, C> R accept(Visitor<R, C> v, @Nullable C context) {
        return query.accept(v, context);
    }

    @Override
    public Class<? extends T> getType() {
        return query.getType();
    }

    @Override
    public SetExpression<T> limit(long limit) {
        query.limit(limit);
        return this;
    }

    @Override
    public SetExpression<T> offset(long offset) {
        query.offset(offset);
        return this;
    }

    @Override
    public SetExpression<T> orderBy(OrderSpecifier<?>... o) {
        query.orderBy(o);
        return this;
    }

    @Override
    public String getQueryString() {
        return query.getQueryString();
    }

    @Override
    public String toString() {
        return query.toString();
    }
}
