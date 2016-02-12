package com.blazebit.persistence.impl;

import com.blazebit.persistence.impl.expression.Expression;


public class SizeExpressionTransformer implements ExpressionTransformer {
    private final SizeTransformationVisitor sizeTransformationVisitor;
    
    public SizeExpressionTransformer(SizeTransformationVisitor sizeTransformationVisitor) {
        this.sizeTransformationVisitor = sizeTransformationVisitor;
    }
    
    @Override
    public Expression transform(Expression original, ClauseType fromClause, boolean joinRequired) {
        // select clause is transformed separately
        if (fromClause != ClauseType.SELECT) {
            sizeTransformationVisitor.setClause(fromClause);
            sizeTransformationVisitor.setOrderBySelectClause(false);
            return original.accept(sizeTransformationVisitor);
        } else {
            return original;
        }
    }

}