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

package com.blazebit.persistence.impl;

import com.blazebit.persistence.parser.expression.AbortableVisitorAdapter;
import com.blazebit.persistence.parser.expression.AggregateExpression;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.FunctionExpression;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
class AggregateDetectionVisitor extends AbortableVisitorAdapter {

    public static final Expression.ResultVisitor<Boolean> INSTANCE = new AggregateDetectionVisitor();

    private AggregateDetectionVisitor() {
    }

    @Override
    public Boolean visit(FunctionExpression expression) {
        if (expression instanceof AggregateExpression) {
            return true;
        }
        return super.visit(expression);
    }
}