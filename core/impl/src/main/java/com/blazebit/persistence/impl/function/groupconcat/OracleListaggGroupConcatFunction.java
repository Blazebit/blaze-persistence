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
public class OracleListaggGroupConcatFunction extends AbstractGroupConcatFunction {

    public OracleListaggGroupConcatFunction() {
        super("listagg(?1)");
    }

    @Override
    public void render(FunctionRenderContext context, GroupConcat groupConcat) {
        StringBuilder sb = new StringBuilder();

        if (groupConcat.isDistinct()) {
            sb.append("distinct ");
        }

        sb.append(groupConcat.getExpression());
        sb.append(", ");
        TypeUtils.STRING_CONVERTER.appendTo(groupConcat.getSeparator(), sb);

        List<Order> orderBys = groupConcat.getOrderBys();
        sb.append(") within group (order by ");
        if (orderBys.size() == 0) {
            sb.append("1");
        } else {
            render(sb, orderBys.get(0));
            
            for (int i = 1; i < orderBys.size(); i++) {
                sb.append(", ");
                render(sb, orderBys.get(i));
            }
        }

        renderer.start(context).addParameter(sb.toString()).build();
    }
}
