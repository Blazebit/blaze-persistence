/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.deltaspike.data.impl.meta;

import com.blazebit.persistence.deltaspike.data.impl.builder.part.EntityViewQueryRoot;
import org.apache.deltaspike.data.impl.meta.RepositoryMethodMetadata;

import java.lang.reflect.Method;

/**
 * Implementation is similar to {@link org.apache.deltaspike.data.impl.meta.RepositoryMethodMetadata} but was modified to
 * work with entity views.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class EntityViewAwareRepositoryMethodMetadata extends RepositoryMethodMetadata {
    private EntityViewQueryRoot entityViewQueryRoot;
    private Class<?> entityViewClass;

    public EntityViewAwareRepositoryMethodMetadata() {
    }

    public EntityViewAwareRepositoryMethodMetadata(Method method) {
        super(method);
    }

    public EntityViewQueryRoot getEntityViewQueryRoot() {
        return entityViewQueryRoot;
    }

    public void setEntityViewQueryRoot(EntityViewQueryRoot entityViewQueryRoot) {
        this.entityViewQueryRoot = entityViewQueryRoot;
    }

    public Class<?> getEntityViewClass() {
        return entityViewClass;
    }

    public void setEntityViewClass(Class<?> entityViewClass) {
        this.entityViewClass = entityViewClass;
    }
}