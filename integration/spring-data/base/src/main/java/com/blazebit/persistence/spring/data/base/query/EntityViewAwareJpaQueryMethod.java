/*
 * Copyright 2014 - 2019 Blazebit.
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

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.jpa.provider.QueryExtractor;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.jpa.repository.query.JpaEntityGraph;
import org.springframework.data.jpa.repository.query.JpaQueryMethod;
import org.springframework.data.projection.ProjectionFactory;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Moritz Becker
 * @author Christian Beikov
 * @since 1.2.0
 */
public class EntityViewAwareJpaQueryMethod extends JpaQueryMethod {

    private final Class<?> entityViewClass;
    private final JpaParameters parameters;
    private final LockModeType lockModeType;
    private final JpaEntityGraph entityGraph;
    private final Map<String, String> queryHints;

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
        this.lockModeType = findLockModeType(method);
        this.entityGraph = findEntityGraph(method);
        this.queryHints = findQueryHints(method);
        this.entityViewClass = metadata.getReturnedEntityViewClass(method);
        this.parameters = new JpaParameters(method);
    }

    public LockModeType getLockModeType() {
        return lockModeType;
    }

    public JpaEntityGraph getEntityGraph() {
        return entityGraph;
    }

    public Map<String, String> getHints() {
        return queryHints;
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

    private JpaEntityGraph findEntityGraph(Method method) {
        EntityGraph entityGraph = AnnotatedElementUtils.findMergedAnnotation(method, EntityGraph.class);
        return entityGraph == null ? null : new JpaEntityGraph(entityGraph, this.getNamedQueryName());
    }

    private LockModeType findLockModeType(Method method) {
        Lock annotation = AnnotatedElementUtils.findMergedAnnotation(method, Lock.class);
        return annotation == null ? null : (LockModeType) AnnotationUtils.getValue(annotation);
    }

    private Map<String, String> findQueryHints(Method method) {
        Map<String, String> queryHints = new HashMap<>();
        QueryHints queryHintsAnnotation = AnnotatedElementUtils.findMergedAnnotation(method, QueryHints.class);

        if (queryHintsAnnotation != null) {

            for (QueryHint hint : queryHintsAnnotation.value()) {
                queryHints.put(hint.name(), hint.value());
            }
        }

        QueryHint queryHintAnnotation = AnnotationUtils.findAnnotation(method, QueryHint.class);

        if (queryHintAnnotation != null) {
            queryHints.put(queryHintAnnotation.name(), queryHintAnnotation.value());
        }

        return Collections.unmodifiableMap(queryHints);
    }
}
