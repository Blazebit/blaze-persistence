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
import com.blazebit.persistence.criteria.impl.BlazeCriteria;
import com.blazebit.persistence.spring.data.base.query.KeysetAwarePageImpl;
import com.blazebit.persistence.spring.data.repository.EntityViewRepository;
import com.blazebit.persistence.spring.data.repository.EntityViewSpecificationExecutor;
import com.blazebit.persistence.spring.data.repository.KeysetPageable;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import javax.persistence.metamodel.EntityType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Moritz Becker
 * @author Christian Beikov
 * @since 1.2.0
 */
@Transactional(readOnly = true)
public class EntityViewAwareRepositoryImpl<V, E, ID extends Serializable> implements EntityViewRepository<V, ID>, EntityViewSpecificationExecutor<V, E> {

    private static final String ID_MUST_NOT_BE_NULL = "The given id must not be null!";
    private static final String[] EMPTY = new String[0];

    private final JpaEntityInformation<E, ?> entityInformation;
    private final EntityManager entityManager;
    private final CriteriaBuilderFactory cbf;
    private final EntityViewManager evm;
    private final Class<V> entityViewClass;

    private EntityViewAwareCrudMethodMetadata metadata;

    public EntityViewAwareRepositoryImpl(JpaEntityInformation<E, ?> entityInformation, EntityManager entityManager, CriteriaBuilderFactory cbf, EntityViewManager evm, Class<V> entityViewClass) {
        this.entityInformation = entityInformation;
        this.entityManager = entityManager;
        this.cbf = cbf;
        this.evm = evm;
        this.entityViewClass = entityViewClass;
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

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.CrudRepository#findOne(java.io.Serializable)
     */
    public V findOne(ID id) {
        Assert.notNull(id, ID_MUST_NOT_BE_NULL);

        CriteriaBuilder<?> cb = cbf.create(entityManager, getDomainClass())
                .where(getIdAttribute()).eq(id);
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

        return findOneQuery.getSingleResult();
    }

    @Override
    public long count() {
        TypedQuery<Long> countQuery = getCountQuery(null);
        return countQuery.getSingleResult();
    }

    @Override
    public boolean exists(ID id) {
        Assert.notNull(id, ID_MUST_NOT_BE_NULL);

        TypedQuery<Long> existsQuery = cbf.create(entityManager, Long.class)
                .from(getDomainClass())
                .select("COUNT(*)")
                .where(getIdAttribute()).eq(id)
                .getQuery();

        applyRepositoryMethodMetadata(existsQuery, true);

        return existsQuery.getSingleResult() > 0;
    }

    @Override
    public Iterable<V> findAll() {
        return getQuery(null, null, null, false).getResultList();
    }

    @Override
    public Iterable<V> findAll(Iterable<ID> idIterable) {
        Assert.notNull(idIterable, ID_MUST_NOT_BE_NULL);

        List<ID> idList = new ArrayList<>();
        for (ID id : idIterable) {
            idList.add(id);
        }
        CriteriaBuilder<?> cb = cbf.create(entityManager, getDomainClass())
                .where(getIdAttribute()).in(idList);

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
        EntityType<?> entityType = entityManager.getMetamodel().entity(entityClass);
        return entityType.getDeclaredId(entityType.getIdType().getJavaType()).getName();
    }

    private String getIdAttribute() {
        return getIdAttribute(getDomainClass());
    }

    @Override
    public V findOne(Specification<E> spec) {
        try {
            return getQuery(spec, (Sort) null).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<V> findAll(Specification<E> spec) {
        return getQuery(spec, (Sort) null).getResultList();
    }

    @Override
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

    @Override
    public List<V> findAll(Specification<E> spec, Sort sort) {
        return getQuery(spec, sort).getResultList();
    }

    @Override
    public long count(Specification<E> spec) {
        return executeCountQuery(getCountQuery(spec));
    }

    protected TypedQuery<V> getQuery(Specification<E> spec, Pageable pageable) {
        Sort sort = pageable == null ? null : pageable.getSort();
        return this.getQuery(spec, pageable, sort, false);
    }

    protected TypedQuery<V> getQuery(Specification<E> spec, Sort sort) {
        return this.getQuery(spec, null, sort, false);
    }

    protected TypedQuery<V> getQuery(Specification<E> spec, Pageable pageable, Sort sort, boolean keysetExtraction) {
        Class<E> domainClass = getDomainClass();
        BlazeCriteriaQuery<E> cq = BlazeCriteria.get(entityManager, cbf, domainClass);
        Root<E> root = this.applySpecificationToCriteria(spec, domainClass, cq);

        if (sort != null) {
            cq.orderBy(QueryUtils.toOrders(sort, root, BlazeCriteria.get(entityManager, cbf)));
        }
        CriteriaBuilder<E> cb = cq.createCriteriaBuilder();

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
                PaginatedCriteriaBuilder<E> paginatedCriteriaBuilder;
                if (pageable instanceof KeysetPageable) {
                    paginatedCriteriaBuilder = cb.page(((KeysetPageable) pageable).getKeysetPage(), pageable.getPageNumber() * pageable.getPageSize(), pageable.getPageSize());
                } else {
                    paginatedCriteriaBuilder = cb.page(pageable.getPageNumber() * pageable.getPageSize(), pageable.getPageSize());
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
                EntityViewSetting<V, PaginatedCriteriaBuilder<V>> setting = EntityViewSetting.create(entityViewClass, pageable.getPageNumber() * pageable.getPageSize(), pageable.getPageSize());
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

    protected TypedQuery<Long> getCountQuery(Specification<E> spec) {
        BlazeCriteriaBuilder builder = BlazeCriteria.get(entityManager, cbf);
        BlazeCriteriaQuery<Long> query = builder.createQuery(Long.class);

        Root<E> root = applySpecificationToCriteria(spec, getDomainClass(), query);

        if (query.isDistinct()) {
            query.select(builder.countDistinct(root));
        } else {
            query.select(builder.count(root));
        }

        // Remove all Orders the Specifications might have applied
        query.orderBy(Collections.<Order> emptyList());

        return this.applyRepositoryMethodMetadata(query.getQuery(), true);
    }

    private Root<E> applySpecificationToCriteria(Specification<E> spec, Class<E> domainClass, CriteriaQuery<?> query) {
        Assert.notNull(domainClass, "Domain class must not be null!");
        Assert.notNull(query, "CriteriaQuery must not be null!");
        Root<E> root = query.from(domainClass);
        if (spec == null) {
            return root;
        } else {
            javax.persistence.criteria.CriteriaBuilder builder = BlazeCriteria.get(entityManager, cbf);
            Predicate predicate = spec.toPredicate(root, query, builder);
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
