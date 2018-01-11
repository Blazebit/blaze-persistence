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

/**
 * Implementation is similar to {@link org.apache.deltaspike.data.impl.meta.RepositoryEntity} but was modified to
 * work with entity views.
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public class RepositoryEntityView {
    private final Class<?> entityViewClass;
    private final Class<?> entityClass;
    private final String entityName;

    public RepositoryEntityView(Class<?> entityViewClass, Class<?> entityClass) {
        this.entityViewClass = entityViewClass;
        this.entityClass = entityClass;
        this.entityName = entityClass.getSimpleName();
    }

    public Class<?> getEntityViewClass() {
        return entityViewClass;
    }

    public Class<?> getEntityClass() {
        return entityClass;
    }

    public String getEntityName() {
        return entityName;
    }
}