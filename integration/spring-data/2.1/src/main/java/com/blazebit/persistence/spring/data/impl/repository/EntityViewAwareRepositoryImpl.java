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

import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.spring.data.base.repository.AbstractEntityViewAwareRepository;
import com.blazebit.persistence.spring.data.repository.EntityViewRepository;
import com.blazebit.persistence.view.EntityViewManager;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.CrudMethodMetadata;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.io.Serializable;
import java.util.Optional;

/**
 * @author Christian Beikov
 * @since 1.3.0
 */
@Transactional(readOnly = true)
public class EntityViewAwareRepositoryImpl<V, E, ID extends Serializable> extends AbstractEntityViewAwareRepository<E, E, ID> implements JpaRepositoryImplementation<E, ID>, EntityViewRepository<E, ID>/*, EntityViewSpecificationExecutor<V, E>*/ { // Can't implement that interface because of clashing methods

    public EntityViewAwareRepositoryImpl(JpaEntityInformation<E, ?> entityInformation, EntityManager entityManager, CriteriaBuilderFactory cbf, EntityViewManager evm, Class<V> entityViewClass) {
        super(entityInformation, entityManager, cbf, evm, (Class<E>) entityViewClass);
    }

    @Override
    public <S extends E> Optional<S> findOne(Example<S> example) {
        try {
            return Optional.of(getQuery(new ExampleSpecification<>(example), example.getProbeType(), (Sort) null).getSingleResult());
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
        return Optional.of((E) findOne(id));
    }

    @Override
    public void setRepositoryMethodMetadata(CrudMethodMetadata crudMethodMetadata) {
        // Ignore the Spring data version of the CrudMethodMetadata
    }

    @Override
    protected int getOffset(Pageable pageable) {
        return (int) pageable.getOffset();
    }
}
