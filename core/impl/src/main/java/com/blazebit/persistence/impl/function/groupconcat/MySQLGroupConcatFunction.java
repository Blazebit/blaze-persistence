/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.groupconcat;

import com.blazebit.persistence.impl.function.Order;
import com.blazebit.persistence.parser.util.TypeUtils;
import com.blazebit.persistence.spi.FunctionRenderContext;

import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class MySQLGroupConcatFunction extends AbstractGroupConcatFunction {

    public MySQLGroupConcatFunction() {
        super("group_concat(?1)");
    }

    @Override
    public void render(FunctionRenderContext context, GroupConcat groupConcat) {
        StringBuilder sb = new StringBuilder();

        if (groupConcat.isDistinct()) {
            sb.append("distinct ");
        }

        sb.append(groupConcat.getExpression());

        List<Order> orderBys = groupConcat.getOrderBys();
        if (!orderBys.isEmpty()) {
            sb.append(" order by ");
            
            render(sb, orderBys.get(0));
            
            for (int i = 1; i < orderBys.size(); i++) {
                sb.append(", ");
                render(sb, orderBys.get(i));
            }
        }

        sb.append(" separator ");
        TypeUtils.STRING_CONVERTER.appendTo(groupConcat.getSeparator(), sb);

        renderer.start(context).addParameter(sb.toString()).build();
    }

    @Override
    protected void render(StringBuilder sb, Order order) {
        // MySQL does not support the nulls clause
        appendEmulatedOrderByElementWithNulls(sb, order);
    }
}
