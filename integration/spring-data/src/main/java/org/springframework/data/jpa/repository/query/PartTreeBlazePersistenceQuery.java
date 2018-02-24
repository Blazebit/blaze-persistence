/*
 * Copyright 2014 - 2018 Blazebit.
 * Copyright 2010-2014 the original author or authors.
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

package org.springframework.data.jpa.repository.query;

import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.PagedList;
import com.blazebit.persistence.criteria.BlazeCriteriaQuery;
import com.blazebit.persistence.criteria.impl.BlazeCriteria;
import com.blazebit.persistence.spring.data.impl.query.EntityViewAwareJpaQueryMethod;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.provider.PersistenceProvider;
import org.springframework.data.repository.query.ParameterAccessor;
import org.springframework.data.repository.query.Parameters;
import org.springframework.data.repository.query.ParametersParameterAccessor;
import org.springframework.data.repository.query.parser.PartTree;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.util.Collections;
import java.util.List;

/**
 * Implementation is similar to {@link PartTreeJpaQuery} but was modified to work with entity views.
 *
 *
 * @author Moritz Becker
 * @author Christian Beikov
 * @since 1.2.0
 */
public class PartTreeBlazePersistenceQuery extends AbstractJpaQuery {

    private final Class<?> domainClass;
    private final Class<?> entityViewClass;
    private final PartTree tree;
    private final JpaParameters parameters;

    private final PartTreeBlazePersistenceQuery.QueryPreparer query;
    private final CriteriaBuilderFactory cbf;
    private final EntityViewManager evm;

    public PartTreeBlazePersistenceQuery(JpaQueryMethod method, EntityManager em, PersistenceProvider persistenceProvider, CriteriaBuilderFactory cbf, EntityViewManager evm) {

        super(method, em);

        this.cbf = cbf;
        this.evm = evm;

        this.entityViewClass = ((EntityViewAwareJpaQueryMethod) method).getEntityViewClass();

        this.domainClass = method.getEntityInformation().getJavaType();
        this.tree = new PartTree(method.getName(), domainClass);
        this.parameters = method.getParameters();

        this.query = tree.isCountProjection() ? new PartTreeBlazePersistenceQuery.CountQueryPreparer(persistenceProvider, parameters.potentiallySortsDynamically())
                : new PartTreeBlazePersistenceQuery.QueryPreparer(persistenceProvider, parameters.potentiallySortsDynamically());
    }

    @Override
    public Query doCreateQuery(Object[] values) {
        return query.createQuery(values);
    }

    @Override
    @SuppressWarnings("unchecked")
    public TypedQuery<Long> doCreateCountQuery(Object[] values) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected JpaQueryExecution getExecution() {
        if (getQueryMethod().isPageQuery()) {
            return new PagedExecution(getQueryMethod().getParameters());
        } else {
            return this.tree.isDelete() ? new JpaQueryExecution.DeleteExecution(getEntityManager()) : super.getExecution();
        }
    }

    private Query createPaginatedQuery(Object[] values) {
        return query.createPaginatedQuery(values);
    }

    /**
     * Uses the {@link com.blazebit.persistence.PaginatedCriteriaBuilder} API for executing the query.
     *
     * @author Christian Beikov
     * @since 1.2.0
     */
    private static class PagedExecution extends JpaQueryExecution {

        private final Parameters<?, ?> parameters;

        public PagedExecution(Parameters<?, ?> parameters) {
            this.parameters = parameters;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected Object doExecute(AbstractJpaQuery repositoryQuery, Object[] values) {
            Query paginatedCriteriaBuilder = ((PartTreeBlazePersistenceQuery) repositoryQuery).createPaginatedQuery(values);
            PagedList<Object> resultList = (PagedList<Object>) paginatedCriteriaBuilder.getResultList();
            Long total = resultList.getTotalSize();
            ParameterAccessor accessor = new ParametersParameterAccessor(parameters, values);
            Pageable pageable = accessor.getPageable();

            if (total.equals(0L)) {
                return new KeysetAwarePageImpl<>(Collections.emptyList(), total, null, pageable);
            }

            return new KeysetAwarePageImpl<>(resultList, pageable);
        }
    }

    /**
     * Query preparer to create {@link CriteriaQuery} instances and potentially cache them.
     *
     * @author Oliver Gierke
     * @author Thomas Darimont
     */
    private class QueryPreparer {

        private final CriteriaQuery<?> cachedCriteriaQuery;
        private final List<ParameterMetadataProvider.ParameterMetadata<?>> expressions;
        private final PersistenceProvider persistenceProvider;

        public QueryPreparer(PersistenceProvider persistenceProvider, boolean recreateQueries) {

            this.persistenceProvider = persistenceProvider;

            FixedJpaQueryCreator creator = createCreator(null, persistenceProvider);

            this.cachedCriteriaQuery = recreateQueries ? null : invokeQueryCreator(creator, null);
            this.expressions = recreateQueries ? null : creator.getParameterExpressions();
        }

        /******************************************
         * Moritz Becker, Christian Beikov:
         * The following methods were modified to work with entity views.
         ******************************************/
        private TypedQuery<?> createQuery(CriteriaQuery<?> criteriaQuery) {
            if (this.cachedCriteriaQuery != null) {
                synchronized (this.cachedCriteriaQuery) {
                    return createQuery0(criteriaQuery);
                }
            }
            return createQuery0(criteriaQuery);
        }

        protected TypedQuery<?> createQuery0(CriteriaQuery<?> criteriaQuery) {
            com.blazebit.persistence.CriteriaBuilder<?> cb = ((BlazeCriteriaQuery<?>) criteriaQuery).createCriteriaBuilder();
            if (entityViewClass == null) {
                return cb.getQuery();
            } else {
                EntityViewSetting<?, ?> setting = EntityViewSetting.create(entityViewClass);
                return evm.applySetting(setting, cb).getQuery();
            }
        }

        Query createPaginatedQuery(Object[] values) {
            CriteriaQuery<?> criteriaQuery = cachedCriteriaQuery;
            List<ParameterMetadataProvider.ParameterMetadata<?>> expressions = this.expressions;
            ParametersParameterAccessor accessor = new ParametersParameterAccessor(parameters, values);

            if (cachedCriteriaQuery == null || accessor.hasBindableNullValue()) {
                FixedJpaQueryCreator creator = createCreator(accessor, persistenceProvider);
                criteriaQuery = invokeQueryCreator(creator, getDynamicSort(values));
                expressions = creator.getParameterExpressions();
            }

            com.blazebit.persistence.CriteriaBuilder<?> cb = ((BlazeCriteriaQuery<?>) criteriaQuery).createCriteriaBuilder();
            TypedQuery<Object> jpaQuery;
            ParameterBinder binder = getBinder(values, expressions);
            int firstResult = binder.getPageable().getOffset();
            int maxResults = binder.getPageable().getPageSize();
            if (entityViewClass == null) {
                jpaQuery = (TypedQuery<Object>) cb.page(firstResult, maxResults).getQuery();
            } else {
                EntityViewSetting<?, ?> setting = EntityViewSetting.create(entityViewClass, firstResult, maxResults);
                jpaQuery = (TypedQuery<Object>) evm.applySetting(setting, cb).getQuery();
            }

            // Just bind the parameters, not the pagination information
            return binder.bind(jpaQuery);
        }

        protected FixedJpaQueryCreator createCreator(ParametersParameterAccessor accessor,
                                                     PersistenceProvider persistenceProvider) {
            BlazeCriteriaQuery<Long> cq = BlazeCriteria.get(getEntityManager(), cbf, Long.class);
            CriteriaBuilder builder = cq.getCriteriaBuilder();

            ParameterMetadataProvider provider = accessor == null
                    ? new ParameterMetadataProvider(builder, parameters, persistenceProvider)
                    : new ParameterMetadataProvider(builder, accessor, persistenceProvider);

            return new FixedJpaQueryCreator(tree, domainClass, builder, provider);
        }
        /******************************************
         * end of changes
         ******************************************/

        /**
         * Creates a new {@link Query} for the given parameter values.
         *
         * @param values
         * @return
         */
        public Query createQuery(Object[] values) {
            CriteriaQuery<?> criteriaQuery = cachedCriteriaQuery;
            List<ParameterMetadataProvider.ParameterMetadata<?>> expressions = this.expressions;
            ParametersParameterAccessor accessor = new ParametersParameterAccessor(parameters, values);

            if (cachedCriteriaQuery == null || accessor.hasBindableNullValue()) {
                FixedJpaQueryCreator creator = createCreator(accessor, persistenceProvider);
                criteriaQuery = invokeQueryCreator(creator, getDynamicSort(values));
                expressions = creator.getParameterExpressions();
            }

            TypedQuery<?> jpaQuery = createQuery(criteriaQuery);

            return restrictMaxResultsIfNecessary(invokeBinding(getBinder(values, expressions), jpaQuery));
        }

        protected CriteriaQuery<?> invokeQueryCreator(FixedJpaQueryCreator creator, Sort sort) {
            if (sort == null) {
                return creator.createQuery().select(null);
            } else {
                return creator.createQuery(sort).select(null);
            }
        }

        /**
         * Restricts the max results of the given {@link Query} if the current {@code tree} marks this {@code query} as
         * limited.
         *
         * @param query
         * @return
         */
        private Query restrictMaxResultsIfNecessary(Query query) {
            if (tree.isLimiting()) {
                if (query.getMaxResults() != Integer.MAX_VALUE) {
                    /*
                     * In order to return the correct results, we have to adjust the first result offset to be returned if:
                     * - a Pageable parameter is present
                     * - AND the requested page number > 0
                     * - AND the requested page size was bigger than the derived result limitation via the First/Top keyword.
                     */
                    if (query.getMaxResults() > tree.getMaxResults() && query.getFirstResult() > 0) {
                        query.setFirstResult(query.getFirstResult() - (query.getMaxResults() - tree.getMaxResults()));
                    }
                }

                query.setMaxResults(tree.getMaxResults());
            }

            return query;
        }

        /**
         * Invokes parameter binding on the given {@link TypedQuery}.
         *
         * @param binder
         * @param query
         * @return
         */
        protected Query invokeBinding(ParameterBinder binder, TypedQuery<?> query) {
            return binder.bindAndPrepare(query);
        }

        private ParameterBinder getBinder(Object[] values, List<ParameterMetadataProvider.ParameterMetadata<?>> expressions) {
            return new CriteriaQueryParameterBinder(parameters, values, expressions);
        }

        private Sort getDynamicSort(Object[] values) {
            return parameters.potentiallySortsDynamically() ? new ParametersParameterAccessor(parameters, values).getSort()
                    : null;
        }
    }

    /**
     * Special {@link PartTreeJpaQuery.QueryPreparer} to create count queries.
     *
     * @author Oliver Gierke
     * @author Thomas Darimont
     */
    private class CountQueryPreparer extends PartTreeBlazePersistenceQuery.QueryPreparer {

        public CountQueryPreparer(PersistenceProvider persistenceProvider, boolean recreateQueries) {
            super(persistenceProvider, recreateQueries);
        }

        @Override
        protected FixedJpaQueryCreator createCreator(ParametersParameterAccessor accessor,
                                                PersistenceProvider persistenceProvider) {

            EntityManager entityManager = getEntityManager();
            CriteriaBuilder builder = entityManager.getCriteriaBuilder();

            ParameterMetadataProvider provider = accessor == null
                    ? new ParameterMetadataProvider(builder, parameters, persistenceProvider)
                    : new ParameterMetadataProvider(builder, accessor, persistenceProvider);

            return new FixedJpaCountQueryCreator(tree, domainClass, builder, provider);
        }

        /**
         * Customizes binding by skipping the pagination.
         *
         * @see org.springframework.data.jpa.repository.query.PartTreeJpaQuery.QueryPreparer#invokeBinding(org.springframework.data.jpa.repository.query.ParameterBinder,
         *      javax.persistence.TypedQuery)
         */
        @Override
        protected Query invokeBinding(ParameterBinder binder, javax.persistence.TypedQuery<?> query) {
            return binder.bind(query);
        }

        @Override
        protected TypedQuery<?> createQuery0(CriteriaQuery<?> criteriaQuery) {
            return getEntityManager().createQuery(criteriaQuery);
        }

        @Override
        protected CriteriaQuery<?> invokeQueryCreator(FixedJpaQueryCreator creator, Sort sort) {
            return creator.createQuery(sort);
        }
    }
}