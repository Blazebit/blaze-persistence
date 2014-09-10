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
package com.blazebit.persistence.impl.predicate;

import com.blazebit.persistence.impl.expression.ArrayExpression;
import com.blazebit.persistence.impl.expression.CompositeExpression;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.FooExpression;
import com.blazebit.persistence.impl.expression.FunctionExpression;
import com.blazebit.persistence.impl.expression.GeneralCaseExpression;
import com.blazebit.persistence.impl.expression.ParameterExpression;
import com.blazebit.persistence.impl.expression.PathElementExpression;
import com.blazebit.persistence.impl.expression.PathExpression;
import com.blazebit.persistence.impl.expression.PropertyExpression;
import com.blazebit.persistence.impl.expression.SimpleCaseExpression;
import com.blazebit.persistence.impl.expression.SubqueryExpression;
import com.blazebit.persistence.impl.expression.WhenClauseExpression;

/**
 *
 * @author Moritz Becker
 * @since 1.0
 */
public abstract class VisitorAdapter implements Expression.Visitor {

    @Override
    public void visit(PathExpression expression) {
        for (PathElementExpression pathElementExpression : expression.getExpressions()) {
            pathElementExpression.accept(this);
        }
    }

    @Override
    public void visit(ArrayExpression expression) {
        expression.getBase().accept(this);
        expression.getIndex().accept(this);
    }

    @Override
    public void visit(PropertyExpression expression) {
    }

    @Override
    public void visit(ParameterExpression expression) {
    }

    @Override
    public void visit(CompositeExpression expression) {
        for (Expression expr : expression.getExpressions()) {
            expr.accept(this);
        }
    }

    @Override
    public void visit(FooExpression expression) {
    }

    @Override
    public void visit(SubqueryExpression expression) {
    }

    @Override
    public void visit(FunctionExpression expression) {
        for (Expression expr : expression.getExpressions()) {
            expr.accept(this);
        }
    }

    @Override
    public void visit(GeneralCaseExpression expression) {
        for(WhenClauseExpression whenClause : expression.getWhenClauses()){
            whenClause.accept(this);
        }
        expression.getDefaultExpr().accept(this);
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
    public void visit(AndPredicate predicate) {
        for (Predicate p : predicate.getChildren()) {
            p.accept(this);
        }
    }

    @Override
    public void visit(OrPredicate predicate) {
        for (Predicate p : predicate.getChildren()) {
            p.accept(this);
        }
    }

    @Override
    public void visit(NotPredicate predicate) {
        predicate.getPredicate().accept(this);
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
        predicate.getRight().accept(this);
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
    }

}
