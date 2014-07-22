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
import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.HavingOrBuilder;
import com.blazebit.persistence.JoinType;
import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.QueryBuilder;
import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.SelectObjectBuilder;
import com.blazebit.persistence.SimpleCaseWhenBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.WhereOrBuilder;
import com.blazebit.persistence.spi.QueryTransformer;
import java.lang.reflect.Constructor;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.Parameter;
import javax.persistence.TemporalType;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;

/**
 *
 * @author ccbem
 */
public class BaseQueryBuilderImpl<T, U extends BaseQueryBuilder<U>> implements BaseQueryBuilder<U>{
    
    protected static final Logger log = Logger.getLogger(CriteriaBuilderImpl.class.getName());
    protected static final String idParamName = "ids";

    protected final Class<?> fromClazz;
    protected Class<T> resultClazz;
    protected final EntityManager em;
    protected final QueryTransformer queryTransformer;

    protected final ParameterManager parameterManager;
    protected final SelectManager<T> selectManager;
    protected final WhereManager<U> whereManager;
    protected final HavingManager<U> havingManager;
    protected final GroupByManager groupByManager;
    protected final OrderByManager orderByManager;
    protected final JoinManager joinManager;
    private final QueryGenerator queryGenerator;

    /**
     * Create flat copy of builder
     *
     * @param builder
     */
    protected BaseQueryBuilderImpl(BaseQueryBuilderImpl<T, ? extends BaseQueryBuilder<?>> builder) {
        this.fromClazz = builder.fromClazz;
        this.resultClazz = builder.resultClazz;
        this.orderByManager = builder.orderByManager;
        this.parameterManager = builder.parameterManager;
        this.selectManager = builder.selectManager;
        this.whereManager = (WhereManager<U>) builder.whereManager;
        this.havingManager = (HavingManager<U>) builder.havingManager;
        this.groupByManager = builder.groupByManager;
        this.joinManager = builder.joinManager;
        this.queryGenerator = builder.queryGenerator;
        this.em = builder.em;
        this.queryTransformer = builder.queryTransformer;
    }

    public BaseQueryBuilderImpl(EntityManager em, Class<T> clazz, String alias) {
        if(em == null){
            throw new NullPointerException("em");
        }
        if(alias == null){
            throw new NullPointerException("alias");
        }
        if(clazz == null){
            throw new NullPointerException("clazz");
        }
        
        this.fromClazz = this.resultClazz = clazz;
        
        this.joinManager = new JoinManager(alias, clazz);
                
        this.parameterManager = new ParameterManager();
        
        this.queryGenerator = new QueryGenerator(parameterManager);
        
        
        this.whereManager = new WhereManager<U>(queryGenerator, parameterManager);
        this.havingManager = new HavingManager<U>(queryGenerator, parameterManager);
        this.groupByManager = new GroupByManager(queryGenerator, parameterManager);
                
        this.selectManager = new SelectManager<T>(queryGenerator, parameterManager);
        this.orderByManager = new OrderByManager(queryGenerator, parameterManager);
        
        //resolve cyclic dependencies
        this.queryGenerator.setSelectManager(selectManager);
        this.em = em;
        this.queryTransformer = getQueryTransformer();
    }
    

    /*
     * Select methods
     */
    @Override
    public U distinct() {
        selectManager.distinct();
        return (U) this;
    }

    /* CASE (WHEN condition THEN scalarExpression)+ ELSE scalarExpression END */
    @Override
    public CaseWhenBuilder<U> selectCase() {
        return new CaseWhenBuilderImpl<U>((U) this);
    }

    /* CASE caseOperand (WHEN scalarExpression THEN scalarExpression)+ ELSE scalarExpression END */
    @Override
    public SimpleCaseWhenBuilder<U> selectCase(String expression) {
        return new SimpleCaseWhenBuilderImpl<U>((U) this, expression);
    }

    @Override
    public CriteriaBuilder<Tuple> select(String... expressions) {
        for (String expression : expressions) {
            select(expression);
        }
        return (CriteriaBuilder<Tuple>) this;
    }

    @Override
    public CriteriaBuilder<Tuple> select(String expression) {
        return select(expression, null);
    }

    @Override
    public CriteriaBuilder<Tuple> select(String expression, String selectAlias) {
        if (expression == null) {
            throw new NullPointerException("expression");
        }
        if (expression.isEmpty() || (selectAlias != null && selectAlias.isEmpty())) {
            throw new IllegalArgumentException("selectAlias");
        }
        verifyBuilderEnded();
        resultClazz = (Class<T>) Tuple.class;
        selectManager.select(this, expression, selectAlias);
        return (CriteriaBuilder<Tuple>) this;
    }
    
    /*
     * Where methods
     */
    @Override
    public RestrictionBuilder<U> where(String expression) {
        if (expression == null) {
            throw new NullPointerException("expression");
        }
        if (expression.isEmpty()) {
            throw new IllegalArgumentException("expression");
        }
        return whereManager.restrict(this, expression);
    }

    @Override
    public WhereOrBuilder<U> whereOr() {
        return whereManager.whereOr(this);
    }

    @Override
    public SubqueryInitiator whereExists() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /*
     * Group by methods
     */
    @Override
    public U groupBy(String... paths) {
        for (String path : paths) {
            groupBy(path);
        }
        return (U) this;
    }

    @Override
    public U groupBy(String expression) {
        verifyBuilderEnded();
        groupByManager.groupBy(expression);
        return (U) this;
    }

    /*
     * Having methods
     */
    @Override
    public RestrictionBuilder<U> having(String expression) {
        if (groupByManager.getGroupByInfos().isEmpty()) {
            throw new IllegalStateException();
        }
        return havingManager.restrict(this, expression);
    }

    @Override
    public HavingOrBuilder<U> havingOr() {
        return havingManager.havingOr(this);
    }

    @Override
    public SubqueryInitiator havingExists() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /*
     * Order by methods
     */
    @Override
    public U orderByDesc(String path) {
        return orderBy(path, false, false);
    }

    @Override
    public U orderByAsc(String path) {
        return orderBy(path, true, false);
    }

    @Override
    public U orderByDesc(String path, boolean nullFirst) {
        return orderBy(path, false, nullFirst);
    }

    @Override
    public U orderByAsc(String path, boolean nullFirst) {
        return orderBy(path, true, nullFirst);
    }

    protected void verifyBuilderEnded() {
        whereManager.verifyBuilderEnded();
        havingManager.verifyBuilderEnded();
        selectManager.verifyBuilderEnded();
    }

    

    @Override
    public U orderBy(String expression, boolean ascending, boolean nullFirst) {
        if (expression == null) {
            throw new NullPointerException("expression");
        }
        if (expression.isEmpty()) {
            throw new IllegalArgumentException("expression");
        }
        verifyBuilderEnded();
        orderByManager.orderBy(expression, ascending, nullFirst);
        return (U) this;
    }

    /*
     * Join methods
     */
    @Override
    public U innerJoin(String path, String alias) {
        return join(path, alias, JoinType.INNER);
    }

    @Override
    public U leftJoin(String path, String alias) {
        return join(path, alias, JoinType.LEFT);
    }

    @Override
    public U rightJoin(String path, String alias) {
        return join(path, alias, JoinType.RIGHT);
    }

    @Override
    public U outerJoin(String path, String alias) {
        return join(path, alias, JoinType.OUTER);
    }

    
    @Override
    public U join(String path, String alias, JoinType type) {
        if (path == null || alias == null || type == null) {
            throw new NullPointerException();
        }
        if (alias.isEmpty()) {
            throw new IllegalArgumentException();
        }
        verifyBuilderEnded();
        joinManager.join(path, alias, type, false);
        return (U) this;
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
    
    protected void applyArrayTransformations(){
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
        whereManager.rootPredicate.predicate.getChildren().addAll(arrayTransformer.getAdditionalWherePredicates());
    }

    private QueryTransformer getQueryTransformer() {
        ServiceLoader<QueryTransformer> serviceLoader = ServiceLoader.load(QueryTransformer.class);
        Iterator<QueryTransformer> iterator = serviceLoader.iterator();
        
        if (iterator.hasNext()) {
            return iterator.next();
        }
        
        throw new IllegalStateException("No QueryTransformer found on the class path. Please check if a valid implementation is on the class path.");
    }
    
    
}
