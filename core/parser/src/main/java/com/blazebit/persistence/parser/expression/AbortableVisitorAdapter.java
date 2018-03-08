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
 *
 * @author Moritz Becker
 * @since 1.0.0
 */
public abstract class AbortableVisitorAdapter implements Expression.ResultVisitor<Boolean> {

    @Override
    public Boolean visit(PathExpression expression) {
        List<PathElementExpression> expressions = expression.getExpressions();
        int size = expressions.size();
        for (int i = 0; i < size; i++) {
            if (expressions.get(i).accept(this)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Boolean visit(ArrayExpression expression) {
        if (expression.getBase().accept(this)) {
            return true;
        }
        return expression.getIndex().accept(this);
    }

    @Override
    public Boolean visit(TreatExpression expression) {
        return expression.getExpression().accept(this);
    }

    @Override
    public Boolean visit(PropertyExpression expression) {
        return false;
    }

    @Override
    public Boolean visit(ParameterExpression expression) {
        return false;
    }

    @Override
    public Boolean visit(ListIndexExpression expression) {
        return expression.getPath().accept(this);
    }

    @Override
    public Boolean visit(MapEntryExpression expression) {
        return expression.getPath().accept(this);
    }

    @Override
    public Boolean visit(MapKeyExpression expression) {
        return expression.getPath().accept(this);
    }

    @Override
    public Boolean visit(MapValueExpression expression) {
        return expression.getPath().accept(this);
    }

    @Override
    public Boolean visit(NullExpression expression) {
        return false;
    }

    @Override
    public Boolean visit(SubqueryExpression expression) {
        return false;
    }

    @Override
    public Boolean visit(FunctionExpression expression) {
        List<Expression> expressions = expression.getExpressions();
        int size = expressions.size();
        for (int i = 0; i < size; i++) {
            if (expressions.get(i).accept(this)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Boolean visit(TypeFunctionExpression expression) {
        return visit((FunctionExpression) expression);
    }

    @Override
    public Boolean visit(TrimExpression expression) {
        if (expression.getTrimCharacter() != null && expression.getTrimCharacter().accept(this)) {
            return true;
        }
        return expression.getTrimSource().accept(this);
    }

    @Override
    public Boolean visit(GeneralCaseExpression expression) {
        List<WhenClauseExpression> expressions = expression.getWhenClauses();
        int size = expressions.size();
        for (int i = 0; i < size; i++) {
            if (expressions.get(i).accept(this)) {
                return true;
            }
        }

        if (expression.getDefaultExpr() != null) {
            return expression.getDefaultExpr().accept(this);
        }

        return false;
    }

    @Override
    public Boolean visit(SimpleCaseExpression expression) {
        if (expression.getCaseOperand().accept(this)) {
            return true;
        }
        return visit((GeneralCaseExpression) expression);
    }

    @Override
    public Boolean visit(WhenClauseExpression expression) {
        if (expression.getCondition().accept(this)) {
            return true;
        }
        return expression.getResult().accept(this);
    }

    @Override
    public Boolean visit(ArithmeticExpression expression) {
        if (expression.getLeft().accept(this)) {
            return true;
        }
        if (expression.getRight().accept(this)) {
            return true;
        }
        return false;
    }

    @Override
    public Boolean visit(ArithmeticFactor expression) {
        return expression.getExpression().accept(this);
    }

    @Override
    public Boolean visit(NumericLiteral expression) {
        return false;
    }

    @Override
    public Boolean visit(BooleanLiteral expression) {
        return false;
    }

    @Override
    public Boolean visit(StringLiteral expression) {
        return false;
    }

    @Override
    public Boolean visit(DateLiteral expression) {
        return false;
    }

    @Override
    public Boolean visit(TimeLiteral expression) {
        return false;
    }

    @Override
    public Boolean visit(TimestampLiteral expression) {
        return false;
    }

    @Override
    public Boolean visit(EnumLiteral expression) {
        return false;
    }

    @Override
    public Boolean visit(EntityLiteral expression) {
        return false;
    }

    @Override
    public Boolean visit(CompoundPredicate predicate) {
        List<Predicate> children = predicate.getChildren();
        int size = children.size();
        for (int i = 0; i < size; i++) {
            if (children.get(i).accept(this)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Boolean visit(EqPredicate predicate) {
        return visit((BinaryExpressionPredicate) predicate);
    }

    @Override
    public Boolean visit(IsNullPredicate predicate) {
        return predicate.getExpression().accept(this);
    }

    @Override
    public Boolean visit(IsEmptyPredicate predicate) {
        return predicate.getExpression().accept(this);
    }

    @Override
    public Boolean visit(MemberOfPredicate predicate) {
        return visit((BinaryExpressionPredicate) predicate);
    }

    @Override
    public Boolean visit(LikePredicate predicate) {
        return visit((BinaryExpressionPredicate) predicate);
    }

    @Override
    public Boolean visit(BetweenPredicate predicate) {
        if (predicate.getLeft().accept(this)) {
            return true;
        }
        if (predicate.getStart().accept(this)) {
            return true;
        }
        return predicate.getEnd().accept(this);
    }

    @Override
    public Boolean visit(InPredicate predicate) {
        if (predicate.getLeft().accept(this)) {
            return true;
        }
        for (Expression right : predicate.getRight()) {
            if (right.accept(this)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Boolean visit(GtPredicate predicate) {
        return visit((BinaryExpressionPredicate) predicate);
    }

    @Override
    public Boolean visit(GePredicate predicate) {
        return visit((BinaryExpressionPredicate) predicate);
    }

    @Override
    public Boolean visit(LtPredicate predicate) {
        return visit((BinaryExpressionPredicate) predicate);
    }

    @Override
    public Boolean visit(LePredicate predicate) {
        return visit((BinaryExpressionPredicate) predicate);
    }

    protected Boolean visit(BinaryExpressionPredicate predicate) {
        if (predicate.getLeft().accept(this)) {
            return true;
        }
        return predicate.getRight().accept(this);
    }

    @Override
    public Boolean visit(ExistsPredicate predicate) {
        return false;
    }

}
