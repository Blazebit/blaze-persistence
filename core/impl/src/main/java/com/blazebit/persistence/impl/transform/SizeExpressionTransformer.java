/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.transform;

import com.blazebit.persistence.impl.ClauseType;
import com.blazebit.persistence.parser.expression.modifier.ExpressionModifier;


/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.2.0
 */
public class SizeExpressionTransformer implements ExpressionModifierVisitor<ExpressionModifier> {

    private final SizeTransformationVisitor sizeTransformationVisitor;

    public SizeExpressionTransformer(SizeTransformationVisitor sizeTransformationVisitor) {
        this.sizeTransformationVisitor = sizeTransformationVisitor;
    }

    @Override
    public void visit(ExpressionModifier expressionModifier, ClauseType clauseType) {
        sizeTransformationVisitor.setClause(clauseType);
        sizeTransformationVisitor.setOrderBySelectClause(false);
        sizeTransformationVisitor.visit(expressionModifier);
    }

}