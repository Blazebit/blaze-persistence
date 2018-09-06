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

import com.blazebit.persistence.spring.data.base.query.EntityViewAwareRepositoryMetadata;
import org.springframework.data.repository.core.CrudMethods;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.util.Streamable;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class EntityViewAwareRepositoryInformation implements RepositoryInformation, EntityViewAwareRepositoryMetadata {

    private final EntityViewAwareRepositoryMetadata metadata;
    private final RepositoryInformation repositoryInformation;

    public EntityViewAwareRepositoryInformation(EntityViewAwareRepositoryMetadata metadata, RepositoryInformation repositoryInformation) {
        this.metadata = metadata;
        this.repositoryInformation = repositoryInformation;
    }

    @Override
    public Class<?> getRepositoryBaseClass() {
        return repositoryInformation.getRepositoryBaseClass();
    }

    @Override
    public boolean hasCustomMethod() {
        return repositoryInformation.hasCustomMethod();
    }

    @Override
    public boolean isCustomMethod(Method method) {
        return repositoryInformation.isCustomMethod(method);
    }

    @Override
    public boolean isQueryMethod(Method method) {
        return repositoryInformation.isQueryMethod(method);
    }

    @Override
    public boolean isBaseClassMethod(Method method) {
        return repositoryInformation.isBaseClassMethod(method);
    }

    @Override
    public Streamable<Method> getQueryMethods() {
        return repositoryInformation.getQueryMethods();
    }

    @Override
    public Method getTargetClassMethod(Method method) {
        return repositoryInformation.getTargetClassMethod(method);
    }

    @Override
    public Class<? extends Serializable> getIdType() {
        return (Class<? extends Serializable>) repositoryInformation.getIdType();
    }

    @Override
    public Class<?> getDomainType() {
        return repositoryInformation.getDomainType();
    }

    @Override
    public Class<?> getEntityViewType() {
        return metadata.getEntityViewType();
    }

    @Override
    public Class<?> getRepositoryInterface() {
        return repositoryInformation.getRepositoryInterface();
    }

    @Override
    public Class<?> getReturnedDomainClass(Method method) {
        return repositoryInformation.getReturnedDomainClass(method);
    }

    @Override
    public Class<?> getReturnedEntityViewClass(Method method) {
        return metadata.getReturnedEntityViewClass(method);
    }

    @Override
    public CrudMethods getCrudMethods() {
        return repositoryInformation.getCrudMethods();
    }

    @Override
    public boolean isPagingRepository() {
        return repositoryInformation.isPagingRepository();
    }

    @Override
    public Set<Class<?>> getAlternativeDomainTypes() {
        return repositoryInformation.getAlternativeDomainTypes();
    }

    public boolean isReactiveRepository() {
        return metadata.isReactiveRepository();
    }
}
