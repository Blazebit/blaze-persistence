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

package com.blazebit.persistence.spring.data.impl.repository;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.PagedList;
import com.blazebit.persistence.criteria.BlazeCriteriaBuilder;
import com.blazebit.persistence.criteria.BlazeCriteriaQuery;
import com.blazebit.persistence.criteria.impl.BlazeCriteria;
import com.blazebit.persistence.spring.data.api.repository.EntityViewRepository;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.data.jpa.repository.support.CrudMethodMetadata;
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
 * @author Moritz Becker (moritz.becker@gmx.at)
 * @since 1.2
 */
@Transactional(readOnly = true)
public class EntityViewRepositoryImpl<V, E, ID extends Serializable> implements EntityViewRepository<V, ID>, EntityViewSpecificationExecutor<V, E> {

    private static final String ID_MUST_NOT_BE_NULL = "The given id must not be null!";

    private final JpaEntityInformation<E, ?> entityInformation;
    private final EntityManager entityManager;
    private final CriteriaBuilderFactory cbf;
    private final EntityViewManager evm;
    private final EntityViewSetting<V, CriteriaBuilder<V>> setting;
    private final Class<V> entityViewClass;

    private CrudMethodMetadata metadata;

    public EntityViewRepositoryImpl(JpaEntityInformation<E, ?> entityInformation, EntityManager entityManager, CriteriaBuilderFactory cbf, EntityViewManager evm, Class<V> entityViewClass) {
        this.entityInformation = entityInformation;
        this.entityManager = entityManager;
        this.cbf = cbf;
        this.evm = evm;
        this.setting = EntityViewSetting.create(entityViewClass);
        this.entityViewClass = entityViewClass;
    }

    public void setRepositoryMethodMetadata(CrudMethodMetadata crudMethodMetadata) {
        this.metadata = crudMethodMetadata;
    }

    protected CrudMethodMetadata getRepositoryMethodMetadata() {
        return metadata;
    }

    protected Class<E> getDomainClass() {
        return entityInformation.getJavaType();
    }

    protected Map<String, Object> getQueryHints() {
        if (metadata == null) {
            return Collections.emptyMap();
        }

        if (metadata.getEntityGraph() == null) {
            return metadata.getQueryHints();
        }

        Map<String, Object> hints = new HashMap<String, Object>();
        hints.putAll(metadata.getQueryHints());

        return hints;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.CrudRepository#findOne(java.io.Serializable)
     */
    public V findOne(ID id) {
        Assert.notNull(id, ID_MUST_NOT_BE_NULL);

        CriteriaBuilder<?> cb = cbf.create(entityManager, getDomainClass())
                .where(getIdAttribute()).eq(id);
        TypedQuery<V> findOneQuery = evm.applySetting(setting, cb).getQuery();

        applyQueryHints(findOneQuery);

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

        applyRepositoryMethodMetadata(existsQuery);

        return existsQuery.getSingleResult() > 0;
    }

    @Override
    public Iterable<V> findAll() {
        return getQuery(null, null, null).getResultList();
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
        TypedQuery<V> findAllByIdsQuery = evm.applySetting(setting, cb).getQuery();

        applyRepositoryMethodMetadata(findAllByIdsQuery);

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
        PagedList<V> content = (PagedList<V>) query.getResultList();
        return new PageImpl<V>(content, pageable, content.getTotalSize());
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
        return this.getQuery(spec, pageable, sort);
    }

    protected TypedQuery<V> getQuery(Specification<E> spec, Sort sort) {
        return this.getQuery(spec, null, sort);
    }

    protected TypedQuery<V> getQuery(Specification<E> spec, Pageable pageable, Sort sort) {
        Class<E> domainClass = getDomainClass();
        BlazeCriteriaQuery<E> cq = BlazeCriteria.get(entityManager, cbf, domainClass);
        Root<E> root = this.applySpecificationToCriteria(spec, domainClass, cq);

        if (sort != null) {
            cq.orderBy(QueryUtils.toOrders(sort, root, BlazeCriteria.get(entityManager, cbf)));
        }
        EntityViewSetting<V, ?> setting;
        if (pageable == null) {
            setting = this.setting;
        } else {
            setting = EntityViewSetting.create(entityViewClass, pageable.getOffset(), pageable.getPageSize());
        }
        TypedQuery<V> query = evm.applySetting(setting, cq.createCriteriaBuilder()).getQuery();

        return this.applyRepositoryMethodMetadata(query);
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

        return this.applyRepositoryMethodMetadata(query.getQuery());
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

    private <S> TypedQuery<S> applyRepositoryMethodMetadata(TypedQuery<S> query) {
        if (this.metadata == null) {
            return query;
        } else {
            LockModeType type = this.metadata.getLockModeType();
            TypedQuery<S> toReturn = type == null ? query : query.setLockMode(type);
            this.applyQueryHints(toReturn);
            return toReturn;
        }
    }

    private void applyQueryHints(Query query) {
        for (Map.Entry<String, Object> hint : getQueryHints().entrySet()) {
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
