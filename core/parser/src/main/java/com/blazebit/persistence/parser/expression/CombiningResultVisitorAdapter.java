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
 * @author Moritz Becker
 * @since 1.2.0
 */
public abstract class CombiningResultVisitorAdapter<T> implements Expression.ResultVisitor<T> {

    protected abstract T getDefaultResult();

    protected abstract T updateResult(T result, T update);

    @Override
    public T visit(PathExpression expression) {
        T result = getDefaultResult();
        List<PathElementExpression> expressions = expression.getExpressions();
        int size = expressions.size();
        for (int i = 0; i < size; i++) {
            result = updateResult(result, expressions.get(i).accept(this));
        }
        return result;
    }

    @Override
    public T visit(ArrayExpression expression) {
        T result = expression.getBase().accept(this);
        return updateResult(result, expression.getIndex().accept(this));
    }

    @Override
    public T visit(TreatExpression expression) {
        return expression.getExpression().accept(this);
    }

    @Override
    public T visit(PropertyExpression expression) {
        return getDefaultResult();
    }

    @Override
    public T visit(ParameterExpression expression) {
        return getDefaultResult();
    }

    @Override
    public T visit(ListIndexExpression expression) {
        return expression.getPath().accept(this);
    }

    @Override
    public T visit(MapEntryExpression expression) {
        return expression.getPath().accept(this);
    }

    @Override
    public T visit(MapKeyExpression expression) {
        return expression.getPath().accept(this);
    }

    @Override
    public T visit(MapValueExpression expression) {
        return expression.getPath().accept(this);
    }

    @Override
    public T visit(NullExpression expression) {
        return getDefaultResult();
    }

    @Override
    public T visit(SubqueryExpression expression) {
        return getDefaultResult();
    }

    @Override
    public T visit(FunctionExpression expression) {
        T result = getDefaultResult();
        List<Expression> expressions = expression.getExpressions();
        int size = expressions.size();
        for (int i = 0; i < size; i++) {
            result = updateResult(result, expressions.get(i).accept(this));
        }
        return result;
    }

    @Override
    public T visit(TypeFunctionExpression expression) {
        return visit((FunctionExpression) expression);
    }

    @Override
    public T visit(TrimExpression expression) {
        T result;
        if (expression.getTrimCharacter() == null) {
            result = getDefaultResult();
        } else {
            result = expression.getTrimCharacter().accept(this);
        }
        return updateResult(result, expression.getTrimSource().accept(this));
    }

    @Override
    public T visit(GeneralCaseExpression expression) {
        T result = getDefaultResult();
        List<WhenClauseExpression> expressions = expression.getWhenClauses();
        int size = expressions.size();
        for (int i = 0; i < size; i++) {
            result = updateResult(result, expressions.get(i).accept(this));
        }

        if (expression.getDefaultExpr() != null) {
            result = updateResult(result, expression.getDefaultExpr().accept(this));
        }

        return result;
    }

    @Override
    public T visit(SimpleCaseExpression expression) {
        T result = expression.getCaseOperand().accept(this);
        return updateResult(result, visit((GeneralCaseExpression) expression));
    }

    @Override
    public T visit(WhenClauseExpression expression) {
        T result = expression.getCondition().accept(this);
        return updateResult(result, expression.getResult().accept(this));
    }

    @Override
    public T visit(ArithmeticExpression expression) {
        T result = expression.getLeft().accept(this);
        return updateResult(result, expression.getRight().accept(this));
    }

    @Override
    public T visit(ArithmeticFactor expression) {
        return expression.getExpression().accept(this);
    }

    @Override
    public T visit(NumericLiteral expression) {
        return getDefaultResult();
    }

    @Override
    public T visit(BooleanLiteral expression) {
        return getDefaultResult();
    }

    @Override
    public T visit(StringLiteral expression) {
        return getDefaultResult();
    }

    @Override
    public T visit(DateLiteral expression) {
        return getDefaultResult();
    }

    @Override
    public T visit(TimeLiteral expression) {
        return getDefaultResult();
    }

    @Override
    public T visit(TimestampLiteral expression) {
        return getDefaultResult();
    }

    @Override
    public T visit(EnumLiteral expression) {
        return getDefaultResult();
    }

    @Override
    public T visit(EntityLiteral expression) {
        return getDefaultResult();
    }

    @Override
    public T visit(CompoundPredicate predicate) {
        T result = getDefaultResult();
        List<Predicate> children = predicate.getChildren();
        int size = children.size();
        for (int i = 0; i < size; i++) {
            result = updateResult(result, children.get(i).accept(this));
        }
        return result;
    }

    @Override
    public T visit(EqPredicate predicate) {
        return visit((BinaryExpressionPredicate) predicate);
    }

    @Override
    public T visit(IsNullPredicate predicate) {
        return predicate.getExpression().accept(this);
    }

    @Override
    public T visit(IsEmptyPredicate predicate) {
        return predicate.getExpression().accept(this);
    }

    @Override
    public T visit(MemberOfPredicate predicate) {
        return visit((BinaryExpressionPredicate) predicate);
    }

    @Override
    public T visit(LikePredicate predicate) {
        return visit((BinaryExpressionPredicate) predicate);
    }

    @Override
    public T visit(BetweenPredicate predicate) {
        T result = predicate.getLeft().accept(this);
        result = updateResult(result, predicate.getStart().accept(this));
        return updateResult(result, predicate.getEnd().accept(this));
    }

    @Override
    public T visit(InPredicate predicate) {
        T result = predicate.getLeft().accept(this);
        for (Expression right : predicate.getRight()) {
            result = updateResult(result, right.accept(this));
        }
        return result;
    }

    @Override
    public T visit(GtPredicate predicate) {
        return visit((BinaryExpressionPredicate) predicate);
    }

    @Override
    public T visit(GePredicate predicate) {
        return visit((BinaryExpressionPredicate) predicate);
    }

    @Override
    public T visit(LtPredicate predicate) {
        return visit((BinaryExpressionPredicate) predicate);
    }

    @Override
    public T visit(LePredicate predicate) {
        return visit((BinaryExpressionPredicate) predicate);
    }

    protected T visit(BinaryExpressionPredicate predicate) {
        T result = predicate.getLeft().accept(this);
        return updateResult(result, predicate.getRight().accept(this));
    }

    @Override
    public T visit(ExistsPredicate predicate) {
        return getDefaultResult();
    }

}