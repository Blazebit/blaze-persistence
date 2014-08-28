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
import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.QueryBuilder;
import com.blazebit.persistence.SelectObjectBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.Expression.Visitor;
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.impl.expression.PathExpression;
import com.blazebit.persistence.impl.expression.SubqueryExpression;
import com.blazebit.persistence.impl.objectbuilder.ClassObjectBuilder;
import com.blazebit.persistence.impl.objectbuilder.ConstructorObjectBuilder;
import com.blazebit.persistence.impl.objectbuilder.TupleObjectBuilder;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
    private SubqueryBuilderListenerImpl subqueryBuilderListener;
    // needed for tuple/alias matching
    private final Map<String, Integer> selectAliasToPositionMap = new HashMap<String, Integer>();
    // TODO: review if this is really necessary
    private final Map<String, SelectInfo> selectAbsolutePathToInfoMap = new HashMap<String, SelectInfo>();
    private final SelectObjectBuilderEndedListenerImpl selectObjectBuilderEndedListener = new SelectObjectBuilderEndedListenerImpl();
    private final AliasManager aliasManager;
    private final SubqueryInitiatorFactory subqueryInitFactory;
    private final ExpressionFactory expressionFactory;

    public SelectManager(QueryGenerator queryGenerator, ParameterManager parameterManager, AliasManager aliasManager, SubqueryInitiatorFactory subqueryInitFactory, ExpressionFactory expressionFactory) {
        super(queryGenerator, parameterManager);
        this.aliasManager = aliasManager;
        this.subqueryInitFactory = subqueryInitFactory;
        this.expressionFactory = expressionFactory;
    }

    public Map<String, SelectInfo> getSelectAbsolutePathToInfoMap() {
        return selectAbsolutePathToInfoMap;
    }

    void verifyBuilderEnded() {
        if (subqueryBuilderListener != null) {
            subqueryBuilderListener.verifySubqueryBuilderEnded();
        }
        selectObjectBuilderEndedListener.verifyBuilderEnded();
    }

    ObjectBuilder<T> getSelectObjectBuilder() {
        return objectBuilder;
    }

    List<SelectInfo> getSelectInfos() {
        return selectInfos;
    }

    void acceptVisitor(Visitor v) {
        for (SelectInfo selectInfo : selectInfos) {
            selectInfo.getExpression().accept(v);
        }
    }

    String buildSelect(String rootAlias) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");

        if (distinct) {
            sb.append("DISTINCT ");
        }

        if (selectInfos.isEmpty()) {
            sb.append(rootAlias);
        } else {
            // we must not replace select alias since we would loose the original expressions
            populateSelectAliasAbsolutePaths();
            queryGenerator.setQueryBuffer(sb);
            Iterator<SelectInfo> iter = selectInfos.iterator();
            applySelect(queryGenerator, sb, iter.next());
            while (iter.hasNext()) {
                sb.append(", ");
                applySelect(queryGenerator, sb, iter.next());
            }
        }
        sb.append(' ');
        return sb.toString();
    }

    void applyTransformer(ExpressionTransformer transformer) {
        // carry out transformations
        for (SelectInfo selectInfo : selectInfos) {
            Expression transformed = transformer.transform(selectInfo.getExpression(), true);
            selectInfo.setExpression(transformed);
        }
    }

    <T extends BaseQueryBuilder<?, ?>> SubqueryInitiator<T> selectSubquery(T builder, final String selectAlias) {
        verifyBuilderEnded();

        subqueryBuilderListener = new SelectSubqueryBuilderListener(selectAlias);
        return subqueryInitFactory.createSubqueryInitiator(builder, subqueryBuilderListener);
    }

    <T extends BaseQueryBuilder<?, ?>> SubqueryInitiator<T> selectSubquery(T builder, String subqueryAlias, Expression expression, String selectAlias) {
        verifyBuilderEnded();

        subqueryBuilderListener = new SuperExpressionSelectSubqueryBuilderListener(subqueryAlias, expression, selectAlias);
        return subqueryInitFactory.createSubqueryInitiator(builder, subqueryBuilderListener);
    }

    void select(AbstractBaseQueryBuilder<?, ?> builder, Expression expr, String selectAlias) {
        SelectInfo selectInfo = new SelectInfo(expr, selectAlias, aliasManager);
        if (selectAlias != null) {
            aliasManager.registerAliasInfo(selectInfo);
            selectAliasToPositionMap.put(selectAlias, selectAliasToPositionMap.size());
        }
        selectInfos.add(selectInfo);

        if (objectBuilder == null || !(objectBuilder instanceof TupleObjectBuilder)) {
            objectBuilder = (ObjectBuilder<T>) new TupleObjectBuilder(selectInfos, selectAliasToPositionMap);
        }
        registerParameterExpressions(expr);
    }
//    CaseWhenBuilder<T> selectCase() {
//        return new CaseWhenBuilderImpl<T>((T) this);
//    }

    /* CASE caseOperand (WHEN scalarExpression THEN scalarExpression)+ ELSE scalarExpression END */
//    SimpleCaseWhenBuilder<T> selectCase(String expression) {
//        return new SimpleCaseWhenBuilderImpl<T>((T) this, expression);
//    }
    <Y, T extends AbstractQueryBuilder<?, ?>> SelectObjectBuilder<? extends QueryBuilder<Y, ?>> selectNew(T builder, Class<Y> clazz) {
        if (selectObjectBuilder != null) {
            throw new IllegalStateException("Only one selectNew is allowed");
        }
        if (!selectInfos.isEmpty()) {
            throw new IllegalStateException("No mixture of select and selectNew is allowed");
        }

        selectObjectBuilder = selectObjectBuilderEndedListener.startBuilder(
            new SelectObjectBuilderImpl(builder, selectObjectBuilderEndedListener, subqueryInitFactory, expressionFactory));
        objectBuilder = new ClassObjectBuilder(clazz);
        return (SelectObjectBuilder) selectObjectBuilder;
    }

    <Y, T extends AbstractQueryBuilder<?, ?>> SelectObjectBuilder<? extends QueryBuilder<Y, ?>> selectNew(T builder, Constructor<Y> constructor) {
        if (selectObjectBuilder != null) {
            throw new IllegalStateException("Only one selectNew is allowed");
        }
        if (!selectInfos.isEmpty()) {
            throw new IllegalStateException("No mixture of select and selectNew is allowed");
        }

        selectObjectBuilder = selectObjectBuilderEndedListener.startBuilder(
            new SelectObjectBuilderImpl(builder, selectObjectBuilderEndedListener, subqueryInitFactory, expressionFactory));
        objectBuilder = new ConstructorObjectBuilder(constructor);
        return (SelectObjectBuilder) selectObjectBuilder;
    }

    void selectNew(QueryBuilder<?, ?> builder, ObjectBuilder<?> objectBuilder) {
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

    private void applySelect(QueryGenerator queryGenerator, StringBuilder sb, SelectInfo select) {
        select.getExpression().accept(queryGenerator);
        if (select.alias != null) {
            sb.append(" AS ").append(select.alias);
        }
    }

    private void populateSelectAliasAbsolutePaths() {
        selectAbsolutePathToInfoMap.clear();
        for (Map.Entry<String, AliasInfo> selectAliasEntry : aliasManager.getAliasMapForBottomLevel().entrySet()) {
            if (selectAliasEntry.getValue() instanceof SelectInfo) {
                SelectInfo selectInfo = (SelectInfo) selectAliasEntry.getValue();
                Expression selectExpr = selectInfo.getExpression();

                if (selectExpr instanceof PathExpression) {
                    PathExpression pathExpr = (PathExpression) selectExpr;
                    JoinNode baseNode = (JoinNode) pathExpr.getBaseNode();
                    String absPath = baseNode.getAliasInfo().getAbsolutePath();

                    if (absPath.isEmpty()) {
                        // if the absPath is empty the pathExpr is relative to the root and we
                        // must not insert any select info for this
                        absPath = pathExpr.getField();

                    } else {
                        absPath += "." + pathExpr.getField();
                    }
                    selectAbsolutePathToInfoMap.put(absPath, selectInfo);
                }
            }
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
            Expression expr = new SubqueryExpression(builder);
            SelectInfo selectInfo = new SelectInfo(expr, selectAlias, aliasManager);
            if (selectAlias != null) {
                aliasManager.registerAliasInfo(selectInfo);
                selectAliasToPositionMap.put(selectAlias, selectAliasToPositionMap.size());
            }
            selectInfos.add(selectInfo);

            if (objectBuilder == null || !(objectBuilder instanceof TupleObjectBuilder)) {
                objectBuilder = (ObjectBuilder<T>) new TupleObjectBuilder(selectInfos, selectAliasToPositionMap);
            }
            registerParameterExpressions(expr);
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

            //TODO: maybe unify with SelectSubqueryBuilderListener
            SelectInfo selectInfo = new SelectInfo(superExpression, selectAlias, aliasManager);
            if (selectAlias != null) {
                aliasManager.registerAliasInfo(selectInfo);
                selectAliasToPositionMap.put(selectAlias, selectAliasToPositionMap.size());
            }
            selectInfos.add(selectInfo);

            if (objectBuilder == null || !(objectBuilder instanceof TupleObjectBuilder)) {
                objectBuilder = (ObjectBuilder<T>) new TupleObjectBuilder(selectInfos, selectAliasToPositionMap);
            }
            registerParameterExpressions(superExpression);
        }
    }

    private class SelectObjectBuilderEndedListenerImpl implements SelectObjectBuilderEndedListener {

        private SelectObjectBuilder currentBuilder;

        protected void verifyBuilderEnded() {
            if (currentBuilder != null) {
                throw new IllegalStateException("A builder was not ended properly.");
            }
        }

        protected <T extends SelectObjectBuilder> T startBuilder(T builder) {
            if (currentBuilder != null) {
                throw new IllegalStateException("There was an attempt to start a builder but a previous builder was not ended.");
            }

            currentBuilder = builder;
            return builder;
        }

        @Override
        public void onBuilderEnded(Collection<Expression> expressions) {
            if (currentBuilder == null) {
                throw new IllegalStateException("There was an attempt to end a builder that was not started or already closed.");
            }
            currentBuilder = null;
            for (Expression e : expressions) {
                registerParameterExpressions(e);
                SelectManager.this.selectInfos.add(new SelectInfo(e));
            }
        }

    }
}
