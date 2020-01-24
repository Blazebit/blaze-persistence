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

import com.blazebit.persistence.WindowBuilder;
import com.blazebit.persistence.impl.transform.ExpressionModifierVisitor;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.ExpressionCopyContext;
import com.blazebit.persistence.parser.expression.OrderByItem;
import com.blazebit.persistence.parser.expression.WindowDefinition;
import com.blazebit.persistence.parser.expression.WindowFrameExclusionType;
import com.blazebit.persistence.parser.expression.WindowFramePositionType;
import com.blazebit.persistence.parser.expression.modifier.ExpressionModifier;
import com.blazebit.persistence.parser.predicate.Predicate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public class WindowManager<T> extends AbstractManager<ExpressionModifier> {

    private final Map<String, WindowDefinition> windows;
    private QueryContext queryContext;
    private WindowBuilder<T> windowBuilder;

    public WindowManager(ResolvingQueryGenerator queryGenerator, ParameterManager parameterManager, SubqueryInitiatorFactory subqueryInitFactory) {
        super(queryGenerator, parameterManager, subqueryInitFactory);
        this.windows = new LinkedHashMap<>(0);
    }

    @Override
    public void apply(ExpressionModifierVisitor<? super ExpressionModifier> visitor) {
    }

    public WindowDefinition resolve(WindowDefinition windowDefinition) {
        if (windowDefinition.getWindowName() == null) {
            return windowDefinition;
        }

        WindowDefinition baseWindowDefinition = windows.get(windowDefinition.getWindowName());
        // See https://www.postgresql.org/docs/current/sql-select.html
        if (baseWindowDefinition == null) {
            throw new IllegalArgumentException("There is no window named '" + windowDefinition.getWindowName() + "' registered!");
        }
        if (baseWindowDefinition.getFrameMode() != null && (!windowDefinition.getPartitionExpressions().isEmpty() || !windowDefinition.getOrderByExpressions().isEmpty() || windowDefinition.getFrameMode() != null)) {
            throw new IllegalArgumentException("The base window '" + windowDefinition.getWindowName() + "' is not allowed to specify a frame clause when being reused!");
        }
        if (!windowDefinition.getPartitionExpressions().isEmpty()) {
            throw new IllegalArgumentException("A window referencing the base window '" + windowDefinition.getWindowName() + "' isn't allowed to specify it's own PARTITION BY clause!");
        }
        if (!windowDefinition.getOrderByExpressions().isEmpty() && !baseWindowDefinition.getOrderByExpressions().isEmpty()) {
            throw new IllegalArgumentException("A window referencing the base window '" + windowDefinition.getWindowName() + "' isn't allowed to specify it's own ORDER BY clause because the base window already has an ORDER BY clause!");
        }
        List<Expression> partitionExpressions = baseWindowDefinition.getPartitionExpressions();
        List<OrderByItem> orderByExpressions = baseWindowDefinition.getOrderByExpressions();
        if (orderByExpressions.isEmpty()) {
            orderByExpressions = windowDefinition.getOrderByExpressions();
        }

        WindowDefinition frameClause = windowDefinition.getFrameMode() == null ? baseWindowDefinition : windowDefinition;

        return new WindowDefinition(
                null,
                partitionExpressions,
                orderByExpressions,
                windowDefinition.getFilterPredicate(),
                frameClause.getFrameMode(),
                frameClause.getFrameStartType(),
                frameClause.getFrameStartExpression(),
                frameClause.getFrameEndType(),
                frameClause.getFrameEndExpression(),
                frameClause.getFrameExclusionType()
        );
    }

    @Override
    public ClauseType getClauseType() {
        return ClauseType.WINDOW;
    }

    void init(AbstractCommonQueryBuilder<?, ?, ?, ?, ?> queryBuilder) {
        if (queryContext == null) {
            this.queryContext = new QueryContext(queryBuilder, ClauseType.WINDOW);
        }
    }

    void applyFrom(WindowManager<?> windowManager, ExpressionCopyContext copyContext) {
        for (Map.Entry<String, WindowDefinition> entry : windowManager.windows.entrySet()) {
            WindowDefinition windowDefinition = entry.getValue();
            int size = windowDefinition.getPartitionExpressions().size();
            List<Expression> partitionExpressions = new ArrayList<>(size);
            List<Expression> expressions = windowDefinition.getPartitionExpressions();
            Expression expr;
            for (int i = 0; i < size; i++) {
                partitionExpressions.add(expr = expressions.get(i).copy(copyContext));
                parameterManager.collectParameterRegistrations(expr, ClauseType.WINDOW, subqueryInitFactory.getQueryBuilder());
            }

            size = windowDefinition.getOrderByExpressions().size();
            List<OrderByItem> orderByExpressions = new ArrayList<>(size);
            OrderByItem orderByItem;
            for (int i = 0; i < size; i++) {
                orderByExpressions.add(orderByItem = windowDefinition.getOrderByExpressions().get(i).copy(copyContext));
                parameterManager.collectParameterRegistrations(orderByItem.getExpression(), ClauseType.WINDOW, subqueryInitFactory.getQueryBuilder());
            }

            Predicate filterPredicate = null;
            if (windowDefinition.getFilterPredicate() != null) {
                filterPredicate = windowDefinition.getFilterPredicate().copy(copyContext);
                parameterManager.collectParameterRegistrations(filterPredicate, ClauseType.WINDOW, subqueryInitFactory.getQueryBuilder());
            }

            Expression frameStartExpression = null;
            if (windowDefinition.getFrameStartExpression() != null) {
                frameStartExpression = windowDefinition.getFrameStartExpression().copy(copyContext);
                parameterManager.collectParameterRegistrations(frameStartExpression, ClauseType.WINDOW, subqueryInitFactory.getQueryBuilder());
            }

            Expression frameEndExpression = null;
            if (windowDefinition.getFrameEndExpression() != null) {
                frameEndExpression = windowDefinition.getFrameEndExpression().copy(copyContext);
                parameterManager.collectParameterRegistrations(frameEndExpression, ClauseType.WINDOW, subqueryInitFactory.getQueryBuilder());
            }

            windows.put(entry.getKey(), new WindowDefinition(entry.getKey(), partitionExpressions, orderByExpressions, filterPredicate, windowDefinition.getFrameMode(), windowDefinition.getFrameStartType(), frameStartExpression, windowDefinition.getFrameEndType(), frameEndExpression, windowDefinition.getFrameExclusionType()));
        }
    }

    QueryContext getQueryContext() {
        return queryContext;
    }

    public Map<String, WindowDefinition> getWindows() {
        return windows;
    }

    public void onBuilderEnded(String name, WindowDefinition windowDefinition) {
        windows.put(name, windowDefinition);
        windowBuilder = null;
    }

    void verifyBuilderEnded() {
        if (windowBuilder != null) {
            throw new BuilderChainingException("A window builder was not ended properly.");
        }
    }

    WindowBuilder<T> window(String name, T result) {
        return this.windowBuilder = new WindowBuilderImpl<>(queryGenerator, parameterManager, subqueryInitFactory, subqueryInitFactory.getQueryBuilder().expressionFactory, this, result, name);
    }

    public void buildWindow(StringBuilder sb) {
        if (windows.isEmpty()) {
            return;
        }

        sb.append(" WINDOW ");
        queryGenerator.setClauseType(ClauseType.WINDOW);
        queryGenerator.setQueryBuffer(sb);
        for (Map.Entry<String, WindowDefinition> entry : windows.entrySet()) {
            sb.append(entry.getKey()).append(" AS (");
            WindowDefinition windowDefinition = entry.getValue();
            boolean needsSpace = false;
            if (windowDefinition.getWindowName() != null) {
                sb.append(windowDefinition.getWindowName());
                needsSpace = true;
            }

            if (!windowDefinition.getPartitionExpressions().isEmpty()) {
                if (needsSpace) {
                    sb.append(' ');
                }
                needsSpace = true;
                sb.append("PARTITION BY ");
                for (Expression partitionExpression : windowDefinition.getPartitionExpressions()) {
                    queryGenerator.generate(partitionExpression);
                    sb.append(", ");
                }
                sb.setLength(sb.length() - 2);
            }

            if (!windowDefinition.getOrderByExpressions().isEmpty()) {
                if (needsSpace) {
                    sb.append(' ');
                }
                needsSpace = true;
                sb.append("ORDER BY ");
                for (OrderByItem orderByExpression : windowDefinition.getOrderByExpressions()) {
                    queryGenerator.generate(orderByExpression.getExpression());
                    if (orderByExpression.isAscending()) {
                        sb.append(" ASC ");
                    } else {
                        sb.append(" DESC ");
                    }
                    if (orderByExpression.isNullFirst()) {
                        sb.append("NULLS FIRST, ");
                    } else {
                        sb.append("NULLS LAST, ");
                    }

                }
                sb.setLength(sb.length() - 2);
            }

            if (windowDefinition.getFrameMode() != null) {
                if (needsSpace) {
                    sb.append(' ');
                }

                sb.append(windowDefinition.getFrameMode().name());
                if (windowDefinition.getFrameEndType() != null) {
                    sb.append(" BETWEEN");
                }

                if (windowDefinition.getFrameStartExpression() != null) {
                    sb.append(' ');
                    queryGenerator.generate(windowDefinition.getFrameStartExpression());
                }

                sb.append(getFrameType(windowDefinition.getFrameStartType()));

                if (windowDefinition.getFrameEndType() != null) {
                    sb.append(" AND");

                    if (windowDefinition.getFrameEndExpression() != null) {
                        sb.append(' ');
                        queryGenerator.generate(windowDefinition.getFrameEndExpression());
                    }
                    sb.append(getFrameType(windowDefinition.getFrameEndType()));
                }

                if (windowDefinition.getFrameExclusionType() != null) {
                    sb.append(getFrameExclusionType(windowDefinition.getFrameExclusionType()));
                }
            }

            sb.append("), ");
        }

        sb.setLength(sb.length() - 2);
        queryGenerator.setClauseType(null);
    }

    protected String getFrameExclusionType(WindowFrameExclusionType frameExclusionType) {
        switch (frameExclusionType) {
            case EXCLUDE_CURRENT_ROW:
                return " EXCLUDE CURRENT ROW";
            case EXCLUDE_GROUP:
                return " EXCLUDE GROUP";
            case EXCLUDE_NO_OTHERS:
                return " EXCLUDE NO OTHERS";
            case EXCLUDE_TIES:
                return " EXCLUDE TIES";
            default:
                throw new IllegalArgumentException("No branch for " + frameExclusionType);
        }
    }

    protected String getFrameType(WindowFramePositionType frameStartType) {
        switch (frameStartType) {
            case CURRENT_ROW:
                return " CURRENT ROW";
            case BOUNDED_FOLLOWING:
                return " FOLLOWING";
            case BOUNDED_PRECEDING:
                return " PRECEDING";
            case UNBOUNDED_FOLLOWING:
                return " UNBOUNDED FOLLOWING";
            case UNBOUNDED_PRECEDING:
                return " UNBOUNDED PRECEDING";
            default:
                throw new IllegalArgumentException("No branch for " + frameStartType);
        }
    }
}
