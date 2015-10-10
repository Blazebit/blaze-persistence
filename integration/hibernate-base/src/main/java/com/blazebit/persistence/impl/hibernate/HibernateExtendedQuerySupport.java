package com.blazebit.persistence.impl.hibernate;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
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
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.metamodel.EntityType;

import org.hibernate.HibernateException;
import org.hibernate.TypeMismatchException;
import org.hibernate.dialect.Dialect;
import org.hibernate.ejb.HibernateEntityManagerImplementor;
import org.hibernate.engine.query.spi.HQLQueryPlan;
import org.hibernate.engine.query.spi.ParameterMetadata;
import org.hibernate.engine.query.spi.QueryPlanCache;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.TypedValue;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.AutoFlushEvent;
import org.hibernate.event.spi.AutoFlushEventListener;
import org.hibernate.event.spi.EventSource;
import org.hibernate.event.spi.EventType;
import org.hibernate.hql.internal.QueryExecutionRequestException;
import org.hibernate.hql.internal.ast.ParameterTranslationsImpl;
import org.hibernate.hql.internal.ast.exec.BasicExecutor;
import org.hibernate.hql.internal.ast.exec.DeleteExecutor;
import org.hibernate.hql.internal.ast.exec.StatementExecutor;
import org.hibernate.hql.internal.ast.tree.FromElement;
import org.hibernate.hql.internal.ast.tree.QueryNode;
import org.hibernate.hql.internal.classic.ParserHelper;
import org.hibernate.hql.spi.ParameterTranslations;
import org.hibernate.hql.spi.QueryTranslator;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.internal.util.collections.BoundedConcurrentHashMap;
import org.hibernate.loader.hql.QueryLoader;
import org.hibernate.param.ParameterSpecification;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.hibernate.type.Type;

import com.blazebit.apt.service.ServiceProvider;
import com.blazebit.persistence.ReturningResult;
import com.blazebit.persistence.spi.CteQueryWrapper;
import com.blazebit.persistence.spi.DbmsDialect;
import com.blazebit.persistence.spi.ExtendedQuerySupport;
import com.blazebit.reflection.ReflectionUtils;

@ServiceProvider(ExtendedQuerySupport.class)
public class HibernateExtendedQuerySupport implements ExtendedQuerySupport {

    private static final Logger LOG = Logger.getLogger(HibernateExtendedQuerySupport.class.getName());
    
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
		HQLQueryPlan queryPlan = getOriginalQueryPlan(session, query);
		String sql = queryPlan.getSqlStrings()[0];
		return sql;
	}

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getCascadingDeleteSql(EntityManager em, Query query) {
        SessionImplementor session = em.unwrap(SessionImplementor.class);
        HQLQueryPlan queryPlan = getOriginalQueryPlan(session, query);
        QueryTranslator queryTranslator = queryPlan.getTranslators()[0];
        BasicExecutor executor = getStatementExecutor(queryTranslator);
        
        try {
            Field deletesField = ReflectionUtils.getField(executor.getClass(), "deletes");
            deletesField.setAccessible(true);
            return (List<String>) deletesField.get(executor);
        } catch (Exception e1) {
            throw new RuntimeException(e1);
        }
    }
    
    private HQLQueryPlan getOriginalQueryPlan(SessionImplementor session, Query query) {
        SessionFactoryImplementor sfi = session.getFactory();
        org.hibernate.Query hibernateQuery = query.unwrap(org.hibernate.Query.class);
        
        Map<String, TypedValue> namedParams = new HashMap<String, TypedValue>(getNamedParams(hibernateQuery));
        String queryString = expandParameterLists(session, hibernateQuery, namedParams);
        return sfi.getQueryPlanCache().getHQLQueryPlan(queryString, false, Collections.emptyMap());
    }

    @Override
	public String[] getColumnNames(EntityManager em, EntityType<?> entityType, String attributeName) {
        SessionImplementor session = em.unwrap(SessionImplementor.class);
        SessionFactoryImplementor sfi = session.getFactory();
	    return ((AbstractEntityPersister) sfi.getClassMetadata(entityType.getJavaType())).getPropertyColumnNames(attributeName);
	}

    @Override
    public String getSqlAlias(EntityManager em, Query query, String alias) {
        SessionImplementor session = em.unwrap(SessionImplementor.class);
        HQLQueryPlan plan = getOriginalQueryPlan(session, query);
        QueryTranslator translator = plan.getTranslators()[0];
        
        try {
            Field sqlAstField = ReflectionUtils.getField(translator.getClass(), "sqlAst");
            sqlAstField.setAccessible(true);
            QueryNode queryNode = (QueryNode) sqlAstField.get(translator);
            FromElement fromElement = queryNode.getFromClause().getFromElement(alias);
            
            if (fromElement == null) {
                throw new IllegalArgumentException("The alias " + alias + " could not be found in the query: " + query);
            }
            
            return fromElement.getTableAlias();
        } catch (Exception e1) {
            throw new RuntimeException(e1);
        }
    }

    @Override
    @SuppressWarnings("rawtypes")
	public List getResultList(DbmsDialect dialect, EntityManager em, List<Query> participatingQueries, Query query, String sqlOverride) {
		try {
			return list(em, participatingQueries, query, sqlOverride);
		} catch (QueryExecutionRequestException he) {
			throw new IllegalStateException(he);
		} catch(TypeMismatchException e) {
			throw new IllegalArgumentException(e);
		} catch (HibernateException he) {
			throw getEntityManager(em).convert(he);
		}
	}
	
	@Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public Object getSingleResult(DbmsDialect dialect, EntityManager em, List<Query> participatingQueries, Query query, String sqlOverride) {
		try {
			final List result = list(em, participatingQueries, query, sqlOverride);

			if (result.size() == 0) {
				NoResultException nre = new NoResultException("No entity found for query");
				getEntityManager(em).handlePersistenceException(nre);
				throw nre;
			} else if (result.size() > 1) {
				final Set uniqueResult = new HashSet(result);
				if (uniqueResult.size() > 1) {
					NonUniqueResultException nure = new NonUniqueResultException("result returns more than one elements");
					getEntityManager(em).handlePersistenceException(nure);
					throw nure;
				} else {
					return uniqueResult.iterator().next();
				}
			} else {
				return result.get(0);
			}
		} catch (QueryExecutionRequestException he) {
			throw new IllegalStateException(he);
		} catch(TypeMismatchException e) {
			throw new IllegalArgumentException(e);
		} catch (HibernateException he) {
			throw getEntityManager(em).convert(he);
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

        prepareQueryPlan(queryPlan, queryParametersEntry, finalSql, session, participatingQueries.get(participatingQueries.size() - 1), false);
        return queryPlan.performList(queryParameters, session);
    }

    @Override
    public int executeUpdate(DbmsDialect dialect, EntityManager em, List<Query> participatingQueries, Query query, String finalSql) {
        SessionImplementor session = em.unwrap(SessionImplementor.class);
        SessionFactoryImplementor sfi = session.getFactory();

        if (session.isClosed()) {
            throw new PersistenceException("Entity manager is closed!");
        }
        
        HQLQueryPlan queryPlan = getQueryPlan(sfi, query, finalSql);
        
        // Create combined query parameters
        QueryParamEntry queryParametersEntry = createQueryParameters(em, participatingQueries);
        QueryParameters queryParameters = queryParametersEntry.queryParameters;
        
        prepareQueryPlan(queryPlan, queryParametersEntry, finalSql, session, participatingQueries.get(participatingQueries.size() - 1), true);
        
        if (queryPlan.getReturnMetadata() == null) {
            return queryPlan.performExecuteUpdate(queryParameters, session);
        }

        String exampleQuerySql = queryPlan.getSqlStrings()[0];
        String[][] returningColumns = getReturningColumns(exampleQuerySql);
        
        @SuppressWarnings("unchecked")
        List<Object> results = queryPlan.performList(queryParameters, wrapSession(session, true, returningColumns, null));
        if (results.size() != 1) {
            throw new IllegalArgumentException("Expected size 1 but was: " + results.size());
        }
        
        Number count = (Number) results.get(0);
        return count.intValue();
    }

    @Override
    @SuppressWarnings("unchecked")
    public ReturningResult<Object[]> executeReturning(DbmsDialect dialect, EntityManager em, List<Query> participatingQueries, Query exampleQuery, String sqlOverride) {
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
        boolean generatedKeys = !dialect.supportsReturningColumns();
        String finalSql = sqlSb.toString();
        
        // Create combined query parameters
        QueryParamEntry queryParametersEntry = createQueryParameters(em, participatingQueries);
        QueryParameters queryParameters = queryParametersEntry.queryParameters;
        
        try {
            QueryTranslator queryTranslator = prepareQueryPlan(queryPlan, queryParametersEntry, finalSql, session, participatingQueries.get(participatingQueries.size() - 1), true);
            
            // Extract query loader for native listing
            Field queryLoaderField = ReflectionUtils.getField(queryTranslator.getClass(), "queryLoader");
            queryLoaderField.setAccessible(true);
            QueryLoader queryLoader = (QueryLoader) queryLoaderField.get(queryTranslator);
            HibernateReturningResult<Object[]> returningResult = new HibernateReturningResult<Object[]>();
            
            // Do the native list operation with custom session and combined parameters
            
            /*
             * NATIVE LIST START
             */
            hibernateAccess.checkTransactionSynchStatus(session);
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
                hibernateAccess.afterTransaction(session, success);
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

    private static String[][] getReturningColumns(String exampleQuerySql) {
        int fromIndex = exampleQuerySql.indexOf("from");
        int selectIndex = exampleQuerySql.indexOf("select");
        String[] selectItems = splitSelectItems(exampleQuerySql.subSequence(selectIndex + "select".length() + 1, fromIndex));
        String[][] returningColumns = new String[selectItems.length][2];
        
        for (int i = 0; i < selectItems.length; i++) {
            String selectItemWithAlias = selectItems[i].substring(selectItems[i].lastIndexOf('.') + 1);
            returningColumns[i][0] = selectItemWithAlias.substring(0, selectItemWithAlias.indexOf(' '));
            returningColumns[i][1] = selectItemWithAlias.substring(selectItemWithAlias.lastIndexOf(' ') + 1);
        }
        
        return returningColumns;
    }
    
    private static String[] splitSelectItems(CharSequence itemsString) {
        List<String> selectItems = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();
        int parenthesis = 0;
        boolean text = false;
        
        int i = 0;
        int length = itemsString.length();
        while (i < length) {
            char c = itemsString.charAt(i);
            
            if (text) {
                if (c == '(') {
                    parenthesis++;
                } else if (c == ')') {
                    parenthesis--;
                } else if (parenthesis == 0 && c == ',') {
                    selectItems.add(trim(sb));
                    sb.setLength(0);
                    text = false;
                    
                    i++;
                    continue;
                }
                
                sb.append(c);
            } else {
                if (Character.isWhitespace(c)) {
                    // skip whitespace
                } else {
                    sb.append(c);
                    text = true;
                }
            }
            
            i++;
        }
        
        if (text) {
            selectItems.add(trim(sb));
        }
        
        return selectItems.toArray(new String[selectItems.size()]);
    }

    private static String trim(StringBuilder sb) {
        int i = sb.length() - 1;
        while (i >= 0) {
            if (!Character.isWhitespace(sb.charAt(i))) {
                break;
            } else {
                i--;
            }
        }
        
        return sb.substring(0, i + 1);
    }

    private <E> Iterable<E> listeners(SessionFactoryImplementor factory, EventType<E> type) {
        return factory.getServiceRegistry().getService(EventListenerRegistry.class).getEventListenerGroup(type).listeners();
    }
    
    // TODO: needs to expand query params like AbstractQueryImpl and move parameters from namedParameterLists to namedParameters
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
        return hibernateAccess.wrapSession(session, generatedKeys, columns, returningResult);
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
            
            Map<String, TypedValue> namedParams = new HashMap<String, TypedValue>(getNamedParams(hibernateQuery));
            String queryString = expandParameterLists(session, hibernateQuery, namedParams);
            
            HQLQueryPlan queryPlan = sfi.getQueryPlanCache().getHQLQueryPlan(queryString, false, Collections.emptyMap());
            QueryTranslator queryTranslator = queryPlan.getTranslators()[0];
            QueryParameters queryParameters;
            List<ParameterSpecification> specifications;
            
            try {
                queryParameters = ((org.hibernate.internal.AbstractQueryImpl) hibernateQuery).getQueryParameters(namedParams);

                Field collectedParameterSpecificationsField = ReflectionUtils.getField(queryTranslator.getClass(), "collectedParameterSpecifications");
                collectedParameterSpecificationsField.setAccessible(true);
                specifications = (List<ParameterSpecification>) collectedParameterSpecificationsField.get(queryTranslator);
                
                // This only happens for modification queries
                if (specifications == null) {
                    BasicExecutor executor = getStatementExecutor(queryTranslator);
                    
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
    
    private BasicExecutor getStatementExecutor(QueryTranslator queryTranslator) {
        try {
            Field statementExectuor = ReflectionUtils.getField(queryTranslator.getClass(), "statementExecutor");
            statementExectuor.setAccessible(true);
            return (BasicExecutor) statementExectuor.get(queryTranslator);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, TypedValue> getNamedParams(org.hibernate.Query hibernateQuery) {
        try {
            Method getNamedParams = ReflectionUtils.getGetter(hibernateQuery.getClass(), "namedParams");
            getNamedParams.setAccessible(true);
            return (Map<String, TypedValue>) getNamedParams.invoke(hibernateQuery);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, TypedValue> getNamedParamLists(org.hibernate.Query hibernateQuery) {
        try {
            Method getNamedParamLists = ReflectionUtils.getGetter(hibernateQuery.getClass(), "namedParameterLists");
            getNamedParamLists.setAccessible(true);
            return (Map<String, TypedValue>) getNamedParamLists.invoke(hibernateQuery);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ParameterMetadata getParameterMetadata(org.hibernate.Query hibernateQuery) {
        try {
            Method getParameterMetadata = ReflectionUtils.getGetter(hibernateQuery.getClass(), "parameterMetadata");
            getParameterMetadata.setAccessible(true);
            return (ParameterMetadata) getParameterMetadata.invoke(hibernateQuery);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private QueryTranslator prepareQueryPlan(HQLQueryPlan queryPlan, QueryParamEntry queryParametersEntry, String finalSql, SessionImplementor session, Query lastQuery, boolean isModification) {
        try {
            QueryTranslator queryTranslator = queryPlan.getTranslators()[0];
            // Override the sql in the query plan
            Field sqlField = ReflectionUtils.getField(queryTranslator.getClass(), "sql");
            sqlField.setAccessible(true);
            sqlField.set(queryTranslator, finalSql);

            // Modification queries keep the sql in the executor
            Field statementExectuor = ReflectionUtils.getField(queryTranslator.getClass(), "statementExecutor");
            statementExectuor.setAccessible(true);
            StatementExecutor executor = (StatementExecutor) statementExectuor.get(queryTranslator);
            
            if (executor == null && isModification) {
                // We have to set an executor
                org.hibernate.Query lastHibernateQuery = lastQuery.unwrap(org.hibernate.Query.class);
                lastHibernateQuery.setResultTransformer(null);
                
                Map<String, TypedValue> namedParams = new HashMap<String, TypedValue>(getNamedParams(lastHibernateQuery));
                String queryString = expandParameterLists(session, lastHibernateQuery, namedParams);
                
                // Extract the executor from the last query which is the actual main query
                HQLQueryPlan lastQueryPlan = session.getFactory().getQueryPlanCache().getHQLQueryPlan(queryString, false, Collections.emptyMap());
                QueryTranslator lastQueryTranslator = lastQueryPlan.getTranslators()[0];
                executor = (StatementExecutor) statementExectuor.get(lastQueryTranslator);
                // Now we use this executor for our example query
                statementExectuor.set(queryTranslator, executor);
            }
            
            if (executor != null) {
                Field executorSqlField = ReflectionUtils.getField(executor.getClass(), "sql");
                executorSqlField.setAccessible(true);
                executorSqlField.set(executor, finalSql);
                
                Field parameterSpecifications = ReflectionUtils.getField(executor.getClass(), "parameterSpecifications");
                parameterSpecifications.setAccessible(true);
                parameterSpecifications.set(executor, queryParametersEntry.specifications);
                
                if (executor instanceof DeleteExecutor) {
                    Field deletesField = ReflectionUtils.getField(executor.getClass(), "deletes");
                    deletesField.setAccessible(true);
                    deletesField.set(executor, new ArrayList<String>());
                }
            }
            
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
    
    private String expandParameterLists(SessionImplementor session, org.hibernate.Query hibernateQuery, Map<String, TypedValue> namedParamsCopy) {
        String query = hibernateQuery.getQueryString();
        ParameterMetadata parameterMetadata = getParameterMetadata(hibernateQuery);
        Iterator<Map.Entry<String, TypedValue>> iter = getNamedParamLists(hibernateQuery).entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, TypedValue> me = iter.next();
            query = expandParameterList(session, parameterMetadata, query, (String) me.getKey(), (TypedValue) me.getValue(), namedParamsCopy);
        }
        return query;
    }

    private String expandParameterList(SessionImplementor session, ParameterMetadata parameterMetadata, String query, String name, TypedValue typedList, Map<String, TypedValue> namedParamsCopy) {
        Collection<?> vals = (Collection<?>) typedList.getValue();
        
        // HHH-1123
        // Some DBs limit number of IN expressions.  For now, warn...
        final Dialect dialect = session.getFactory().getDialect();
        final int inExprLimit = dialect.getInExpressionCountLimit();
        if (inExprLimit > 0 && vals.size() > inExprLimit) {
            LOG.warning(String.format("Dialect [%s] limits the number of elements in an IN predicate to %s entries.  " +
                    "However, the given parameter list [%s] contained %s entries, which will likely cause failures " +
                    "to execute the query in the database", dialect.getClass().getName(), inExprLimit, name, vals.size()));
        }

        Type type = typedList.getType();

        boolean isJpaPositionalParam = parameterMetadata.getNamedParameterDescriptor(name).isJpaStyle();
        String paramPrefix = isJpaPositionalParam ? "?" : ParserHelper.HQL_VARIABLE_PREFIX;
        String placeholder =
                new StringBuilder(paramPrefix.length() + name.length())
                        .append(paramPrefix).append(name)
                        .toString();

        if (query == null) {
            return query;
        }
        int loc = query.indexOf(placeholder);

        if (loc < 0) {
            return query;
        }

        String beforePlaceholder = query.substring(0, loc);
        String afterPlaceholder =  query.substring(loc + placeholder.length());

        // check if placeholder is already immediately enclosed in parentheses
        // (ignoring whitespace)
        boolean isEnclosedInParens =
                StringHelper.getLastNonWhitespaceCharacter(beforePlaceholder) == '(' &&
                StringHelper.getFirstNonWhitespaceCharacter(afterPlaceholder) == ')';

        if (vals.size() == 1  && isEnclosedInParens) {
            // short-circuit for performance when only 1 value and the
            // placeholder is already enclosed in parentheses...
            namedParamsCopy.put(name, new TypedValue(type, vals.iterator().next()));
            return query;
        }

        StringBuilder list = new StringBuilder(16);
        Iterator<?> iter = vals.iterator();
        int i = 0;
        while (iter.hasNext()) {
            // Variable 'name' can represent a number or contain digit at the end. Surrounding it with
            // characters to avoid ambiguous definition after concatenating value of 'i' counter.
            String alias = (isJpaPositionalParam ? 'x' + name : name) + '_' + i++ + '_';
            if (namedParamsCopy.put(alias, new TypedValue(type, iter.next())) != null) {
                throw new HibernateException("Repeated usage of alias '" + alias + "' while expanding list parameter.");
            }
            list.append(ParserHelper.HQL_VARIABLE_PREFIX).append(alias);
            if (iter.hasNext()) {
                list.append(", ");
            }
        }
        return StringHelper.replace(
                beforePlaceholder,
                afterPlaceholder,
                placeholder.toString(),
                list.toString(),
                true,
                true
        );
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
