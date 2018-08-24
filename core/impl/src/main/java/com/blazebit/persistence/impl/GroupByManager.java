/*
 * Copyright 2014 - 2018 Blazebit.
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

import com.blazebit.persistence.parser.SimpleQueryGenerator;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.Expression.Visitor;
import com.blazebit.persistence.parser.expression.modifier.ExpressionModifier;
import com.blazebit.persistence.impl.transform.ExpressionModifierVisitor;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class GroupByManager extends AbstractManager<ExpressionModifier> {

    private static final ResolvedExpression[] EMPTY = new ResolvedExpression[0];

    private final FunctionalDependencyAnalyzerVisitor functionalDependencyAnalyzerVisitor;
    /**
     * We use an ArrayList since a HashSet causes problems when the path reference in the expression is changed
     * after it was inserted into the set (e.g. when implicit joining is performed).
     */
    private final List<NodeInfo> groupByInfos;
    // These are the collected group by clauses
    private final Map<ResolvedExpression, Set<ClauseType>> groupByClauses;

    GroupByManager(ResolvingQueryGenerator queryGenerator, ParameterManager parameterManager, SubqueryInitiatorFactory subqueryInitFactory, FunctionalDependencyAnalyzerVisitor functionalDependencyAnalyzerVisitor) {
        super(queryGenerator, parameterManager, subqueryInitFactory);
        this.functionalDependencyAnalyzerVisitor = functionalDependencyAnalyzerVisitor;
        groupByInfos = new ArrayList<>();
        groupByClauses = new LinkedHashMap<>();
    }

    void applyFrom(GroupByManager groupByManager) {
        for (NodeInfo info : groupByManager.groupByInfos) {
            groupBy(subqueryInitFactory.reattachSubqueries(info.getExpression().clone(true), ClauseType.GROUP_BY));
        }
    }

    @Override
    public ClauseType getClauseType() {
        return ClauseType.GROUP_BY;
    }

    public void groupBy(Expression expr) {
        groupByInfos.add(new NodeInfo(expr));
        registerParameterExpressions(expr);
    }
    
    boolean existsGroupBy(Expression expr) {
        return groupByInfos.contains(new NodeInfo(expr));
    }

    void collectGroupByClauses() {
        if (groupByInfos.isEmpty()) {
            return;
        }

        SimpleQueryGenerator.BooleanLiteralRenderingContext oldBooleanLiteralRenderingContext = queryGenerator.setBooleanLiteralRenderingContext(SimpleQueryGenerator.BooleanLiteralRenderingContext.CASE_WHEN);
        StringBuilder sb = new StringBuilder();

        queryGenerator.setClauseType(ClauseType.GROUP_BY);
        queryGenerator.setQueryBuffer(sb);
        for (NodeInfo info : groupByInfos) {
            sb.setLength(0);
            queryGenerator.generate(info.getExpression());
            collect(new ResolvedExpression(sb.toString(), info.getExpression()), ClauseType.GROUP_BY, false);
        }
        queryGenerator.setBooleanLiteralRenderingContext(oldBooleanLiteralRenderingContext);
        queryGenerator.setClauseType(null);
    }

    void buildGroupBy(StringBuilder sb) {
        buildGroupBy(sb, EnumSet.noneOf(ClauseType.class));
    }

    void buildGroupBy(StringBuilder sb, Set<ClauseType> excludedClauses) {
        if (!groupByClauses.isEmpty()) {
            queryGenerator.setQueryBuffer(sb);
            int initialIndex = sb.length();
            sb.append(" GROUP BY ");
            int emptyIndex = sb.length();
            for (Map.Entry<ResolvedExpression, Set<ClauseType>> entry : groupByClauses.entrySet()) {
                if (!excludedClauses.containsAll(entry.getValue())) {
                    entry.getKey().getExpression().accept(queryGenerator);
                    sb.append(", ");
                }
            }

            if (sb.length() == emptyIndex) {
                sb.setLength(initialIndex);
            } else {
                sb.setLength(sb.length() - 2);
            }
        }
    }

    void buildGroupBy(StringBuilder sb, Set<ClauseType> excludedClauses, ResolvedExpression[] additionalGroupBys) {
        queryGenerator.setQueryBuffer(sb);
        if (groupByClauses.isEmpty()) {
            if (additionalGroupBys.length != 0) {
                sb.append(" GROUP BY ");
                for (ResolvedExpression additionalGroupBy : additionalGroupBys) {
                    additionalGroupBy.getExpression().accept(queryGenerator);
                    sb.append(", ");
                }
                sb.setLength(sb.length() - 2);
            }
        } else {
            int initialIndex = sb.length();
            sb.append(" GROUP BY ");
            int emptyIndex = sb.length();
            for (Map.Entry<ResolvedExpression, Set<ClauseType>> entry : groupByClauses.entrySet()) {
                if (!excludedClauses.containsAll(entry.getValue())) {
                    entry.getKey().getExpression().accept(queryGenerator);
                    sb.append(", ");
                }
            }

            if (additionalGroupBys.length == 0) {
                if (sb.length() == emptyIndex) {
                    sb.setLength(initialIndex);
                } else {
                    sb.setLength(sb.length() - 2);
                }
            } else {
                for (ResolvedExpression additionalGroupBy : additionalGroupBys) {
                    Set<ClauseType> clauseTypes = groupByClauses.get(additionalGroupBy);
                    if ((clauseTypes == null || excludedClauses.containsAll(clauseTypes))) {
                        additionalGroupBy.getExpression().accept(queryGenerator);
                        sb.append(", ");
                    }
                }
                sb.setLength(sb.length() - 2);
            }
        }
    }

    @Override
    public void apply(ExpressionModifierVisitor<? super ExpressionModifier> visitor) {
        List<NodeInfo> infos = groupByInfos;
        int size = infos.size();
        for (int i = 0; i < size; i++) {
            final NodeInfo groupBy = infos.get(i);
            visitor.visit(groupBy, ClauseType.GROUP_BY);
        }
    }

    void acceptVisitor(Visitor v) {
        for (NodeInfo groupBy : groupByInfos) {
            groupBy.getExpression().accept(v);
        }
    }

    public boolean hasGroupBys() {
        return groupByInfos.size() > 0;
    }

    boolean isEmpty() {
        return groupByInfos.isEmpty();
    }

    public void resetCollected() {
        groupByClauses.clear();
    }

    public void collect(ResolvedExpression expression, ClauseType clauseType, boolean hasGroupBy) {
        Set<ClauseType> clauseTypes = groupByClauses.get(expression);
        if (clauseTypes == null) {
            clauseTypes = EnumSet.of(clauseType);
            if (hasGroupBy) {
                expression.getExpression().accept(ImplicitGroupByClauseDependencyRegistrationVisitor.INSTANCE);
            }
            groupByClauses.put(expression, clauseTypes);
        } else {
            clauseTypes.add(clauseType);
        }
    }

    public void collect(ResolvedExpression expression, Set<ClauseType> newClauseTypes) {
        Set<ClauseType> clauseTypes = groupByClauses.get(expression);
        if (clauseTypes == null) {
            clauseTypes = EnumSet.copyOf(newClauseTypes);
            expression.getExpression().accept(ImplicitGroupByClauseDependencyRegistrationVisitor.INSTANCE);
            groupByClauses.put(expression, clauseTypes);
        } else {
            clauseTypes.addAll(newClauseTypes);
        }
    }

    public Map<ResolvedExpression, Set<ClauseType>> getCollectedGroupByClauses() {
        return groupByClauses;
    }

    public boolean hasCollectedGroupByClauses() {
        return groupByClauses.size() != 0;
    }

    public boolean hasCollectedGroupByClauses(Set<ClauseType> excludedClauses) {
        for (Map.Entry<ResolvedExpression, Set<ClauseType>> entry : groupByClauses.entrySet()) {
            if (!excludedClauses.containsAll(entry.getValue())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + (this.groupByInfos != null ? this.groupByInfos.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GroupByManager other = (GroupByManager) obj;
        if (this.groupByInfos != other.groupByInfos && (this.groupByInfos == null || !this.groupByInfos.equals(other.groupByInfos))) {
            return false;
        }
        return true;
    }

}
