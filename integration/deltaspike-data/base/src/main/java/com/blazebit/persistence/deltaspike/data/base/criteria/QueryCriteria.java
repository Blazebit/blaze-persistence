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

package com.blazebit.persistence.deltaspike.data.base.criteria;

import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.criteria.BlazeCriteriaBuilder;
import com.blazebit.persistence.criteria.BlazeCriteriaQuery;
import com.blazebit.persistence.criteria.BlazeCriteria;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import org.apache.deltaspike.data.api.criteria.Criteria;
import org.apache.deltaspike.data.api.criteria.QuerySelection;
import org.apache.deltaspike.data.impl.criteria.predicate.PredicateBuilder;
import org.apache.deltaspike.data.impl.criteria.processor.QueryProcessor;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation is similar to {@link org.apache.deltaspike.data.impl.criteria.QueryCriteria} but was modified to
 * work with entity views.
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public class QueryCriteria<C, R> extends org.apache.deltaspike.data.impl.criteria.QueryCriteria<C, R> {

    private static final Logger LOG = Logger.getLogger(QueryCriteria.class.getName());

    private boolean entityViewCriteria;
    private final Class<C> entityClass;
    private final EntityManager entityManager;
    private final Class<R> resultClass;
    private final CriteriaBuilderFactory criteriaBuilderFactory;
    private final EntityViewManager entityViewManager;

    public QueryCriteria(Class<C> entityClass, Class<R> resultClass, EntityManager entityManager, CriteriaBuilderFactory criteriaBuilderFactory, EntityViewManager entityViewManager) {
        this(entityClass, resultClass, entityManager, null, criteriaBuilderFactory, entityViewManager);
    }

    public QueryCriteria(Class<C> entityClass, Class<R> resultClass, EntityManager entityManager, JoinType joinType, CriteriaBuilderFactory criteriaBuilderFactory, EntityViewManager entityViewManager) {
        super(entityClass, resultClass, entityManager, joinType);
        this.entityClass = entityClass;
        this.resultClass = resultClass;
        this.entityManager = entityManager;
        this.criteriaBuilderFactory = criteriaBuilderFactory;
        this.entityViewManager = entityViewManager;
        this.entityViewCriteria = entityViewManager.getMetamodel().managedView(resultClass) != null;
    }

    @Override
    public TypedQuery<R> createQuery() {
        if (entityViewCriteria) {
            try {
                BlazeCriteriaBuilder builder = BlazeCriteria.get(criteriaBuilderFactory);
                BlazeCriteriaQuery<C> query = builder.createQuery(entityClass);
                From<C, C> root = query.from(entityClass);
                List<Predicate> predicates = predicates(builder, root);
                query.distinct(isDistinct());
                if (!predicates.isEmpty()) {
                    query.where(predicates.toArray(new Predicate[predicates.size()]));
                }
                applyProcessors(query, builder, root);
                return entityViewManager.applySetting(EntityViewSetting.create(resultClass), query.createCriteriaBuilder(entityManager)).getQuery();
            } catch (RuntimeException e) {
                LOG.log(Level.SEVERE, "Exception while creating JPA query", e);
                throw e;
            }
        } else {
            return super.createQuery();
        }
    }

    private void applyProcessors(CriteriaQuery<?> query, CriteriaBuilder builder, From<C, C> from) {
        try {
            Method applyProcessorsMethod = org.apache.deltaspike.data.impl.criteria.QueryCriteria.class.getDeclaredMethod("applyProcessors", CriteriaQuery.class, CriteriaBuilder.class, From.class);
            applyProcessorsMethod.setAccessible(true);
            applyProcessorsMethod.invoke(this, query, builder, from);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isDistinct() {
        try {
            Field distinctField = org.apache.deltaspike.data.impl.criteria.QueryCriteria.class.getDeclaredField("distinct");
            distinctField.setAccessible(true);
            return distinctField.getBoolean(this);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void setDistinct(boolean distinct) {
        try {
            Field distinctField = org.apache.deltaspike.data.impl.criteria.QueryCriteria.class.getDeclaredField("distinct");
            distinctField.setAccessible(true);
            distinctField.set(this, distinct);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private List<PredicateBuilder<C>> getBuilders() {
        try {
            Field distinctField = org.apache.deltaspike.data.impl.criteria.QueryCriteria.class.getDeclaredField("builders");
            distinctField.setAccessible(true);
            return (List<PredicateBuilder<C>>) distinctField.get(this);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void addBuilders(List<PredicateBuilder<C>> builders) {
        getBuilders().addAll(builders);
    }

    private List<QueryProcessor<C>> getProcessors() {
        try {
            Field distinctField = org.apache.deltaspike.data.impl.criteria.QueryCriteria.class.getDeclaredField("processors");
            distinctField.setAccessible(true);
            return (List<QueryProcessor<C>>) distinctField.get(this);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void addProcessors(List<QueryProcessor<C>> processors) {
        getProcessors().addAll(processors);
    }

    @Override
    public <N> Criteria<C, N> select(Class<N> resultClass, QuerySelection<? super C, ?>... selection) {
        if (entityViewManager.getMetamodel().managedView(resultClass) == null) {
            return super.select(resultClass, selection);
        } else {
            if (selection.length == 0) {
                QueryCriteria<C, N> result = new QueryCriteria<>(entityClass, resultClass, entityManager, criteriaBuilderFactory, entityViewManager);
                result.addBuilders(getBuilders());
                result.setDistinct(isDistinct());
                result.addProcessors(getProcessors());
                return result;
            } else {
                throw selectionNotSupported();
            }
        }
    }

    @Override
    public Criteria<C, Object[]> select(QuerySelection<? super C, ?>... selection) {
        if (entityViewCriteria) {
            throw selectionNotSupported();
        } else {
            return super.select(selection);
        }
    }

    private RuntimeException selectionNotSupported() {
        return new UnsupportedOperationException("Selection not supported for entity view repositories");
    }
}