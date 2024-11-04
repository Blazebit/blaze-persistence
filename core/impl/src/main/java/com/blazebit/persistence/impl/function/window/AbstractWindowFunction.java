/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.window;

import com.blazebit.persistence.parser.expression.WindowFrameExclusionType;
import com.blazebit.persistence.parser.expression.WindowFrameMode;
import com.blazebit.persistence.parser.expression.WindowFramePositionType;
import com.blazebit.persistence.impl.function.Order;
import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * See the following for details: https://www.postgresql.org/docs/current/sql-expressions.html#SYNTAX-WINDOW-FUNCTIONS
 *
 * @author Jan-Willem Gmelig Meyling
 * @author Sayra Ranjha
 * @since 1.4.0
 */
public abstract class AbstractWindowFunction implements JpqlFunction {

    protected final String functionName;
    protected final boolean nullIsSmallest;
    protected final boolean supportsNullPrecedence;
    protected final boolean supportsFilterClause;
    protected final boolean allowsFilterClause;

    protected AbstractWindowFunction(String functionName, boolean nullIsSmallest, boolean supportsNullPrecedence, boolean supportsFilterClause, boolean allowsFilterClause) {
        this.functionName = functionName;
        this.nullIsSmallest = nullIsSmallest;
        this.supportsNullPrecedence = supportsNullPrecedence;
        this.supportsFilterClause = supportsFilterClause;
        this.allowsFilterClause = allowsFilterClause;
    }

    @Override
    public boolean hasArguments() {
        return true;
    }

    @Override
    public boolean hasParenthesesIfNoArguments() {
        return true;
    }

    @Override
    public Class<?> getReturnType(Class<?> firstArgumentType) {
        return firstArgumentType;
    }

    @Override
    public final void render(FunctionRenderContext context) {
        WindowFunction windowFunction = getWindowFunction(context);
        render(context, windowFunction);
    }

    protected WindowFunction getWindowFunction(FunctionRenderContext context) {
        return getWindowFunction(context, new WindowFunction(functionName), 0);
    }

    protected <T extends WindowFunction> T getWindowFunction(FunctionRenderContext context, T function, int startIndex) {
        WindowFunction windowFunction = function;
        Mode mode = Mode.ARGUMENTS;
        Enum<?> argumentMode = null;
        Boolean startFrame = null;

        for (int parameterIndex = startIndex; parameterIndex < context.getArgumentsSize(); parameterIndex++) {
            String argument = context.getArgument(parameterIndex);

            switch (argument.toUpperCase()) {
                case "'WITHIN GROUP'":
                    mode = Mode.WITHIN_GROUP;
                    break;
                case "'FILTER'":
                    if (!allowsFilterClause) {
                        throw new IllegalArgumentException("FILTER clause is disallowed for function: " + functionName);
                    }
                    mode = Mode.FILTER;
                    break;
                case "'PARTITION BY'":
                    mode = Mode.PARTITION_BY;
                    break;
                case "'ORDER BY'":
                    mode = Mode.ORDER_BY;
                    break;
                case "'RANGE'":
                    mode = Mode.FRAME_CLAUSE;
                    windowFunction.frameMode = WindowFrameMode.RANGE;
                    startFrame = true;
                    break;
                case "'ROWS'":
                    mode = Mode.FRAME_CLAUSE;
                    windowFunction.frameMode = WindowFrameMode.ROWS;
                    startFrame = true;
                    break;
                case "'GROUPS'":
                    mode = Mode.FRAME_CLAUSE;
                    windowFunction.frameMode = WindowFrameMode.GROUPS;
                    startFrame = true;
                    break;
                default:
                    Order order;
                    switch (mode) {
                        case ARGUMENTS:
                            if ("'DISTINCT'".equals(argument)) {
                                windowFunction.distinct = true;
                            } else {
                                argumentMode = processArgument(argumentMode, windowFunction, argument);
                            }
                            break;
                        case WITHIN_GROUP:
                            order = null;
                            if (context.getArgumentsSize() > parameterIndex + 1) {
                                String sortOrder = context.getArgument(parameterIndex + 1);
                                order = getOrder(sortOrder, argument);
                                if (order != null) {
                                    parameterIndex++;
                                }
                            }

                            if (order == null) {
                                order = new Order(argument, null, null);
                            }

                            windowFunction.withinGroup.add(order);
                            break;
                        case FILTER:
                            windowFunction.filterExpressions.add(argument.substring("case when ".length(), argument.length() - " then 1 else 0 end".length()));
                            break;
                        case PARTITION_BY:
                            windowFunction.partitionExpressions.add(argument);
                            break;
                        case ORDER_BY:
                            order = null;
                            if (context.getArgumentsSize() > parameterIndex + 1) {
                                String sortOrder = context.getArgument(parameterIndex + 1);
                                order = getOrder(sortOrder, argument);
                                if (order != null) {
                                    parameterIndex++;
                                }
                            }

                            if (order == null) {
                                order = new Order(argument, null, null);
                            }

                            windowFunction.orderBys.add(order);
                            break;
                        case FRAME_CLAUSE:
                            if (startFrame == null) {
                                throw new IllegalArgumentException("Illegal frame clause, expected to see 'RANGE' first.");
                            }
                            switch (argument) {
                                case "'BETWEEN'":
                                    startFrame = true;
                                    break;
                                case "'AND'":
                                    startFrame = false;
                                    break;
                                case "'UNBOUNDED PRECEDING'":
                                    if (startFrame) {
                                        windowFunction.frameStartType = WindowFramePositionType.UNBOUNDED_PRECEDING;
                                    } else {
                                        throw new IllegalArgumentException("Illegal frame clause! The end frame type can't be UNBOUNDED_PRECEDING");
                                    }
                                    break;
                                case "'PRECEDING'":
                                    if (startFrame) {
                                        windowFunction.frameStartType = WindowFramePositionType.BOUNDED_PRECEDING;
                                    } else {
                                        windowFunction.frameEndType = WindowFramePositionType.BOUNDED_PRECEDING;
                                    }
                                    break;
                                case "'CURRENT ROW'":
                                    if (startFrame) {
                                        windowFunction.frameStartType = WindowFramePositionType.CURRENT_ROW;
                                    } else {
                                        windowFunction.frameEndType = WindowFramePositionType.CURRENT_ROW;
                                    }
                                    break;
                                case "'FOLLOWING'":
                                    if (startFrame) {
                                        windowFunction.frameStartType = WindowFramePositionType.BOUNDED_FOLLOWING;
                                    } else {
                                        windowFunction.frameEndType = WindowFramePositionType.BOUNDED_FOLLOWING;
                                    }
                                    break;
                                case "'UNBOUNDED FOLLOWING'":
                                    if (startFrame) {
                                        throw new IllegalArgumentException("Illegal frame clause! The start frame type can't be UNBOUNDED_FOLLOWING");
                                    } else {
                                        windowFunction.frameEndType = WindowFramePositionType.UNBOUNDED_FOLLOWING;
                                    }
                                    break;
                                default:
                                    if (startFrame) {
                                        windowFunction.frameStartExpression = argument;
                                    } else {
                                        windowFunction.frameEndExpression = argument;
                                    }
                                    break;
                            }

                            break;
                        default:
                            throw new IllegalArgumentException("No branch for " + mode);
                    }
            }
        }

        return function;
    }

    protected Enum<?> processArgument(Enum<?> mode, WindowFunction windowFunction, String argument) {
        windowFunction.arguments.add(argument);
        return null;
    }

    private static Order getOrder(String sort, String expression) {
        String type = sort.trim().toUpperCase();

        if ("'ASC'".equals(type)) {
            return new Order(expression, true, null);
        } else if ("'DESC'".equals(type)) {
            return new Order(expression, false, null);
        } else if ("'ASC NULLS FIRST'".equals(type)) {
            return new Order(expression, true, true);
        } else if ("'ASC NULLS LAST'".equals(type)) {
            return new Order(expression, true, false);
        } else if ("'DESC NULLS FIRST'".equals(type)) {
            return new Order(expression, false, true);
        } else if ("'DESC NULLS LAST'".equals(type)) {
            return new Order(expression, false, false);
        }

        return null;
    }

    protected boolean requiresOver() {
        return false;
    }
    
    protected void render(FunctionRenderContext context, WindowFunction windowFunction) {
        renderFunction(context, windowFunction);
        renderWithinGroup(context, windowFunction.getWithinGroup());
        renderFilterExpressions(context, windowFunction.getFilterExpressions());

        if (requiresOver() || !windowFunction.getPartitionExpressions().isEmpty() || !windowFunction.getOrderBys().isEmpty() || windowFunction.getFrameMode() != null) {
            context.addChunk(" OVER (");
            renderPartitions(context, windowFunction.getPartitionExpressions());
            if (!windowFunction.getOrderBys().isEmpty() && !windowFunction.getPartitionExpressions().isEmpty()) {
                context.addChunk(" ");
            }
            renderOrderBy(context, windowFunction.getOrderBys());
            if (windowFunction.getFrameMode() != null && (!windowFunction.getOrderBys().isEmpty() || !windowFunction.getPartitionExpressions().isEmpty())) {
                context.addChunk(" ");
            }
            renderFrame(context, windowFunction);
            context.addChunk(")");
        }
    }

    protected void renderFunction(FunctionRenderContext context, WindowFunction windowFunction) {
        context.addChunk(windowFunction.getFunctionName());
        context.addChunk("(");

        renderArguments(context, windowFunction);

        context.addChunk(")");
    }

    protected void renderArguments(FunctionRenderContext context, WindowFunction windowFunction) {
        List<String> arguments = windowFunction.getArguments();
        int size = arguments.size();
        if (size != 0) {
            List<String> filterExpressions = windowFunction.getFilterExpressions();
            int filtersSize = filterExpressions.size();
            if (supportsFilterClause || filtersSize == 0) {
                renderArgument(context, windowFunction, null, null, arguments.get(0), 0);
                for (int i = 1; i < size; i++) {
                    context.addChunk(", ");
                    renderArgument(context, windowFunction, null, null, arguments.get(i), i);
                }
            } else {
                String caseWhenPre = getCaseWhenPre(filterExpressions);
                String caseWhenPost = getCaseWhenPost();
                renderArgument(context, windowFunction, caseWhenPre, caseWhenPost, arguments.get(0), 0);
                for (int i = 1; i < size; i++) {
                    context.addChunk(", ");
                    renderArgument(context, windowFunction, caseWhenPre, caseWhenPost, arguments.get(i), i);
                }
            }
        }
    }

    protected static String getCaseWhenPre(List<String> filterExpressions) {
        int size = filterExpressions.size();
        StringBuilder sb = new StringBuilder();
        sb.append("CASE WHEN ");
        sb.append(filterExpressions.get(0));
        for (int i = 1; i < size; i++) {
            sb.append(" AND ");
            sb.append(filterExpressions.get(i));
        }
        sb.append(" THEN ");
        return sb.toString();
    }

    protected static String getCaseWhenPost() {
        return " ELSE NULL END";
    }

    protected void renderArgument(FunctionRenderContext context, WindowFunction windowFunction, String caseWhenPre, String caseWhenPost, String argument, int argumentIndex) {
        // Only the first argument will receive the CASE WHEN wrapper
        if (caseWhenPre == null || argumentIndex != 0) {
            context.addChunk(argument);
        } else {
            context.addChunk(caseWhenPre);
            context.addChunk(argument);
            context.addChunk(caseWhenPost);
        }
    }

    protected void renderWithinGroup(FunctionRenderContext context, List<Order> withinGroup) {
        int size = withinGroup.size();
        if (size != 0) {
            context.addChunk(" WITHIN GROUP (ORDER BY ");
            renderOrder(context, withinGroup.get(0));
            for (int i = 1; i < size; i++) {
                context.addChunk(" AND ");
                renderOrder(context, withinGroup.get(i));
            }
            context.addChunk(")");
        }
    }

    protected void renderFilterExpressions(FunctionRenderContext context, List<String> filterExpressions) {
        int size = filterExpressions.size();
        if (size != 0 && supportsFilterClause) {
            context.addChunk(" FILTER (WHERE ");
            context.addChunk(filterExpressions.get(0));
            for (int i = 1; i < size; i++) {
                context.addChunk(" AND ");
                context.addChunk(filterExpressions.get(i));
            }
            context.addChunk(")");
        }
    }

    protected void renderPartitions(FunctionRenderContext context, List<String> partitionExpressions) {
        int size = partitionExpressions.size();
        if (size != 0) {
            context.addChunk("PARTITION BY ");
            context.addChunk(partitionExpressions.get(0));
            for (int i = 1; i < size; i++) {
                context.addChunk(", ");
                context.addChunk(partitionExpressions.get(i));
            }
        }
    }

    protected void renderOrderBy(FunctionRenderContext context, List<Order> orderBys) {
        int size = orderBys.size();
        if (size != 0) {
            context.addChunk("ORDER BY ");
            renderOrder(context, orderBys.get(0));
            for (int i = 1; i < size; i++) {
                context.addChunk(", ");
                renderOrder(context, orderBys.get(i));
            }
        }
    }

    protected void renderOrder(FunctionRenderContext context, Order order) {
        if (supportsNullPrecedence) {
            context.addChunk(order.getExpression());
            context.addChunk(" ");
            context.addChunk(order.isAscending() ? "ASC " : "DESC ");
            context.addChunk(order.isNullsFirst() ? "NULLS FIRST" : "NULLS LAST");
        } else {
            appendEmulatedOrderByElementWithNulls(context, order);
        }
    }

    protected void renderFrame(FunctionRenderContext context, WindowFunction windowFunction) {
        if (windowFunction.getFrameMode() != null) {
            context.addChunk(windowFunction.getFrameMode().toString());
            context.addChunk(" ");

            if (windowFunction.getFrameEndType() == null) {
                renderFramePosition(context, windowFunction.getFrameStartType(), windowFunction.getFrameStartExpression());
            } else {
                context.addChunk("BETWEEN ");
                renderFramePosition(context, windowFunction.getFrameStartType(), windowFunction.getFrameStartExpression());
                context.addChunk("AND ");
                renderFramePosition(context, windowFunction.getFrameEndType(), windowFunction.getFrameEndExpression());
            }

            if (windowFunction.getFrameExclusionType() != null) {
                switch (windowFunction.getFrameExclusionType()) {
                    case EXCLUDE_CURRENT_ROW:
                        context.addChunk("EXCLUDE CURRENT ROW");
                        break;
                    case EXCLUDE_GROUP:
                        context.addChunk("EXCLUDE GROUP");
                        break;
                    case EXCLUDE_NO_OTHERS:
                        context.addChunk("EXCLUDE NO OTHERS");
                        break;
                    case EXCLUDE_TIES:
                        context.addChunk("EXCLUDE TIES");
                        break;
                    default:
                        throw new IllegalArgumentException("No branch for " + windowFunction.getFrameExclusionType());
                }
            }
        }
    }

    protected void renderFramePosition(FunctionRenderContext context, WindowFramePositionType type, String frameExpression) {
        switch (type) {
            case UNBOUNDED_PRECEDING:
                context.addChunk("UNBOUNDED PRECEDING");
                break;
            case BOUNDED_PRECEDING:
                context.addChunk(frameExpression);
                context.addChunk(" PRECEDING");
                break;
            case CURRENT_ROW:
                context.addChunk("CURRENT ROW");
                break;
            case UNBOUNDED_FOLLOWING:
                context.addChunk("UNBOUNDED FOLLOWING");
                break;
            case BOUNDED_FOLLOWING:
                context.addChunk(frameExpression);
                context.addChunk(" FOLLOWING");
                break;
            default:
                throw new IllegalArgumentException("No branch for " + type);
        }
        context.addChunk(" ");
    }

    protected boolean optimizeNullPrecedence() {
        return true;
    }

    protected void appendEmulatedOrderByElementWithNulls(FunctionRenderContext context, Order element) {
        boolean optimizeNullPrecedence = optimizeNullPrecedence();
        if (optimizeNullPrecedence && nullIsSmallest && element.isAscending() == element.isNullsFirst()) {
            // Since null is the smallest, we don't need to apply the nulls emulation when we want ASC NULLS FIRST or DESC NULLS LAST
        } else if (optimizeNullPrecedence && !nullIsSmallest && element.isAscending() != element.isNullsFirst()) {
            // Since null is the highest, we don't need to apply the nulls emulation when we want ASC NULLS LAST or DESC NULLS FIRST
        } else if (element.isNullsFirst()) {
            context.addChunk("case when ");
            context.addChunk(element.getExpression());
            context.addChunk(" is null then 0 else 1 end, ");
        } else {
            context.addChunk("case when ");
            context.addChunk(element.getExpression());
            context.addChunk(" is null then 1 else 0 end, ");
        }
        context.addChunk(element.getExpression());
        context.addChunk(element.isAscending() ? " asc" : " desc");
    }


    /**
     *
     * @author Jan-Willem Gmelig Meyling
     * @author Sayra Ranjha
     * @since 1.4.0
     */
    private enum Mode {
        ARGUMENTS,
        WITHIN_GROUP,
        FILTER,
        PARTITION_BY,
        ORDER_BY,
        FRAME_CLAUSE
    }
    
    /**
     *
     * @author Jan-Willem Gmelig Meyling
     * @author Sayra Ranjha
     * @since 1.4.0
     */
    protected static class WindowFunction {

        private final String functionName;
        private final List<String> arguments = new ArrayList<>();
        private final List<Order> withinGroup = new ArrayList<>();
        private final List<String> filterExpressions = new ArrayList<>();
        private final List<String> partitionExpressions = new ArrayList<>();
        private final List<Order> orderBys = new ArrayList<>();
        private boolean distinct;
        private WindowFrameMode frameMode;
        // The default frame start is UNBOUNDED_PRECEDING
        private WindowFramePositionType frameStartType;
        private String frameStartExpression;
        // The default frame end is CURRENT_ROW
        private WindowFramePositionType frameEndType;
        private String frameEndExpression;
        private WindowFrameExclusionType frameExclusionType;

        public WindowFunction(String functionName) {
            this.functionName = functionName;
        }

        public String getFunctionName() {
            return functionName;
        }

        public boolean isDistinct() {
            return distinct;
        }

        public List<String> getArguments() {
            return arguments;
        }

        public List<Order> getWithinGroup() {
            return withinGroup;
        }

        public List<String> getFilterExpressions() {
            return filterExpressions;
        }

        public List<String> getPartitionExpressions() {
            return partitionExpressions;
        }

        public List<Order> getOrderBys() {
            return orderBys;
        }

        public WindowFrameMode getFrameMode() {
            return frameMode;
        }

        public void setFrameMode(WindowFrameMode frameMode) {
            this.frameMode = frameMode;
        }

        public WindowFramePositionType getFrameStartType() {
            return frameStartType;
        }

        public void setFrameStartType(WindowFramePositionType frameStartType) {
            this.frameStartType = frameStartType;
        }

        public String getFrameStartExpression() {
            return frameStartExpression;
        }

        public void setFrameStartExpression(String frameStartExpression) {
            this.frameStartExpression = frameStartExpression;
        }

        public WindowFramePositionType getFrameEndType() {
            return frameEndType;
        }

        public void setFrameEndType(WindowFramePositionType frameEndType) {
            this.frameEndType = frameEndType;
        }

        public String getFrameEndExpression() {
            return frameEndExpression;
        }

        public void setFrameEndExpression(String frameEndExpression) {
            this.frameEndExpression = frameEndExpression;
        }

        public WindowFrameExclusionType getFrameExclusionType() {
            return frameExclusionType;
        }

        public void setFrameExclusionType(WindowFrameExclusionType frameExclusionType) {
            this.frameExclusionType = frameExclusionType;
        }
    }


}
