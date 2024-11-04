/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.transform;


import com.blazebit.persistence.impl.ClauseType;
import com.blazebit.persistence.impl.OrderByManager;
import com.blazebit.persistence.impl.SelectInfo;
import com.blazebit.persistence.parser.util.ExpressionUtils;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.2.0
 */
public class SizeSelectInfoTransformer implements ExpressionModifierVisitor<SelectInfo> {

    private final OrderByManager orderByManager;
    private final SizeTransformationVisitor sizeTransformationVisitor;

    public SizeSelectInfoTransformer(SizeTransformationVisitor sizeTransformationVisitor, OrderByManager orderByManager) {
        this.sizeTransformationVisitor = sizeTransformationVisitor;
        this.orderByManager = orderByManager;
    }

    @Override
    public void visit(SelectInfo info, ClauseType clauseType) {
        sizeTransformationVisitor.setClause(clauseType);
        sizeTransformationVisitor.setOrderBySelectClause(orderByManager.containsOrderBySelectAlias(info.getAlias()));
        if (ExpressionUtils.isSizeFunction(info.getExpression())) {
            sizeTransformationVisitor.visit(info);
        } else {
            info.getExpression().accept(sizeTransformationVisitor);
        }
    }

}
