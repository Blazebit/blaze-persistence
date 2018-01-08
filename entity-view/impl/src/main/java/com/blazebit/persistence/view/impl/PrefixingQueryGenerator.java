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

package com.blazebit.persistence.view.impl;

import java.util.List;

import com.blazebit.persistence.impl.SimpleQueryGenerator;
import com.blazebit.persistence.impl.expression.PathElementExpression;
import com.blazebit.persistence.impl.expression.PathExpression;
import com.blazebit.persistence.impl.expression.PropertyExpression;
import com.blazebit.persistence.impl.expression.TreatExpression;

/**
 *
 * @author Christian Beikov
 * @since 1.0.1
 */
public class PrefixingQueryGenerator extends SimpleQueryGenerator {
    
    private final String prefix;

    public PrefixingQueryGenerator(List<String> prefixParts) {
        StringBuilder prefixSb = new StringBuilder();
        
        if (prefixParts != null) {
            for (int i = 0; i < prefixParts.size(); i++) {
                prefixSb.append(prefixParts.get(i));
                prefixSb.append('.');
            }
        }
        
        this.prefix = prefixSb.toString();
    }

    @Override
    public void visit(PathExpression expression) {
        final List<PathElementExpression> expressions = expression.getExpressions();
        final PathElementExpression firstElement = expressions.get(0);
        // Prefixing will only be done for the inner most path, but not the treat expression
        if (!(firstElement instanceof TreatExpression)) {
            sb.append(prefix);
        }
        if (firstElement instanceof PropertyExpression && "this".equalsIgnoreCase(((PropertyExpression) firstElement).getProperty())) {
            final int size = expressions.size();
            if (size > 1) {
                for (int i = 1; i < size; i++) {
                    expressions.get(i).accept(this);
                }
            } else {
                sb.setLength(sb.length() - 1);
            }
        } else {
            super.visit(expression);
        }
    }

//    @Override
//    public void visit(ArrayExpression expression) {
//        sb.append(prefix);
//        super.visit(expression);
//    }
}
