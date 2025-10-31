/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.data.impl.query;

import com.blazebit.persistence.spring.data.base.query.EntityViewAwareRepositoryMetadata;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import org.springframework.data.repository.core.CrudMethods;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFragment;
import org.springframework.data.core.TypeInformation;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class EntityViewAwareRepositoryMetadataImpl implements EntityViewAwareRepositoryMetadata {

    private final RepositoryMetadata metadata;
    private final EntityViewManager evm;
    private final Class<?> domainType;
    private final Class<?> entityViewType;

    public EntityViewAwareRepositoryMetadataImpl(RepositoryMetadata metadata, EntityViewManager evm) {
        this.metadata = metadata;
        this.evm = evm;
        Class<?> domainType = metadata.getDomainType();
        ManagedViewType<?> managedViewType = evm.getMetamodel().managedView(domainType);
        if (managedViewType == null) {
            this.domainType = domainType;
            this.entityViewType = null;
        } else {
            this.domainType = managedViewType.getEntityClass();
            this.entityViewType = managedViewType.getJavaType();
        }
    }

    @Override
    public EntityViewManager getEntityViewManager() {
        return evm;
    }

    @Override
    public Class<?> getIdType() {
        return metadata.getIdType();
    }

    @Override
    public Class<?> getDomainType() {
        return domainType;
    }

    @Override
    public Class<?> getEntityViewType() {
        return entityViewType;
    }

    @Override
    public Class<?> getRepositoryInterface() {
        return metadata.getRepositoryInterface();
    }

    @Override
    public Class<?> getReturnedDomainClass(Method method) {
        Class<?> returnedDomainClass = metadata.getReturnedDomainClass(method);
        ManagedViewType<?> managedViewType = evm.getMetamodel().managedView(returnedDomainClass);
        if (managedViewType == null) {
            return returnedDomainClass;
        } else {
            return managedViewType.getEntityClass();
        }
    }

    @Override
    public TypeInformation<?> getReturnType(Method method) {
        return metadata.getReturnType(method);
    }

    @Override
    public Class<?> getReturnedEntityViewClass(Method method) {
        Class<?> returnedDomainClass = metadata.getReturnedDomainClass(method);
        ManagedViewType<?> managedViewType = evm.getMetamodel().managedView(returnedDomainClass);
        if (managedViewType == null) {
            return null;
        } else {
            return managedViewType.getJavaType();
        }
    }

    @Override
    public CrudMethods getCrudMethods() {
        return metadata.getCrudMethods();
    }

    @Override
    public boolean isPagingRepository() {
        return metadata.isPagingRepository();
    }

    @Override
    public Set<Class<?>> getAlternativeDomainTypes() {
        return metadata.getAlternativeDomainTypes();
    }

    @Override
    public boolean isReactiveRepository() {
        return metadata.isReactiveRepository();
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
