/*
 * Copyright 2014 - 2016 Blazebit.
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

import java.lang.reflect.Constructor;
import java.util.*;

import javax.persistence.Tuple;
import javax.persistence.metamodel.Metamodel;

import com.blazebit.persistence.CaseWhenStarterBuilder;
import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.MultipleSubqueryInitiator;
import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.SelectObjectBuilder;
import com.blazebit.persistence.SimpleCaseWhenStarterBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.impl.builder.expression.CaseWhenBuilderImpl;
import com.blazebit.persistence.impl.builder.expression.ExpressionBuilder;
import com.blazebit.persistence.impl.builder.expression.ExpressionBuilderEndedListener;
import com.blazebit.persistence.impl.builder.expression.ExpressionBuilderEndedListenerImpl;
import com.blazebit.persistence.impl.builder.expression.SimpleCaseWhenBuilderImpl;
import com.blazebit.persistence.impl.builder.expression.SuperExpressionSubqueryBuilderListener;
import com.blazebit.persistence.impl.builder.object.ClassObjectBuilder;
import com.blazebit.persistence.impl.builder.object.ConstructorObjectBuilder;
import com.blazebit.persistence.impl.builder.object.SelectObjectBuilderImpl;
import com.blazebit.persistence.impl.builder.object.TupleObjectBuilder;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.Expression.ResultVisitor;
import com.blazebit.persistence.impl.expression.Expression.Visitor;
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.impl.expression.PathElementExpression;
import com.blazebit.persistence.impl.expression.PathExpression;
import com.blazebit.persistence.impl.expression.PropertyExpression;
import com.blazebit.persistence.impl.expression.SimplePathReference;
import com.blazebit.persistence.impl.expression.SubqueryExpression;
import com.blazebit.persistence.impl.transform.ExpressionTransformer;
import com.blazebit.persistence.impl.transform.NodeInfoExpressionModifier;
import com.blazebit.persistence.impl.transform.SelectInfoTransformer;
import com.blazebit.persistence.spi.JpaProvider;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public class SelectManager<T> extends AbstractManager {

    private final List<SelectInfo> selectInfos = new ArrayList<SelectInfo>();
    private boolean distinct = false;
    private SelectObjectBuilderImpl<?> selectObjectBuilder;
    private ObjectBuilder<T> objectBuilder;
    private SubqueryBuilderListenerImpl<?> subqueryBuilderListener;
    // needed for tuple/alias matching
    private final Map<String, Integer> selectAliasToPositionMap = new HashMap<String, Integer>();
    private final SelectObjectBuilderEndedListenerImpl selectObjectBuilderEndedListener = new SelectObjectBuilderEndedListenerImpl();
    private CaseExpressionBuilderListener caseExpressionBuilderListener;
    private final JoinManager joinManager;
    private final AliasManager aliasManager;
    private final SubqueryInitiatorFactory subqueryInitFactory;
    private final ExpressionFactory expressionFactory;
    private final JpaProvider jpaProvider;
    private final GroupByUsableDetectionVisitor groupByUsableDetectionVisitor = new GroupByUsableDetectionVisitor(false);
    
    @SuppressWarnings("unchecked")
    public SelectManager(ResolvingQueryGenerator queryGenerator, ParameterManager parameterManager, JoinManager joinManager, AliasManager aliasManager, SubqueryInitiatorFactory subqueryInitFactory, ExpressionFactory expressionFactory, JpaProvider jpaProvider, Class<?> resultClazz) {
        super(queryGenerator, parameterManager);
        this.joinManager = joinManager;
        this.aliasManager = aliasManager;
        this.subqueryInitFactory = subqueryInitFactory;
        this.expressionFactory = expressionFactory;
        this.jpaProvider = jpaProvider;
        if (resultClazz.equals(Tuple.class)) {
            objectBuilder = (ObjectBuilder<T>) new TupleObjectBuilder(selectInfos, selectAliasToPositionMap);
        }
    }

    @Override
    public ClauseType getClauseType() {
        return ClauseType.SELECT;
    }

    void verifyBuilderEnded() {
        if (subqueryBuilderListener != null) {
            subqueryBuilderListener.verifySubqueryBuilderEnded();
        }
        if (caseExpressionBuilderListener != null) {
            caseExpressionBuilderListener.verifyBuilderEnded();
        }
        selectObjectBuilderEndedListener.verifyBuilderEnded();
    }

    ObjectBuilder<T> getSelectObjectBuilder() {
        return objectBuilder;
    }

    public List<SelectInfo> getSelectInfos() {
        return selectInfos;
    }
    
    /**
     * 
     * @return an array with length 2. 
     * Element 0 is true if the select clause contains a any expression that would be required in group by when aggregates are used.
     * Element 1 is true if the select clause contains a any complex expression that would be required in group by when aggregates are used.
     */
    public boolean[] containsGroupBySelect(boolean treatSizeAsAggregate) {
        GroupByExpressionGatheringVisitor gatheringVisitor = new GroupByExpressionGatheringVisitor(treatSizeAsAggregate);
        boolean containsGroupBySelect = false;
        for (SelectInfo selectInfo : selectInfos) {
            if (!(selectInfo.getExpression() instanceof PathExpression)) {
                selectInfo.getExpression().accept(gatheringVisitor);
            } else {
                containsGroupBySelect = true;
            }
        }
        boolean containsComplexGroupBySelect = !gatheringVisitor.getExpressions().isEmpty();
        return new boolean[] {containsGroupBySelect || containsComplexGroupBySelect, containsComplexGroupBySelect};
    }

    public Set<Expression>[] getGroupBySelectExpressions(boolean treatSizeAsAggregate) {
        GroupByExpressionGatheringVisitor gatheringVisitor = new GroupByExpressionGatheringVisitor(treatSizeAsAggregate);
        Set<Expression> groupBySelects = new HashSet<Expression>();
        for (SelectInfo selectInfo : selectInfos) {
            if (!(selectInfo.getExpression() instanceof PathExpression)) {
                selectInfo.getExpression().accept(gatheringVisitor);
            } else {
                groupBySelects.add(selectInfo.getExpression());
            }
        }
        return new Set[] { groupBySelects, gatheringVisitor.getExpressions()};
    }
    
    public boolean containsSizeSelect() {
        List<SelectInfo> infos = selectInfos;
        int size = selectInfos.size();
        for (int i = 0; i < size; i++) {
            final SelectInfo selectInfo = infos.get(i);
            if (ExpressionUtils.containsSizeExpression(selectInfo.getExpression())) {
                return true;
            }
        }
        return false;
    }

    void acceptVisitor(Visitor v) {
        List<SelectInfo> infos = selectInfos;
        int size = selectInfos.size();
        for (int i = 0; i < size; i++) {
            final SelectInfo selectInfo = infos.get(i);
            selectInfo.getExpression().accept(v);
        }
    }

    <X> X acceptVisitor(ResultVisitor<X> v, X stopValue) {
        List<SelectInfo> infos = selectInfos;
        int size = selectInfos.size();
        for (int i = 0; i < size; i++) {
            final SelectInfo selectInfo = infos.get(i);
            if (stopValue.equals(selectInfo.getExpression().accept(v))) {
                return stopValue;
            }
        }

        return null;
    }

    /**
     * Builds the clauses needed for the group by clause for a query that uses aggregate functions to work.
     * 
     * @param m
     * @return
     */
    void buildGroupByClauses(final Metamodel m, Set<String> clauses) {
        SimpleQueryGenerator.BooleanLiteralRenderingContext oldBooleanLiteralRenderingContext = queryGenerator.setBooleanLiteralRenderingContext(SimpleQueryGenerator.BooleanLiteralRenderingContext.CASE_WHEN);
        StringBuilder sb = new StringBuilder();

        Set<PathExpression> componentPaths = new LinkedHashSet<PathExpression>();
        EntitySelectResolveVisitor resolveVisitor = new EntitySelectResolveVisitor(m, componentPaths);

        // When no select infos are available, it can only be a root entity select
        if (selectInfos.isEmpty()) {
            // TODO: GroupByTest#testGroupByEntitySelect uses this. It's problematic because it's not aware of VALUES clause
            List<JoinNode> roots = joinManager.getRoots();
            
            if (roots.size() > 1) {
                throw new IllegalArgumentException("Empty select not allowed when having multiple roots!");
            }
            
            JoinNode rootNode = roots.get(0);
            String rootAlias = rootNode.getAliasInfo().getAlias();
            
            List<PathElementExpression> path = Arrays.asList((PathElementExpression) new PropertyExpression(rootAlias));
            resolveVisitor.visit(new PathExpression(path, new SimplePathReference(rootNode, null, null), false, false));

            for (PathExpression pathExpr : componentPaths) {
                sb.setLength(0);
                queryGenerator.setQueryBuffer(sb);
                pathExpr.accept(queryGenerator);
                clauses.add(sb.toString());
            }
        } else {
            List<SelectInfo> infos = selectInfos;
            int size = selectInfos.size();
            for (int i = 0; i < size; i++) {
                final SelectInfo selectInfo = infos.get(i);
                componentPaths.clear();
                selectInfo.getExpression().accept(resolveVisitor);

                // The select info can only either an entity select or any other expression
                // but entity selects can't be nested in other expressions, therefore we can differentiate here
                if (componentPaths.size() > 0) {
                    for (PathExpression pathExpr : componentPaths) {
                        sb.setLength(0);
                        queryGenerator.setQueryBuffer(sb);
                        pathExpr.accept(queryGenerator);
                        clauses.add(sb.toString());
                    }
                } else {
                    // This visitor checks if an expression is usable in a group by
                    if (!selectInfo.getExpression().accept(groupByUsableDetectionVisitor)) {
                        sb.setLength(0);
                        queryGenerator.setQueryBuffer(sb);
                        selectInfo.getExpression().accept(queryGenerator);
                        clauses.add(sb.toString());
                    }
                }
            }
        }

        queryGenerator.setBooleanLiteralRenderingContext(oldBooleanLiteralRenderingContext);
    }

    void buildSelect(StringBuilder sb) {
        sb.append("SELECT ");

        if (distinct) {
            sb.append("DISTINCT ");
        }

        List<SelectInfo> infos = selectInfos;
        int size = selectInfos.size();
        if (size == 0) {
            List<JoinNode> roots = joinManager.getRoots();
            
            if (roots.size() > 1) {
                throw new IllegalArgumentException("Empty select not allowed when having multiple roots!");
            }

            roots.get(0).appendAlias(sb, null);
        } else {
            // we must not replace select alias since we would loose the original expressions
            queryGenerator.setQueryBuffer(sb);
            SimpleQueryGenerator.BooleanLiteralRenderingContext oldBooleanLiteralRenderingContext = queryGenerator.setBooleanLiteralRenderingContext(SimpleQueryGenerator.BooleanLiteralRenderingContext.CASE_WHEN);
            
            for (int i = 0; i < size; i++) {
                if (i != 0) {
                    sb.append(", ");
                }
                
                applySelect(queryGenerator, sb, infos.get(i));
            }
            
            queryGenerator.setBooleanLiteralRenderingContext(oldBooleanLiteralRenderingContext);
        }
    }

    @Override
    public void applyTransformer(ExpressionTransformer transformer) {
        List<SelectInfo> infos = selectInfos;
        int size = selectInfos.size();
        // carry out transformations
        for (int i = 0; i < size; i++) {
            final SelectInfo selectInfo = infos.get(i);
            Expression transformed = transformer.transform(new NodeInfoExpressionModifier(selectInfo), selectInfo.getExpression(), ClauseType.SELECT, true);
            selectInfo.setExpression(transformed);
        }
    }

    public void applySelectInfoTransformer(SelectInfoTransformer selectInfoTransformer) {
        List<SelectInfo> infos = selectInfos;
        int size = selectInfos.size();
        for (int i = 0; i < size; i++) {
            final SelectInfo selectInfo = infos.get(i);
            selectInfoTransformer.transform(selectInfo);
        }
    }

    @SuppressWarnings("unchecked")
    <X> SubqueryInitiator<X> selectSubquery(X builder, final String selectAlias) {
        verifyBuilderEnded();

        subqueryBuilderListener = new SelectSubqueryBuilderListener<X>(selectAlias);
        SubqueryInitiator<X> initiator = subqueryInitFactory.createSubqueryInitiator(builder, (SubqueryBuilderListener<X>) subqueryBuilderListener);
        subqueryBuilderListener.onInitiatorStarted(initiator);
        return initiator;
    }

    @SuppressWarnings("unchecked")
    <X> SubqueryInitiator<X> selectSubquery(X builder, String subqueryAlias, Expression expression, String selectAlias) {
        verifyBuilderEnded();

        subqueryBuilderListener = new SuperExpressionSelectSubqueryBuilderListener<X>(subqueryAlias, expression, selectAlias);
        SubqueryInitiator<X> initiator = subqueryInitFactory.createSubqueryInitiator(builder, (SubqueryBuilderListener<X>) subqueryBuilderListener);
        subqueryBuilderListener.onInitiatorStarted(initiator);
        return initiator;
    }

    <X> MultipleSubqueryInitiator<X> selectSubqueries(X builder, Expression expression, final String selectAlias) {
        verifyBuilderEnded();

        MultipleSubqueryInitiator<X> initiator = new MultipleSubqueryInitiatorImpl<X>(builder, expression, new ExpressionBuilderEndedListener() {
            
            @Override
            public void onBuilderEnded(ExpressionBuilder builder) {
                select(builder.getExpression(), selectAlias);
            }
            
        }, subqueryInitFactory);
        return initiator;
    }

    <X> CaseWhenStarterBuilder<X> selectCase(X builder, final String selectAlias) {
        verifyBuilderEnded();
        caseExpressionBuilderListener = new CaseExpressionBuilderListener(selectAlias);
        return caseExpressionBuilderListener.startBuilder(new CaseWhenBuilderImpl<X>(builder, caseExpressionBuilderListener, subqueryInitFactory, expressionFactory, parameterManager));
    }

    <X> SimpleCaseWhenStarterBuilder<X> selectSimpleCase(X builder, final String selectAlias, Expression caseOperandExpression) {
        verifyBuilderEnded();
        caseExpressionBuilderListener = new CaseExpressionBuilderListener(selectAlias);
        return caseExpressionBuilderListener.startBuilder(new SimpleCaseWhenBuilderImpl<X>(builder, caseExpressionBuilderListener, expressionFactory, caseOperandExpression));
    }

    Class<?> getExpectedQueryResultType() {
        // Tuple case
        if (selectInfos.size() > 1) {
            return Object[].class;
        }

        return jpaProvider.getDefaultQueryResultType();
    }

    void select(Expression expr, String selectAlias) {
        SelectInfo selectInfo = new SelectInfo(expr, selectAlias, aliasManager);
        if (selectAlias != null) {
            aliasManager.registerAliasInfo(selectInfo);
            selectAliasToPositionMap.put(selectAlias, selectAliasToPositionMap.size());
        }
        selectInfos.add(selectInfo);

        registerParameterExpressions(expr);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    <Y, X extends AbstractFullQueryBuilder<?, ?, ?, ?, ?>> SelectObjectBuilder<? extends FullQueryBuilder<Y, ?>> selectNew(X builder, Class<Y> clazz) {
        if (selectObjectBuilder != null) {
            throw new IllegalStateException("Only one selectNew is allowed");
        }
        if (!selectInfos.isEmpty()) {
            throw new IllegalStateException("No mixture of select and selectNew is allowed");
        }

        selectObjectBuilder = selectObjectBuilderEndedListener.startBuilder(new SelectObjectBuilderImpl(builder, selectObjectBuilderEndedListener, subqueryInitFactory, expressionFactory));
        objectBuilder = new ClassObjectBuilder(clazz);
        return (SelectObjectBuilder) selectObjectBuilder;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    <Y, X extends AbstractFullQueryBuilder<?, ?, ?, ?, ?>> SelectObjectBuilder<? extends FullQueryBuilder<Y, ?>> selectNew(X builder, Constructor<Y> constructor) {
        if (selectObjectBuilder != null) {
            throw new IllegalStateException("Only one selectNew is allowed");
        }
        if (!selectInfos.isEmpty()) {
            throw new IllegalStateException("No mixture of select and selectNew is allowed");
        }

        selectObjectBuilder = selectObjectBuilderEndedListener.startBuilder(new SelectObjectBuilderImpl(builder, selectObjectBuilderEndedListener, subqueryInitFactory, expressionFactory));
        objectBuilder = new ConstructorObjectBuilder(constructor);
        return (SelectObjectBuilder) selectObjectBuilder;
    }

    @SuppressWarnings("unchecked")
    <X extends FullQueryBuilder<?, X>> void selectNew(X builder, ObjectBuilder<?> objectBuilder) {
        if (selectObjectBuilder != null) {
            throw new IllegalStateException("Only one selectNew is allowed");
        }
        if (!selectInfos.isEmpty()) {
            throw new IllegalStateException("No mixture of select and selectNew is allowed");
        }

        objectBuilder.applySelects(builder);
        this.objectBuilder = (ObjectBuilder<T>) objectBuilder;
    }

    void distinct() {
        this.distinct = true;
    }

    boolean isDistinct() {
        return this.distinct;
    }

    private void applySelect(ResolvingQueryGenerator queryGenerator, StringBuilder sb, SelectInfo select) {
        select.getExpression().accept(queryGenerator);
        if (select.alias != null) {
            sb.append(" AS ").append(select.alias);
        }
    }

    // TODO: needs equals-hashCode implementation
    private class SelectSubqueryBuilderListener<X> extends SubqueryBuilderListenerImpl<X> {

        private final String selectAlias;

        public SelectSubqueryBuilderListener(String selectAlias) {
            this.selectAlias = selectAlias;
        }

        @Override
        public void onBuilderEnded(SubqueryInternalBuilder<X> builder) {
            super.onBuilderEnded(builder);
            select(new SubqueryExpression(builder), selectAlias);
        }
    }

    private class SuperExpressionSelectSubqueryBuilderListener<X> extends SuperExpressionSubqueryBuilderListener<X> {

        private final String selectAlias;

        public SuperExpressionSelectSubqueryBuilderListener(String subqueryAlias, Expression superExpression, String selectAlias) {
            super(subqueryAlias, superExpression);
            this.selectAlias = selectAlias;
        }

        @Override
        public void onBuilderEnded(SubqueryInternalBuilder<X> builder) {
            super.onBuilderEnded(builder);
            select(superExpression, selectAlias);
        }
    }

    private class CaseExpressionBuilderListener extends ExpressionBuilderEndedListenerImpl {

        private final String selectAlias;

        public CaseExpressionBuilderListener(String selectAlias) {
            this.selectAlias = selectAlias;
        }

        @Override
        public void onBuilderEnded(ExpressionBuilder builder) {
            super.onBuilderEnded(builder); // To change body of generated methods, choose Tools | Templates.
            select(builder.getExpression(), selectAlias);
        }

    }

    private class SelectObjectBuilderEndedListenerImpl implements SelectObjectBuilderEndedListener {

        private SelectObjectBuilder<?> currentBuilder;

        protected void verifyBuilderEnded() {
            if (currentBuilder != null) {
                throw new IllegalStateException("A builder was not ended properly.");
            }
        }

        protected <X extends SelectObjectBuilder<?>> X startBuilder(X builder) {
            if (currentBuilder != null) {
                throw new IllegalStateException("There was an attempt to start a builder but a previous builder was not ended.");
            }

            currentBuilder = builder;
            return builder;
        }

        @Override
        public void onBuilderEnded(Collection<Map.Entry<Expression, String>> expressions) {
            if (currentBuilder == null) {
                throw new IllegalStateException("There was an attempt to end a builder that was not started or already closed.");
            }
            currentBuilder = null;
            for (Map.Entry<Expression, String> e : expressions) {
                select(e.getKey(), e.getValue());
                // SelectInfo selectInfo = new SelectInfo(e.getKey(), e.getValue(), aliasManager);
                // if (e.getValue() != null) {
                // aliasManager.registerAliasInfo(selectInfo);
                // selectAliasToPositionMap.put(e.getValue(), selectAliasToPositionMap.size());
                // }
                // registerParameterExpressions(e.getKey());
                // SelectManager.this.selectInfos.add(selectInfo);
            }
        }

    }

}
