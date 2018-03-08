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

package com.blazebit.persistence.parser.expression;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class FunctionExpression extends AbstractExpression {

    protected final String functionName;
    protected List<Expression> expressions;

    @SuppressWarnings("unchecked")
    public FunctionExpression(String functionName, List<? extends Expression> expressions) {
        this.functionName = functionName;
        this.expressions = (List<Expression>) expressions;
    }

    @Override
    public FunctionExpression clone(boolean resolved) {
        int size = expressions.size();
        List<Expression> newExpressions = new ArrayList<Expression>(size);

        for (int i = 0; i < size; i++) {
            newExpressions.add(expressions.get(i).clone(resolved));
        }

        return new FunctionExpression(functionName, newExpressions);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T accept(ResultVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public String getFunctionName() {
        return functionName;
    }

    public List<Expression> getExpressions() {
        return expressions;
    }

    public void setExpressions(List<Expression> expressions) {
        this.expressions = expressions;
    }

    @Override
    public int hashCode() {
        int hash = 7;
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
        final FunctionExpression other = (FunctionExpression) obj;
        if ((this.functionName == null) ? (other.functionName != null) : !this.functionName.equals(other.functionName)) {
            return false;
        }
        if (this.expressions != other.expressions && (this.expressions == null || !this.expressions.equals(other.expressions))) {
            return false;
        }
        return true;
    }
}
