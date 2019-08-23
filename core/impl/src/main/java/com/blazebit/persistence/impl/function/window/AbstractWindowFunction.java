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
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Jan-Willem Gmelig Meyling
 * @author Sayra Ranjha
 * @since 1.4.0
 */
public abstract class AbstractWindowFunction implements JpqlFunction {

    protected final DbmsDialect dbmsDialect;

    protected AbstractWindowFunction(DbmsDialect dbmsDialect) {
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
        WindowFunction windowFunction = new WindowFunction();
        windowFunction.functionName = getFunctionName();
        Mode mode = Mode.ARGUMENTS;
        FramePosition currentFrame = null;

        for (int parameterIndex = 0; parameterIndex < context.getArgumentsSize(); parameterIndex++) {
            String argument = context.getArgument(parameterIndex);

            switch (argument.toUpperCase()) {
                case "'PARTITION BY'":
                    mode = Mode.PARTITION_BY;
                    break;
                case "'ORDER BY'":
                    mode = Mode.ORDER_BY;
                    break;
                case "'RANGE'":
                    mode = Mode.FRAME_CLAUSE;
                    windowFunction.window.frame = new Frame();
                    windowFunction.window.frame.mode = FrameMode.RANGE;
                    currentFrame = windowFunction.window.frame.start = new FramePosition();
                    break;
                case "'ROWS'":
                    mode = Mode.FRAME_CLAUSE;
                    windowFunction.window.frame = new Frame();
                    windowFunction.window.frame.mode = FrameMode.ROWS;
                    currentFrame = windowFunction.window.frame.start = new FramePosition();
                    break;
                default:
                    switch (mode) {
                        case ARGUMENTS:
                            windowFunction.arguments.add(argument);
                            break;
                        case PARTITION_BY:
                            windowFunction.window.partition.expressions.add(argument);
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

                            windowFunction.window.orderBys.add(order);
                            break;
                        case FRAME_CLAUSE:
                            if (currentFrame == null) {
                                throw new IllegalArgumentException("Illegal frame clause, expected to see 'RANGE' first.");
                            }
                            switch (argument) {
                                case "'BETWEEN'":
                                    windowFunction.window.frame.end = new FramePosition();
                                    break;
                                case "'AND'":
                                    currentFrame = windowFunction.window.frame.end;
                                    break;
                                case "'UNBOUNDED PRECEDING'":
                                    currentFrame.setType(FramePositionType.UNBOUNDED_PRECEDING);
                                    break;
                                case "'PRECEDING'":
                                    currentFrame.setType(FramePositionType.BOUNDED_PRECEDING);
                                    break;
                                case "'CURRENT ROW'":
                                    currentFrame.setType(FramePositionType.CURRENT_ROW);
                                    break;
                                case "'FOLLOWING'":
                                    currentFrame.setType(FramePositionType.BOUNDED_FOLLOWING);
                                    break;
                                case "'UNBOUNDED FOLLOWING'":
                                    currentFrame.setType(FramePositionType.UNBOUNDED_FOLLOWING);
                                    break;
                                default:
                                    currentFrame.setExpression(argument);
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

    protected abstract String getFunctionName();


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

        List<String> filterExpressions = windowFunction.getFilterExpressions();
        if (filterExpressions != null && !filterExpressions.isEmpty()) {
            renderFilterExpressions(context, filterExpressions);
        }

        context.addChunk(" OVER (");

        WindowDefinition window = windowFunction.getWindow();

        Partition partition = window.getPartition();
        if (partition != null && partition.getExpressions() != null && ! partition.getExpressions().isEmpty()) {
            render(context, partition);
        }

        List<Order> orderBys = window.getOrderBys();
        if (orderBys != null && ! orderBys.isEmpty()) {
            render(context, orderBys);
        }

        Frame frame = window.getFrame();
        if (frame != null) {
            render(context, frame);

        }

        context.addChunk(")");
    }

    protected void renderFunction(FunctionRenderContext context, WindowFunction windowFunction) {
        context.addChunk(windowFunction.getFunctionName());
        context.addChunk("(");

        renderArguments(context, windowFunction);

        context.addChunk(")");
    }

    protected void renderArguments(FunctionRenderContext context, WindowFunction windowFunction) {
        Iterator<String> argumentIterator = windowFunction.getArguments().iterator();
        while (argumentIterator.hasNext()) {
            context.addChunk(argumentIterator.next());
            if (argumentIterator.hasNext()) {
                context.addChunk(", ");
            }
        }
    }

    protected void renderFilterExpressions(FunctionRenderContext context, List<String> filterExpressions) {
        context.addChunk("FILTER ( WHERE ");
        Iterator<String> filterExpressionIterator = filterExpressions.iterator();
        while (filterExpressionIterator.hasNext()) {
            context.addChunk(filterExpressionIterator.next());
            if (filterExpressionIterator.hasNext()) {
                context.addChunk(", ");
            }
        }

        context.addChunk(") ");
    }

    protected void render(FunctionRenderContext context, Partition partition) {
        context.addChunk("PARTITION BY ");
        Iterator<String> partitionExpressionIterator = partition.getExpressions().iterator();
        while (partitionExpressionIterator.hasNext()) {
            context.addChunk(partitionExpressionIterator.next());
            if (partitionExpressionIterator.hasNext()) {
                context.addChunk(", ");
            }
        }

        context.addChunk(" ");
    }

    protected void render(FunctionRenderContext context, List<Order> orderBys) {
        context.addChunk("ORDER BY ");

        Iterator<Order> orderByIterator = orderBys.iterator();
        while (orderByIterator.hasNext()) {
            Order order = orderByIterator.next();
            render(context, order);
            if (orderByIterator.hasNext()) {
                context.addChunk(", ");
            }
        }

        context.addChunk(" ");
    }

    protected void render(FunctionRenderContext context, Order order) {
        if (dbmsDialect.supportsNullPrecedence()) {
            context.addChunk(order.getExpression());
            context.addChunk(" ");
            context.addChunk(order.isAscending() ? "ASC " : "DESC ");
            context.addChunk(order.isNullsFirst() ? "NULLS FIRST" : "NULLS LAST");
        } else {
            appendEmulatedOrderByElementWithNulls(context, order);
        }
    }

    protected void render(FunctionRenderContext context, Frame frame) {
        context.addChunk(frame.getMode().toString());
        context.addChunk(" ");

        if (frame.getEnd() == null) {
            render(context, frame.getStart());
        } else {
            context.addChunk("BETWEEN ");
            render(context, frame.getStart());
            context.addChunk("AND ");
            render(context, frame.getEnd());
        }
    }

    protected void render(FunctionRenderContext context, FramePosition position) {
        switch (position.getType()) {
            case UNBOUNDED_PRECEDING:
                context.addChunk("UNBOUNDED PRECEDING");
                break;
            case BOUNDED_PRECEDING:
                context.addChunk(position.getExpression());
                context.addChunk(" PRECEDING");
                break;
            case CURRENT_ROW:
                context.addChunk("CURRENT ROW");
                break;
            case UNBOUNDED_FOLLOWING:
                context.addChunk("UNBOUNDED FOLLOWING");
                break;
            case BOUNDED_FOLLOWING:
                context.addChunk(position.getExpression());
                context.addChunk(" FOLLOWING");
                break;
            default:
                throw new IllegalArgumentException("No branch for " + position.getType());
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

        private String functionName;

        private final List<String> arguments = new ArrayList<>();
        
        private final List<String> filterExpressions = new ArrayList<>();

        private final WindowDefinition window = new WindowDefinition();

        public String getFunctionName() {
            return functionName;
        }

        public List<String> getArguments() {
            return arguments;
        }

        public List<String> getFilterExpressions() {
            return filterExpressions;
        }

        public WindowDefinition getWindow() {
            return window;
        }
    }

    /**
     *
     * @author Jan-Willem Gmelig Meyling
     * @author Sayra Ranjha
     * @since 1.4.0
     */
    protected static final class WindowDefinition {

        private Partition partition = new Partition();

        private List<Order> orderBys  = new ArrayList<>();

        private Frame frame;

        public Partition getPartition() {
            return partition;
        }

        public void setPartition(Partition partition) {
            this.partition = partition;
        }

        public List<Order> getOrderBys() {
            return orderBys;
        }

        public void setOrderBys(List<Order> orderBys) {
            this.orderBys = orderBys;
        }

        public Frame getFrame() {
            return frame;
        }

        public void setFrame(Frame frame) {
            this.frame = frame;
        }
    }

    /**
     *
     * @author Jan-Willem Gmelig Meyling
     * @author Sayra Ranjha
     * @since 1.4.0
     */
    protected static final class Partition {

        private List<String> expressions = new ArrayList<>();

        public List<String> getExpressions() {
            return expressions;
        }
    }

    /**
     *
     * @author Jan-Willem Gmelig Meyling
     * @author Sayra Ranjha
     * @since 1.4.0
     */
    protected static final class Frame {
        
        private FrameMode mode;
        
        private FramePosition start;
        
        private FramePosition end;

        public FrameMode getMode() {
            return mode;
        }

        public void setMode(FrameMode mode) {
            this.mode = mode;
        }

        public FramePosition getStart() {
            return start;
        }

        public void setStart(FramePosition start) {
            this.start = start;
        }

        public FramePosition getEnd() {
            return end;
        }

        public void setEnd(FramePosition end) {
            this.end = end;
        }
    }

    /**
     *
     * @author Jan-Willem Gmelig Meyling
     * @author Sayra Ranjha
     * @since 1.4.0
     */
    protected static final class FramePosition {

        private FramePositionType type;
        private String expression;

        public FramePositionType getType() {
            return type;
        }

        public void setType(FramePositionType type) {
            this.type = type;
        }

        public String getExpression() {
            return expression;
        }

        public void setExpression(String expression) {
            this.expression = expression;
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

        private String expression;
        private boolean ascending;
        private boolean nullsFirst;

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
