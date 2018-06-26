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

package com.blazebit.persistence.integration.hibernate;

import com.blazebit.persistence.integration.hibernate.base.SubselectLoaderUtils;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.LoadQueryInfluencers;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.loader.collection.SubselectCollectionLoader;
import org.hibernate.persister.collection.QueryableCollection;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

import static com.blazebit.persistence.integration.hibernate.base.SubselectLoaderUtils.getPreparedStatementProxy;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.2.0
 */
public class CustomSubselectCollectionLoader extends SubselectCollectionLoader {

    private final int cteParameterCount;

    public CustomSubselectCollectionLoader(QueryableCollection persister, String subquery, Collection entityKeys, QueryParameters queryParameters, Map<String, int[]> namedParameterLocMap, SessionFactoryImplementor factory, LoadQueryInfluencers loadQueryInfluencers) throws MappingException {
        super(persister, subquery, entityKeys, queryParameters, namedParameterLocMap, factory, loadQueryInfluencers);
        String originalSql = queryParameters.getFilteredSQL();
        if (originalSql.startsWith("with ")) {
            StringBuilder sb = new StringBuilder(sql.length() + originalSql.length());
            cteParameterCount = SubselectLoaderUtils.applyCteAndCountParameters(originalSql, sb);
            sb.append(sql);
            this.sql = sb.toString();
        } else {
            cteParameterCount = 0;
        }
    }

    @Override
    protected int bindParameterValues(PreparedStatement statement, QueryParameters queryParameters, int startIndex, SharedSessionContractImplementor session) throws SQLException {
        if (cteParameterCount > 0) {
            statement = getPreparedStatementProxy(statement, queryParameters, cteParameterCount);
        }
        return super.bindParameterValues(statement, queryParameters, startIndex, session);
    }
}
