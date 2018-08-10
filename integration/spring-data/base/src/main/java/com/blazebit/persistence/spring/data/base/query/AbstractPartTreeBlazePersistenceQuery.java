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

package com.blazebit.persistence.spring.data.base.query;

import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.PagedList;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.criteria.BlazeCriteria;
import com.blazebit.persistence.criteria.BlazeCriteriaBuilder;
import com.blazebit.persistence.criteria.BlazeCriteriaQuery;
import com.blazebit.persistence.spring.data.base.query.JpaParameters.JpaParameter;
import com.blazebit.persistence.spring.data.repository.EntityViewSettingProcessor;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.provider.PersistenceProvider;
import org.springframework.data.jpa.repository.query.AbstractJpaQuery;
import org.springframework.data.jpa.repository.query.FixedJpaCountQueryCreator;
import org.springframework.data.jpa.repository.query.FixedJpaQueryCreator;
import org.springframework.data.jpa.repository.query.JpaQueryExecution;
import org.springframework.data.jpa.repository.query.PartTreeJpaQuery;
import org.springframework.data.repository.query.ParameterAccessor;
import org.springframework.data.repository.query.Parameters;
import org.springframework.data.repository.query.ParametersParameterAccessor;
import org.springframework.data.repository.query.parser.PartTree;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Implementation is similar to {@link PartTreeJpaQuery} but was modified to work with entity views.
 *
 * @author Moritz Becker
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractPartTreeBlazePersistenceQuery extends AbstractJpaQuery {

    private static final Pattern QUERY_PATTERN = Pattern.compile("^(find|read|get|query|stream)All$");

    private final Class<?> domainClass;
    private final Class<?> entityViewClass;
    private final PartTree tree;
    private final JpaParameters parameters;

    private final AbstractPartTreeBlazePersistenceQuery.QueryPreparer query;
    private final CriteriaBuilderFactory cbf;
    private final EntityViewManager evm;

    public AbstractPartTreeBlazePersistenceQuery(EntityViewAwareJpaQueryMethod method, EntityManager em, PersistenceProvider persistenceProvider, CriteriaBuilderFactory cbf, EntityViewManager evm) {

        super(method, em);

        this.cbf = cbf;
        this.evm = evm;

        this.entityViewClass = method.getEntityViewClass();
        this.domainClass = method.getEntityInformation().getJavaType();

        this.parameters = method.getJpaParameters();
        String methodName = method.getName();
        boolean skipMethodNamePredicateMatching = QUERY_PATTERN.matcher(methodName).matches();
        String source = skipMethodNamePredicateMatching ? "" : methodName;
        this.tree = new PartTree(source, domainClass);

        boolean hasEntityViewSettingProcessorParameter = parameters.hasEntityViewSettingProcessorParameter();
        boolean recreateQueries = parameters.potentiallySortsDynamically() || entityViewClass != null
            || skipMethodNamePredicateMatching || hasEntityViewSettingProcessorParameter;
        this.query = isCountProjection(tree) ? new AbstractPartTreeBlazePersistenceQuery.CountQueryPreparer(persistenceProvider,
            recreateQueries) : new AbstractPartTreeBlazePersistenceQuery.QueryPreparer(persistenceProvider, recreateQueries);
    }

    protected abstract boolean isCountProjection(PartTree tree);

    protected abstract boolean isDelete(PartTree tree);

    protected abstract int getOffset(Pageable pageable);

    protected abstract int getLimit(Pageable pageable);

    protected abstract ParameterBinder createCriteriaQueryParameterBinder(JpaParameters parameters, Object[] values, List<ParameterMetadataProvider.ParameterMetadata<?>> expressions);

    @Override
    public Query doCreateQuery(Object[] values) {
        return query.createQuery(values);
    }

    @Override
    public TypedQuery<Long> doCreateCountQuery(Object[] values) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected JpaQueryExecution getExecution() {
        if (getQueryMethod().isSliceQuery()) {
            return new SlicedExecution(getQueryMethod().getParameters());
        } else if (getQueryMethod().isPageQuery()) {
            return new PagedExecution(getQueryMethod().getParameters());
        } else {
            return isDelete(this.tree) ? new DeleteExecution(getEntityManager()) : super.getExecution();
        }
    }

    private Query createPaginatedQuery(Object[] values, boolean withCount) {
        return query.createPaginatedQuery(values, withCount);
    }

    /**
     * Uses the {@link com.blazebit.persistence.PaginatedCriteriaBuilder} API for executing the query.
     *
     * @author Christian Beikov
     * @since 1.2.0
     */
    private static class SlicedExecution extends JpaQueryExecution {

        private final Parameters<?, ?> parameters;

        public SlicedExecution(Parameters<?, ?> parameters) {
            this.parameters = parameters;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected Object doExecute(AbstractJpaQuery repositoryQuery, Object[] values) {
            Query paginatedCriteriaBuilder = ((AbstractPartTreeBlazePersistenceQuery) repositoryQuery).createPaginatedQuery(values, false);
            PagedList<Object> resultList = (PagedList<Object>) paginatedCriteriaBuilder.getResultList();
            ParameterAccessor accessor = new ParametersParameterAccessor(parameters, values);
            Pageable pageable = accessor.getPageable();

            return new KeysetAwareSliceImpl<>(resultList, pageable);
        }
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
            Query paginatedCriteriaBuilder = ((AbstractPartTreeBlazePersistenceQuery) repositoryQuery).createPaginatedQuery(values, true);
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
     * {@link JpaQueryExecution} removing entities matching the query.
     *
     * @author Thomas Darimont
     * @author Oliver Gierke
     * @since 1.6
     */
    static class DeleteExecution extends JpaQueryExecution {

        private final EntityManager em;

        public DeleteExecution(EntityManager em) {
            this.em = em;
        }

        /*
         * (non-Javadoc)
         * @see org.springframework.data.jpa.repository.query.JpaQueryExecution#doExecute(org.springframework.data.jpa.repository.query.AbstractJpaQuery, java.lang.Object[])
         */
        @Override
        protected Object doExecute(AbstractJpaQuery jpaQuery, Object[] values) {

            Query query = ((AbstractPartTreeBlazePersistenceQuery) jpaQuery).createQuery(values);
            List<?> resultList = query.getResultList();

            for (Object o : resultList) {
                em.remove(o);
            }

            return jpaQuery.getQueryMethod().isCollectionQuery() ? resultList : resultList.size();
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
        private TypedQuery<?> createQuery(CriteriaQuery<?> criteriaQuery, Object[] values) {
            if (this.cachedCriteriaQuery != null) {
                synchronized (this.cachedCriteriaQuery) {
                    return createQuery0(criteriaQuery, values);
                }
            }
            return createQuery0(criteriaQuery, values);
        }

        protected TypedQuery<?> createQuery0(CriteriaQuery<?> criteriaQuery, Object[] values) {
            processSpecification(criteriaQuery, values);

            com.blazebit.persistence.CriteriaBuilder<?> cb = ((BlazeCriteriaQuery<?>) criteriaQuery).createCriteriaBuilder(getEntityManager());

            if (entityViewClass == null) {
                return cb.getQuery();
            } else {
                EntityViewSetting<?, ?> setting = EntityViewSetting.create(entityViewClass);
                processSetting(setting, values);
                return evm.applySetting(setting, cb).getQuery();
            }
        }

        Query createPaginatedQuery(Object[] values, boolean withCount) {
            CriteriaQuery<?> criteriaQuery = cachedCriteriaQuery;
            List<ParameterMetadataProvider.ParameterMetadata<?>> expressions = this.expressions;
            ParametersParameterAccessor accessor = new ParametersParameterAccessor(parameters, values);

            if (cachedCriteriaQuery == null || accessor.hasBindableNullValue()) {
                FixedJpaQueryCreator creator = createCreator(accessor, persistenceProvider);
                criteriaQuery = invokeQueryCreator(creator, getDynamicSort(values));
                expressions = creator.getParameterExpressions();
            }

            processSpecification(criteriaQuery, values);

            com.blazebit.persistence.CriteriaBuilder<?> cb = ((BlazeCriteriaQuery<?>) criteriaQuery).createCriteriaBuilder(getEntityManager());
            TypedQuery<Object> jpaQuery;
            ParameterBinder binder = getBinder(values, expressions);
            int firstResult = getOffset(binder.getPageable());
            int maxResults = getLimit(binder.getPageable());
            if (entityViewClass == null) {
                if (withCount) {
                    jpaQuery = (TypedQuery<Object>) cb.page(firstResult, maxResults).withCountQuery(true).getQuery();
                } else {
                    jpaQuery = (TypedQuery<Object>) cb.page(firstResult, maxResults + 1).withHighestKeysetOffset(1).withCountQuery(false).getQuery();
                }
            } else {
                if (withCount) {
                    EntityViewSetting<?, ?> setting = EntityViewSetting.create(entityViewClass, firstResult, maxResults);
                    processSetting(setting, values);
                    jpaQuery = (TypedQuery<Object>) ((PaginatedCriteriaBuilder<?>) evm.applySetting(setting, cb)).withCountQuery(true).getQuery();
                } else {
                    EntityViewSetting<?, ?> setting = EntityViewSetting.create(entityViewClass, firstResult, maxResults + 1);
                    processSetting(setting, values);
                    jpaQuery = (TypedQuery<Object>) ((PaginatedCriteriaBuilder<?>) evm.applySetting(setting, cb)).withHighestKeysetOffset(1).withCountQuery(false).getQuery();
                }
            }

            // Just bind the parameters, not the pagination information
            return binder.bind(jpaQuery);
        }

        @SuppressWarnings("unchecked")
        protected <T> void processSetting(EntityViewSetting<T, ?> setting, Object[] values) {
            int entityViewSettingProcessorIndex = parameters.getEntityViewSettingProcessorIndex();
            if (entityViewSettingProcessorIndex >= 0) {
                EntityViewSettingProcessor<T> processor = (EntityViewSettingProcessor<T>) values[entityViewSettingProcessorIndex];
                if (processor != null) {
                    setting = processor.acceptEntityViewSetting(setting);
                }
            }
            for (JpaParameter parameter : parameters.getOptionalParameters()) {
                String parameterName = parameter.getParameterName();
                Object parameterValue = values[parameter.getIndex()];
                setting.addOptionalParameter(parameterName, parameterValue);
            }
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        protected void processSpecification(CriteriaQuery<?> criteriaQuery, Object[] values) {
            BlazeCriteriaQuery<?> blazeCriteriaQuery = (BlazeCriteriaQuery<?>) criteriaQuery;
            int specificationIndex = parameters.getSpecificationIndex();
            if (specificationIndex >= 0) {
                Specification<?> specification = (Specification<?>) values[specificationIndex];
                Root root = criteriaQuery.getRoots().iterator().next();
                BlazeCriteriaBuilder criteriaBuilder = blazeCriteriaQuery.getCriteriaBuilder();
                Predicate predicate = specification.toPredicate(root, criteriaQuery, criteriaBuilder);
                criteriaQuery.where(predicate);
            }
        }

        protected FixedJpaQueryCreator createCreator(ParametersParameterAccessor accessor,
                                                     PersistenceProvider persistenceProvider) {
            BlazeCriteriaQuery<Long> cq = BlazeCriteria.get(cbf, Long.class);
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

            TypedQuery<?> jpaQuery = createQuery(criteriaQuery, values);

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
            return createCriteriaQueryParameterBinder(parameters, values, expressions);
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
    private class CountQueryPreparer extends AbstractPartTreeBlazePersistenceQuery.QueryPreparer {

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
         * @see AbstractPartTreeBlazePersistenceQuery.QueryPreparer#invokeBinding(ParameterBinder,
         *      javax.persistence.TypedQuery)
         */
        @Override
        protected Query invokeBinding(ParameterBinder binder, javax.persistence.TypedQuery<?> query) {
            return binder.bind(query);
        }

        @Override
        protected TypedQuery<?> createQuery0(CriteriaQuery<?> criteriaQuery, Object[] values) {
            return getEntityManager().createQuery(criteriaQuery);
        }

        @Override
        protected CriteriaQuery<?> invokeQueryCreator(FixedJpaQueryCreator creator, Sort sort) {
            return creator.createQuery();
        }
    }
}
