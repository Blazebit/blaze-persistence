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

import com.blazebit.persistence.impl.expression.ArrayExpression;
import com.blazebit.persistence.impl.expression.CompositeExpression;
import com.blazebit.persistence.impl.expression.ExpressionVisitorAdapter;
import com.blazebit.persistence.impl.expression.FooExpression;
import com.blazebit.persistence.impl.expression.OuterExpression;
import com.blazebit.persistence.impl.expression.ParameterExpression;
import com.blazebit.persistence.impl.expression.PathExpression;
import com.blazebit.persistence.impl.expression.PropertyExpression;
import com.blazebit.persistence.impl.expression.SubqueryExpression;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class AbsoluteExpressionStringVisitor extends ExpressionVisitorAdapter {

    private final StringBuilder sb;
    private final String rootAlias;

    public AbsoluteExpressionStringVisitor(StringBuilder sb, String rootAlias) {
        this.sb = sb;
        this.rootAlias = rootAlias;
    }

    @Override
    public void visit(OuterExpression expression) {
        sb.append("OUTER(");
        super.visit(expression);
        sb.append(')');
    }

    @Override
    public void visit(SubqueryExpression expression) {
        sb.append("(");
        // TODO: actually we should also expand the expressions in the subquery to their absolute paths, but for now this should be enough
        super.visit(expression);
        sb.append(')');
    }

    @Override
    public void visit(FooExpression expression) {
        sb.append(expression);
    }

    @Override
    public void visit(ParameterExpression expression) {
        sb.append(expression);
    }

    @Override
    public void visit(PropertyExpression expression) {
        throw new RuntimeException("No property expressions expected!");
    }

    @Override
    public void visit(CompositeExpression expression) {
        super.visit(expression);
    }

    @Override
    public void visit(ArrayExpression expression) {
        throw new RuntimeException("No array expressions expected!");
    }

    @Override
    public void visit(PathExpression expression) {
        if (expression.getBaseNode() == null) {
            sb.append(expression.getPath());
        } else if (expression.getField() == null) {
            appendAbsolutePath((JoinNode) expression.getBaseNode());
        } else {
            appendAbsolutePath((JoinNode) expression.getBaseNode());
            sb.append('.').append(expression.getField());
        }
    }

    private void appendAbsolutePath(JoinNode baseNode) {
        sb.append(rootAlias);
        String absolutePath = baseNode.getAliasInfo().getAbsolutePath();
        if (absolutePath != null && absolutePath.length() > 0) {
            sb.append('.').append(absolutePath);
        }
    }

}
