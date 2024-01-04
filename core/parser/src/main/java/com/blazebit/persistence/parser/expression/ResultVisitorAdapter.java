/*
 * Copyright 2014 - 2024 Blazebit.
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

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public class ResultVisitorAdapter implements Expression.Visitor {

    private final Expression.ResultVisitor<?> delegate;

    public ResultVisitorAdapter(Expression.ResultVisitor<?> delegate) {
        this.delegate = delegate;
    }

    @Override
    public void visit(PathExpression expression) {
        delegate.visit(expression);
    }

    @Override
    public void visit(PropertyExpression expression) {
        delegate.visit(expression);
    }

    @Override
    public void visit(ParameterExpression expression) {
        delegate.visit(expression);
    }

    @Override
    public void visit(ArrayExpression expression) {
        delegate.visit(expression);
    }

    @Override
    public void visit(TreatExpression expression) {
        delegate.visit(expression);
    }

    @Override
    public void visit(ListIndexExpression expression) {
        delegate.visit(expression);
    }

    @Override
    public void visit(MapEntryExpression expression) {
        delegate.visit(expression);
    }

    @Override
    public void visit(MapKeyExpression expression) {
        delegate.visit(expression);
    }

    @Override
    public void visit(MapValueExpression expression) {
        delegate.visit(expression);
    }

    @Override
    public void visit(NullExpression expression) {
        delegate.visit(expression);
    }

    @Override
    public void visit(SubqueryExpression expression) {
        delegate.visit(expression);
    }

    @Override
    public void visit(FunctionExpression expression) {
        delegate.visit(expression);
    }

    @Override
    public void visit(TypeFunctionExpression expression) {
        delegate.visit(expression);
    }

    @Override
    public void visit(TrimExpression expression) {
        delegate.visit(expression);
    }

    @Override
    public void visit(WhenClauseExpression expression) {
        delegate.visit(expression);
    }

    @Override
    public void visit(GeneralCaseExpression expression) {
        delegate.visit(expression);
    }

    @Override
    public void visit(SimpleCaseExpression expression) {
        delegate.visit(expression);
    }

    @Override
    public void visit(ArithmeticExpression expression) {
        delegate.visit(expression);
    }

    @Override
    public void visit(ArithmeticFactor expression) {
        delegate.visit(expression);
    }

    @Override
    public void visit(NumericLiteral expression) {
        delegate.visit(expression);
    }

    @Override
    public void visit(StringLiteral expression) {
        delegate.visit(expression);
    }

    @Override
    public void visit(DateLiteral expression) {
        delegate.visit(expression);
    }

    @Override
    public void visit(TimeLiteral expression) {
        delegate.visit(expression);
    }

    @Override
    public void visit(TimestampLiteral expression) {
        delegate.visit(expression);
    }

    @Override
    public void visit(EnumLiteral expression) {
        delegate.visit(expression);
    }

    @Override
    public void visit(EntityLiteral expression) {
        delegate.visit(expression);
    }

    @Override
    public void visit(EqPredicate predicate) {
        delegate.visit(predicate);
    }

    @Override
    public void visit(IsNullPredicate predicate) {
        delegate.visit(predicate);
    }

    @Override
    public void visit(IsEmptyPredicate predicate) {
        delegate.visit(predicate);
    }

    @Override
    public void visit(MemberOfPredicate predicate) {
        delegate.visit(predicate);
    }

    @Override
    public void visit(LikePredicate predicate) {
        delegate.visit(predicate);
    }

    @Override
    public void visit(BetweenPredicate predicate) {
        delegate.visit(predicate);
    }

    @Override
    public void visit(InPredicate predicate) {
        delegate.visit(predicate);
    }

    @Override
    public void visit(GtPredicate predicate) {
        delegate.visit(predicate);
    }

    @Override
    public void visit(GePredicate predicate) {
        delegate.visit(predicate);
    }

    @Override
    public void visit(LtPredicate predicate) {
        delegate.visit(predicate);
    }

    @Override
    public void visit(LePredicate predicate) {
        delegate.visit(predicate);
    }

    @Override
    public void visit(ExistsPredicate predicate) {
        delegate.visit(predicate);
    }

    @Override
    public void visit(BooleanLiteral predicate) {
        delegate.visit(predicate);
    }

    @Override
    public void visit(CompoundPredicate predicate) {
        delegate.visit(predicate);
    }
}
