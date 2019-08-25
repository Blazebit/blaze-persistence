/*
 * Copyright 2014 - 2019 Blazebit.
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

package com.blazebit.persistence.impl.function.window;

import com.blazebit.persistence.spi.DbmsDialect;
import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jan-Willem Gmelig Meyling
 * @author Sayra Ranjha
 * @since 1.4.0
 */
public abstract class AbstractWindowFunction implements JpqlFunction {

    protected final String functionName;
    protected final DbmsDialect dbmsDialect;

    protected AbstractWindowFunction(String functionName, DbmsDialect dbmsDialect) {
        this.functionName = functionName;
        this.dbmsDialect = dbmsDialect;
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
        WindowFunction windowFunction = new WindowFunction(functionName);
        Mode mode = Mode.ARGUMENTS;
        Boolean startFrame = null;

        for (int parameterIndex = 0; parameterIndex < context.getArgumentsSize(); parameterIndex++) {
            String argument = context.getArgument(parameterIndex);

            switch (argument.toUpperCase()) {
                case "'FILTER'":
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
                    windowFunction.frameMode = FrameMode.RANGE;
                    startFrame = true;
                    break;
                case "'ROWS'":
                    mode = Mode.FRAME_CLAUSE;
                    windowFunction.frameMode = FrameMode.ROWS;
                    startFrame = true;
                    break;
                default:
                    switch (mode) {
                        case ARGUMENTS:
                            windowFunction.arguments.add(argument);
                            break;
                        case FILTER:
                            windowFunction.filterExpressions.add(argument);
                            break;
                        case PARTITION_BY:
                            windowFunction.partitionExpressions.add(argument);
                            break;
                        case ORDER_BY:
                            Order order = null;

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
                                        windowFunction.frameStartType = FramePositionType.UNBOUNDED_PRECEDING;
                                    } else {
                                        windowFunction.frameEndType = FramePositionType.UNBOUNDED_PRECEDING;
                                    }
                                    break;
                                case "'PRECEDING'":
                                    if (startFrame) {
                                        windowFunction.frameStartType = FramePositionType.BOUNDED_PRECEDING;
                                    } else {
                                        windowFunction.frameEndType = FramePositionType.BOUNDED_PRECEDING;
                                    }
                                    break;
                                case "'CURRENT ROW'":
                                    if (startFrame) {
                                        windowFunction.frameStartType = FramePositionType.CURRENT_ROW;
                                    } else {
                                        windowFunction.frameEndType = FramePositionType.CURRENT_ROW;
                                    }
                                    break;
                                case "'FOLLOWING'":
                                    if (startFrame) {
                                        windowFunction.frameStartType = FramePositionType.BOUNDED_FOLLOWING;
                                    } else {
                                        windowFunction.frameEndType = FramePositionType.BOUNDED_FOLLOWING;
                                    }
                                    break;
                                case "'UNBOUNDED FOLLOWING'":
                                    if (startFrame) {
                                        windowFunction.frameStartType = FramePositionType.UNBOUNDED_FOLLOWING;
                                    } else {
                                        windowFunction.frameEndType = FramePositionType.UNBOUNDED_FOLLOWING;
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

        render(context, windowFunction);
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
    
    protected void render(FunctionRenderContext context, WindowFunction windowFunction) {
        renderFunction(context, windowFunction);
        renderFilterExpressions(context, windowFunction.getFilterExpressions());

        context.addChunk(" OVER (");
        renderPartitions(context, windowFunction.getPartitionExpressions());
        renderOrderBy(context, windowFunction.getOrderBys());
        renderFrame(context, windowFunction);
        context.addChunk(")");
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
            if (dbmsDialect.supportsFilterClause() || filtersSize == 0) {
                context.addChunk(arguments.get(0));
                for (int i = 1; i < size; i++) {
                    context.addChunk(", ");
                    context.addChunk(arguments.get(i));
                }
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("CASE WHEN ");
                sb.append(filterExpressions.get(0));
                for (int i = 1; i < filtersSize; i++) {
                    sb.append(" AND ");
                    sb.append(filterExpressions.get(i));
                }
                sb.append(" THEN ");
                String caseWhenPart = sb.toString();
                context.addChunk(caseWhenPart);
                context.addChunk(arguments.get(0));
                for (int i = 1; i < size; i++) {
                    context.addChunk(" ELSE NULL END, ");
                    context.addChunk(caseWhenPart);
                    context.addChunk(arguments.get(i));
                }
                context.addChunk(" ELSE NULL END");
            }
        }
    }

    protected void renderFilterExpressions(FunctionRenderContext context, List<String> filterExpressions) {
        int size = filterExpressions.size();
        if (size != 0 && dbmsDialect.supportsFilterClause()) {
            context.addChunk("FILTER ( WHERE ");
            context.addChunk(filterExpressions.get(0));
            for (int i = 1; i < size; i++) {
                context.addChunk(" AND ");
                context.addChunk(filterExpressions.get(i));
            }
            context.addChunk(") ");
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
            context.addChunk(" ");
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
            context.addChunk(" ");
        }
    }

    protected void renderOrder(FunctionRenderContext context, Order order) {
        if (dbmsDialect.supportsNullPrecedence()) {
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
        }
    }

    protected void renderFramePosition(FunctionRenderContext context, FramePositionType type, String frameExpression) {
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

    protected void appendEmulatedOrderByElementWithNulls(FunctionRenderContext context, Order element) {
        context.addChunk("case when ");
        context.addChunk(element.getExpression());
        context.addChunk(" is null then ");
        context.addChunk(element.isNullsFirst() ? "0" : "1");
        context.addChunk(" else ");
        context.addChunk(element.isNullsFirst() ? "1" : "0");
        context.addChunk(" end, ");
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
    protected static final class WindowFunction {

        private final String functionName;
        private final List<String> arguments = new ArrayList<>();
        private final List<String> filterExpressions = new ArrayList<>();
        private final List<String> partitionExpressions = new ArrayList<>();
        private final List<Order> orderBys  = new ArrayList<>();
        private FrameMode frameMode;
        private FramePositionType frameStartType;
        private String frameStartExpression;
        private FramePositionType frameEndType;
        private String frameEndExpression;

        public WindowFunction(String functionName) {
            this.functionName = functionName;
        }

        public String getFunctionName() {
            return functionName;
        }

        public List<String> getArguments() {
            return arguments;
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

        public FrameMode getFrameMode() {
            return frameMode;
        }

        public void setFrameMode(FrameMode frameMode) {
            this.frameMode = frameMode;
        }

        public FramePositionType getFrameStartType() {
            return frameStartType;
        }

        public void setFrameStartType(FramePositionType frameStartType) {
            this.frameStartType = frameStartType;
        }

        public String getFrameStartExpression() {
            return frameStartExpression;
        }

        public void setFrameStartExpression(String frameStartExpression) {
            this.frameStartExpression = frameStartExpression;
        }

        public FramePositionType getFrameEndType() {
            return frameEndType;
        }

        public void setFrameEndType(FramePositionType frameEndType) {
            this.frameEndType = frameEndType;
        }

        public String getFrameEndExpression() {
            return frameEndExpression;
        }

        public void setFrameEndExpression(String frameEndExpression) {
            this.frameEndExpression = frameEndExpression;
        }
    }

    /**
     *
     * @author Jan-Willem Gmelig Meyling
     * @author Sayra Ranjha
     * @since 1.4.0
     */
    protected enum FramePositionType {
        UNBOUNDED_PRECEDING,
        BOUNDED_PRECEDING,
        CURRENT_ROW,
        UNBOUNDED_FOLLOWING,
        BOUNDED_FOLLOWING
    }

    /**
     *
     * @author Jan-Willem Gmelig Meyling
     * @author Sayra Ranjha
     * @since 1.4.0
     */
    protected enum FrameMode {
        RANGE, ROWS;
    }

    /**
     *
     * @author Jan-Willem Gmelig Meyling
     * @author Sayra Ranjha
     * @since 1.4.0
     */
    protected static final class Order {

        private final String expression;
        private final boolean ascending;
        private final boolean nullsFirst;

        public Order(String expression, Boolean ascending, Boolean nullsFirst) {
            this.expression = expression;

            if (Boolean.FALSE.equals(ascending)) {
                this.ascending = false;
                // Default NULLS FIRST
                if (nullsFirst == null) {
                    this.nullsFirst = true;
                } else {
                    this.nullsFirst = nullsFirst;
                }
            } else {
                this.ascending = true;
                // Default NULLS LAST
                if (nullsFirst == null) {
                    this.nullsFirst = false;
                } else {
                    this.nullsFirst = nullsFirst;
                }
            }
        }

        public String getExpression() {
            return expression;
        }

        public boolean isAscending() {
            return ascending;
        }

        public boolean isNullsFirst() {
            return nullsFirst;
        }
    }



}
