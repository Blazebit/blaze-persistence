package com.blazebit.persistence.impl;

import com.blazebit.persistence.impl.expression.AbortableVisitorAdapter;
import com.blazebit.persistence.impl.expression.AggregateExpression;
import com.blazebit.persistence.impl.expression.FunctionExpression;
import com.blazebit.persistence.impl.expression.SubqueryExpression;

class GroupByUsableDetectionVisitor extends AbortableVisitorAdapter {
	
	private final boolean treatSizeAsAggreagte;
	
	public GroupByUsableDetectionVisitor(boolean treatSizeAsAggreagte) {
		this.treatSizeAsAggreagte = treatSizeAsAggreagte;
	}

	@Override
    public Boolean visit(FunctionExpression expression) {
        if (expression instanceof AggregateExpression || (treatSizeAsAggreagte && ExpressionUtils.isSizeFunction(expression))) {
            return true;
        }
        return super.visit(expression);
    }

    @Override
    public Boolean visit(SubqueryExpression expression) {
        return true;
    }
//    
//    @Override
//    public Boolean visit(FunctionExpression expression) {
//        if (expression instanceof AggregateExpression) {
//            return false;
//        } else {
//        	return true;
//        }
//    }
//
//    @Override
//    public Boolean visit(SubqueryExpression expression) {
//        return false;
//    }
//
//	@Override
//	public Boolean visit(PathExpression expression) {
//		return true;
//	}
//
//	@Override
//	public Boolean visit(PropertyExpression expression) {
//		return true;
//	}
//
//	@Override
//	public Boolean visit(ParameterExpression expression) {
//		return true;
//	}
//
//	@Override
//	public Boolean visit(ArrayExpression expression) {
//		return true;
//	}
//
//	@Override
//	public Boolean visit(CompositeExpression expression) {
//		for (Expression expr : expression.getExpressions()) {
//			if (!expr.accept(this)) {
//				return false;
//			}
//		}
//		return true;
//	}
//
//	@Override
//	public Boolean visit(LiteralExpression expression) {
//		return true;
//	}
//
//	@Override
//	public Boolean visit(NullExpression expression) {
//		return true;
//	}
//
//	@Override
//	public Boolean visit(FooExpression expression) {
//		return true;
//	}
//
//	@Override
//	public Boolean visit(WhenClauseExpression expression) {
//		return expression.getCondition().accept(this) && expression.getResult().accept(this);
//	}
//
//	@Override
//	public Boolean visit(GeneralCaseExpression expression) {
//		if (expression.getDefaultExpr().accept(this)) {
//			for (WhenClauseExpression whenClause : expression.getWhenClauses()) {
//				if (!whenClause.accept(this)) {
//					return false;
//				}
//			}
//			return true;
//		} else {
//			return false;
//		}
//	}
//
//	@Override
//	public Boolean visit(SimpleCaseExpression expression) {
//		return visit((GeneralCaseExpression) expression);
//	}
//
//	@Override
//	public Boolean visit(AndPredicate predicate) {
//		for (Predicate pred : predicate.getChildren()) {
//			if (!pred.accept(this)) {
//				return false;
//			}
//		}
//		return true;
//	}
//
//	@Override
//	public Boolean visit(OrPredicate predicate) {
//		for (Predicate pred : predicate.getChildren()) {
//			if (!pred.accept(this)) {
//				return false;
//			}
//		}
//		return true;
//	}
//
//	@Override
//	public Boolean visit(NotPredicate predicate) {
//		return predicate.getPredicate().accept(this);
//	}
//
//	@Override
//	public Boolean visit(EqPredicate predicate) {
//		return visit((BinaryExpressionPredicate) predicate);
//	}
//
//	@Override
//	public Boolean visit(IsNullPredicate predicate) {
//		return predicate.getExpression().accept(this);
//	}
//
//	@Override
//	public Boolean visit(IsEmptyPredicate predicate) {
//		return predicate.getExpression().accept(this);
//	}
//
//	@Override
//	public Boolean visit(MemberOfPredicate predicate) {
//		return visit((BinaryExpressionPredicate) predicate);
//	}
//
//	@Override
//	public Boolean visit(LikePredicate predicate) {
//		return visit((BinaryExpressionPredicate) predicate);
//	}
//
//	@Override
//	public Boolean visit(BetweenPredicate predicate) {
//		return predicate.getLeft().accept(this) && predicate.getStart().accept(this) && predicate.getEnd().accept(this);
//	}
//
//	@Override
//	public Boolean visit(InPredicate predicate) {
//		return visit((BinaryExpressionPredicate) predicate);
//	}
//
//	@Override
//	public Boolean visit(GtPredicate predicate) {
//		return visit((BinaryExpressionPredicate) predicate);
//	}
//
//	@Override
//	public Boolean visit(GePredicate predicate) {
//		return visit((BinaryExpressionPredicate) predicate);
//	}
//
//	@Override
//	public Boolean visit(LtPredicate predicate) {
//		return visit((BinaryExpressionPredicate) predicate);
//	}
//
//	@Override
//	public Boolean visit(LePredicate predicate) {
//		return visit((BinaryExpressionPredicate) predicate);
//	}
//
//	@Override
//	public Boolean visit(ExistsPredicate predicate) {
//		return predicate.getExpression().accept(this);
//	}
//	
//	private Boolean visit(BinaryExpressionPredicate binaryExpressionPredicate) {
//		return binaryExpressionPredicate.getLeft().accept(this) && binaryExpressionPredicate.getRight().accept(this);
//	}
    
    
}