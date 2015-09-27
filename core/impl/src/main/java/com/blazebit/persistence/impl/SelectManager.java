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

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Tuple;
import javax.persistence.metamodel.Metamodel;

import com.blazebit.persistence.CaseWhenStarterBuilder;
import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.QueryBuilder;
import com.blazebit.persistence.SelectObjectBuilder;
import com.blazebit.persistence.SimpleCaseWhenStarterBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.impl.builder.expression.CaseWhenBuilderImpl;
import com.blazebit.persistence.impl.builder.expression.ExpressionBuilder;
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
import com.blazebit.persistence.impl.expression.SubqueryExpression;
import com.blazebit.persistence.impl.jpaprovider.JpaProvider;

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

    void acceptVisitor(Visitor v) {
        for (SelectInfo selectInfo : selectInfos) {
            selectInfo.getExpression().accept(v);
        }
    }

    <X> X acceptVisitor(ResultVisitor<X> v, X stopValue) {
        for (SelectInfo selectInfo : selectInfos) {
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
    Set<String> buildGroupByClauses(final Metamodel m) {
        Set<String> groupByClauses = new LinkedHashSet<String>();
        boolean conditionalContext = queryGenerator.setConditionalContext(false);
        StringBuilder sb = new StringBuilder();

        Set<PathExpression> componentPaths = new LinkedHashSet<PathExpression>();
        EntitySelectResolveVisitor resolveVisitor = new EntitySelectResolveVisitor(m, componentPaths);

        // When no select infos are available, it can only be a root entity select
        if (selectInfos.isEmpty()) {
            List<JoinNode> roots = joinManager.getRoots();
            
            if (roots.size() > 1) {
            	throw new IllegalArgumentException("Empty select not allowed when having multiple roots!");
            }
            
        	JoinNode rootNode = roots.get(0);
        	String rootAlias = rootNode.getAliasInfo().getAlias();
        	
            List<PathElementExpression> path = Arrays.asList((PathElementExpression) new PropertyExpression(rootAlias));
            resolveVisitor.visit(new PathExpression(path, rootNode, null, false, false));

            for (PathExpression pathExpr : componentPaths) {
                sb.setLength(0);
                queryGenerator.setQueryBuffer(sb);
                pathExpr.accept(queryGenerator);
                groupByClauses.add(sb.toString());
            }

            componentPaths.clear();
        } else {
            for (SelectInfo selectInfo : selectInfos) {
                selectInfo.getExpression().accept(resolveVisitor);

                // The select info can only either an entity select or any other expression
                // but entity selects can't be nested in other expressions, therefore we can differentiate here
                if (componentPaths.size() > 0) {
                    for (PathExpression pathExpr : componentPaths) {
                        sb.setLength(0);
                        queryGenerator.setQueryBuffer(sb);
                        pathExpr.accept(queryGenerator);
                        groupByClauses.add(sb.toString());
                    }

                    componentPaths.clear();
                } else {
                    // This visitor checks if an expression is usable in a group by
                    GroupByUsableDetectionVisitor groupByUsableDetectionVisitor = new GroupByUsableDetectionVisitor();
                    if (!Boolean.TRUE.equals(selectInfo.getExpression().accept(groupByUsableDetectionVisitor))) {
                        sb.setLength(0);
                        queryGenerator.setQueryBuffer(sb);
                        selectInfo.getExpression().accept(queryGenerator);
                        groupByClauses.add(sb.toString());
                    }
                }
            }
        }

        queryGenerator.setConditionalContext(conditionalContext);
        return groupByClauses;
    }

    String buildSelect() {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");

        if (distinct) {
            sb.append("DISTINCT ");
        }

        if (selectInfos.isEmpty()) {
            List<JoinNode> roots = joinManager.getRoots();
            
            if (roots.size() > 1) {
            	throw new IllegalArgumentException("Empty select not allowed when having multiple roots!");
            }
            
            sb.append(roots.get(0).getAliasInfo().getAlias());
        } else {
            // we must not replace select alias since we would loose the original expressions
            queryGenerator.setQueryBuffer(sb);
            Iterator<SelectInfo> iter = selectInfos.iterator();
            boolean conditionalContext = queryGenerator.setConditionalContext(false);
            applySelect(queryGenerator, sb, iter.next());
            while (iter.hasNext()) {
                sb.append(", ");
                applySelect(queryGenerator, sb, iter.next());
            }
            queryGenerator.setConditionalContext(conditionalContext);
        }

        return sb.toString();
    }

    void applyTransformer(ExpressionTransformer transformer) {
        // carry out transformations
        for (SelectInfo selectInfo : selectInfos) {
            Expression transformed = transformer.transform(selectInfo.getExpression(), ClauseType.SELECT, true);
            selectInfo.setExpression(transformed);
        }
    }

    void applySelectInfoTransformer(SelectInfoTransformer selectInfoTransformer) {
        for (SelectInfo selectInfo : selectInfos) {
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

    <X> CaseWhenStarterBuilder<X> selectCase(X builder, final String selectAlias) {
        verifyBuilderEnded();
        caseExpressionBuilderListener = new CaseExpressionBuilderListener(selectAlias);
        return caseExpressionBuilderListener.startBuilder(new CaseWhenBuilderImpl<X>(builder, caseExpressionBuilderListener, subqueryInitFactory, expressionFactory));
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
    <Y, X extends AbstractQueryBuilder<?, ?>> SelectObjectBuilder<? extends QueryBuilder<Y, ?>> selectNew(X builder, Class<Y> clazz) {
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
    <Y, X extends AbstractQueryBuilder<?, ?>> SelectObjectBuilder<? extends QueryBuilder<Y, ?>> selectNew(X builder, Constructor<Y> constructor) {
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
    <X extends QueryBuilder<?, X>> void selectNew(X builder, ObjectBuilder<?> objectBuilder) {
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
        public void onBuilderEnded(SubqueryBuilderImpl<X> builder) {
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
        public void onBuilderEnded(SubqueryBuilderImpl<X> builder) {
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
