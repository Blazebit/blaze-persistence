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

package com.blazebit.persistence.spring.data.impl.query;

import com.blazebit.persistence.view.EntityView;
import org.springframework.data.jpa.provider.QueryExtractor;
import org.springframework.data.jpa.repository.query.DefaultJpaEntityMetadata;
import org.springframework.data.jpa.repository.query.JpaEntityMetadata;
import org.springframework.data.jpa.repository.query.JpaQueryMethod;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;

import java.lang.reflect.Method;

/**
 * @author Moritz Becker (moritz.becker@gmx.at)
 * @since 1.2
 */
public class EntityViewAwareJpaQueryMethod extends JpaQueryMethod {

    /**
     * Creates a {@link JpaQueryMethod}.
     *
     * @param method must not be {@literal null}
     * @param extractor must not be {@literal null}
     * @param metadata must not be {@literal null}
     */
    public EntityViewAwareJpaQueryMethod(Method method, RepositoryMetadata metadata, ProjectionFactory factory,
                          QueryExtractor extractor) {

        super(method, metadata, factory, extractor);
    }

    public boolean isEntityViewQuery() {
        return getReturnedObjectType().isAnnotationPresent(EntityView.class);
    }

    public Class<?> getEntityViewClass() {
        if (isEntityViewQuery()) {
            return getReturnedObjectType();
        } else {
            return null;
        }
    }

    @Override
    public JpaEntityMetadata<?> getEntityInformation() {
        return new DefaultJpaEntityMetadata(getDomainClass());
    }

    @Override
    public Class<?> getDomainClass() {
        if (isEntityViewQuery()) {
            return getReturnedObjectType().getAnnotation(EntityView.class).value();
        } else {
            return super.getDomainClass();
        }
    }
}
