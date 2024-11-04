/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.metamodel;

import com.blazebit.persistence.view.impl.EntityViewListenerClassKey;
import com.blazebit.persistence.view.impl.EntityViewListenerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface MetamodelBootContext {

    public ViewMapping getViewMapping(Class<?> clazz);

    public void addViewMapping(Class<?> clazz, ViewMapping viewMapping);

    public Map<Class<?>, ViewMapping> getViewMappingMap();

    public Collection<ViewMapping> getViewMappings();

    public Set<Class<?>> getViewClasses();

    public void addEntityViewListener(Class<?> entityViewClass, Class<?> entityViewListenerClass);

    public void addEntityViewListener(Class<?> entityViewClass, Class<?> entityClass, Class<?> entityViewListenerClass);

    public void addEntityViewListener(Class<?> entityViewClass, Class<?> entityClass, EntityViewListenerFactory<?> entityViewListenerFactory);

    public EntityViewListenerFactory<?>[] createViewListenerFactories(Class<?> entityViewListenerClass);

    public Map<EntityViewListenerClassKey, EntityViewListenerFactory<?>> getViewListeners();

    public void addError(String error);

    public boolean hasErrors();

    public Set<String> getErrors();

    public Set<Class<?>> getViewListenerClasses();

    public Set<Class<?>> getViewListenerClasses(Class<?> entityViewClass);

    public Set<Class<?>> getViewListenerClasses(Class<?> entityViewClass, Class<?> entityClass);
}
