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

package com.blazebit.persistence.view.impl;

import com.blazebit.persistence.impl.expression.PathExpression;
import com.blazebit.persistence.impl.expression.PropertyExpression;
import com.blazebit.persistence.impl.expression.VisitorAdapter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class SubviewPrefixExpressionVisitor extends VisitorAdapter {
    private final List<PropertyExpression> prefixElements;

    public SubviewPrefixExpressionVisitor(List<String> prefixParts) {
        this.prefixElements = new ArrayList<PropertyExpression>(prefixParts.size());
        for (String prefixPart : prefixParts) {
            prefixElements.add(new PropertyExpression(prefixPart));
        }
    }

    @Override
    public void visit(PathExpression expression) {
        expression.getExpressions().addAll(0, prefixElements);
    }
    
}
