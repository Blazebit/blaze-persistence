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

import com.blazebit.persistence.impl.predicate.VisitorAdapter;
import com.blazebit.persistence.impl.expression.CompositeExpression;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.SubqueryExpression;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Moritz Becker
 * @since 1.0
 */
public class ExpressionUtils {

    public static boolean containsSubqueryExpression(Expression e) {
        SubqueryExpressionDetector detector = new SubqueryExpressionDetector();
        e.accept(detector);
        return detector.hasSubquery;
    }
    
    public static void replaceSubexpression(Expression superExpression, String placeholder, Expression substitute) {
        final AliasReplacementTransformer replacementTransformer = new AliasReplacementTransformer(substitute, placeholder);
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

    private static class SubqueryExpressionDetector extends VisitorAdapter {

        private boolean hasSubquery = false;

        @Override
        public void visit(SubqueryExpression expression) {
            hasSubquery = true;
        }

        public boolean hasSubquery() {
            return hasSubquery;
        }
    }
}
