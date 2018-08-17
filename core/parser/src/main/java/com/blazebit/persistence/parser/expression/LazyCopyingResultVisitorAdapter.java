/*
 * Copyright 2014 - 2018 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blazebit.persistence.parser.expression;

import com.blazebit.persistence.parser.predicate.BetweenPredicate;
import com.blazebit.persistence.parser.predicate.BooleanLiteral;
import com.blazebit.persistence.parser.predicate.CompoundPredicate;
import com.blazebit.persistence.parser.predicate.EqPredicate;
import com.blazebit.persistence.parser.predicate.ExistsPredicate;
import com.blazebit.persistence.parser.predicate.GePredicate;
import com.blazebit.persistence.parser.predicate.GtPredicate;
import com.blazebit.persistence.parser.predicate.InPredicate;
import com.blazebit.persistence.parser.predicate.IsEmptyPredicate;
import com.blazebit.persistence.parser.predicate.IsNullPredicate;
import com.blazebit.persistence.parser.predicate.LePredicate;
import com.blazebit.persistence.parser.predicate.LikePredicate;
import com.blazebit.persistence.parser.predicate.LtPredicate;
import com.blazebit.persistence.parser.predicate.MemberOfPredicate;
import com.blazebit.persistence.parser.predicate.Predicate;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a visitor that can be used to copy an expression tree by returning a different expression than the original one.
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
public abstract class LazyCopyingResultVisitorAdapter implements Expression.ResultVisitor<Expression> {

    protected void onPathExpressionCopy(PathExpression expression) {
        // No-op
    }

    protected  <T extends Expression> List<T> visitExpressionList(List<T> expressions) {
        List<T> newExpressions = null;
        int size = expressions.size();
        for (int i = 0; i < size; i++) {
            T originalExpr = expressions.get(i);
            T newExpr = (T) originalExpr.accept(this);
            if (newExpressions == null) {
                if (originalExpr != newExpr) {
                    newExpressions = new ArrayList<>(expressions.size());
                    for (int j = 0; j < i; j++) {
                        newExpressions.add(expressions.get(j));
                    }
                }
            } else {
                newExpressions.add(newExpr);
            }
        }

        return newExpressions;
    }

    @Override
    public Expression visit(ArrayExpression expression) {
        Expression newBase = expression.getBase().accept(this);
        Expression newIndex = expression.getIndex().accept(this);
        if (expression.getBase() != newBase || expression.getIndex() != newIndex) {
            return new ArrayExpression((PropertyExpression) newBase, newIndex);
        }
        return expression;
    }

    @Override
    public Expression visit(TreatExpression expression) {
        Expression newExpression = expression.getExpression().accept(this);
        if (newExpression != expression.getExpression()) {
            return new TreatExpression(newExpression, expression.getType());
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
    public Expression visit(ListIndexExpression expression) {
        Expression newExpression = expression.getPath().accept(this);
        if (newExpression != expression.getPath()) {
            return new ListIndexExpression((PathExpression) newExpression);
        }
        return expression;
    }

    @Override
    public Expression visit(MapEntryExpression expression) {
        Expression newExpression = expression.getPath().accept(this);
        if (newExpression != expression.getPath()) {
            return new MapEntryExpression((PathExpression) newExpression);
        }
        return expression;
    }

    @Override
    public Expression visit(MapKeyExpression expression) {
        Expression newExpression = expression.getPath().accept(this);
        if (newExpression != expression.getPath()) {
            return new MapKeyExpression((PathExpression) newExpression);
        }
        return expression;
    }

    @Override
    public Expression visit(MapValueExpression expression) {
        Expression newExpression = expression.getPath().accept(this);
        if (newExpression != expression.getPath()) {
            return new MapValueExpression((PathExpression) newExpression);
        }
        return expression;
    }

    @Override
    public Expression visit(NullExpression expression) {
        return expression;
    }

    @Override
    public Expression visit(SubqueryExpression expression) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Expression visit(FunctionExpression expression) {
        List<Expression> newExpressions = visitExpressionList(expression.getExpressions());
        if (newExpressions != null) {
            return new FunctionExpression(expression.getFunctionName(), newExpressions);
        }
        return expression;
    }

    @Override
    public Expression visit(TypeFunctionExpression expression) {
        Expression originalExpr = expression.getExpressions().get(0);
        Expression newExpr = originalExpr.accept(this);
        if (originalExpr != newExpr) {
            return new TypeFunctionExpression(newExpr);
        }
        return expression;
    }

    @Override
    public Expression visit(TrimExpression expression) {
        Expression newTrimSource = expression.getTrimSource().accept(this);
        Expression newTrimCharacter = expression.getTrimCharacter().accept(this);
        if (expression.getTrimSource() != newTrimSource || expression.getTrimCharacter() != newTrimCharacter) {
            return new TrimExpression(expression.getTrimspec(), newTrimSource, newTrimCharacter);
        }
        return expression;
    }

    @Override
    public Expression visit(WhenClauseExpression expression) {
        Expression newCondition = expression.getCondition().accept(this);
        Expression newResult = expression.getResult().accept(this);
        if (expression.getCondition() != newCondition || expression.getResult() != newResult) {
            return new WhenClauseExpression(newCondition, newResult);
        }
        return expression;
    }

    @Override
    public Expression visit(GeneralCaseExpression expression) {
        List<WhenClauseExpression> newExpressions = visitExpressionList(expression.getWhenClauses());
        Expression newDefaultExpr = null;
        if (expression.getDefaultExpr() != null) {
            newDefaultExpr = expression.getDefaultExpr().accept(this);
        }

        if (newExpressions != null) {
            return new GeneralCaseExpression(newExpressions, newDefaultExpr);
        } else if (expression.getDefaultExpr() != newDefaultExpr) {
            return new GeneralCaseExpression(expression.getWhenClauses(), newDefaultExpr);
        }

        return expression;
    }

    @Override
    public Expression visit(SimpleCaseExpression expression) {
        Expression newCaseOperandExpr = expression.getCaseOperand().accept(this);
        List<WhenClauseExpression> newExpressions = visitExpressionList(expression.getWhenClauses());
        Expression newDefaultExpr = null;
        if (expression.getDefaultExpr() != null) {
            newDefaultExpr = expression.getDefaultExpr().accept(this);
        }

        if (newExpressions != null) {
            return new SimpleCaseExpression(newCaseOperandExpr, newExpressions, newDefaultExpr);
        } else if (expression.getCaseOperand() != newCaseOperandExpr || expression.getDefaultExpr() != newDefaultExpr) {
            return new SimpleCaseExpression(newCaseOperandExpr, expression.getWhenClauses(), newDefaultExpr);
        }

        return expression;
    }

    @Override
    public Expression visit(PathExpression expression) {
        List<PathElementExpression> newExpressions = visitExpressionList(expression.getExpressions());
        if (newExpressions != null) {
            PathExpression pathExpression = new PathExpression(newExpressions);
            onPathExpressionCopy(pathExpression);
            return pathExpression;
        }
        return expression;
    }

    @Override
    public Expression visit(ArithmeticExpression expression) {
        Expression newLeft = expression.getLeft().accept(this);
        Expression newRight = expression.getRight().accept(this);
        if (newLeft != expression.getLeft() || newRight != expression.getRight()) {
            return new ArithmeticExpression(newLeft, newRight, expression.getOp());
        }
        return expression;
    }

    @Override
    public Expression visit(ArithmeticFactor expression) {
        Expression newExpr = expression.getExpression().accept(this);
        if (newExpr != expression.getExpression()) {
            return new ArithmeticFactor(newExpr, expression.isInvertSignum());
        }
        return expression;
    }

    @Override
    public Expression visit(NumericLiteral expression) {
        return expression;
    }

    @Override
    public Expression visit(BooleanLiteral expression) {
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
    public Expression visit(CompoundPredicate predicate) {
        List<Predicate> newPredicates = visitExpressionList(predicate.getChildren());
        if (newPredicates != null) {
            return new CompoundPredicate(predicate.getOperator(), newPredicates, predicate.isNegated());
        }
        return predicate;
    }

    @Override
    public Expression visit(EqPredicate predicate) {
        Expression leftExpr = predicate.getLeft().accept(this);
        Expression rightExpr = predicate.getRight().accept(this);
        if (leftExpr != predicate.getLeft() || rightExpr != predicate.getRight()) {
            return new EqPredicate(leftExpr, rightExpr, predicate.getQuantifier(), predicate.isNegated());
        }
        return predicate;
    }

    @Override
    public Expression visit(IsNullPredicate predicate) {
        Expression newExpr = predicate.getExpression().accept(this);
        if (newExpr != predicate.getExpression()) {
            return new IsNullPredicate(newExpr, predicate.isNegated());
        }
        return predicate;
    }

    @Override
    public Expression visit(IsEmptyPredicate predicate) {
        Expression newExpr = predicate.getExpression().accept(this);
        if (newExpr != predicate.getExpression()) {
            return new IsEmptyPredicate(newExpr, predicate.isNegated());
        }
        return predicate;
    }

    @Override
    public Expression visit(MemberOfPredicate predicate) {
        Expression leftExpr = predicate.getLeft().accept(this);
        Expression rightExpr = predicate.getRight().accept(this);
        if (leftExpr != predicate.getLeft() || rightExpr != predicate.getRight()) {
            return new MemberOfPredicate(leftExpr, rightExpr, predicate.isNegated());
        }
        return predicate;
    }

    @Override
    public Expression visit(LikePredicate predicate) {
        Expression leftExpr = predicate.getLeft().accept(this);
        Expression rightExpr = predicate.getRight().accept(this);
        if (leftExpr != predicate.getLeft() || rightExpr != predicate.getRight()) {
            return new LikePredicate(leftExpr, rightExpr, predicate.isCaseSensitive(), predicate.getEscapeCharacter(), predicate.isNegated());
        }
        return predicate;
    }

    @Override
    public Expression visit(BetweenPredicate predicate) {
        Expression newLeftExpr = predicate.getLeft().accept(this);
        Expression newStartExpr = predicate.getStart().accept(this);
        Expression newEndExpr = predicate.getEnd().accept(this);

        if (newLeftExpr != predicate.getLeft() || newStartExpr != predicate.getStart() || newEndExpr != predicate.getEnd()) {
            return new BetweenPredicate(newLeftExpr, newStartExpr, newEndExpr, predicate.isNegated());
        }

        return predicate;
    }

    @Override
    public Expression visit(InPredicate predicate) {
        Expression newLeftExpr = predicate.getLeft().accept(this);
        List<Expression> newExpressions = visitExpressionList(predicate.getRight());
        if (newExpressions == null) {
            if (newLeftExpr != predicate.getLeft()) {
                return new InPredicate(predicate.isNegated(), newLeftExpr, predicate.getRight());
            }
        } else {
            return new InPredicate(predicate.isNegated(), newLeftExpr, newExpressions);
        }
        return predicate;
    }

    @Override
    public Expression visit(GtPredicate predicate) {
        Expression leftExpr = predicate.getLeft().accept(this);
        Expression rightExpr = predicate.getRight().accept(this);
        if (leftExpr != predicate.getLeft() || rightExpr != predicate.getRight()) {
            return new GtPredicate(leftExpr, rightExpr, predicate.getQuantifier(), predicate.isNegated());
        }
        return predicate;
    }

    @Override
    public Expression visit(GePredicate predicate) {
        Expression leftExpr = predicate.getLeft().accept(this);
        Expression rightExpr = predicate.getRight().accept(this);
        if (leftExpr != predicate.getLeft() || rightExpr != predicate.getRight()) {
            return new GePredicate(leftExpr, rightExpr, predicate.getQuantifier(), predicate.isNegated());
        }
        return predicate;
    }

    @Override
    public Expression visit(LtPredicate predicate) {
        Expression leftExpr = predicate.getLeft().accept(this);
        Expression rightExpr = predicate.getRight().accept(this);
        if (leftExpr != predicate.getLeft() || rightExpr != predicate.getRight()) {
            return new LtPredicate(leftExpr, rightExpr, predicate.getQuantifier(), predicate.isNegated());
        }
        return predicate;
    }

    @Override
    public Expression visit(LePredicate predicate) {
        Expression leftExpr = predicate.getLeft().accept(this);
        Expression rightExpr = predicate.getRight().accept(this);
        if (leftExpr != predicate.getLeft() || rightExpr != predicate.getRight()) {
            return new LePredicate(leftExpr, rightExpr, predicate.getQuantifier(), predicate.isNegated());
        }
        return predicate;
    }

    @Override
    public Expression visit(ExistsPredicate predicate) {
        Expression newExpr = predicate.getExpression().accept(this);
        if (newExpr != predicate.getExpression()) {
            return new ExistsPredicate(newExpr, predicate.isNegated());
        }
        return predicate;
    }

}
