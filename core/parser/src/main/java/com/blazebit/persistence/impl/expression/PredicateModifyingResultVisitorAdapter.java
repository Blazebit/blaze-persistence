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
import com.blazebit.persistence.impl.predicate.MultinaryPredicate;
import com.blazebit.persistence.impl.predicate.NotPredicate;
import com.blazebit.persistence.impl.predicate.OrPredicate;
import com.blazebit.persistence.impl.predicate.Predicate;

/**
 *
 * @author Moritz Becker
 * @since 1.0
 */
public abstract class PredicateModifyingResultVisitorAdapter implements Expression.ResultVisitor<Expression> {

    @Override
    public Expression visit(PathExpression expression) {
        for (PathElementExpression pathElementExpression : expression.getExpressions()) {
            pathElementExpression.accept(this);
        }
        return expression;
    }

    @Override
    public Expression visit(ArrayExpression expression) {
        expression.getBase().accept(this);
        expression.getIndex().accept(this);
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
    public Expression visit(CompositeExpression expression) {
        for (Expression expr : expression.getExpressions()) {
            expr.accept(this);
        }
        return expression;
    }

    @Override
    public Expression visit(FooExpression expression) {
        return expression;
    }

    @Override
    public Expression visit(LiteralExpression expression) {
        return expression;
    }

    @Override
    public Expression visit(SubqueryExpression expression) {
        return expression;
    }

    @Override
    public Expression visit(FunctionExpression expression) {
        for (Expression expr : expression.getExpressions()) {
            expr.accept(this);
        }
        return expression;
    }

    @Override
    public Expression visit(GeneralCaseExpression expression) {
        for(WhenClauseExpression whenClause : expression.getWhenClauses()){
            whenClause.accept(this);
        }
        expression.getDefaultExpr().accept(this);
        return expression;
    }

    @Override
    public Expression visit(SimpleCaseExpression expression) {
        expression.getCaseOperand().accept(this);
        visit((GeneralCaseExpression) expression);
        return expression;
    }

    @Override
    public Expression visit(WhenClauseExpression expression) {
        expression.getCondition().accept(this);
        expression.getResult().accept(this);
        return expression;
    }
    
    @Override
    public Expression visit(AndPredicate predicate) {
        return visit((MultinaryPredicate) predicate);
    }

    @Override
    public Expression visit(OrPredicate predicate) {
        return visit((MultinaryPredicate) predicate);
    }

    @Override
    public Expression visit(NotPredicate predicate) {
        predicate.setPredicate((Predicate) predicate.getPredicate().accept(this));
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
        return visit((BinaryExpressionPredicate) predicate);
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
    
    private BinaryExpressionPredicate visit(BinaryExpressionPredicate predicate){
        predicate.setLeft(predicate.getLeft().accept(this));
        predicate.setRight(predicate.getRight().accept(this));
        return predicate;
    }
    
    private MultinaryPredicate visit(MultinaryPredicate predicate) {
        for (int i = 0; i < predicate.getChildren().size(); i++){
            Predicate p = predicate.getChildren().get(i);
            predicate.getChildren().set(i, (Predicate) p.accept(this));
        }
        return predicate;
    }
}
