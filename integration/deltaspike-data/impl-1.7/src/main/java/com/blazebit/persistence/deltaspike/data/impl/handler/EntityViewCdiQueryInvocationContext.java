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

package com.blazebit.persistence.deltaspike.data.impl.handler;

import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.deltaspike.data.base.handler.CriteriaBuilderPostProcessor;
import com.blazebit.persistence.deltaspike.data.base.handler.CriteriaBuilderQueryCreator;
import com.blazebit.persistence.deltaspike.data.impl.meta.EntityViewRepositoryMethod;
import com.blazebit.persistence.deltaspike.data.impl.param.ExtendedParameters;
import com.blazebit.persistence.view.EntityViewManager;
import org.apache.deltaspike.data.api.EntityGraph;
import org.apache.deltaspike.data.api.SingleResultType;
import org.apache.deltaspike.data.api.mapping.QueryInOutMapper;
import org.apache.deltaspike.data.impl.graph.EntityGraphHelper;
import org.apache.deltaspike.data.impl.handler.QueryStringPostProcessor;
import org.apache.deltaspike.data.impl.util.bean.Destroyable;
import org.apache.deltaspike.data.spi.QueryInvocationContext;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.persistence.QueryHint;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

/**
 * Implementation is similar to {@link org.apache.deltaspike.data.impl.handler.CdiQueryInvocationContext} but was modified to
 * work with entity views.
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public class EntityViewCdiQueryInvocationContext implements QueryInvocationContext {

    private final EntityManager entityManager;
    private final EntityViewManager entityViewManager;
    private final CriteriaBuilderFactory criteriaBuilderFactory;
    private final ExtendedParameters params;
    private final Class<?> entityClass;
    private final Class<?> entityViewClass;
    private final Object proxy;
    private final Method method;
    private final Object[] args;
    private final EntityViewRepositoryMethod repoMethod;
    private final List<QueryStringPostProcessor> queryPostProcessors;
    private final List<com.blazebit.persistence.deltaspike.data.base.handler.CriteriaBuilderPostProcessor> criteriaBuilderPostProcessors;
    private final List<EntityViewJpaQueryPostProcessor> jpaPostProcessors;
    private final List<Destroyable> cleanup;
    private CriteriaBuilderQueryCreator queryCreator;
    private String queryString;

    public EntityViewCdiQueryInvocationContext(Object proxy, Method method, Object[] args, EntityViewRepositoryMethod repoMethod,
                                               EntityManager entityManager, EntityViewManager entityViewManager, CriteriaBuilderFactory criteriaBuilderFactory) {
        this.entityManager = entityManager;
        this.entityViewManager = entityViewManager;
        this.criteriaBuilderFactory = criteriaBuilderFactory;
        this.args = args == null ? new Object[]{} : args;
        this.params = ExtendedParameters.create(method, this.args, repoMethod);
        this.proxy = proxy;
        this.method = method;
        this.repoMethod = repoMethod;
        this.entityClass = repoMethod.getRepository().getEntityClass();
        this.entityViewClass = repoMethod.getEntityViewClass();
        this.criteriaBuilderPostProcessors = new LinkedList<>();
        this.queryPostProcessors = new LinkedList<>();
        this.jpaPostProcessors = new LinkedList<>();
        this.cleanup = new LinkedList<>();
    }

    public void initMapper() {
        if (hasQueryInOutMapper()) {
            QueryInOutMapper<?> mapper = getQueryInOutMapper();
            params.applyMapper(mapper);
            for (int i = 0; i < args.length; i++) {
                if (mapper.mapsParameter(args[i])) {
                    args[i] = mapper.mapParameter(args[i]);
                }
            }
        }
    }

    @Override
    public EntityManager getEntityManager() {
        return entityManager;
    }

    public EntityViewManager getEntityViewManager() {
        return entityViewManager;
    }

    public CriteriaBuilderFactory getCriteriaBuilderFactory() {
        return criteriaBuilderFactory;
    }

    @Override
    public boolean isNew(Object entity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Class<?> getEntityClass() {
        return entityClass;
    }

    public Class<?> getEntityViewClass() {
        return entityViewClass;
    }

    @Override
    public Class<?> getRepositoryClass() {
        return repoMethod.getRepository().getRepositoryClass();
    }

    public Object proceed() throws Exception {
        return method.invoke(proxy, args);
    }

    @Override
    public Method getMethod() {
        return method;
    }

    public Query applyRestrictions(Query query) {
        return applyRestrictions(query, true);
    }

    public Query applyRestrictions(Query query, boolean applyFetchGraph) {
        Method method = getMethod();
        if (hasLockMode(method)) {
            query.setLockMode(extractLockMode(method));
        }
        if (hasQueryHints(method)) {
            QueryHint[] hints = extractQueryHints(method);
            for (QueryHint hint : hints) {
                query.setHint(hint.name(), hint.value());
            }
        }
        if (applyFetchGraph) {
            applyEntityGraph(query, method);
        }
        return applyJpaQueryPostProcessors(query);
    }

    public Object[] getMethodParameters() {
        return args;
    }

    public void addQueryStringPostProcessor(QueryStringPostProcessor postProcessor) {
        queryPostProcessors.add(postProcessor);
    }

    public void addCriteriaBuilderPostProcessor(com.blazebit.persistence.deltaspike.data.base.handler.CriteriaBuilderPostProcessor postProcessor) {
        criteriaBuilderPostProcessors.add(postProcessor);
    }

    public void setQueryCreator(CriteriaBuilderQueryCreator queryCreator) {
        this.queryCreator = queryCreator;
    }

    public Query createQuery(FullQueryBuilder<?, ?> queryBuilder) {
        if (queryCreator == null) {
            return queryBuilder.getQuery();
        }
        return queryCreator.createQuery(queryBuilder);
    }

    public List<com.blazebit.persistence.deltaspike.data.base.handler.CriteriaBuilderPostProcessor> getCriteriaBuilderPostProcessors() {
        return criteriaBuilderPostProcessors;
    }

    public boolean hasCriteriaBuilderPostProcessors() {
        return !criteriaBuilderPostProcessors.isEmpty();
    }

    public FullQueryBuilder<?, ?> applyCriteriaBuilderPostProcessors(FullQueryBuilder<?, ?> criteriaBuilder) {
        FullQueryBuilder<?, ?> fullCb = criteriaBuilder;
        for (CriteriaBuilderPostProcessor processor : criteriaBuilderPostProcessors) {
            fullCb = processor.postProcess(fullCb);
        }
        return fullCb;
    }

    public void addJpaQueryPostProcessor(EntityViewJpaQueryPostProcessor postProcessor) {
        jpaPostProcessors.add(postProcessor);
    }

    public void removeJpaQueryPostProcessor(EntityViewJpaQueryPostProcessor postProcessor) {
        jpaPostProcessors.remove(postProcessor);
    }

    public boolean hasQueryStringPostProcessors() {
        return !queryPostProcessors.isEmpty();
    }

    public String applyQueryStringPostProcessors(String queryString) {
        String result = queryString;
        for (QueryStringPostProcessor processor : queryPostProcessors) {
            result = processor.postProcess(result);
        }
        return result;
    }

    public Query applyJpaQueryPostProcessors(Query query) {
        Query result = query;
        for (EntityViewJpaQueryPostProcessor processor : jpaPostProcessors) {
            result = processor.postProcess(this, result);
        }
        return result;
    }

    public void addDestroyable(Destroyable destroyable) {
        cleanup.add(destroyable);
    }

    public void cleanup() {
        for (Destroyable destroy : cleanup) {
            destroy.destroy();
        }
        cleanup.clear();
    }

    public Object executeQuery(Query jpaQuery) {
        return repoMethod.getEntityViewQueryProcessor().executeQuery(jpaQuery, this);
    }

    public ExtendedParameters getParams() {
        return params;
    }

    public EntityViewRepositoryMethod getRepositoryMethod() {
        return repoMethod;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public List<QueryStringPostProcessor> getQueryStringPostProcessors() {
        return queryPostProcessors;
    }

    public boolean hasQueryInOutMapper() {
        return repoMethod.hasQueryInOutMapper();
    }

    public QueryInOutMapper<?> getQueryInOutMapper() {
        return repoMethod.getQueryInOutMapperInstance(this);
    }

    public SingleResultType getSingleResultStyle() {
        SingleResultType baseSingleResultType = repoMethod.getSingleResultStyle();
        if (repoMethod.isOptional() && baseSingleResultType == SingleResultType.JPA) {
            return SingleResultType.OPTIONAL;
        } else {
            return baseSingleResultType;
        }
    }

    public Object getProxy() {
        return proxy;
    }

    private boolean hasLockMode(Method method) {
        return extractLockMode(method) != null;
    }

    private LockModeType extractLockMode(Method method) {
        Class<org.apache.deltaspike.data.api.Query> query = org.apache.deltaspike.data.api.Query.class;
        if (method.isAnnotationPresent(query) &&
                method.getAnnotation(query).lock() != LockModeType.NONE) {
            return method.getAnnotation(query).lock();
        }
        return null;
    }

    private QueryHint[] extractQueryHints(Method method) {
        Class<org.apache.deltaspike.data.api.Query> query = org.apache.deltaspike.data.api.Query.class;
        if (method.isAnnotationPresent(query) &&
                method.getAnnotation(query).hints().length > 0) {
            return method.getAnnotation(query).hints();
        }
        return null;
    }

    private boolean hasQueryHints(Method method) {
        return extractQueryHints(method) != null;
    }

    private void applyEntityGraph(Query query, Method method) {
        EntityGraph entityGraphAnn = method.getAnnotation(EntityGraph.class);
        if (entityGraphAnn == null) {
            return;
        }

        Object graph = EntityGraphHelper.getEntityGraph(getEntityManager(), entityClass, entityGraphAnn);
        query.setHint(entityGraphAnn.type().getHintName(), graph);
    }

    public boolean isOptional() {
        return this.repoMethod.isOptional();
    }
}