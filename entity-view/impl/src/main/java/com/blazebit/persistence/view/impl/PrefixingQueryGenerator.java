/*
 * Copyright 2015 Blazebit.
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
import com.blazebit.persistence.impl.expression.LiteralExpression;
import com.blazebit.persistence.impl.expression.PathExpression;

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
    public void visit(LiteralExpression expression) {
        // We override this because the resulting string will be parsed by us again
        sb.append(expression.getWrapperFunction());
        sb.append('(');
        sb.append(expression.getLiteral());
        sb.append(')');
    }

    @Override
    public void visit(PathExpression expression) {
        sb.append(prefix);
        super.visit(expression);
    }

//    @Override
//    public void visit(ArrayExpression expression) {
//        sb.append(prefix);
//        super.visit(expression);
//    }
}
