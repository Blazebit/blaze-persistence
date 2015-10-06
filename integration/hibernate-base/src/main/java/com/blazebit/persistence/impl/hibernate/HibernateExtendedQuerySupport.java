package com.blazebit.persistence.impl.hibernate;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.metamodel.EntityType;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.TypeMismatchException;
import org.hibernate.dialect.DB2Dialect;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.HSQLDialect;
import org.hibernate.dialect.Oracle8iDialect;
import org.hibernate.dialect.Oracle9Dialect;
import org.hibernate.dialect.PostgreSQL81Dialect;
import org.hibernate.ejb.HibernateEntityManagerImplementor;
import org.hibernate.engine.jdbc.spi.JdbcCoordinator;
import org.hibernate.engine.query.spi.HQLQueryPlan;
import org.hibernate.engine.query.spi.QueryPlanCache;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.TypedValue;
import org.hibernate.engine.transaction.spi.TransactionCoordinator;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.AutoFlushEvent;
import org.hibernate.event.spi.AutoFlushEventListener;
import org.hibernate.event.spi.EventSource;
import org.hibernate.event.spi.EventType;
import org.hibernate.hql.internal.QueryExecutionRequestException;
import org.hibernate.hql.internal.ast.ParameterTranslationsImpl;
import org.hibernate.hql.internal.ast.exec.BasicExecutor;
import org.hibernate.hql.spi.ParameterTranslations;
import org.hibernate.hql.spi.QueryTranslator;
import org.hibernate.internal.util.collections.BoundedConcurrentHashMap;
import org.hibernate.loader.hql.QueryLoader;
import org.hibernate.param.ParameterSpecification;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.hibernate.type.Type;

import com.blazebit.apt.service.ServiceProvider;
import com.blazebit.persistence.ReturningResult;
import com.blazebit.persistence.spi.CteQueryWrapper;
import com.blazebit.persistence.spi.ExtendedQuerySupport;
import com.blazebit.reflection.ExpressionUtils;
import com.blazebit.reflection.ReflectionUtils;

@SuppressWarnings("deprecation")
@ServiceProvider(ExtendedQuerySupport.class)
public class HibernateExtendedQuerySupport implements ExtendedQuerySupport {

    private final ConcurrentMap<SessionFactoryImplementor, BoundedConcurrentHashMap<String, HQLQueryPlan>> queryPlanCachesCache = new ConcurrentHashMap<SessionFactoryImplementor, BoundedConcurrentHashMap<String,HQLQueryPlan>>();
    private final HibernateAccess hibernateAccess;
    
	public HibernateExtendedQuerySupport() {
	    Iterator<HibernateAccess> serviceIter = ServiceLoader.load(HibernateAccess.class).iterator();
	    if (!serviceIter.hasNext()) {
	        throw new IllegalStateException("Hibernate integration was not found on the class path!");
	    }
	    this.hibernateAccess = serviceIter.next();
    }

    @Override
	public String getSql(EntityManager em, Query query) {
    	SessionImplementor session = em.unwrap(SessionImplementor.class);
		SessionFactoryImplementor sfi = session.getFactory();
		org.hibernate.Query hibernateQuery = query.unwrap(org.hibernate.Query.class);
		hibernateQuery.setResultTransformer(null);
		String queryString = hibernateQuery.getQueryString();
		HQLQueryPlan queryPlan = sfi.getQueryPlanCache().getHQLQueryPlan(queryString, false, Collections.emptyMap());
		String sql = queryPlan.getSqlStrings()[0];
		return sql;
	}

    @Override
	public String[] getColumnNames(EntityManager em, EntityType<?> entityType, String attributeName) {
        SessionImplementor session = em.unwrap(SessionImplementor.class);
        SessionFactoryImplementor sfi = session.getFactory();
	    return ((AbstractEntityPersister) sfi.getClassMetadata(entityType.getJavaType())).getPropertyColumnNames(attributeName);
	}

	@Override
    public Connection getConnection(EntityManager em) {
		SessionImplementor session = em.unwrap(SessionImplementor.class);
		return session.connection();
    }

    @Override
    @SuppressWarnings("rawtypes")
	public List getResultList(EntityManager em, List<Query> participatingQueries, Query query, String sqlOverride) {
		try {
			return list(em, participatingQueries, query, sqlOverride);
		} catch (QueryExecutionRequestException he) {
			throw new IllegalStateException(he);
		} catch( TypeMismatchException e ) {
			throw new IllegalArgumentException(e);
		} catch (HibernateException he) {
			throw getEntityManager(em).convert( he );
		}
	}
	
	@Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public Object getSingleResult(EntityManager em, List<Query> participatingQueries, Query query, String sqlOverride) {
		try {
			final List result = list(em, participatingQueries, query, sqlOverride);

			if ( result.size() == 0 ) {
				NoResultException nre = new NoResultException("No entity found for query");
				getEntityManager(em).handlePersistenceException( nre );
				throw nre;
			} else if ( result.size() > 1 ) {
				final Set uniqueResult = new HashSet(result);
				if ( uniqueResult.size() > 1 ) {
					NonUniqueResultException nure = new NonUniqueResultException("result returns more than one elements");
					getEntityManager(em).handlePersistenceException( nure );
					throw nure;
				} else {
					return uniqueResult.iterator().next();
				}
			} else {
				return result.get(0);
			}
		} catch (QueryExecutionRequestException he) {
			throw new IllegalStateException(he);
		} catch( TypeMismatchException e ) {
			throw new IllegalArgumentException(e);
		} catch (HibernateException he) {
			throw getEntityManager(em).convert( he );
		}
	}

    @SuppressWarnings("rawtypes")
    private List list(EntityManager em, List<Query> participatingQueries, Query query, String finalSql) {
        SessionImplementor session = em.unwrap(SessionImplementor.class);
        SessionFactoryImplementor sfi = session.getFactory();

        if (session.isClosed()) {
            throw new PersistenceException("Entity manager is closed!");
        }

        HQLQueryPlan queryPlan = getQueryPlan(sfi, query, finalSql);
        
        // Create combined query parameters
        QueryParamEntry queryParametersEntry = createQueryParameters(em, participatingQueries);
        QueryParameters queryParameters = queryParametersEntry.queryParameters;

        prepareQueryPlan(queryPlan, queryParametersEntry, finalSql);
        return queryPlan.performList(queryParameters, session);
    }

    @Override
    public int executeUpdate(EntityManager em, List<Query> participatingQueries, Query query, String finalSql) {
        SessionImplementor session = em.unwrap(SessionImplementor.class);
        SessionFactoryImplementor sfi = session.getFactory();

        if (session.isClosed()) {
            throw new PersistenceException("Entity manager is closed!");
        }
        
        HQLQueryPlan queryPlan = getQueryPlan(sfi, query, finalSql);
        
        // Create combined query parameters
        QueryParamEntry queryParametersEntry = createQueryParameters(em, participatingQueries);
        QueryParameters queryParameters = queryParametersEntry.queryParameters;
        
        prepareQueryPlan(queryPlan, queryParametersEntry, finalSql);
        return queryPlan.performExecuteUpdate(queryParameters, session);
    }

    @Override
    @SuppressWarnings("unchecked")
    public ReturningResult<Object[]> executeReturning(EntityManager em, List<Query> participatingQueries, Query exampleQuery, String sqlOverride) {
        SessionImplementor session = em.unwrap(SessionImplementor.class);
        SessionFactoryImplementor sfi = session.getFactory();

        if (session.isClosed()) {
            throw new PersistenceException("Entity manager is closed!");
        }
        
        // Create plan for example query
        HQLQueryPlan queryPlan = getQueryPlan(sfi, exampleQuery, sqlOverride);
        String exampleQuerySql = queryPlan.getSqlStrings()[0];
        
        StringBuilder sqlSb = new StringBuilder(sqlOverride.length() + 100);
        sqlSb.append(sqlOverride);
        
        String[][] returningColumns = getReturningColumns(exampleQuerySql);
        boolean generatedKeys = appendReturning(em, sqlSb, returningColumns);
        String finalSql = sqlSb.toString();
        
        // Create combined query parameters
        QueryParamEntry queryParametersEntry = createQueryParameters(em, participatingQueries);
        QueryParameters queryParameters = queryParametersEntry.queryParameters;
        
        try {
            QueryTranslator queryTranslator = prepareQueryPlan(queryPlan, queryParametersEntry, finalSql);
            
            // Extract query loader for native listing
            Field queryLoaderField = ReflectionUtils.getField(queryTranslator.getClass(), "queryLoader");
            queryLoaderField.setAccessible(true);
            QueryLoader queryLoader = (QueryLoader) queryLoaderField.get(queryTranslator);
            HibernateReturningResult<Object[]> returningResult = new HibernateReturningResult<Object[]>();
            
            // Do the native list operation with custom session and combined parameters
            
            /*
             * NATIVE LIST START
             */
            TransactionCoordinator transactionCoordinator = session.getTransactionCoordinator();
            transactionCoordinator.pulse();
            transactionCoordinator.getSynchronizationCallbackCoordinator().processAnyDelayedAfterCompletion();
            queryParameters.validateParameters();
            AutoFlushEvent event = new AutoFlushEvent(queryPlan.getQuerySpaces(), (EventSource) session);
            for (AutoFlushEventListener listener : listeners(sfi, EventType.AUTO_FLUSH) ) {
                listener.onAutoFlush( event );
            }

            List<Object[]> results = Collections.EMPTY_LIST;
            boolean success = false;

            try {
                results = queryLoader.list(wrapSession(session, generatedKeys, returningColumns, returningResult), queryParameters);
                success = true;
            } finally {
                transactionCoordinator.afterNonTransactionalQuery( success );
                transactionCoordinator.getSynchronizationCallbackCoordinator().processAnyDelayedAfterCompletion();
            }
            /*
             * NATIVE LIST END
             */
            
            returningResult.setResultList(results);
            return returningResult;
        } catch (Exception e1) {
            throw new RuntimeException(e1);
        }
    }

    private String[][] getReturningColumns(String exampleQuerySql) {
        int fromIndex = exampleQuerySql.indexOf("from");
        int selectIndex = exampleQuerySql.indexOf("select");
        String[] selectItems = exampleQuerySql.substring(selectIndex + "select".length() + 1, fromIndex).trim().split(",");
        String[][] returningColumns = new String[selectItems.length][2];
        
        for (int i = 0; i < selectItems.length; i++) {
            String selectItemWithAlias = selectItems[i].substring(selectItems[i].lastIndexOf('.') + 1);
            returningColumns[i][0] = selectItemWithAlias.substring(0, selectItemWithAlias.indexOf(' '));
            returningColumns[i][1] = selectItemWithAlias.substring(selectItemWithAlias.lastIndexOf(' ') + 1);
        }
        
        return returningColumns;
    }
    
    private boolean appendReturning(EntityManager em, StringBuilder sqlSb, String[][] columns) {
        Session s = em.unwrap(Session.class);
        SessionFactoryImplementor sf = (SessionFactoryImplementor) s.getSessionFactory();
        Dialect dialect = sf.getDialect();
        
        if (dialect instanceof PostgreSQL81Dialect) {
            // Use vendor specific syntax for postgresql
            sqlSb.append(" returning ");
            
            for (int i = 0; i < columns.length; i++) {
                if (i != 0) {
                    sqlSb.append(',');
                }
                
                sqlSb.append(columns[i][0]);
            }
            
            sqlSb.append(";--");
            return true;
        } else if (dialect instanceof HSQLDialect || dialect instanceof DB2Dialect || dialect instanceof Oracle8iDialect || dialect instanceof Oracle9Dialect) {
            // HSQL, DB2 and Oracle support the prepare variant for which one can define the return columns
            return false;
        }
        
        return true;
    }

    private <E> Iterable<E> listeners(SessionFactoryImplementor factory, EventType<E> type) {
        return factory.getServiceRegistry().getService(EventListenerRegistry.class).getEventListenerGroup(type).listeners();
    }
    
    private QueryParamEntry createQueryParameters(EntityManager em, List<Query> participatingQueries) {
        List<ParameterSpecification> parameterSpecifications = new ArrayList<ParameterSpecification>();
        
        List<Type> types = new ArrayList<Type>();
        List<Object> values = new ArrayList<Object>();
        Map<String, TypedValue> namedParams = new LinkedHashMap<String, TypedValue>();
        Serializable collectionKey = null;
//        LockOptions lockOptions = new LockOptions();
//        RowSelection rowSelection = new RowSelection();
//        boolean readOnly = false;
//        boolean cacheable = false;
//        String cacheRegion = null;
//        String comment = null;
//        ResultTransformer resultTransformer = null;
        
        for (QueryParamEntry queryParamEntry : getQueryParamEntries(em, participatingQueries)) {
            QueryParameters participatingQueryParameters = queryParamEntry.queryParameters;
            Collections.addAll(types, participatingQueryParameters.getPositionalParameterTypes());
            Collections.addAll(values, participatingQueryParameters.getPositionalParameterValues());
            namedParams.putAll(participatingQueryParameters.getNamedParameters());
            parameterSpecifications.addAll(queryParamEntry.specifications);
            
            // Merge lock options
//            @SuppressWarnings("unchecked")
//            Iterator<Map.Entry<String, LockMode>> aliasLockIter = participatingQueryParameters.getLockOptions().getAliasLockIterator();
//            while (aliasLockIter.hasNext()) {
//                Map.Entry<String, LockMode> entry = aliasLockIter.next();
//                lockOptions.setAliasSpecificLockMode(entry.getKey(), entry.getValue());
//            }
//            
//            if (participatingQueryParameters.getLockOptions().getLockMode() != LockMode.NONE) {
//                lockOptions.setLockMode(participatingQueryParameters.getLockOptions().getLockMode());
//            }
//            if (participatingQueryParameters.getLockOptions().getScope()) {
//                lockOptions.setScope(true);
//            }
//            if (participatingQueryParameters.getLockOptions().getTimeOut() < lockOptions.getTimeOut()) {
//                lockOptions.setTimeOut(participatingQueryParameters.getLockOptions().getTimeOut());
//            }
            
            // NOTE: we don't merge row selection because there actually shouldn't be any
        }
        
        // We need to create our own queryParameters which joins the query parameters of the participating queries
        // NOTE: Rather use this, because it's more compatible across hibernate versions
        QueryParameters queryParameters = new QueryParameters(
                                   types.toArray(new Type[types.size()]),
                                   values.toArray(new Object[values.size()]),
                                   namedParams,
                                   collectionKey == null ? null : new Serializable[] { collectionKey }
        );
//        QueryParameters queryParameters = new QueryParameters(
//                                  types.toArray(new Type[types.size()]),
//                                  values.toArray(new Object[values.size()]),
//                                  namedParams,
//                                  lockOptions,
//                                  rowSelection,
//                                  true,
//                                  readOnly,
//                                  cacheable,
//                                  cacheRegion,
//                                  comment,
//                                  collectionKey == null ? null : new Serializable[] { collectionKey },
//                                  resultTransformer
//                          );
        
        return new QueryParamEntry(queryParameters, parameterSpecifications);
    }

    private SessionImplementor wrapSession(SessionImplementor session, boolean generatedKeys, String[][] columns, HibernateReturningResult<?> returningResult) {
        // We do all this wrapping to change the StatementPreparer that is returned by the JdbcCoordinator
        // Instead of calling executeQuery, we delegate to executeUpdate and then return the generated keys in the prepared statement wrapper that we apply
        Object transactionCoordinator = hibernateAccess.getTransactionCoordinator(session);
        JdbcCoordinator jdbcCoordinator = (JdbcCoordinator) ExpressionUtils.getValue(transactionCoordinator, "jdbcCoordinator");
        
        Object jdbcCoordinatorProxy = Proxy.newProxyInstance(jdbcCoordinator.getClass().getClassLoader(), new Class[]{ JdbcCoordinator.class }, new JdbcCoordinatorInvocationHandler(jdbcCoordinator, session.getFactory(), generatedKeys, columns, returningResult));
        Object transactionCoordinatorProxy = Proxy.newProxyInstance(transactionCoordinator.getClass().getClassLoader(), new Class[]{ TransactionCoordinator.class }, new TransactionCoordinatorInvocationHandler(transactionCoordinator, jdbcCoordinatorProxy));
        Object sessionProxy = Proxy.newProxyInstance(session.getClass().getClassLoader(), new Class[]{ SessionImplementor.class, EventSource.class }, new SessionInvocationHandler(session, transactionCoordinatorProxy));
        return (SessionImplementor) sessionProxy;
    }
    
    @SuppressWarnings("unchecked")
    private List<QueryParamEntry> getQueryParamEntries(EntityManager em, List<Query> queries) {
        SessionImplementor session = em.unwrap(SessionImplementor.class);
        SessionFactoryImplementor sfi = session.getFactory();
        List<QueryParamEntry> result = new ArrayList<QueryParamEntry>(queries.size());
        Deque<Query> queryQueue = new LinkedList<Query>(queries);
        
        while (queryQueue.size() > 0) {
            Query q = queryQueue.remove();
            if (q instanceof CteQueryWrapper) {
                List<Query> participatingQueries = ((CteQueryWrapper) q).getParticipatingQueries();
                for (int i = participatingQueries.size() - 1; i > -1; i--) {
                    queryQueue.addFirst(participatingQueries.get(i));
                }
                continue;
            }
            
            org.hibernate.Query hibernateQuery = q.unwrap(org.hibernate.Query.class);
            hibernateQuery.setResultTransformer(null);
            String queryString = hibernateQuery.getQueryString();
            
            HQLQueryPlan queryPlan = sfi.getQueryPlanCache().getHQLQueryPlan(queryString, false, Collections.emptyMap());
            QueryTranslator queryTranslator = queryPlan.getTranslators()[0];
            QueryParameters queryParameters;
            List<ParameterSpecification> specifications;
            
            try {
                Method getNamedParams = ReflectionUtils.getGetter(hibernateQuery.getClass(), "namedParams");
                getNamedParams.setAccessible(true);
                Map<String, TypedValue> namedParams = (Map<String, TypedValue>) getNamedParams.invoke(hibernateQuery);
                
                queryParameters = ((org.hibernate.internal.AbstractQueryImpl) hibernateQuery).getQueryParameters(namedParams);

                Field collectedParameterSpecificationsField = ReflectionUtils.getField(queryTranslator.getClass(), "collectedParameterSpecifications");
                collectedParameterSpecificationsField.setAccessible(true);
                specifications = (List<ParameterSpecification>) collectedParameterSpecificationsField.get(queryTranslator);
                
                // This only happens for modification queries
                if (specifications == null) {
                    Field statementExectuor = ReflectionUtils.getField(queryTranslator.getClass(), "statementExecutor");
                    statementExectuor.setAccessible(true);
                    BasicExecutor executor = (BasicExecutor) statementExectuor.get(queryTranslator);
                    
                    Field parameterSpecifications = ReflectionUtils.getField(executor.getClass(), "parameterSpecifications");
                    parameterSpecifications.setAccessible(true);
                    specifications = (List<ParameterSpecification>) parameterSpecifications.get(executor);
                }
            } catch (Exception e1) {
                throw new RuntimeException(e1);
            }
            
            result.add(new QueryParamEntry(queryParameters, specifications));
        }
        
        return result;
    }
    
    private QueryTranslator prepareQueryPlan(HQLQueryPlan queryPlan, QueryParamEntry queryParametersEntry, String finalSql) {
        try {
            QueryTranslator queryTranslator = queryPlan.getTranslators()[0];
            // Override the sql in the query plan
            Field sqlField = ReflectionUtils.getField(queryTranslator.getClass(), "sql");
            sqlField.setAccessible(true);
            sqlField.set(queryTranslator, finalSql);
            
            // Prepare queryTranslator for aggregated parameters
            ParameterTranslations translations = new ParameterTranslationsImpl(queryParametersEntry.specifications);
            Field paramTranslationsField = ReflectionUtils.getField(queryTranslator.getClass(), "paramTranslations");
            paramTranslationsField.setAccessible(true);
            paramTranslationsField.set(queryTranslator, translations);
            
            Field collectedParameterSpecificationsField = ReflectionUtils.getField(queryTranslator.getClass(), "collectedParameterSpecifications");
            collectedParameterSpecificationsField.setAccessible(true);
            collectedParameterSpecificationsField.set(queryTranslator, queryParametersEntry.specifications);

            return queryTranslator;
        } catch (Exception e1) {
            throw new RuntimeException(e1);
        }
    }
    
    private HQLQueryPlan getQueryPlan(SessionFactoryImplementor sfi, Query query, String cacheKey) {
        BoundedConcurrentHashMap<String, HQLQueryPlan> queryPlanCache = getQueryPlanCache(sfi);
        HQLQueryPlan queryPlan = queryPlanCache.get(cacheKey);
        if (queryPlan == null) {
            queryPlan = createQueryPlan(sfi, query);
            HQLQueryPlan oldQueryPlan = queryPlanCache.putIfAbsent(cacheKey, queryPlan);
            if (oldQueryPlan != null) {
                queryPlan = oldQueryPlan;
            }
        }
        
        return queryPlan;
    }
    
    private HQLQueryPlan createQueryPlan(SessionFactoryImplementor sfi, Query query) {
        org.hibernate.Query hibernateQuery = query.unwrap(org.hibernate.Query.class);
        String queryString = hibernateQuery.getQueryString();
        return new HQLQueryPlan( queryString, false, Collections.emptyMap(), sfi);
    }
    
    private BoundedConcurrentHashMap<String, HQLQueryPlan> getQueryPlanCache(SessionFactoryImplementor sfi) {
        BoundedConcurrentHashMap<String, HQLQueryPlan> queryPlanCache = queryPlanCachesCache.get(sfi);
        if (queryPlanCache == null) {
            queryPlanCache = new BoundedConcurrentHashMap<String, HQLQueryPlan>(QueryPlanCache.DEFAULT_QUERY_PLAN_MAX_COUNT, 20, BoundedConcurrentHashMap.Eviction.LIRS);
            BoundedConcurrentHashMap<String, HQLQueryPlan> oldQueryPlanCache = queryPlanCachesCache.putIfAbsent(sfi, queryPlanCache);
            if (oldQueryPlanCache != null) {
                queryPlanCache = oldQueryPlanCache;
            }
        }
        
        return queryPlanCache;
    }
    
    private static class QueryParamEntry {
        QueryParameters queryParameters;
        List<ParameterSpecification> specifications;
        
        public QueryParamEntry(QueryParameters queryParameters, List<ParameterSpecification> specifications) {
            this.queryParameters = queryParameters;
            this.specifications = specifications;
        }
    }
	
	private HibernateEntityManagerImplementor getEntityManager(EntityManager em) {
		return (HibernateEntityManagerImplementor) em.unwrap(EntityManager.class);
	}

}
