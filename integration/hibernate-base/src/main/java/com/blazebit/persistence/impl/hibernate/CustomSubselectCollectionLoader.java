package com.blazebit.persistence.impl.hibernate;

import org.hibernate.MappingException;
import org.hibernate.engine.spi.LoadQueryInfluencers;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.loader.collection.SubselectCollectionLoader;
import org.hibernate.persister.collection.QueryableCollection;

import java.util.Collection;
import java.util.Map;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.2.0
 */
public class CustomSubselectCollectionLoader extends SubselectCollectionLoader {

    public CustomSubselectCollectionLoader(QueryableCollection persister, String subquery, Collection entityKeys, QueryParameters queryParameters, Map<String, int[]> namedParameterLocMap, SessionFactoryImplementor factory, LoadQueryInfluencers loadQueryInfluencers) throws MappingException {
        super(persister, subquery, entityKeys, queryParameters, namedParameterLocMap, factory, loadQueryInfluencers);
        String originalSql = queryParameters.getFilteredSQL();
        if (originalSql.startsWith("with ")) {
            StringBuilder sb = new StringBuilder(sql.length() + originalSql.length());
            int brackets = 0;
            boolean cteMode = false;
            for (int i = 0; i < originalSql.length(); i++) {
                final char c = originalSql.charAt(i);
                if (c == '(') {
                    brackets++;
                } else if (c == ')') {
                    brackets--;
                    if (brackets == 0) {
                        cteMode = !cteMode;
                    }
                }

                if (!cteMode && brackets == 0 && originalSql.regionMatches(true, i, "select ", 0, "select ".length())) {
                    break;
                }

                sb.append(c);
            }

            sb.append(sql);
            this.sql = sb.toString();
        }
    }

}
