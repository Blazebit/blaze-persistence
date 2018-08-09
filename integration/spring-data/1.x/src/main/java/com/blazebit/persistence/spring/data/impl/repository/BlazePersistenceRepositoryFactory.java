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
import com.blazebit.persistence.spring.data.base.query.EntityViewAwareJpaQueryMethod;
import com.blazebit.persistence.spring.data.base.query.EntityViewAwareRepositoryMetadata;
import com.blazebit.persistence.spring.data.base.repository.EntityViewAwareCrudMethodMetadata;
import com.blazebit.persistence.spring.data.base.repository.EntityViewAwareCrudMethodMetadataPostProcessor;
import com.blazebit.persistence.spring.data.base.repository.AbstractEntityViewAwareRepository;
import com.blazebit.persistence.spring.data.impl.query.EntityViewAwareRepositoryInformation;
import com.blazebit.persistence.spring.data.impl.query.EntityViewAwareRepositoryMetadataImpl;
import com.blazebit.persistence.spring.data.impl.query.PartTreeBlazePersistenceQuery;
import com.blazebit.persistence.view.EntityViewManager;
import org.springframework.data.jpa.provider.PersistenceProvider;
import org.springframework.data.jpa.provider.QueryExtractor;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryProxyPostProcessor;
import org.springframework.data.repository.query.EvaluationContextProvider;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Partly copied from {@link JpaRepositoryFactory} to retain functionality but mostly original.
 *
 * @author Moritz Becker
 * @author Christian Beikov
 * @since 1.2.0
 */
public class BlazePersistenceRepositoryFactory extends JpaRepositoryFactory {

    private final EntityManager entityManager;
    private final CriteriaBuilderFactory cbf;
    private final EntityViewManager evm;
    private final QueryExtractor extractor;
    private List<RepositoryProxyPostProcessor> postProcessors;
    private EntityViewAwareCrudMethodMetadataPostProcessor crudMethodMetadataPostProcessor;

    public BlazePersistenceRepositoryFactory(EntityManager entityManager, CriteriaBuilderFactory cbf, EntityViewManager evm) {
        super(entityManager);
        this.entityManager = entityManager;
        this.extractor = PersistenceProvider.fromEntityManager(entityManager);
        this.cbf = cbf;
        this.evm = evm;
        addRepositoryProxyPostProcessor(this.crudMethodMetadataPostProcessor = new EntityViewAwareCrudMethodMetadataPostProcessor(evm));
    }

    @Override
    public void addRepositoryProxyPostProcessor(RepositoryProxyPostProcessor processor) {
        if (crudMethodMetadataPostProcessor != null) {
            Assert.notNull(processor, "RepositoryProxyPostProcessor must not be null!");
            super.addRepositoryProxyPostProcessor(processor);
            if (postProcessors == null) {
                this.postProcessors = new ArrayList<>();
            }
            this.postProcessors.add(processor);
        }
    }

    protected EntityViewAwareCrudMethodMetadata getCrudMethodMetadata() {
        return crudMethodMetadataPostProcessor == null ? null : crudMethodMetadataPostProcessor.getCrudMethodMetadata();
    }

    @Override
    protected RepositoryMetadata getRepositoryMetadata(Class<?> repositoryInterface) {
        return new EntityViewAwareRepositoryMetadataImpl(super.getRepositoryMetadata(repositoryInterface), evm);
    }

    @Override
    protected RepositoryInformation getRepositoryInformation(RepositoryMetadata metadata, Class<?> customImplementationClass) {
        return new EntityViewAwareRepositoryInformation((EntityViewAwareRepositoryMetadata) metadata, super.getRepositoryInformation(metadata, customImplementationClass));
    }

    @Override
    protected Object getTargetRepository(RepositoryInformation information) {
        // TODO: at some point, we might want to switch to the default if the repository doesn't contain entity views or keyset pagination
        JpaEntityInformation<?, Serializable> entityInformation = getEntityInformation(information.getDomainType());
        AbstractEntityViewAwareRepository<?, ?, ?> entityViewAwareRepository = getTargetRepositoryViaReflection(information, entityInformation, entityManager, cbf, evm, ((EntityViewAwareRepositoryInformation) information).getEntityViewType());
        entityViewAwareRepository.setRepositoryMethodMetadata(getCrudMethodMetadata());
        return entityViewAwareRepository;
    }

    @Override
    protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
        // TODO: at some point, we might want to switch to the default if the repository doesn't contain entity views or keyset pagination
        return EntityViewAwareRepositoryImpl.class;
    }

    @Override
    protected QueryLookupStrategy getQueryLookupStrategy(QueryLookupStrategy.Key key, EvaluationContextProvider evaluationContextProvider) {
        switch (key != null ? key : QueryLookupStrategy.Key.CREATE_IF_NOT_FOUND) {
            case CREATE:
                return new CreateQueryLookupStrategy(entityManager, extractor, cbf, evm);
            case USE_DECLARED_QUERY:
                return new DelegateQueryLookupStrategy(super.getQueryLookupStrategy(key, evaluationContextProvider));
            case CREATE_IF_NOT_FOUND:
                return new CreateIfNotFoundQueryLookupStrategy(entityManager, extractor, new CreateQueryLookupStrategy(entityManager, extractor, cbf, evm),
                        new DelegateQueryLookupStrategy(super.getQueryLookupStrategy(QueryLookupStrategy.Key.USE_DECLARED_QUERY, evaluationContextProvider)));
            default:
                throw new IllegalArgumentException(String.format("Unsupported query lookup strategy %s!", key));
        }
    }

    private static class CreateQueryLookupStrategy implements QueryLookupStrategy {

        private final EntityManager em;
        private final QueryExtractor provider;
        private final PersistenceProvider persistenceProvider;
        private final CriteriaBuilderFactory cbf;
        private final EntityViewManager evm;

        public CreateQueryLookupStrategy(EntityManager em, QueryExtractor extractor, CriteriaBuilderFactory cbf, EntityViewManager evm) {
            this.em = em;
            this.provider = extractor;
            this.persistenceProvider = PersistenceProvider.fromEntityManager(em);
            this.cbf = cbf;
            this.evm = evm;
        }

        @Override
        public RepositoryQuery resolveQuery(Method method, RepositoryMetadata metadata, ProjectionFactory factory, NamedQueries namedQueries) {
            try {
                // TODO: at some point, we might want to switch to the default if the repository doesn't contain entity views or keyset pagination
                return new PartTreeBlazePersistenceQuery(new EntityViewAwareJpaQueryMethod(method, (EntityViewAwareRepositoryMetadata) metadata, factory, provider), em, persistenceProvider, cbf, evm);
            } catch (RuntimeException e) {
                throw new IllegalArgumentException(
                        String.format("Could not create query metamodel for method %s!", method.toString()), e);
            }
        }
    }

    private static class DelegateQueryLookupStrategy implements QueryLookupStrategy {

        private final QueryLookupStrategy delegate;

        public DelegateQueryLookupStrategy(QueryLookupStrategy delegate) {
            this.delegate = delegate;
        }

        @Override
        public RepositoryQuery resolveQuery(Method method, RepositoryMetadata metadata, ProjectionFactory factory, NamedQueries namedQueries) {
            return delegate.resolveQuery(method, metadata, factory, namedQueries);
        }
    }

    private static class CreateIfNotFoundQueryLookupStrategy implements QueryLookupStrategy {

        private final EntityManager em;
        private final QueryExtractor provider;
        private final DelegateQueryLookupStrategy lookupStrategy;
        private final CreateQueryLookupStrategy createStrategy;

        public CreateIfNotFoundQueryLookupStrategy(EntityManager em, QueryExtractor extractor,
                                                   CreateQueryLookupStrategy createStrategy, DelegateQueryLookupStrategy lookupStrategy) {
            this.em = em;
            this.provider = extractor;
            this.createStrategy = createStrategy;
            this.lookupStrategy = lookupStrategy;
        }

        @Override
        public RepositoryQuery resolveQuery(Method method, RepositoryMetadata metadata, ProjectionFactory factory, NamedQueries namedQueries) {
            try {
                return lookupStrategy.resolveQuery(method, metadata, factory, namedQueries);
            } catch (IllegalStateException e) {
                return createStrategy.resolveQuery(method, metadata, factory, namedQueries);
            }
        }
    }

    // Make the protected method public for BlazeRepositoryFactoryBeanSupport
    @Override
    public List<QueryMethod> getQueryMethods() {
        return super.getQueryMethods();
    }

}
