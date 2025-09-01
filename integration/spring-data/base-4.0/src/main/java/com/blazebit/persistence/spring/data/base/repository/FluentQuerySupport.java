/*
 * Copyright 2021-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.blazebit.persistence.spring.data.base.repository;

import jakarta.persistence.Query;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * Supporting class containing some state and convenience methods for building and executing fluent queries.
 *
 * Christian Beikov: Copied to be able to share code between Spring Data integrations for 2.6 and 2.7.
 *
 * @param <R> The resulting type of the query.
 * @author Greg Turnquist
 * @author Jens Schauder
 * @since 2.6
 */
public abstract class FluentQuerySupport<S, R> {

    protected final Class<R> resultType;
    protected final Sort sort;
    protected final int limit;
    protected final Set<String> properties;
    protected final Class<S> entityType;

    private final SpelAwareProxyProjectionFactory projectionFactory = new SpelAwareProxyProjectionFactory();

    public FluentQuerySupport(Class<R> resultType, Sort sort, int limit, Collection<String> properties, Class<S> entityType) {

        this.resultType = resultType;
        this.sort = sort;
        this.limit = limit;

        if (properties != null) {
            this.properties = new HashSet<>(properties);
        } else {
            this.properties = Collections.emptySet();
        }

        this.entityType = entityType;
    }

    final Collection<String> mergeProperties(Collection<String> additionalProperties) {

        Set<String> newProperties = new HashSet<>();
        newProperties.addAll(properties);
        newProperties.addAll(additionalProperties);
        return Collections.unmodifiableCollection(newProperties);
    }

    @SuppressWarnings("unchecked")
    final Function<Object, R> getConversionFunction(Class<S> inputType, Class<R> targetType) {

        if (targetType.isAssignableFrom(inputType)) {
            return (Function<Object, R>) Function.identity();
        }

        if (targetType.isInterface()) {
            return o -> projectionFactory.createProjection(targetType, o);
        }

        return o -> DefaultConversionService.getSharedInstance().convert(o, targetType);
    }

    interface ScrollQueryFactory {
        Query createQuery(Sort sort, ScrollPosition scrollPosition);
    }
}
