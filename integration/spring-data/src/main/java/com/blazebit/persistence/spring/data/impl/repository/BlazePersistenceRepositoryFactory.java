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
import com.blazebit.persistence.spring.data.impl.query.EntityViewAwareRepositoryInformation;
import com.blazebit.persistence.spring.data.impl.query.EntityViewAwareRepositoryMetadata;
import com.blazebit.persistence.spring.data.impl.query.EntityViewAwareRepositoryMetadataImpl;
import com.blazebit.persistence.view.EntityViewManager;
import org.aopalliance.aop.Advice;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.interceptor.ExposeInvocationInterceptor;
import org.springframework.data.jpa.provider.PersistenceProvider;
import org.springframework.data.jpa.provider.QueryExtractor;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.projection.DefaultMethodInvokingMethodInterceptor;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.core.support.RepositoryProxyPostProcessor;
import org.springframework.data.repository.query.EvaluationContextProvider;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;
import java.io.Serializable;
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

    private static final boolean IS_JAVA_8 = org.springframework.util.ClassUtils.isPresent("java.util.Optional",
            RepositoryFactorySupport.class.getClassLoader());
    private static final Class<?> TRANSACTION_PROXY_TYPE = getTransactionProxyType();

    private final EntityManager entityManager;
    private final CriteriaBuilderFactory cbf;
    private final EntityViewManager evm;
    private final QueryExtractor extractor;
    private List<RepositoryProxyPostProcessor> postProcessors;
    private EntityViewAwareCrudMethodMetadataPostProcessor crudMethodMetadataPostProcessor;
    private ClassLoader classLoader;

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
    protected RepositoryInformation getRepositoryInformation(RepositoryMetadata metadata,
                                                             Class<?> customImplementationClass) {
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
        return BlazePersistenceQueryLookupStrategy.create(entityManager, key, extractor, evaluationContextProvider, cbf, evm);
    }

    private Advice createQueryExecutorMethodInterceptor(RepositoryInformation information, Object customImplementation, Object target) {
        return new QueryExecutorMethodInterceptor(information, customImplementation, target);
    }

    /* Mostly copied from here on to be able to replace the QueryExecutorMethodInterceptor */

    public <T> T getRepository(Class<T> repositoryInterface, Object customImplementation) {

        RepositoryMetadata metadata = getRepositoryMetadata(repositoryInterface);
        Class<?> customImplementationClass = null == customImplementation ? null : customImplementation.getClass();
        RepositoryInformation information = getRepositoryInformation(metadata, customImplementationClass);

        validate(information, customImplementation);

        Object target = getTargetRepository(information);

        // Create proxy
        ProxyFactory result = new ProxyFactory();
        result.setTarget(target);
        result.setInterfaces(new Class[] { repositoryInterface, Repository.class });

        result.addAdvice(ExposeInvocationInterceptor.INSTANCE);

        if (TRANSACTION_PROXY_TYPE != null) {
            result.addInterface(TRANSACTION_PROXY_TYPE);
        }

        for (RepositoryProxyPostProcessor processor : postProcessors) {
            processor.postProcess(result, information);
        }

        if (IS_JAVA_8) {
            result.addAdvice(new DefaultMethodInvokingMethodInterceptor());
        }

        result.addAdvice(createQueryExecutorMethodInterceptor(information, customImplementation, target));

        return (T) result.getProxy(classLoader);
    }

    /**
     * Validates the given repository interface as well as the given custom implementation.
     *
     * @param repositoryInformation
     * @param customImplementation
     */
    private void validate(RepositoryInformation repositoryInformation, Object customImplementation) {

        if (null == customImplementation && repositoryInformation.hasCustomMethod()) {

            throw new IllegalArgumentException(
                    String.format("You have custom methods in %s but not provided a custom implementation!",
                            repositoryInformation.getRepositoryInterface()));
        }

        validate(repositoryInformation);
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        super.setBeanClassLoader(classLoader);
        this.classLoader = classLoader == null ? org.springframework.util.ClassUtils.getDefaultClassLoader() : classLoader;
    }

    /**
     * Returns the TransactionProxy type or {@literal null} if not on the classpath.
     *
     * @return
     */
    private static Class<?> getTransactionProxyType() {

        try {
            return org.springframework.util.ClassUtils
                    .forName("org.springframework.transaction.interceptor.TransactionalProxy", null);
        } catch (ClassNotFoundException o_O) {
            return null;
        }
    }

}
