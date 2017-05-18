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

package com.blazebit.persistence.deltaspike.data.base.handler;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.PagedList;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.criteria.BlazeCriteriaBuilder;
import com.blazebit.persistence.criteria.BlazeCriteriaQuery;
import com.blazebit.persistence.criteria.BlazeCriteria;
import com.blazebit.persistence.deltaspike.data.EntityViewRepository;
import com.blazebit.persistence.deltaspike.data.KeysetPageable;
import com.blazebit.persistence.deltaspike.data.Page;
import com.blazebit.persistence.deltaspike.data.Pageable;
import com.blazebit.persistence.deltaspike.data.Sort;
import com.blazebit.persistence.deltaspike.data.Specification;
import com.blazebit.persistence.deltaspike.data.base.builder.QueryBuilderUtils;
import com.blazebit.persistence.view.EntityViewSetting;
import org.apache.deltaspike.data.impl.builder.QueryBuilder;
import org.apache.deltaspike.data.impl.meta.RequiresTransaction;
import org.apache.deltaspike.data.impl.property.Property;
import org.apache.deltaspike.data.impl.property.query.NamedPropertyCriteria;
import org.apache.deltaspike.data.impl.property.query.PropertyQueries;
import org.apache.deltaspike.data.impl.util.jpa.PersistenceUnitUtilDelegateFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.apache.deltaspike.core.util.ArraysUtils.isEmpty;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public abstract class AbstractEntityViewAwareRepositoryHandler<E, V, PK extends Serializable> implements EntityViewRepository<E, V, PK>, EntityViewDelegateQueryHandler {

    protected abstract String[] getFetches();

    protected abstract void applyQueryHints(Query q, boolean applyFetches);

    protected abstract boolean isNew(E entity);

    @RequiresTransaction
    public E save(E entity) {
        if (isNew(entity)) {
            entityManager().persist(entity);
            return entity;
        }
        return entityManager().merge(entity);
    }

    @RequiresTransaction
    public E saveAndFlush(E entity) {
        E result = save(entity);
        flush();
        return result;
    }

    @RequiresTransaction
    public E saveAndFlushAndRefresh(E entity) {
        E result = saveAndFlush(entity);
        entityManager().refresh(result);
        return result;
    }

    @RequiresTransaction
    public void refresh(E entity) {
        entityManager().refresh(entity);
    }

    @SuppressWarnings("unchecked")
    public PK getPrimaryKey(E entity) {
        return (PK) PersistenceUnitUtilDelegateFactory.get(entityManager()).getIdentifier(entity);
    }

    @RequiresTransaction
    public void remove(E entity) {
        entityManager().remove(entity);
    }

    @RequiresTransaction
    public void removeAndFlush(E entity) {
        entityManager().remove(entity);
        flush();
    }

    @RequiresTransaction
    public void attachAndRemove(E entity) {
        if (!entityManager().contains(entity)) {
            entity = entityManager().merge(entity);
        }
        remove(entity);
    }

    @RequiresTransaction
    public void flush() {
        entityManager().flush();
    }

    public Long count() {
        Query query = entityManager().createQuery(QueryBuilder.countQuery(entityName()));
        applyQueryHints(query, false);
        return (Long) query.getSingleResult();
    }

    public Long count(E example, SingularAttribute<E, ?>... attributes) {
        return executeCountQuery(example, false, attributes);
    }

    public Long countLike(E example, SingularAttribute<E, ?>... attributes) {
        return executeCountQuery(example, true, attributes);
    }

    private Long executeCountQuery(E example, boolean useLikeOperator, SingularAttribute<E, ?>... attributes) {
        if (isEmpty(attributes)) {
            return count();
        }
        TypedQuery<Long> query = (TypedQuery<Long>) createExampleQuery(example, 0, 0, useLikeOperator, "", attributes);
        applyQueryHints(query, false);
        return query.getSingleResult();
    }

    @Override
    public V findBy(PK pk) {
        CriteriaBuilder<E> cb = createCriteriaBuilder().where(idAttribute()).eq(pk);
        String[] fetches = getFetches();
        if (fetches.length != 0) {
            cb.fetch(fetches);
        }

        TypedQuery<V> query;
        if (viewClass() == null) {
            query = (TypedQuery<V>) cb.getQuery();
        } else {
            query = applySetting(
                    createSetting(),
                    cb
            ).getQuery();
        }

        applyQueryHints(query, fetches.length == 0);
        List<V> result = query.getResultList();
        return result.isEmpty() ? null : result.get(0);
    }

    @Override
    public List<V> findAll() {
        CriteriaBuilder<E> cb = createCriteriaBuilder();
        String[] fetches = getFetches();
        if (fetches.length != 0) {
            cb.fetch(fetches);
        }

        TypedQuery<V> query;
        if (viewClass() == null) {
            query = (TypedQuery<V>) cb.getQuery();
        } else {
            query = applySetting(
                    createSetting(),
                    cb
            ).getQuery();
        }

        applyQueryHints(query, fetches.length == 0);
        return query.getResultList();
    }

    @Override
    public List<V> findAll(int start, int max) {
        CriteriaBuilder<E> cb = createCriteriaBuilder().orderByAsc(idAttribute());
        String[] fetches = getFetches();
        if (fetches.length != 0) {
            cb.fetch(fetches);
        }

        TypedQuery<V> query;
        if (viewClass() == null) {
            query = (TypedQuery<V>) cb.getQuery();
        } else {
            EntityViewSetting<V, PaginatedCriteriaBuilder<V>> setting = EntityViewSetting.create(viewClass(), start, max);
            query = applySetting(setting, cb)
                    .getQuery();
        }

        applyQueryHints(query, fetches.length == 0);
        return query.getResultList();
    }

    @Override
    public Iterable<V> findAll(Sort sort) {
        CriteriaBuilder<E> cb = createCriteriaBuilder();
        String[] fetches = getFetches();
        if (fetches.length != 0) {
            cb.fetch(fetches);
        }

        TypedQuery<V> query;
        if (viewClass() == null) {
            QueryBuilderUtils.applySort(sort, cb);
            query = (TypedQuery<V>) cb.getQuery();
        } else {
            EntityViewSetting<V, CriteriaBuilder<V>> setting = EntityViewSetting.create(viewClass());
            QueryBuilderUtils.applySort(sort, setting);
            query = applySetting(setting, cb)
                    .getQuery();
        }

        applyQueryHints(query, fetches.length == 0);
        return query.getResultList();
    }

    @Override
    public Page<V> findAll(Pageable pageable) {
        return findAll(null, pageable);
    }

    @Override
    public Page<V> findAll(Specification<E> specification, Pageable pageable) {
        CriteriaBuilder<E> cb;
        if (specification == null) {
            cb = createCriteriaBuilder();
        } else {
            BlazeCriteriaBuilder blazeCriteriaBuilder = BlazeCriteria.get(criteriaBuilderFactory());
            BlazeCriteriaQuery<?> query = blazeCriteriaBuilder.createQuery(entityClass());
            Root queryRoot = query.from(entityClass());
            Predicate predicate = specification.toPredicate(queryRoot, query, blazeCriteriaBuilder);
            if (predicate != null) {
                if (query.getRestriction() == null) {
                    query.where(predicate);
                } else {
                    query.where(query.getRestriction(), predicate);
                }
            }
            cb = (CriteriaBuilder<E>) query.createCriteriaBuilder(entityManager());
        }

        String[] fetches = getFetches();
        if (fetches.length != 0) {
            cb.fetch(fetches);
        }

        TypedQuery<V> query;
        if (viewClass() == null) {
            PaginatedCriteriaBuilder<V> pcb;
            if (pageable instanceof KeysetPageable) {
                pcb = (PaginatedCriteriaBuilder<V>) cb.page(((KeysetPageable) pageable).getKeysetPage(), pageable.getOffset(), pageable.getPageSize());
            } else {
                pcb = (PaginatedCriteriaBuilder<V>) cb.page(pageable.getOffset(), pageable.getPageSize());
            }
            QueryBuilderUtils.applySort(pageable.getSort(), pcb);
            query = pcb.getQuery();
        } else {
            EntityViewSetting<V, PaginatedCriteriaBuilder<V>> setting = EntityViewSetting.create(viewClass(), pageable.getOffset(), pageable.getPageSize());
            if (pageable instanceof KeysetPageable) {
                setting.withKeysetPage(((KeysetPageable) pageable).getKeysetPage());
            }
            QueryBuilderUtils.applySort(pageable.getSort(), setting);
            query = applySetting(setting, cb).getQuery();
        }

        applyQueryHints(query, fetches.length == 0);
        PagedList<V> resultList = (PagedList<V>) query.getResultList();
        return new KeysetAwarePageImpl<>(resultList, pageable);
    }

    @Override
    public List<V> findBy(E e, SingularAttribute<E, ?>... singularAttributes) {
        return executeExampleQuery(e, 0, 0, false, null, singularAttributes);
    }

    @Override
    public List<V> findBy(E e, int start, int max, SingularAttribute<E, ?>... singularAttributes) {
        return executeExampleQuery(e, start, max, false, null, singularAttributes);
    }

    @Override
    public List<V> findByLike(E e, SingularAttribute<E, ?>... singularAttributes) {
        return executeExampleQuery(e, 0, 0, true, null, singularAttributes);
    }

    @Override
    public List<V> findByLike(E e, int start, int max, SingularAttribute<E, ?>... singularAttributes) {
        return executeExampleQuery(e, start, max, true, null, singularAttributes);
    }

    private EntityViewSetting<V, CriteriaBuilder<V>> createSetting() {
        return EntityViewSetting.create((Class<V>) viewClass());
    }

    private List<String> extractPropertyNames(SingularAttribute<E, ?>... attributes) {
        List<String> result = new ArrayList<String>(attributes.length);
        for (SingularAttribute<E, ?> attribute : attributes) {
            result.add(attribute.getName());
        }
        return result;
    }

    private List<Property<Object>> extractProperties(SingularAttribute<E, ?>... attributes) {
        List<String> names = extractPropertyNames(attributes);
        List<Property<Object>> properties = PropertyQueries.createQuery(entityClass())
                .addCriteria(new NamedPropertyCriteria(names.toArray(new String[]{}))).getResultList();
        return properties;
    }

    private void prepareWhere(CriteriaBuilder<?> cb, E example, List<Property<Object>> properties, boolean useLikeOperator) {
        Iterator<Property<Object>> iterator = properties.iterator();
        while (iterator.hasNext()) {
            Property<Object> property = iterator.next();
            String name = property.getName();
            if (useLikeOperator && property.getJavaClass().getName().equals(String.class.getName())) {
                cb.where(name).like(false).value(property.getValue(example)).noEscape();
            } else {
                cb.where(name).eq(property.getValue(example));
            }
        }
    }

    private List<V> executeExampleQuery(E example, int start, int max, boolean useLikeOperator,
                                        String selectExpression, SingularAttribute<E, ?>... attributes) {
        if (isEmpty(attributes)) {
            return findAll();
        }

        return (List<V>) createExampleQuery(example, start, max, useLikeOperator, selectExpression, attributes).getResultList();
    }

    private TypedQuery<V> createExampleQuery(E example, int start, int max, boolean useLikeOperator,
        String selectExpression, SingularAttribute<E, ?>... attributes) {
        CriteriaBuilder<?> cb = createCriteriaBuilder();
        if (selectExpression != null) {
            cb.select(selectExpression);
        }
        List<Property<Object>> properties = extractProperties(attributes);
        prepareWhere(cb, example, properties, useLikeOperator);
        cb.orderByAsc(idAttribute());

        String[] fetches = getFetches();
        if (fetches.length != 0) {
            cb.fetch(fetches);
        }

        TypedQuery<V> query;
        if (viewClass() == null) {
            if (start > 0 || max > 0) {
                query = (TypedQuery<V>) cb.page(start, max).getQuery();
            } else {
                query = (TypedQuery<V>) cb.getQuery();
            }
        } else {
            EntityViewSetting<V, ?> setting;
            if (start > 0 || max > 0) {
                setting = EntityViewSetting.create(viewClass(), start, max);
            } else {
                setting = EntityViewSetting.create(viewClass());
            }

            query = applySetting(setting, cb)
                    .getQuery();
        }

        applyQueryHints(query, fetches.length == 0);
        return query;
    }

    protected abstract <T, Q extends FullQueryBuilder<T, Q>> Q applySetting(EntityViewSetting<T, Q> setting, CriteriaBuilder<?> criteriaBuilder);

    protected abstract String idAttribute();

    protected CriteriaBuilder<E> createCriteriaBuilder() {
        return criteriaBuilderFactory().create(entityManager(), entityClass());
    }

    protected abstract EntityManager entityManager();

    protected abstract CriteriaBuilderFactory criteriaBuilderFactory();

    protected abstract Class<V> viewClass();

    protected abstract Class<E> entityClass();

    protected abstract String entityName();
}