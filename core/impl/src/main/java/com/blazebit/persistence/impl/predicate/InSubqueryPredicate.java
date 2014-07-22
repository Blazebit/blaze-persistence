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
package com.blazebit.persistence.impl.predicate;

import com.blazebit.persistence.BaseQueryBuilder;
import com.blazebit.persistence.SubqueryBuilder;
import com.blazebit.persistence.impl.SubqueryBuilderImpl;
import com.blazebit.persistence.impl.expression.Expression;

/**
 *
 * @author ccbem
 */
public class InSubqueryPredicate implements Predicate {

    private Expression left;
    private SubqueryBuilderImpl<?> right;

    public InSubqueryPredicate(Expression left, SubqueryBuilderImpl<?> right) {
        this.left = left;
        this.right = right;
    }

    public Expression getLeft() {
        return left;
    }

    public SubqueryBuilderImpl<?> getRight() {
        return right;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

}
