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

import java.util.List;

/**
 *
 * @author Moritz Becker
 * @since 1.0.0
 */
public abstract class VisitorAdapter implements Expression.Visitor {

    @Override
    public void visit(PathExpression expression) {
        List<PathElementExpression> expressions = expression.getExpressions();
        int size = expressions.size();
        for (int i = 0; i < size; i++) {
            expressions.get(i).accept(this);
        }
    }

    @Override
    public void visit(ArrayExpression expression) {
        expression.getBase().accept(this);
        expression.getIndex().accept(this);
    }

    @Override
    public void visit(TreatExpression expression) {
        expression.getExpression().accept(this);
    }

    @Override
    public void visit(ListIndexExpression expression) {
        expression.getPath().accept(this);
    }

    @Override
    public void visit(MapEntryExpression expression) {
        expression.getPath().accept(this);
    }

    @Override
    public void visit(MapKeyExpression expression) {
        expression.getPath().accept(this);
    }

    @Override
    public void visit(MapValueExpression expression) {
        expression.getPath().accept(this);
    }

    @Override
    public void visit(PropertyExpression expression) {
    }

    @Override
    public void visit(ParameterExpression expression) {
    }

    @Override
    public void visit(NullExpression expression) {
    }

    @Override
    public void visit(SubqueryExpression expression) {
    }

    @Override
    public void visit(FunctionExpression expression) {
        List<Expression> expressions = expression.getExpressions();
        int size = expressions.size();
        for (int i = 0; i < size; i++) {
            expressions.get(i).accept(this);
        }
    }

    @Override
    public void visit(TypeFunctionExpression expression) {
        visit((FunctionExpression) expression);
    }

    @Override
    public void visit(TrimExpression expression) {
        if (expression.getTrimCharacter() != null) {
            expression.getTrimCharacter().accept(this);
        }
        expression.getTrimSource().accept(this);
    }

    @Override
    public void visit(GeneralCaseExpression expression) {
        List<WhenClauseExpression> expressions = expression.getWhenClauses();
        int size = expressions.size();
        for (int i = 0; i < size; i++) {
            expressions.get(i).accept(this);
        }
        if (expression.getDefaultExpr() != null) {
            expression.getDefaultExpr().accept(this);
        }
    }

    @Override
    public void visit(SimpleCaseExpression expression) {
        expression.getCaseOperand().accept(this);
        visit((GeneralCaseExpression) expression);
    }

    @Override
    public void visit(WhenClauseExpression expression) {
        expression.getCondition().accept(this);
        expression.getResult().accept(this);
    }

    @Override
    public void visit(ArithmeticExpression expression) {
        expression.getLeft().accept(this);
        expression.getRight().accept(this);
    }

    @Override
    public void visit(ArithmeticFactor expression) {
        expression.getExpression().accept(this);
    }

    @Override
    public void visit(NumericLiteral expression) {
    }

    @Override
    public void visit(BooleanLiteral expression) {
    }

    @Override
    public void visit(StringLiteral expression) {
    }

    @Override
    public void visit(DateLiteral expression) {
    }

    @Override
    public void visit(TimeLiteral expression) {
    }

    @Override
    public void visit(TimestampLiteral expression) {
    }

    @Override
    public void visit(EnumLiteral expression) {
    }

    @Override
    public void visit(EntityLiteral expression) {
    }

    @Override
    public void visit(CompoundPredicate predicate) {
        List<Predicate> children = predicate.getChildren();
        int size = children.size();
        for (int i = 0; i < size; i++) {
            children.get(i).accept(this);
        }
    }

    @Override
    public void visit(EqPredicate predicate) {
        predicate.getLeft().accept(this);
        predicate.getRight().accept(this);
    }

    @Override
    public void visit(IsNullPredicate predicate) {
        predicate.getExpression().accept(this);
    }

    @Override
    public void visit(IsEmptyPredicate predicate) {
        predicate.getExpression().accept(this);
    }

    @Override
    public void visit(MemberOfPredicate predicate) {
        predicate.getLeft().accept(this);
        predicate.getRight().accept(this);
    }

    @Override
    public void visit(LikePredicate predicate) {
        predicate.getLeft().accept(this);
        predicate.getRight().accept(this);
    }

    @Override
    public void visit(BetweenPredicate predicate) {
        predicate.getLeft().accept(this);
        predicate.getStart().accept(this);
        predicate.getEnd().accept(this);
    }

    @Override
    public void visit(InPredicate predicate) {
        predicate.getLeft().accept(this);
        for (Expression right : predicate.getRight()) {
            right.accept(this);
        }
    }

    @Override
    public void visit(GtPredicate predicate) {
        predicate.getLeft().accept(this);
        predicate.getRight().accept(this);
    }

    @Override
    public void visit(GePredicate predicate) {
        predicate.getLeft().accept(this);
        predicate.getRight().accept(this);
    }

    @Override
    public void visit(LtPredicate predicate) {
        predicate.getLeft().accept(this);
        predicate.getRight().accept(this);
    }

    @Override
    public void visit(LePredicate predicate) {
        predicate.getLeft().accept(this);
        predicate.getRight().accept(this);
    }

    @Override
    public void visit(ExistsPredicate predicate) {
        predicate.getExpression().accept(this);
    }

}
