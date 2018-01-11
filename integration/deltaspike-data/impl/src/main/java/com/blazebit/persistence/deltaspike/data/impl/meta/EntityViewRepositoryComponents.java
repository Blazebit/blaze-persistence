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

import com.blazebit.persistence.view.EntityViewManager;
import org.apache.deltaspike.data.impl.RepositoryDefinitionException;
import org.apache.deltaspike.data.impl.meta.RepositoryComponent;
import org.apache.deltaspike.data.impl.meta.RepositoryEntity;
import org.apache.deltaspike.data.impl.meta.RepositoryMethod;
import org.apache.deltaspike.data.impl.meta.extractor.AnnotationMetadataExtractor;
import org.apache.deltaspike.data.impl.meta.extractor.MetadataExtractor;
import org.apache.deltaspike.data.impl.meta.extractor.TypeMetadataExtractor;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation is similar to {@link org.apache.deltaspike.data.impl.meta.RepositoryComponents} but was modified to
 * work with entity views.
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public class EntityViewRepositoryComponents {
    private final Map<Class<?>, EntityViewRepositoryComponent> repos = new ConcurrentHashMap<Class<?>, EntityViewRepositoryComponent>();

    private final List<MetadataExtractor> extractors = Arrays.asList(new AnnotationMetadataExtractor(), new TypeMetadataExtractor());

    /**
     * Add a Repository class to the meta data repository.
     *
     * @param repoClass The repo class.
     */
    public void add(Class<?> repoClass, EntityViewManager evm) {
        RepositoryEntity entityClass = extractEntityMetaData(repoClass, evm);
        EntityViewRepositoryComponent repo = new EntityViewRepositoryComponent(repoClass, entityClass, evm);
        repos.put(repoClass, repo);
    }

    /**
     * Repository access - lookup the Repository component meta data from a list of candidate classes.
     * Depending on the implementation, proxy objects might have been modified so the actual class
     * does not match the original Repository class.
     *
     * @param candidateClasses List of candidates to check.
     * @return A {@link RepositoryComponent} corresponding to the repoClass parameter.
     */
    public EntityViewRepositoryComponent lookupComponent(List<Class<?>> candidateClasses) {
        for (Class<?> repoClass : candidateClasses) {
            if (repos.containsKey(repoClass)) {
                return repos.get(repoClass);
            }
        }
        throw new RuntimeException("Unknown Repository classes " + candidateClasses);
    }

    /**
     * Repository access - lookup the Repository component meta data for a specific Repository class.
     *
     * @param repoClass The Repository class to lookup the method for
     * @return A {@link RepositoryComponent} corresponding to the repoClass parameter.
     */
    public EntityViewRepositoryComponent lookupComponent(Class<?> repoClass) {
        if (repos.containsKey(repoClass)) {
            return repos.get(repoClass);
        }
        throw new RuntimeException("Unknown Repository class " + repoClass.getName());
    }

    /**
     * Repository access - lookup method information for a specific Repository class.
     *
     * @param repoClass The Repository class to lookup the method for
     * @param method    The Method object to get Repository meta data for.
     * @return A {@link RepositoryMethod} corresponding to the method parameter.
     */
    public RepositoryMethod lookupMethod(Class<?> repoClass, Method method) {
        return lookupComponent(repoClass).lookupMethod(method);
    }

    public RepositoryMethod lookupMethod(EntityViewRepositoryComponent component, Method method) {
        return component.lookupMethod(method);
    }

    private RepositoryEntity extractEntityMetaData(Class<?> repoClass, EntityViewManager evm) {
        for (MetadataExtractor extractor : extractors) {
            RepositoryEntity entity = extractor.extract(repoClass);
            if (entity != null) {
                return entity;
            }
        }
        throw new RepositoryDefinitionException(repoClass);
    }

    public Map<Class<?>, EntityViewRepositoryComponent> getRepositories() {
        return repos;
    }

    public void addAll(Map<Class<?>, EntityViewRepositoryComponent> repositories) {
        this.repos.putAll(repositories);
    }
}