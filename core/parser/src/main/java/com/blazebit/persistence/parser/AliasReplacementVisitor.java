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

package com.blazebit.persistence.parser;

import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.InplaceModificationResultVisitorAdapter;
import com.blazebit.persistence.parser.expression.PathElementExpression;
import com.blazebit.persistence.parser.expression.PathExpression;
import com.blazebit.persistence.parser.expression.PropertyExpression;

import java.util.List;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class AliasReplacementVisitor extends InplaceModificationResultVisitorAdapter {

    private final Expression substitute;
    private final String alias;

    public AliasReplacementVisitor(Expression substitute, String alias) {
        this.substitute = substitute;
        this.alias = alias;
    }

    @Override
    public Expression visit(PathExpression expression) {
        List<PathElementExpression> expressions = expression.getExpressions();
        int size = expressions.size();

        if (size == 1) {
            PathElementExpression elementExpression = expressions.get(0);
            if (elementExpression instanceof PropertyExpression) {
                if (alias.equals(((PropertyExpression) elementExpression).getProperty())) {
                    return substitute;
                }
            }
        }
        return expression;
    }

}
