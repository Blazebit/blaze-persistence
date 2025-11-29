/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.view.cdi;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionPoint;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class CustomBean<T> implements Bean<T> {
    
    private final Class<?> beanClass;
    private final Set<Type> types;
    private final Set<Annotation> qualifiers;
    private final Class<? extends Annotation> scope;
    private final T instance;

    public CustomBean(Class<?> beanClass, Class<?>[] types, Annotation[] qualifiers, Class<? extends Annotation> scope, T instance) {
        this.beanClass = beanClass;
        this.types = Collections.unmodifiableSet(new HashSet<Type>(Arrays.asList(types)));
        this.qualifiers = Collections.unmodifiableSet(new HashSet<Annotation>(Arrays.asList(qualifiers)));
        this.scope = scope;
        this.instance = instance;
    }

    @Override
    public T create(CreationalContext<T> cc) {
        return instance;
    }

    @Override
    public void destroy(T t, CreationalContext<T> cc) {
        // No op
    }

    @Override
    public Set<Type> getTypes() {
        return types;
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return qualifiers;
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return scope;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return Collections.emptySet();
    }

    @Override
    public Class<?> getBeanClass() {
        return beanClass;
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes() {
        return Collections.emptySet();
    }

    @Override
    public boolean isAlternative() {
        return false;
    }
}
