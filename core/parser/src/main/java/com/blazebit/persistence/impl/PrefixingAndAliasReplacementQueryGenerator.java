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

import com.blazebit.persistence.impl.expression.PathElementExpression;
import com.blazebit.persistence.impl.expression.PathExpression;
import com.blazebit.persistence.impl.expression.PropertyExpression;

import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class PrefixingAndAliasReplacementQueryGenerator extends SimpleQueryGenerator {

    private final String prefix;
    private final String substitute;
    private final String alias;
    private final String aliasToSkip;
    private final boolean skipPrefix;

    public PrefixingAndAliasReplacementQueryGenerator(String prefix, String substitute, String alias, String aliasToSkip, boolean skipPrefix) {
        this.prefix = prefix;
        this.substitute = substitute;
        this.alias = alias;
        this.aliasToSkip = aliasToSkip;
        this.skipPrefix = skipPrefix;
    }

    @Override
    public void visit(PathExpression expression) {
        List<PathElementExpression> expressions = expression.getExpressions();
        int size = expressions.size();

        if (size == 1) {
            PathElementExpression elementExpression = expressions.get(0);
            if (elementExpression instanceof PropertyExpression) {
                String property = ((PropertyExpression) elementExpression).getProperty();
                if (alias.equals(property)) {
                    sb.append(substitute);
                    return;
                }
                if (skipPrefix && prefix.equals(property)) {
                    super.visit(expression);
                    return;
                }
            }
        } else if (aliasToSkip != null) {
            PathElementExpression elementExpression = expressions.get(0);
            if (elementExpression instanceof PropertyExpression) {
                if (aliasToSkip.equals(((PropertyExpression) elementExpression).getProperty())) {
                    super.visit(expression);
                    return;
                }
            }
        }
        sb.append(prefix);
        sb.append('.');
        super.visit(expression);
    }

}
