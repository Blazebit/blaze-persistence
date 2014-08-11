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
    private SelectSubqueryBuilderListener subqueryBuilderListener;
    // Maps alias to SelectInfo
//    private final Map<String, SelectInfo> selectAliasToInfoMap = new HashMap<String, SelectInfo>();
    // needed for tuple/alias matching
    private final Map<String, Integer> selectAliasToPositionMap = new HashMap<String, Integer>();
    private final Map<String, SelectInfo> selectAbsolutePathToInfoMap = new HashMap<String, SelectInfo>();
    private final SelectObjectBuilderEndedListenerImpl selectObjectBuilderEndedListener = new SelectObjectBuilderEndedListenerImpl();
    private final AliasManager aliasManager;
    private final BaseQueryBuilder<?, ?> aliasOwner;
    private final SubqueryInitiatorFactory subqueryInitFactory;
    private final ExpressionFactory expressionFactory;

    public SelectManager(QueryGenerator queryGenerator, ParameterManager parameterManager, AliasManager aliasManager, BaseQueryBuilder<?, ?> aliasOwner, SubqueryInitiatorFactory subqueryInitFactory, ExpressionFactory expressionFactory) {
        super(queryGenerator, parameterManager);
        this.aliasManager = aliasManager;
        this.aliasOwner = aliasOwner;
        this.subqueryInitFactory = subqueryInitFactory;
        this.expressionFactory = expressionFactory;
    }

    void verifyBuilderEnded() {
        selectObjectBuilderEndedListener.verifyBuilderEnded();
    }

    ObjectBuilder<T> getSelectObjectBuilder() {
        return objectBuilder;
    }

    public Map<String, SelectInfo> getSelectAbsolutePathToInfoMap() {
        return selectAbsolutePathToInfoMap;
    }

    public List<SelectManager.SelectInfo> getSelectInfos() {
        return selectInfos;
    }

    public Map<String, Integer> getSelectAliasToPositionMap() {
        return selectAliasToPositionMap;
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
        if (subqueryBuilderListener != null) {
            throw new IllegalStateException("A builder was not ended properly.");
        }

        subqueryBuilderListener = new SelectSubqueryBuilderListener(selectAlias);
        return subqueryInitFactory.createSubqueryInitiator(builder, subqueryBuilderListener);
    }

    public String[] getSelectAliases() {
        String[] aliases = new String[selectInfos.size()];
        for (int i = 0; i < aliases.length; i++) {
            aliases[i] = selectInfos.get(i).getAlias();
        }

        return aliases;
    }

    private class SelectSubqueryBuilderListener<X> extends SubqueryBuilderListenerImpl<X> {

        private final String selectAlias;

        public SelectSubqueryBuilderListener(String selectAlias) {
            this.selectAlias = selectAlias;
        }

        @Override
        public void onBuilderEnded(SubqueryBuilderImpl<X> builder) {
            super.onBuilderEnded(builder);
            Expression expr = new SubqueryExpression(builder);
            SelectInfo selectInfo = new SelectInfo(expr, selectAlias, aliasOwner);
            if (selectAlias != null) {
                aliasManager.registerAliasInfo(selectInfo);
                //            selectAliasToInfoMap.put(selectAlias, selectInfo);
                selectAliasToPositionMap.put(selectAlias, selectAliasToPositionMap.size());
            }
            selectInfos.add(selectInfo);

            if (objectBuilder == null || !(objectBuilder instanceof TupleObjectBuilder)) {
                objectBuilder = (ObjectBuilder<T>) new TupleObjectBuilder(SelectManager.this);
            }
            registerParameterExpressions(expr);
        }
    }

    void select(AbstractBaseQueryBuilder<?, ?> builder, Expression expr, String selectAlias) {
        SelectInfo selectInfo = new SelectInfo(expr, selectAlias, aliasOwner);
        if (selectAlias != null) {
            aliasManager.registerAliasInfo(selectInfo);
//            selectAliasToInfoMap.put(selectAlias, selectInfo);
            selectAliasToPositionMap.put(selectAlias, selectAliasToPositionMap.size());
        }
        selectInfos.add(selectInfo);

        if (objectBuilder == null || !(objectBuilder instanceof TupleObjectBuilder)) {
            objectBuilder = (ObjectBuilder<T>) new TupleObjectBuilder(this);
        }
        registerParameterExpressions(expr);
    }
//    public CaseWhenBuilder<U> selectCase() {
//        return new CaseWhenBuilderImpl<U>((U) this);
//    }

    /* CASE caseOperand (WHEN scalarExpression THEN scalarExpression)+ ELSE scalarExpression END */
//    public SimpleCaseWhenBuilder<U> selectCase(String expression) {
//        return new SimpleCaseWhenBuilderImpl<U>((U) this, expression);
//    }
    <Y, T extends AbstractQueryBuilder<?, ?>> SelectObjectBuilder<? extends QueryBuilder<Y, ?>> selectNew(T builder, Class<Y> clazz) {
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

    <Y, T extends AbstractQueryBuilder<?, ?>> SelectObjectBuilder<? extends QueryBuilder<Y, ?>> selectNew(T builder, Constructor<Y> constructor) {
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
    
    boolean isDistinct(){
        return this.distinct;
    }

    private void applySelect(QueryGenerator queryGenerator, StringBuilder sb, SelectInfo select) {
        if (select.getExpression() instanceof SubqueryExpression) {
            sb.append("(");
        }
        select.getExpression().accept(queryGenerator);
        if (select.getExpression() instanceof SubqueryExpression) {
            sb.append(")");
        }
        if (select.alias != null) {
            sb.append(" AS ").append(select.alias);
        }
    }

    protected void populateSelectAliasAbsolutePaths() {
        selectAbsolutePathToInfoMap.clear();
        for (Map.Entry<String, AliasInfo> selectAliasEntry : aliasManager.getAliasMapForBottomLevel().entrySet()) {
            if (selectAliasEntry.getValue() instanceof SelectInfo) {
                SelectInfo selectInfo = (SelectInfo) selectAliasEntry.getValue();
                Expression selectExpr = selectInfo.getExpression();
                if (selectExpr instanceof PathExpression) {
                    PathExpression pathExpr = (PathExpression) selectExpr;
                    String absPath = pathExpr.getBaseNode().getAliasInfo().getAbsolutePath();
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

    public static class SelectInfo extends NodeInfo implements AliasInfo {

        private final String alias;
        private final BaseQueryBuilder<?, ?> aliasOwner;

        public SelectInfo(Expression expression) {
            super(expression);
            this.alias = null;
            this.aliasOwner = null;
        }

        public SelectInfo(Expression expression, String alias, BaseQueryBuilder<?, ?> aliasOwner) {
            super(expression);
            this.alias = alias;
            this.aliasOwner = aliasOwner;
        }

        @Override
        public String getAlias() {
            return alias;
        }

        @Override
        public BaseQueryBuilder<?, ?> getAliasOwner() {
            return aliasOwner;
        }
    }
}
