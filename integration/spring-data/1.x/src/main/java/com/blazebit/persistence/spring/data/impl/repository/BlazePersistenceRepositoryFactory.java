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
import com.blazebit.persistence.spring.data.base.repository.EntityViewAwareCrudMethodMetadata;
import com.blazebit.persistence.spring.data.base.repository.EntityViewAwareCrudMethodMetadataPostProcessor;
import com.blazebit.persistence.spring.data.base.repository.EntityViewAwareRepositoryImpl;
import com.blazebit.persistence.spring.data.impl.query.EntityViewAwareRepositoryInformation;
import com.blazebit.persistence.spring.data.base.query.EntityViewAwareRepositoryMetadata;
import com.blazebit.persistence.spring.data.impl.query.EntityViewAwareRepositoryMetadataImpl;
import com.blazebit.persistence.spring.data.impl.query.PartTreeBlazePersistenceQuery;
import com.blazebit.persistence.view.EntityViewManager;
import org.springframework.data.jpa.provider.PersistenceProvider;
import org.springframework.data.jpa.provider.QueryExtractor;
import org.springframework.data.jpa.repository.query.JpaQueryMethod;
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

    /**
     * Creates a new {@link JpaRepositoryFactory}.
     *
     * @param entityManager must not be {@literal null}
     * @param cbf
     * @param evm
     */
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
        EntityViewAwareRepositoryImpl<?, ?, ?> entityViewAwareRepository = getTargetRepositoryViaReflection(information, entityInformation, entityManager, cbf, evm, ((EntityViewAwareRepositoryInformation) information).getEntityViewType());
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
            case CREATE_IF_NOT_FOUND:
                return new CreateQueryLookupStrategy(entityManager, extractor, cbf, evm);
            default:
                throw new IllegalArgumentException(String.format("Unsupported query lookup strategy %s!", key));
        }
    }

    /**
     * Base class for {@link QueryLookupStrategy} implementations that need access to an {@link EntityManager}.
     *
     * @author Oliver Gierke
     * @author Thomas Darimont
     */
    private abstract static class AbstractQueryLookupStrategy implements QueryLookupStrategy {

        private final EntityManager em;
        private final QueryExtractor provider;

        /**
         * Creates a new {@link BlazePersistenceQueryLookupStrategy.AbstractQueryLookupStrategy}.
         * @param em the entity manager
         * @param extractor the query extractor
         */
        public AbstractQueryLookupStrategy(EntityManager em, QueryExtractor extractor) {

            this.em = em;
            this.provider = extractor;
        }

        /*
         * (non-Javadoc)
         * @see org.springframework.data.repository.query.QueryLookupStrategy#resolveQuery(java.lang.reflect.Method, org.springframework.data.repository.core.RepositoryMetadata, org.springframework.data.projection.ProjectionFactory, org.springframework.data.repository.core.NamedQueries)
         */
        @Override
        public final RepositoryQuery resolveQuery(Method method, RepositoryMetadata metadata, ProjectionFactory factory,
                                                  NamedQueries namedQueries) {
            return resolveQuery(new EntityViewAwareJpaQueryMethod(method, (EntityViewAwareRepositoryMetadata) metadata, factory, provider), em, namedQueries);
        }

        protected abstract RepositoryQuery resolveQuery(JpaQueryMethod method, EntityManager em, NamedQueries namedQueries);
    }

    /**
     * {@link QueryLookupStrategy} to create a query from the method name.
     *
     * @author Oliver Gierke
     * @author Thomas Darimont
     */
    private static class CreateQueryLookupStrategy extends AbstractQueryLookupStrategy {

        private final PersistenceProvider persistenceProvider;
        private final CriteriaBuilderFactory cbf;
        private final EntityViewManager evm;

        public CreateQueryLookupStrategy(EntityManager em, QueryExtractor extractor, CriteriaBuilderFactory cbf, EntityViewManager evm) {
            super(em, extractor);
            this.persistenceProvider = PersistenceProvider.fromEntityManager(em);
            this.cbf = cbf;
            this.evm = evm;
        }

        @Override
        protected RepositoryQuery resolveQuery(JpaQueryMethod method, EntityManager em, NamedQueries namedQueries) {

            try {
                // TODO: at some point, we might want to switch to the default if the repository doesn't contain entity views or keyset pagination
                return new PartTreeBlazePersistenceQuery((EntityViewAwareJpaQueryMethod) method, em, persistenceProvider, cbf, evm);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                        String.format("Could not create query metamodel for method %s!", method.toString()), e);
            }
        }
    }

    // Make the protected method public for BlazeRepositoryFactoryBeanSupport
    @Override
    public List<QueryMethod> getQueryMethods() {
        return super.getQueryMethods();
    }

}
