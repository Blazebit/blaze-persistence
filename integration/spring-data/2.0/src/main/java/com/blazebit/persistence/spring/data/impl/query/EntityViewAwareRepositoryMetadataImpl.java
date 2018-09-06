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
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import org.springframework.data.repository.core.CrudMethods;
import org.springframework.data.repository.core.RepositoryMetadata;

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
}
