package com.blazebit.persistence.impl;

import com.blazebit.persistence.impl.expression.AbortableVisitorAdapter;
import com.blazebit.persistence.impl.expression.AggregateExpression;
import com.blazebit.persistence.impl.expression.FunctionExpression;
import com.blazebit.persistence.impl.expression.SubqueryExpression;

/**
 * Returns false if expression is usable in groupBy, true otherwise
 */
class GroupByUsableDetectionVisitor extends AbortableVisitorAdapter {
	
	private final boolean treatSizeAsAggreagte;
	
	public GroupByUsableDetectionVisitor(boolean treatSizeAsAggreagte) {
		this.treatSizeAsAggreagte = treatSizeAsAggreagte;
	}

	@Override
    public Boolean visit(FunctionExpression expression) {
        if (expression instanceof AggregateExpression || (treatSizeAsAggreagte && com.blazebit.persistence.impl.util.ExpressionUtils.isSizeFunction(expression))) {
            return true;
        }
        return super.visit(expression);
    }

    @Override
    public Boolean visit(SubqueryExpression expression) {
        return true;
    }
    
}