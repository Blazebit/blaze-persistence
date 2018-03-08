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

package com.blazebit.persistence.impl.function.groupconcat;

import java.util.ArrayList;
import java.util.List;

import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;
import com.blazebit.persistence.spi.TemplateRenderer;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public abstract class AbstractGroupConcatFunction implements JpqlFunction {

    protected final TemplateRenderer renderer;

    public AbstractGroupConcatFunction(String template) {
        this.renderer = new TemplateRenderer(template);
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
        return String.class;
    }

    protected GroupConcat getGroupConcat(FunctionRenderContext context) {
        if (context.getArgumentsSize() == 0) {
            throw new RuntimeException("The group concat function needs at least one argument! args=" + context);
        }

        boolean distinct = false;
        String expression;
        int startIndex = 0;
        int argsSize = context.getArgumentsSize();
        String maybeDistinct = context.getArgument(0);

        if ("'DISTINCT'".equalsIgnoreCase(maybeDistinct)) {
            distinct = true;
            startIndex++;
        }

        if (startIndex >= argsSize) {
            throw new RuntimeException("The group concat function needs at least one expression to concatenate! args=" + context);
        }

        expression = context.getArgument(startIndex);

        String separator = null;
        String orderExpression = null;
        List<Order> orders = new ArrayList<Order>();
        Mode mode = null;

        for (int i = startIndex + 1; i < argsSize; i++) {
            String argument = context.getArgument(i);
            if ("'SEPARATOR'".equalsIgnoreCase(argument)) {
                mode = Mode.SEPARATOR;
            } else if ("'ORDER BY'".equalsIgnoreCase(argument)) {
                mode = Mode.ORDER_BY;
            } else {
                if (mode == Mode.ORDER_BY) {
                    Order order = getOrder(argument, orderExpression);
                    if (order != null) {
                        orders.add(order);
                        orderExpression = null;
                    } else {
                        if (orderExpression != null) {
                            orders.add(new Order(orderExpression, null, null));
                        }
                        
                        orderExpression = argument;
                    }
                } else if (mode == Mode.SEPARATOR) {
                    if (separator != null) {
                        throw new IllegalArgumentException("Illegal multiple separators for group concat '" + argument + "'. Expected 'ORDER BY'!");
                    }

                    separator = argument.substring(argument.indexOf('\'') + 1, argument.lastIndexOf('\''));
                } else {
                    throw new IllegalArgumentException("Illegal input for group concat '" + argument + "'. Expected 'SEPARATOR' or 'ORDER BY'!");
                }
            }
        }

        if (orderExpression != null) {
            orders.add(new Order(orderExpression, null, null));
        }

        if (separator == null) {
            separator = ",";
        }

        return new GroupConcat(distinct, expression, orders, separator);
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    private enum Mode {
        SEPARATOR,
        ORDER_BY
    }

    protected void render(StringBuilder sb, Order order) {
        sb.append(order.getExpression());
        
        if (order.isAscending()) {
            sb.append(" ASC");
        } else {
            sb.append(" DESC");
        }
        
        if (order.isNullsFirst()) {
            sb.append(" NULLS FIRST");
        } else {
            sb.append(" NULLS LAST");
        }
    }
    
    protected void appendQuoted(StringBuilder sb, String s) {
        sb.append('\'');
        
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            
            if (c == '\'') {
                sb.append('\'');
            }
            
            sb.append(c);
        }
        
        sb.append('\'');
    }
    
    protected void appendEmulatedOrderByElementWithNulls(StringBuilder sb, Order element) {
        sb.append("case when ");
        sb.append(element.getExpression());
        sb.append(" is null then ");
        sb.append(element.isNullsFirst() ? 0 : 1);
        sb.append(" else ");
        sb.append(element.isNullsFirst() ? 1 : 0);
        sb.append(" end, ");
        sb.append(element.getExpression());
        sb.append(element.isAscending() ? " asc" : " desc");
    }

    private static Order getOrder(String s, String expression) {
        if (expression == null) {
            return null;
        }
        
        String type = s.trim().toUpperCase();
        
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

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    protected static final class GroupConcat {

        private final boolean distinct;
        private final String expression;
        private final List<Order> orderBys;
        private final String separator;

        public GroupConcat(boolean distinct, String expression, List<Order> orderBys, String separator) {
            this.distinct = distinct;
            this.expression = expression;
            this.orderBys = orderBys;
            this.separator = separator;
        }

        public boolean isDistinct() {
            return distinct;
        }

        public String getExpression() {
            return expression;
        }

        public List<Order> getOrderBys() {
            return orderBys;
        }

        public String getSeparator() {
            return separator;
        }
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
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
