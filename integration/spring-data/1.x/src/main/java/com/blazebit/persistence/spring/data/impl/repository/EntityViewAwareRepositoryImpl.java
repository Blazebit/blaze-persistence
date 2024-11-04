/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.data.impl.repository;

import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.spring.data.base.repository.AbstractEntityViewAwareRepository;
import com.blazebit.persistence.spring.data.repository.EntityViewRepository;
import com.blazebit.persistence.spring.data.repository.EntityViewSpecificationExecutor;
import com.blazebit.persistence.spring.data.repository.KeysetPageable;
import com.blazebit.persistence.view.EntityViewManager;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.query.Jpa21Utils;
import org.springframework.data.jpa.repository.query.JpaEntityGraph;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.io.Serializable;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.3.0
 */
@Transactional(readOnly = true)
public class EntityViewAwareRepositoryImpl<V, E, ID extends Serializable> extends AbstractEntityViewAwareRepository<V, E, ID> implements EntityViewRepository<V, ID>, EntityViewSpecificationExecutor<V, E> {

    public EntityViewAwareRepositoryImpl(JpaEntityInformation<E, ?> entityInformation, EntityManager entityManager, CriteriaBuilderFactory cbf, EntityViewManager evm, Class<V> entityViewClass) {
        super(entityInformation, entityManager, cbf, evm, entityViewClass);
    }

    @Override
    protected Map<String, Object> tryGetFetchGraphHints(EntityManager entityManager, JpaEntityGraph entityGraph, Class<?> entityType) {
        return Jpa21Utils.tryGetFetchGraphHints(entityManager, entityGraph, entityType);
    }

    public <S extends E> S findOne(Example<S> example) {
        try {
            return getQuery(new ExampleSpecification<>(example, escapeCharacter), example.getProbeType(), (Sort) null).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public V findOne(Specification<E> spec) {
        try {
            return (V) getQuery(spec, (Sort) null).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public E findById(ID id) {
        return (E) findOne(id);
    }

    @Override
    protected int getOffset(Pageable pageable) {
        if (pageable instanceof KeysetPageable) {
            return ((KeysetPageable) pageable).getIntOffset();
        } else {
            return pageable.getOffset();
        }
    }
}
