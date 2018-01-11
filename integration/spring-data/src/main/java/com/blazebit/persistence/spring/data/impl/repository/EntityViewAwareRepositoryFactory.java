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
import com.blazebit.persistence.spring.data.impl.query.BlazePersistenceQueryLookupStrategy;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.EntityViewManager;
import org.springframework.data.jpa.provider.PersistenceProvider;
import org.springframework.data.jpa.provider.QueryExtractor;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.EvaluationContextProvider;
import org.springframework.data.repository.query.QueryLookupStrategy;

import javax.persistence.EntityManager;
import java.io.Serializable;

/**
 * @author Moritz Becker (moritz.becker@gmx.at)
 * @since 1.2
 */
public class EntityViewAwareRepositoryFactory extends JpaRepositoryFactory {

    private final EntityManager entityManager;
    private final CriteriaBuilderFactory cbf;
    private final EntityViewManager evm;
    private final QueryExtractor extractor;

    /**
     * Creates a new {@link JpaRepositoryFactory}.
     *
     * @param entityManager must not be {@literal null}
     * @param cbf
     * @param evm
     */
    public EntityViewAwareRepositoryFactory(EntityManager entityManager, CriteriaBuilderFactory cbf, EntityViewManager evm) {
        super(entityManager);
        this.entityManager = entityManager;
        this.extractor = PersistenceProvider.fromEntityManager(entityManager);
        this.cbf = cbf;
        this.evm = evm;
    }

    @Override
    public <T, ID extends Serializable> JpaEntityInformation<T, ID> getEntityInformation(Class<T> domainClass) {
        if (isEntityView(domainClass)) {
            return super.getEntityInformation((Class<T>) domainClass.getAnnotation(EntityView.class).value());
        } else {
            return super.getEntityInformation(domainClass);
        }
    }

    @Override
    protected Object getTargetRepository(RepositoryInformation information) {
        if (isEntityView(information.getDomainType())) {
            JpaEntityInformation<?, Serializable> entityInformation = getEntityInformation(information.getDomainType());
            EntityViewRepositoryImpl<?, ?, ?> entityViewRepository = getTargetRepositoryViaReflection(information, entityInformation, entityManager, cbf, evm, information.getDomainType());
            return entityViewRepository;
        } else {
            return super.getTargetRepository(information);
        }
    }

    @Override
    protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
        if (isEntityView(metadata.getDomainType())) {
            return EntityViewRepositoryImpl.class;
        } else {
            return super.getRepositoryBaseClass(metadata);
        }
    }

    @Override
    protected QueryLookupStrategy getQueryLookupStrategy(QueryLookupStrategy.Key key, EvaluationContextProvider evaluationContextProvider) {
        return BlazePersistenceQueryLookupStrategy.create(entityManager, key, extractor, evaluationContextProvider, cbf, evm);
    }

    private boolean isEntityView(Class<?> clazz) {
        return clazz.isAnnotationPresent(EntityView.class);
    }

}
