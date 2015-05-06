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
package com.blazebit.persistence.impl.expression;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public class CompositeExpression extends AbstractExpression {

    private final List<Expression> expressions;

    public CompositeExpression(List<Expression> expressions) {
        this.expressions = expressions;
    }

    @Override
    public CompositeExpression clone() {
        int size = expressions.size();
        List<Expression> newExpressions = new ArrayList<Expression>(size);
        
        for (int i = 0; i < size; i++) {
            newExpressions.add(expressions.get(i).clone());
        }
        
        return new CompositeExpression(newExpressions);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T accept(ResultVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public List<Expression> getExpressions() {
        return expressions;
    }

    public void append(Expression expr) {
        Expression lastExpr;
        if (expr instanceof CompositeExpression) {
            CompositeExpression composite = (CompositeExpression) expr;
            for (Expression subexpr : composite.getExpressions()) {
                append(subexpr);
            }
        } else if (!expressions.isEmpty() && expr instanceof FooExpression && (lastExpr = expressions.get(expressions.size() - 1)) instanceof FooExpression) {
            ((FooExpression) lastExpr).getStringBuilder().append(((FooExpression) expr).getStringBuilder());
        } else {
            expressions.add(expr);
        }
    }

    public void prepend(String str) {
        if (!str.isEmpty()) {
            Expression lastExpr;
            if (!expressions.isEmpty() && (lastExpr = expressions.get(expressions.size() - 1)) instanceof FooExpression) {
                ((FooExpression) lastExpr).getStringBuilder().insert(0, str);
            } else {
                expressions.add(0, new FooExpression(str));
            }
        }
    }

    public void append(String str) {
        if (str != null && !str.isEmpty()) {
            Expression lastExpr;
            if (!expressions.isEmpty() && (lastExpr = expressions.get(expressions.size() - 1)) instanceof FooExpression) {
                ((FooExpression) lastExpr).getStringBuilder().append(str);
            } else {
                expressions.add(new FooExpression(str));
            }
        }
    }

    public void append(StringBuilder sb) {
        if (sb != null && sb.length() > 0) {
            Expression lastExpr;
            if (!expressions.isEmpty() && (lastExpr = expressions.get(expressions.size() - 1)) instanceof FooExpression) {
                ((FooExpression) lastExpr).getStringBuilder().append(sb);
            } else {
                expressions.add(new FooExpression(sb));
            }
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + (this.expressions != null ? this.expressions.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CompositeExpression other = (CompositeExpression) obj;
        if (this.expressions != other.expressions && (this.expressions == null || !this.expressions.equals(other.expressions))) {
            return false;
        }
        return true;
    }
}
