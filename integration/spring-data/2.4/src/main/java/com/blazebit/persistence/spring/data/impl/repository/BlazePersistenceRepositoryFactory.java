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
import com.blazebit.persistence.parser.EntityMetamodel;
import com.blazebit.persistence.spi.ExtendedManagedType;
import com.blazebit.persistence.spring.data.base.query.EntityViewAwareJpaQueryMethod;
import com.blazebit.persistence.spring.data.base.query.EntityViewAwareRepositoryMetadata;
import com.blazebit.persistence.spring.data.base.repository.AbstractEntityViewAwareRepository;
import com.blazebit.persistence.spring.data.base.repository.EntityViewAwareCrudMethodMetadata;
import com.blazebit.persistence.spring.data.base.repository.EntityViewAwareCrudMethodMetadataPostProcessor;
import com.blazebit.persistence.spring.data.impl.query.EntityViewAwareRepositoryInformation;
import com.blazebit.persistence.spring.data.impl.query.EntityViewAwareRepositoryMetadataImpl;
import com.blazebit.persistence.spring.data.impl.query.PartTreeBlazePersistenceQuery;
import com.blazebit.persistence.spring.data.repository.EntityViewReplacingMethodInterceptor;
import com.blazebit.persistence.view.EntityViewManager;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.interceptor.ExposeInvocationInterceptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.jpa.provider.PersistenceProvider;
import org.springframework.data.jpa.provider.QueryExtractor;
import org.springframework.data.jpa.repository.query.EscapeCharacter;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.data.projection.DefaultMethodInvokingMethodInterceptor;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.MethodInvocationValidator;
import org.springframework.data.repository.core.support.PropertiesBasedNamedQueries;
import org.springframework.data.repository.core.support.QueryCreationListener;
import org.springframework.data.repository.core.support.RepositoryComposition;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.core.support.RepositoryFragment;
import org.springframework.data.repository.core.support.RepositoryMethodInvocationListener;
import org.springframework.data.repository.core.support.RepositoryProxyPostProcessor;
import org.springframework.data.repository.core.support.SurroundingTransactionDetectorMethodInterceptor;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.util.ClassUtils;
import org.springframework.data.repository.util.ReactiveWrapperConverters;
import org.springframework.data.repository.util.ReactiveWrappers;
import org.springframework.lang.Nullable;
import org.springframework.transaction.interceptor.TransactionalProxy;
import org.springframework.util.Assert;
import org.springframework.util.ConcurrentReferenceHashMap;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * Partly copied from {@link JpaRepositoryFactory} to retain functionality but mostly original.
 *
 * @author Moritz Becker
 * @author Christian Beikov
 * @since 1.5.0
 */
public class BlazePersistenceRepositoryFactory extends JpaRepositoryFactory {

    private static final Constructor<Advice> IMPLEMENTATION_METHOD_EXECUTION_INTERCEPTOR;
    private static final Constructor<Advice> QUERY_EXECUTOR_METHOD_INTERCEPTOR;

    static {
        Constructor<Advice> implementationMethodExecutionInterceptor;
        Constructor<Advice> queryExecutorMethodInterceptor;
        try {
            implementationMethodExecutionInterceptor = (Constructor<Advice>) Class.forName("org.springframework.data.repository.core.support.RepositoryFactorySupport$ImplementationMethodExecutionInterceptor")
                    .getConstructor(
                            RepositoryInformation.class,
                            RepositoryComposition.class,
                            List.class
                    );
            implementationMethodExecutionInterceptor.setAccessible(true);
            queryExecutorMethodInterceptor = (Constructor<Advice>) Class.forName("org.springframework.data.repository.core.support.QueryExecutorMethodInterceptor")
                    .getConstructor(
                            RepositoryInformation.class,
                            ProjectionFactory.class,
                            Optional.class,
                            NamedQueries.class,
                            List.class,
                            List.class
                    );
            queryExecutorMethodInterceptor.setAccessible(true);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        IMPLEMENTATION_METHOD_EXECUTION_INTERCEPTOR = implementationMethodExecutionInterceptor;
        QUERY_EXECUTOR_METHOD_INTERCEPTOR = queryExecutorMethodInterceptor;
    }

    private final EntityManager entityManager;
    private final CriteriaBuilderFactory cbf;
    private final EntityViewManager evm;
    private final QueryExtractor extractor;
    private final Map<RepositoryInformationCacheKey, RepositoryInformation> repositoryInformationCache;
    private final EntityViewReplacingMethodInterceptor entityViewReplacingMethodInterceptor;
    private List<RepositoryProxyPostProcessor> postProcessors;
    private EntityViewAwareCrudMethodMetadataPostProcessor crudMethodMetadataPostProcessor;
    private Optional<Class<?>> repositoryBaseClass;
    private QueryLookupStrategy.Key queryLookupStrategyKey;
    private List<QueryCreationListener<?>> queryPostProcessors;
    private List<RepositoryMethodInvocationListener> methodInvocationListeners;
    private NamedQueries namedQueries;
    private EscapeCharacter escapeCharacter = EscapeCharacter.DEFAULT;
    private ClassLoader classLoader;
    private QueryMethodEvaluationContextProvider evaluationContextProvider;
    private BeanFactory beanFactory;

    private final QueryCollectingQueryCreationListener collectingListener = new QueryCollectingQueryCreationListener();

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
        this.repositoryInformationCache = new ConcurrentReferenceHashMap<>(16, ConcurrentReferenceHashMap.ReferenceType.WEAK);
        this.cbf = cbf;
        this.evm = evm;
        this.namedQueries = PropertiesBasedNamedQueries.EMPTY;
        this.evaluationContextProvider = QueryMethodEvaluationContextProvider.DEFAULT;
        this.queryPostProcessors = new ArrayList<>();
        this.queryPostProcessors.add(collectingListener);
        this.methodInvocationListeners = new ArrayList<>();
        addRepositoryProxyPostProcessor(this.crudMethodMetadataPostProcessor = new EntityViewAwareCrudMethodMetadataPostProcessor());
        this.repositoryBaseClass = Optional.empty();
        this.entityViewReplacingMethodInterceptor = new EntityViewReplacingMethodInterceptor(entityManager, evm);
    }

    @Override
    public void setQueryLookupStrategyKey(QueryLookupStrategy.Key key) {
        this.queryLookupStrategyKey = key;
    }

    @Override
    public void setNamedQueries(NamedQueries namedQueries) {
        this.namedQueries = namedQueries == null ? PropertiesBasedNamedQueries.EMPTY : namedQueries;
    }

    @Override
    public void addQueryCreationListener(QueryCreationListener<?> listener) {
        Assert.notNull(listener, "Listener must not be null!");
        this.queryPostProcessors.add(listener);
    }

    @Override
    public void addInvocationListener(RepositoryMethodInvocationListener listener) {
        Assert.notNull(listener, "Listener must not be null!");
        this.methodInvocationListeners.add(listener);
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

    @Override
    protected List<QueryMethod> getQueryMethods() {
        return collectingListener.getQueryMethods();
    }

    @Override
    public void setEscapeCharacter(EscapeCharacter escapeCharacter) {
        this.escapeCharacter = escapeCharacter;
    }

    protected EntityViewAwareCrudMethodMetadata getCrudMethodMetadata() {
        return crudMethodMetadataPostProcessor == null ? null : crudMethodMetadataPostProcessor.getCrudMethodMetadata();
    }

    @Override
    protected RepositoryMetadata getRepositoryMetadata(Class<?> repositoryInterface) {
        return new EntityViewAwareRepositoryMetadataImpl(super.getRepositoryMetadata(repositoryInterface), evm);
    }

    @Override
    protected RepositoryInformation getRepositoryInformation(RepositoryMetadata metadata, RepositoryComposition.RepositoryFragments fragments) {
        return getRepositoryInformation(metadata, super.getRepositoryInformation(metadata, fragments));
    }

    protected RepositoryInformation getRepositoryInformation(RepositoryMetadata metadata, RepositoryInformation repositoryInformation) {
        return new EntityViewAwareRepositoryInformation((EntityViewAwareRepositoryMetadata) metadata, repositoryInformation);
    }

    @Override
    protected void validate(RepositoryMetadata repositoryMetadata) {
        super.validate(repositoryMetadata);

        if (cbf.getService(EntityMetamodel.class).getEntity(repositoryMetadata.getDomainType()) == null) {
            throw new InvalidDataAccessApiUsageException(
                    String.format("Cannot implement repository %s when using a non-entity domain type %s. Only types annotated with @Entity are supported!",
                            repositoryMetadata.getRepositoryInterface().getName(), repositoryMetadata.getDomainType().getName()));
        }
    }

    @Override
    protected JpaRepositoryImplementation<?, ?> getTargetRepository(RepositoryInformation information, EntityManager entityManager) {
        if (information instanceof EntityViewAwareRepositoryInformation && information.getRepositoryBaseClass() == EntityViewAwareRepositoryImpl.class) {
            // TODO: at some point, we might want to switch to the default if the repository doesn't contain entity views or keyset pagination
            JpaEntityInformation<?, Serializable> entityInformation = getEntityInformation(information.getDomainType());
            AbstractEntityViewAwareRepository<?, ?, ?> entityViewAwareRepository = getTargetRepositoryViaReflection(information, entityInformation, entityManager, cbf, evm, ((EntityViewAwareRepositoryInformation) information).getEntityViewType());
            entityViewAwareRepository.setRepositoryMethodMetadata(getCrudMethodMetadata());
            return (JpaRepositoryImplementation<?, ?>) entityViewAwareRepository;
        }
        return super.getTargetRepository(information, entityManager);
    }

    @Override
    protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
        ExtendedManagedType<?> managedType = cbf.getService(EntityMetamodel.class).getManagedType(ExtendedManagedType.class, metadata.getDomainType());
        // Only use the entity view aware repository if the domain type has a single id attribute
        if (managedType.getIdAttributes().size() == 1) {
            // TODO: at some point, we might want to switch to the default if the repository doesn't contain entity views or keyset pagination
            return EntityViewAwareRepositoryImpl.class;
        }
        return super.getRepositoryBaseClass(metadata);
    }

    @Override
    protected Optional<QueryLookupStrategy> getQueryLookupStrategy(QueryLookupStrategy.Key key, QueryMethodEvaluationContextProvider evaluationContextProvider) {
        switch (key != null ? key : QueryLookupStrategy.Key.CREATE_IF_NOT_FOUND) {
            case CREATE:
                return Optional.of(new CreateQueryLookupStrategy(entityManager, extractor, escapeCharacter, cbf, evm));
            case USE_DECLARED_QUERY:
                return Optional.of(new DelegateQueryLookupStrategy(super.getQueryLookupStrategy(key, evaluationContextProvider).get()));
            case CREATE_IF_NOT_FOUND:
                return Optional.of(new CreateIfNotFoundQueryLookupStrategy(entityManager, extractor, new CreateQueryLookupStrategy(entityManager, extractor, escapeCharacter, cbf, evm),
                        new DelegateQueryLookupStrategy(super.getQueryLookupStrategy(QueryLookupStrategy.Key.USE_DECLARED_QUERY, evaluationContextProvider).get())));
            default:
                throw new IllegalArgumentException(String.format("Unsupported query lookup strategy %s!", key));
        }
    }

    private static class CreateQueryLookupStrategy implements QueryLookupStrategy {

        private final EntityManager em;
        private final QueryExtractor provider;
        private final PersistenceProvider persistenceProvider;
        private final EscapeCharacter escapeCharacter;
        private final CriteriaBuilderFactory cbf;
        private final EntityViewManager evm;

        public CreateQueryLookupStrategy(EntityManager em, QueryExtractor extractor, EscapeCharacter escapeCharacter, CriteriaBuilderFactory cbf, EntityViewManager evm) {
            this.em = em;
            this.provider = extractor;
            this.persistenceProvider = PersistenceProvider.fromEntityManager(em);
            this.escapeCharacter = escapeCharacter;
            this.cbf = cbf;
            this.evm = evm;
        }

        @Override
        public RepositoryQuery resolveQuery(Method method, RepositoryMetadata metadata, ProjectionFactory factory, NamedQueries namedQueries) {
            try {
                // TODO: at some point, we might want to switch to the default if the repository doesn't contain entity views or keyset pagination
                return new PartTreeBlazePersistenceQuery(new EntityViewAwareJpaQueryMethod(method, (EntityViewAwareRepositoryMetadata) metadata, factory, provider), em, persistenceProvider, escapeCharacter, cbf, evm);
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

    // Mostly copied from RepositoryFactorySupport to be able to use a custom RepositoryInformation implementation

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        super.setBeanClassLoader(classLoader);
        this.classLoader = classLoader;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        super.setBeanFactory(beanFactory);
        this.beanFactory = beanFactory;
    }

    @Override
    public void setRepositoryBaseClass(Class<?> repositoryBaseClass) {
        super.setRepositoryBaseClass(repositoryBaseClass);
        this.repositoryBaseClass = Optional.ofNullable(repositoryBaseClass);
    }

    private static final BiFunction<Method, Object[], Object[]> REACTIVE_ARGS_CONVERTER = (method, o) -> {

        if (ReactiveWrappers.isAvailable()) {

            Class<?>[] parameterTypes = method.getParameterTypes();

            Object[] converted = new Object[o.length];
            for (int i = 0; i < parameterTypes.length; i++) {

                Class<?> parameterType = parameterTypes[i];
                Object value = o[i];

                if (value == null) {
                    continue;
                }

                if (!parameterType.isAssignableFrom(value.getClass())
                        && ReactiveWrapperConverters.canConvert(value.getClass(), parameterType)) {

                    converted[i] = ReactiveWrapperConverters.toWrapper(value, parameterType);
                } else {
                    converted[i] = value;
                }
            }

            return converted;
        }

        return o;
    };

    /**
     * Returns a repository instance for the given interface backed by an instance providing implementation logic for
     * custom logic.
     *
     * @param repositoryInterface must not be {@literal null}.
     * @param fragments must not be {@literal null}.
     * @return
     * @since 2.0
     */
    @SuppressWarnings({ "unchecked" })
    public <T> T getRepository(Class<T> repositoryInterface, RepositoryComposition.RepositoryFragments fragments) {

        Assert.notNull(repositoryInterface, "Repository interface must not be null!");
        Assert.notNull(fragments, "RepositoryFragments must not be null!");

        RepositoryMetadata metadata = getRepositoryMetadata(repositoryInterface);
        RepositoryComposition composition = getRepositoryComposition(metadata, fragments);
        RepositoryInformation information = getRepositoryInformation(metadata, composition);

        validate(information, composition);

        Object target = getTargetRepository(information);

        // Create proxy
        ProxyFactory result = new ProxyFactory();
        result.setTarget(target);
        result.setInterfaces(repositoryInterface, Repository.class, TransactionalProxy.class);

        if (MethodInvocationValidator.supports(repositoryInterface)) {
            result.addAdvice(new MethodInvocationValidator());
        }

        result.addAdvice(SurroundingTransactionDetectorMethodInterceptor.INSTANCE);
        result.addAdvisor(ExposeInvocationInterceptor.ADVISOR);

        postProcessors.forEach(processor -> processor.postProcess(result, information));

        result.addAdvice(entityViewReplacingMethodInterceptor);
        result.addAdvice(new DefaultMethodInvokingMethodInterceptor());

        ProjectionFactory projectionFactory = getProjectionFactory(classLoader, beanFactory);
        Optional<QueryLookupStrategy> queryLookupStrategy = getQueryLookupStrategy(queryLookupStrategyKey,
                evaluationContextProvider);
        result.addAdvice(queryExecutorMethodInterceptor(information, projectionFactory, queryLookupStrategy,
                namedQueries, queryPostProcessors, methodInvocationListeners));

        composition = composition.append(RepositoryFragment.implemented(target));
        result.addAdvice(implementationMethodExecutionInterceptor(information, composition, methodInvocationListeners));

        return (T) result.getProxy(classLoader);
    }

    private Advice implementationMethodExecutionInterceptor(
            RepositoryInformation information,
            RepositoryComposition composition,
            List<RepositoryMethodInvocationListener> methodInvocationListeners) {
        try {
            return IMPLEMENTATION_METHOD_EXECUTION_INTERCEPTOR.newInstance(
                    information,
                    composition,
                    methodInvocationListeners
            );
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private Advice queryExecutorMethodInterceptor(RepositoryInformation repositoryInformation,
                                                  ProjectionFactory projectionFactory, Optional<QueryLookupStrategy> queryLookupStrategy, NamedQueries namedQueries,
                                                  List<QueryCreationListener<?>> queryPostProcessors,
                                                  List<RepositoryMethodInvocationListener> methodInvocationListeners) {
        try {
            return QUERY_EXECUTOR_METHOD_INTERCEPTOR.newInstance(
                    repositoryInformation,
                    projectionFactory,
                    queryLookupStrategy,
                    namedQueries,
                    queryPostProcessors,
                    methodInvocationListeners
            );
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Validates the given repository interface as well as the given custom implementation.
     *
     * @param repositoryInformation
     * @param composition
     */
    private void validate(RepositoryInformation repositoryInformation, RepositoryComposition composition) {

        if (repositoryInformation.hasCustomMethod()) {

            if (composition.isEmpty()) {

                throw new IllegalArgumentException(
                        String.format("You have custom methods in %s but not provided a custom implementation!",
                                repositoryInformation.getRepositoryInterface()));
            }

            composition.validateImplementation();
        }

        validate(repositoryInformation);
    }

    private RepositoryInformation getRepositoryInformation(RepositoryMetadata metadata,
                                                           RepositoryComposition composition) {

        RepositoryInformationCacheKey cacheKey = new RepositoryInformationCacheKey(metadata, composition);

        return repositoryInformationCache.computeIfAbsent(cacheKey, key -> {

            Class<?> baseClass = repositoryBaseClass.orElse(getRepositoryBaseClass(metadata));

            return getRepositoryInformation(metadata, new DefaultRepositoryInformation(metadata, baseClass, composition));
        });
    }

    private RepositoryComposition getRepositoryComposition(RepositoryMetadata metadata, RepositoryComposition.RepositoryFragments fragments) {

        Assert.notNull(metadata, "RepositoryMetadata must not be null!");
        Assert.notNull(fragments, "RepositoryFragments must not be null!");

        RepositoryComposition composition = getRepositoryComposition(metadata);
        RepositoryComposition.RepositoryFragments repositoryAspects = getRepositoryFragments(metadata);

        return composition.append(fragments).append(repositoryAspects);
    }

    /**
     * Creates {@link RepositoryComposition} based on {@link RepositoryMetadata} for repository-specific method handling.
     *
     * @param metadata
     * @return
     */
    private RepositoryComposition getRepositoryComposition(RepositoryMetadata metadata) {

        RepositoryComposition composition = RepositoryComposition.empty();

        if (metadata.isReactiveRepository()) {
            return composition.withMethodLookup(MethodLookups.forReactiveTypes(metadata))
                    .withArgumentConverter(REACTIVE_ARGS_CONVERTER);
        }

        return composition.withMethodLookup(MethodLookups.forRepositoryTypes(metadata));
    }

    private static class RepositoryInformationCacheKey {

        String repositoryInterfaceName;
        final long compositionHash;

        /**
         * Creates a new {@link RepositoryInformationCacheKey} for the given {@link RepositoryMetadata} and composition.
         *
         * @param metadata must not be {@literal null}.
         * @param composition must not be {@literal null}.
         */
        public RepositoryInformationCacheKey(RepositoryMetadata metadata, RepositoryComposition composition) {

            this.repositoryInterfaceName = metadata.getRepositoryInterface().getName();
            this.compositionHash = composition.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof RepositoryInformationCacheKey)) {
                return false;
            }

            RepositoryInformationCacheKey that = (RepositoryInformationCacheKey) o;

            if (compositionHash != that.compositionHash) {
                return false;
            }
            return repositoryInterfaceName != null ? repositoryInterfaceName.equals(that.repositoryInterfaceName) : that.repositoryInterfaceName == null;
        }

        @Override
        public int hashCode() {
            int result = repositoryInterfaceName != null ? repositoryInterfaceName.hashCode() : 0;
            result = 31 * result + (int) (compositionHash ^ (compositionHash >>> 32));
            return result;
        }
    }

}
