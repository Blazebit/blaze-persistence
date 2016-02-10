/*
 * Copyright 2014 Blazebit.
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
package com.blazebit.persistence.impl.expression;

import java.util.List;

import com.blazebit.persistence.impl.predicate.AndPredicate;
import com.blazebit.persistence.impl.predicate.BetweenPredicate;
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

/**
 *
 * @author Moritz Becker
 * @since 1.0
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
    public Boolean visit(PropertyExpression expression) {
        return false;
    }

    @Override
    public Boolean visit(ParameterExpression expression) {
        return false;
    }

    @Override
    public Boolean visit(CompositeExpression expression) {
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
    public Boolean visit(FooExpression expression) {
        return false;
    }

    @Override
    public Boolean visit(LiteralExpression expression) {
        return false;
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
    public Boolean visit(GeneralCaseExpression expression) {
        List<WhenClauseExpression> expressions = expression.getWhenClauses();
        int size = expressions.size();
        for (int i = 0; i < size; i++) {
            if (expressions.get(i).accept(this)) {
                return true;
            }
        }
        return expression.getDefaultExpr().accept(this);
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
    public Boolean visit(AndPredicate predicate) {
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
    public Boolean visit(OrPredicate predicate) {
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
    public Boolean visit(NotPredicate predicate) {
        return predicate.getPredicate().accept(this);
    }

    @Override
    public Boolean visit(EqPredicate predicate) {
        if (predicate.getLeft().accept(this)) {
            return true;
        }
        return predicate.getRight().accept(this);
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
        if (predicate.getLeft().accept(this)) {
            return true;
        }
        return predicate.getRight().accept(this);
    }

    @Override
    public Boolean visit(LikePredicate predicate) {
        if (predicate.getLeft().accept(this)) {
            return true;
        }
        return predicate.getRight().accept(this);
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
        return predicate.getRight().accept(this);
    }

    @Override
    public Boolean visit(GtPredicate predicate) {
        if (predicate.getLeft().accept(this)) {
            return true;
        }
        return predicate.getRight().accept(this);
    }

    @Override
    public Boolean visit(GePredicate predicate) {
        if (predicate.getLeft().accept(this)) {
            return true;
        }
        return predicate.getRight().accept(this);
    }

    @Override
    public Boolean visit(LtPredicate predicate) {
        if (predicate.getLeft().accept(this)) {
            return true;
        }
        return predicate.getRight().accept(this);
    }

    @Override
    public Boolean visit(LePredicate predicate) {
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
