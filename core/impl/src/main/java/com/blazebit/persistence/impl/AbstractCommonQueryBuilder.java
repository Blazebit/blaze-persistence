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

import com.blazebit.persistence.*;
import com.blazebit.persistence.impl.expression.*;
import com.blazebit.persistence.spi.JpaProvider;
import com.blazebit.persistence.impl.keyset.*;
import com.blazebit.persistence.impl.predicate.Predicate;
import com.blazebit.persistence.impl.util.PropertyUtils;
import com.blazebit.persistence.spi.*;

import javax.persistence.*;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import java.io.Serializable;
import java.util.*;
import java.util.logging.Logger;

/**
 *
 * @param <QueryResultType> The query result type
 * @param <BuilderType> The concrete builder type
 * @param <SetReturn> The builder type that should be returned on set operations
 * @param <SubquerySetReturn> The builder type that should be returned on subquery set operations
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public abstract class AbstractCommonQueryBuilder<QueryResultType, BuilderType, SetReturn, SubquerySetReturn, FinalSetReturn extends BaseFinalSetOperationBuilderImpl<?, ?, ?>> {

    protected static final Logger LOG = Logger.getLogger(CriteriaBuilderImpl.class.getName());
    public static final String idParamName = "ids";

    protected final MainQuery mainQuery;
    /* This might change when transitioning to a set operation */
    protected boolean isMainQuery;
    
    protected final CriteriaBuilderFactoryImpl cbf;
    protected final EntityManager em;
    protected final DbmsStatementType statementType;
    protected final Map<Class<?>, Map<String, DbmsModificationState>> explicitVersionEntities = new HashMap<Class<?>, Map<String, DbmsModificationState>>(0);
    
    protected final ParameterManager parameterManager;
    protected final SelectManager<QueryResultType> selectManager;
    protected final WhereManager<BuilderType> whereManager;
    protected final HavingManager<BuilderType> havingManager;
    protected final GroupByManager groupByManager;
    protected final OrderByManager orderByManager;
    protected final JoinManager joinManager;
    protected final KeysetManager keysetManager;
	protected final CTEManager cteManager;
    protected final ResolvingQueryGenerator queryGenerator;
    protected final SubqueryInitiatorFactory subqueryInitFactory;
    
    // This builder will be passed in when using set operations
    protected final FinalSetReturn finalSetOperationBuilder;

    protected final DbmsDialect dbmsDialect;
    protected final JpaProvider jpaProvider;
    protected final Set<String> registeredFunctions;

    protected final AliasManager aliasManager;
    protected final ExpressionFactory expressionFactory;

    private final List<ExpressionTransformer> transformers;
    private final SizeSelectInfoTransformer sizeSelectToCountTransformer;

    // Mutable state
    protected Class<QueryResultType> resultType;
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
    protected AbstractCommonQueryBuilder(AbstractCommonQueryBuilder<QueryResultType, ?, ?, ?, ?> builder) {
        this.mainQuery = builder.mainQuery;
        this.isMainQuery = builder.isMainQuery;
        this.cbf = builder.cbf;
        this.statementType = builder.statementType;
        this.orderByManager = builder.orderByManager;
        this.parameterManager = builder.parameterManager;
        this.selectManager = builder.selectManager;
        this.whereManager = (WhereManager<BuilderType>) builder.whereManager;
        this.havingManager = (HavingManager<BuilderType>) builder.havingManager;
        this.groupByManager = builder.groupByManager;
        this.keysetManager = builder.keysetManager;
        this.cteManager = builder.cteManager;
        this.joinManager = builder.joinManager;
        this.queryGenerator = builder.queryGenerator;
        this.em = builder.em;
        this.finalSetOperationBuilder = (FinalSetReturn) builder.finalSetOperationBuilder;
        this.dbmsDialect = builder.dbmsDialect;
        this.jpaProvider = builder.jpaProvider;
        this.registeredFunctions = builder.registeredFunctions;
        this.subqueryInitFactory = builder.subqueryInitFactory;
        this.aliasManager = builder.aliasManager;
        this.expressionFactory = builder.expressionFactory;
        this.transformers = builder.transformers;
        this.resultType = builder.resultType;
        this.sizeSelectToCountTransformer = builder.sizeSelectToCountTransformer;
    }
    
    protected AbstractCommonQueryBuilder(MainQuery mainQuery, boolean isMainQuery, DbmsStatementType statementType, Class<QueryResultType> resultClazz, String alias, AliasManager aliasManager, JoinManager parentJoinManager, ExpressionFactory expressionFactory, FinalSetReturn finalSetOperationBuilder) {
        if (mainQuery == null) {
            throw new NullPointerException("mainQuery");
        }
        if (statementType == null) {
            throw new NullPointerException("statementType");
        }
        if (resultClazz == null) {
            throw new NullPointerException("resultClazz");
        }
        
        this.mainQuery = mainQuery;
        this.isMainQuery = isMainQuery;
        this.statementType = statementType;
        this.cbf = mainQuery.cbf;
        this.parameterManager = mainQuery.parameterManager;
        this.cteManager = mainQuery.cteManager;
        this.em = mainQuery.em;
        this.dbmsDialect = mainQuery.dbmsDialect;
        this.jpaProvider = mainQuery.jpaProvider;
        this.registeredFunctions = mainQuery.registeredFunctions;

        this.aliasManager = new AliasManager(aliasManager);
        this.expressionFactory = expressionFactory;
        this.queryGenerator = new ResolvingQueryGenerator(this.aliasManager, jpaProvider, registeredFunctions);
        this.joinManager = new JoinManager(mainQuery, queryGenerator, this.aliasManager, parentJoinManager, expressionFactory);

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

        this.subqueryInitFactory = new SubqueryInitiatorFactory(mainQuery, this.aliasManager, joinManager);
        this.joinManager.setSubqueryInitFactory(subqueryInitFactory);

        this.whereManager = new WhereManager<BuilderType>(queryGenerator, parameterManager, subqueryInitFactory, expressionFactory);
        this.havingManager = new HavingManager<BuilderType>(queryGenerator, parameterManager, subqueryInitFactory, expressionFactory);
        this.groupByManager = new GroupByManager(queryGenerator, parameterManager);

        this.selectManager = new SelectManager<QueryResultType>(queryGenerator, parameterManager, this.joinManager, this.aliasManager, subqueryInitFactory, expressionFactory, jpaProvider, resultClazz);
        this.orderByManager = new OrderByManager(queryGenerator, parameterManager, this.aliasManager, jpaProvider);
        this.keysetManager = new KeysetManager(queryGenerator, parameterManager);

        final SizeTransformationVisitor sizeTransformationVisitor = new SizeTransformationVisitor(mainQuery, this.aliasManager, subqueryInitFactory, joinManager, groupByManager, dbmsDialect);
        this.transformers = Arrays.asList(new OuterFunctionTransformer(joinManager), new SubqueryRecursiveExpressionTransformer(), new SizeExpressionTransformer(sizeTransformationVisitor, selectManager));
        this.sizeSelectToCountTransformer = new SizeSelectInfoTransformer(sizeTransformationVisitor, orderByManager, selectManager);
        this.resultType = resultClazz;
        
        this.finalSetOperationBuilder = finalSetOperationBuilder;
    }

    public AbstractCommonQueryBuilder(MainQuery mainQuery, boolean isMainQuery, DbmsStatementType statementType, Class<QueryResultType> resultClazz, String alias, FinalSetReturn finalSetOperationBuilder) {
        this(mainQuery, isMainQuery, statementType, resultClazz, alias, null, null, mainQuery.expressionFactory, finalSetOperationBuilder);
    }

    public AbstractCommonQueryBuilder(MainQuery mainQuery, boolean isMainQuery, DbmsStatementType statementType, Class<QueryResultType> resultClazz, String alias) {
        this(mainQuery, isMainQuery, statementType, resultClazz, alias, null);
    }
    
    public CriteriaBuilderFactory getCriteriaBuilderFactory() {
        return cbf;
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getService(Class<T> serviceClass) {
        if (CriteriaBuilderFactory.class.equals(serviceClass)) {
            return (T) cbf;
        } else if (EntityManager.class.equals(serviceClass)) {
            return (T) em;
        } else if (DbmsDialect.class.equals(serviceClass)) {
            return (T) dbmsDialect;
        } else if (SubqueryExpressionFactory.class.equals(serviceClass)) {
            return (T) mainQuery.subqueryExpressionFactory;
        } else if (ExpressionFactory.class.isAssignableFrom(serviceClass)) {
            return (T) mainQuery.expressionFactory;
        }
        
        return cbf.getService(serviceClass);
    }

    @SuppressWarnings("unchecked")
    public BuilderType registerMacro(String macroName, JpqlMacro jpqlMacro) {
        this.mainQuery.registerMacro(macroName, jpqlMacro);
        return (BuilderType) this;
    }
    
    @SuppressWarnings("unchecked")
    public BuilderType setProperty(String propertyName, String propertyValue) {
        this.mainQuery.properties.put(propertyName, propertyValue);
        return (BuilderType) this;
    }

    @SuppressWarnings("unchecked")
    public BuilderType setProperties(Map<String, String> properties) {
        this.mainQuery.properties.clear();
        this.mainQuery.properties.putAll(properties);
        return (BuilderType) this;
    }
    
    public Map<String, String> getProperties() {
        return this.mainQuery.properties;
    }
    
    @SuppressWarnings("unchecked")
    public StartOngoingSetOperationCTECriteriaBuilder<BuilderType, LeafOngoingSetOperationCTECriteriaBuilder<BuilderType>> withStartSet(Class<?> cteClass) {
        if (!dbmsDialect.supportsWithClause()) {
            throw new UnsupportedOperationException("The database does not support the with clause!");
        }
        
        return cteManager.withStartSet(cteClass, (BuilderType) this);
    }

	@SuppressWarnings("unchecked")
    public FullSelectCTECriteriaBuilder<BuilderType> with(Class<?> cteClass) {
        if (!dbmsDialect.supportsWithClause()) {
            throw new UnsupportedOperationException("The database does not support the with clause!");
        }
        
		return cteManager.with(cteClass, (BuilderType) this);
	}

    @SuppressWarnings("unchecked")
	public SelectRecursiveCTECriteriaBuilder<BuilderType> withRecursive(Class<?> cteClass) {
        if (!dbmsDialect.supportsWithClause()) {
            throw new UnsupportedOperationException("The database does not support the with clause!");
        }
        
		return cteManager.withRecursive(cteClass, (BuilderType) this);
	}

    @SuppressWarnings("unchecked")
    public ReturningModificationCriteriaBuilderFactory<BuilderType> withReturning(Class<?> cteClass) {
        if (!dbmsDialect.supportsWithClause()) {
            throw new UnsupportedOperationException("The database does not support the with clause!");
        }
        if (!dbmsDialect.supportsModificationQueryInWithClause()) {
            throw new UnsupportedOperationException("The database does not support modification queries in the with clause!");
        }
        
		return cteManager.withReturning(cteClass, (BuilderType) this);
    }
    
    public SetReturn union() {
        return addSetOperation(SetOperationType.UNION);
    }

    public SetReturn unionAll() {
        return addSetOperation(SetOperationType.UNION_ALL);
    }

    public SetReturn intersect() {
        return addSetOperation(SetOperationType.INTERSECT);
    }

    public SetReturn intersectAll() {
        return addSetOperation(SetOperationType.INTERSECT_ALL);
    }

    public SetReturn except() {
        return addSetOperation(SetOperationType.EXCEPT);
    }

    public SetReturn exceptAll() {
        return addSetOperation(SetOperationType.EXCEPT_ALL);
    }

    public SubquerySetReturn startUnion() {
        return addSubquerySetOperation(SetOperationType.UNION);
    }

    public SubquerySetReturn startUnionAll() {
        return addSubquerySetOperation(SetOperationType.UNION_ALL);
    }

    public SubquerySetReturn startIntersect() {
        return addSubquerySetOperation(SetOperationType.INTERSECT);
    }

    public SubquerySetReturn startIntersectAll() {
        return addSubquerySetOperation(SetOperationType.INTERSECT_ALL);
    }

    public SubquerySetReturn startExcept() {
        return addSubquerySetOperation(SetOperationType.EXCEPT);
    }

    public SubquerySetReturn startExceptAll() {
        return addSubquerySetOperation(SetOperationType.EXCEPT_ALL);
    }

    public SubquerySetReturn startSet() {
        return addSubquerySetOperation(null);
    }
    
    private SetReturn addSetOperation(SetOperationType type) {
        FinalSetReturn finalSetOperationBuilder = this.finalSetOperationBuilder;
        
        if (finalSetOperationBuilder == null) {
            finalSetOperationBuilder = createFinalSetOperationBuilder(type, false);
            finalSetOperationBuilder.setOperationManager.setStartQueryBuilder(this);
        } else {
            SetOperationManager oldOperationManager = finalSetOperationBuilder.setOperationManager;
            if (oldOperationManager.getOperator() == null) {
                oldOperationManager.setOperator(type);
            } else if (oldOperationManager.getOperator() != type) {
                // Put existing set operands into a sub builder and use the sub builder as new start
                FinalSetReturn subFinalSetOperationBuilder = createFinalSetOperationBuilder(oldOperationManager.getOperator(), false);
                subFinalSetOperationBuilder.setOperationManager.setStartQueryBuilder(oldOperationManager.getStartQueryBuilder());
                subFinalSetOperationBuilder.setOperationManager.getSetOperations().addAll(oldOperationManager.getSetOperations());
                oldOperationManager.setStartQueryBuilder(subFinalSetOperationBuilder);
                oldOperationManager.getSetOperations().clear();
                oldOperationManager.setOperator(type);
            }
        }
        
        SetReturn setOperand = createSetOperand(finalSetOperationBuilder);
        finalSetOperationBuilder.setOperationManager.addSetOperation((AbstractCommonQueryBuilder<?, ?, ?, ?, ?>) setOperand);
        return setOperand;
    }
    
    private SubquerySetReturn addSubquerySetOperation(SetOperationType type) {
        FinalSetReturn parentFinalSetOperationBuilder = this.finalSetOperationBuilder;
        
        if (parentFinalSetOperationBuilder == null) {
            parentFinalSetOperationBuilder = createFinalSetOperationBuilder(type, false);
            parentFinalSetOperationBuilder.setOperationManager.setStartQueryBuilder(this);
        } else {
            SetOperationManager oldParentOperationManager = finalSetOperationBuilder.setOperationManager; 

            if (oldParentOperationManager.getOperator() == null) {
                oldParentOperationManager.setOperator(type);
            } else if (oldParentOperationManager.getOperator() != type) {
                // Put existing set operands into a sub builder and use the sub builder as new start
                FinalSetReturn subFinalSetOperationBuilder = createFinalSetOperationBuilder(oldParentOperationManager.getOperator(), false);
                subFinalSetOperationBuilder.setOperationManager.setStartQueryBuilder(oldParentOperationManager.getStartQueryBuilder());
                subFinalSetOperationBuilder.setOperationManager.getSetOperations().addAll(oldParentOperationManager.getSetOperations());
                oldParentOperationManager.setStartQueryBuilder(subFinalSetOperationBuilder);
                oldParentOperationManager.getSetOperations().clear();
                oldParentOperationManager.setOperator(type);
            }
        }
        
        FinalSetReturn finalSetOperationBuilder = createFinalSetOperationBuilder(type, true);
        SubquerySetReturn subquerySetOperand = createSubquerySetOperand(finalSetOperationBuilder, parentFinalSetOperationBuilder);
        finalSetOperationBuilder.setOperationManager.setStartQueryBuilder((AbstractCommonQueryBuilder<?, ?, ?, ?, ?>) subquerySetOperand);
        
        if (type != null) {
            parentFinalSetOperationBuilder.setOperationManager.addSetOperation(finalSetOperationBuilder);
        } else {
            parentFinalSetOperationBuilder.setOperationManager.setStartQueryBuilder(finalSetOperationBuilder);
        }
        
        return subquerySetOperand;
    }
    
    protected FinalSetReturn createFinalSetOperationBuilder(SetOperationType operator, boolean nested) {
        throw new IllegalArgumentException("Set operations aren't supported!");
    }
    
    protected SetReturn createSetOperand(FinalSetReturn baseQueryBuilder) {
        throw new IllegalArgumentException("Set operations aren't supported!");
    }
    
    protected SubquerySetReturn createSubquerySetOperand(FinalSetReturn baseQueryBuilder, FinalSetReturn resultFinalSetOperationBuilder) {
        throw new IllegalArgumentException("Set operations aren't supported!");
    }

    public BuilderType from(String correlationPath) {
        return from(correlationPath, null);
    }

    public BuilderType from(String correlationPath, String alias) {
        if (!(this instanceof SubqueryBuilder<?>)) {
            throw new IllegalStateException("Cannot use a correlation path in a non-subquery!");
        }
        joinManager.addRoot(correlationPath, alias);
        return (BuilderType) this;
    }

    public BuilderType from(Class<?> clazz) {
        return from(clazz, clazz.getSimpleName().toLowerCase());
    }

    public BuilderType from(Class<?> clazz, String alias) {
        return from(clazz, alias, null);
    }

    public BuilderType fromCte(Class<?> clazz, String cteName) {
        return fromCte(clazz, clazz.getSimpleName().toLowerCase());
    }

    public BuilderType fromCte(Class<?> clazz, String cteName, String alias) {
        return from(clazz, alias, null);
    }

    public BuilderType fromOld(Class<?> clazz) {
        return fromOld(clazz, clazz.getSimpleName().toLowerCase());
    }

    public BuilderType fromOld(Class<?> clazz, String alias) {
        return from(clazz, alias, DbmsModificationState.OLD);
    }

    public BuilderType fromNew(Class<?> clazz) {
        return fromNew(clazz, clazz.getSimpleName().toLowerCase());
    }

    public BuilderType fromNew(Class<?> clazz, String alias) {
        return from(clazz, alias, DbmsModificationState.NEW);
    }

    @SuppressWarnings("unchecked")
    private BuilderType from(Class<?> clazz, String alias, DbmsModificationState state) {
    	if (!fromClassExplicitelySet) {
    		// When from is explicitly called we have to revert the implicit root
    		if (joinManager.getRoots().size() > 0) {
    			joinManager.removeRoot();
    		}
    	}
    	
    	EntityType<?> type = em.getMetamodel().entity(clazz);
    	String finalAlias = joinManager.addRoot(type, alias);
        fromClassExplicitelySet = true;
        
        // Handle old and new references
    	if (state != null) {
    	    Map<String, DbmsModificationState> versionEntities = explicitVersionEntities.get(clazz);
    	    if (versionEntities == null) {
    	        versionEntities = new HashMap<String, DbmsModificationState>(1);
    	        explicitVersionEntities.put(clazz, versionEntities);
    	    }
    	    
    	    versionEntities.put(finalAlias, state);
    	}
    	
        return (BuilderType) this;
    }

    public Set<Root> getRoots() {
        return new LinkedHashSet<Root>(joinManager.getRoots());
    }

    public Root getRoot() {
        return joinManager.getRootNodeOrFail("This should never happen. Please report this error!");
    }

	@SuppressWarnings("unchecked")
    public BuilderType setFirstResult(int firstResult) {
    	this.firstResult = firstResult;
        return (BuilderType) this;
    }

	@SuppressWarnings("unchecked")
	public BuilderType setMaxResults(int maxResults) {
    	this.maxResults = maxResults;
        return (BuilderType) this;
	}

    public int getFirstResult() {
		return firstResult;
	}

	public int getMaxResults() {
		return maxResults;
	}

	public EntityManager getEntityManager() {
	    return em;
    }

	public Metamodel getMetamodel() {
        return em.getMetamodel();
    }

    void parameterizeQuery(Query q) {
        for (Parameter<?> p : q.getParameters()) {
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
    public BuilderType setParameter(String name, Object value) {
        parameterManager.satisfyParameter(name, value);
        return (BuilderType) this;
    }

    @SuppressWarnings("unchecked")
    public BuilderType setParameter(String name, Calendar value, TemporalType temporalType) {
        parameterManager.satisfyParameter(name, new ParameterManager.TemporalCalendarParameterWrapper(value, temporalType));
        return (BuilderType) this;
    }

    @SuppressWarnings("unchecked")
    public BuilderType setParameter(String name, Date value, TemporalType temporalType) {
        parameterManager.satisfyParameter(name, new ParameterManager.TemporalDateParameterWrapper(value, temporalType));
        return (BuilderType) this;
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
        Object paramValue = parameterManager.getParameterValue(name);
        if (paramValue instanceof ParameterManager.TemporalCalendarParameterWrapper) {
            ParameterManager.TemporalCalendarParameterWrapper wrappedValue = (ParameterManager.TemporalCalendarParameterWrapper) paramValue;
            return wrappedValue.getValue();
        } else if (paramValue instanceof ParameterManager.TemporalDateParameterWrapper) {
            ParameterManager.TemporalDateParameterWrapper wrappedValue = (ParameterManager.TemporalDateParameterWrapper) paramValue;
            return wrappedValue.getValue();
        } else {
            return paramValue;
        }
    }

    @SuppressWarnings("unchecked")
    public BuilderType setParameterType(String name, Class<?> type) {
        parameterManager.setParameterType(name, type);
        return (BuilderType) this;
    }

    /*
     * Select methods
     */
    @SuppressWarnings("unchecked")
    public BuilderType distinct() {
        clearCache();
        selectManager.distinct();
        return (BuilderType) this;
    }

    public CaseWhenStarterBuilder<BuilderType> selectCase() {
        return selectCase(null);
    }

    /* CASE (WHEN condition THEN scalarExpression)+ ELSE scalarExpression END */
    @SuppressWarnings("unchecked")
    public CaseWhenStarterBuilder<BuilderType> selectCase(String selectAlias) {
        if (selectAlias != null && selectAlias.isEmpty()) {
            throw new IllegalArgumentException("selectAlias");
        }
        return selectManager.selectCase((BuilderType) this, selectAlias);
    }

    public SimpleCaseWhenStarterBuilder<BuilderType> selectSimpleCase(String expression) {
        return selectSimpleCase(expression, null);
    }

    /* CASE caseOperand (WHEN scalarExpression THEN scalarExpression)+ ELSE scalarExpression END */
    @SuppressWarnings("unchecked")
    public SimpleCaseWhenStarterBuilder<BuilderType> selectSimpleCase(String caseOperandExpression, String selectAlias) {
        if (selectAlias != null && selectAlias.isEmpty()) {
            throw new IllegalArgumentException("selectAlias");
        }
        return selectManager.selectSimpleCase((BuilderType) this, selectAlias, expressionFactory.createCaseOperandExpression(caseOperandExpression));
    }

    public BuilderType select(String expression) {
        return select(expression, null);
    }

    @SuppressWarnings("unchecked")
    public BuilderType select(String expression, String selectAlias) {
        Expression expr = expressionFactory.createSimpleExpression(expression, false);
        if (selectAlias != null && selectAlias.isEmpty()) {
            throw new IllegalArgumentException("selectAlias");
        }
        verifyBuilderEnded();
        selectManager.select(expr, selectAlias);
        if (selectManager.getSelectInfos().size() > 1) {
            // TODO: don't know if we should override this here
            resultType = (Class<QueryResultType>) Tuple.class;
        }
        return (BuilderType) this;
    }

    public SubqueryInitiator<BuilderType> selectSubquery() {
        return selectSubquery(null);
    }

    @SuppressWarnings("unchecked")
    public SubqueryInitiator<BuilderType> selectSubquery(String selectAlias) {
        if (selectAlias != null && selectAlias.isEmpty()) {
            throw new IllegalArgumentException("selectAlias");
        }
        verifyBuilderEnded();
        return selectManager.selectSubquery((BuilderType) this, selectAlias);
    }

    public SubqueryInitiator<BuilderType> selectSubquery(String subqueryAlias, String expression) {
        return selectSubquery(subqueryAlias, expression, null);
    }

    @SuppressWarnings("unchecked")
    public SubqueryInitiator<BuilderType> selectSubquery(String subqueryAlias, String expression, String selectAlias) {
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
        return selectManager.selectSubquery((BuilderType) this, subqueryAlias, expressionFactory.createSimpleExpression(expression, false), selectAlias);
    }

    public MultipleSubqueryInitiator<BuilderType> selectSubqueries(String expression) {
        return selectSubqueries(null, expression);
    }

    @SuppressWarnings("unchecked")
    public MultipleSubqueryInitiator<BuilderType> selectSubqueries(String selectAlias, String expression) {
        if (selectAlias != null && selectAlias.isEmpty()) {
            throw new IllegalArgumentException("selectAlias");
        }
        if (expression == null) {
            throw new NullPointerException("expression");
        }
        verifyBuilderEnded();
        return selectManager.selectSubqueries((BuilderType) this, expressionFactory.createSimpleExpression(expression, false), selectAlias);
    }

    /*
     * Where methods
     */
    public RestrictionBuilder<BuilderType> where(String expression) {
        Expression expr = expressionFactory.createSimpleExpression(expression, false);
        return whereManager.restrict(this, expr);
    }

    public CaseWhenStarterBuilder<RestrictionBuilder<BuilderType>> whereCase() {
        return whereManager.restrictCase(this);
    }

    public SimpleCaseWhenStarterBuilder<RestrictionBuilder<BuilderType>> whereSimpleCase(String expression) {
        return whereManager.restrictSimpleCase(this, expressionFactory.createCaseOperandExpression(expression));
    }

    public WhereOrBuilder<BuilderType> whereOr() {
        return whereManager.whereOr(this);
    }

    @SuppressWarnings("unchecked")
    public SubqueryInitiator<BuilderType> whereExists() {
        return whereManager.restrictExists((BuilderType) this);
    }

    @SuppressWarnings("unchecked")
    public SubqueryInitiator<BuilderType> whereNotExists() {
        return whereManager.restrictNotExists((BuilderType) this);
    }

    public SubqueryInitiator<RestrictionBuilder<BuilderType>> whereSubquery() {
        return whereManager.restrict(this);
    }

    public SubqueryInitiator<RestrictionBuilder<BuilderType>> whereSubquery(String subqueryAlias, String expression) {
        return whereManager.restrict(this, subqueryAlias, expression);
    }

    public MultipleSubqueryInitiator<RestrictionBuilder<BuilderType>> whereSubqueries(String expression) {
        return whereManager.restrictSubqueries(this, expression);
    }
    
    @SuppressWarnings("unchecked")
    public BuilderType whereExpression(String expression) {
        Predicate predicate = expressionFactory.createBooleanExpression(expression, false);
        whereManager.restrictExpression(this, predicate);
        return (BuilderType) this;
    }
    
    @SuppressWarnings("unchecked")
    public MultipleSubqueryInitiator<BuilderType> whereExpressionSubqueries(String expression) {
        Predicate predicate = expressionFactory.createBooleanExpression(expression, true);
        return whereManager.restrictExpressionSubqueries((BuilderType) this, predicate);
    }

    /*
     * Group by methods
     */
    @SuppressWarnings("unchecked")
    public BuilderType groupBy(String... paths) {
        for (String path : paths) {
            groupBy(path);
        }
        return (BuilderType) this;
    }

    @SuppressWarnings("unchecked")
    public BuilderType groupBy(String expression) {
        clearCache();
        Expression expr;
        if (isCompatibleModeEnabled()) {
            expr = expressionFactory.createPathExpression(expression);
        } else {
        	expr = expressionFactory.createSimpleExpression(expression, false);
        	if (!(expr instanceof PathExpression) && dbmsDialect.supportsComplexGroupBy()) {
        		throw new RuntimeException("The complex group by expression [" + expression + "] is not supported by the underlying database");
        	}
            
        }
        verifyBuilderEnded();
        groupByManager.groupBy(expr);
        return (BuilderType) this;
    }

    /*
     * Having methods
     */
    public RestrictionBuilder<BuilderType> having(String expression) {
        clearCache();
        if (groupByManager.isEmpty()) {
            throw new IllegalStateException("Having without group by");
        }
        Expression expr = expressionFactory.createSimpleExpression(expression, false);
        return havingManager.restrict(this, expr);
    }

    public CaseWhenStarterBuilder<RestrictionBuilder<BuilderType>> havingCase() {
        return havingManager.restrictCase(this);
    }

    public SimpleCaseWhenStarterBuilder<RestrictionBuilder<BuilderType>> havingSimpleCase(String expression) {
        return havingManager.restrictSimpleCase(this, expressionFactory.createCaseOperandExpression(expression));
    }

    public HavingOrBuilder<BuilderType> havingOr() {
        clearCache();
        if (groupByManager.isEmpty()) {
            throw new IllegalStateException("Having without group by");
        }
        return havingManager.havingOr(this);
    }

    @SuppressWarnings("unchecked")
    public SubqueryInitiator<BuilderType> havingExists() {
        clearCache();
        if (groupByManager.isEmpty()) {
            throw new IllegalStateException("Having without group by");
        }
        return havingManager.restrictExists((BuilderType) this);
    }

    @SuppressWarnings("unchecked")
    public SubqueryInitiator<BuilderType> havingNotExists() {
        clearCache();
        if (groupByManager.isEmpty()) {
            throw new IllegalStateException("Having without group by");
        }
        return havingManager.restrictNotExists((BuilderType) this);
    }

    public SubqueryInitiator<RestrictionBuilder<BuilderType>> havingSubquery() {
        clearCache();
        return havingManager.restrict(this);
    }

    public SubqueryInitiator<RestrictionBuilder<BuilderType>> havingSubquery(String subqueryAlias, String expression) {
        clearCache();
        return havingManager.restrict(this, subqueryAlias, expression);
    }

    public MultipleSubqueryInitiator<RestrictionBuilder<BuilderType>> havingSubqueries(String expression) {
        clearCache();
        return havingManager.restrictSubqueries(this, expression);
    }
    
    @SuppressWarnings("unchecked")
    public BuilderType havingExpression(String expression) {
        Predicate predicate = expressionFactory.createBooleanExpression(expression, false);
        havingManager.restrictExpression(this, predicate);
        return (BuilderType) this;
    }
    
    @SuppressWarnings("unchecked")
    public MultipleSubqueryInitiator<BuilderType> havingExpressionSubqueries(String expression) {
        Predicate predicate = expressionFactory.createBooleanExpression(expression, true);
        return havingManager.restrictExpressionSubqueries((BuilderType) this, predicate);
    }

    /*
     * Order by methods
     */
    public BuilderType orderByDesc(String expression) {
        return orderBy(expression, false, false);
    }

    public BuilderType orderByAsc(String expression) {
        return orderBy(expression, true, false);
    }

    public BuilderType orderByDesc(String expression, boolean nullFirst) {
        return orderBy(expression, false, nullFirst);
    }

    public BuilderType orderByAsc(String expression, boolean nullFirst) {
        return orderBy(expression, true, nullFirst);
    }

    @SuppressWarnings("unchecked")
    public BuilderType orderBy(String expression, boolean ascending, boolean nullFirst) {
        Expression expr;
        if (isCompatibleModeEnabled()) {
            expr = expressionFactory.createOrderByExpression(expression);
        } else {
            expr = expressionFactory.createSimpleExpression(expression, false);
        }
        _orderBy(expr, ascending, nullFirst);
        return (BuilderType) this;
    }

    protected void verifyBuilderEnded() {
        if (isMainQuery) {
            cteManager.verifyBuilderEnded();
        }
        
        whereManager.verifyBuilderEnded();
        keysetManager.verifyBuilderEnded();
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
    public BuilderType innerJoin(String path, String alias) {
        return join(path, alias, JoinType.INNER);
    }

    public BuilderType innerJoinDefault(String path, String alias) {
        return joinDefault(path, alias, JoinType.INNER);
    }

    public BuilderType leftJoin(String path, String alias) {
        return join(path, alias, JoinType.LEFT);
    }

    public BuilderType leftJoinDefault(String path, String alias) {
        return joinDefault(path, alias, JoinType.LEFT);
    }

    public BuilderType rightJoin(String path, String alias) {
        return join(path, alias, JoinType.RIGHT);
    }

    public BuilderType rightJoinDefault(String path, String alias) {
        return joinDefault(path, alias, JoinType.RIGHT);
    }

    @SuppressWarnings("unchecked")
    public BuilderType join(String path, String alias, JoinType type) {
        clearCache();
        checkJoinPreconditions(path, alias, type);
        joinManager.join(path, alias, type, false, false);
        return (BuilderType) this;
    }

    @SuppressWarnings("unchecked")
    public BuilderType joinDefault(String path, String alias, JoinType type) {
        clearCache();
        checkJoinPreconditions(path, alias, type);
        joinManager.join(path, alias, type, false, true);
        return (BuilderType) this;
    }

    @SuppressWarnings("unchecked")
    public JoinOnBuilder<BuilderType> joinOn(String path, String alias, JoinType type) {
        clearCache();
        checkJoinPreconditions(path, alias, type);
        return joinManager.joinOn((BuilderType) this, path, alias, type, false);
    }

    @SuppressWarnings("unchecked")
    public JoinOnBuilder<BuilderType> joinDefaultOn(String path, String alias, JoinType type) {
        clearCache();
        checkJoinPreconditions(path, alias, type);
        return joinManager.joinOn((BuilderType) this, path, alias, type, true);
    }

    public JoinOnBuilder<BuilderType> innerJoinOn(String path, String alias) {
        return joinOn(path, alias, JoinType.INNER);
    }

    public JoinOnBuilder<BuilderType> innerJoinDefaultOn(String path, String alias) {
        return joinDefaultOn(path, alias, JoinType.INNER);
    }

    public JoinOnBuilder<BuilderType> leftJoinOn(String path, String alias) {
        return joinOn(path, alias, JoinType.LEFT);
    }

    public JoinOnBuilder<BuilderType> leftJoinDefaultOn(String path, String alias) {
        return joinDefaultOn(path, alias, JoinType.LEFT);
    }

    public JoinOnBuilder<BuilderType> rightJoinOn(String path, String alias) {
        return joinOn(path, alias, JoinType.RIGHT);
    }

    public JoinOnBuilder<BuilderType> rightJoinDefaultOn(String path, String alias) {
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

        joinVisitor.setFromClause(ClauseType.HAVING);
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
        if (selectManager.containsSizeSelect()) {
            selectManager.applySelectInfoTransformer(sizeSelectToCountTransformer);
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
            havingManager.applyTransformer(transformer);
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

    public Class<QueryResultType> getResultType() {
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

    protected TypedQuery<QueryResultType> getTypedQuery() {
        // If we have no ctes, there is nothing to do
        if (!isMainQuery || cteManager.getCtes().isEmpty()) {
            return getTypedQuery(getBaseQueryString());
        }

        TypedQuery<QueryResultType> baseQuery = getTypedQuery(getBaseQueryString());
        List<Query> participatingQueries = new ArrayList<Query>();
        
        String sqlQuery = cbf.getExtendedQuerySupport().getSql(em, baseQuery);
        StringBuilder sqlSb = new StringBuilder(sqlQuery);
        StringBuilder withClause = applyCtes(sqlSb, baseQuery, false, participatingQueries);
        applyExtendedSql(sqlSb, false, false, withClause, null, null);
        
        String finalQuery = sqlSb.toString();
        participatingQueries.add(baseQuery);
        TypedQuery<QueryResultType> query = new CustomSQLTypedQuery<QueryResultType>(participatingQueries, baseQuery, (CommonQueryBuilder<?>) this, cbf.getExtendedQuerySupport(), finalQuery);
        
        // TODO: needs tests
        if (selectManager.getSelectObjectBuilder() != null) {
            query = transformQuery(query);
        }
        
        return query;
    }
    
    protected Query getQuery() {
        return getTypedQuery();
    }
    
    protected Query getQuery(Map<DbmsModificationState, String> includedModificationStates) {
        return getQuery();
    }
    
    @SuppressWarnings("unchecked")
    protected TypedQuery<QueryResultType> getTypedQuery(String queryString) {
        TypedQuery<QueryResultType> query = (TypedQuery<QueryResultType>) em.createQuery(queryString, selectManager.getExpectedQueryResultType());
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
    public KeysetBuilder<BuilderType> beforeKeyset() {
        clearCache();
        return keysetManager.startBuilder(new KeysetBuilderImpl<BuilderType>((BuilderType) this, keysetManager, KeysetMode.PREVIOUS));
    }

    public BuilderType beforeKeyset(Serializable... values) {
        return beforeKeyset(new KeysetImpl(values));
    }

    @SuppressWarnings("unchecked")
    public BuilderType beforeKeyset(Keyset keyset) {
        clearCache();
        keysetManager.verifyBuilderEnded();
        keysetManager.setKeysetLink(new SimpleKeysetLink(keyset, KeysetMode.PREVIOUS));
        return (BuilderType) this;
    }

    @SuppressWarnings("unchecked")
    public KeysetBuilder<BuilderType> afterKeyset() {
        clearCache();
        return keysetManager.startBuilder(new KeysetBuilderImpl<BuilderType>((BuilderType) this, keysetManager, KeysetMode.NEXT));
    }

    public BuilderType afterKeyset(Serializable... values) {
        return afterKeyset(new KeysetImpl(values));
    }

    @SuppressWarnings("unchecked")
    public BuilderType afterKeyset(Keyset keyset) {
        clearCache();
        keysetManager.verifyBuilderEnded();
        keysetManager.setKeysetLink(new SimpleKeysetLink(keyset, KeysetMode.NEXT));
        return (BuilderType) this;
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
        if (isMainQuery) {
            cteManager.buildClause(sbSelectFrom);
        }
        getQueryString1(sbSelectFrom);
    }

    protected void appendSelectClause(StringBuilder sbSelectFrom) {
        selectManager.buildSelect(sbSelectFrom);
    }

    protected void appendFromClause(StringBuilder sbSelectFrom) {
        joinManager.buildClause(sbSelectFrom, EnumSet.noneOf(ClauseType.class), null, false);
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
        groupByManager.buildGroupByClauses(clauses);
        if (hasGroupBy) {
        	if (isImplicitGroupByFromSelect()) {
        		selectManager.buildGroupByClauses(em.getMetamodel(), clauses);
        	}
        	if (isImplicitGroupByFromHaving()) {
        		havingManager.buildGroupByClauses(clauses);
        	}
        	if (isImplicitGroupByFromOrderBy()) {
        		orderByManager.buildGroupByClauses(clauses);
        	}
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
    
    private boolean applyAddedCtes(Query query, AbstractCommonQueryBuilder<?, ?, ?, ?, ?> queryBuilder, StringBuilder sb, Map<String, String> tableNameRemapping, boolean firstCte) {
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
        if (!isMainQuery || isSubquery || !cteManager.hasCtes() && statementType != DbmsStatementType.DELETE) {
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
                
                if (!dbmsDialect.supportsJoinsInRecursiveCte() && cteInfo.recursiveCriteriaBuilder.joinManager.hasJoins()) {
                    throw new IllegalStateException("The dbms dialect does not support joins in the recursive part of a CTE!");
                }
                
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
            
            String cteName = cteInfo.cteType.getName();
            sb.append(cteName);
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
        }

        // Add cascading delete statements from base query as CTEs
        firstCte = applyCascadingDelete(baseQuery, this, participatingQueries, sb, "main_query", firstCte);
        
        // If no CTE has been added, we can just return
        if (firstCte) {
            return null;
        }

        for (CTEInfo cteInfo : cteManager.getCtes()) {
            String cteName = cteInfo.cteType.getName();
            // TODO: this is a hibernate specific integration detail
            // Replace the subview subselect that is generated for this cte
            final String subselect = "( select * from " + cteName + " )";
            int subselectIndex = 0;
            while ((subselectIndex = sb.indexOf(subselect, subselectIndex)) > -1) {
                sb.replace(subselectIndex, subselectIndex + subselect.length(), cteName);
            }

            final String mainSubselect = "( select * from " + cteName + " )";
            subselectIndex = 0;
            while ((subselectIndex = sqlSb.indexOf(mainSubselect, subselectIndex)) > -1) {
                sqlSb.replace(subselectIndex, subselectIndex + mainSubselect.length(), cteName);
            }
        }
        
        sb.append("\n");
        
        for (Map.Entry<String, String> tableNameRemappingEntry : tableNameRemapping.entrySet()) {
            String sqlAlias = cbf.getExtendedQuerySupport().getSqlAlias(em, baseQuery, tableNameRemappingEntry.getKey());
            String newCteName = tableNameRemappingEntry.getValue();

            applyTableNameRemapping(sqlSb, sqlAlias, newCteName);
        }
        
        return sb;
    }
    
    private boolean applyCascadingDelete(Query baseQuery, AbstractCommonQueryBuilder<?, ?, ?, ?, ?> queryBuilder, List<Query> participatingQueries, StringBuilder sb, String cteBaseName, boolean firstCte) {
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
                
                sb.append(cteBaseName);
                sb.append('_').append(cteBaseNameCount++);
                sb.append(" AS (\n");

                cascadingDeleteSqlSb.setLength(0);
                cascadingDeleteSqlSb.append(cascadingDeleteSql);
                dbmsDialect.appendExtendedSql(cascadingDeleteSqlSb, DbmsStatementType.DELETE, false, true, null, null, null, null, null);
                sb.append(cascadingDeleteSqlSb);
                
                sb.append("\n)");
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
    
    protected Map<String, String> applyExtendedSql(StringBuilder sqlSb, boolean isSubquery, boolean isEmbedded, StringBuilder withClause, String[] returningColumns, Map<DbmsModificationState, String> includedModificationStates) {
        String limit = null;
        String offset = null;
        
        if (firstResult != 0) {
            offset = Integer.toString(firstResult);
        }
        if (maxResults != Integer.MAX_VALUE) {
            limit = Integer.toString(maxResults);
        }
        
        return dbmsDialect.appendExtendedSql(sqlSb, statementType, isSubquery, isEmbedded, withClause, limit, offset, returningColumns, includedModificationStates);
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
    
    private boolean isCompatibleModeEnabled() {
        return PropertyUtils.getAsBooleanProperty(mainQuery.properties, ConfigurationProperties.COMPATIBLE_MODE, false);
    }
    
    private boolean isImplicitGroupByFromSelect() {
    	return PropertyUtils.getAsBooleanProperty(mainQuery.properties, ConfigurationProperties.IMPLICIT_GROUP_BY_FROM_SELECT, true);
    }
    
    private boolean isImplicitGroupByFromHaving() {
    	return PropertyUtils.getAsBooleanProperty(mainQuery.properties, ConfigurationProperties.IMPLICIT_GROUP_BY_FROM_HAVING, true);
    }
    
    private boolean isImplicitGroupByFromOrderBy() {
    	return PropertyUtils.getAsBooleanProperty(mainQuery.properties, ConfigurationProperties.IMPLICIT_GROUP_BY_FROM_ORDER_BY, true);
    }

    // TODO: needs equals-hashCode implementation
}
