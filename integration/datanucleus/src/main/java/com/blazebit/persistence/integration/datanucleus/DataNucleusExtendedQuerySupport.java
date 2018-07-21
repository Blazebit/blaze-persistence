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

package com.blazebit.persistence.integration.datanucleus;

import com.blazebit.apt.service.ServiceProvider;
import com.blazebit.persistence.ReturningResult;
import com.blazebit.persistence.spi.ExtendedQuerySupport;
import org.datanucleus.store.rdbms.query.JPQLQuery;
import org.datanucleus.store.rdbms.query.RDBMSQueryCompilation;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.lang.reflect.Field;
import java.util.List;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
@ServiceProvider(ExtendedQuerySupport.class)
public class DataNucleusExtendedQuerySupport implements ExtendedQuerySupport {
    
    private static final Field DATASTORE_COMPILATION_FIELD;
    
    static {
        try {
            DATASTORE_COMPILATION_FIELD = JPQLQuery.class.getDeclaredField("datastoreCompilation");
            DATASTORE_COMPILATION_FIELD.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException("Unsupported datanucleus version!", e);
        }
    }

    @Override
    public boolean supportsAdvancedSql() {
        return false;
    }

    @Override
    public String getSql(EntityManager em, Query query) {
        org.datanucleus.store.query.Query<?> dnQuery = query.unwrap(org.datanucleus.store.query.Query.class);
        dnQuery.compile();
        return (String) dnQuery.getNativeQuery();
    }
    
    public List<String> getCascadingDeleteSql(EntityManager em, Query query) {
        // TODO: implement
        throw new UnsupportedOperationException("Not yet implemeneted!");
    }

    @Override
    public int getSqlSelectAliasPosition(EntityManager em, Query query, String alias) {
        // TODO: implement
        throw new UnsupportedOperationException("Not yet implemeneted!");
    }

    @Override
    public String getSqlAlias(EntityManager em, Query query, String alias) {
        // TODO: implement
        throw new UnsupportedOperationException("Not yet implemeneted!");
    }

    @Override
    public int getSqlSelectAttributePosition(EntityManager em, Query query, String alias) {
        // TODO: implement
        throw new UnsupportedOperationException("Not yet implemeneted!");
    }

    @Override
    @SuppressWarnings("rawtypes")
    public List getResultList(com.blazebit.persistence.spi.ServiceProvider serviceProvider, List<Query> participatingQueries, Query query, String sqlOverride) {
        applySql(query, sqlOverride);
        return query.getResultList();
    }
    
    @Override
    public Object getSingleResult(com.blazebit.persistence.spi.ServiceProvider serviceProvider, List<Query> participatingQueries, Query query, String sqlOverride) {
        applySql(query, sqlOverride);
        return query.getSingleResult();
    }

    @Override
    public int executeUpdate(com.blazebit.persistence.spi.ServiceProvider serviceProvider, List<Query> participatingQueries, Query baseQuery, Query query, String sqlOverride) {
        applySql(query, sqlOverride);
        return query.executeUpdate();
    }

    @Override
    public ReturningResult<Object[]> executeReturning(com.blazebit.persistence.spi.ServiceProvider serviceProvider, List<Query> participatingQueries, Query modificationBaseQuery, Query exampleQuery, String sqlOverride) {
        // TODO: implement
        throw new UnsupportedOperationException("Not yet implemeneted!");
    }
    
    private void applySql(Query query, String sqlOverride) {
        // TODO: parameter handling
        org.datanucleus.store.query.Query<?> dnQuery = query.unwrap(org.datanucleus.store.query.Query.class);
        // Disable caching for these queries
        dnQuery.addExtension("datanucleus.query.compilation.cached", Boolean.FALSE);
        try {
            RDBMSQueryCompilation datastoreCompilation = (RDBMSQueryCompilation) DATASTORE_COMPILATION_FIELD.get(dnQuery);
            datastoreCompilation.setSQL(sqlOverride);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
