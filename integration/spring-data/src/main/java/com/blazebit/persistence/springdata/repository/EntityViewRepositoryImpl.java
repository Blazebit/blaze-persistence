/*
 * Copyright 2014 - 2016 Blazebit.
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

package com.blazebit.persistence.springdata.repository;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import org.springframework.data.jpa.repository.support.CrudMethodMetadata;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.metamodel.EntityType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Moritz Becker (moritz.becker@gmx.at)
 * @since 1.2
 */
@Transactional(readOnly = true)
public class EntityViewRepositoryImpl<T, ID extends Serializable> implements EntityViewRepository<T, ID> {

    private static final String ID_MUST_NOT_BE_NULL = "The given id must not be null!";

    private final JpaEntityInformation<T, ?> entityInformation;
    private final EntityManager entityManager;
    private final CriteriaBuilderFactory cbf;
    private final EntityViewManager evm;
    private final Class<T> entityViewClass;
    private final EntityViewSetting<T, ?> setting;

    private CrudMethodMetadata metadata;

    public EntityViewRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager, CriteriaBuilderFactory cbf, EntityViewManager evm, Class<T> entityViewClass) {
        this.entityInformation = entityInformation;
        this.entityManager = entityManager;
        this.cbf = cbf;
        this.evm = evm;
        this.entityViewClass = entityViewClass;
        this.setting = EntityViewSetting.create(entityViewClass);
    }

    public void setRepositoryMethodMetadata(CrudMethodMetadata crudMethodMetadata) {
        this.metadata = crudMethodMetadata;
    }

    protected CrudMethodMetadata getRepositoryMethodMetadata() {
        return metadata;
    }

    protected Class<T> getDomainClass() {
        return entityInformation.getJavaType();
    }

    protected Map<String, Object> getQueryHints() {

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
    public T findOne(ID id) {
        Assert.notNull(id, ID_MUST_NOT_BE_NULL);

        CriteriaBuilder<?> cb = cbf.create(entityManager, getDomainClass())
                .where(getIdAttribute()).eq(id);
        TypedQuery<T> findOneQuery = evm.applySetting(setting, cb).getQuery();

        if (metadata != null) {
            applyQueryHints(getQueryHints(), findOneQuery);
        }

        return findOneQuery.getSingleResult();
    }

    @Override
    public long count() {
        TypedQuery<Long> countQuery = cbf.create(entityManager, Long.class)
                .from(getDomainClass())
                .select("COUNT(*)")
                .getQuery();

        if (metadata != null) {
            applyQueryHints(getQueryHints(), countQuery);
        }
        return countQuery.getSingleResult();
    }

    @Override
    public boolean exists(ID id) {
        Assert.notNull(id, ID_MUST_NOT_BE_NULL);

        TypedQuery<Boolean> existsQuery = cbf.create(entityManager, Boolean.class)
                .from(getDomainClass())
                .selectCase().when("COUNT(*)").gtExpression("0").thenExpression("true").otherwiseExpression("false")
                .where(getIdAttribute()).eq(id)
                .getQuery();

        if (metadata != null) {
            applyQueryHints(getQueryHints(), existsQuery);
        }

        return existsQuery.getSingleResult();
    }

    @Override
    public Iterable<T> findAll() {
        CriteriaBuilder<?> cb = cbf.create(entityManager, getDomainClass());
        TypedQuery<T> findAllQuery = evm.applySetting(setting, cb).getQuery();

        if (metadata != null) {
            applyQueryHints(getQueryHints(), findAllQuery);
        }
        return findAllQuery.getResultList();
    }

    @Override
    public Iterable<T> findAll(Iterable<ID> idIterable) {
        Assert.notNull(idIterable, ID_MUST_NOT_BE_NULL);

        List<ID> idList = new ArrayList<>();
        for (ID id : idIterable) {
            idList.add(id);
        }
        CriteriaBuilder<?> cb = cbf.create(entityManager, getDomainClass())
                .where(getIdAttribute()).in(idList);
        TypedQuery<T> findAllByIdsQuery = evm.applySetting(setting, cb).getQuery();

        if (metadata != null) {
            applyQueryHints(getQueryHints(), findAllByIdsQuery);
        }
        return findAllByIdsQuery.getResultList();
    }

    private String getIdAttribute(Class<?> entityClass) {
        EntityType<?> entityType = entityManager.getMetamodel().entity(entityClass);
        return entityType.getDeclaredId(entityType.getIdType().getJavaType()).getName();
    }

    private String getIdAttribute() {
        return getIdAttribute(getDomainClass());
    }

    private void applyQueryHints(Map<String, Object> hints, TypedQuery<?> query) {
        for (Map.Entry<String, Object> hint : hints.entrySet()) {
            query.setHint(hint.getKey(), hint.getValue());
        }
    }
}
