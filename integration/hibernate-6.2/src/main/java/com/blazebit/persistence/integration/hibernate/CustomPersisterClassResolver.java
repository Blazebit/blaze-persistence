/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.hibernate;

import com.blazebit.apt.service.ServiceProvider;
import org.hibernate.mapping.Collection;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.internal.StandardPersisterClassResolver;
import org.hibernate.persister.spi.PersisterClassResolver;

/**
 * @author Christian Beikov
 * @since 1.6.7
 */
@ServiceProvider(PersisterClassResolver.class)
public class CustomPersisterClassResolver extends StandardPersisterClassResolver implements PersisterClassResolver {

    @Override
    public Class<? extends CollectionPersister> getCollectionPersisterClass(Collection metadata) {
        return metadata.isOneToMany() ? CustomOneToManyPersister.class : CustomBasicCollectionPersister.class;
    }

}
