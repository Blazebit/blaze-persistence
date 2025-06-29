/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.data.impl.query;

import com.blazebit.persistence.spring.data.base.query.EntityViewAwareRepositoryMetadata;
import com.blazebit.persistence.view.EntityViewManager;
import org.springframework.data.repository.core.CrudMethods;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.support.RepositoryFragment;
import org.springframework.data.util.Streamable;
import org.springframework.data.util.TypeInformation;

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
    public EntityViewManager getEntityViewManager() {
        return metadata.getEntityViewManager();
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


    @Override
    public TypeInformation<?> getReturnType(Method method) {
        return repositoryInformation.getReturnType(method);
    }

    @Override
    public TypeInformation<?> getIdTypeInformation() {
        return metadata.getIdTypeInformation();
    }

    @Override
    public TypeInformation<?> getDomainTypeInformation() {
        return metadata.getDomainTypeInformation();
    }

    @Override
    public Set<RepositoryFragment<?>> getFragments() {
        // TODO: implement
        return null;
    }
}
