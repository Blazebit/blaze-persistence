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

import com.blazebit.persistence.SubqueryBuilder;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public class SubqueryExpression implements Expression {

    private final SubqueryBuilder<?> builder;

    public SubqueryExpression(SubqueryBuilder<?> builder) {
        this.builder = builder;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public SubqueryBuilder<?> getBuilder() {
        return builder;
    }
    
    @Override
    // TODO: this method should not be used for query generation since it consumes another string builder
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        sb.append(builder.getQueryString());
        sb.append(')');
        return sb.toString();
    }
    
    // TODO: needs a good equals-hashCode implementation
}
