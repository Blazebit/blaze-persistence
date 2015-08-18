package com.blazebit.persistence.impl;

import com.blazebit.persistence.impl.expression.AbortableVisitorAdapter;
import com.blazebit.persistence.impl.expression.AggregateExpression;
import com.blazebit.persistence.impl.expression.FunctionExpression;
import com.blazebit.persistence.impl.expression.SubqueryExpression;

class GroupByUsableDetectionVisitor extends AbortableVisitorAdapter {

    @Override
    public Boolean visit(FunctionExpression expression) {
        if (expression instanceof AggregateExpression) {
            return true;
        }
        return super.visit(expression);
    }

    @Override
    public Boolean visit(SubqueryExpression expression) {
        return true;
    }
}