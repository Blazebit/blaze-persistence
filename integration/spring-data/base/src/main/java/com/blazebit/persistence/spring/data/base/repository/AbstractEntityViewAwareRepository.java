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

package com.blazebit.persistence.spring.data.base.repository;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.PagedList;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.criteria.BlazeCriteriaBuilder;
import com.blazebit.persistence.criteria.BlazeCriteriaQuery;
import com.blazebit.persistence.criteria.BlazeCriteria;
import com.blazebit.persistence.parser.EntityMetamodel;
import com.blazebit.persistence.spi.ExtendedManagedType;
import com.blazebit.persistence.spring.data.base.query.KeysetAwarePageImpl;
import com.blazebit.persistence.spring.data.repository.KeysetPageable;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.spi.type.EntityViewProxy;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.convert.QueryByExamplePredicateBuilder;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.query.Jpa21Utils;
import org.springframework.data.jpa.repository.query.JpaEntityGraph;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.data.jpa.repository.query.QueryUtils.applyAndBind;
import static org.springframework.data.jpa.repository.query.QueryUtils.getQueryString;

/**
 * @author Moritz Becker
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractEntityViewAwareRepository<V, E, ID extends Serializable> {

    private static final String ID_MUST_NOT_BE_NULL = "The given id must not be null!";
    private static final String DELETE_ALL_QUERY_STRING = "delete from %s x";
    private static final String[] EMPTY = new String[0];

    private final JpaEntityInformation<E, ?> entityInformation;
    private final EntityManager entityManager;
    private final CriteriaBuilderFactory cbf;
    private final EntityViewManager evm;
    private final Class<V> entityViewClass;
    private final String idAttributeName;

    private EntityViewAwareCrudMethodMetadata metadata;

    public AbstractEntityViewAwareRepository(JpaEntityInformation<E, ?> entityInformation, EntityManager entityManager, CriteriaBuilderFactory cbf, EntityViewManager evm, Class<V> entityViewClass) {
        this.entityInformation = entityInformation;
        this.entityManager = entityManager;
        this.cbf = cbf;
        this.evm = evm;
        this.entityViewClass = entityViewClass;
        this.idAttributeName = getIdAttribute(getDomainClass());
    }

    public void setRepositoryMethodMetadata(EntityViewAwareCrudMethodMetadata crudMethodMetadata) {
        this.metadata = crudMethodMetadata;
    }

    protected EntityViewAwareCrudMethodMetadata getRepositoryMethodMetadata() {
        return metadata;
    }

    protected Class<E> getDomainClass() {
        return entityInformation.getJavaType();
    }

    protected Map<String, Object> getQueryHints(boolean applyFetchGraph) {
        if (metadata == null) {
            return Collections.emptyMap();
        }

        if (metadata.getEntityGraph() == null || !applyFetchGraph) {
            return metadata.getQueryHints();
        }

        Map<String, Object> hints = new HashMap<String, Object>();
        hints.putAll(metadata.getQueryHints());

        hints.putAll(Jpa21Utils.tryGetFetchGraphHints(entityManager, getEntityGraph(), getDomainClass()));

        return hints;
    }

    private JpaEntityGraph getEntityGraph() {
        String fallbackName = this.entityInformation.getEntityName() + "." + metadata.getMethod().getName();
        return new JpaEntityGraph(metadata.getEntityGraph(), fallbackName);
    }

    @Transactional
    public <S extends E> S save(S entity) {
        if (entity instanceof EntityViewProxy) {
            evm.update(entityManager, entity);
            return entity;
        } else if (entityInformation.isNew(entity)) {
            entityManager.persist(entity);
            return entity;
        } else {
            return entityManager.merge(entity);
        }
    }

    @Transactional
    public <S extends E> List<S> saveAll(Iterable<S> entities) {
        return save(entities);
    }

    @Transactional
    public <S extends E> List<S> save(Iterable<S> entities) {
        List<S> result = new ArrayList<S>();

        if (entities == null) {
            return result;
        }

        for (S entity : entities) {
            result.add(save(entity));
        }

        return result;
    }

    @Transactional
    public void flush() {
        entityManager.flush();
    }

    @Transactional
    public <S extends E> S saveAndFlush(S entity) {
        S result = save(entity);
        flush();

        return result;
    }

    @Transactional
    public void deleteById(ID id) {
        delete(id);
    }

    @Transactional
    public void delete(ID id) {
        Assert.notNull(id, ID_MUST_NOT_BE_NULL);

        E entity = (E) findOne(id);

        if (entity == null) {
            throw new EmptyResultDataAccessException(
                    String.format("No %s entity with id %s exists!", entityInformation.getJavaType(), id), 1);
        }

        delete(entity);
    }

    @Transactional
    public void delete(E entity) {
        Assert.notNull(entity, "The entity must not be null!");
        if (entity instanceof EntityViewProxy) {
            evm.remove(entityManager, entity);
        } else {
            entityManager.remove(entityManager.contains(entity) ? entity : entityManager.merge(entity));
        }
    }

    @Transactional
    public void delete(Iterable<? extends E> entities) {
        Assert.notNull(entities, "The given Iterable of entities not be null!");

        for (E entity : entities) {
            delete(entity);
        }
    }

    @Transactional
    public void deleteAll() {
        for (E element : (Iterable<E>) findAll()) {
            delete(element);
        }
    }

    @Transactional
    public void deleteAll(Iterable<? extends E> entities) {
        delete(entities);
    }

    @Transactional
    public void deleteInBatch(Iterable<E> entities) {
        Assert.notNull(entities, "The given Iterable of entities not be null!");

        if (!entities.iterator().hasNext()) {
            return;
        }

        applyAndBind(getQueryString(DELETE_ALL_QUERY_STRING, entityInformation.getEntityName()), entities, entityManager)
                .executeUpdate();
    }

    public void deleteAllInBatch() {
        entityManager.createQuery(getQueryString(DELETE_ALL_QUERY_STRING, entityInformation.getEntityName())).executeUpdate();
    }

    public E getOne(ID id) {
        return (E) findOne(id);
    }

    public <S extends E> long count(Example<S> example) {
        return executeCountQuery(getCountQuery(new ExampleSpecification<>(example), example.getProbeType()));
    }

    public <S extends E> boolean exists(Example<S> example) {
        return !getQuery(new ExampleSpecification<>(example), example.getProbeType(), (Sort) null).getResultList()
                .isEmpty();
    }

    public <S extends E> List<S> findAll(Example<S> example) {
        return getQuery(new ExampleSpecification<>(example), example.getProbeType(), (Sort) null).getResultList();
    }

    public <S extends E> List<S> findAll(Example<S> example, Sort sort) {
        return getQuery(new ExampleSpecification<>(example), example.getProbeType(), sort).getResultList();
    }

    public <S extends E> Page<S> findAll(Example<S> example, Pageable pageable) {
        ExampleSpecification<S> spec = new ExampleSpecification<>(example);
        Class<S> probeType = example.getProbeType();
        TypedQuery<S> query = getQuery(new ExampleSpecification<>(example), probeType, pageable);

        return pageable == null ? new KeysetAwarePageImpl<>(query.getResultList()) : new KeysetAwarePageImpl<>((PagedList<S>) query.getResultList(), pageable);
    }

    public List<E> findAll(Sort sort) {
        return getQuery(null, sort).getResultList();
    }

    public Page<E> findAll(Pageable pageable) {
        if (null == pageable) {
            return (Page<E>) new PageImpl<>(findAll());
        }

        return (Page<E>) findAll((Specification<E>) null, pageable);
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    protected static class ExampleSpecification<T> implements Specification<T> {

        private final Example<T> example;

        public ExampleSpecification(Example<T> example) {
            Assert.notNull(example, "Example must not be null!");
            this.example = example;
        }

        @Override
        public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, javax.persistence.criteria.CriteriaBuilder cb) {
            return QueryByExamplePredicateBuilder.getPredicate(root, cb, example);
        }
    }

    public V findOne(ID id) {
        Assert.notNull(id, ID_MUST_NOT_BE_NULL);

        CriteriaBuilder<?> cb = cbf.create(entityManager, getDomainClass())
                .where(idAttributeName).eq(id);
        String[] fetches = EMPTY;
        if (metadata != null && metadata.getEntityGraph() != null && (fetches = metadata.getEntityGraph().attributePaths()).length != 0) {
            cb.fetch(fetches);
        }
        TypedQuery<V> findOneQuery;
        Class<V> entityViewClass = metadata == null || metadata.getEntityViewClass() == null ? this.entityViewClass : (Class<V>) metadata.getEntityViewClass();
        if (entityViewClass == null) {
            findOneQuery = (TypedQuery<V>) cb.getQuery();
        } else {
            findOneQuery = evm.applySetting(EntityViewSetting.create(entityViewClass), cb).getQuery();
        }

        applyQueryHints(findOneQuery, fetches.length == 0);

        try {
            return findOneQuery.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public long count() {
        TypedQuery<Long> countQuery = getCountQuery(null, getDomainClass());
        return countQuery.getSingleResult();
    }

    public boolean existsById(ID id) {
        return exists(id);
    }

    public boolean exists(ID id) {
        Assert.notNull(id, ID_MUST_NOT_BE_NULL);

        TypedQuery<Object> existsQuery = cbf.create(entityManager, Object.class)
                .from(getDomainClass())
                // Empty string because SQLServer can't interpret a number properly when using TOP clause
                .select("''")
                .where(idAttributeName).eq(id)
                .setMaxResults(1)
                .getQuery();

        applyRepositoryMethodMetadata(existsQuery, true);

        try {
            return !existsQuery.getResultList().isEmpty();
        } catch (NoResultException e) {
            return false;
        }
    }

    public List<V> findAll() {
        return getQuery(null, getDomainClass(), null, null, false).getResultList();
    }

    public List<V> findAllById(Iterable<ID> idIterable) {
        return findAll(idIterable);
    }

    public List<V> findAll(Iterable<ID> idIterable) {
        Assert.notNull(idIterable, ID_MUST_NOT_BE_NULL);

        List<ID> idList = new ArrayList<>();
        for (ID id : idIterable) {
            idList.add(id);
        }
        CriteriaBuilder<?> cb = cbf.create(entityManager, getDomainClass())
                .where(idAttributeName).in(idList);

        String[] fetches = EMPTY;
        if (metadata != null && metadata.getEntityGraph() != null && (fetches = metadata.getEntityGraph().attributePaths()).length != 0) {
            cb.fetch(fetches);
        }
        TypedQuery<V> findAllByIdsQuery;
        Class<V> entityViewClass = metadata == null || metadata.getEntityViewClass() == null ? this.entityViewClass : (Class<V>) metadata.getEntityViewClass();
        if (entityViewClass == null) {
            findAllByIdsQuery = (TypedQuery<V>) cb.getQuery();
        } else {
            findAllByIdsQuery = evm.applySetting(EntityViewSetting.create(entityViewClass), cb).getQuery();
        }

        applyRepositoryMethodMetadata(findAllByIdsQuery, fetches.length == 0);

        return findAllByIdsQuery.getResultList();
    }

    private String getIdAttribute(Class<?> entityClass) {
        return cbf.getService(EntityMetamodel.class)
                .getManagedType(ExtendedManagedType.class, entityClass)
                .getIdAttribute()
                .getName();
    }

    public List<V> findAll(Specification<E> spec) {
        return (List<V>) getQuery(spec, (Sort) null).getResultList();
    }

    public Page<V> findAll(Specification<E> spec, Pageable pageable) {
        TypedQuery<V> query = getQuery(spec, pageable);
        if (pageable == null) {
            return new KeysetAwarePageImpl<>(query.getResultList());
        }
        PagedList<V> resultList = (PagedList<V>) query.getResultList();
        Long total = resultList.getTotalSize();

        if (total.equals(0L)) {
            return new KeysetAwarePageImpl<>(Collections.<V>emptyList(), total, null, pageable);
        }

        return new KeysetAwarePageImpl<>(resultList, pageable);
    }

    public List<V> findAll(Specification<E> spec, Sort sort) {
        return (List<V>) getQuery(spec, sort).getResultList();
    }

    public long count(Specification<E> spec) {
        return executeCountQuery(getCountQuery(spec, getDomainClass()));
    }

    protected TypedQuery<V> getQuery(Specification<E> spec, Pageable pageable) {
        Sort sort = pageable == null ? null : pageable.getSort();
        return this.getQuery(spec, getDomainClass(), pageable, sort, false);
    }

    protected <S extends E> TypedQuery<S> getQuery(Specification<S> spec, Class<S> domainClass, Pageable pageable) {
        Sort sort = pageable == null ? null : pageable.getSort();
        return (TypedQuery<S>) this.getQuery(spec, domainClass, pageable, sort, false);
    }

    protected TypedQuery<E> getQuery(Specification<E> spec, Sort sort) {
        return (TypedQuery<E>) this.getQuery(spec, getDomainClass(), null, sort, false);
    }

    protected <S extends E> TypedQuery<S> getQuery(Specification<S> spec, Class<S> domainClass, Sort sort) {
        return (TypedQuery<S>) this.getQuery(spec, domainClass, null, sort, false);
    }

    protected <S extends E> TypedQuery<V> getQuery(Specification<S> spec, Class<S> domainClass, Pageable pageable, Sort sort, boolean keysetExtraction) {
        BlazeCriteriaQuery<S> cq = BlazeCriteria.get(cbf, domainClass);
        Root<S> root = this.applySpecificationToCriteria(spec, domainClass, cq);

        if (sort != null) {
            cq.orderBy(QueryUtils.toOrders(sort, root, BlazeCriteria.get(cbf)));
        }
        CriteriaBuilder<S> cb = cq.createCriteriaBuilder(entityManager);

        String[] fetches = EMPTY;
        if (metadata != null && metadata.getEntityGraph() != null && (fetches = metadata.getEntityGraph().attributePaths()).length != 0) {
            cb.fetch(fetches);
        }
        TypedQuery<V> query;
        Class<V> entityViewClass = metadata == null || metadata.getEntityViewClass() == null ? this.entityViewClass : (Class<V>) metadata.getEntityViewClass();
        if (entityViewClass == null) {
            if (pageable == null) {
                query = (TypedQuery<V>) cb.getQuery();
            } else {
                PaginatedCriteriaBuilder<S> paginatedCriteriaBuilder;
                if (pageable instanceof KeysetPageable) {
                    paginatedCriteriaBuilder = cb.page(((KeysetPageable) pageable).getKeysetPage(), getOffset(pageable), pageable.getPageSize());
                } else {
                    paginatedCriteriaBuilder = cb.page(getOffset(pageable), pageable.getPageSize());
                }
                if (keysetExtraction) {
                    paginatedCriteriaBuilder.withKeysetExtraction(true);
                }
                query = (TypedQuery<V>) paginatedCriteriaBuilder.getQuery();
            }
        } else {
            if (pageable == null) {
                EntityViewSetting<V, CriteriaBuilder<V>> setting = EntityViewSetting.create(entityViewClass);
                query = evm.applySetting(setting, cb).getQuery();
            } else {
                EntityViewSetting<V, PaginatedCriteriaBuilder<V>> setting = EntityViewSetting.create(entityViewClass, getOffset(pageable), pageable.getPageSize());
                if (pageable instanceof KeysetPageable) {
                    setting.withKeysetPage(((KeysetPageable) pageable).getKeysetPage());
                }
                PaginatedCriteriaBuilder<V> paginatedCriteriaBuilder = evm.applySetting(setting, cb);
                if (keysetExtraction) {
                    paginatedCriteriaBuilder.withKeysetExtraction(true);
                }
                query = paginatedCriteriaBuilder.getQuery();
            }
        }

        return this.applyRepositoryMethodMetadata(query, fetches.length == 0);
    }

    protected abstract int getOffset(Pageable pageable);

    protected <S extends E> TypedQuery<Long> getCountQuery(Specification<S> spec, Class<S> domainClass) {
        BlazeCriteriaBuilder builder = BlazeCriteria.get(cbf);
        BlazeCriteriaQuery<Long> query = builder.createQuery(Long.class);

        Root<S> root = applySpecificationToCriteria(spec, domainClass, query);

        if (query.isDistinct()) {
            query.select(builder.countDistinct(root));
        } else {
            query.select(builder.count(root));
        }

        // Remove all Orders the Specifications might have applied
        query.orderBy(Collections.<Order> emptyList());

        return this.applyRepositoryMethodMetadata(query.createCriteriaBuilder(entityManager).getQuery(), true);
    }

    private <S extends E> Root<S> applySpecificationToCriteria(Specification<S> spec, Class<S> domainClass, CriteriaQuery<?> query) {
        Assert.notNull(domainClass, "Domain class must not be null!");
        Assert.notNull(query, "CriteriaQuery must not be null!");
        Root<S> root = query.from(domainClass);
        if (spec == null) {
            return root;
        } else {
            Predicate predicate = spec.toPredicate(root, query, ((BlazeCriteriaQuery<?>) query).getCriteriaBuilder());
            if (predicate != null) {
                query.where(predicate);
            }

            return root;
        }
    }

    private <S> TypedQuery<S> applyRepositoryMethodMetadata(TypedQuery<S> query, boolean applyFetchGraph) {
        if (this.metadata == null) {
            return query;
        } else {
            LockModeType type = this.metadata.getLockModeType();
            TypedQuery<S> toReturn = type == null ? query : query.setLockMode(type);
            this.applyQueryHints(toReturn, applyFetchGraph);
            return toReturn;
        }
    }

    private void applyQueryHints(Query query, boolean applyFetchGraph) {
        for (Map.Entry<String, Object> hint : getQueryHints(applyFetchGraph).entrySet()) {
            query.setHint(hint.getKey(), hint.getValue());
        }
    }

    private static Long executeCountQuery(TypedQuery<Long> query) {

        Assert.notNull(query, "TypedQuery must not be null!");

        List<Long> totals = query.getResultList();
        Long total = 0L;

        for (Long element : totals) {
            total += element == null ? 0 : element;
        }

        return total;
    }
}
