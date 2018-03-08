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

package com.blazebit.persistence.spring.data.base.query;

import org.springframework.data.jpa.provider.QueryExtractor;
import org.springframework.data.jpa.repository.query.JpaQueryMethod;
import org.springframework.data.projection.ProjectionFactory;

import java.lang.reflect.Method;

/**
 * @author Moritz Becker
 * @author Christian Beikov
 * @since 1.2.0
 */
public class EntityViewAwareJpaQueryMethod extends JpaQueryMethod {

    private final Class<?> entityViewClass;
    private final JpaParameters parameters;

    /**
     * Creates a {@link JpaQueryMethod}.
     *
     * @param method must not be {@literal null}
     * @param extractor must not be {@literal null}
     * @param metadata must not be {@literal null}
     */
    public EntityViewAwareJpaQueryMethod(Method method, EntityViewAwareRepositoryMetadata metadata, ProjectionFactory factory,
                          QueryExtractor extractor) {

        super(method, metadata, factory, extractor);
        this.entityViewClass = metadata.getReturnedEntityViewClass(method);
        this.parameters = new JpaParameters(method);
    }

    public boolean isEntityViewQuery() {
        return entityViewClass != null;
    }

    public Class<?> getEntityViewClass() {
        return entityViewClass;
    }

    public JpaParameters getJpaParameters() {
        return parameters;
    }
}
