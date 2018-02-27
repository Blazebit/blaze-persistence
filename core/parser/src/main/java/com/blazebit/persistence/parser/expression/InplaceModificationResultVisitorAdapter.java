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
import com.blazebit.persistence.parser.predicate.BinaryExpressionPredicate;
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

import java.util.List;

/**
 * This is a visitor that can be used to do inplace changes to an expression.
 * This is quite similar to {@link ExpressionModifierCollectingResultVisitorAdapter},
 * but more targeted for multiple possibly nested replacements.
 *
 * @author Moritz Becker
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class InplaceModificationResultVisitorAdapter implements Expression.ResultVisitor<Expression> {

    @Override
    public Expression visit(ArrayExpression expression) {
        expression.setBase((PropertyExpression) expression.getBase().accept(this));
        expression.setIndex(expression.getIndex().accept(this));
        return expression;
    }

    @Override
    public Expression visit(TreatExpression expression) {
        expression.setExpression(expression.getExpression().accept(this));
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
        expression.setPath((PathExpression) expression.getPath().accept(this));
        return expression;
    }

    @Override
    public Expression visit(MapEntryExpression expression) {
        expression.setPath((PathExpression) expression.getPath().accept(this));
        return expression;
    }

    @Override
    public Expression visit(MapKeyExpression expression) {
        expression.setPath((PathExpression) expression.getPath().accept(this));
        return expression;
    }

    @Override
    public Expression visit(MapValueExpression expression) {
        expression.setPath((PathExpression) expression.getPath().accept(this));
        return expression;
    }

    @Override
    public Expression visit(NullExpression expression) {
        return expression;
    }

    @Override
    public Expression visit(SubqueryExpression expression) {
        return expression;
    }

    @Override
    public Expression visit(FunctionExpression expression) {
        List<Expression> expressions = expression.getExpressions();
        int size = expressions.size();
        for (int i = 0; i < size; i++) {
            expressions.set(i, expressions.get(i).accept(this));
        }
        return expression;
    }

    @Override
    public Expression visit(TypeFunctionExpression expression) {
        return visit((FunctionExpression) expression);
    }

    @Override
    public Expression visit(TrimExpression expression) {
        expression.setTrimSource(expression.getTrimSource().accept(this));
        expression.setTrimCharacter(expression.getTrimCharacter().accept(this));
        return expression;
    }

    @Override
    public Expression visit(WhenClauseExpression expression) {
        expression.setCondition(expression.getCondition().accept(this));
        expression.setResult(expression.getResult().accept(this));
        return expression;
    }

    @Override
    public Expression visit(GeneralCaseExpression expression) {
        List<WhenClauseExpression> expressions = expression.getWhenClauses();
        int size = expressions.size();
        for (int i = 0; i < size; i++) {
            expressions.set(i, (WhenClauseExpression) expressions.get(i).accept(this));
        }
        if (expression.getDefaultExpr() != null) {
            expression.setDefaultExpr(expression.getDefaultExpr().accept(this));
        }
        return expression;
    }

    @Override
    public Expression visit(SimpleCaseExpression expression) {
        expression.setCaseOperand(expression.getCaseOperand().accept(this));
        return visit((GeneralCaseExpression) expression);
    }

    @Override
    public Expression visit(PathExpression expression) {
        List<PathElementExpression> expressions = expression.getExpressions();
        int size = expressions.size();
        for (int i = 0; i < size; i++) {
            expressions.set(i, (PathElementExpression) expressions.get(i).accept(this));
        }
        return expression;
    }

    @Override
    public Expression visit(ArithmeticExpression expression) {
        expression.setLeft(expression.getLeft().accept(this));
        expression.setRight(expression.getRight().accept(this));
        return expression;
    }

    @Override
    public Expression visit(ArithmeticFactor expression) {
        expression.setExpression(expression.getExpression().accept(this));
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
        List<Predicate> predicates = predicate.getChildren();
        int size = predicates.size();
        for (int i = 0; i < size; i++) {
            predicates.set(i, (Predicate) predicates.get(i).accept(this));
        }
        return predicate;
    }

    @Override
    public Expression visit(EqPredicate predicate) {
        return visit((BinaryExpressionPredicate) predicate);
    }

    @Override
    public Expression visit(IsNullPredicate predicate) {
        predicate.setExpression(predicate.getExpression().accept(this));
        return predicate;
    }

    @Override
    public Expression visit(IsEmptyPredicate predicate) {
        predicate.setExpression(predicate.getExpression().accept(this));
        return predicate;
    }

    @Override
    public Expression visit(MemberOfPredicate predicate) {
        return visit((BinaryExpressionPredicate) predicate);
    }

    @Override
    public Expression visit(LikePredicate predicate) {
        return visit((BinaryExpressionPredicate) predicate);
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

        List<Expression> expressions = predicate.getRight();
        int size = expressions.size();
        for (int i = 0; i < size; i++) {
            expressions.set(i, expressions.get(i).accept(this));
        }
        return predicate;
    }

    @Override
    public Expression visit(GtPredicate predicate) {
        return visit((BinaryExpressionPredicate) predicate);
    }

    @Override
    public Expression visit(GePredicate predicate) {
        return visit((BinaryExpressionPredicate) predicate);
    }

    @Override
    public Expression visit(LtPredicate predicate) {
        return visit((BinaryExpressionPredicate) predicate);
    }

    @Override
    public Expression visit(LePredicate predicate) {
        return visit((BinaryExpressionPredicate) predicate);
    }

    @Override
    public Expression visit(ExistsPredicate predicate) {
        predicate.setExpression(predicate.getExpression().accept(this));
        return predicate;
    }

    private BinaryExpressionPredicate visit(BinaryExpressionPredicate predicate) {
        predicate.setLeft(predicate.getLeft().accept(this));
        predicate.setRight(predicate.getRight().accept(this));
        return predicate;
    }

}
