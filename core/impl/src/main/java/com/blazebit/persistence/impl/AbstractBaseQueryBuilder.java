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
import com.blazebit.persistence.CaseWhenBuilder;
import com.blazebit.persistence.HavingOrBuilder;
import com.blazebit.persistence.JoinType;
import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.SimpleCaseWhenBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.WhereOrBuilder;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.spi.QueryTransformer;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;

/**
 *
 * @param <T> The query result type
 * @param <X> The concrete builder type
 * @author Moritz Becker
 * @author Christian Beikov
 */
public class AbstractBaseQueryBuilder<T, X extends BaseQueryBuilder<T, X>> implements BaseQueryBuilder<T, X> {

    protected static final Logger LOG = Logger.getLogger(CriteriaBuilderImpl.class.getName());
    protected static final String idParamName = "ids";

    protected final CriteriaBuilderFactoryImpl cbf;
    protected final Class<?> fromClazz;
    protected final EntityManager em;

    protected final ParameterManager parameterManager;
    protected final SelectManager<T> selectManager;
    protected final WhereManager<X> whereManager;
    protected final HavingManager<X> havingManager;
    protected final GroupByManager groupByManager;
    protected final OrderByManager orderByManager;
    protected final JoinManager joinManager;
    private final QueryGenerator queryGenerator;
    private final SubqueryInitiatorFactory subqueryInitFactory;

    private final JPAInfo jpaInfo;

    private final BuilderEndedListenerImpl subqueryBuilderListener = new BuilderEndedListenerImpl();

    private final AliasManager aliasManager;
    private final ExpressionFactory expressionFactory;

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
        this.joinManager = builder.joinManager;
        this.queryGenerator = builder.queryGenerator;
        this.em = builder.em;
        this.jpaInfo = builder.jpaInfo;
        this.subqueryInitFactory = builder.subqueryInitFactory;
        this.aliasManager = builder.aliasManager;
        this.expressionFactory = builder.expressionFactory;
    }

    protected AbstractBaseQueryBuilder(CriteriaBuilderFactoryImpl cbf, EntityManager em, Class<T> resultClazz, Class<?> fromClazz, String alias, ParameterManager parameterManager, AliasManager aliasManager, ExpressionFactory expressionFactory) {

        if (cbf == null) {
            throw new NullPointerException("cbf");
        }
        if (em == null) {
            throw new NullPointerException("em");
        }
        if (fromClazz == null) {
            throw new NullPointerException("fromClazz");
        }
        if (resultClazz == null) {
            throw new NullPointerException("resultClazz");
        }

        this.cbf = cbf;
        this.jpaInfo = new JPAInfo(em);
        this.fromClazz = fromClazz;
        this.aliasManager = new AliasManager(aliasManager);
        this.expressionFactory = expressionFactory;
        
        this.parameterManager = parameterManager;

        this.subqueryInitFactory = new SubqueryInitiatorFactory(cbf, em, parameterManager, this.aliasManager, expressionFactory);

        this.queryGenerator = new QueryGenerator(this);

        this.joinManager = new JoinManager(alias, fromClazz, queryGenerator, jpaInfo, this.aliasManager, this, em.getMetamodel());
        this.whereManager = new WhereManager<X>(queryGenerator, parameterManager, subqueryInitFactory, expressionFactory);
        this.havingManager = new HavingManager<X>(queryGenerator, parameterManager, subqueryInitFactory, expressionFactory);
        this.groupByManager = new GroupByManager(queryGenerator, parameterManager);

        this.selectManager = new SelectManager<T>(queryGenerator, parameterManager, this.aliasManager, this, subqueryInitFactory, expressionFactory);
        this.orderByManager = new OrderByManager(queryGenerator, parameterManager);

        //resolve cyclic dependencies
        this.queryGenerator.setSelectManager(selectManager);
        this.em = em;
    }

    public AbstractBaseQueryBuilder(CriteriaBuilderFactoryImpl cbf, EntityManager em, Class<T> clazz, String alias, ExpressionFactory expressionFactory) {
        this(cbf, em, clazz, clazz, alias, new ParameterManager(), new AliasManager(), expressionFactory);
    }


    /*
     * Select methods
     */
    @Override
    public X distinct() {
        selectManager.distinct();
        return (X) this;
    }

    /* CASE (WHEN condition THEN scalarExpression)+ ELSE scalarExpression END */
    @Override
    public CaseWhenBuilder<X> selectCase() {
        return new CaseWhenBuilderImpl<X>((X) this, subqueryInitFactory, expressionFactory);
    }

    /* CASE caseOperand (WHEN scalarExpression THEN scalarExpression)+ ELSE scalarExpression END */
    @Override
    public SimpleCaseWhenBuilder<X> selectCase(String expression) {
        return new SimpleCaseWhenBuilderImpl<X>((X) this, expressionFactory, expression);
    }

    @Override
    public BaseQueryBuilder<Tuple, ?> select(String expression) {
        return select(expression, null);
    }

    @Override
    public BaseQueryBuilder<Tuple, ?> select(String expression, String selectAlias) {
        Expression expr = expressionFactory.createSimpleExpression(expression);
        if (selectAlias != null && selectAlias.isEmpty()) {
            throw new IllegalArgumentException("selectAlias");
        }
        verifyBuilderEnded();
        selectManager.select(this, expr, selectAlias);
        return (BaseQueryBuilder<Tuple, ?>) this;
    }

    /*
     * Where methods
     */
    @Override
    public RestrictionBuilder<X> where(String expression) {
        Expression expr = expressionFactory.createSimpleExpression(expression);
        return whereManager.restrict(this, expr);
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

    /*
     * Group by methods
     */
    @Override
    public X groupBy(String... paths) {
        for (String path : paths) {
            groupBy(path);
        }
        return (X) this;
    }

    @Override
    public X groupBy(String expression) {
        Expression expr = expressionFactory.createSimpleExpression(expression);
        verifyBuilderEnded();
        groupByManager.groupBy(expr);
        return (X) this;
    }

    /*
     * Having methods
     */
    @Override
    public RestrictionBuilder<X> having(String expression) {
        if (groupByManager.getGroupByInfos()
                .isEmpty()) {
            throw new IllegalStateException("Having without group by");
        }
        Expression expr = expressionFactory.createSimpleExpression(expression);
        return havingManager.restrict(this, expr);
    }

    @Override
    public HavingOrBuilder<X> havingOr() {
        if (groupByManager.getGroupByInfos()
                .isEmpty()) {
            throw new IllegalStateException("Having without group by");
        }
        return havingManager.havingOr(this);
    }

    @Override
    public SubqueryInitiator<X> havingExists() {
        if (groupByManager.getGroupByInfos()
                .isEmpty()) {
            throw new IllegalStateException("Having without group by");
        }
        return havingManager.restrictExists((X) this);
    }
    
    @Override
    public SubqueryInitiator<X> havingNotExists() {
        if (groupByManager.getGroupByInfos()
                .isEmpty()) {
            throw new IllegalStateException("Having without group by");
        }
        return havingManager.restrictNotExists((X) this);
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

    protected void verifyBuilderEnded() {
        whereManager.verifyBuilderEnded();
        havingManager.verifyBuilderEnded();
        selectManager.verifyBuilderEnded();
        subqueryBuilderListener.verifySubqueryBuilderEnded();
    }

    @Override
    public X orderBy(String expression, boolean ascending, boolean nullFirst) {
        Expression expr = expressionFactory.createSimpleExpression(expression);
        verifyBuilderEnded();
        orderByManager.orderBy(expr, ascending, nullFirst);
        return (X) this;
    }

    /*
     * Join methods
     */
    @Override
    public X innerJoin(String path, String alias) {
        return join(path, alias, JoinType.INNER);
    }

    @Override
    public X leftJoin(String path, String alias) {
        return join(path, alias, JoinType.LEFT);
    }

    @Override
    public X rightJoin(String path, String alias) {
        return join(path, alias, JoinType.RIGHT);
    }

    @Override
    public X outerJoin(String path, String alias) {
        return join(path, alias, JoinType.OUTER);
    }

    @Override
    public X join(String path, String alias, JoinType type) {
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
        joinManager.join(path, alias, type, false);
        return (X) this;
    }

    protected void applyImplicitJoins() {
        final JoinVisitor joinVisitor = new JoinVisitor(joinManager, selectManager);
        // carry out implicit joins
        joinVisitor.setFromSelect(true);
        selectManager.acceptVisitor(joinVisitor);
        joinVisitor.setFromSelect(false);

        whereManager.acceptVisitor(joinVisitor);
        groupByManager.acceptVisitor(joinVisitor);

        havingManager.acceptVisitor(joinVisitor);
        joinVisitor.setJoinWithObjectLeafAllowed(false);
        orderByManager.acceptVisitor(joinVisitor);
        joinVisitor.setJoinWithObjectLeafAllowed(true);
    }

    protected void applyArrayTransformations() {
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
        ArrayExpressionTransformer arrayTransformer = new ArrayExpressionTransformer(joinManager);
        selectManager.applyTransformer(arrayTransformer);
        whereManager.applyTransformer(arrayTransformer);
        groupByManager.applyTransformer(arrayTransformer);
        orderByManager.applyTransformer(arrayTransformer);
    }

    @Override
    public String getQueryString() {
        verifyBuilderEnded();
        StringBuilder sb = new StringBuilder();
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
        applyArrayTransformations();

        sb.append(selectManager.buildSelect());
        if (sb.length() > 0) {
            sb.append(' ');
        }
        sb.append("FROM ")
                .append(fromClazz.getSimpleName())
                .append(' ')
                .append(joinManager.getRootAlias());
        joinManager.buildJoins(true, sb);
        whereManager.buildClause(sb);
        groupByManager.buildGroupBy(sb);
        havingManager.buildClause(sb);
        orderByManager.buildOrderBy(sb);
        return sb.toString();
    }

    protected void transformQuery(TypedQuery<T> query) {
        for (QueryTransformer transformer : cbf.getQueryTransformers()) {
            transformer.transformQuery(query, selectManager.getSelectObjectBuilder());
        }
    }
}
