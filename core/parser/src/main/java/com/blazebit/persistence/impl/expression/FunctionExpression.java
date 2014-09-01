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

import java.util.List;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public class FunctionExpression implements Expression {

    private final String functionName;
    private final List<Expression> expressions;

    public FunctionExpression(String functionName, List<Expression> expressions) {
        this.functionName = functionName;
        this.expressions = expressions;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
    
    public String getFunctionName() {
        return functionName;
    }

    public List<Expression> getExpressions() {
        return expressions;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(functionName);
        sb.append('(');
        if(!expressions.isEmpty()){
            sb.append(expressions.get(0));
            for (int i = 1; i < expressions.size(); i++) {
                sb.append(',');
                sb.append(expressions.get(i));
            }
        }

        sb.append(')');
        return sb.toString();
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
