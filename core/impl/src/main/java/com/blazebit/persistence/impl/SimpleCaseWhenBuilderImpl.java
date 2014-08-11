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

import com.blazebit.persistence.SimpleCaseWhenBuilder;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO: implement
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public class SimpleCaseWhenBuilderImpl<T> implements SimpleCaseWhenBuilder<T> {

    private final T result;
    private final Expression caseOperandExpression;
    private final List<Expression[]> whenExpressions;
    private Expression elseExpression;
    private final ExpressionFactory expressionFactory;

    public SimpleCaseWhenBuilderImpl(T result, ExpressionFactory expressionFactory, String caseOperandExpression) {
        this.result = result;
        this.caseOperandExpression = expressionFactory.createCaseOperandExpression(caseOperandExpression);
        this.whenExpressions = new ArrayList<Expression[]>();
        this.expressionFactory = expressionFactory;
    }

    @Override
    public SimpleCaseWhenBuilder<T> when(String expression, String thenExpression) {
        Expression[] whenExpression = new Expression[2];
        whenExpression[0] = expressionFactory.createScalarExpression(expression);
        whenExpression[1] = expressionFactory.createScalarExpression(thenExpression);
        whenExpressions.add(whenExpression);
        return this;
    }

    @Override
    public T thenElse(String elseExpression) {
        this.elseExpression = expressionFactory.createScalarExpression(elseExpression);
        return result;
    }

}
