package com.blazebit.persistence.impl.transform;

import com.blazebit.persistence.impl.ClauseType;
import com.blazebit.persistence.impl.SelectManager;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.modifier.ExpressionModifier;


public class SizeExpressionTransformer implements ExpressionTransformer {
    private final SizeTransformationVisitor sizeTransformationVisitor;
    private final SelectManager<?> selectManager;

    public SizeExpressionTransformer(SizeTransformationVisitor sizeTransformationVisitor, SelectManager<?> selectManager) {
        this.sizeTransformationVisitor = sizeTransformationVisitor;
        this.selectManager = selectManager;
    }

    @Override
    public Expression transform(ExpressionModifier<? extends Expression> parentModifier, Expression original, ClauseType fromClause, boolean joinRequired) {
        sizeTransformationVisitor.setClause(fromClause);
        sizeTransformationVisitor.setOrderBySelectClause(false);
        boolean[] groupBySelectStatus = selectManager.containsGroupBySelect(true);
        sizeTransformationVisitor.setHasGroupBySelects(groupBySelectStatus[0]);
        sizeTransformationVisitor.setHasComplexGroupBySelects(groupBySelectStatus[1]);
        sizeTransformationVisitor.setParentModifier((ExpressionModifier<Expression>) parentModifier);
        return original.accept(sizeTransformationVisitor);
    }

    @Override
    public Expression transform(Expression original, ClauseType fromClause, boolean joinRequired) {
        throw new UnsupportedOperationException("Not supported");
    }

}