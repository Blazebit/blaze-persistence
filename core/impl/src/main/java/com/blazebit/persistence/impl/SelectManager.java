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
import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.QueryBuilder;
import com.blazebit.persistence.SelectObjectBuilder;
import com.blazebit.persistence.SimpleCaseWhenStarterBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.impl.builder.expression.ExpressionBuilder;
import com.blazebit.persistence.impl.builder.expression.ExpressionBuilderEndedListenerImpl;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.Expression.Visitor;
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.impl.expression.SubqueryExpression;
import com.blazebit.persistence.impl.builder.object.ClassObjectBuilder;
import com.blazebit.persistence.impl.builder.object.ConstructorObjectBuilder;
import com.blazebit.persistence.impl.builder.object.TupleObjectBuilder;
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
    private SubqueryBuilderListenerImpl<?> subqueryBuilderListener;
    // needed for tuple/alias matching
    private final Map<String, Integer> selectAliasToPositionMap = new HashMap<String, Integer>();
    private final SelectObjectBuilderEndedListenerImpl selectObjectBuilderEndedListener = new SelectObjectBuilderEndedListenerImpl();
    private CaseExpressionBuilderListener caseExpressionBuilderListener;
    private final AliasManager aliasManager;
    private final SubqueryInitiatorFactory subqueryInitFactory;
    private final ExpressionFactory expressionFactory;

    public SelectManager(QueryGenerator queryGenerator, ParameterManager parameterManager, AliasManager aliasManager, SubqueryInitiatorFactory subqueryInitFactory, ExpressionFactory expressionFactory) {
        super(queryGenerator, parameterManager);
        this.aliasManager = aliasManager;
        this.subqueryInitFactory = subqueryInitFactory;
        this.expressionFactory = expressionFactory;
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

    List<SelectInfo> getSelectInfos() {
        return selectInfos;
    }

    void acceptVisitor(Visitor v) {
        for (SelectInfo selectInfo : selectInfos) {
            selectInfo.getExpression().accept(v);
        }
    }

    String buildClausesForAliases(List<String> selectAliases) {
        if (selectAliases.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        queryGenerator.setQueryBuffer(sb);
        Iterator<String> iter = selectAliases.iterator();
        applySelect(queryGenerator, sb, (SelectInfo) aliasManager.getAliasInfo(iter.next()));
        while (iter.hasNext()) {
            sb.append(", ");
            applySelect(queryGenerator, sb, (SelectInfo) aliasManager.getAliasInfo(iter.next()));
        }
        return sb.toString();
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
            queryGenerator.setQueryBuffer(sb);
            Iterator<SelectInfo> iter = selectInfos.iterator();
            applySelect(queryGenerator, sb, iter.next());
            while (iter.hasNext()) {
                sb.append(", ");
                applySelect(queryGenerator, sb, iter.next());
            }
        }

        return sb.toString();
    }

    void applyTransformer(ExpressionTransformer transformer) {
        // carry out transformations
        for (SelectInfo selectInfo : selectInfos) {
            Expression transformed = transformer.transform(selectInfo.getExpression(), ClauseType.SELECT);
            selectInfo.setExpression(transformed);
        }
    }

    void applySelectInfoTransformer(SelectInfoTransformer selectInfoTransformer) {
        for (SelectInfo selectInfo : selectInfos) {
            selectInfoTransformer.transform(selectInfo);
        }
    }

    <T extends BaseQueryBuilder<?, ?>> SubqueryInitiator<T> selectSubquery(T builder, final String selectAlias) {
        verifyBuilderEnded();

        subqueryBuilderListener = new SelectSubqueryBuilderListener<T>(selectAlias);
        return subqueryInitFactory.createSubqueryInitiator(builder, (SubqueryBuilderListener<T>) subqueryBuilderListener);
    }

    <T extends BaseQueryBuilder<?, ?>> SubqueryInitiator<T> selectSubquery(T builder, String subqueryAlias, Expression expression, String selectAlias) {
        verifyBuilderEnded();

        subqueryBuilderListener = new SuperExpressionSelectSubqueryBuilderListener<T>(subqueryAlias, expression, selectAlias);
        return subqueryInitFactory.createSubqueryInitiator(builder, (SubqueryBuilderListener<T>) subqueryBuilderListener);
    }

    <T extends BaseQueryBuilder<?, ?>> CaseWhenStarterBuilder<T> selectCase(T builder, final String selectAlias) {
        verifyBuilderEnded();
        caseExpressionBuilderListener = new CaseExpressionBuilderListener(selectAlias);
        return caseExpressionBuilderListener.startBuilder(new CaseWhenBuilderImpl<T>(builder, caseExpressionBuilderListener, subqueryInitFactory, expressionFactory));
    }

    <T extends BaseQueryBuilder<?, ?>> SimpleCaseWhenStarterBuilder<T> selectSimpleCase(T builder, final String selectAlias, String caseOperandExpression) {
        verifyBuilderEnded();
        caseExpressionBuilderListener = new CaseExpressionBuilderListener(selectAlias);
        return caseExpressionBuilderListener.startBuilder(new SimpleCaseWhenBuilderImpl<T>(builder, caseExpressionBuilderListener, expressionFactory, caseOperandExpression));
    }

    void select(AbstractBaseQueryBuilder<?, ?> builder, Expression expr, String selectAlias) {
        handleSelect(expr, selectAlias);
        if (objectBuilder == null || !(objectBuilder instanceof TupleObjectBuilder)) {
            objectBuilder = (ObjectBuilder<T>) new TupleObjectBuilder(selectInfos, selectAliasToPositionMap);
        }
    }

    private void handleSelect(Expression expr, String selectAlias) {
        SelectInfo selectInfo = new SelectInfo(expr, selectAlias, aliasManager);
        if (selectAlias != null) {
            aliasManager.registerAliasInfo(selectInfo);
            selectAliasToPositionMap.put(selectAlias, selectAliasToPositionMap.size());
        }
        selectInfos.add(selectInfo);

        registerParameterExpressions(expr);
    }

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

    // TODO: needs equals-hashCode implementation
    private class SelectSubqueryBuilderListener<X> extends SubqueryBuilderListenerImpl<X> {

        private final String selectAlias;

        public SelectSubqueryBuilderListener(String selectAlias) {
            this.selectAlias = selectAlias;
        }

        @Override
        public void onBuilderEnded(SubqueryBuilderImpl<X> builder) {
            super.onBuilderEnded(builder);
            handleSelect(new SubqueryExpression(builder), selectAlias);
            if (objectBuilder == null || !(objectBuilder instanceof TupleObjectBuilder)) {
                objectBuilder = (ObjectBuilder<T>) new TupleObjectBuilder(selectInfos, selectAliasToPositionMap);
            }
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
            handleSelect(superExpression, selectAlias);
            if (objectBuilder == null || !(objectBuilder instanceof TupleObjectBuilder)) {
                objectBuilder = (ObjectBuilder<T>) new TupleObjectBuilder(selectInfos, selectAliasToPositionMap);
            }
        }
    }

    private class CaseExpressionBuilderListener extends ExpressionBuilderEndedListenerImpl {

        private final String selectAlias;

        public CaseExpressionBuilderListener(String selectAlias) {
            this.selectAlias = selectAlias;
        }

        @Override
        public void onBuilderEnded(ExpressionBuilder builder) {
            super.onBuilderEnded(builder); //To change body of generated methods, choose Tools | Templates.
            handleSelect(builder.getExpression(), selectAlias);
            if (objectBuilder == null || !(objectBuilder instanceof TupleObjectBuilder)) {
                objectBuilder = (ObjectBuilder<T>) new TupleObjectBuilder(selectInfos, selectAliasToPositionMap);
            }
        }

    }

    private class SelectObjectBuilderEndedListenerImpl implements SelectObjectBuilderEndedListener {

        private SelectObjectBuilder<?> currentBuilder;

        protected void verifyBuilderEnded() {
            if (currentBuilder != null) {
                throw new IllegalStateException("A builder was not ended properly.");
            }
        }

        protected <T extends SelectObjectBuilder<?>> T startBuilder(T builder) {
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
                handleSelect(e.getKey(), e.getValue());
//                SelectInfo selectInfo = new SelectInfo(e.getKey(), e.getValue(), aliasManager);
//                if (e.getValue() != null) {
//                    aliasManager.registerAliasInfo(selectInfo);
//                    selectAliasToPositionMap.put(e.getValue(), selectAliasToPositionMap.size());
//                }
//                registerParameterExpressions(e.getKey());
//                SelectManager.this.selectInfos.add(selectInfo);
            }
        }

    }
}
