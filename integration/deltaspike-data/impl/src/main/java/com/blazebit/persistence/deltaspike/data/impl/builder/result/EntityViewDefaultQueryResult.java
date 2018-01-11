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

package com.blazebit.persistence.deltaspike.data.impl.builder.result;

import com.blazebit.persistence.deltaspike.data.impl.builder.postprocessor.FlushModePostProcessor;
import com.blazebit.persistence.deltaspike.data.impl.builder.postprocessor.LockModePostProcessor;
import com.blazebit.persistence.deltaspike.data.impl.builder.postprocessor.PaginationCriteriaBuilderPostProcessor;
import com.blazebit.persistence.deltaspike.data.impl.handler.EntityViewCdiQueryInvocationContext;
import com.blazebit.persistence.deltaspike.data.impl.builder.EntityViewQueryBuilder;
import com.blazebit.persistence.deltaspike.data.impl.builder.postprocessor.HintPostProcessor;
import com.blazebit.persistence.deltaspike.data.impl.handler.CriteriaBuilderPostProcessor;
import com.blazebit.persistence.deltaspike.data.impl.builder.postprocessor.OrderByCriteriaBuilderPostProcessor;
import org.apache.deltaspike.data.api.QueryResult;
import org.apache.deltaspike.data.impl.builder.OrderDirection;

import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.metamodel.SingularAttribute;
import java.util.Iterator;
import java.util.List;

/**
 * Implementation is similar to {@link org.apache.deltaspike.data.impl.builder.result.DefaultQueryResult} but was modified to
 * work with entity views.
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public class EntityViewDefaultQueryResult<T> implements QueryResult<T> {

    private final EntityViewQueryBuilder builder;
    private final EntityViewCdiQueryInvocationContext context;

    private int page = 0;
    private int pageSize = 10;
    private int firstResult = 0;
    private int maxResults = -1;

    public EntityViewDefaultQueryResult(EntityViewQueryBuilder builder, EntityViewCdiQueryInvocationContext context) {
        this.builder = builder;
        this.context = context;
    }

    @Override
    public <X> QueryResult<T> orderAsc(SingularAttribute<T, X> attribute) {
        return orderAsc(attribute, true);
    }

    @Override
    public <X> QueryResult<T> orderAsc(SingularAttribute<T, X> attribute, boolean appendEntityName) {
        context.addCriteriaBuilderPostProcessor(new OrderByCriteriaBuilderPostProcessor(attribute, OrderDirection.ASC));
        return this;
    }

    @Override
    public QueryResult<T> orderAsc(String attribute) {
        return orderAsc(attribute, true);
    }

    @Override
    public QueryResult<T> orderAsc(String attribute, boolean appendEntityName) {
        context.addCriteriaBuilderPostProcessor(new OrderByCriteriaBuilderPostProcessor(attribute, OrderDirection.ASC));
        return this;
    }

    @Override
    public <X> QueryResult<T> orderDesc(SingularAttribute<T, X> attribute) {
        return orderDesc(attribute, true);
    }

    @Override
    public <X> QueryResult<T> orderDesc(SingularAttribute<T, X> attribute, boolean appendEntityName) {
        context.addCriteriaBuilderPostProcessor(new OrderByCriteriaBuilderPostProcessor(attribute, OrderDirection.DESC));
        return this;
    }

    @Override
    public QueryResult<T> orderDesc(String attribute) {
        return orderDesc(attribute, true);
    }

    @Override
    public QueryResult<T> orderDesc(String attribute, boolean appendEntityName) {
        context.addCriteriaBuilderPostProcessor(new OrderByCriteriaBuilderPostProcessor(attribute, OrderDirection.DESC));
        return this;
    }

    @Override
    public <X> QueryResult<T> changeOrder(final SingularAttribute<T, X> attribute) {
        changeOrder(new EntityViewDefaultQueryResult.ChangeOrder() {
            @Override
            public boolean matches(OrderByCriteriaBuilderPostProcessor orderBy) {
                return orderBy.matches(attribute);
            }

            @Override
            public void addDefault() {
                orderAsc(attribute);
            }
        });
        return this;
    }

    @Override
    public QueryResult<T> changeOrder(final String attribute) {
        changeOrder(new EntityViewDefaultQueryResult.ChangeOrder() {
            @Override
            public boolean matches(OrderByCriteriaBuilderPostProcessor orderBy) {
                return orderBy.matches(attribute);
            }

            @Override
            public void addDefault() {
                orderAsc(attribute);
            }
        });
        return this;
    }

    @Override
    public QueryResult<T> clearOrder() {
        for (Iterator<CriteriaBuilderPostProcessor> it = context.getCriteriaBuilderPostProcessors().iterator(); it.hasNext(); ) {
            if (it.next() instanceof OrderByCriteriaBuilderPostProcessor) {
                it.remove();
            }
        }
        return this;
    }

    @Override
    public QueryResult<T> maxResults(int max) {
        maxResults = max;
        pageSize = max;
        return this;
    }

    @Override
    public QueryResult<T> firstResult(int first) {
        firstResult = first;
        return this;
    }

    private void updatePaginationPostProcessor() {
        clearPagination();
        if (isFirstResultSet() || isMaxResultsSet()) {
            context.addCriteriaBuilderPostProcessor(new PaginationCriteriaBuilderPostProcessor(firstResult, maxResults < 0 ? Integer.MAX_VALUE : maxResults));
        }
    }

    private void clearPagination() {
        Iterator<CriteriaBuilderPostProcessor> iterator = context.getCriteriaBuilderPostProcessors().iterator();
        while (iterator.hasNext()) {
            if (iterator.next() instanceof PaginationCriteriaBuilderPostProcessor) {
                iterator.remove();
            }
        }
    }

    private boolean isFirstResultSet() {
        return firstResult > 0;
    }

    private boolean isMaxResultsSet() {
        return maxResults >= 0;
    }

    @Override
    public QueryResult<T> lockMode(LockModeType lockMode) {
        context.addJpaQueryPostProcessor(new LockModePostProcessor(lockMode));
        return this;
    }

    @Override
    public QueryResult<T> flushMode(FlushModeType flushMode) {
        context.addJpaQueryPostProcessor(new FlushModePostProcessor(flushMode));
        return this;
    }

    @Override
    public QueryResult<T> hint(String hint, Object value) {
        context.addJpaQueryPostProcessor(new HintPostProcessor(hint, value));
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<T> getResultList() {
        updatePaginationPostProcessor();
        return ((Query) builder.executeQuery(context)).getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getSingleResult() {
        updatePaginationPostProcessor();
        return (T) ((Query) builder.executeQuery(context)).getSingleResult();
    }

    @Override
    public T getOptionalResult() {
        try {
            return getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public T getAnyResult() {
        List<T> queryResult = getResultList();
        return !queryResult.isEmpty() ? queryResult.get(0) : null;
    }

    @Override
    public long count() {
        throw new UnsupportedOperationException("Count rewrite not supported for entity view repositories");
    }

    @Override
    public QueryResult<T> withPageSize(int pageSize) {
        return maxResults(pageSize);
    }

    @Override
    public QueryResult<T> toPage(int page) {
        this.page = page;
        return firstResult(pageSize * page);
    }

    @Override
    public QueryResult<T> nextPage() {
        page = page + 1;
        return firstResult(pageSize * page);
    }

    @Override
    public QueryResult<T> previousPage() {
        page = page > 0 ? page - 1 : page;
        return firstResult(pageSize * page);
    }

    @Override
    public int countPages() {
        return (int) Math.ceil((double) count() / pageSize);
    }

    @Override
    public int currentPage() {
        return page;
    }

    @Override
    public int pageSize() {
        return pageSize;
    }

    private <X> QueryResult<T> changeOrder(EntityViewDefaultQueryResult.ChangeOrder changeOrder) {
        for (CriteriaBuilderPostProcessor processor : context.getCriteriaBuilderPostProcessors()) {
            if (processor instanceof OrderByCriteriaBuilderPostProcessor) {
                OrderByCriteriaBuilderPostProcessor orderBy = (OrderByCriteriaBuilderPostProcessor) processor;
                if (changeOrder.matches(orderBy)) {
                    orderBy.changeDirection();
                    return this;
                }
            }
        }
        changeOrder.addDefault();
        return this;
    }

    private abstract static class ChangeOrder {

        public abstract boolean matches(OrderByCriteriaBuilderPostProcessor orderBy);

        public abstract void addDefault();

    }

}