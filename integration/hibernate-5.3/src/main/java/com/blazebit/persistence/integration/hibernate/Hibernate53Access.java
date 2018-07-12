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

import com.blazebit.apt.service.ServiceProvider;
import com.blazebit.persistence.integration.hibernate.base.HibernateAccess;
import com.blazebit.persistence.integration.hibernate.base.HibernateExtendedQuerySupport;
import com.blazebit.persistence.integration.hibernate.base.HibernateReturningResult;
import com.blazebit.persistence.spi.DbmsDialect;
import org.hibernate.HibernateException;
import org.hibernate.LockOptions;
import org.hibernate.Query;
import org.hibernate.engine.jdbc.spi.JdbcCoordinator;
import org.hibernate.engine.query.spi.HQLQueryPlan;
import org.hibernate.engine.spi.ExceptionConverter;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.RowSelection;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.engine.spi.TypedValue;
import org.hibernate.event.spi.EventSource;
import org.hibernate.hql.internal.ast.ParameterTranslationsImpl;
import org.hibernate.hql.internal.ast.exec.BasicExecutor;
import org.hibernate.hql.internal.ast.exec.StatementExecutor;
import org.hibernate.hql.spi.ParameterTranslations;
import org.hibernate.loader.hql.QueryLoader;
import org.hibernate.param.ParameterSpecification;
import org.hibernate.query.internal.AbstractProducedQuery;
import org.hibernate.query.internal.QueryParameterBindingsImpl;
import org.hibernate.query.spi.QueryImplementor;
import org.hibernate.resource.transaction.backend.jta.internal.JtaTransactionCoordinatorImpl;
import org.hibernate.resource.transaction.spi.TransactionCoordinator;
import org.hibernate.type.Type;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
@ServiceProvider(HibernateAccess.class)
public class Hibernate53Access implements HibernateAccess {

    private static final Logger LOG = Logger.getLogger(HibernateExtendedQuerySupport.class.getName());
    private static final Method DO_EXECUTE_METHOD;
    private static final Constructor<ParameterTranslationsImpl> PARAMETER_TRANSLATIONS_CONSTRUCTOR;

    static {
        try {
            Method m = BasicExecutor.class.getDeclaredMethod("doExecute", QueryParameters.class, SharedSessionContractImplementor.class, String.class, List.class);
            m.setAccessible(true);
            DO_EXECUTE_METHOD = m;
            Constructor<ParameterTranslationsImpl> c = ParameterTranslationsImpl.class.getDeclaredConstructor(List.class);
            c.setAccessible(true);
            PARAMETER_TRANSLATIONS_CONSTRUCTOR = c;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    @Override
    public SessionImplementor wrapSession(SessionImplementor session, DbmsDialect dbmsDialect, String[][] columns, int[] returningSqlTypes, HibernateReturningResult<?> returningResult) {
        JdbcCoordinator jdbcCoordinator = session.getJdbcCoordinator();
        
        Object jdbcCoordinatorProxy = Proxy.newProxyInstance(jdbcCoordinator.getClass().getClassLoader(), new Class[]{ JdbcCoordinator.class }, new JdbcCoordinatorInvocationHandler(jdbcCoordinator, session.getFactory(), dbmsDialect, columns, returningSqlTypes, returningResult));
        Object sessionProxy = Proxy.newProxyInstance(session.getClass().getClassLoader(), new Class[]{ SessionImplementor.class, EventSource.class }, new Hibernate53SessionInvocationHandler(session, jdbcCoordinatorProxy));
        return (SessionImplementor) sessionProxy;
    }

    @Override
    public SessionFactoryImplementor wrapSessionFactory(SessionFactoryImplementor sessionFactory, DbmsDialect dbmsDialect) {
        Object dialectProxy = new Hibernate53LimitHandlingDialect(sessionFactory.getDialect(), dbmsDialect);
        Object sessionFactoryProxy = Proxy.newProxyInstance(sessionFactory.getClass().getClassLoader(), new Class[]{ SessionFactoryImplementor.class }, new Hibernate53SessionFactoryInvocationHandler(sessionFactory, dialectProxy));
        return (SessionFactoryImplementor) sessionFactoryProxy;
    }

    @Override
    public void checkTransactionSynchStatus(SessionImplementor session) {
        TransactionCoordinator coordinator = session.getTransactionCoordinator();
        coordinator.pulse();
        if (coordinator instanceof JtaTransactionCoordinatorImpl) {
            ((JtaTransactionCoordinatorImpl) coordinator).getSynchronizationCallbackCoordinator().processAnyDelayedAfterCompletion();
        }
    }

    @Override
    public void afterTransaction(SessionImplementor session, boolean success) {
        TransactionCoordinator coordinator = session.getTransactionCoordinator();
        if (!session.isTransactionInProgress() ) {
            session.getJdbcCoordinator().afterTransaction();
        }
        if (coordinator instanceof JtaTransactionCoordinatorImpl) {
            ((JtaTransactionCoordinatorImpl) coordinator).getSynchronizationCallbackCoordinator().processAnyDelayedAfterCompletion();
        }
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    public List<Object[]> list(QueryLoader queryLoader, SessionImplementor sessionImplementor, QueryParameters queryParameters) {
        return queryLoader.list(sessionImplementor, queryParameters);
    }

    @Override
    public List<Object> performList(HQLQueryPlan queryPlan, SessionImplementor sessionImplementor, QueryParameters queryParameters) {
        return queryPlan.performList(queryParameters, sessionImplementor);
    }

    @Override
    public int performExecuteUpdate(HQLQueryPlan queryPlan, SessionImplementor sessionImplementor, QueryParameters queryParameters) {
        return queryPlan.performExecuteUpdate(queryParameters, sessionImplementor);
    }

    @Override
    public void doExecute(StatementExecutor executor, String delete, QueryParameters parameters, SessionImplementor session, List<ParameterSpecification> parameterSpecifications) {
        try {
            DO_EXECUTE_METHOD.invoke(executor, parameters, session, delete, parameterSpecifications);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public QueryParameters getQueryParameters(Query hibernateQuery, Map<String, TypedValue> namedParams) {
        return ((AbstractProducedQuery<?>) hibernateQuery).getQueryParameters();
    }

    @Override
    public Map<String, TypedValue> getNamedParams(Query hibernateQuery) {
        QueryParameterBindingsImpl queryParameterBindings = hibernateQuery.unwrap(QueryParameterBindingsImpl.class);
        return queryParameterBindings.collectNamedParameterBindings();
    }

    @Override
    public String expandParameterLists(SessionImplementor session, org.hibernate.Query hibernateQuery, Map<String, TypedValue> namedParamsCopy) {
        QueryParameterBindingsImpl queryParameterBindings = hibernateQuery.unwrap(QueryParameterBindingsImpl.class);
        SharedSessionContractImplementor producer = (SharedSessionContractImplementor) ((QueryImplementor<?>) hibernateQuery).getProducer();
        String query = hibernateQuery.getQueryString();

        // NOTE: In Hibernate 5.3.0.CR1 this is call causes a side effect which is why this is essentially unusable for us
        query = queryParameterBindings.expandListValuedParameters(query, producer);
        return query;
    }

    private ExceptionConverter getExceptionConverter(EntityManager em) {
        return em.unwrap(SharedSessionContractImplementor.class).getExceptionConverter();
    }

    @Override
    public RuntimeException convert(EntityManager em, HibernateException e) {
        return getExceptionConverter(em).convert(e);
    }

    @Override
    public void handlePersistenceException(EntityManager em, PersistenceException e) {
        getExceptionConverter(em).convert(e);
    }

    @Override
    public void throwPersistenceException(EntityManager em, HibernateException e) {
        getExceptionConverter(em).convert(e);
    }

    @Override
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
            final Serializable[] collectionKeys) {
        return new QueryParameters(
                positionalParameterTypes,
                positionalParameterValues,
                namedParameters,
                lockOptions,
                rowSelection,
                isReadOnlyInitialized,
                readOnly,
                cacheable,
                cacheRegion,
                comment,
                queryHints,
                collectionKeys,
                null
        );
    }

    @Override
    public ParameterTranslations createParameterTranslations(List<ParameterSpecification> queryParameterSpecifications) {
        try {
            return PARAMETER_TRANSLATIONS_CONSTRUCTOR.newInstance(queryParameterSpecifications);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
