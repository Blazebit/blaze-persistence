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