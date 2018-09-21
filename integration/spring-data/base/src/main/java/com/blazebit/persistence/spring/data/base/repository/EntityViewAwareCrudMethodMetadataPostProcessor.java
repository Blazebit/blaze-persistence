/*
 * Copyright 2014 - 2018 Blazebit.
 * Copyright 2011-2016 the original author or authors.
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

package com.blazebit.persistence.spring.data.base.repository;

import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.reflection.ReflectionUtils;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.ProxyMethodInvocation;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.interceptor.ExposeInvocationInterceptor;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.jpa.repository.support.CrudMethodMetadata;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.support.RepositoryProxyPostProcessor;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Adapted {@link org.springframework.data.jpa.repository.support.CrudMethodMetadataPostProcessor} to be able to use the {@link EntityViewManager} so that we can safely determine if a class is an entity view.
 *
 * @author Oliver Gierke
 * @author Thomas Darimont
 * @author Christoph Strobl
 * @author Christian Beikov
 * @since 1.2.0
 */
public class EntityViewAwareCrudMethodMetadataPostProcessor implements RepositoryProxyPostProcessor, BeanClassLoaderAware {

    private static final ConcurrentMap<EntityViewManager, MethodInterceptor> INTERCEPTOR_CACHE = new ConcurrentHashMap<>();
    private final MethodInterceptor interceptor;
    private ClassLoader classLoader = ClassUtils.getDefaultClassLoader();

    public EntityViewAwareCrudMethodMetadataPostProcessor(EntityViewManager evm) {
        MethodInterceptor methodInterceptor = INTERCEPTOR_CACHE.get(evm);
        if (methodInterceptor == null) {
            methodInterceptor = new CrudMethodMetadataPopulatingMethodInterceptor(evm);
            MethodInterceptor old = INTERCEPTOR_CACHE.putIfAbsent(evm, methodInterceptor);
            if (old != null) {
                methodInterceptor = old;
            }
        }
        this.interceptor = methodInterceptor;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.BeanClassLoaderAware#setBeanClassLoader(java.lang.ClassLoader)
     */
    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader == null ? ClassUtils.getDefaultClassLoader() : classLoader;

    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.core.support.RepositoryProxyPostProcessor#postProcess(org.springframework.aop.framework.ProxyFactory, org.springframework.data.repository.core.RepositoryInformation)
     */
    @Override
    public void postProcess(ProxyFactory factory, RepositoryInformation repositoryInformation) {
        factory.addAdvice(interceptor);
    }

    /**
     * Returns a {@link CrudMethodMetadata} proxy that will lookup the actual target object by obtaining a thread bound
     * instance from the {@link TransactionSynchronizationManager} later.
     */
    public EntityViewAwareCrudMethodMetadata getCrudMethodMetadata() {

        ProxyFactory factory = new ProxyFactory();

        factory.addInterface(EntityViewAwareCrudMethodMetadata.class);
        factory.setTargetSource(new ThreadBoundTargetSource());

        return (EntityViewAwareCrudMethodMetadata) factory.getProxy(this.classLoader);
    }

    /**
     * {@link MethodInterceptor} to build and cache {@link EntityViewAwareDefaultCrudMethodMetadata} instances for the invoked methods.
     * Will bind the found information to a {@link TransactionSynchronizationManager} for later lookup.
     *
     * @author Oliver Gierke
     * @author Thomas Darimont
     * @see EntityViewAwareDefaultCrudMethodMetadata
     */
    private static final class CrudMethodMetadataPopulatingMethodInterceptor implements MethodInterceptor {

        private final ConcurrentMap<EntityViewMetadataCacheKey, EntityViewAwareCrudMethodMetadata> metadataCache = new ConcurrentHashMap<>();
        private final EntityViewManager evm;

        private CrudMethodMetadataPopulatingMethodInterceptor(EntityViewManager evm) {
            this.evm = evm;
        }

        /*
         * (non-Javadoc)
         * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
         */
        public Object invoke(MethodInvocation invocation) throws Throwable {
            Method method = invocation.getMethod();
            EntityViewAwareCrudMethodMetadata metadata = (EntityViewAwareCrudMethodMetadata) TransactionSynchronizationManager.getResource(method);

            if (metadata != null) {
                return invocation.proceed();
            }

            EntityViewMetadataCacheKey cacheKey = new EntityViewMetadataCacheKey(method, ((ProxyMethodInvocation) invocation).getProxy().getClass());
            EntityViewAwareCrudMethodMetadata methodMetadata = metadataCache.get(cacheKey);

            if (methodMetadata == null) {
                methodMetadata = new EntityViewAwareDefaultCrudMethodMetadata(cacheKey.proxyClass, method, evm);
                EntityViewAwareCrudMethodMetadata tmp = metadataCache.putIfAbsent(cacheKey, methodMetadata);

                if (tmp != null) {
                    methodMetadata = tmp;
                }
            }

            TransactionSynchronizationManager.bindResource(method, methodMetadata);

            try {
                return invocation.proceed();
            } finally {
                TransactionSynchronizationManager.unbindResource(method);
            }
        }
    }

    /**
     * Cache key for entity view metadata.
     *
     * @author Christian Beikov
     * @since 1.3.0
     */
    private static class EntityViewMetadataCacheKey {
        private final Method method;
        private final Class<?> proxyClass;

        public EntityViewMetadataCacheKey(Method method, Class<?> proxyClass) {
            this.method = method;
            this.proxyClass = proxyClass;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof EntityViewMetadataCacheKey)) {
                return false;
            }

            EntityViewMetadataCacheKey that = (EntityViewMetadataCacheKey) o;
            return method.equals(that.method) && proxyClass.equals(that.proxyClass);
        }

        @Override
        public int hashCode() {
            int result = method.hashCode();
            result = 31 * result + proxyClass.hashCode();
            return result;
        }
    }

    /**
     * Default implementation of {@link CrudMethodMetadata} that will inspect the backing method for annotations.
     *
     * @author Oliver Gierke
     * @author Thomas Darimont
     */
    private static class EntityViewAwareDefaultCrudMethodMetadata implements EntityViewAwareCrudMethodMetadata {

        private final LockModeType lockModeType;
        private final Map<String, Object> queryHints;
        private final EntityGraph entityGraph;
        private final Class<?> entityViewClass;
        private final Method method;

        /**
         * Creates a new {@link EntityViewAwareDefaultCrudMethodMetadata} for the given {@link Method}.
         *  @param repositoryClass The repository class
         * @param method must not be {@literal null}.
         * @param evm the {@link EntityViewManager}
         */
        public EntityViewAwareDefaultCrudMethodMetadata(Class<?> repositoryClass, Method method, EntityViewManager evm) {
            Assert.notNull(method, "Method must not be null!");

            Method annotationTargetMethod = findAnnotationTargetMethod(method);
            this.lockModeType = findLockModeType(annotationTargetMethod);
            this.queryHints = findQueryHints(annotationTargetMethod);
            this.entityGraph = findEntityGraph(annotationTargetMethod);
            this.entityViewClass = findEntityViewClass(repositoryClass, method, evm);
            this.method = method;
        }

        private static Method findAnnotationTargetMethod(Method method) {
            if (method.isBridge() || method.isSynthetic()) {
                try {
                    return method.getDeclaringClass().getMethod(method.getName(), method.getParameterTypes());
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }

            return method;
        }

        private static Class<?> findEntityViewClass(Class<?> repositoryClass, Method methodToAnalyze, EntityViewManager evm) {
            Class<?> entityViewClass;
            Class<?>[] typeArguments = ReflectionUtils.getResolvedMethodReturnTypeArguments(repositoryClass, methodToAnalyze);
            if (typeArguments.length == 0) {
                entityViewClass = ReflectionUtils.getResolvedMethodReturnType(repositoryClass, methodToAnalyze);
            } else {
                entityViewClass = typeArguments[typeArguments.length - 1];
            }
            ManagedViewType<?> managedViewType = evm.getMetamodel().managedView(entityViewClass);
            if (managedViewType == null) {
                return null;
            }
            return managedViewType.getJavaType();
        }

        private static EntityGraph findEntityGraph(Method method) {
            return AnnotatedElementUtils.findMergedAnnotation(method, EntityGraph.class);
        }

        private static LockModeType findLockModeType(Method method) {

            Lock annotation = AnnotatedElementUtils.findMergedAnnotation(method, Lock.class);
            return annotation == null ? null : (LockModeType) AnnotationUtils.getValue(annotation);
        }

        private static Map<String, Object> findQueryHints(Method method) {

            Map<String, Object> queryHints = new HashMap<String, Object>();
            QueryHints queryHintsAnnotation = AnnotatedElementUtils.findMergedAnnotation(method, QueryHints.class);

            if (queryHintsAnnotation != null) {

                for (QueryHint hint : queryHintsAnnotation.value()) {
                    queryHints.put(hint.name(), hint.value());
                }
            }

            QueryHint queryHintAnnotation = AnnotationUtils.findAnnotation(method, QueryHint.class);

            if (queryHintAnnotation != null) {
                queryHints.put(queryHintAnnotation.name(), queryHintAnnotation.value());
            }

            return Collections.unmodifiableMap(queryHints);
        }

        /*
         * (non-Javadoc)
         * @see org.springframework.data.jpa.repository.support.CrudMethodMetadata#getLockModeType()
         */
        @Override
        public LockModeType getLockModeType() {
            return lockModeType;
        }

        /*
         * (non-Javadoc)
         * @see org.springframework.data.jpa.repository.support.CrudMethodMetadata#getQueryHints()
         */
        @Override
        public Map<String, Object> getQueryHints() {
            return queryHints;
        }

        /*
         * (non-Javadoc)
         * @see org.springframework.data.jpa.repository.support.CrudMethodMetadata#getEntityGraph()
         */
        @Override
        public EntityGraph getEntityGraph() {
            return entityGraph;
        }

        /*
         * (non-Javadoc)
         * @see com.blazebit.persistence.spring.data.impl.repository.EntityViewAwareCrudMethodMetadata#getEntityViewClass()
         */
        @Override
        public Class<?> getEntityViewClass() {
            return entityViewClass;
        }

        /*
         * (non-Javadoc)
         * @see org.springframework.data.jpa.repository.support.CrudMethodMetadata#getMethod()
         */
        @Override
        public Method getMethod() {
            return method;
        }
    }

    /**
     * @author Oliver Gierke
     * @author Thomas Darimont
     * @author Christoph Strobl
     * @author Christian Beikov
     * @since 1.2.0
     */
    private static class ThreadBoundTargetSource implements TargetSource {

        /*
         * (non-Javadoc)
         * @see org.springframework.aop.TargetSource#getTargetClass()
         */
        @Override
        public Class<?> getTargetClass() {
            return EntityViewAwareCrudMethodMetadata.class;
        }

        /*
         * (non-Javadoc)
         * @see org.springframework.aop.TargetSource#isStatic()
         */
        @Override
        public boolean isStatic() {
            return false;
        }

        /*
         * (non-Javadoc)
         * @see org.springframework.aop.TargetSource#getTarget()
         */
        @Override
        public Object getTarget() throws Exception {

            MethodInvocation invocation = ExposeInvocationInterceptor.currentInvocation();
            return TransactionSynchronizationManager.getResource(invocation.getMethod());
        }

        /*
         * (non-Javadoc)
         * @see org.springframework.aop.TargetSource#releaseTarget(java.lang.Object)
         */
        @Override
        public void releaseTarget(Object target) throws Exception {
        }
    }
}
