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

import com.blazebit.persistence.BaseQueryBuilder;
import com.blazebit.persistence.CaseWhenStarterBuilder;
import com.blazebit.persistence.HavingOrBuilder;
import com.blazebit.persistence.JoinOnBuilder;
import com.blazebit.persistence.JoinType;
import com.blazebit.persistence.Keyset;
import com.blazebit.persistence.KeysetBuilder;
import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.SimpleCaseWhenStarterBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.WhereOrBuilder;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.impl.expression.SubqueryExpressionFactory;
import com.blazebit.persistence.impl.expression.VisitorAdapter;
import com.blazebit.persistence.impl.jpaprovider.JpaProvider;
import com.blazebit.persistence.impl.jpaprovider.JpaProviders;
import com.blazebit.persistence.impl.keyset.KeysetManager;
import com.blazebit.persistence.impl.keyset.KeysetBuilderImpl;
import com.blazebit.persistence.impl.keyset.KeysetImpl;
import com.blazebit.persistence.impl.keyset.KeysetLink;
import com.blazebit.persistence.impl.keyset.KeysetMode;
import com.blazebit.persistence.impl.keyset.SimpleKeysetLink;
import com.blazebit.persistence.internal.OrderByBuilderExperimental;
import com.blazebit.persistence.spi.QueryTransformer;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
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

/**
 *
 * @param <T> The query result type
 * @param <X> The concrete builder type
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public abstract class AbstractBaseQueryBuilder<T, X extends BaseQueryBuilder<T, X>> implements BaseQueryBuilder<T, X>, OrderByBuilderExperimental<X> {

    protected static final Logger LOG = Logger.getLogger(CriteriaBuilderImpl.class.getName());
    public static final String idParamName = "ids";

    protected final CriteriaBuilderFactoryImpl cbf;
    protected EntityType<?> fromClazz;
    protected final EntityManager em;

    protected final ParameterManager parameterManager;
    protected final SelectManager<T> selectManager;
    protected final WhereManager<X> whereManager;
    protected final HavingManager<X> havingManager;
    protected final GroupByManager groupByManager;
    protected final OrderByManager orderByManager;
    protected final JoinManager joinManager;
    protected final KeysetManager keysetManager;
    protected final ResolvingQueryGenerator queryGenerator;
    private final SubqueryInitiatorFactory subqueryInitFactory;

    protected final JpaProvider jpaProvider;
    protected final Set<String> registeredFunctions;

    protected final AliasManager aliasManager;
    protected final ExpressionFactory expressionFactory;

    private final List<ExpressionTransformer> transformers;
    private final SizeSelectToCountTransformer sizeSelectToCountTransformer;
    private final SizeSelectToSubqueryTransformer sizeSelectToSubqueryTransformer;
    private boolean fromClassExplicitelySet = false;

    // Mutable state
    protected Class<T> resultType;

    private boolean needsCheck = true;
    private boolean implicitJoinsApplied = false;

    // Cache
    protected String cachedQueryString;

    /**
     * Create flat copy of builder
     *
     * @param builder
     */
    protected AbstractBaseQueryBuilder(AbstractBaseQueryBuilder<T, ? extends BaseQueryBuilder<T, ?>> builder) {
        this.cbf = builder.cbf;
        this.fromClazz = builder.fromClazz;
        this.orderByManager = builder.orderByManager;
        this.parameterManager = builder.parameterManager;
        this.selectManager = builder.selectManager;
        this.whereManager = (WhereManager<X>) builder.whereManager;
        this.havingManager = (HavingManager<X>) builder.havingManager;
        this.groupByManager = builder.groupByManager;
        this.keysetManager = builder.keysetManager;
        this.joinManager = builder.joinManager;
        this.queryGenerator = builder.queryGenerator;
        this.em = builder.em;
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

    protected AbstractBaseQueryBuilder(CriteriaBuilderFactoryImpl cbf, EntityManager em, Class<T> resultClazz, String alias, ParameterManager parameterManager, AliasManager aliasManager, JoinManager parentJoinManager, ExpressionFactory expressionFactory, Set<String> registeredFunctions) {
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
        this.jpaProvider = JpaProviders.resolveJpaProvider(em);
        this.aliasManager = new AliasManager(aliasManager);
        this.expressionFactory = expressionFactory;

        this.parameterManager = parameterManager;

        this.registeredFunctions = registeredFunctions;
        this.queryGenerator = new ResolvingQueryGenerator(this.aliasManager, this.jpaProvider, registeredFunctions);

        this.joinManager = new JoinManager(queryGenerator, parameterManager, null, expressionFactory, jpaProvider, this.aliasManager, em.getMetamodel(),
                parentJoinManager);

        // set defaults
        try {
            this.fromClazz = em.getMetamodel().entity(resultClazz);
            this.joinManager.setRoot(fromClazz, alias);
        } catch(IllegalArgumentException ex) {
            this.fromClazz = null;
        }

        this.subqueryInitFactory = new SubqueryInitiatorFactory(cbf, em, parameterManager, this.aliasManager, joinManager, new SubqueryExpressionFactory(), registeredFunctions);

        this.joinManager.setSubqueryInitFactory(subqueryInitFactory);

        this.whereManager = new WhereManager<X>(queryGenerator, parameterManager, subqueryInitFactory, expressionFactory);
        this.havingManager = new HavingManager<X>(queryGenerator, parameterManager, subqueryInitFactory, expressionFactory);
        this.groupByManager = new GroupByManager(queryGenerator, parameterManager);

        this.selectManager = new SelectManager<T>(queryGenerator, parameterManager, this.aliasManager, subqueryInitFactory, expressionFactory, jpaProvider, resultClazz);
        this.orderByManager = new OrderByManager(queryGenerator, parameterManager, this.aliasManager);
        this.keysetManager = new KeysetManager(queryGenerator, parameterManager);

        //resolve cyclic dependencies
        this.em = em;

        this.transformers = Arrays.asList(new OuterFunctionTransformer(joinManager), new SubqueryRecursiveExpressionTransformer());
        this.sizeSelectToCountTransformer = new SizeSelectToCountTransformer(joinManager, groupByManager, orderByManager);
        this.sizeSelectToSubqueryTransformer = new SizeSelectToSubqueryTransformer(subqueryInitFactory, this.aliasManager);
        this.resultType = resultClazz;
    }

    public AbstractBaseQueryBuilder(CriteriaBuilderFactoryImpl cbf, EntityManager em, Class<T> clazz, String alias, Set<String> registeredFunctions) {
        this(cbf, em, clazz, alias, new ParameterManager(), null, null, cbf.getExpressionFactory(), registeredFunctions);
    }

    @Override
    public BaseQueryBuilder<T, ?> from(Class<?> clazz) {
        return from(clazz, clazz.getSimpleName().toLowerCase());
    }

    @Override
    public BaseQueryBuilder<T, ?> from(Class<?> clazz, String alias) {
        if (fromClassExplicitelySet) {
            throw new UnsupportedOperationException("Multiple from clauses are not supported at the moment");
        }
        this.fromClazz = em.getMetamodel().entity(clazz);
        this.joinManager.setRoot(fromClazz, alias);
        fromClassExplicitelySet = true;
        return this;
    }

    @Override
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

    @Override
    public X setParameter(String name, Object value) {
        parameterManager.satisfyParameter(name, value);
        return (X) this;
    }

    @Override
    public X setParameter(String name, Calendar value, TemporalType temporalType) {
        parameterManager.satisfyParameter(name, new ParameterManager.TemporalCalendarParameterWrapper(value, temporalType));
        return (X) this;
    }

    @Override
    public X setParameter(String name, Date value, TemporalType temporalType) {
        parameterManager.satisfyParameter(name, new ParameterManager.TemporalDateParameterWrapper(value, temporalType));
        return (X) this;
    }

    @Override
    public boolean containsParameter(String name) {
        return parameterManager.containsParameter(name);
    }

    @Override
    public boolean isParameterSet(String name) {
        return parameterManager.isParameterSet(name);
    }

    @Override
    public Parameter<?> getParameter(String name) {
        return parameterManager.getParameter(name);
    }

    @Override
    public Set<? extends Parameter<?>> getParameters() {
        return parameterManager.getParameters();
    }

    @Override
    public Object getParameterValue(String name) {
        return parameterManager.getParameterValue(name);
    }

    /*
     * Select methods
     */
    public X distinct() {
        clearCache();
        selectManager.distinct();
        return (X) this;
    }

    @Override
    public CaseWhenStarterBuilder<? extends BaseQueryBuilder<T, ?>> selectCase() {
        return selectCase(null);
    }

    /* CASE (WHEN condition THEN scalarExpression)+ ELSE scalarExpression END */
    @Override
    public CaseWhenStarterBuilder<? extends BaseQueryBuilder<T, ?>> selectCase(String selectAlias) {
        if (selectAlias != null && selectAlias.isEmpty()) {
            throw new IllegalArgumentException("selectAlias");
        }
        return selectManager.selectCase((BaseQueryBuilder<T, ?>) this, selectAlias);
    }

    @Override
    public SimpleCaseWhenStarterBuilder<? extends BaseQueryBuilder<T, ?>> selectSimpleCase(String expression) {
        return selectSimpleCase(expression, null);
    }

    /* CASE caseOperand (WHEN scalarExpression THEN scalarExpression)+ ELSE scalarExpression END */
    @Override
    public SimpleCaseWhenStarterBuilder<? extends BaseQueryBuilder<T, ?>> selectSimpleCase(String caseOperandExpression, String selectAlias) {
        if (selectAlias != null && selectAlias.isEmpty()) {
            throw new IllegalArgumentException("selectAlias");
        }
        return selectManager.selectSimpleCase((BaseQueryBuilder<T, ?>) this, selectAlias, expressionFactory.createCaseOperandExpression(caseOperandExpression));
    }

    @Override
    public BaseQueryBuilder<T, ?> select(String expression) {
        return select(expression, null);
    }

    @Override
    public BaseQueryBuilder<T, ?> select(String expression, String selectAlias) {
        Expression expr = expressionFactory.createSimpleExpression(expression);
        if (selectAlias != null && selectAlias.isEmpty()) {
            throw new IllegalArgumentException("selectAlias");
        }
        verifyBuilderEnded();
        selectManager.select(this, expr, selectAlias);
        resultType = (Class<T>) Tuple.class;
        return (BaseQueryBuilder<T, ?>) this;
    }

    @Override
    public SubqueryInitiator<? extends BaseQueryBuilder<T, ?>> selectSubquery() {
        return selectSubquery(null);
    }

    @Override
    public SubqueryInitiator<? extends BaseQueryBuilder<T, ?>> selectSubquery(String selectAlias) {
        if (selectAlias != null && selectAlias.isEmpty()) {
            throw new IllegalArgumentException("selectAlias");
        }
        verifyBuilderEnded();
        return selectManager.selectSubquery((BaseQueryBuilder<T, ?>) this, selectAlias);
    }

    @Override
    public SubqueryInitiator<? extends BaseQueryBuilder<T, ?>> selectSubquery(String subqueryAlias, String expression) {
        return selectSubquery(subqueryAlias, expression, null);
    }

    @Override
    public SubqueryInitiator<? extends BaseQueryBuilder<T, ?>> selectSubquery(String subqueryAlias, String expression, String selectAlias) {
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
        return selectManager.selectSubquery((BaseQueryBuilder<T, ?>) this, subqueryAlias, expressionFactory.createSimpleExpression(expression), selectAlias);
    }

    /*
     * Where methods
     */
    @Override
    public RestrictionBuilder<X> where(String expression) {
        Expression expr = expressionFactory.createSimpleExpression(expression);
        return whereManager.restrict(this, expr);
    }

    /*
     * Where methods
     */
    @Override
    public CaseWhenStarterBuilder<RestrictionBuilder<X>> whereCase() {
        return whereManager.restrictCase(this);
    }

    @Override
    public SimpleCaseWhenStarterBuilder<RestrictionBuilder<X>> whereSimpleCase(String expression) {
        return whereManager.restrictSimpleCase(this, expressionFactory.createCaseOperandExpression(expression));
    }

    @Override
    public WhereOrBuilder<X> whereOr() {
        return whereManager.whereOr(this);
    }

    @Override
    public SubqueryInitiator<X> whereExists() {
        return whereManager.restrictExists((X) this);
    }

    @Override
    public SubqueryInitiator<X> whereNotExists() {
        return whereManager.restrictNotExists((X) this);
    }

    @Override
    public SubqueryInitiator<RestrictionBuilder<X>> whereSubquery() {
        return whereManager.restrict(this);
    }

    @Override
    public SubqueryInitiator<RestrictionBuilder<X>> whereSubquery(String subqueryAlias, String expression) {
        return whereManager.restrict(this, subqueryAlias, expression);
    }

    /*
     * Group by methods
     */
    public X groupBy(String... paths) {
        for (String path : paths) {
            groupBy(path);
        }
        return (X) this;
    }

    public X groupBy(String expression) {
        clearCache();
        Expression expr = expressionFactory.createPathExpression(expression);
        verifyBuilderEnded();
        groupByManager.groupBy(expr);
        return (X) this;
    }

    /*
     * Having methods
     */
    public RestrictionBuilder<X> having(String expression) {
        clearCache();
        if (groupByManager.getGroupByInfos().isEmpty()) {
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
        if (groupByManager.getGroupByInfos().isEmpty()) {
            throw new IllegalStateException("Having without group by");
        }
        return havingManager.havingOr(this);
    }

    public SubqueryInitiator<X> havingExists() {
        clearCache();
        if (groupByManager.getGroupByInfos().isEmpty()) {
            throw new IllegalStateException("Having without group by");
        }
        return havingManager.restrictExists((X) this);
    }

    public SubqueryInitiator<X> havingNotExists() {
        clearCache();
        if (groupByManager.getGroupByInfos().isEmpty()) {
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
    @Override
    public X orderByDesc(String expression) {
        return orderBy(expression, false, false);
    }

    @Override
    public X orderByAsc(String expression) {
        return orderBy(expression, true, false);
    }

    @Override
    public X orderByDesc(String expression, boolean nullFirst) {
        return orderBy(expression, false, nullFirst);
    }

    @Override
    public X orderByAsc(String expression, boolean nullFirst) {
        return orderBy(expression, true, nullFirst);
    }
    
    @Override
    public X orderBy(String expression, boolean ascending, boolean nullFirst) {
        _orderBy(expressionFactory.createOrderByExpression(expression), ascending, nullFirst);
        return (X) this;
    }
    
    /*
     * Experimental order by methods
     */
    
    @Override
    public X orderByFunctionDesc(String expression) {
        return orderByFunction(expression, false, false);
    }

    @Override
    public X orderByFunctionAsc(String expression) {
        return orderByFunction(expression, true, false);
    }

    @Override
    public X orderByFunctionDesc(String expression, boolean nullFirst) {
        return orderByFunction(expression, false, nullFirst);
    }

    @Override
    public X orderByFunctionAsc(String expression, boolean nullFirst) {
        return orderByFunction(expression, true, nullFirst);
    }
    
    @Override
    public X orderByFunction(String expression, boolean ascending, boolean nullFirst) {
        _orderBy(expressionFactory.createSimpleExpression(expression), ascending, nullFirst);
        return (X) this;
    }
    
    protected void verifyBuilderEnded() {
        whereManager.verifyBuilderEnded();
        havingManager.verifyBuilderEnded();
        selectManager.verifyBuilderEnded();
        joinManager.verifyBuilderEnded();
    }

    public void _orderBy(Expression expression, boolean ascending, boolean nullFirst){
        clearCache();
        verifyBuilderEnded();
        orderByManager.orderBy(expression, ascending, nullFirst);
    }

    /*
     * Join methods
     */
    @Override
    public X innerJoin(String path, String alias) {
        return join(path, alias, JoinType.INNER);
    }

    @Override
    public X innerJoinDefault(String path, String alias) {
        return joinDefault(path, alias, JoinType.INNER);
    }

    @Override
    public X leftJoin(String path, String alias) {
        return join(path, alias, JoinType.LEFT);
    }

    @Override
    public X leftJoinDefault(String path, String alias) {
        return joinDefault(path, alias, JoinType.LEFT);
    }

    @Override
    public X rightJoin(String path, String alias) {
        return join(path, alias, JoinType.RIGHT);
    }

    @Override
    public X rightJoinDefault(String path, String alias) {
        return joinDefault(path, alias, JoinType.RIGHT);
    }

    @Override
    public X join(String path, String alias, JoinType type) {
        clearCache();
        checkJoinPreconditions(path, alias, type);
        joinManager.join(path, alias, type, false, false);
        return (X) this;
    }

    @Override
    public X joinDefault(String path, String alias, JoinType type) {
        clearCache();
        checkJoinPreconditions(path, alias, type);
        joinManager.join(path, alias, type, false, true);
        return (X) this;
    }

    @Override
    public JoinOnBuilder<X> joinOn(String path, String alias, JoinType type) {
        clearCache();
        checkJoinPreconditions(path, alias, type);
        return joinManager.joinOn((X) this, path, alias, type, false);
    }

    @Override
    public JoinOnBuilder<X> joinDefaultOn(String path, String alias, JoinType type) {
        clearCache();
        checkJoinPreconditions(path, alias, type);
        return joinManager.joinOn((X) this, path, alias, type, true);
    }

    @Override
    public JoinOnBuilder<X> innerJoinOn(String path, String alias) {
        return joinOn(path, alias, JoinType.INNER);
    }

    @Override
    public JoinOnBuilder<X> innerJoinDefaultOn(String path, String alias) {
        return joinDefaultOn(path, alias, JoinType.INNER);
    }

    @Override
    public JoinOnBuilder<X> leftJoinOn(String path, String alias) {
        return joinOn(path, alias, JoinType.LEFT);
    }

    @Override
    public JoinOnBuilder<X> leftJoinDefaultOn(String path, String alias) {
        return joinDefaultOn(path, alias, JoinType.LEFT);
    }

    @Override
    public JoinOnBuilder<X> rightJoinOn(String path, String alias) {
        return joinOn(path, alias, JoinType.RIGHT);
    }

    @Override
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
        selectManager.acceptVisitor(joinVisitor);

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
            if (joinManager.hasCollections()) {
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

        // Problem we must have the complete (i.e. including array indices) absolute path available during array transformation of a path expression
        // since the path expression might not be based on the root node
        // we must track absolute paths to detect redundancies
        // However, the absolute path in the path expression's join node does not contain information about the indices so far but it would
        // also be a wrong match to add the indices in this structure since there can be multiple indices for the same join path element
        // consider d.contacts[l] and d.contacts[x], the absolute join path is d.contacts but this path occurs with two different indices
        // So where should be store this information or from where should we retrieve it during arrayTransformation?
        // i think the answer is: we can't
        // d.contacts[1].localized[1]
        // d.contacts contacts, contacts.localized localized
        // or we remember the already transfomred path in a Set<(BaseNode, RelativePath)> - maybe this would be sufficient
        // because access to the same array with two different indices has an empty result set anyway. so if we had basePaths with
        // two different indices for the same array we would output the two accesses for the subpath and the access for the current path just once (and not once for each distinct subpath)
        for (ExpressionTransformer transformer : transformers) {
            joinManager.applyTransformer(transformer);
            selectManager.applyTransformer(transformer);
            whereManager.applyTransformer(transformer);
            groupByManager.applyTransformer(transformer);
            orderByManager.applyTransformer(transformer);
        }

        applySizeSelectTransformer();
    }

    @Override
    public Class<T> getResultType() {
        return resultType;
    }

    @Override
    public String getQueryString() {
        prepareAndCheck();
        return getQueryString0();
    }

    @Override
    public KeysetBuilder<X> beforeKeyset() {
        clearCache();
        return keysetManager.startBuilder(new KeysetBuilderImpl<X>((X) this, keysetManager, KeysetMode.PREVIOUS));
    }

    @Override
    public X beforeKeyset(Serializable... values) {
        return beforeKeyset(new KeysetImpl(values));
    }

    @Override
    public X beforeKeyset(Keyset keyset) {
        clearCache();
        keysetManager.verifyBuilderEnded();
        keysetManager.setKeysetLink(new SimpleKeysetLink(keyset, KeysetMode.PREVIOUS));
        return (X) this;
    }

    @Override
    public KeysetBuilder<X> afterKeyset() {
        clearCache();
        return keysetManager.startBuilder(new KeysetBuilderImpl<X>((X) this, keysetManager, KeysetMode.NEXT));
    }

    @Override
    public X afterKeyset(Serializable... values) {
        return afterKeyset(new KeysetImpl(values));
    }

    @Override
    public X afterKeyset(Keyset keyset) {
        clearCache();
        keysetManager.verifyBuilderEnded();
        keysetManager.setKeysetLink(new SimpleKeysetLink(keyset, KeysetMode.NEXT));
        return (X) this;
    }

    private String getQueryString0() {
        if (cachedQueryString == null) {
            cachedQueryString = getQueryString1();
        }

        return cachedQueryString;
    }

    protected void clearCache() {
        needsCheck = true;
        cachedQueryString = null;
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

    private String getQueryString1() {
        StringBuilder sbSelectFrom = new StringBuilder();

        sbSelectFrom.append(selectManager.buildSelect(joinManager.getRootAlias()));
        sbSelectFrom.append(" FROM ")
                .append(fromClazz.getName())
                .append(' ')
                .append(joinManager.getRootAlias());

        joinManager.buildJoins(sbSelectFrom, EnumSet.noneOf(ClauseType.class), null);
        
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

        Set<String> clauses = new LinkedHashSet<String>();
        clauses.addAll(groupByManager.buildGroupByClauses());
        if (selectManager.hasAggregateFunctions()) {
            clauses.addAll(selectManager.buildGroupByClauses(em.getMetamodel()));
            clauses.addAll(orderByManager.buildGroupByClauses());
        }
        groupByManager.buildGroupBy(sbSelectFrom, clauses);

        havingManager.buildClause(sbSelectFrom);
        queryGenerator.setResolveSelectAliases(false);
        orderByManager.buildOrderBy(sbSelectFrom, false, false);
        queryGenerator.setResolveSelectAliases(true);
        return sbSelectFrom.toString();
    }

    protected <T> TypedQuery<T> transformQuery(TypedQuery<T> query) {
        TypedQuery<T> currentQuery = query;
        for (QueryTransformer transformer : cbf.getQueryTransformers()) {
            currentQuery = (TypedQuery<T>) transformer.transformQuery(query, selectManager.getSelectObjectBuilder());
        }
        return currentQuery;
    }

    // TODO: needs equals-hashCode implementation
}
