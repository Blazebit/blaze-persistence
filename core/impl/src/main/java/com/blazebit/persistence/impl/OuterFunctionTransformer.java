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

import java.util.ArrayList;
import java.util.List;

import com.blazebit.persistence.impl.expression.CompositeExpression;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.FunctionExpression;
import com.blazebit.persistence.impl.expression.PathExpression;

/**
 * This Transformer runs through the expressions of the query
 * For each OUTER(pp) expression it performs an implicitJoin for the join manager
 * of the surrounding query and replaces the OUTER(pp) expression with the base node alias '.' the field.
 *
 * We need a join manager hierarchy to do this.
 * We have decided to limit the outer statement to the join manager of the directly surrounding query so that the
 * user can specify the absolute path in a normalized form.
 *
 * @author Moritz Becker
 * @since 1.0
 */
public class OuterFunctionTransformer implements ExpressionTransformer {

    private final JoinManager joinManager;

    public OuterFunctionTransformer(JoinManager joinManager) {
        this.joinManager = joinManager;
    }

    @Override
    public Expression transform(Expression original, ClauseType fromClause, boolean joinRequired) {
        if (original instanceof CompositeExpression) {
            CompositeExpression compExpr = (CompositeExpression) original;
            CompositeExpression transformed = new CompositeExpression(new ArrayList<Expression>());
            List<Expression> expressions = compExpr.getExpressions();
            int size = expressions.size();
            for (int i = 0; i < size; i++) {
                transformed.getExpressions().add(transform(expressions.get(i), fromClause, joinRequired));
            }
            return transformed;
        } else if (original instanceof FunctionExpression && !ExpressionUtils.isOuterFunction((FunctionExpression) original)) {
            FunctionExpression func = (FunctionExpression) original;
            List<Expression> transformed = new ArrayList<Expression>();
            List<Expression> expressions = func.getExpressions();
            int size = expressions.size();
            for (int i = 0; i < size; i++) {
                transformed.add(transform(expressions.get(i), fromClause, joinRequired));
            }
            func.setExpressions(transformed);
            return func;
        }

        if (!(original instanceof FunctionExpression)) {
            return original;
        }
        PathExpression path = (PathExpression) ((FunctionExpression) original).getExpressions().get(0);

        if (joinManager.getParent() != null) {
            joinManager.getParent().implicitJoin(path, true, fromClause, false, true, joinRequired, false);
        }

        return original;
    }

}
