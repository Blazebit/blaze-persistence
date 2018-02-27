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

/**
 * SubqueryExpressions can never be returned by the parser and are therefore never cached. Thus, the clone method
 * does not need to do deep cloning.
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class SubqueryExpression extends AbstractExpression {

    private final Subquery subquery;

    public SubqueryExpression(Subquery builder) {
        this.subquery = builder;
    }

    @Override
    public SubqueryExpression clone(boolean resolved) {
        return new SubqueryExpression(subquery);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T accept(ResultVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public Subquery getSubquery() {
        return subquery;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + (this.subquery != null ? this.subquery.hashCode() : 0);
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
        final SubqueryExpression other = (SubqueryExpression) obj;
        if (this.subquery != other.subquery && (this.subquery == null || !this.subquery.equals(other.subquery))) {
            return false;
        }
        return true;
    }
}
