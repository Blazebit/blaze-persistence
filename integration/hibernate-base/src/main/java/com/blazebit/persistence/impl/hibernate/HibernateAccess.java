package com.blazebit.persistence.impl.hibernate;

import org.hibernate.HibernateException;
import org.hibernate.LockOptions;
import org.hibernate.Query;
import org.hibernate.engine.query.spi.HQLQueryPlan;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.RowSelection;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.TypedValue;
import org.hibernate.loader.hql.QueryLoader;
import org.hibernate.type.Type;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;


public interface HibernateAccess {

    public List<Object[]> list(QueryLoader queryLoader, SessionImplementor sessionImplementor, QueryParameters queryParameters);

    public List<Object> performList(HQLQueryPlan queryPlan, SessionImplementor sessionImplementor, QueryParameters queryParameters);

    public int performExecuteUpdate(HQLQueryPlan queryPlan, SessionImplementor sessionImplementor, QueryParameters queryParameters);

    public QueryParameters getQueryParameters(Query hibernateQuery, Map<String, TypedValue> namedParams);

    public Map<String, TypedValue> getNamedParams(Query hibernateQuery);

    public String expandParameterLists(SessionImplementor session, Query hibernateQuery, Map<String, TypedValue> namedParamsCopy);

    public SessionImplementor wrapSession(SessionImplementor session, boolean generated, String[][] columns, HibernateReturningResult<?> returningResult);
    
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
}
