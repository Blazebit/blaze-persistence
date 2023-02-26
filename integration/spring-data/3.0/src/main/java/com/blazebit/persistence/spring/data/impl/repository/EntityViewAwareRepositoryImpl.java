/*
 * Copyright 2014 - 2023 Blazebit.
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

import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.spring.data.base.repository.AbstractEntityViewAwareRepository;
import com.blazebit.persistence.spring.data.repository.EntityViewRepository;
import com.blazebit.persistence.spring.data.repository.KeysetPageable;
import com.blazebit.persistence.view.EntityViewManager;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.query.Jpa21Utils;
import org.springframework.data.jpa.repository.query.JpaEntityGraph;
import org.springframework.data.jpa.repository.support.CrudMethodMetadata;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author Christian Beikov
 * @author Eugen Mayer
 * @since 1.6.9
 */
@Transactional(readOnly = true)
public class EntityViewAwareRepositoryImpl<V, E, ID extends Serializable> extends AbstractEntityViewAwareRepository<E, E, ID> implements JpaRepositoryImplementation<E, ID>, EntityViewRepository<E, ID>/*, EntityViewSpecificationExecutor<V, E>*/ { // Can't implement that interface because of clashing methods

    public EntityViewAwareRepositoryImpl(JpaEntityInformation<E, ?> entityInformation, EntityManager entityManager, CriteriaBuilderFactory cbf, EntityViewManager evm, Class<V> entityViewClass) {
        super(entityInformation, entityManager, cbf, evm, (Class<E>) entityViewClass);
    }

    @Override
    protected Map<String, Object> tryGetFetchGraphHints(EntityManager entityManager, JpaEntityGraph entityGraph, Class<?> entityType) {
        Map<String, Object> map = new HashMap<>();
        Jpa21Utils.getFetchGraphHint(entityManager, entityGraph, entityType).forEach( map::put );
        return map;
    }

    @Override
    public <S extends E> Optional<S> findOne(Example<S> example) {
        try {
            return Optional.of(getQuery(new ExampleSpecification<>(example, escapeCharacter), example.getProbeType(), (Sort) null).getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<E> findOne(Specification<E> spec) {
        try {
            return Optional.of((E) getQuery(spec, (Sort) null).getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<E> findById(ID id) {
        return Optional.ofNullable((E) findOne(id));
    }

    public E getById(ID id) {
        return (E) getReference(id);
    }

    @Override
    public void setRepositoryMethodMetadata(CrudMethodMetadata crudMethodMetadata) {
        // Ignore the Spring data version of the CrudMethodMetadata
    }

    @Override
    protected int getOffset(Pageable pageable) {
        if (pageable instanceof KeysetPageable) {
            return ((KeysetPageable) pageable).getIntOffset();
        } else {
            return (int) pageable.getOffset();
        }
    }

    @Override
    public <S extends E> long count(Example<S> example) {
        return super.count(example);
    }

    @Override
    public <S extends E> boolean exists(Example<S> example) {
        return super.exists(example);
    }

    @Override
    public <S extends E, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        Assert.notNull(example, "Sample must not be null!");
        Assert.notNull(queryFunction, "Query function must not be null!");

        Function<Sort, TypedQuery<S>> finder = sort -> {

            ExampleSpecification<S> spec = new ExampleSpecification<>(example, escapeCharacter);
            Class<S> probeType = example.getProbeType();

            return getQuery(spec, probeType, sort);
        };

        FluentQuery.FetchableFluentQuery<S> fluentQuery = new FetchableFluentQueryByExample<>(example, finder, this::count, this::exists, getEntityManager(), this.escapeCharacter);

        return queryFunction.apply(fluentQuery);
    }

    public <S extends E, R> R findBy(Specification<E> spec, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        // TODO: implement
        return null;
    }

    @Override
    public long delete(Specification<E> spec) {
        // TODO: implement
        return 0;
    }
}
