package com.blazebit.persistence.impl;

import com.blazebit.persistence.impl.expression.AbortableVisitorAdapter;
import com.blazebit.persistence.impl.expression.AggregateExpression;
import com.blazebit.persistence.impl.expression.ArrayExpression;
import com.blazebit.persistence.impl.expression.CompositeExpression;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.FooExpression;
import com.blazebit.persistence.impl.expression.FunctionExpression;
import com.blazebit.persistence.impl.expression.GeneralCaseExpression;
import com.blazebit.persistence.impl.expression.LiteralExpression;
import com.blazebit.persistence.impl.expression.NullExpression;
import com.blazebit.persistence.impl.expression.ParameterExpression;
import com.blazebit.persistence.impl.expression.PathExpression;
import com.blazebit.persistence.impl.expression.PropertyExpression;
import com.blazebit.persistence.impl.expression.SimpleCaseExpression;
import com.blazebit.persistence.impl.expression.SubqueryExpression;
import com.blazebit.persistence.impl.expression.WhenClauseExpression;
import com.blazebit.persistence.impl.predicate.AndPredicate;
import com.blazebit.persistence.impl.predicate.BetweenPredicate;
import com.blazebit.persistence.impl.predicate.BinaryExpressionPredicate;
import com.blazebit.persistence.impl.predicate.EqPredicate;
import com.blazebit.persistence.impl.predicate.ExistsPredicate;
import com.blazebit.persistence.impl.predicate.GePredicate;
import com.blazebit.persistence.impl.predicate.GtPredicate;
import com.blazebit.persistence.impl.predicate.InPredicate;
import com.blazebit.persistence.impl.predicate.IsEmptyPredicate;
import com.blazebit.persistence.impl.predicate.IsNullPredicate;
import com.blazebit.persistence.impl.predicate.LePredicate;
import com.blazebit.persistence.impl.predicate.LikePredicate;
import com.blazebit.persistence.impl.predicate.LtPredicate;
import com.blazebit.persistence.impl.predicate.MemberOfPredicate;
import com.blazebit.persistence.impl.predicate.NotPredicate;
import com.blazebit.persistence.impl.predicate.OrPredicate;
import com.blazebit.persistence.impl.predicate.Predicate;

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