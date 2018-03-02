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