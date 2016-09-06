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

import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.PathExpression;

/**
 *
 * @author Moritz Becker
 * @since 1.0
 */
public class AliasReplacementTransformer implements ExpressionTransformer {

    private final Expression substitute;
    private final String alias;

    public AliasReplacementTransformer(Expression substitute, String alias) {
        this.substitute = substitute;
        this.alias = alias;
    }

    @Override
    public Expression transform(Expression original, ClauseType fromClause, boolean joinRequired) {
        if (original instanceof PathExpression) {
            PathExpression originalPathExpr = (PathExpression) original;
            if (originalPathExpr.toString().equals(alias)) {
                return substitute;
            }
        }
        return original;
    }

}
