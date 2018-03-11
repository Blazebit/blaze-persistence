/*
 * Copyright 2014 - 2018 Blazebit.
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