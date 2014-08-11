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
import com.blazebit.persistence.impl.expression.SubqueryExpression;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ccbem
 */
public abstract class AbstractSuperExpressionSubqueryBuilderListener<X> extends SubqueryBuilderListenerImpl<X> {

    protected final String subqueryAlias;
    protected final Expression superExpression;

    public AbstractSuperExpressionSubqueryBuilderListener(String subqueryAlias, Expression superExpression) {
        this.subqueryAlias = subqueryAlias;
        this.superExpression = superExpression;
    }

    @Override
    public void onBuilderEnded(SubqueryBuilderImpl<X> builder) {
        super.onBuilderEnded(builder);
        final AliasReplacementTransformer replacementTransformer = new AliasReplacementTransformer(new SubqueryExpression(builder), subqueryAlias);
        VisitorAdapter transformationVisitor = new VisitorAdapter() {

            @Override
            public void visit(CompositeExpression expression) {
                List<Expression> transformed = new ArrayList<Expression>();
                for (Expression expr : expression.getExpressions()) {
                    transformed.add(replacementTransformer.transform(expr));
                }
                expression.getExpressions().clear();
                expression.getExpressions().addAll(transformed);
            }

        };
        superExpression.accept(transformationVisitor);
    }

}
