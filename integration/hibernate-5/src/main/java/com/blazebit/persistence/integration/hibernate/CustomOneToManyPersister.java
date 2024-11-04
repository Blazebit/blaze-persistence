/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.hibernate;

import com.blazebit.persistence.integration.hibernate.base.CustomCollectionPersister;
import org.hibernate.MappingException;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.access.CollectionRegionAccessStrategy;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.SubselectFetch;
import org.hibernate.loader.collection.CollectionInitializer;
import org.hibernate.mapping.Collection;
import org.hibernate.persister.collection.OneToManyPersister;
import org.hibernate.persister.spi.PersisterCreationContext;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CustomOneToManyPersister extends OneToManyPersister implements CustomCollectionPersister {

    public CustomOneToManyPersister(Collection collectionBinding, CollectionRegionAccessStrategy cacheAccessStrategy, PersisterCreationContext creationContext) throws MappingException, CacheException {
        super(collectionBinding, cacheAccessStrategy, creationContext);
    }

    @Override
    protected CollectionInitializer createSubselectInitializer(SubselectFetch subselect, SessionImplementor session) {
        return new CustomSubselectOneToManyLoader(
                this,
                subselect.toSubselectString( getCollectionType().getLHSPropertyName() ),
                subselect.getResult(),
                subselect.getQueryParameters(),
                subselect.getNamedParameterLocMap(),
                session.getFactory(),
                session.getLoadQueryInfluencers()
        );
    }
}