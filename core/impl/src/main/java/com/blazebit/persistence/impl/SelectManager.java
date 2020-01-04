/*
 * Copyright 2014 - 2020 Blazebit.
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

import com.blazebit.persistence.CaseWhenStarterBuilder;
import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.MultipleSubqueryInitiator;
import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.SelectObjectBuilder;
import com.blazebit.persistence.SimpleCaseWhenStarterBuilder;
import com.blazebit.persistence.SubqueryBuilder;
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
import com.blazebit.persistence.impl.transform.ExpressionModifierVisitor;
import com.blazebit.persistence.parser.EntityMetamodel;
import com.blazebit.persistence.parser.SimpleQueryGenerator;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.Expression.ResultVisitor;
import com.blazebit.persistence.parser.expression.Expression.Visitor;
import com.blazebit.persistence.parser.expression.ExpressionFactory;
import com.blazebit.persistence.parser.expression.MapKeyExpression;
import com.blazebit.persistence.parser.expression.MapValueExpression;
import com.blazebit.persistence.parser.expression.PathElementExpression;
import com.blazebit.persistence.parser.expression.PathExpression;
import com.blazebit.persistence.parser.expression.PropertyExpression;
import com.blazebit.persistence.parser.expression.SubqueryExpression;
import com.blazebit.persistence.spi.JpaProvider;

import javax.persistence.Tuple;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class SelectManager<T> extends AbstractManager<SelectInfo> {

    private final List<SelectInfo> selectInfos = new ArrayList<SelectInfo>();
    private boolean distinct = false;
    private boolean hasDefaultSelect;
    private Set<JoinNode> defaultSelectNodes;
    private boolean hasSizeSelect;
    private SelectObjectBuilderImpl<?> selectObjectBuilder;
    private ObjectBuilder<T> objectBuilder;
    private SubqueryBuilderListenerImpl<?> subqueryBuilderListener;
    // needed for tuple/alias matching
    private final Map<String, Integer> selectAliasToPositionMap = new HashMap<String, Integer>();
    private final SelectObjectBuilderEndedListenerImpl selectObjectBuilderEndedListener = new SelectObjectBuilderEndedListenerImpl();
    private CaseExpressionBuilderListener caseExpressionBuilderListener;
    private final AbstractCommonQueryBuilder<?, ?, ?, ?, ?> queryBuilder;
    private final GroupByExpressionGatheringVisitor groupByExpressionGatheringVisitor;
    private final JoinManager joinManager;
    private final AliasManager aliasManager;
    private final ExpressionFactory expressionFactory;
    private final JpaProvider jpaProvider;
    private final MainQuery mainQuery;
    private final Class<?> resultClazz;

    @SuppressWarnings("unchecked")
    public SelectManager(ResolvingQueryGenerator queryGenerator, ParameterManager parameterManager, AbstractCommonQueryBuilder<?, ?, ?, ?, ?> queryBuilder, JoinManager joinManager, AliasManager aliasManager, SubqueryInitiatorFactory subqueryInitFactory, ExpressionFactory expressionFactory, JpaProvider jpaProvider,
                         MainQuery mainQuery, GroupByExpressionGatheringVisitor groupByExpressionGatheringVisitor, Class<?> resultClazz) {
        super(queryGenerator, parameterManager, subqueryInitFactory);
        this.queryBuilder = queryBuilder;
        this.groupByExpressionGatheringVisitor = groupByExpressionGatheringVisitor;
        this.joinManager = joinManager;
        this.aliasManager = aliasManager;
        this.expressionFactory = expressionFactory;
        this.jpaProvider = jpaProvider;
        this.mainQuery = mainQuery;
        this.resultClazz = resultClazz;
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
        if (objectBuilder == null && resultClazz.equals(Tuple.class)) {
            return (ObjectBuilder<T>) new TupleObjectBuilder(selectInfos, selectAliasToPositionMap);
        }
        return objectBuilder;
    }

    public List<SelectInfo> getSelectInfos() {
        return selectInfos;
    }
    
    public boolean containsSizeSelect() {
        return hasSizeSelect;
    }

    public Set<JoinNode> collectFetchOwners() {
        Set<JoinNode> fetchOwners = new HashSet<>();
        List<SelectInfo> infos = selectInfos;
        int size = selectInfos.size();

        for (int i = 0; i < size; i++) {
            final SelectInfo selectInfo = infos.get(i);
            Expression expression = selectInfo.getExpression();

            // Map key and values are just qualified path expressions
            if (expression instanceof MapValueExpression) {
                expression = ((MapValueExpression) expression).getPath();
            } else if (expression instanceof MapKeyExpression) {
                expression = ((MapKeyExpression) expression).getPath();
            }

            // We only look for entity selects and those can only be path expressions
            if (expression instanceof PathExpression) {
                PathExpression pathExpression = (PathExpression) expression;
                JoinNode node = (JoinNode) pathExpression.getBaseNode();
                if (pathExpression.getField() == null) {
                    fetchOwners.add(node);
                }
            }
        }

        if (size == 0) {
            fetchOwners.add(joinManager.getRootNodeOrFail("Empty select not allowed when having multiple roots!"));
        }

        return fetchOwners;
    }

    void acceptVisitor(Visitor v) {
        for (int i = 0; i < selectInfos.size(); i++) {
            final SelectInfo selectInfo = selectInfos.get(i);
            selectInfo.getExpression().accept(v);
        }
    }

    void acceptVisitor(SelectInfoVisitor v) {
        for (int i = 0; i < selectInfos.size(); i++) {
            final SelectInfo selectInfo = selectInfos.get(i);
            selectInfo.accept(v);
        }
    }

    <X> X acceptVisitor(ResultVisitor<X> v, X stopValue) {
        for (int i = 0; i < selectInfos.size(); i++) {
            final SelectInfo selectInfo = selectInfos.get(i);
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
     * @param hasGroupBy
     * @param joinVisitor
     * @return
     */
    void buildGroupByClauses(final EntityMetamodel m, GroupByManager groupByManager, boolean hasGroupBy, JoinVisitor joinVisitor) {
        SimpleQueryGenerator.BooleanLiteralRenderingContext oldBooleanLiteralRenderingContext = queryGenerator.setBooleanLiteralRenderingContext(SimpleQueryGenerator.BooleanLiteralRenderingContext.CASE_WHEN);
        StringBuilder sb = new StringBuilder();

        Set<PathExpression> componentPaths = new LinkedHashSet<>();
        EntitySelectResolveVisitor resolveVisitor = new EntitySelectResolveVisitor(m, jpaProvider, componentPaths);

        // When no select infos are available, it can only be a root entity select
        if (selectInfos.isEmpty()) {
            // TODO: GroupByTest#testGroupByEntitySelect uses this. It's problematic because it's not aware of VALUES clause
            JoinNode rootNode = joinManager.getRootNodeOrFail("Empty select not allowed when having multiple roots!");
            String rootAlias = rootNode.getAliasInfo().getAlias();
            
            List<PathElementExpression> path = new ArrayList<>();
            path.add(new PropertyExpression(rootAlias));
            PathExpression pathExpression = new PathExpression(path, new SimplePathReference(rootNode, null, rootNode.getNodeType()), false, false);

            if (jpaProvider.supportsGroupByEntityAlias()) {
                Set<Expression> extractedGroupByExpressions = groupByExpressionGatheringVisitor.extractGroupByExpressions(pathExpression);
                if (!extractedGroupByExpressions.isEmpty()) {
                    collectGroupBys(sb, joinVisitor, groupByManager, hasGroupBy, extractedGroupByExpressions, null);
                }
            } else {
                resolveVisitor.visit(pathExpression);
                collectGroupBys(sb, joinVisitor, groupByManager, hasGroupBy, componentPaths, resolveVisitor.getRootNode());
            }
        } else {
            List<SelectInfo> infos = selectInfos;
            int size = selectInfos.size();
            for (int i = 0; i < size; i++) {
                final SelectInfo selectInfo = infos.get(i);
                if (jpaProvider.supportsGroupByEntityAlias()) {
                    Set<Expression> extractedGroupByExpressions = groupByExpressionGatheringVisitor.extractGroupByExpressions(selectInfo.getExpression());
                    if (!extractedGroupByExpressions.isEmpty()) {
                        collectGroupBys(sb, joinVisitor, groupByManager, hasGroupBy, extractedGroupByExpressions, null);
                    }
                } else {
                    componentPaths.clear();
                    selectInfo.getExpression().accept(resolveVisitor);

                    // The select info can only either an entity select or any other expression
                    // but entity selects can't be nested in other expressions, therefore we can differentiate here
                    if (componentPaths.size() > 0) {
                        collectGroupBys(sb, joinVisitor, groupByManager, hasGroupBy, componentPaths, resolveVisitor.getRootNode());
                    } else {
                        Set<Expression> extractedGroupByExpressions = groupByExpressionGatheringVisitor.extractGroupByExpressions(selectInfo.getExpression());
                        if (!extractedGroupByExpressions.isEmpty()) {
                            collectGroupBys(sb, joinVisitor, groupByManager, hasGroupBy, extractedGroupByExpressions, null);
                        }
                    }
                }
            }
        }

        queryGenerator.setBooleanLiteralRenderingContext(oldBooleanLiteralRenderingContext);
        groupByExpressionGatheringVisitor.clear();
    }

    private void collectGroupBys(StringBuilder sb, JoinVisitor joinVisitor, GroupByManager groupByManager, boolean hasGroupBy, Set<? extends Expression> componentPaths, JoinNode rootNode) {
        queryGenerator.setClauseType(ClauseType.GROUP_BY);
        queryGenerator.setQueryBuffer(sb);
        if (joinVisitor == null) {
            for (Expression expr : componentPaths) {
                sb.setLength(0);
                queryGenerator.generate(expr);
                groupByManager.collect(new ResolvedExpression(sb.toString(), expr), ClauseType.SELECT, hasGroupBy, joinVisitor);
            }
        } else {
            joinVisitor.setFromClause(ClauseType.SELECT);
            // Only reuse the existing node if the source of the component paths is an entity select
            boolean old = joinVisitor.setReuseExisting(rootNode != null);

            for (Expression expr : componentPaths) {
                expr.accept(joinVisitor);
                sb.setLength(0);
                queryGenerator.generate(expr);
                // When we encounter an association id access that works with a reused join node, we have to also generate a non-reused path
                // Otherwise we won't have the owning column in the group by clause which will lead to an error on some DBMS
                if (expr instanceof PathExpression && rootNode != ((PathExpression) expr).getBaseNode()) {
                    PathExpression pathExpression = (PathExpression) expr;
                    PathExpression associationIdAccess = pathExpression.clone(false);
                    associationIdAccess.setPathReference(pathExpression.getPathReference());
                    groupByManager.collect(new ResolvedExpression(sb.toString(), associationIdAccess), ClauseType.SELECT, hasGroupBy, joinVisitor);

                    joinVisitor.setReuseExisting(false);
                    expr.accept(joinVisitor);
                    sb.setLength(0);
                    queryGenerator.generate(expr);
                    groupByManager.collect(new ResolvedExpression(sb.toString(), expr), ClauseType.SELECT, hasGroupBy, joinVisitor);
                    joinVisitor.setReuseExisting(true);
                } else {
                    groupByManager.collect(new ResolvedExpression(sb.toString(), expr), ClauseType.SELECT, hasGroupBy, joinVisitor);
                }
            }
            joinVisitor.setReuseExisting(old);
        }
        queryGenerator.setClauseType(null);
    }

    void buildSelect(StringBuilder sb, boolean isInsertInto, boolean externalRepresentation) {
        sb.append("SELECT ");

        if (distinct) {
            sb.append("DISTINCT ");
        }

        List<SelectInfo> infos = selectInfos;
        int size = selectInfos.size();
        if (size == 0) {
            JoinNode rootNode = joinManager.getRootNodeOrFail("Empty select not allowed when having multiple roots!");
            rootNode.appendAlias(sb, externalRepresentation);
        } else {
            // we must not replace select alias since we would loose the original expressions
            queryGenerator.setClauseType(ClauseType.SELECT);
            queryGenerator.setQueryBuffer(sb);
            SimpleQueryGenerator.BooleanLiteralRenderingContext oldBooleanLiteralRenderingContext = queryGenerator.setBooleanLiteralRenderingContext(SimpleQueryGenerator.BooleanLiteralRenderingContext.CASE_WHEN);
            SimpleQueryGenerator.ParameterRenderingMode oldParameterRenderingMode;
            if (!mainQuery.getQueryConfiguration().isParameterAsLiteralRenderingEnabled() || isInsertInto) {
                // Insert into supports parameters
                oldParameterRenderingMode = queryGenerator.setParameterRenderingMode(SimpleQueryGenerator.ParameterRenderingMode.PLACEHOLDER);
            } else {
                oldParameterRenderingMode = queryGenerator.setParameterRenderingMode(SimpleQueryGenerator.ParameterRenderingMode.LITERAL);
            }
            
            for (int i = 0; i < size; i++) {
                if (i != 0) {
                    sb.append(", ");
                }
                
                applySelect(queryGenerator, sb, infos.get(i));
            }
            
            queryGenerator.setBooleanLiteralRenderingContext(oldBooleanLiteralRenderingContext);
            queryGenerator.setParameterRenderingMode(oldParameterRenderingMode);
            queryGenerator.setClauseType(null);
        }
    }

    @Override
    public void apply(ExpressionModifierVisitor<? super SelectInfo> visitor) {
        List<SelectInfo> infos = selectInfos;
        int size = selectInfos.size();
        // carry out transformations
        for (int i = 0; i < size; i++) {
            final SelectInfo selectInfo = infos.get(i);
            visitor.visit(selectInfo, ClauseType.SELECT);
        }
    }

    @SuppressWarnings("unchecked")
    <X> SubqueryInitiator<X> selectSubquery(X builder, final String selectAlias) {
        verifyBuilderEnded();
        clearDefaultSelects();

        subqueryBuilderListener = new SelectSubqueryBuilderListener<X>(selectAlias);
        SubqueryInitiator<X> initiator = subqueryInitFactory.createSubqueryInitiator(builder, (SubqueryBuilderListener<X>) subqueryBuilderListener, false, ClauseType.SELECT);
        subqueryBuilderListener.onInitiatorStarted(initiator);
        return initiator;
    }

    @SuppressWarnings("unchecked")
    <X> SubqueryInitiator<X> selectSubquery(X builder, String subqueryAlias, Expression expression, String selectAlias) {
        verifyBuilderEnded();
        clearDefaultSelects();

        subqueryBuilderListener = new SuperExpressionSelectSubqueryBuilderListener<X>(subqueryAlias, expression, selectAlias);
        SubqueryInitiator<X> initiator = subqueryInitFactory.createSubqueryInitiator(builder, (SubqueryBuilderListener<X>) subqueryBuilderListener, false, ClauseType.SELECT);
        subqueryBuilderListener.onInitiatorStarted(initiator);
        return initiator;
    }

    <X> MultipleSubqueryInitiator<X> selectSubqueries(X builder, Expression expression, final String selectAlias) {
        verifyBuilderEnded();
        clearDefaultSelects();

        MultipleSubqueryInitiator<X> initiator = new MultipleSubqueryInitiatorImpl<X>(builder, expression, new ExpressionBuilderEndedListener() {
            
            @Override
            public void onBuilderEnded(ExpressionBuilder builder) {
                select(builder.getExpression(), selectAlias);
            }
            
        }, subqueryInitFactory, ClauseType.SELECT);
        return initiator;
    }

    @SuppressWarnings("unchecked")
    <X> SubqueryBuilder<X> selectSubquery(X builder, final String selectAlias, FullQueryBuilder<?, ?> criteriaBuilder) {
        verifyBuilderEnded();
        clearDefaultSelects();

        subqueryBuilderListener = new SelectSubqueryBuilderListener<X>(selectAlias);
        SubqueryBuilderImpl<X> subqueryBuilder = subqueryInitFactory.createSubqueryBuilder(builder, (SubqueryBuilderListener<X>) subqueryBuilderListener, false, criteriaBuilder, ClauseType.SELECT);
        subqueryBuilderListener.onBuilderStarted((SubqueryInternalBuilder) subqueryBuilder);
        return subqueryBuilder;
    }

    @SuppressWarnings("unchecked")
    <X> SubqueryBuilder<X> selectSubquery(X builder, String subqueryAlias, Expression expression, String selectAlias, FullQueryBuilder<?, ?> criteriaBuilder) {
        verifyBuilderEnded();
        clearDefaultSelects();

        subqueryBuilderListener = new SuperExpressionSelectSubqueryBuilderListener<X>(subqueryAlias, expression, selectAlias);
        SubqueryBuilderImpl<X> subqueryBuilder = subqueryInitFactory.createSubqueryBuilder(builder, (SubqueryBuilderListener<X>) subqueryBuilderListener, false, criteriaBuilder, ClauseType.SELECT);
        subqueryBuilderListener.onBuilderStarted((SubqueryInternalBuilder) subqueryBuilder);
        return subqueryBuilder;
    }

    <X> CaseWhenStarterBuilder<X> selectCase(X builder, final String selectAlias) {
        verifyBuilderEnded();
        clearDefaultSelects();

        caseExpressionBuilderListener = new CaseExpressionBuilderListener(selectAlias);
        return caseExpressionBuilderListener.startBuilder(new CaseWhenBuilderImpl<X>(builder, caseExpressionBuilderListener, subqueryInitFactory, expressionFactory, parameterManager, ClauseType.SELECT));
    }

    <X> SimpleCaseWhenStarterBuilder<X> selectSimpleCase(X builder, final String selectAlias, Expression caseOperandExpression) {
        verifyBuilderEnded();
        clearDefaultSelects();

        caseExpressionBuilderListener = new CaseExpressionBuilderListener(selectAlias);
        return caseExpressionBuilderListener.startBuilder(new SimpleCaseWhenBuilderImpl<X>(builder, caseExpressionBuilderListener, expressionFactory, caseOperandExpression, subqueryInitFactory, parameterManager, ClauseType.SELECT));
    }

    Class<?> getExpectedQueryResultType() {
        // Tuple case
        if (selectInfos.size() > 1) {
            return Object[].class;
        }

        return jpaProvider.getDefaultQueryResultType();
    }

    void select(Expression expr, String selectAlias) {
        select(expr, selectAlias, -1);
    }

    void select(Expression expr, String selectAlias, int index) {
        verifyBuilderEnded();
        clearDefaultSelects();
        selectInternal(expr, selectAlias, index);
    }

    private void selectInternal(Expression expr, String selectAlias) {
        selectInternal(expr, selectAlias, -1);
    }

    private void selectInternal(Expression expr, String selectAlias, int index) {
        SelectInfo selectInfo = new SelectInfo(expr, selectAlias, aliasManager);
        if (selectAlias != null) {
            aliasManager.registerAliasInfo(selectInfo);
            selectAliasToPositionMap.put(selectAlias, selectAliasToPositionMap.size());
        }
        if (index == -1) {
            selectInfos.add(selectInfo);
        } else {
            selectInfos.add(index, selectInfo);
        }
        hasSizeSelect = hasSizeSelect || ExpressionUtils.containsSizeExpression(selectInfo.getExpression());

        registerParameterExpressions(expr);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    <Y, X extends AbstractFullQueryBuilder<?, ?, ?, ?, ?>> SelectObjectBuilder<? extends FullQueryBuilder<Y, ?>> selectNew(X builder, Class<Y> clazz) {
        verifyBuilderEnded();
        clearDefaultSelects();

        if (selectObjectBuilder != null) {
            throw new IllegalStateException("Only one selectNew is allowed");
        }

        selectObjectBuilder = selectObjectBuilderEndedListener.startBuilder(new SelectObjectBuilderImpl(builder, selectObjectBuilderEndedListener, subqueryInitFactory, expressionFactory));
        objectBuilder = new ClassObjectBuilder(clazz);
        return (SelectObjectBuilder) selectObjectBuilder;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    <Y, X extends AbstractFullQueryBuilder<?, ?, ?, ?, ?>> SelectObjectBuilder<? extends FullQueryBuilder<Y, ?>> selectNew(X builder, Constructor<Y> constructor) {
        verifyBuilderEnded();
        clearDefaultSelects();

        if (selectObjectBuilder != null) {
            throw new IllegalStateException("Only one selectNew is allowed");
        }

        selectObjectBuilder = selectObjectBuilderEndedListener.startBuilder(new SelectObjectBuilderImpl(builder, selectObjectBuilderEndedListener, subqueryInitFactory, expressionFactory));
        objectBuilder = new ConstructorObjectBuilder(constructor);
        return (SelectObjectBuilder) selectObjectBuilder;
    }

    @SuppressWarnings("unchecked")
    <X extends FullQueryBuilder<?, X>> void selectNew(X builder, ObjectBuilder<?> objectBuilder) {
        verifyBuilderEnded();
        clearDefaultSelects();

        if (selectObjectBuilder != null) {
            throw new IllegalStateException("Only one selectNew is allowed");
        }

        objectBuilder.applySelects(builder);
        this.objectBuilder = (ObjectBuilder<T>) objectBuilder;
    }

    void setDefaultSelect(Map<JoinNode, JoinNode> nodeMapping, List<SelectInfo> selectInfos) {
        if (!this.selectInfos.isEmpty()) {
            throw new IllegalStateException("Can't set default select when explicit select items are already set!");
        }

        hasDefaultSelect = true;
        Set<JoinNode> nodes = null;
        JoinNodeGathererVisitor visitor = null;
        if (nodeMapping != null) {
            nodes = new HashSet<>();
            visitor = new JoinNodeGathererVisitor(nodes);
        }
        for (int i = 0; i < selectInfos.size(); i++) {
            SelectInfo selectInfo = selectInfos.get(i);
            String selectAlias = selectInfo.getAlias();
            Expression expr = subqueryInitFactory.reattachSubqueries(selectInfo.getExpression().clone(false), ClauseType.SELECT);
            if (nodeMapping != null) {
                selectInfo.getExpression().accept(visitor);
            }
            selectInternal(expr, selectAlias);
        }

        if (nodeMapping != null) {
            defaultSelectNodes = new HashSet<>();
            for (JoinNode node : nodes) {
                defaultSelectNodes.add(nodeMapping.get(node));
            }
        }
    }

    void distinct() {
        this.distinct = true;
    }

    boolean isDistinct() {
        return this.distinct;
    }

    void setDefaultSelect() {
        hasDefaultSelect = true;
    }

    void unsetDefaultSelect() {
        hasDefaultSelect = false;
    }

    private void clearDefaultSelects() {
        if (!hasDefaultSelect) {
            return;
        }

        for (int i = 0; i < selectInfos.size(); i++) {
            SelectInfo selectInfo = selectInfos.get(i);
            if (selectInfo.getAlias() != null) {
                queryBuilder.inlineSelectAlias(selectInfo.getAlias(), selectInfo.getExpression());
            }
            aliasManager.unregisterAliasInfoForBottomLevel(selectInfo);
            unregisterParameterExpressions(selectInfo.getExpression());
        }

        selectAliasToPositionMap.clear();
        selectInfos.clear();
        hasDefaultSelect = false;
        hasSizeSelect = false;
        if (defaultSelectNodes != null) {
            joinManager.removeSelectOnlyNodes(defaultSelectNodes);
        }
        defaultSelectNodes = null;
    }

    private void applySelect(ResolvingQueryGenerator queryGenerator, StringBuilder sb, SelectInfo select) {
        try {
            queryGenerator.addAlias(select.alias);
            queryGenerator.generate(select.getExpression());
            if (select.alias != null) {
                sb.append(" AS ").append(select.alias);
            }
        } finally {
            queryGenerator.removeAlias(select.alias);
        }
    }

    // Thanks to the Oracle 30 characters limitation, we have to substitute the original JPQL alias here since we kind of pass that through to SQL

    public String getSubquerySelectAlias(SelectInfo s) {
        for (int i = 0; i < selectInfos.size(); i++) {
            if (s == selectInfos.get(i)) {
                return "synth_alias_" + i;
            }
        }

        return null;
    }

    public String getSubquerySelectAlias(String alias) {
        for (int i = 0; i < selectInfos.size(); i++) {
            if (alias.equals(selectInfos.get(i).alias)) {
                return "synth_alias_" + i;
            }
        }

        return null;
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
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

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
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

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    private class CaseExpressionBuilderListener extends ExpressionBuilderEndedListenerImpl {

        private final String selectAlias;

        public CaseExpressionBuilderListener(String selectAlias) {
            this.selectAlias = selectAlias;
        }

        @Override
        public void onBuilderEnded(ExpressionBuilder builder) {
            super.onBuilderEnded(builder);
            select(builder.getExpression(), selectAlias);
        }

    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
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
            }
        }

    }

}
