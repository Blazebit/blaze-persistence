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

package com.blazebit.persistence.impl.builder.expression;

import com.blazebit.persistence.impl.ExpressionUtils;
import com.blazebit.persistence.impl.SubqueryBuilderListenerImpl;
import com.blazebit.persistence.impl.SubqueryInternalBuilder;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.SubqueryExpression;

/**
 *
 * @author Moritz Becker
 * @since 1.0.0
 */
public class SuperExpressionSubqueryBuilderListener<T> extends SubqueryBuilderListenerImpl<T> {

    protected Expression superExpression;
    private final String subqueryAlias;

    public SuperExpressionSubqueryBuilderListener(String subqueryAlias, Expression superExpression) {
        this.subqueryAlias = subqueryAlias;
        this.superExpression = superExpression;
    }

    @Override
    public void onBuilderEnded(SubqueryInternalBuilder<T> builder) {
        super.onBuilderEnded(builder);
        superExpression = ExpressionUtils.replaceSubexpression(superExpression, subqueryAlias, new SubqueryExpression(builder));
    }

}
