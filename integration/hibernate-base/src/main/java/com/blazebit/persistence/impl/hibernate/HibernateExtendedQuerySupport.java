package com.blazebit.persistence.impl.hibernate;

import java.io.Serializable;
import java.lang.reflect.Field;
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
import org.hibernate.hql.internal.ast.tree.SelectClause;
import org.hibernate.hql.internal.classic.ParserHelper;
import org.hibernate.hql.spi.ParameterTranslations;
import org.hibernate.hql.spi.QueryTranslator;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.internal.util.collections.BoundedConcurrentHashMap;
import org.hibernate.loader.hql.QueryLoader;
import org.hibernate.param.ParameterSpecification;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.hibernate.type.ManyToOneType;
import org.hibernate.type.Type;

import com.blazebit.apt.service.ServiceProvider;
import com.blazebit.persistence.CommonQueryBuilder;
import com.blazebit.persistence.ReturningResult;
import com.blazebit.persistence.spi.CteQueryWrapper;
import com.blazebit.persistence.spi.DbmsDialect;
import com.blazebit.persistence.spi.ExtendedQuerySupport;
import com.blazebit.reflection.ReflectionUtils;

@ServiceProvider(ExtendedQuerySupport.class)
public class HibernateExtendedQuerySupport implements ExtendedQuerySupport {

    private static final Logger LOG = Logger.getLogger(HibernateExtendedQuerySupport.class.getName());
    
    private final ConcurrentMap<SessionFactoryImplementor, BoundedConcurrentHashMap<QueryPlanCacheKey, HQLQueryPlan>> queryPlanCachesCache = new ConcurrentHashMap<SessionFactoryImplementor, BoundedConcurrentHashMap<QueryPlanCacheKey, HQLQueryPlan>>();
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
    public List<String> getCascadingDeleteSql(EntityManager em, Query query) {
        SessionImplementor session = em.unwrap(SessionImplementor.class);
        HQLQueryPlan queryPlan = getOriginalQueryPlan(session, query);
        if (queryPlan.getTranslators().length > 1) {
            throw new IllegalArgumentException("No support for multiple translators yet!");
        }
        QueryTranslator queryTranslator = queryPlan.getTranslators()[0];
        BasicExecutor executor = getStatementExecutor(queryTranslator);
        
        return getField(executor, "deletes");
    }
    
    private HQLQueryPlan getOriginalQueryPlan(SessionImplementor session, Query query) {
        SessionFactoryImplementor sfi = session.getFactory();
        org.hibernate.Query hibernateQuery = query.unwrap(org.hibernate.Query.class);
        
        Map<String, TypedValue> namedParams = new HashMap<String, TypedValue>(hibernateAccess.getNamedParams(hibernateQuery));
        String queryString = hibernateAccess.expandParameterLists(session, hibernateQuery, namedParams);
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
        if (plan.getTranslators().length > 1) {
            throw new IllegalArgumentException("No support for multiple translators yet!");
        }
        QueryTranslator translator = plan.getTranslators()[0];
        
        QueryNode queryNode = getField(translator, "sqlAst");
        FromElement fromElement = queryNode.getFromClause().getFromElement(alias);
        
        if (fromElement == null) {
            throw new IllegalArgumentException("The alias " + alias + " could not be found in the query: " + query);
        }
        
        return fromElement.getTableAlias();
    }

    @Override
    public int getSqlSelectAliasPosition(EntityManager em, Query query, String alias) {
        SessionImplementor session = em.unwrap(SessionImplementor.class);
        HQLQueryPlan plan = getOriginalQueryPlan(session, query);
        if (plan.getTranslators().length > 1) {
            throw new IllegalArgumentException("No support for multiple translators yet!");
        }
        QueryTranslator translator = plan.getTranslators()[0];

        try {
            QueryNode queryNode = getField(translator, "sqlAst");
            
            String[] aliases = queryNode.getSelectClause().getQueryReturnAliases();

            for (int i = 0; i < aliases.length; i++) {
                if (alias.equals(aliases[i])) {
                    // the ordinal is 1 based
                    return i + 1;
                }
            }
            
            return -1;
        } catch (Exception e1) {
            throw new RuntimeException(e1);
        }
    }
    
    @Override
    public int getSqlSelectAttributePosition(EntityManager em, Query query, String expression) {
        if (expression.contains(".")) {
            // TODO: implement
            throw new UnsupportedOperationException("Embeddables are not yet supported!");
        }
        
        SessionImplementor session = em.unwrap(SessionImplementor.class);
        HQLQueryPlan plan = getOriginalQueryPlan(session, query);
        if (plan.getTranslators().length > 1) {
            throw new IllegalArgumentException("No support for multiple translators yet!");
        }
        QueryTranslator translator = plan.getTranslators()[0];

        try {
            QueryNode queryNode = getField(translator, "sqlAst");
            SelectClause selectClause = queryNode.getSelectClause();
            Type[] queryReturnTypes = selectClause.getQueryReturnTypes();
            
            boolean found = false;
            // The ordinal is 1 based
            int position = 1;
            for (int i = 0; i < queryReturnTypes.length; i++) {
                Type t = queryReturnTypes[i];
                if (t instanceof ManyToOneType) {
                    ManyToOneType manyToOneType = (ManyToOneType) t;
                    AbstractEntityPersister persister = (AbstractEntityPersister) session.getFactory().getEntityPersister(manyToOneType.getAssociatedEntityName());
                    
                    int propertyIndex = persister.getPropertyIndex(expression);
                    found = true;
                    for (int j = 0; j < propertyIndex; j++) {
                        position += persister.getPropertyColumnNames(j).length;
                    }
                    // Increment to the actual property position
                    position++;
                } else {
                    position++;
                }
            }
            
            if (found) {
                return position;
            }
            
            return -1;
        } catch (Exception e1) {
            throw new RuntimeException(e1);
        }
    }

    @Override
    @SuppressWarnings("rawtypes")
	public List getResultList(CommonQueryBuilder<?> cqb, List<Query> participatingQueries, Query query, String sqlOverride) {
        EntityManager em = cqb.getService(EntityManager.class);
		try {
			return list(em, participatingQueries, query, sqlOverride);
		} catch (QueryExecutionRequestException he) {
            LOG.severe("Could not execute the following SQL query: " + sqlOverride);
			throw new IllegalStateException(he);
		} catch(TypeMismatchException e) {
            LOG.severe("Could not execute the following SQL query: " + sqlOverride);
			throw new IllegalArgumentException(e);
		} catch (HibernateException he) {
		    LOG.severe("Could not execute the following SQL query: " + sqlOverride);
			throw getEntityManager(em).convert(he);
		}
	}
	
	@Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public Object getSingleResult(CommonQueryBuilder<?> cqb, List<Query> participatingQueries, Query query, String sqlOverride) {
        EntityManager em = cqb.getService(EntityManager.class);
		try {
			final List result = list(em, participatingQueries, query, sqlOverride);

			if (result.size() == 0) {
				NoResultException nre = new NoResultException("No entity found for query");
				getEntityManager(em).handlePersistenceException(nre);
				throw nre;
			} else if (result.size() > 1) {
				final Set uniqueResult = new HashSet(result);
				if (uniqueResult.size() > 1) {
					NonUniqueResultException nure = new NonUniqueResultException("result returns more than one element");
					getEntityManager(em).handlePersistenceException(nure);
					throw nure;
				} else {
					return uniqueResult.iterator().next();
				}
			} else {
				return result.get(0);
			}
		} catch (QueryExecutionRequestException he) {
            LOG.severe("Could not execute the following SQL query: " + sqlOverride);
			throw new IllegalStateException(he);
		} catch(TypeMismatchException e) {
            LOG.severe("Could not execute the following SQL query: " + sqlOverride);
			throw new IllegalArgumentException(e);
		} catch (HibernateException he) {
            LOG.severe("Could not execute the following SQL query: " + sqlOverride);
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

        QueryPlanCacheKey cacheKey = createCacheKey(participatingQueries);
        CacheEntry<HQLQueryPlan> queryPlanEntry = getQueryPlan(sfi, query, cacheKey);
        HQLQueryPlan queryPlan = queryPlanEntry.getValue();

        // Create combined query parameters
        QueryParamEntry queryParametersEntry = createQueryParameters(em, participatingQueries);
        QueryParameters queryParameters = queryParametersEntry.queryParameters;
        
        if (!queryPlanEntry.isFromCache()) {
            prepareQueryPlan(queryPlan, queryParametersEntry.specifications, finalSql, session, participatingQueries.get(participatingQueries.size() - 1), false);
            queryPlan = putQueryPlanIfAbsent(sfi, cacheKey, queryPlan);
        }
        
        return hibernateAccess.performList(queryPlan, session, queryParameters);
    }

    @Override
    public int executeUpdate(CommonQueryBuilder<?> cqb, List<Query> participatingQueries, Query query, String finalSql) {
        EntityManager em = cqb.getService(EntityManager.class);
        SessionImplementor session = em.unwrap(SessionImplementor.class);
        SessionFactoryImplementor sfi = session.getFactory();

        if (session.isClosed()) {
            throw new PersistenceException("Entity manager is closed!");
        }

        QueryPlanCacheKey cacheKey = createCacheKey(participatingQueries);
        CacheEntry<HQLQueryPlan> queryPlanEntry = getQueryPlan(sfi, query, cacheKey);
        HQLQueryPlan queryPlan = queryPlanEntry.getValue();
        
        // Create combined query parameters
        QueryParamEntry queryParametersEntry = createQueryParameters(em, participatingQueries);
        QueryParameters queryParameters = queryParametersEntry.queryParameters;

        if (!queryPlanEntry.isFromCache()) {
            prepareQueryPlan(queryPlan, queryParametersEntry.specifications, finalSql, session, participatingQueries.get(participatingQueries.size() - 1), true);
            queryPlan = putQueryPlanIfAbsent(sfi, cacheKey, queryPlan);
        }
        
        if (queryPlan.getReturnMetadata() == null) {
            return hibernateAccess.performExecuteUpdate(queryPlan, session, queryParameters);
        }

        boolean caseInsensitive = !Boolean.valueOf(cqb.getProperties().get("com.blazebit.persistence.returning_clause_case_sensitive"));
        String exampleQuerySql = queryPlan.getSqlStrings()[0];
        String[][] returningColumns = getReturningColumns(caseInsensitive, exampleQuerySql);
        
        try {
            @SuppressWarnings("unchecked")
            List<Object> results = hibernateAccess.performList(queryPlan, wrapSession(session, true, returningColumns, null), queryParameters);
            
            if (results.size() != 1) {
                throw new IllegalArgumentException("Expected size 1 but was: " + results.size());
            }
            
            Number count = (Number) results.get(0);
            return count.intValue();
        } catch (QueryExecutionRequestException he) {
            LOG.severe("Could not execute the following SQL query: " + finalSql);
            throw new IllegalStateException(he);
        } catch(TypeMismatchException e) {
            LOG.severe("Could not execute the following SQL query: " + finalSql);
            throw new IllegalArgumentException(e);
        } catch (HibernateException he) {
            LOG.severe("Could not execute the following SQL query: " + finalSql);
            getEntityManager(em).throwPersistenceException(he);
            return 0;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public ReturningResult<Object[]> executeReturning(CommonQueryBuilder<?> cqb, List<Query> participatingQueries, Query exampleQuery, String sqlOverride) {
        DbmsDialect dialect = cqb.getService(DbmsDialect.class);
        EntityManager em = cqb.getService(EntityManager.class);
        SessionImplementor session = em.unwrap(SessionImplementor.class);
        SessionFactoryImplementor sfi = session.getFactory();

        if (session.isClosed()) {
            throw new PersistenceException("Entity manager is closed!");
        }
        
        // Create plan for example query
        QueryPlanCacheKey cacheKey = createCacheKey(participatingQueries);
        CacheEntry<HQLQueryPlan> queryPlanEntry = getQueryPlan(sfi, exampleQuery, cacheKey);
        HQLQueryPlan queryPlan = queryPlanEntry.getValue();
        String exampleQuerySql = queryPlan.getSqlStrings()[0];
        
        StringBuilder sqlSb = new StringBuilder(sqlOverride.length() + 100);
        sqlSb.append(sqlOverride);

        boolean caseInsensitive = !Boolean.valueOf(cqb.getProperties().get("com.blazebit.persistence.returning_clause_case_sensitive"));
        String[][] returningColumns = getReturningColumns(caseInsensitive, exampleQuerySql);
        boolean generatedKeys = !dialect.supportsReturningColumns();
        String finalSql = sqlSb.toString();
        
        // Create combined query parameters
        QueryParamEntry queryParametersEntry = createQueryParameters(em, participatingQueries);
        QueryParameters queryParameters = queryParametersEntry.queryParameters;
        
        try {
            HibernateReturningResult<Object[]> returningResult = new HibernateReturningResult<Object[]>();
            if (!queryPlanEntry.isFromCache()) {
                prepareQueryPlan(queryPlan, queryParametersEntry.specifications, finalSql, session, participatingQueries.get(participatingQueries.size() - 1), true);
                queryPlan = putQueryPlanIfAbsent(sfi, cacheKey, queryPlan);
            }

            if (queryPlan.getTranslators().length > 1) {
                throw new IllegalArgumentException("No support for multiple translators yet!");
            }

            QueryTranslator queryTranslator = queryPlan.getTranslators()[0];
            
            // Extract query loader for native listing
            QueryLoader queryLoader = getField(queryTranslator, "queryLoader");
            
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
                results = hibernateAccess.list(queryLoader, wrapSession(session, generatedKeys, returningColumns, returningResult), queryParameters);
                success = true;
            } catch (QueryExecutionRequestException he) {
                LOG.severe("Could not execute the following SQL query: " + finalSql);
                throw new IllegalStateException(he);
            } catch(TypeMismatchException e) {
                LOG.severe("Could not execute the following SQL query: " + finalSql);
                throw new IllegalArgumentException(e);
            } catch (HibernateException he) {
                LOG.severe("Could not execute the following SQL query: " + finalSql);
                throw getEntityManager(em).convert(he);
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

    private static String[][] getReturningColumns(boolean caseInsensitive, String exampleQuerySql) {
        int fromIndex = exampleQuerySql.indexOf("from");
        int selectIndex = exampleQuerySql.indexOf("select");
        String[] selectItems = splitSelectItems(exampleQuerySql.subSequence(selectIndex + "select".length() + 1, fromIndex));
        String[][] returningColumns = new String[selectItems.length][2];
        
        for (int i = 0; i < selectItems.length; i++) {
            String selectItemWithAlias = selectItems[i].substring(selectItems[i].lastIndexOf('.') + 1);
            if (caseInsensitive) {
            	returningColumns[i][0] = selectItemWithAlias.substring(0, selectItemWithAlias.indexOf(' ')).toLowerCase();
            } else {
            	returningColumns[i][0] = selectItemWithAlias.substring(0, selectItemWithAlias.indexOf(' '));
            }
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
            
            Map<String, TypedValue> namedParams = new HashMap<String, TypedValue>(hibernateAccess.getNamedParams(hibernateQuery));
            String queryString = hibernateAccess.expandParameterLists(session, hibernateQuery, namedParams);
            
            HQLQueryPlan queryPlan = sfi.getQueryPlanCache().getHQLQueryPlan(queryString, false, Collections.emptyMap());

            if (queryPlan.getTranslators().length > 1) {
                throw new IllegalArgumentException("No support for multiple translators yet!");
            }
            QueryTranslator queryTranslator = queryPlan.getTranslators()[0];
            QueryParameters queryParameters;
            List<ParameterSpecification> specifications;
            
            try {
                queryParameters = hibernateAccess.getQueryParameters(hibernateQuery, namedParams);
                specifications = getField(queryTranslator, "collectedParameterSpecifications");
                
                // This only happens for modification queries
                if (specifications == null) {
                    BasicExecutor executor = getStatementExecutor(queryTranslator);
                    specifications = getField(executor, "parameterSpecifications");
                }
            } catch (Exception e1) {
                throw new RuntimeException(e1);
            }
            
            result.add(new QueryParamEntry(queryParameters, specifications));
        }
        
        return result;
    }
    
    private BasicExecutor getStatementExecutor(QueryTranslator queryTranslator) {
        return getField(queryTranslator, "statementExecutor");
    }
    
    private QueryTranslator prepareQueryPlan(HQLQueryPlan queryPlan, List<ParameterSpecification> queryParameterSpecifications, String finalSql, SessionImplementor session, Query lastQuery, boolean isModification) {
        try {
            if (queryPlan.getTranslators().length > 1) {
                throw new IllegalArgumentException("No support for multiple translators yet!");
            }

            QueryTranslator queryTranslator = queryPlan.getTranslators()[0];
            // Override the sql in the query plan
            setField(queryTranslator, "sql", finalSql);

            // Modification queries keep the sql in the executor
            StatementExecutor executor = null;
            
            Field statementExectuor = null;
            boolean madeAccessible = false;
            
            try {
                statementExectuor = ReflectionUtils.getField(queryTranslator.getClass(), "statementExecutor");
                madeAccessible = !statementExectuor.isAccessible();
                
                if (madeAccessible) {
                    statementExectuor.setAccessible(true);
                }
                
                executor = (StatementExecutor) statementExectuor.get(queryTranslator);
                
                if (executor == null && isModification) {
                    // We have to set an executor
                    org.hibernate.Query lastHibernateQuery = lastQuery.unwrap(org.hibernate.Query.class);
                    lastHibernateQuery.setResultTransformer(null);
                    
                    Map<String, TypedValue> namedParams = new HashMap<String, TypedValue>(hibernateAccess.getNamedParams(lastHibernateQuery));
                    String queryString = hibernateAccess.expandParameterLists(session, lastHibernateQuery, namedParams);
                    
                    // Extract the executor from the last query which is the actual main query
                    HQLQueryPlan lastQueryPlan = session.getFactory().getQueryPlanCache().getHQLQueryPlan(queryString, false, Collections.emptyMap());
                    if (lastQueryPlan.getTranslators().length > 1) {
                        throw new IllegalArgumentException("No support for multiple translators yet!");
                    }
                    QueryTranslator lastQueryTranslator = lastQueryPlan.getTranslators()[0];
                    executor = (StatementExecutor) statementExectuor.get(lastQueryTranslator);
                    // Now we use this executor for our example query
                    statementExectuor.set(queryTranslator, executor);
                }
            } finally {
                if (madeAccessible) {
                    statementExectuor.setAccessible(false);
                }
            }
            
            if (executor != null) {
                setField(executor, "sql", finalSql);
                setField(executor, "parameterSpecifications", queryParameterSpecifications);
                
                if (executor instanceof DeleteExecutor) {
                    setField(executor, "deletes", new ArrayList<String>());
                }
            }
            
            // Prepare queryTranslator for aggregated parameters
            ParameterTranslations translations = new ParameterTranslationsImpl(queryParameterSpecifications);
            setField(queryTranslator, "paramTranslations", translations);
            setField(queryTranslator, "collectedParameterSpecifications", queryParameterSpecifications);

            return queryTranslator;
        } catch (Exception e1) {
            throw new RuntimeException(e1);
        }
    }
    
    private CacheEntry<HQLQueryPlan> getQueryPlan(SessionFactoryImplementor sfi, Query query, QueryPlanCacheKey cacheKey) {
        BoundedConcurrentHashMap<QueryPlanCacheKey, HQLQueryPlan> queryPlanCache = getQueryPlanCache(sfi);
        HQLQueryPlan queryPlan = queryPlanCache.get(cacheKey);
        boolean fromCache = true;
        if (queryPlan == null) {
            fromCache = false;
            queryPlan = createQueryPlan(sfi, query);
        }
        
        return new CacheEntry<HQLQueryPlan>(queryPlan, fromCache);
    }
    
    private HQLQueryPlan putQueryPlanIfAbsent(SessionFactoryImplementor sfi, QueryPlanCacheKey cacheKey, HQLQueryPlan queryPlan) {
        BoundedConcurrentHashMap<QueryPlanCacheKey, HQLQueryPlan> queryPlanCache = getQueryPlanCache(sfi);
        HQLQueryPlan oldQueryPlan = queryPlanCache.putIfAbsent(cacheKey, queryPlan);
        if (oldQueryPlan != null) {
            queryPlan = oldQueryPlan;
        }
        
        return queryPlan;
    }
    
    private HQLQueryPlan createQueryPlan(SessionFactoryImplementor sfi, Query query) {
        org.hibernate.Query hibernateQuery = query.unwrap(org.hibernate.Query.class);
        String queryString = hibernateQuery.getQueryString();
        return new HQLQueryPlan(queryString, false, Collections.emptyMap(), sfi);
    }
    
    private BoundedConcurrentHashMap<QueryPlanCacheKey, HQLQueryPlan> getQueryPlanCache(SessionFactoryImplementor sfi) {
        BoundedConcurrentHashMap<QueryPlanCacheKey, HQLQueryPlan> queryPlanCache = queryPlanCachesCache.get(sfi);
        if (queryPlanCache == null) {
            queryPlanCache = new BoundedConcurrentHashMap<QueryPlanCacheKey, HQLQueryPlan>(QueryPlanCache.DEFAULT_QUERY_PLAN_MAX_COUNT, 20, BoundedConcurrentHashMap.Eviction.LIRS);
            BoundedConcurrentHashMap<QueryPlanCacheKey, HQLQueryPlan> oldQueryPlanCache = queryPlanCachesCache.putIfAbsent(sfi, queryPlanCache);
            if (oldQueryPlanCache != null) {
                queryPlanCache = oldQueryPlanCache;
            }
        }
        
        return queryPlanCache;
    }
    
    private QueryPlanCacheKey createCacheKey(List<Query> queries) {
        List<String> parts = new ArrayList<String>(queries.size());
        addAll(queries, parts);
        return new QueryPlanCacheKey(parts);
    }
    
    private void addAll(List<Query> queries, List<String> parts) {
        for (int i = 0; i< queries.size(); i++) {
            Query q = queries.get(i);
            
            if (q instanceof CteQueryWrapper) {
                addAll(((CteQueryWrapper) q).getParticipatingQueries(), parts);
            } else {
                parts.add(q.unwrap(org.hibernate.Query.class).getQueryString());
            }
        }
    }
    
    private static class QueryParamEntry {
        final QueryParameters queryParameters;
        final List<ParameterSpecification> specifications;
        
        public QueryParamEntry(QueryParameters queryParameters, List<ParameterSpecification> specifications) {
            this.queryParameters = queryParameters;
            this.specifications = specifications;
        }
    }
    
    private static class QueryPlanCacheKey {
        final List<String> cacheKeyParts;

        public QueryPlanCacheKey(List<String> cacheKeyParts) {
            this.cacheKeyParts = cacheKeyParts;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((cacheKeyParts == null) ? 0 : cacheKeyParts.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            QueryPlanCacheKey other = (QueryPlanCacheKey) obj;
            if (cacheKeyParts == null) {
                if (other.cacheKeyParts != null)
                    return false;
            } else if (!cacheKeyParts.equals(other.cacheKeyParts))
                return false;
            return true;
        }
        
    }
    
    private static class CacheEntry<T> {
        
        private final T value;
        private final boolean fromCache;
        
        public CacheEntry(T value, boolean fromCache) {
            this.value = value;
            this.fromCache = fromCache;
        }

        public T getValue() {
            return value;
        }

        public boolean isFromCache() {
            return fromCache;
        }
    }
	
	private HibernateEntityManagerImplementor getEntityManager(EntityManager em) {
		return (HibernateEntityManagerImplementor) em.unwrap(EntityManager.class);
	}
    
    @SuppressWarnings("unchecked")
    private <T> T getField(Object object, String field) {
        boolean madeAccessible = false;
        Field f = null;
        try {
            f = ReflectionUtils.getField(object.getClass(), field);
            madeAccessible = !f.isAccessible();
            
            if (madeAccessible) {
                f.setAccessible(true);
            }
            return (T) f.get(object);
        } catch (Exception e1) {
            throw new RuntimeException(e1);
        } finally {
            if (madeAccessible) {
                f.setAccessible(false);
            }
        }
    }
    
    private void setField(Object object, String field, Object value) {
        boolean madeAccessible = false;
        Field f = null;
        try {
            f = ReflectionUtils.getField(object.getClass(), field);
            madeAccessible = !f.isAccessible();
            
            if (madeAccessible) {
                f.setAccessible(true);
            }
            
            f.set(object, value);
        } catch (Exception e1) {
            throw new RuntimeException(e1);
        } finally {
            if (madeAccessible) {
                f.setAccessible(false);
            }
        }
    }

}
