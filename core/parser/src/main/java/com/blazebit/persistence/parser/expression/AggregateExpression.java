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
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class AggregateExpression extends FunctionExpression {

    private boolean distinct;

    public AggregateExpression(boolean distinct, String functionName, List<Expression> expressions) {
        super(functionName, expressions);
        this.distinct = distinct;
    }

    /**
     * Constructor for COUNT(*)
     */
    public AggregateExpression() {
        super("COUNT", Collections.<Expression>emptyList());
        this.distinct = false;
    }

    @Override
    public AggregateExpression clone(boolean resolved) {
        if (expressions.isEmpty()) {
            return new AggregateExpression();
        } else {
            int size = expressions.size();
            List<Expression> newExpressions = new ArrayList<Expression>(size);

            for (int i = 0; i < size; i++) {
                newExpressions.add(expressions.get(i).clone(resolved));
            }
            return new AggregateExpression(distinct, functionName, newExpressions);
        }
    }

    public boolean isDistinct() {
        return distinct;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T accept(ResultVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
