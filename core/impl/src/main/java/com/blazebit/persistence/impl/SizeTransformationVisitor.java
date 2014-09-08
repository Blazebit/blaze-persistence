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
package com.blazebit.persistence.impl;

import com.blazebit.persistence.impl.expression.CompositeExpression;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.ExpressionResultVisitorAdapter;
import com.blazebit.persistence.impl.expression.GeneralCaseExpression;
import com.blazebit.persistence.impl.expression.SimpleCaseExpression;
import com.blazebit.persistence.impl.expression.WhenClauseExpression;

/**
 *
 * @author Moritz Becker
 */
public abstract class SizeTransformationVisitor extends ExpressionResultVisitorAdapter {

    @Override
    public Expression visit(CompositeExpression expression) {
        for (int i = 0; i < expression.getExpressions().size(); i++) {
            expression.getExpressions().set(i, expression.getExpressions().get(i).accept(this));
        }
        return expression;
    }

    @Override
    public Expression visit(WhenClauseExpression expression) {
        expression.getCondition().accept(this);
        expression.setResult(expression.getResult().accept(this));
        return expression;
    }

    @Override
    public Expression visit(GeneralCaseExpression expression) {
        for (WhenClauseExpression whenClause : expression.getWhenClauses()) {
            whenClause.accept(this);
        }
        expression.setDefaultExpr(expression.getDefaultExpr().accept(this));
        return expression;
    }

    @Override
    public Expression visit(SimpleCaseExpression expression) {
        return visit((GeneralCaseExpression) expression);
    }
}
