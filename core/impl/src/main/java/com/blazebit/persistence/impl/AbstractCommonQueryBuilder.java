/*
 * Copyright 2014 Blazebit.
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
package com.blazebit.persistence.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.Parameter;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

import com.blazebit.persistence.BaseQueryBuilder;
import com.blazebit.persistence.CaseWhenStarterBuilder;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.FullSelectCTECriteriaBuilder;
import com.blazebit.persistence.HavingOrBuilder;
import com.blazebit.persistence.JoinOnBuilder;
import com.blazebit.persistence.JoinType;
import com.blazebit.persistence.Keyset;
import com.blazebit.persistence.KeysetBuilder;
import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.ReturningModificationCriteriaBuilderFactory;
import com.blazebit.persistence.SelectRecursiveCTECriteriaBuilder;
import com.blazebit.persistence.SimpleCaseWhenStarterBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.WhereOrBuilder;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.impl.expression.SubqueryExpressionFactory;
import com.blazebit.persistence.impl.expression.VisitorAdapter;
import com.blazebit.persistence.impl.jpaprovider.JpaProvider;
import com.blazebit.persistence.impl.jpaprovider.JpaProviders;
import com.blazebit.persistence.impl.keyset.KeysetBuilderImpl;
import com.blazebit.persistence.impl.keyset.KeysetImpl;
import com.blazebit.persistence.impl.keyset.KeysetLink;
import com.blazebit.persistence.impl.keyset.KeysetManager;
import com.blazebit.persistence.impl.keyset.KeysetMode;
import com.blazebit.persistence.impl.keyset.SimpleKeysetLink;
import com.blazebit.persistence.spi.DbmsDialect;
import com.blazebit.persistence.spi.DbmsModificationState;
import com.blazebit.persistence.spi.DbmsStatementType;
import com.blazebit.persistence.spi.QueryTransformer;

/**
 *
 * @param <T> The query result type
 * @param <X> The concrete builder type
 * @param <Z> The builder type that should be returned on set operations
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public abstract class AbstractCommonQueryBuilder<T, X, Z> {

    protected static final Logger LOG = Logger.getLogger(CriteriaBuilderImpl.class.getName());
    public static final String idParamName = "ids";

    protected final CriteriaBuilderFactoryImpl cbf;
    protected final EntityManager em;
    protected final DbmsStatementType statementType;
    protected final Map<Class<?>, Map<String, DbmsModificationState>> explicitVersionEntities = new HashMap<Class<?>, Map<String, DbmsModificationState>>(0);

    protected final ParameterManager parameterManager;
    protected final SelectManager<T> selectManager;
    protected final WhereManager<X> whereManager;
    protected final HavingManager<X> havingManager;
    protected final GroupByManager groupByManager;
    protected final OrderByManager orderByManager;
    protected final JoinManager joinManager;
    protected final KeysetManager keysetManager;
	protected final CTEManager<T> cteManager;
    protected final ResolvingQueryGenerator queryGenerator;
    protected final SubqueryInitiatorFactory subqueryInitFactory;

    protected final DbmsDialect dbmsDialect;
    protected final JpaProvider jpaProvider;
    protected final Set<String> registeredFunctions;

    protected final AliasManager aliasManager;
    protected final ExpressionFactory expressionFactory;

    private final List<ExpressionTransformer> transformers;
    private final SizeSelectToCountTransformer sizeSelectToCountTransformer;
    private final SizeSelectToSubqueryTransformer sizeSelectToSubqueryTransformer;

    // Mutable state
    protected Class<T> resultType;
    protected int firstResult = 0;
    protected int maxResults = Integer.MAX_VALUE;
    protected boolean fromClassExplicitelySet = false;

    private boolean needsCheck = true;
    private boolean implicitJoinsApplied = false;

    // Cache
    protected String cachedQueryString;
    protected String cachedCteQueryString;
    protected boolean hasGroupBy = false;

    /**
     * Create flat copy of builder
     *
     * @param builder
     */
    @SuppressWarnings("unchecked")
    protected AbstractCommonQueryBuilder(AbstractCommonQueryBuilder<T, ? extends BaseQueryBuilder<T, ?>, ?> builder) {
        this.cbf = builder.cbf;
        this.statementType = builder.statementType;
        this.orderByManager = builder.orderByManager;
        this.parameterManager = builder.parameterManager;
        this.selectManager = builder.selectManager;
        this.whereManager = (WhereManager<X>) builder.whereManager;
        this.havingManager = (HavingManager<X>) builder.havingManager;
        this.groupByManager = builder.groupByManager;
        this.keysetManager = builder.keysetManager;
        this.cteManager = builder.cteManager;
        this.joinManager = builder.joinManager;
        this.queryGenerator = builder.queryGenerator;
        this.em = builder.em;
        this.dbmsDialect = builder.dbmsDialect;
        this.jpaProvider = builder.jpaProvider;
        this.registeredFunctions = builder.registeredFunctions;
        this.subqueryInitFactory = builder.subqueryInitFactory;
        this.aliasManager = builder.aliasManager;
        this.expressionFactory = builder.expressionFactory;
        this.transformers = builder.transformers;
        this.resultType = builder.resultType;
        this.sizeSelectToCountTransformer = builder.sizeSelectToCountTransformer;
        this.sizeSelectToSubqueryTransformer = builder.sizeSelectToSubqueryTransformer;
    }

    protected AbstractCommonQueryBuilder(CriteriaBuilderFactoryImpl cbf, EntityManager em, DbmsStatementType statementType, DbmsDialect dbmsDialect, Class<T> resultClazz, String alias, ParameterManager parameterManager, AliasManager aliasManager, JoinManager parentJoinManager, ExpressionFactory expressionFactory, Set<String> registeredFunctions) {
        if (cbf == null) {
            throw new NullPointerException("criteriaBuilderFactory");
        }
        if (em == null) {
            throw new NullPointerException("entityManager");
        }
        if (resultClazz == null) {
            throw new NullPointerException("resultClazz");
        }

        this.cbf = cbf;
        this.statementType = statementType;
        this.jpaProvider = JpaProviders.resolveJpaProvider(em);
        this.dbmsDialect = dbmsDialect;
        this.aliasManager = new AliasManager(aliasManager);
        this.expressionFactory = expressionFactory;

        this.parameterManager = parameterManager;

        this.registeredFunctions = registeredFunctions;
        this.queryGenerator = new ResolvingQueryGenerator(this.aliasManager, this.jpaProvider, registeredFunctions);

        this.joinManager = new JoinManager(queryGenerator, parameterManager, null, expressionFactory, jpaProvider, this.aliasManager, em.getMetamodel(), parentJoinManager);

        // set defaults
        if (alias == null) {
        	alias = resultClazz.getSimpleName().toLowerCase();
        } else {
        	// If the user supplies an alias, the intention is clear
        	fromClassExplicitelySet = true;
        }
        
        try {
            this.joinManager.addRoot(em.getMetamodel().entity(resultClazz), alias);
        } catch (IllegalArgumentException ex) {
        	// the result class might not be an entity
        	if (fromClassExplicitelySet) {
        		// If the intention was to use that as from clause, we have to throw an exception
        		throw new IllegalArgumentException("The class [" + resultClazz.getName() + "] is not an entity and therefore can't be aliased!");
        	}
        }

        this.subqueryInitFactory = new SubqueryInitiatorFactory(cbf, em, dbmsDialect, parameterManager, this.aliasManager, joinManager, new SubqueryExpressionFactory(cbf.getAggregateFunctions()), registeredFunctions);

        this.joinManager.setSubqueryInitFactory(subqueryInitFactory);

        this.whereManager = new WhereManager<X>(queryGenerator, parameterManager, subqueryInitFactory, expressionFactory);
        this.havingManager = new HavingManager<X>(queryGenerator, parameterManager, subqueryInitFactory, expressionFactory);
        this.groupByManager = new GroupByManager(queryGenerator, parameterManager);

        this.selectManager = new SelectManager<T>(queryGenerator, parameterManager, this.joinManager, this.aliasManager, subqueryInitFactory, expressionFactory, jpaProvider, resultClazz);
        this.orderByManager = new OrderByManager(queryGenerator, parameterManager, this.aliasManager, jpaProvider);
        this.keysetManager = new KeysetManager(queryGenerator, parameterManager);
        this.cteManager = new CTEManager<T>(cbf, em, dbmsDialect, registeredFunctions, parameterManager);

        // resolve cyclic dependencies
        this.em = em;

        this.transformers = Arrays.asList(new OuterFunctionTransformer(joinManager), new SubqueryRecursiveExpressionTransformer());
        this.sizeSelectToCountTransformer = new SizeSelectToCountTransformer(joinManager, groupByManager, orderByManager, em.getMetamodel());
        this.sizeSelectToSubqueryTransformer = new SizeSelectToSubqueryTransformer(subqueryInitFactory, this.aliasManager);
        this.resultType = resultClazz;
    }

    public AbstractCommonQueryBuilder(CriteriaBuilderFactoryImpl cbf, EntityManager em, DbmsStatementType statementType, DbmsDialect dbmsDialect, Class<T> clazz, String alias, Set<String> registeredFunctions, ParameterManager parameterManager) {
        this(cbf, em, statementType, dbmsDialect, clazz, alias, parameterManager, null, null, cbf.getExpressionFactory(), registeredFunctions);
    }

    public AbstractCommonQueryBuilder(CriteriaBuilderFactoryImpl cbf, EntityManager em, DbmsStatementType statementType, DbmsDialect dbmsDialect, Class<T> clazz, String alias, Set<String> registeredFunctions) {
        this(cbf, em, statementType, dbmsDialect, clazz, alias, new ParameterManager(), null, null, cbf.getExpressionFactory(), registeredFunctions);
    }

    public CriteriaBuilderFactory getCriteriaBuilderFactory() {
        return cbf;
    }

	@SuppressWarnings("unchecked")
    public <Y> FullSelectCTECriteriaBuilder<Y, X> with(Class<Y> cteClass) {
        if (!dbmsDialect.supportsWithClause()) {
            throw new UnsupportedOperationException("The database does not support the with clause!");
        }
        
		return cteManager.with(cteClass, (X) this);
	}

    @SuppressWarnings("unchecked")
	public <Y> SelectRecursiveCTECriteriaBuilder<Y, X> withRecursive(Class<Y> cteClass) {
        if (!dbmsDialect.supportsWithClause()) {
            throw new UnsupportedOperationException("The database does not support the with clause!");
        }
        
		return cteManager.withRecursive(cteClass, (X) this);
	}

    @SuppressWarnings("unchecked")
    public <Y> ReturningModificationCriteriaBuilderFactory<X> withReturning(Class<Y> cteClass) {
        if (!dbmsDialect.supportsWithClause()) {
            throw new UnsupportedOperationException("The database does not support the with clause!");
        }
        if (!dbmsDialect.supportsModificationQueryInWithClause()) {
            throw new UnsupportedOperationException("The database does not support modification queries in the with clause!");
        }
        
		return cteManager.withReturning(cteClass, (X) this);
    }
    
    public Z union() {
        // TODO: implement
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public Z unionAll() {
        // TODO: implement
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public Z intersect() {
        // TODO: implement
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public Z intersectAll() {
        // TODO: implement
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public Z except() {
        // TODO: implement
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public Z exceptAll() {
        // TODO: implement
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public X from(Class<?> clazz) {
        return from(clazz, clazz.getSimpleName().toLowerCase());
    }

    public X from(Class<?> clazz, String alias) {
        return from(clazz, alias, null);
    }

    public X fromOld(Class<?> clazz) {
        return fromOld(clazz, clazz.getSimpleName().toLowerCase());
    }

    public X fromOld(Class<?> clazz, String alias) {
        return from(clazz, alias, DbmsModificationState.OLD);
    }

    public X fromNew(Class<?> clazz) {
        return fromNew(clazz, clazz.getSimpleName().toLowerCase());
    }

    public X fromNew(Class<?> clazz, String alias) {
        return from(clazz, alias, DbmsModificationState.NEW);
    }

    @SuppressWarnings("unchecked")
    private X from(Class<?> clazz, String alias, DbmsModificationState state) {
    	if (!fromClassExplicitelySet) {
    		// When from is explicitly called we have to revert the implicit root
    		if (joinManager.getRoots().size() > 0) {
    			joinManager.removeRoot();
    		}
    	}
    	
    	EntityType<?> type = em.getMetamodel().entity(clazz);
    	String finalAlias = joinManager.addRoot(type, alias);
        fromClassExplicitelySet = true;
        
    	if (state != null) {
    	    Map<String, DbmsModificationState> versionEntities = explicitVersionEntities.get(clazz);
    	    if (versionEntities == null) {
    	        versionEntities = new HashMap<String, DbmsModificationState>(1);
    	        explicitVersionEntities.put(clazz, versionEntities);
    	    }
    	    
    	    versionEntities.put(finalAlias, state);
    	}
    	
        return (X) this;
    }

	@SuppressWarnings("unchecked")
    public X setFirstResult(int firstResult) {
    	this.firstResult = firstResult;
        return (X) this;
    }

	@SuppressWarnings("unchecked")
	public X setMaxResults(int maxResults) {
    	this.maxResults = maxResults;
        return (X) this;
	}

    public int getFirstResult() {
		return firstResult;
	}

	public int getMaxResults() {
		return maxResults;
	}

	public Metamodel getMetamodel() {
        return em.getMetamodel();
    }

    void parameterizeQuery(Query q) {
        for (Parameter<?> p : q.getParameters()) {
            if (!isParameterSet(p.getName())) {
                throw new IllegalStateException("Unsatisfied parameter " + p.getName());
            }
            Object paramValue = parameterManager.getParameterValue(p.getName());
            if (paramValue instanceof ParameterManager.TemporalCalendarParameterWrapper) {
                ParameterManager.TemporalCalendarParameterWrapper wrappedValue = (ParameterManager.TemporalCalendarParameterWrapper) paramValue;
                q.setParameter(p.getName(), wrappedValue.getValue(), wrappedValue.getType());
            } else if (paramValue instanceof ParameterManager.TemporalDateParameterWrapper) {
                ParameterManager.TemporalDateParameterWrapper wrappedValue = (ParameterManager.TemporalDateParameterWrapper) paramValue;
                q.setParameter(p.getName(), wrappedValue.getValue(), wrappedValue.getType());
            } else {
                q.setParameter(p.getName(), paramValue);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public X setParameter(String name, Object value) {
        parameterManager.satisfyParameter(name, value);
        return (X) this;
    }

    @SuppressWarnings("unchecked")
    public X setParameter(String name, Calendar value, TemporalType temporalType) {
        parameterManager.satisfyParameter(name, new ParameterManager.TemporalCalendarParameterWrapper(value, temporalType));
        return (X) this;
    }

    @SuppressWarnings("unchecked")
    public X setParameter(String name, Date value, TemporalType temporalType) {
        parameterManager.satisfyParameter(name, new ParameterManager.TemporalDateParameterWrapper(value, temporalType));
        return (X) this;
    }

    public boolean containsParameter(String name) {
        return parameterManager.containsParameter(name);
    }

    public boolean isParameterSet(String name) {
        return parameterManager.isParameterSet(name);
    }

    public Parameter<?> getParameter(String name) {
        return parameterManager.getParameter(name);
    }

    public Set<? extends Parameter<?>> getParameters() {
        return parameterManager.getParameters();
    }

    public Object getParameterValue(String name) {
        return parameterManager.getParameterValue(name);
    }

    /*
     * Select methods
     */
    @SuppressWarnings("unchecked")
    public X distinct() {
        clearCache();
        selectManager.distinct();
        return (X) this;
    }

    public CaseWhenStarterBuilder<X> selectCase() {
        return selectCase(null);
    }

    /* CASE (WHEN condition THEN scalarExpression)+ ELSE scalarExpression END */
    @SuppressWarnings("unchecked")
    public CaseWhenStarterBuilder<X> selectCase(String selectAlias) {
        if (selectAlias != null && selectAlias.isEmpty()) {
            throw new IllegalArgumentException("selectAlias");
        }
        return selectManager.selectCase((X) this, selectAlias);
    }

    public SimpleCaseWhenStarterBuilder<X> selectSimpleCase(String expression) {
        return selectSimpleCase(expression, null);
    }

    /* CASE caseOperand (WHEN scalarExpression THEN scalarExpression)+ ELSE scalarExpression END */
    @SuppressWarnings("unchecked")
    public SimpleCaseWhenStarterBuilder<X> selectSimpleCase(String caseOperandExpression, String selectAlias) {
        if (selectAlias != null && selectAlias.isEmpty()) {
            throw new IllegalArgumentException("selectAlias");
        }
        return selectManager.selectSimpleCase((X) this, selectAlias, expressionFactory.createCaseOperandExpression(caseOperandExpression));
    }

    public X select(String expression) {
        return select(expression, null);
    }

    @SuppressWarnings("unchecked")
    public X select(String expression, String selectAlias) {
        Expression expr = expressionFactory.createSimpleExpression(expression);
        if (selectAlias != null && selectAlias.isEmpty()) {
            throw new IllegalArgumentException("selectAlias");
        }
        verifyBuilderEnded();
        selectManager.select(expr, selectAlias);
        resultType = (Class<T>) Tuple.class;
        return (X) this;
    }

    public SubqueryInitiator<X> selectSubquery() {
        return selectSubquery(null);
    }

    @SuppressWarnings("unchecked")
    public SubqueryInitiator<X> selectSubquery(String selectAlias) {
        if (selectAlias != null && selectAlias.isEmpty()) {
            throw new IllegalArgumentException("selectAlias");
        }
        verifyBuilderEnded();
        return selectManager.selectSubquery((X) this, selectAlias);
    }

    public SubqueryInitiator<X> selectSubquery(String subqueryAlias, String expression) {
        return selectSubquery(subqueryAlias, expression, null);
    }

    @SuppressWarnings("unchecked")
    public SubqueryInitiator<X> selectSubquery(String subqueryAlias, String expression, String selectAlias) {
        if (selectAlias != null && selectAlias.isEmpty()) {
            throw new IllegalArgumentException("selectAlias");
        }
        if (subqueryAlias == null) {
            throw new NullPointerException("subqueryAlias");
        }
        if (subqueryAlias.isEmpty()) {
            throw new IllegalArgumentException("subqueryAlias");
        }
        if (expression == null) {
            throw new NullPointerException("expression");
        }
        if (!expression.contains(subqueryAlias)) {
            throw new IllegalArgumentException("Expression [" + expression + "] does not contain subquery alias [" + subqueryAlias + "]");
        }
        verifyBuilderEnded();
        return selectManager.selectSubquery((X) this, subqueryAlias, expressionFactory.createSimpleExpression(expression), selectAlias);
    }

    /*
     * Where methods
     */
    public RestrictionBuilder<X> where(String expression) {
        Expression expr = expressionFactory.createSimpleExpression(expression);
        return whereManager.restrict(this, expr);
    }

    /*
     * Where methods
     */
    public CaseWhenStarterBuilder<RestrictionBuilder<X>> whereCase() {
        return whereManager.restrictCase(this);
    }

    public SimpleCaseWhenStarterBuilder<RestrictionBuilder<X>> whereSimpleCase(String expression) {
        return whereManager.restrictSimpleCase(this, expressionFactory.createCaseOperandExpression(expression));
    }

    public WhereOrBuilder<X> whereOr() {
        return whereManager.whereOr(this);
    }

    @SuppressWarnings("unchecked")
    public SubqueryInitiator<X> whereExists() {
        return whereManager.restrictExists((X) this);
    }

    @SuppressWarnings("unchecked")
    public SubqueryInitiator<X> whereNotExists() {
        return whereManager.restrictNotExists((X) this);
    }

    public SubqueryInitiator<RestrictionBuilder<X>> whereSubquery() {
        return whereManager.restrict(this);
    }

    public SubqueryInitiator<RestrictionBuilder<X>> whereSubquery(String subqueryAlias, String expression) {
        return whereManager.restrict(this, subqueryAlias, expression);
    }

    /*
     * Group by methods
     */
    @SuppressWarnings("unchecked")
    public X groupBy(String... paths) {
        for (String path : paths) {
            groupBy(path);
        }
        return (X) this;
    }

    @SuppressWarnings("unchecked")
    public X groupBy(String expression) {
        clearCache();
        Expression expr;
        if (cbf.isCompatibleModeEnabled()) {
            expr = expressionFactory.createPathExpression(expression);
        } else {
            expr = expressionFactory.createSimpleExpression(expression);
        }
        verifyBuilderEnded();
        groupByManager.groupBy(expr);
        return (X) this;
    }

    /*
     * Having methods
     */
    public RestrictionBuilder<X> having(String expression) {
        clearCache();
        if (groupByManager.isEmpty()) {
            throw new IllegalStateException("Having without group by");
        }
        Expression expr = expressionFactory.createSimpleExpression(expression);
        return havingManager.restrict(this, expr);
    }

    public CaseWhenStarterBuilder<RestrictionBuilder<X>> havingCase() {
        return havingManager.restrictCase(this);
    }

    public SimpleCaseWhenStarterBuilder<RestrictionBuilder<X>> havingSimpleCase(String expression) {
        return havingManager.restrictSimpleCase(this, expressionFactory.createCaseOperandExpression(expression));
    }

    public HavingOrBuilder<X> havingOr() {
        clearCache();
        if (groupByManager.isEmpty()) {
            throw new IllegalStateException("Having without group by");
        }
        return havingManager.havingOr(this);
    }

    @SuppressWarnings("unchecked")
    public SubqueryInitiator<X> havingExists() {
        clearCache();
        if (groupByManager.isEmpty()) {
            throw new IllegalStateException("Having without group by");
        }
        return havingManager.restrictExists((X) this);
    }

    @SuppressWarnings("unchecked")
    public SubqueryInitiator<X> havingNotExists() {
        clearCache();
        if (groupByManager.isEmpty()) {
            throw new IllegalStateException("Having without group by");
        }
        return havingManager.restrictNotExists((X) this);
    }

    public SubqueryInitiator<RestrictionBuilder<X>> havingSubquery() {
        clearCache();
        return havingManager.restrict(this);
    }

    public SubqueryInitiator<RestrictionBuilder<X>> havingSubquery(String subqueryAlias, String expression) {
        clearCache();
        return havingManager.restrict(this, subqueryAlias, expression);
    }

    /*
     * Order by methods
     */
    public X orderByDesc(String expression) {
        return orderBy(expression, false, false);
    }

    public X orderByAsc(String expression) {
        return orderBy(expression, true, false);
    }

    public X orderByDesc(String expression, boolean nullFirst) {
        return orderBy(expression, false, nullFirst);
    }

    public X orderByAsc(String expression, boolean nullFirst) {
        return orderBy(expression, true, nullFirst);
    }

    @SuppressWarnings("unchecked")
    public X orderBy(String expression, boolean ascending, boolean nullFirst) {
        Expression expr;
        if (cbf.isCompatibleModeEnabled()) {
            expr = expressionFactory.createOrderByExpression(expression);
        } else {
            expr = expressionFactory.createSimpleExpression(expression);
        }
        _orderBy(expr, ascending, nullFirst);
        return (X) this;
    }

    protected void verifyBuilderEnded() {
        whereManager.verifyBuilderEnded();
        havingManager.verifyBuilderEnded();
        selectManager.verifyBuilderEnded();
        joinManager.verifyBuilderEnded();
    }

    public void _orderBy(Expression expression, boolean ascending, boolean nullFirst) {
        clearCache();
        verifyBuilderEnded();
        orderByManager.orderBy(expression, ascending, nullFirst);
    }

    /*
     * Join methods
     */
    public X innerJoin(String path, String alias) {
        return join(path, alias, JoinType.INNER);
    }

    public X innerJoinDefault(String path, String alias) {
        return joinDefault(path, alias, JoinType.INNER);
    }

    public X leftJoin(String path, String alias) {
        return join(path, alias, JoinType.LEFT);
    }

    public X leftJoinDefault(String path, String alias) {
        return joinDefault(path, alias, JoinType.LEFT);
    }

    public X rightJoin(String path, String alias) {
        return join(path, alias, JoinType.RIGHT);
    }

    public X rightJoinDefault(String path, String alias) {
        return joinDefault(path, alias, JoinType.RIGHT);
    }

    @SuppressWarnings("unchecked")
    public X join(String path, String alias, JoinType type) {
        clearCache();
        checkJoinPreconditions(path, alias, type);
        joinManager.join(path, alias, type, false, false);
        return (X) this;
    }

    @SuppressWarnings("unchecked")
    public X joinDefault(String path, String alias, JoinType type) {
        clearCache();
        checkJoinPreconditions(path, alias, type);
        joinManager.join(path, alias, type, false, true);
        return (X) this;
    }

    @SuppressWarnings("unchecked")
    public JoinOnBuilder<X> joinOn(String path, String alias, JoinType type) {
        clearCache();
        checkJoinPreconditions(path, alias, type);
        return joinManager.joinOn((X) this, path, alias, type, false);
    }

    @SuppressWarnings("unchecked")
    public JoinOnBuilder<X> joinDefaultOn(String path, String alias, JoinType type) {
        clearCache();
        checkJoinPreconditions(path, alias, type);
        return joinManager.joinOn((X) this, path, alias, type, true);
    }

    public JoinOnBuilder<X> innerJoinOn(String path, String alias) {
        return joinOn(path, alias, JoinType.INNER);
    }

    public JoinOnBuilder<X> innerJoinDefaultOn(String path, String alias) {
        return joinDefaultOn(path, alias, JoinType.INNER);
    }

    public JoinOnBuilder<X> leftJoinOn(String path, String alias) {
        return joinOn(path, alias, JoinType.LEFT);
    }

    public JoinOnBuilder<X> leftJoinDefaultOn(String path, String alias) {
        return joinDefaultOn(path, alias, JoinType.LEFT);
    }

    public JoinOnBuilder<X> rightJoinOn(String path, String alias) {
        return joinOn(path, alias, JoinType.RIGHT);
    }

    public JoinOnBuilder<X> rightJoinDefaultOn(String path, String alias) {
        return joinDefaultOn(path, alias, JoinType.RIGHT);
    }

    private void checkJoinPreconditions(String path, String alias, JoinType type) {
        if (path == null) {
            throw new NullPointerException("path");
        }
        if (alias == null) {
            throw new NullPointerException("alias");
        }
        if (type == null) {
            throw new NullPointerException("type");
        }
        if (alias.isEmpty()) {
            throw new IllegalArgumentException("Empty alias");
        }
        verifyBuilderEnded();
    }
    
    protected boolean isJoinRequiredForSelect() {
        return true;
    }

    protected void applyImplicitJoins() {
        if (implicitJoinsApplied) {
            return;
        }

        final JoinVisitor joinVisitor = new JoinVisitor(joinManager);
        final JoinNodeVisitor joinNodeVisitor = new OnClauseJoinNodeVisitor(joinVisitor) {

            @Override
            public void visit(JoinNode node) {
                super.visit(node);
                node.registerDependencies();
            }

        };
        joinVisitor.setFromClause(null);
        joinManager.acceptVisitor(joinNodeVisitor);
        // carry out implicit joins
        joinVisitor.setFromClause(ClauseType.SELECT);
        // There might be clauses for which joins are not required
        joinVisitor.setJoinRequired(isJoinRequiredForSelect());
        selectManager.acceptVisitor(joinVisitor);
        joinVisitor.setJoinRequired(true);

        joinVisitor.setFromClause(ClauseType.WHERE);
        whereManager.acceptVisitor(joinVisitor);
        joinVisitor.setFromClause(ClauseType.GROUP_BY);
        groupByManager.acceptVisitor(joinVisitor);

        joinVisitor.setFromClause(ClauseType.HAVING);// SELECT SIZE(d.contacts) AS c FROM Document d ORDER BY c
        havingManager.acceptVisitor(joinVisitor);
        joinVisitor.setJoinWithObjectLeafAllowed(false);

        joinVisitor.setFromClause(ClauseType.ORDER_BY);
        orderByManager.acceptVisitor(joinVisitor);
        joinVisitor.setJoinWithObjectLeafAllowed(true);
        // No need to implicit join again if no mutation occurs
        implicitJoinsApplied = true;
    }

    protected void applyVisitor(VisitorAdapter expressionVisitor) {
        selectManager.acceptVisitor(expressionVisitor);
        joinManager.acceptVisitor(new OnClauseJoinNodeVisitor(expressionVisitor));
        whereManager.acceptVisitor(expressionVisitor);
        groupByManager.acceptVisitor(expressionVisitor);
        havingManager.acceptVisitor(expressionVisitor);
        orderByManager.acceptVisitor(expressionVisitor);
    }

    protected void applySizeSelectTransformer() {
        boolean containsSizeSelect = false;
        for (SelectInfo selectInfo : selectManager.getSelectInfos()) {
            if (ExpressionUtils.containsSizeExpression(selectInfo.getExpression())) {
                containsSizeSelect = true;
                break;
            }
        }

        if (containsSizeSelect) {
            if (joinManager.hasCollections() || joinManager.getRoots().size() > 1) {
                selectManager.applySelectInfoTransformer(sizeSelectToSubqueryTransformer);
            } else {
                selectManager.applySelectInfoTransformer(sizeSelectToCountTransformer);
            }
        }
    }

    protected void applyExpressionTransformers() {
        // run through expressions
        // for each arrayExpression, look up the alias in the joinManager's aliasMap
        // do the transformation using the alias
        // exchange old arrayExpression with new PathExpression
        // introduce applyTransformer method in managers
        // transformer has a method that returns the transformed Expression
        // the applyTransformer method will replace the transformed expression with the original one

        // Problem we must have the complete (i.e. including array indices) absolute path available during array transformation of a
        // path expression
        // since the path expression might not be based on the root node
        // we must track absolute paths to detect redundancies
        // However, the absolute path in the path expression's join node does not contain information about the indices so far but it
        // would
        // also be a wrong match to add the indices in this structure since there can be multiple indices for the same join path element
        // consider d.contacts[l] and d.contacts[x], the absolute join path is d.contacts but this path occurs with two different
        // indices
        // So where should be store this information or from where should we retrieve it during arrayTransformation?
        // i think the answer is: we can't
        // d.contacts[1].localized[1]
        // d.contacts contacts, contacts.localized localized
        // or we remember the already transfomred path in a Set<(BaseNode, RelativePath)> - maybe this would be sufficient
        // because access to the same array with two different indices has an empty result set anyway. so if we had basePaths with
        // two different indices for the same array we would output the two accesses for the subpath and the access for the current path
        // just once (and not once for each distinct subpath)
        for (ExpressionTransformer transformer : transformers) {
            joinManager.applyTransformer(transformer);
            selectManager.applyTransformer(transformer);
            whereManager.applyTransformer(transformer);
            groupByManager.applyTransformer(transformer);
            orderByManager.applyTransformer(transformer);
        }

        applySizeSelectTransformer();

        // After all transformations are done, we can finally check if aggregations are used
        AggregateDetectionVisitor aggregateDetector = new AggregateDetectionVisitor();
        hasGroupBy = groupByManager.hasGroupBys();
        hasGroupBy = hasGroupBy || Boolean.TRUE.equals(selectManager.acceptVisitor(aggregateDetector, true));
        hasGroupBy = hasGroupBy || Boolean.TRUE.equals(joinManager.acceptVisitor(aggregateDetector, true));
        hasGroupBy = hasGroupBy || Boolean.TRUE.equals(whereManager.acceptVisitor(aggregateDetector));
        hasGroupBy = hasGroupBy || Boolean.TRUE.equals(orderByManager.acceptVisitor(aggregateDetector, true));
        hasGroupBy = hasGroupBy || Boolean.TRUE.equals(havingManager.acceptVisitor(aggregateDetector));
    }

    public Class<T> getResultType() {
        return resultType;
    }

    public String getQueryString() {
        prepareAndCheck();
        return getCteQueryString0();
    }
    
    protected String getBaseQueryString() {
        prepareAndCheck();
        return getQueryString0();
    }
    
    protected String getCteQueryString() {
        prepareAndCheck();
        return getCteQueryString0();
    }

    protected TypedQuery<T> getTypedQuery() {
        // If we have no ctes, there is nothing to do
        if (cteManager.getCtes().isEmpty()) {
            return getTypedQuery(getBaseQueryString());
        }

        TypedQuery<T> baseQuery = getTypedQuery(getBaseQueryString());
        List<Query> participatingQueries = new ArrayList<Query>();
        
        String sqlQuery = cbf.getExtendedQuerySupport().getSql(em, baseQuery);
        StringBuilder sqlSb = new StringBuilder(sqlQuery);
        StringBuilder withClause = applyCtes(sqlSb, baseQuery, false, participatingQueries);
        applyExtendedSql(sqlSb, false, withClause, null, null);
        
        String finalQuery = sqlSb.toString();
        participatingQueries.add(baseQuery);
        TypedQuery<T> query = new CustomSQLTypedQuery<T>(participatingQueries, baseQuery, dbmsDialect, em, cbf.getExtendedQuerySupport(), finalQuery);
        // TODO: object builder?
        
        return query;
    }
    
    protected Query getQuery() {
        return getTypedQuery();
    }
    
    protected Query getQuery(Map<DbmsModificationState, String> includedModificationStates) {
        return getQuery();
    }
    
    @SuppressWarnings("unchecked")
    protected TypedQuery<T> getTypedQuery(String queryString) {
        TypedQuery<T> query = (TypedQuery<T>) em.createQuery(queryString, selectManager.getExpectedQueryResultType());
        if (firstResult != 0) {
            query.setFirstResult(firstResult);
        }
        if (maxResults != Integer.MAX_VALUE) {
            query.setMaxResults(maxResults);
        }
        if (selectManager.getSelectObjectBuilder() != null) {
            query = transformQuery(query);
        }

        parameterizeQuery(query);
        return query;
    }

    @SuppressWarnings("unchecked")
    public KeysetBuilder<X> beforeKeyset() {
        clearCache();
        return keysetManager.startBuilder(new KeysetBuilderImpl<X>((X) this, keysetManager, KeysetMode.PREVIOUS));
    }

    public X beforeKeyset(Serializable... values) {
        return beforeKeyset(new KeysetImpl(values));
    }

    @SuppressWarnings("unchecked")
    public X beforeKeyset(Keyset keyset) {
        clearCache();
        keysetManager.verifyBuilderEnded();
        keysetManager.setKeysetLink(new SimpleKeysetLink(keyset, KeysetMode.PREVIOUS));
        return (X) this;
    }

    @SuppressWarnings("unchecked")
    public KeysetBuilder<X> afterKeyset() {
        clearCache();
        return keysetManager.startBuilder(new KeysetBuilderImpl<X>((X) this, keysetManager, KeysetMode.NEXT));
    }

    public X afterKeyset(Serializable... values) {
        return afterKeyset(new KeysetImpl(values));
    }

    @SuppressWarnings("unchecked")
    public X afterKeyset(Keyset keyset) {
        clearCache();
        keysetManager.verifyBuilderEnded();
        keysetManager.setKeysetLink(new SimpleKeysetLink(keyset, KeysetMode.NEXT));
        return (X) this;
    }

    protected String getQueryString0() {
        if (cachedQueryString == null) {
            cachedQueryString = getQueryString1();
        }

        return cachedQueryString;
    }

    protected String getCteQueryString0() {
        if (cachedCteQueryString == null) {
            cachedCteQueryString = getCteQueryString1();
        }

        return cachedCteQueryString;
    }

    protected void clearCache() {
        needsCheck = true;
        cachedQueryString = null;
        cachedCteQueryString = null;
        implicitJoinsApplied = false;
    }

    protected void prepareAndCheck() {
        if (!needsCheck) {
            return;
        }

        verifyBuilderEnded();
        // resolve unresolved aliases, object model etc.
        // we must do implicit joining at the end because we can only do
        // the aliases resolving at the end and alias resolving must happen before
        // the implicit joins
        // it makes no sense to do implicit joining before this point, since
        // the user can call the api in arbitrary orders
        // so where("b.c").join("a.b") but also
        // join("a.b", "b").where("b.c")
        // in the first case
        applyImplicitJoins();
        applyExpressionTransformers();

        if (keysetManager.hasKeyset()) {
            // The last order by expression must be unique, otherwise keyset scrolling wouldn't work
            Metamodel m = em.getMetamodel();
            List<OrderByExpression> orderByExpressions = orderByManager.getOrderByExpressions(m);
            if (!orderByExpressions.get(orderByExpressions.size() - 1).isUnique()) {
                throw new IllegalStateException("The last order by item must be unique!");
            }
            keysetManager.initialize(orderByExpressions);
        }

        // Check if aggregate functions are used

        // No need to do all that stuff again if no mutation occurs
        needsCheck = false;
    }

    protected String getQueryString1() {
        StringBuilder sbSelectFrom = new StringBuilder();
        getQueryString1(sbSelectFrom);
        return sbSelectFrom.toString();
    }

    protected void getQueryString1(StringBuilder sbSelectFrom) {
    	appendSelectClause(sbSelectFrom);
    	appendFromClause(sbSelectFrom);
    	appendWhereClause(sbSelectFrom);
    	appendGroupByClause(sbSelectFrom);
    	appendOrderByClause(sbSelectFrom);
    }

    protected String getCteQueryString1() {
        StringBuilder sbSelectFrom = new StringBuilder();
        getCteQueryString1(sbSelectFrom);
        return sbSelectFrom.toString();
    }

    protected void getCteQueryString1(StringBuilder sbSelectFrom) {
        cteManager.buildClause(sbSelectFrom);
        getQueryString1(sbSelectFrom);
    }

    protected void appendSelectClause(StringBuilder sbSelectFrom) {
        sbSelectFrom.append(selectManager.buildSelect());
    }

    protected void appendFromClause(StringBuilder sbSelectFrom) {
        joinManager.buildClause(sbSelectFrom, EnumSet.noneOf(ClauseType.class), null);
    }

    protected void appendWhereClause(StringBuilder sbSelectFrom) {
        KeysetLink keysetLink = keysetManager.getKeysetLink();
        if (keysetLink == null || keysetLink.getKeysetMode() == KeysetMode.NONE) {
            whereManager.buildClause(sbSelectFrom);
        } else {
            sbSelectFrom.append(" WHERE ");

            keysetManager.buildKeysetPredicate(sbSelectFrom);

            if (whereManager.hasPredicates()) {
                sbSelectFrom.append(" AND ");
                whereManager.buildClausePredicate(sbSelectFrom);
            }
        }
    }

    protected void appendGroupByClause(StringBuilder sbSelectFrom) {
        Set<String> clauses = new LinkedHashSet<String>();
        clauses.addAll(groupByManager.buildGroupByClauses());
        if (hasGroupBy) {
            clauses.addAll(selectManager.buildGroupByClauses(em.getMetamodel()));
            clauses.addAll(havingManager.buildGroupByClauses());
            clauses.addAll(orderByManager.buildGroupByClauses());
        }
        groupByManager.buildGroupBy(sbSelectFrom, clauses);
        havingManager.buildClause(sbSelectFrom);
    }

    protected void appendOrderByClause(StringBuilder sbSelectFrom) {
        queryGenerator.setResolveSelectAliases(false);
        orderByManager.buildOrderBy(sbSelectFrom, false, false);
        queryGenerator.setResolveSelectAliases(true);
    }
    
    protected Map<DbmsModificationState, String> getModificationStates(Map<Class<?>, Map<String, DbmsModificationState>> explicitVersionEntities) {
        return null;
    }
    
    protected Map<String, String> getModificationStateRelatedTableNameRemappings(Map<Class<?>, Map<String, DbmsModificationState>> explicitVersionEntities) {
        return null;
    }
    
    private boolean applyAddedCtes(Query query, AbstractCommonQueryBuilder<?, ?, ?> queryBuilder, StringBuilder sb, Map<String, String> tableNameRemapping, boolean firstCte) {
        if (query instanceof CustomSQLQuery) {
            // EntityAlias -> CteName
            Map<String, String> cteTableNameRemappings = queryBuilder.getModificationStateRelatedTableNameRemappings(explicitVersionEntities);
            // CteName -> CteQueryString
            Map<String, String> addedCtes = ((CustomSQLQuery) query).getAddedCtes();
            if (addedCtes != null && addedCtes.size() > 0) {
                for (Map.Entry<String, String> simpleCteEntry : addedCtes.entrySet()) {
                    for (Map.Entry<String, String> cteTableNameRemapping : cteTableNameRemappings.entrySet()) {
                        if (cteTableNameRemapping.getValue().equals(simpleCteEntry.getKey())) {
                            tableNameRemapping.put(cteTableNameRemapping.getKey(), cteTableNameRemapping.getValue());
                        }
                    }
                    
                    if (firstCte) {
                        firstCte = false;
                    } else {
                        sb.append(",\n");
                    }
                    
                    sb.append(simpleCteEntry.getKey());
                    sb.append(" AS (\n");
                    sb.append(simpleCteEntry.getValue());
                    sb.append("\n)");
                }
            }
        }
        
        return firstCte;
    }
    
    protected StringBuilder applyCtes(StringBuilder sqlSb, Query baseQuery, boolean isSubquery, List<Query> participatingQueries) {
        // NOTE: Delete statements could cause CTEs to be generated for the cascading deletes
        if (isSubquery || !cteManager.hasCtes() && statementType != DbmsStatementType.DELETE) {
            return null;
        }

        // EntityAlias -> CteName
        Map<String, String> tableNameRemapping = new LinkedHashMap<String, String>(0);
        
        StringBuilder sb = new StringBuilder(cteManager.getCtes().size() * 100);
        sb.append(dbmsDialect.getWithClause(cteManager.isRecursive()));
        sb.append(" ");

        boolean firstCte = true;
        for (CTEInfo cteInfo : cteManager.getCtes()) {
            // Build queries and add as participating queries
            Map<DbmsModificationState, String> modificationStates = cteInfo.nonRecursiveCriteriaBuilder.getModificationStates(explicitVersionEntities);
            Query nonRecursiveQuery = cteInfo.nonRecursiveCriteriaBuilder.getQuery(modificationStates);
            participatingQueries.add(nonRecursiveQuery);
            
            Query recursiveQuery = null;
            if (cteInfo.recursive) {
                modificationStates = cteInfo.nonRecursiveCriteriaBuilder.getModificationStates(explicitVersionEntities);
                recursiveQuery = cteInfo.recursiveCriteriaBuilder.getQuery(modificationStates);
                participatingQueries.add(recursiveQuery);
            }

            // add cascading delete statements as CTEs
            firstCte = applyCascadingDelete(nonRecursiveQuery, cteInfo.nonRecursiveCriteriaBuilder, participatingQueries, sb, cteInfo.name, firstCte);
            
            firstCte = applyAddedCtes(nonRecursiveQuery, cteInfo.nonRecursiveCriteriaBuilder, sb, tableNameRemapping, firstCte);
            firstCte = applyAddedCtes(recursiveQuery, cteInfo.recursiveCriteriaBuilder, sb, tableNameRemapping, firstCte);
            
            String cteNonRecursiveSqlQuery = getSql(nonRecursiveQuery);

            if (firstCte) {
                firstCte = false;
            } else {
                sb.append(",\n");
            }
            
            sb.append(cteInfo.name);
            sb.append('(');

            final List<String> attributes = cteInfo.attributes;
            boolean first = true;
            for (int i = 0; i < attributes.size(); i++) {
                String[] columns = cbf.getExtendedQuerySupport().getColumnNames(em, cteInfo.cteType, attributes.get(i));
                for (String column : columns) {
                    if (first) {
                        first = false;
                    } else {
                        sb.append(", ");
                    }
                    
                    sb.append(column);
                }
            }

            sb.append(')');
            
            sb.append(" AS(\n");
            
            sb.append(cteNonRecursiveSqlQuery);
            
            if (cteInfo.recursive) {
                String cteRecursiveSqlQuery = getSql(recursiveQuery);
                if (cteInfo.unionAll) {
                    sb.append("\nUNION ALL\n");
                } else {
                    sb.append("\nUNION\n");
                }
                sb.append(cteRecursiveSqlQuery);
            } else if (!dbmsDialect.supportsNonRecursiveWithClause()) {
                sb.append("\nUNION ALL\n");
                sb.append("SELECT ");
                
                sb.append("NULL");
                
                for (int i = 1; i < attributes.size(); i++) {
                    sb.append(", ");
                    sb.append("NULL");
                }
                
                sb.append(" FROM DUAL WHERE 1=0");
            }
            
            sb.append("\n)");

            // TODO: this is a hibernate specific integration detail
            final String subselect = "( select * from " + cteInfo.name + " )";
            int subselectIndex = 0;
            while ((subselectIndex = sb.indexOf(subselect, subselectIndex)) > -1) {
                sb.replace(subselectIndex, subselectIndex + subselect.length(), cteInfo.name);
            }
            subselectIndex = 0;
            while ((subselectIndex = sqlSb.indexOf(subselect, subselectIndex)) > -1) {
                sqlSb.replace(subselectIndex, subselectIndex + subselect.length(), cteInfo.name);
            }
        }

        // Add cascading delete statements from base query as CTEs
        firstCte = applyCascadingDelete(baseQuery, this, participatingQueries, sb, "main_query", firstCte);
        
        // If no CTE has been added, we can just return
        if (firstCte) {
            return null;
        }
        
        sb.append("\n");
        
        for (Map.Entry<String, String> tableNameRemappingEntry : tableNameRemapping.entrySet()) {
            String sqlAlias = cbf.getExtendedQuerySupport().getSqlAlias(em, baseQuery, tableNameRemappingEntry.getKey());
            String newCteName = tableNameRemappingEntry.getValue();

            applyTableNameRemapping(sqlSb, sqlAlias, newCteName);
        }
        
        return sb;
    }
    
    private boolean applyCascadingDelete(Query baseQuery, AbstractCommonQueryBuilder<?, ?, ?> queryBuilder, List<Query> participatingQueries, StringBuilder sb, String cteBaseName, boolean firstCte) {
        if (queryBuilder.statementType == DbmsStatementType.DELETE) {
            List<String> cascadingDeleteSqls = cbf.getExtendedQuerySupport().getCascadingDeleteSql(em, baseQuery);
            StringBuilder cascadingDeleteSqlSb = new StringBuilder();
            int cteBaseNameCount = 0;
            for (String cascadingDeleteSql : cascadingDeleteSqls) {
                if (firstCte) {
                    firstCte = false;
                } else {
                    sb.append(",\n");
                }
                
                // Since we kind of need the parameters from the base query, it will participate for each cascade
                participatingQueries.add(baseQuery);
                
                int currentSize = sb.length();
                sb.append(cteBaseName);
                sb.append('_').append(cteBaseNameCount);
                sb.append(" AS (\n");

                cascadingDeleteSqlSb.setLength(0);
                cascadingDeleteSqlSb.append(cascadingDeleteSql);
                dbmsDialect.appendExtendedSql(cascadingDeleteSqlSb, DbmsStatementType.DELETE, true, null, null, null, null, null);
                sb.append(cascadingDeleteSqlSb);
                
                sb.append("\n)");
                
                for (CTEInfo cteInfo : cteManager.getCtes()) {
                    // TODO: this is a hibernate specific integration detail
                    final String subselect = "( select * from " + cteInfo.name + " )";
                    int subselectIndex = currentSize;
                    while ((subselectIndex = sb.indexOf(subselect, subselectIndex)) > -1) {
                        sb.replace(subselectIndex, subselectIndex + subselect.length(), cteInfo.name);
                    }
                }
            }
        }
        
        return firstCte;
    }
    
    private void applyTableNameRemapping(StringBuilder sb, String sqlAlias, String newCteName) {
        final String searchAs = " as";
        final String searchAlias = " " + sqlAlias;
        int searchIndex = 0;
        while ((searchIndex = sb.indexOf(searchAlias, searchIndex)) > -1) {
            char c = sb.charAt(searchIndex + searchAlias.length());
            if (c == '.') {
                // This is a dereference of the alias, skip this
            } else {
                int[] indexRange;
                if (searchAs.equalsIgnoreCase(sb.substring(searchIndex - searchAs.length(), searchIndex))) {
                    // Uses aliasing with the AS keyword
                    indexRange = rtrimBackwardsToFirstWhitespace(sb, searchIndex - searchAs.length());
                } else {
                    // Uses aliasing without the AS keyword
                    indexRange = rtrimBackwardsToFirstWhitespace(sb, searchIndex);
                }
                
                int oldLength = indexRange[1] - indexRange[0];
                // Replace table name with cte name
                sb.replace(indexRange[0], indexRange[1], newCteName);
                // Adjust index after replacing
                searchIndex += newCteName.length() - oldLength;
            }
            
            searchIndex = searchIndex + 1;
        }
    }
    
    private int[] rtrimBackwardsToFirstWhitespace(StringBuilder sb, int startIndex) {
        int tableNameStartIndex;
        int tableNameEndIndex = startIndex;
        boolean text = false;
        for (tableNameStartIndex = tableNameEndIndex; tableNameStartIndex >= 0; tableNameStartIndex--) {
            if (text) {
                final char c = sb.charAt(tableNameStartIndex);
                if (Character.isWhitespace(c) || c == ',') {
                    tableNameStartIndex++;
                    break;
                }
            } else {
                if (Character.isWhitespace(sb.charAt(tableNameStartIndex))) {
                    tableNameEndIndex--;
                } else {
                    text = true;
                    tableNameEndIndex++;
                }
            }
        }
        
        return new int[]{ tableNameStartIndex, tableNameEndIndex };
    }
    
    private String getSql(Query query) {
        if (query instanceof CustomSQLQuery) {
            return ((CustomSQLQuery) query).getSql();
        } else if (query instanceof CustomSQLTypedQuery<?>) {
            return ((CustomSQLTypedQuery<?>) query).getSql();
        }
        return cbf.getExtendedQuerySupport().getSql(em, query);
    }
    
    protected boolean hasLimit() {
        return firstResult != 0 || maxResults != Integer.MAX_VALUE;
    }
    
    protected Map<String, String> applyExtendedSql(StringBuilder sqlSb, boolean isSubquery, StringBuilder withClause, String[] returningColumns, Map<DbmsModificationState, String> includedModificationStates) {
        String limit = null;
        String offset = null;
        
        if (firstResult != 0) {
            offset = Integer.toString(firstResult);
        }
        if (maxResults != Integer.MAX_VALUE) {
            limit = Integer.toString(maxResults);
        }
        
        return dbmsDialect.appendExtendedSql(sqlSb, statementType, isSubquery, withClause, limit, offset, returningColumns, includedModificationStates);
    }
    
    protected void applyJpaLimit(StringBuilder sbSelectFrom) {
        if (hasLimit()) {
            sbSelectFrom.append(" LIMIT ");
            sbSelectFrom.append(maxResults);
            
            if (firstResult > 0) {
                sbSelectFrom.append(" OFFSET ");
                sbSelectFrom.append(firstResult);
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected <Y> TypedQuery<Y> transformQuery(TypedQuery<Y> query) {
        TypedQuery<Y> currentQuery = query;
        for (QueryTransformer transformer : cbf.getQueryTransformers()) {
            currentQuery = (TypedQuery<Y>) transformer.transformQuery(query, selectManager.getSelectObjectBuilder());
        }
        return currentQuery;
    }

    // TODO: needs equals-hashCode implementation
}
