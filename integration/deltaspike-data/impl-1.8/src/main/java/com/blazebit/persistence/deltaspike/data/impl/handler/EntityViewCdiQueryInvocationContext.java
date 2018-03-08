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
import com.blazebit.persistence.deltaspike.data.impl.meta.EntityViewAwareRepositoryMetadata;
import com.blazebit.persistence.deltaspike.data.impl.meta.EntityViewAwareRepositoryMethodMetadata;
import com.blazebit.persistence.view.EntityViewManager;
import org.apache.deltaspike.data.api.EntityGraph;
import org.apache.deltaspike.data.impl.graph.EntityGraphHelper;
import org.apache.deltaspike.data.impl.handler.CdiQueryInvocationContext;

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
public class EntityViewCdiQueryInvocationContext extends CdiQueryInvocationContext {

    private final EntityViewManager entityViewManager;
    private final CriteriaBuilderFactory criteriaBuilderFactory;
    private final Class<?> entityViewClass;
    private final List<CriteriaBuilderPostProcessor> criteriaBuilderPostProcessors;

    public EntityViewCdiQueryInvocationContext(Object proxy, Method method, Object[] args,
                                               EntityViewAwareRepositoryMetadata repositoryMetadata, EntityViewAwareRepositoryMethodMetadata repositoryMethodMetadata,
                                               EntityManager entityManager, EntityViewManager entityViewManager, CriteriaBuilderFactory criteriaBuilderFactory) {
        super(proxy, method, args, repositoryMetadata, repositoryMethodMetadata, entityManager);
        this.entityViewManager = entityViewManager;
        this.criteriaBuilderFactory = criteriaBuilderFactory;
        this.entityViewClass = repositoryMethodMetadata.getEntityViewClass();
        this.criteriaBuilderPostProcessors = new LinkedList<>();
    }

    public EntityViewManager getEntityViewManager() {
        return entityViewManager;
    }

    public CriteriaBuilderFactory getCriteriaBuilderFactory() {
        return criteriaBuilderFactory;
    }

    public Class<?> getEntityViewClass() {
        return entityViewClass;
    }

    public void addCriteriaBuilderPostProcessor(CriteriaBuilderPostProcessor postProcessor) {
        criteriaBuilderPostProcessors.add(postProcessor);
    }

    public List<CriteriaBuilderPostProcessor> getCriteriaBuilderPostProcessors() {
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

    @Override
    public EntityViewAwareRepositoryMetadata getRepositoryMetadata() {
        return (EntityViewAwareRepositoryMetadata) super.getRepositoryMetadata();
    }

    @Override
    public EntityViewAwareRepositoryMethodMetadata getRepositoryMethodMetadata() {
        return (EntityViewAwareRepositoryMethodMetadata) super.getRepositoryMethodMetadata();
    }

    @Override
    public Query applyRestrictions(Query query) {
        // Note that we skip setting the firstResult and maxResults here since we use the PaginatedCriteriaBuilder API
        Method method = getMethod();

        LockModeType lockMode = extractLockMode();
        if (lockMode != null) {
            query.setLockMode(lockMode);
        }

        QueryHint[] hints = extractQueryHints();
        if (hints != null) {
            for (QueryHint hint : hints) {
                query.setHint(hint.name(), hint.value());
            }
        }

        applyEntityGraph(query, method);
        query = applyJpaQueryPostProcessors(query);
        return query;
    }

    private LockModeType extractLockMode() {
        org.apache.deltaspike.data.api.Query query = getRepositoryMethodMetadata().getQuery();
        if (query != null && query.lock() != LockModeType.NONE) {
            return query.lock();
        }

        return null;
    }

    private QueryHint[] extractQueryHints() {
        org.apache.deltaspike.data.api.Query query = getRepositoryMethodMetadata().getQuery();
        if (query != null && query.hints().length > 0) {
            return query.hints();
        }

        return null;
    }

    private void applyEntityGraph(Query query, Method method) {
        EntityGraph entityGraphAnn = method.getAnnotation(EntityGraph.class);
        if (entityGraphAnn == null) {
            return;
        }

        Object graph = EntityGraphHelper.getEntityGraph(getEntityManager(), getRepositoryMetadata().getEntityMetadata().getEntityClass(), entityGraphAnn);
        query.setHint(entityGraphAnn.type().getHintName(), graph);
    }

}