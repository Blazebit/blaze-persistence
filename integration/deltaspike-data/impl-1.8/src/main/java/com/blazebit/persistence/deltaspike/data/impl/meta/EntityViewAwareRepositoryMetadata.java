/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.deltaspike.data.impl.meta;

import com.blazebit.persistence.deltaspike.data.EntityViewManagerResolver;
import org.apache.deltaspike.data.impl.meta.EntityMetadata;
import org.apache.deltaspike.data.impl.meta.RepositoryMetadata;

/**
 * Implementation is similar to {@link RepositoryMetadata} but was modified to
 * work with entity views.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class EntityViewAwareRepositoryMetadata extends RepositoryMetadata {

    private Class<? extends EntityViewManagerResolver> entityViewManagerResolver;

    public EntityViewAwareRepositoryMetadata(Class<?> repositoryClass) {
        super(repositoryClass);
    }

    public EntityViewAwareRepositoryMetadata(Class<?> repositoryClass, EntityMetadata entityMetadata) {
        super(repositoryClass, entityMetadata);
    }

    public boolean hasEntityViewManagerResolver() {
        return getEntityViewManagerResolverClass() != null;
    }

    public Class<? extends EntityViewManagerResolver> getEntityViewManagerResolverClass() {
        return entityViewManagerResolver;
    }

    public void setEntityViewManagerResolver(Class<? extends EntityViewManagerResolver> entityViewManagerResolver) {
        this.entityViewManagerResolver = entityViewManagerResolver;
    }
}