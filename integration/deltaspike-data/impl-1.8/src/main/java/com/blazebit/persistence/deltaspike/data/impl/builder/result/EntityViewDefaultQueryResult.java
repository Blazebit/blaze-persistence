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

import com.blazebit.persistence.PaginatedTypedQuery;
import com.blazebit.persistence.deltaspike.data.ExtendedQueryResult;
import com.blazebit.persistence.deltaspike.data.base.builder.postprocessor.CountCriteriaBuilderQueryCreator;
import com.blazebit.persistence.deltaspike.data.base.builder.postprocessor.OrderByCriteriaBuilderPostProcessor;
import com.blazebit.persistence.deltaspike.data.base.builder.postprocessor.PaginationCriteriaBuilderPostProcessor;
import com.blazebit.persistence.deltaspike.data.base.handler.CriteriaBuilderPostProcessor;
import com.blazebit.persistence.deltaspike.data.impl.builder.EntityViewQueryBuilder;
import com.blazebit.persistence.deltaspike.data.impl.handler.EntityViewCdiQueryInvocationContext;
import org.apache.deltaspike.data.impl.builder.OrderDirection;
import org.apache.deltaspike.data.impl.builder.postprocessor.CountQueryPostProcessor;
import org.apache.deltaspike.data.impl.builder.postprocessor.FirstResultPostProcessor;
import org.apache.deltaspike.data.impl.builder.postprocessor.FlushModePostProcessor;
import org.apache.deltaspike.data.impl.builder.postprocessor.HintPostProcessor;
import org.apache.deltaspike.data.impl.builder.postprocessor.LockModePostProcessor;
import org.apache.deltaspike.data.impl.builder.postprocessor.MaxResultPostProcessor;
import org.apache.deltaspike.data.impl.builder.postprocessor.OrderByQueryStringPostProcessor;
import org.apache.deltaspike.data.impl.handler.QueryStringPostProcessor;

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
public class EntityViewDefaultQueryResult<T> implements ExtendedQueryResult<T> {

    private final EntityViewQueryBuilder builder;
    private final EntityViewCdiQueryInvocationContext context;

    private PaginatedTypedQuery<T> lastPaginatedQuery;
    private boolean withCount = false;
    private int page = 0;
    private int pageSize = 10;
    private int firstResult = 0;
    private int maxResults = -1;

    public EntityViewDefaultQueryResult(EntityViewQueryBuilder builder, EntityViewCdiQueryInvocationContext context) {
        this.builder = builder;
        this.context = context;
    }

    @Override
    public ExtendedQueryResult<T> withCountQuery(boolean withCountQuery) {
        this.withCount = withCountQuery;
        return this;
    }

    @Override
    public <X> ExtendedQueryResult<T> orderAsc(SingularAttribute<T, X> attribute) {
        return orderAsc(attribute, true);
    }

    @Override
    public <X> ExtendedQueryResult<T> orderAsc(SingularAttribute<T, X> attribute, boolean appendEntityName) {
        lastPaginatedQuery = null;
        if (context.getRepositoryMethodMetadata().getQuery() == null) {
            context.addCriteriaBuilderPostProcessor(new OrderByCriteriaBuilderPostProcessor(attribute, OrderDirection.ASC));
        } else {
            context.addQueryStringPostProcessor(new OrderByQueryStringPostProcessor(attribute, OrderDirection.ASC, appendEntityName));
        }
        return this;
    }

    @Override
    public ExtendedQueryResult<T> orderAsc(String attribute) {
        return orderAsc(attribute, true);
    }

    @Override
    public ExtendedQueryResult<T> orderAsc(String attribute, boolean appendEntityName) {
        lastPaginatedQuery = null;
        if (context.getRepositoryMethodMetadata().getQuery() == null) {
            context.addCriteriaBuilderPostProcessor(new OrderByCriteriaBuilderPostProcessor(attribute, OrderDirection.ASC));
        } else {
            context.addQueryStringPostProcessor(new OrderByQueryStringPostProcessor(attribute, OrderDirection.ASC, appendEntityName));
        }
        return this;
    }

    @Override
    public <X> ExtendedQueryResult<T> orderDesc(SingularAttribute<T, X> attribute) {
        return orderDesc(attribute, true);
    }

    @Override
    public <X> ExtendedQueryResult<T> orderDesc(SingularAttribute<T, X> attribute, boolean appendEntityName) {
        lastPaginatedQuery = null;
        if (context.getRepositoryMethodMetadata().getQuery() == null) {
            context.addCriteriaBuilderPostProcessor(new OrderByCriteriaBuilderPostProcessor(attribute, OrderDirection.DESC));
        } else {
            context.addQueryStringPostProcessor(new OrderByQueryStringPostProcessor(attribute, OrderDirection.DESC, appendEntityName));
        }
        return this;
    }

    @Override
    public ExtendedQueryResult<T> orderDesc(String attribute) {
        return orderDesc(attribute, true);
    }

    @Override
    public ExtendedQueryResult<T> orderDesc(String attribute, boolean appendEntityName) {
        lastPaginatedQuery = null;
        if (context.getRepositoryMethodMetadata().getQuery() == null) {
            context.addCriteriaBuilderPostProcessor(new OrderByCriteriaBuilderPostProcessor(attribute, OrderDirection.DESC));
        } else {
            context.addQueryStringPostProcessor(new OrderByQueryStringPostProcessor(attribute, OrderDirection.DESC, appendEntityName));
        }
        return this;
    }

    @Override
    public <X> ExtendedQueryResult<T> changeOrder(final SingularAttribute<T, X> attribute) {
        changeOrder(new EntityViewDefaultQueryResult.ChangeOrder() {
            @Override
            public boolean matches(OrderByQueryStringPostProcessor orderBy) {
                return orderBy.matches(attribute);
            }

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
    public ExtendedQueryResult<T> changeOrder(final String attribute) {
        changeOrder(new EntityViewDefaultQueryResult.ChangeOrder() {
            @Override
            public boolean matches(OrderByQueryStringPostProcessor orderBy) {
                return orderBy.matches(attribute);
            }

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
    public ExtendedQueryResult<T> clearOrder() {
        lastPaginatedQuery = null;
        if (context.getRepositoryMethodMetadata().getQuery() == null) {
            for (Iterator<CriteriaBuilderPostProcessor> it = context.getCriteriaBuilderPostProcessors().iterator(); it.hasNext(); ) {
                if (it.next() instanceof OrderByCriteriaBuilderPostProcessor) {
                    it.remove();
                }
            }
        } else {
            for (Iterator<QueryStringPostProcessor> it = context.getQueryStringPostProcessors().iterator(); it.hasNext(); ) {
                if (it.next() instanceof OrderByQueryStringPostProcessor) {
                    it.remove();
                }
            }
        }
        return this;
    }

    @Override
    public ExtendedQueryResult<T> maxResults(int max) {
        lastPaginatedQuery = null;
        if (context.getRepositoryMethodMetadata().getQuery() == null) {
            maxResults = max;
            pageSize = max;
        } else {
            context.addJpaQueryPostProcessor(new MaxResultPostProcessor(max));
        }
        return this;
    }

    @Override
    public ExtendedQueryResult<T> firstResult(int first) {
        lastPaginatedQuery = null;
        if (context.getRepositoryMethodMetadata().getQuery() == null) {
            firstResult = first;
        } else {
            context.addJpaQueryPostProcessor(new FirstResultPostProcessor(first));
        }
        return this;
    }

    private boolean updatePaginationPostProcessor() {
        clearPagination();
        if (context.getRepositoryMethodMetadata().getQuery() == null) {
            if (isFirstResultSet() || isMaxResultsSet()) {
                context.addCriteriaBuilderPostProcessor(new PaginationCriteriaBuilderPostProcessor(firstResult, maxResults < 0 ? Integer.MAX_VALUE : maxResults));
                return true;
            }
        }
        return false;
    }

    private void clearPagination() {
        if (context.getRepositoryMethodMetadata().getQuery() == null) {
            Iterator<CriteriaBuilderPostProcessor> iterator = context.getCriteriaBuilderPostProcessors().iterator();
            while (iterator.hasNext()) {
                if (iterator.next() instanceof PaginationCriteriaBuilderPostProcessor) {
                    iterator.remove();
                }
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
    public ExtendedQueryResult<T> lockMode(LockModeType lockMode) {
        lastPaginatedQuery = null;
        context.addJpaQueryPostProcessor(new LockModePostProcessor(lockMode));
        return this;
    }

    @Override
    public ExtendedQueryResult<T> flushMode(FlushModeType flushMode) {
        lastPaginatedQuery = null;
        context.addJpaQueryPostProcessor(new FlushModePostProcessor(flushMode));
        return this;
    }

    @Override
    public ExtendedQueryResult<T> hint(String hint, Object value) {
        lastPaginatedQuery = null;
        context.addJpaQueryPostProcessor(new HintPostProcessor(hint, value));
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<T> getResultList() {
        return getResultList(withCount);
    }

    @Override
    public List<T> getPageResultList() {
        return getResultList(false);
    }

    public List<T> getResultList(boolean withCountQuery) {
        if (lastPaginatedQuery != null) {
            if (withCountQuery) {
                return lastPaginatedQuery.getResultList();
            } else {
                return lastPaginatedQuery.getPageResultList();
            }
        }
        updatePaginationPostProcessor();
        Query query = (Query) builder.executeQuery(context);
        if (query instanceof PaginatedTypedQuery<?>) {
            lastPaginatedQuery = (PaginatedTypedQuery<T>) query;
            if (withCountQuery) {
                return lastPaginatedQuery.getResultList();
            } else {
                return lastPaginatedQuery.getPageResultList();
            }
        }

        return query.getResultList();
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
        if (context.getRepositoryMethodMetadata().getQuery() == null) {
            if (lastPaginatedQuery != null) {
                return lastPaginatedQuery.getTotalCount();
            }
            if (updatePaginationPostProcessor()) {
                Query query = (Query) builder.executeQuery(context);
                if (query instanceof PaginatedTypedQuery<?>) {
                    lastPaginatedQuery = (PaginatedTypedQuery<T>) query;
                    return lastPaginatedQuery.getTotalCount();
                }
            }

            context.setQueryCreator(new CountCriteriaBuilderQueryCreator());
            try {
                Long result = (Long) ((Query) builder.executeQuery(context)).getSingleResult();
                return result;
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                context.setQueryCreator(null);
            }
        } else {
            CountQueryPostProcessor counter = new CountQueryPostProcessor();
            context.addJpaQueryPostProcessor(counter);
            try {
                Long result = (Long) ((Query) builder.executeQuery(context)).getSingleResult();
                return result;
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                context.removeJpaQueryPostProcessor(counter);
            }
        }
    }

    @Override
    public ExtendedQueryResult<T> withPageSize(int pageSize) {
        return maxResults(pageSize);
    }

    @Override
    public ExtendedQueryResult<T> toPage(int page) {
        this.page = page;
        return firstResult(pageSize * page);
    }

    @Override
    public ExtendedQueryResult<T> nextPage() {
        page = page + 1;
        return firstResult(pageSize * page);
    }

    @Override
    public ExtendedQueryResult<T> previousPage() {
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

    private <X> ExtendedQueryResult<T> changeOrder(EntityViewDefaultQueryResult.ChangeOrder changeOrder) {
        lastPaginatedQuery = null;

        if (context.getRepositoryMethodMetadata().getQuery() == null) {
            for (CriteriaBuilderPostProcessor processor : context.getCriteriaBuilderPostProcessors()) {
                if (processor instanceof OrderByCriteriaBuilderPostProcessor) {
                    OrderByCriteriaBuilderPostProcessor orderBy = (OrderByCriteriaBuilderPostProcessor) processor;
                    if (changeOrder.matches(orderBy)) {
                        orderBy.changeDirection();
                        return this;
                    }
                }
            }
        } else {
            for (QueryStringPostProcessor processor : context.getQueryStringPostProcessors()) {
                if (processor instanceof OrderByQueryStringPostProcessor) {
                    OrderByQueryStringPostProcessor orderBy = (OrderByQueryStringPostProcessor) processor;
                    if (changeOrder.matches(orderBy)) {
                        orderBy.changeDirection();
                        return this;
                    }
                }
            }
        }
        changeOrder.addDefault();
        return this;
    }

    /**
     * @author Moritz Becker
     * @since 1.2.0
     */
    private abstract static class ChangeOrder {

        public abstract boolean matches(OrderByQueryStringPostProcessor orderBy);

        public abstract boolean matches(OrderByCriteriaBuilderPostProcessor orderBy);

        public abstract void addDefault();

    }

}