/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
import org.springframework.data.expression.ValueExpressionParser;
import org.springframework.data.jpa.provider.PersistenceProvider;
import org.springframework.data.jpa.provider.QueryExtractor;
import org.springframework.data.jpa.repository.query.AbstractJpaQuery;
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
import org.springframework.data.repository.core.RepositoryMethodContext;
import org.springframework.data.repository.core.RepositoryMethodContextHolder;
import org.springframework.data.repository.core.support.MethodInvocationValidator;
import org.springframework.data.repository.core.support.PropertiesBasedNamedQueries;
import org.springframework.data.repository.core.support.QueryCreationListener;
import org.springframework.data.repository.core.support.RepositoryComposition;
import org.springframework.data.repository.core.support.RepositoryFragment;
import org.springframework.data.repository.core.support.RepositoryMetadataAccess;
import org.springframework.data.repository.core.support.RepositoryMethodInvocationListener;
import org.springframework.data.repository.core.support.RepositoryProxyPostProcessor;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.QueryMethodValueEvaluationContextAccessor;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.ValueExpressionDelegate;
import org.springframework.data.spel.EvaluationContextProvider;
import org.springframework.data.util.NullnessMethodInvocationValidator;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.lang.Nullable;
import org.springframework.transaction.interceptor.TransactionalProxy;
import org.springframework.util.Assert;
import org.springframework.util.ConcurrentReferenceHashMap;

import jakarta.persistence.EntityManager;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * Partly copied from {@link JpaRepositoryFactory} to retain functionality but mostly original.
 *
 * @author Moritz Becker
 * @author Christian Beikov
 * @author Eugen Mayer
 * @since 1.6.9
 */
public class BlazePersistenceRepositoryFactory extends JpaRepositoryFactory {

    private static final Constructor<Advice> IMPLEMENTATION_METHOD_EXECUTION_INTERCEPTOR;
    private static final Constructor<Advice> QUERY_EXECUTOR_METHOD_INTERCEPTOR;
    private static final ExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();
    private static final ValueExpressionParser VALUE_PARSER = ValueExpressionParser.create( () -> EXPRESSION_PARSER);

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
                            QueryLookupStrategy.class,
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
    private final Map<RepositoryInformationCacheKey, RepositoryStub> repositoryInformationCache;
    private final EntityViewReplacingMethodInterceptor entityViewReplacingMethodInterceptor;
    private List<RepositoryProxyPostProcessor> postProcessors;
    private EntityViewAwareCrudMethodMetadataPostProcessor crudMethodMetadataPostProcessor;
    private Class<?> repositoryBaseClass;
    private QueryLookupStrategy.Key queryLookupStrategyKey;
    private List<QueryCreationListener<?>> queryPostProcessors;
    private List<RepositoryMethodInvocationListener> methodInvocationListeners;
    private NamedQueries namedQueries;
    private EscapeCharacter escapeCharacter = EscapeCharacter.DEFAULT;
    private ClassLoader classLoader;
    private EvaluationContextProvider evaluationContextProvider;
    private boolean exposeMetadata;

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
        this.repositoryInformationCache = new HashMap<>(8);
        this.cbf = cbf;
        this.evm = evm;
        this.namedQueries = PropertiesBasedNamedQueries.EMPTY;
        this.evaluationContextProvider = QueryMethodValueEvaluationContextAccessor.DEFAULT_CONTEXT_PROVIDER;
        this.queryPostProcessors = new ArrayList<>();
        this.queryPostProcessors.add(collectingListener);
        this.methodInvocationListeners = new ArrayList<>();
        addRepositoryProxyPostProcessor(this.crudMethodMetadataPostProcessor = new EntityViewAwareCrudMethodMetadataPostProcessor());
        this.entityViewReplacingMethodInterceptor = new EntityViewReplacingMethodInterceptor(entityManager, evm);
    }

    @Override
    public void setQueryLookupStrategyKey(QueryLookupStrategy.Key key) {
        this.queryLookupStrategyKey = key;
    }

    public void setEvaluationContextProvider(@Nullable EvaluationContextProvider evaluationContextProvider) {
        this.evaluationContextProvider = evaluationContextProvider == null ? EvaluationContextProvider.DEFAULT
                : evaluationContextProvider;
    }

    @Override
    public void setNamedQueries(NamedQueries namedQueries) {
        this.namedQueries = namedQueries == null ? PropertiesBasedNamedQueries.EMPTY : namedQueries;
    }

    @Override
    public void addQueryCreationListener(QueryCreationListener<?> listener) {
        Assert.notNull(listener, "Listener must not be null!");
        if (queryPostProcessors != null) {
            this.queryPostProcessors.add(listener);
        }
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

    @Override
    public void setExposeMetadata(boolean exposeMetadata) {
        this.exposeMetadata = exposeMetadata;
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
        return getRepositoryStub(metadata, fragments).information();
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
        if (repositoryBaseClass != EntityViewAwareRepositoryImpl.class) {
            return repositoryBaseClass;
        }
        ExtendedManagedType<?> managedType = cbf.getService(EntityMetamodel.class).getManagedType(ExtendedManagedType.class, metadata.getDomainType());
        // Only use the entity view aware repository if the domain type has a single id attribute
        if (managedType.getIdAttributes().size() == 1) {
            // TODO: at some point, we might want to switch to the default if the repository doesn't contain entity views or keyset pagination
            return EntityViewAwareRepositoryImpl.class;
        }
        return super.getRepositoryBaseClass(metadata);
    }

    @Override
    protected Optional<QueryLookupStrategy> getQueryLookupStrategy(QueryLookupStrategy.Key key, ValueExpressionDelegate valueExpressionDelegate) {
        switch (key != null ? key : QueryLookupStrategy.Key.CREATE_IF_NOT_FOUND) {
            case CREATE:
                return Optional.of(new CreateQueryLookupStrategy(entityManager, extractor, escapeCharacter, cbf, evm));
            case USE_DECLARED_QUERY:
                return Optional.of(new DelegateQueryLookupStrategy(super.getQueryLookupStrategy(key, valueExpressionDelegate).get()));
            case CREATE_IF_NOT_FOUND:
                return Optional.of(new CreateIfNotFoundQueryLookupStrategy(entityManager, extractor, new CreateQueryLookupStrategy(entityManager, extractor, escapeCharacter, cbf, evm),
                        new DelegateQueryLookupStrategy(super.getQueryLookupStrategy(QueryLookupStrategy.Key.USE_DECLARED_QUERY, valueExpressionDelegate).get())));
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
                RepositoryQuery repositoryQuery = lookupStrategy.resolveQuery(method, metadata, factory, namedQueries);
                // Only return something if the RepositoryQuery is not an instance of org.springframework.data.jpa.repository.query.JpaQueryLookupStrategy.NoQuery
                // Since we can't refer to the class though, we instead check if the returned class is an instance of AbstractJpaQuery,
                // because we know that NoQuery is not matching that
                if (repositoryQuery instanceof AbstractJpaQuery) {
                    return repositoryQuery;
                }
            } catch (IllegalStateException e) {
                // Ignore
            }
            return createStrategy.resolveQuery(method, metadata, factory, namedQueries);
        }
    }

    // Mostly copied from RepositoryFactorySupport to be able to use a custom RepositoryInformation implementation

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        super.setBeanClassLoader(classLoader);
        this.classLoader = classLoader;
    }

    @Override
    public void setRepositoryBaseClass(Class<?> repositoryBaseClass) {
        super.setRepositoryBaseClass(repositoryBaseClass);
        this.repositoryBaseClass = repositoryBaseClass;
    }

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
        RepositoryStub stub = getRepositoryStub(metadata, fragments);
        RepositoryComposition composition = stub.composition();
        RepositoryInformation information = stub.information();

        Object target = getTargetRepository(information);

        RepositoryComposition compositionToUse = composition.append(RepositoryFragment.implemented(target));
        validate(information, compositionToUse);

        // Create proxy
        ProxyFactory result = new ProxyFactory();
        result.setTarget(target);
        result.setInterfaces(repositoryInterface, Repository.class, TransactionalProxy.class);

        if (NullnessMethodInvocationValidator.supports(repositoryInterface)) {
            result.addAdvice(new MethodInvocationValidator());
        }

        if (this.exposeMetadata || shouldExposeMetadata(fragments)) {
            result.addAdvice(new ExposeMetadataInterceptor(metadata));
        }
        // Always need this interceptor to access EVM
        result.addAdvisor(ExposeInvocationInterceptor.ADVISOR);

        postProcessors.forEach(processor -> processor.postProcess(result, information));

        result.addAdvice(entityViewReplacingMethodInterceptor);
        if (DefaultMethodInvokingMethodInterceptor.hasDefaultMethods(repositoryInterface)) {
            result.addAdvice(new DefaultMethodInvokingMethodInterceptor());
        }

        Optional<QueryLookupStrategy> queryLookupStrategy = getQueryLookupStrategy(queryLookupStrategyKey, new ValueExpressionDelegate(new QueryMethodValueEvaluationContextAccessor(getEnvironment(), evaluationContextProvider), VALUE_PARSER));
        result.addAdvice(queryExecutorMethodInterceptor(information, getProjectionFactory(), queryLookupStrategy.orElse(null), namedQueries, queryPostProcessors, methodInvocationListeners));

        result.addAdvice(implementationMethodExecutionInterceptor(information, compositionToUse, methodInvocationListeners));

        return (T) result.getProxy(classLoader);
    }

    /**
     * Checks if at least one {@link RepositoryFragment} indicates need to access to {@link RepositoryMetadata} by being
     * flagged with {@link org.springframework.data.repository.core.support.RepositoryMetadataAccess}.
     *
     * @param fragments
     * @return {@literal true} if access to metadata is required.
     */
    private static boolean shouldExposeMetadata(RepositoryComposition.RepositoryFragments fragments) {
        for (RepositoryFragment<?> fragment : fragments) {
            if (fragment.getImplementation().filter(RepositoryMetadataAccess.class::isInstance).isPresent()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Interceptor for repository proxies when the repository needs exposing metadata.
     */
    private static class ExposeMetadataInterceptor implements MethodInterceptor, Serializable {

        private final RepositoryMetadata repositoryMetadata;

        public ExposeMetadataInterceptor(RepositoryMetadata repositoryMetadata) {
            this.repositoryMetadata = repositoryMetadata;
        }

        @Override
        public @Nullable Object invoke(MethodInvocation invocation) throws Throwable {

            RepositoryMethodContext oldMetadata = null;

            try {

                oldMetadata = RepositoryMethodContextHolder
                        .setContext(new DefaultRepositoryMethodContext( repositoryMetadata, invocation.getMethod()));

                return invocation.proceed();

            } finally {
                RepositoryMethodContextHolder.setContext(oldMetadata);
            }
        }
    }

    public static class DefaultRepositoryMethodContext implements RepositoryMethodContext {

        private final RepositoryMetadata repositoryMetadata;
        private final Method method;

        DefaultRepositoryMethodContext(RepositoryMetadata repositoryMetadata, Method method) {

            this.repositoryMetadata = repositoryMetadata;
            this.method = method;
        }

        @Override
        public RepositoryMetadata getMetadata() {
            return repositoryMetadata;
        }

        @Override
        public Method getMethod() {
            return method;
        }
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
                                                  ProjectionFactory projectionFactory, QueryLookupStrategy queryLookupStrategy, NamedQueries namedQueries,
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

    /**
     * Returns the cached {@link RepositoryStub} for the given repository and composition. {@link RepositoryMetadata} is a
     * strong cache key while {@link org.springframework.data.repository.core.support.RepositoryComposition.RepositoryFragments} contributes a light-weight caching component by using only the
     * fragments hash code. In a typical Spring scenario, that shouldn't impose issues as one repository factory produces
     * only a single repository instance for one repository interface. Things might be different when using various
     * fragments for the same repository interface.
     *
     * @param metadata
     * @param fragments
     * @return
     */
    private RepositoryStub getRepositoryStub(RepositoryMetadata metadata, RepositoryComposition.RepositoryFragments fragments) {

        RepositoryInformationCacheKey cacheKey = new RepositoryInformationCacheKey(metadata, fragments);

        synchronized (repositoryInformationCache) {

            return repositoryInformationCache.computeIfAbsent(cacheKey, key -> {

                RepositoryComposition composition = RepositoryComposition.fromMetadata(metadata);
                RepositoryComposition.RepositoryFragments repositoryAspects = getRepositoryFragments( metadata);

                composition = composition.append(fragments).append(repositoryAspects);

                Class<?> baseClass = getRepositoryBaseClass(metadata);

                return new RepositoryStub(new EntityViewAwareRepositoryInformation((EntityViewAwareRepositoryMetadata) metadata, new DefaultRepositoryInformation(metadata, baseClass, composition)), composition);
            });
        }
    }

    record RepositoryStub(RepositoryInformation information, RepositoryComposition composition) {

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
        public RepositoryInformationCacheKey(RepositoryMetadata metadata, RepositoryComposition.RepositoryFragments composition) {

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
