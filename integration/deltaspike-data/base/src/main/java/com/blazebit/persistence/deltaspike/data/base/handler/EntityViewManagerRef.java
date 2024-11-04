/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.deltaspike.data.base.handler;

import com.blazebit.persistence.deltaspike.data.EntityViewManagerResolver;
import com.blazebit.persistence.view.EntityViewManager;
import org.apache.deltaspike.core.api.provider.DependentProvider;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public class EntityViewManagerRef {
    private EntityViewManager entityViewManager;
    private DependentProvider<? extends EntityViewManager> entityViewManagerDependentProvider;

    private Class<? extends EntityViewManagerResolver> entityViewManagerResolverClass;
    private EntityViewManagerResolver entityViewManagerResolver;
    private DependentProvider<? extends EntityViewManagerResolver> entityViewManagerResolverDependentProvider;

    public void release() {
        if (entityViewManagerDependentProvider != null) {
            entityViewManagerDependentProvider.destroy();
        }

        if (entityViewManagerResolverDependentProvider != null) {
            entityViewManagerResolverDependentProvider.destroy();
        }
    }

    public Class<? extends EntityViewManagerResolver> getEntityViewManagerResolverClass() {
        return entityViewManagerResolverClass;
    }

    public void setEntityViewManagerResolverClass(Class<? extends EntityViewManagerResolver> entityViewManagerResolverClass) {
        this.entityViewManagerResolverClass = entityViewManagerResolverClass;
    }

    public DependentProvider<? extends EntityViewManagerResolver> getEntityViewManagerResolverDependentProvider() {
        return entityViewManagerResolverDependentProvider;
    }

    public void setEntityViewManagerResolverDependentProvider(
            DependentProvider<? extends EntityViewManagerResolver> entityViewManagerResolverDependentProvider) {
        this.entityViewManagerResolverDependentProvider = entityViewManagerResolverDependentProvider;
    }

    public EntityViewManager getEntityViewManager() {
        return entityViewManager;
    }

    public void setEntityViewManager(EntityViewManager entityViewManager) {
        this.entityViewManager = entityViewManager;
    }

    public EntityViewManagerResolver getEntityViewManagerResolver() {
        return entityViewManagerResolver;
    }

    public void setEntityViewManagerResolver(EntityViewManagerResolver entityViewManagerResolver) {
        this.entityViewManagerResolver = entityViewManagerResolver;
    }

    public DependentProvider<? extends EntityViewManager> getEntityViewManagerDependentProvider() {
        return entityViewManagerDependentProvider;
    }

    public void setEntityViewManagerDependentProvider(
            DependentProvider<? extends EntityViewManager> entityViewManagerDependentProvider) {
        this.entityViewManagerDependentProvider = entityViewManagerDependentProvider;
    }
}