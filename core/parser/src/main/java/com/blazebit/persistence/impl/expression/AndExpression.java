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
public final class AndExpression extends MultinaryBooleanExpression {

    public AndExpression() {
    }

    public AndExpression(BooleanExpression... children) {
        super(children);
    }

    private AndExpression(List<BooleanExpression> children) {
        super(children);
    }

    @Override
    public AndExpression clone() {
        int size = children.size();
        List<BooleanExpression> newChildren = new ArrayList<BooleanExpression>(size);

        for (int i = 0; i < size; i++) {
            newChildren.add(children.get(i).clone());
        }

        return new AndExpression(newChildren);
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
