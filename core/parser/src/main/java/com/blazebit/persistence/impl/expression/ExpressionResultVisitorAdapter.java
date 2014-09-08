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

/**
 *
 * @author Moritz Becker
 * @since 1.0
 */
public abstract class ExpressionResultVisitorAdapter implements Expression.ResultVisitor<Expression> {

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
}
