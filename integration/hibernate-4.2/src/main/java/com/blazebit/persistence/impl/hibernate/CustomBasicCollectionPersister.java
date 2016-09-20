package com.blazebit.persistence.impl.hibernate;

import org.hibernate.MappingException;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.access.CollectionRegionAccessStrategy;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.SubselectFetch;
import org.hibernate.loader.collection.CollectionInitializer;
import org.hibernate.mapping.Collection;
import org.hibernate.persister.collection.BasicCollectionPersister;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.2.0
 */
public class CustomBasicCollectionPersister extends BasicCollectionPersister {

    public CustomBasicCollectionPersister(Collection collection, CollectionRegionAccessStrategy cacheAccessStrategy, Configuration cfg, SessionFactoryImplementor factory) throws MappingException, CacheException {
        super(collection, cacheAccessStrategy, cfg, factory);
    }

    @Override
    protected CollectionInitializer createSubselectInitializer(SubselectFetch subselect, SessionImplementor session) {
        // Hibernate before 4 couldn't find the correct from clause
        StringBuilder sb = null;
        String subselectQuery = subselect.toSubselectString( getCollectionType().getLHSPropertyName() );
        int parens = 0;

        for (int i = 0; i < subselectQuery.length(); i++) {
            final char c = subselectQuery.charAt(i);
            if (c == '(') {
                parens++;
            } else if (c == ')') {
                parens--;
                if (parens < 0) {
                    // This is the case when we have a CTE
                    int fromIndex = subselectQuery.indexOf("from");
                    int otherFromIndex = subselectQuery.indexOf("from", i);
                    sb = new StringBuilder(fromIndex + (subselectQuery.length() - otherFromIndex));
                    sb.append(subselectQuery, 0, fromIndex);
                    sb.append(subselectQuery, otherFromIndex, subselectQuery.length());
                    break;
                }
            }
        }

        if (sb != null) {
            subselectQuery = sb.toString();
        }

        return new CustomSubselectCollectionLoader(
            this,
            subselectQuery,
            subselect.getResult(),
            subselect.getQueryParameters(),
            subselect.getNamedParameterLocMap(),
            session.getFactory(),
            session.getLoadQueryInfluencers()
        );
    }
}
