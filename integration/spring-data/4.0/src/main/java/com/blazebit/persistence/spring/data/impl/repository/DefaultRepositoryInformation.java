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

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jspecify.annotations.Nullable;

import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryInformationSupport;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryComposition;
import org.springframework.data.repository.core.support.RepositoryFragment;
import org.springframework.data.util.Lazy;
import org.springframework.lang.Contract;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * Default implementation of {@link RepositoryInformation}.
 *
 * @author Oliver Gierke
 * @author Thomas Darimont
 * @author Mark Paluch
 * @author Christoph Strobl
 * @author Alessandro Nistico
 */
class DefaultRepositoryInformation extends RepositoryInformationSupport implements RepositoryInformation {

    private final Map<Method, Method> methodCache = new ConcurrentHashMap<>();

    private final RepositoryComposition composition;
    private final RepositoryComposition baseComposition;
    private final Lazy<RepositoryComposition> fullComposition;

    /**
     * Creates a new {@link org.springframework.data.repository.core.support.DefaultRepositoryMetadata} for the given repository interface and repository base class.
     *
     * @param metadata must not be {@literal null}.
     * @param repositoryBaseClass must not be {@literal null}.
     * @param composition must not be {@literal null}.
     */
    public DefaultRepositoryInformation(RepositoryMetadata metadata, Class<?> repositoryBaseClass,
            RepositoryComposition composition) {

        super(() -> metadata, () -> repositoryBaseClass);
        Assert.notNull(composition, "Repository composition must not be null");

        this.composition = composition;
        this.baseComposition = RepositoryComposition.of(RepositoryFragment.structural(repositoryBaseClass)) //
                .withArgumentConverter(composition.getArgumentConverter()) //
                .withMethodLookup(composition.getMethodLookup());

        this.fullComposition = Lazy.of(() -> composition.append(baseComposition.getFragments()));
    }

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

    @Contract("_, null -> null; _, !null -> !null")
    private @Nullable Method cacheAndReturn(Method key, @Nullable Method value) {

        if (value != null) {
            ReflectionUtils.makeAccessible(value);
        }

        methodCache.put(key, value);
        return value;
    }

    @Override
    public boolean isCustomMethod(Method method) {
        return composition.findMethod(method).isPresent();
    }

    @Override
    public boolean isBaseClassMethod(Method method) {

        Assert.notNull(method, "Method must not be null");
        return baseComposition.findMethod(method).isPresent();
    }

    @Override
    protected boolean isQueryMethodCandidate(Method method) {

        boolean queryMethodCandidate = super.isQueryMethodCandidate(method);
        if(!isQueryAnnotationPresentOn(method)) {
            return queryMethodCandidate;
        }

        if (!queryMethodCandidate) {
            return false;
        }

        for (RepositoryFragment<?> fragment : getFragments()) {

            if (fragment.getImplementation().isPresent() && fragment.hasMethod(method)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Set<RepositoryFragment<?>> getFragments() {
        return composition.getFragments().toSet();
    }

    @Override
    public RepositoryComposition getRepositoryComposition() {
        return fullComposition.get();
    }

}
