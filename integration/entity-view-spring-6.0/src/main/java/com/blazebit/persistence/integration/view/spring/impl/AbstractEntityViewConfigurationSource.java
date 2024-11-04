/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.view.spring.impl;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.filter.TypeFilter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public abstract class AbstractEntityViewConfigurationSource {

    private final Environment environment;

    public AbstractEntityViewConfigurationSource(Environment environment) {
        this.environment = environment;
    }

    public abstract Iterable<String> getBasePackages();

    protected abstract Iterable<TypeFilter> getExcludeFilters();

    protected abstract Iterable<TypeFilter> getIncludeFilters();

    public Collection<BeanDefinition> getCandidates(ResourceLoader resourceLoader) {
        EntityViewComponentProvider scanner = new EntityViewComponentProvider(getIncludeFilters());
//        scanner.setConsiderNestedRepositoryInterfaces(shouldConsiderNestedRepositories());
        scanner.setResourceLoader(resourceLoader);
        scanner.setEnvironment(environment);

        for (TypeFilter filter : getExcludeFilters()) {
            scanner.addExcludeFilter(filter);
        }

        Set<BeanDefinition> result = new HashSet<BeanDefinition>();

        for (String basePackage : getBasePackages()) {
            Set<BeanDefinition> candidate = scanner.findCandidateComponents(basePackage);
            result.addAll(candidate);
        }

        return result;
    }
}
