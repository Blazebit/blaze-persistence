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

package com.blazebit.persistence.integration.hibernate.base;

import com.blazebit.persistence.spi.DbmsDialect;
import org.hibernate.HibernateException;
import org.hibernate.LockOptions;
import org.hibernate.Query;
import org.hibernate.engine.query.spi.HQLQueryPlan;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.RowSelection;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.TypedValue;
import org.hibernate.hql.internal.ast.exec.StatementExecutor;
import org.hibernate.hql.spi.ParameterTranslations;
import org.hibernate.loader.hql.QueryLoader;
import org.hibernate.param.ParameterSpecification;
import org.hibernate.type.Type;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface HibernateAccess {

    public List<Object[]> list(QueryLoader queryLoader, SessionImplementor sessionImplementor, QueryParameters queryParameters);

    public List<Object> performList(HQLQueryPlan queryPlan, SessionImplementor sessionImplementor, QueryParameters queryParameters);

    public int performExecuteUpdate(HQLQueryPlan queryPlan, SessionImplementor sessionImplementor, QueryParameters queryParameters);

    public void doExecute(StatementExecutor executor, String delete, QueryParameters parameters, SessionImplementor session, List<ParameterSpecification> parameterSpecifications);

    public QueryParameters getQueryParameters(Query hibernateQuery, Map<String, TypedValue> namedParams);

    public Map<String, TypedValue> getNamedParams(Query hibernateQuery);

    public String expandParameterLists(SessionImplementor session, Query hibernateQuery, Map<String, TypedValue> namedParamsCopy);

    public SessionImplementor wrapSession(SessionImplementor session, DbmsDialect dbmsDialect, String[][] columns, int[] returningSqlTypes, HibernateReturningResult<?> returningResult);

    public SessionFactoryImplementor wrapSessionFactory(SessionFactoryImplementor sessionFactory, DbmsDialect dbmsDialect);
    
    public void checkTransactionSynchStatus(SessionImplementor session);
    
    public void afterTransaction(SessionImplementor session, boolean success);

    public RuntimeException convert(EntityManager em, HibernateException e);

    public void handlePersistenceException(EntityManager em, PersistenceException e);

    public void throwPersistenceException(EntityManager em, HibernateException e);

    public QueryParameters createQueryParameters(
            final Type[] positionalParameterTypes,
            final Object[] positionalParameterValues,
            final Map<String,TypedValue> namedParameters,
            final LockOptions lockOptions,
            final RowSelection rowSelection,
            final boolean isReadOnlyInitialized,
            final boolean readOnly,
            final boolean cacheable,
            final String cacheRegion,
            //final boolean forceCacheRefresh,
            final String comment,
            final List<String> queryHints,
            final Serializable[] collectionKeys);

    public ParameterTranslations createParameterTranslations(List<ParameterSpecification> queryParameterSpecifications);
}
