package com.blazebit.persistence.impl.expression;

import com.blazebit.persistence.impl.predicate.*;
import org.antlr.v4.runtime.atn.SemanticContext;

import java.util.List;

/**
 * Created
 * by Moritz Becker (moritz.becker@gmx.at)
 * on 08.08.2016.
 */
public class ExpressionOptimizer implements Expression.ResultVisitor<Expression> {

    @Override
    public Expression visit(PathExpression expression) {
        for (int i = 0; i < expression.getExpressions().size(); i++) {
            expression.getExpressions().set(i, (PathElementExpression) expression.getExpressions().get(i).accept(this));
        }
        return expression;
    }

    @Override
    public Expression visit(PropertyExpression expression) {
        return expression;
    }

    @Override
    public Expression visit(ParameterExpression expression) {
        return expression;
    }

    @Override
    public Expression visit(ArrayExpression expression) {
        return new ArrayExpression(expression.getBase(), expression.getIndex().accept(this));
    }

    @Override
    public Expression visit(NullExpression expression) {
        return expression;
    }

    @Override
    public Expression visit(FooExpression expression) {
        return expression;
    }

    @Override
    public Expression visit(SubqueryExpression expression) {
        return expression;
    }

    @Override
    public Expression visit(FunctionExpression expression) {
        for (int i = 0; i < expression.getExpressions().size(); i++) {
            expression.getExpressions().set(i, expression.getExpressions().get(i).accept(this));
        }
        return expression;
    }

    @Override
    public Expression visit(TypeFunctionExpression expression) {
        return visit((FunctionExpression) expression);
    }

    @Override
    public Expression visit(TrimExpression expression) {
        expression.getTrimSource().accept(this);
        return expression;
    }

    @Override
    public Expression visit(WhenClauseExpression expression) {
        return new WhenClauseExpression(expression.getCondition().accept(this), expression.getResult().accept(this));
    }

    @Override
    public Expression visit(GeneralCaseExpression expression) {
        for (int i = 0; i < expression.getWhenClauses().size(); i++) {
            expression.getWhenClauses().set(i, (WhenClauseExpression) expression.getWhenClauses().get(i).accept(this));
        }
        expression.setDefaultExpr(expression.getDefaultExpr().accept(this));
        return expression;
    }

    @Override
    public Expression visit(SimpleCaseExpression expression) {
        expression = (SimpleCaseExpression) visit((GeneralCaseExpression) expression);
        return new SimpleCaseExpression(expression.getCaseOperand().accept(this), expression.getWhenClauses(), expression.getDefaultExpr());
    }

    @Override
    public Expression visit(ArithmeticExpression expression) {
        expression.setLeft(expression.getLeft().accept(this));
        expression.setRight(expression.getRight().accept(this));
        return expression;
    }

    @Override
    public Expression visit(ArithmeticFactor expression) {
        if (expression.getExpression() instanceof ArithmeticFactor) {
            ArithmeticFactor subFactor = (ArithmeticFactor) expression.getExpression();
            boolean invert = expression.isInvertSignum();
            boolean subInvert = subFactor.isInvertSignum();
            if (invert && subInvert) {
                subFactor.setInvertSignum(false);
            } else if (invert && !subInvert) {
                subFactor.setInvertSignum(true);
            }
            return subFactor.accept(this);
        } else if (expression.getExpression() instanceof NumericLiteral && !expression.isInvertSignum()) {
            return expression.getExpression();
        } else {
            expression.setExpression(expression.getExpression().accept(this));
            return expression;
        }
    }

    @Override
    public Expression visit(NumericLiteral expression) {
        return expression;
    }

    @Override
    public Expression visit(StringLiteral expression) {
        return expression;
    }

    @Override
    public Expression visit(DateLiteral expression) {
        return expression;
    }

    @Override
    public Expression visit(TimeLiteral expression) {
        return expression;
    }

    @Override
    public Expression visit(TimestampLiteral expression) {
        return expression;
    }

    @Override
    public Expression visit(EnumLiteral expression) {
        return expression;
    }

    @Override
    public Expression visit(EntityLiteral expression) {
        return expression;
    }

    @Override
    public Expression visit(EqPredicate predicate) {
        visitBinaryExpressionPredicate(predicate);
        return predicate;
    }

    @Override
    public Expression visit(IsNullPredicate predicate) {
        visitUnaryExpressionPredicate(predicate);
        return predicate;
    }

    @Override
    public Expression visit(IsEmptyPredicate predicate) {
        visitUnaryExpressionPredicate(predicate);
        return predicate;
    }

    @Override
    public Expression visit(MemberOfPredicate predicate) {
        visitBinaryExpressionPredicate(predicate);
        return predicate;
    }

    @Override
    public Expression visit(LikePredicate predicate) {
        visitBinaryExpressionPredicate(predicate);
        return predicate;
    }

    @Override
    public Expression visit(BetweenPredicate predicate) {
        predicate.setLeft(predicate.getLeft().accept(this));
        predicate.setStart(predicate.getStart().accept(this));
        predicate.setEnd(predicate.getEnd().accept(this));
        return predicate;
    }

    @Override
    public Expression visit(InPredicate predicate) {
        predicate.setLeft(predicate.getLeft().accept(this));
        List<Expression> right = predicate.getRight();
        for (int i = 0; i < right.size(); i++) {
            right.set(i, right.get(i).accept(this));
        }
        return predicate;
    }

    @Override
    public Expression visit(GtPredicate predicate) {
        visitBinaryExpressionPredicate(predicate);
        return predicate;
    }

    @Override
    public Expression visit(GePredicate predicate) {
        visitBinaryExpressionPredicate(predicate);
        return predicate;
    }

    @Override
    public Expression visit(LtPredicate predicate) {
        visitBinaryExpressionPredicate(predicate);
        return predicate;
    }

    @Override
    public Expression visit(LePredicate predicate) {
        visitBinaryExpressionPredicate(predicate);
        return predicate;
    }

    @Override
    public Expression visit(ExistsPredicate predicate) {
        visitUnaryExpressionPredicate(predicate);
        return predicate;
    }

    @Override
    public Expression visit(BooleanLiteral predicate) {
        return predicate;
    }

    @Override
    public Expression visit(CompoundPredicate predicate) {
        if (predicate.getChildren().size() == 1) {
            Predicate subPredicate = predicate.getChildren().get(0);
            if (predicate.isNegated()) {
                subPredicate.negate();
            }
            return subPredicate.accept(this);
        } else {
            boolean negate = predicate.isNegated();
            if (negate) {
                predicate = new CompoundPredicate(predicate.getOperator().invert(), predicate.getChildren());
            }
            for (int i = 0; i < predicate.getChildren().size(); i++) {
                Predicate child = predicate.getChildren().get(i);
                if (negate) {
                    child.negate();
                }
                predicate.getChildren().set(i, (Predicate) child.accept(this));
            }
            return predicate;
        }
    }

    private void visitBinaryExpressionPredicate(BinaryExpressionPredicate predicate) {
        predicate.setLeft(predicate.getLeft().accept(this));
        predicate.setRight(predicate.getRight().accept(this));
    }

    private void visitUnaryExpressionPredicate(UnaryExpressionPredicate predicate) {
        predicate.setExpression(predicate.getExpression().accept(this));
    }
}
