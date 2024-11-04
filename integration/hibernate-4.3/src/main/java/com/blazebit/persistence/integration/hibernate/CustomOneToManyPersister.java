/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.hibernate;

import com.blazebit.persistence.integration.hibernate.base.CustomCollectionPersister;
import com.blazebit.persistence.integration.hibernate.base.SubselectLoaderUtils;
import org.hibernate.MappingException;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.access.CollectionRegionAccessStrategy;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.SubselectFetch;
import org.hibernate.loader.collection.CollectionInitializer;
import org.hibernate.mapping.Collection;
import org.hibernate.persister.collection.OneToManyPersister;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CustomOneToManyPersister extends OneToManyPersister implements CustomCollectionPersister {

    public CustomOneToManyPersister(Collection collection, CollectionRegionAccessStrategy cacheAccessStrategy, Configuration cfg, SessionFactoryImplementor factory) throws MappingException, CacheException {
        super(collection, cacheAccessStrategy, cfg, factory);
    }

    @Override
    protected CollectionInitializer createSubselectInitializer(SubselectFetch subselect, SessionImplementor session) {
        return new CustomSubselectOneToManyLoader(
                this,
                SubselectLoaderUtils.getSubselectQueryForHibernatePre5(subselect.toSubselectString(getCollectionType().getLHSPropertyName())),
                subselect.getResult(),
                subselect.getQueryParameters(),
                subselect.getNamedParameterLocMap(),
                session.getFactory(),
                session.getLoadQueryInfluencers()
        );
    }
}