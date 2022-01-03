/*
 * Copyright 2014 - 2022 Blazebit.
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
import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.OrderByBuilder;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.criteria.BlazeCriteria;
import com.blazebit.persistence.criteria.BlazeCriteriaBuilder;
import com.blazebit.persistence.criteria.BlazeCriteriaQuery;
import com.blazebit.persistence.spring.data.base.EntityViewSortUtil;
import com.blazebit.persistence.spring.data.base.query.JpaParameters.JpaParameter;
import com.blazebit.persistence.spring.data.repository.BlazeSpecification;
import com.blazebit.persistence.spring.data.repository.EntityViewSettingProcessor;
import com.blazebit.persistence.spring.data.repository.KeysetPageable;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.Sorter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.provider.PersistenceProvider;
import org.springframework.data.jpa.repository.query.AbstractJpaQuery;
import org.springframework.data.jpa.repository.query.FixedJpaCountQueryCreator;
import org.springframework.data.jpa.repository.query.FixedJpaQueryCreator;
import org.springframework.data.jpa.repository.query.Jpa21Utils;
import org.springframework.data.jpa.repository.query.JpaEntityGraph;
import org.springframework.data.jpa.repository.query.PartTreeJpaQuery;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Implementation is similar to {@link PartTreeJpaQuery} but was modified to work with entity views.
 *
 * About sorting
 * The implementation of both {@link AbstractPartTreeBlazePersistenceQuery} and {@link com.blazebit.persistence.spring.data.base.repository.AbstractEntityViewAwareRepository}
 * aims to support mixed sorting, i.e. sorting by entity view attributes and entity attributes at the same time.
 * To make this work we abstain from using the attribute sorter API of {@link EntityViewSetting} because it would not
 * allow us to add the entity attribute sorts. Instead, we add both entity view attribute and entity attribute sorters
 * uniformly via the core order API of {@link OrderByBuilder} *after* the entity view settings have been applied:
 *   - For entity view attribute sorts we resolve deterministic aliases from the order property that correspond to the
 *     select item aliases
 *   - For entity attribute sorts we just use the order property as is
 *
 * Sorters are retrieved from 4 sources. The order these sources are applied to the final query is as follows:
 *   1. method name sorters e.g. findAllByOrderByNameAsc()
 *   2./3. {@link Pageable} or {@link Sort} method parameter
 *   4. {@link EntityViewSettingProcessor} method parameter that adds attribute sorters to the entity view settings
 *
 * @author Moritz Becker
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractPartTreeBlazePersistenceQuery extends AbstractJpaQuery {

    private static final String QUERY_PATTERN = "find|read|get|query|stream";
    private static final String COUNT_PATTERN = "count";
    private static final String EXISTS_PATTERN = "exists";
    private static final String DELETE_PATTERN = "delete|remove";
    private static final Pattern PREFIX_TEMPLATE = Pattern.compile( //
                    "^(" + QUERY_PATTERN + "|" + COUNT_PATTERN + "|" + EXISTS_PATTERN + "|" + DELETE_PATTERN + ")((\\p{Lu}.*?))??By");

    private final EntityViewAwareJpaQueryMethod method;
    private final Class<?> domainClass;
    private final Class<?> entityViewClass;
    private final PartTree tree;
    private final JpaParameters parameters;

    private final AbstractPartTreeBlazePersistenceQuery.QueryPreparer query;
    private final CriteriaBuilderFactory cbf;
    private final Object escape;
    protected final EntityViewManager evm;

    public AbstractPartTreeBlazePersistenceQuery(EntityViewAwareJpaQueryMethod method, EntityManager em, PersistenceProvider persistenceProvider, Object escape, CriteriaBuilderFactory cbf, EntityViewManager evm) {

        super(method, em);

        this.method = method;
        this.escape = escape;
        this.cbf = cbf;
        this.evm = evm;

        this.entityViewClass = method.getEntityViewClass();
        this.domainClass = method.getEntityInformation().getJavaType();

        this.parameters = method.getJpaParameters();
        String methodName = method.getName();
        boolean skipMethodNamePredicateMatching = !PREFIX_TEMPLATE.matcher(methodName).find();
        String source = skipMethodNamePredicateMatching ? "" : methodName;
        this.tree = new PartTree(source, domainClass);

        boolean hasEntityViewSettingProcessorParameter = parameters.hasEntityViewSettingProcessorParameter();
        boolean hasSpecificationParameter = parameters.hasSpecificationParameter();
        boolean hasCriteriaBuilderProcessorParameter = parameters.hasBlazeSpecificationParameter();
        boolean recreateQueries = parameters.hasDynamicProjection() || parameters.potentiallySortsDynamically()
                || entityViewClass != null
                || skipMethodNamePredicateMatching
                || hasEntityViewSettingProcessorParameter
                || hasSpecificationParameter
                || hasCriteriaBuilderProcessorParameter;
        this.query = isCountProjection(tree) ? new AbstractPartTreeBlazePersistenceQuery.CountQueryPreparer(persistenceProvider,
            recreateQueries) : new AbstractPartTreeBlazePersistenceQuery.QueryPreparer(persistenceProvider, recreateQueries);
    }

    protected abstract ParameterMetadataProvider createParameterMetadataProvider(CriteriaBuilder builder, ParametersParameterAccessor accessor, PersistenceProvider provider, Object escape);

    protected abstract ParameterMetadataProvider createParameterMetadataProvider(CriteriaBuilder builder, JpaParameters parameters, PersistenceProvider provider, Object escape);

    protected abstract boolean isCountProjection(PartTree tree);

    protected boolean isDelete() {
        return isDelete(this.tree);
    }

    protected boolean isExists() {
        return isExists(this.tree);
    }

    protected abstract boolean isDelete(PartTree tree);

    protected abstract boolean isExists(PartTree tree);

    protected abstract int getOffset(Pageable pageable);

    protected abstract int getLimit(Pageable pageable);

    protected abstract ParameterBinder createCriteriaQueryParameterBinder(JpaParameters parameters, Object[] values, List<ParameterMetadataProvider.ParameterMetadata<?>> expressions);

    protected abstract Map<String, Object> tryGetFetchGraphHints(JpaEntityGraph entityGraph, Class<?> entityType);

    public Query doCreateQuery(Object[] values) {
        return query.createQuery(values);
    }

    public Query createPaginatedQuery(Object[] values, boolean withCount) {
        Query paginatedQuery = query.createPaginatedQuery(values, withCount);
        if (method.getLockModeType() != null) {
            paginatedQuery.setLockMode(method.getLockModeType());
        }

        JpaEntityGraph entityGraph = method.getEntityGraph();
        if (entityGraph != null) {
            Map<String, Object> hints = tryGetFetchGraphHints(method.getEntityGraph(), this.getQueryMethod().getEntityInformation().getJavaType());
            for (Map.Entry<String, Object> entry : hints.entrySet()) {
                paginatedQuery.setHint(entry.getKey(), entry.getValue());
            }
        }
        Map<String, String> hints = method.getHints();
        if (!hints.isEmpty()) {
            for (Map.Entry<String, String> entry : hints.entrySet()) {
                paginatedQuery.setHint(entry.getKey(), entry.getValue());
            }
        }
        return paginatedQuery;
    }

    public TypedQuery<Long> doCreateCountQuery(Object[] values) {
        throw new UnsupportedOperationException();
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

            processBlazeSpecification(cb, values);

            Class<?> entityViewClass = AbstractPartTreeBlazePersistenceQuery.this.entityViewClass;
            if (parameters.hasDynamicProjection()) {
                // If the dynamic projection is an entity view, we use that and null it for the result processor
                entityViewClass = (Class<?>) values[parameters.getDynamicProjectionIndex()];
                if (evm.getMetamodel().managedView(entityViewClass) == null) {
                    entityViewClass = null;
                } else {
                    values[parameters.getDynamicProjectionIndex()] = null;
                }
            }
            if (entityViewClass == null) {
                return cb.getQuery();
            } else {
                EntityViewSetting<?, ?> setting = EntityViewSetting.create(entityViewClass);
                // the entity view processor may append attribute sorters to the settings
                setting = processSetting(setting, values);
                // we do not want to apply the sorters during evm.applySetting so we extract them here for later application
                // in order to maintain the defined order of sort applications
                Map<String, Sorter> settingProcessorAttributeSorters = new HashMap<>(setting.getAttributeSorters());
                setting.getAttributeSorters().clear();
                FullQueryBuilder<?, ?> fqb = evm.applySetting(setting, cb);
                processSort(fqb, values, entityViewClass, settingProcessorAttributeSorters);
                return fqb.getQuery();
            }
        }

        Query createPaginatedQuery(Object[] values, boolean withCount) {
            CriteriaQuery<?> criteriaQuery = cachedCriteriaQuery;
            List<ParameterMetadataProvider.ParameterMetadata<?>> expressions = this.expressions;
            ParametersParameterAccessor accessor = new ParametersParameterAccessor(parameters, values);

            if (cachedCriteriaQuery == null || accessor.hasBindableNullValue()) {
                FixedJpaQueryCreator creator = createCreator(accessor, persistenceProvider);
                criteriaQuery = invokeQueryCreator(creator, appliesSortThroughAttributeSorters() ? null : getDynamicSort(values));
                expressions = creator.getParameterExpressions();
            }

            processSpecification(criteriaQuery, values);

            com.blazebit.persistence.CriteriaBuilder<?> cb = ((BlazeCriteriaQuery<?>) criteriaQuery).createCriteriaBuilder(getEntityManager());

            processBlazeSpecification(cb, values);

            TypedQuery<Object> jpaQuery;
            ParameterBinder binder = getBinder(values, expressions);
            int firstResult = getOffset(binder.getPageable());
            int maxResults = getLimit(binder.getPageable());
            Class<?> entityViewClass = AbstractPartTreeBlazePersistenceQuery.this.entityViewClass;
            if (parameters.hasDynamicProjection()) {
                // If the dynamic projection is an entity view, we use that and null it for the result processor
                entityViewClass = (Class<?>) values[parameters.getDynamicProjectionIndex()];
                if (evm.getMetamodel().managedView(entityViewClass) == null) {
                    entityViewClass = null;
                } else {
                    values[parameters.getDynamicProjectionIndex()] = null;
                }
            }

            boolean withKeysetExtraction = false;
            boolean withExtractAllKeysets = false;
            Pageable pageable = binder.getPageable();
            if (!withCount) {
                maxResults++;
            }

            if (entityViewClass == null) {
                PaginatedCriteriaBuilder<Object> paginatedCriteriaBuilder;
                if (pageable instanceof KeysetPageable) {
                    KeysetPageable keysetPageable = (KeysetPageable) pageable;
                    paginatedCriteriaBuilder = (PaginatedCriteriaBuilder<Object>) cb.page(keysetPageable.getKeysetPage(), firstResult, maxResults);
                    withKeysetExtraction = true;
                    withExtractAllKeysets = keysetPageable.isWithExtractAllKeysets();
                } else {
                    paginatedCriteriaBuilder = (PaginatedCriteriaBuilder<Object>) cb.page(firstResult, maxResults);
                }
                if (withKeysetExtraction) {
                    paginatedCriteriaBuilder.withKeysetExtraction(true);
                    paginatedCriteriaBuilder.withExtractAllKeysets(withExtractAllKeysets);
                }
                if (withCount) {
                    paginatedCriteriaBuilder.withCountQuery(true);
                } else {
                    paginatedCriteriaBuilder.withHighestKeysetOffset(1).withCountQuery(false);
                }
                jpaQuery = paginatedCriteriaBuilder.getQuery();
            } else {
                EntityViewSetting<?, ?> setting = EntityViewSetting.create(entityViewClass, firstResult, maxResults);
                if (pageable instanceof KeysetPageable) {
                    KeysetPageable keysetPageable = (KeysetPageable) pageable;
                    withKeysetExtraction = true;
                    withExtractAllKeysets = keysetPageable.isWithExtractAllKeysets();
                    setting.withKeysetPage(keysetPageable.getKeysetPage());
                }
                setting = processSetting(setting, values);
                Map<String, Sorter> settingProcessorAttributeSorters = new HashMap<>(setting.getAttributeSorters());
                setting.getAttributeSorters().clear();
                PaginatedCriteriaBuilder<Object> paginatedCriteriaBuilder = (PaginatedCriteriaBuilder<Object>) evm.applySetting(setting, cb);
                processSort(paginatedCriteriaBuilder, values, entityViewClass, settingProcessorAttributeSorters);
                if (withCount) {
                    paginatedCriteriaBuilder.withCountQuery(true);
                } else {
                    paginatedCriteriaBuilder.withHighestKeysetOffset(1).withCountQuery(false);
                }
                if (withKeysetExtraction) {
                    paginatedCriteriaBuilder.withKeysetExtraction(true);
                    paginatedCriteriaBuilder.withExtractAllKeysets(withExtractAllKeysets);
                }
                jpaQuery = paginatedCriteriaBuilder.getQuery();
            }

            // Just bind the parameters, not the pagination information
            return binder.bind(jpaQuery);
        }

        @SuppressWarnings("unchecked")
        protected <T> EntityViewSetting<? extends T, ?> processSetting(EntityViewSetting<T, ?> setting, Object[] values) {
            EntityViewSetting<? extends T, ?> processedSetting = setting;
            int entityViewSettingProcessorIndex = parameters.getEntityViewSettingProcessorIndex();
            if (entityViewSettingProcessorIndex >= 0) {
                EntityViewSettingProcessor<T> processor = (EntityViewSettingProcessor<T>) values[entityViewSettingProcessorIndex];
                if (processor != null) {
                    processedSetting = processor.acceptEntityViewSetting(setting);
                }
            }
            for (JpaParameter parameter : parameters.getOptionalParameters()) {
                String parameterName = parameter.getParameterName();
                Object parameterValue = values[parameter.getIndex()];
                processedSetting.addOptionalParameter(parameterName, parameterValue);
            }
            return processedSetting;
        }

        protected void processSort(FullQueryBuilder<?, ?> cb, Object[] values, Class<?> entityViewClass, Map<String, Sorter> evsAttributeSorter) {
            Sort sort;
            int sortIndex;
            int pageableIndex;
            if ((sortIndex = parameters.getSortIndex()) >= 0 && (sort = (Sort) values[sortIndex]) != null) {
                EntityViewSortUtil.applySort(evm, entityViewClass, cb, sort);
            } else if ((pageableIndex = parameters.getPageableIndex()) >= 0 && (sort = ((Pageable) values[pageableIndex]).getSort()) != null) {
                EntityViewSortUtil.applySort(evm, entityViewClass, cb, sort);
            }
            for (Map.Entry<String, Sorter> attributeSorterEntry : evsAttributeSorter.entrySet()) {
                attributeSorterEntry.getValue().apply((OrderByBuilder) cb, attributeSorterEntry.getKey());
            }
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        protected void processSpecification(CriteriaQuery<?> criteriaQuery, Object[] values) {
            BlazeCriteriaQuery<?> blazeCriteriaQuery = (BlazeCriteriaQuery<?>) criteriaQuery;
            int specificationIndex = parameters.getSpecificationIndex();
            if (specificationIndex >= 0) {
                Specification<?> specification = (Specification<?>) values[specificationIndex];
                if (specification != null) {
                    Root root = criteriaQuery.getRoots().iterator().next();
                    BlazeCriteriaBuilder criteriaBuilder = blazeCriteriaQuery.getCriteriaBuilder();
                    Predicate predicate = specification.toPredicate(root, criteriaQuery, criteriaBuilder);
                    criteriaQuery.where(predicate);
                }
            }
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        protected void processBlazeSpecification(com.blazebit.persistence.CriteriaBuilder<?> criteriaBuilder, Object[] values) {
            int criteriaBuilderProcessorIndex = parameters.getBlazeSpecificationIndex();
            if (criteriaBuilderProcessorIndex >= 0) {
                String rootAlias = criteriaBuilder.getRoots().iterator().next().getAlias();
                BlazeSpecification blazeSpecification = (BlazeSpecification) values[criteriaBuilderProcessorIndex];
                if (blazeSpecification != null) {
                    blazeSpecification.applySpecification(rootAlias, criteriaBuilder);
                }
            }
        }

        protected FixedJpaQueryCreator createCreator(ParametersParameterAccessor accessor,
                                                     PersistenceProvider persistenceProvider) {
            BlazeCriteriaQuery<Long> cq = BlazeCriteria.get(cbf, Long.class);
            CriteriaBuilder builder = cq.getCriteriaBuilder();

            ParameterMetadataProvider provider = accessor == null
                    ? createParameterMetadataProvider(builder, parameters, persistenceProvider, escape)
                    : createParameterMetadataProvider(builder, accessor, persistenceProvider, escape);

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
                criteriaQuery = invokeQueryCreator(creator, appliesSortThroughAttributeSorters() ? null : getDynamicSort(values));
                expressions = creator.getParameterExpressions();
            }

            TypedQuery<?> jpaQuery = createQuery(criteriaQuery, values);

            return restrictMaxResultsIfNecessary(invokeBinding(getBinder(values, expressions), jpaQuery));
        }

        private boolean appliesSortThroughAttributeSorters() {
            return entityViewClass != null || parameters.hasDynamicProjection();
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
                    ? createParameterMetadataProvider(builder, parameters, persistenceProvider, escape)
                    : createParameterMetadataProvider(builder, accessor, persistenceProvider, escape);

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
