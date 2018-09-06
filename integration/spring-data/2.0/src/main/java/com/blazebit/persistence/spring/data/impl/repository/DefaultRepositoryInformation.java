/*
 * Copyright 2011-2017 the original author or authors.
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

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.annotation.QueryAnnotation;
import org.springframework.data.repository.core.CrudMethods;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.DefaultRepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryComposition;
import org.springframework.data.repository.core.support.RepositoryFragment;
import org.springframework.data.util.Streamable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.data.repository.util.ClassUtils.isGenericRepositoryInterface;
import static org.springframework.util.ReflectionUtils.makeAccessible;

/**
 * Default implementation of {@link RepositoryInformation}.
 *
 * @author Oliver Gierke
 * @author Thomas Darimont
 * @author Mark Paluch
 * @author Christoph Strobl
 */
class DefaultRepositoryInformation implements RepositoryInformation {

    private final Map<Method, Method> methodCache = new ConcurrentHashMap<>();

    private final RepositoryMetadata metadata;
    private final Class<?> repositoryBaseClass;
    private final RepositoryComposition composition;
    private final RepositoryComposition baseComposition;

    /**
     * Creates a new {@link DefaultRepositoryMetadata} for the given repository interface and repository base class.
     *
     * @param metadata must not be {@literal null}.
     * @param repositoryBaseClass must not be {@literal null}.
     * @param composition must not be {@literal null}.
     */
    public DefaultRepositoryInformation(RepositoryMetadata metadata, Class<?> repositoryBaseClass,
                                        RepositoryComposition composition) {

        Assert.notNull(metadata, "Repository metadata must not be null!");
        Assert.notNull(repositoryBaseClass, "Repository base class must not be null!");
        Assert.notNull(composition, "Repository composition must not be null!");

        this.metadata = metadata;
        this.repositoryBaseClass = repositoryBaseClass;
        this.composition = composition;
        this.baseComposition = RepositoryComposition.of(RepositoryFragment.structural(repositoryBaseClass)) //
                .withArgumentConverter(composition.getArgumentConverter()) //
                .withMethodLookup(composition.getMethodLookup());
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.support.RepositoryMetadata#getDomainClass()
     */
    @Override
    public Class<?> getDomainType() {
        return metadata.getDomainType();
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.support.RepositoryMetadata#getIdClass()
     */
    @Override
    public Class<?> getIdType() {
        return metadata.getIdType();
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.support.RepositoryInformation#getRepositoryBaseClass()
     */
    @Override
    public Class<?> getRepositoryBaseClass() {
        return this.repositoryBaseClass;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.support.RepositoryInformation#getTargetClassMethod(java.lang.reflect.Method)
     */
    @Override
    public Method getTargetClassMethod(Method method) {

        if (methodCache.containsKey(method)) {
            return methodCache.get(method);
        }

        Method result = composition.findMethod(method).orElse(method);

        if (!result.equals(method)) {
            return cacheAndReturn(method, result);
        }

        return cacheAndReturn(method, baseComposition.findMethod(method).orElse(method));
    }

    private Method cacheAndReturn(Method key, Method value) {

        if (value != null) {
            makeAccessible(value);
        }

        methodCache.put(key, value);
        return value;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.support.RepositoryInformation#getQueryMethods()
     */
    @Override
    public Streamable<Method> getQueryMethods() {

        Set<Method> result = new HashSet<>();

        for (Method method : getRepositoryInterface().getMethods()) {
            method = ClassUtils.getMostSpecificMethod(method, getRepositoryInterface());
            if (isQueryMethodCandidate(method)) {
                result.add(method);
            }
        }

        return Streamable.of(Collections.unmodifiableSet(result));
    }

    /**
     * Checks whether the given method is a query method candidate.
     *
     * @param method
     * @return
     */
    private boolean isQueryMethodCandidate(Method method) {
        return !method.isBridge() && !method.isDefault() //
                && !Modifier.isStatic(method.getModifiers()) //
                && (isQueryAnnotationPresentOn(method) || !isCustomMethod(method) && !isBaseClassMethod(method));
    }

    /**
     * Checks whether the given method contains a custom store specific query annotation annotated with
     * {@link QueryAnnotation}. The method-hierarchy is also considered in the search for the annotation.
     *
     * @param method
     * @return
     */
    private boolean isQueryAnnotationPresentOn(Method method) {

        return AnnotationUtils.findAnnotation(method, QueryAnnotation.class) != null;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.support.RepositoryInformation#isCustomMethod(java.lang.reflect.Method)
     */
    @Override
    public boolean isCustomMethod(Method method) {
        return composition.findMethod(method).isPresent();
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.core.RepositoryInformation#isQueryMethod(java.lang.reflect.Method)
     */
    @Override
    public boolean isQueryMethod(Method method) {
        return getQueryMethods().stream().anyMatch(it -> it.equals(method));
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.core.RepositoryInformation#isBaseClassMethod(java.lang.reflect.Method)
     */
    @Override
    public boolean isBaseClassMethod(Method method) {

        Assert.notNull(method, "Method must not be null!");
        return baseComposition.findMethod(method).isPresent();
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.support.RepositoryInformation#hasCustomMethod()
     */
    @Override
    public boolean hasCustomMethod() {

        Class<?> repositoryInterface = getRepositoryInterface();

        // No detection required if no typing interface was configured
        if (isGenericRepositoryInterface(repositoryInterface)) {
            return false;
        }

        for (Method method : repositoryInterface.getMethods()) {
            if (isCustomMethod(method) && !isBaseClassMethod(method)) {
                return true;
            }
        }

        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.core.RepositoryMetadata#getRepositoryInterface()
     */
    @Override
    public Class<?> getRepositoryInterface() {
        return metadata.getRepositoryInterface();
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.core.RepositoryMetadata#getReturnedDomainClass(java.lang.reflect.Method)
     */
    @Override
    public Class<?> getReturnedDomainClass(Method method) {
        return metadata.getReturnedDomainClass(method);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.core.RepositoryMetadata#getCrudMethods()
     */
    @Override
    public CrudMethods getCrudMethods() {
        return metadata.getCrudMethods();
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.core.RepositoryMetadata#isPagingRepository()
     */
    @Override
    public boolean isPagingRepository() {
        return metadata.isPagingRepository();
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.core.RepositoryMetadata#getAlternativeDomainTypes()
     */
    @Override
    public Set<Class<?>> getAlternativeDomainTypes() {
        return metadata.getAlternativeDomainTypes();
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.core.RepositoryMetadata#isReactiveRepository()
     */
    @Override
    public boolean isReactiveRepository() {
        return metadata.isReactiveRepository();
    }
}
