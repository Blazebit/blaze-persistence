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
 * @author Moritz Becker
 * @since 1.0.0
 */
public class GeneralCaseExpression extends AbstractExpression {

    protected List<WhenClauseExpression> whenClauses;
    protected Expression defaultExpr;

    public GeneralCaseExpression(List<WhenClauseExpression> whenClauses, Expression defaultExpr) {
        this.whenClauses = whenClauses;
        this.defaultExpr = defaultExpr;
    }

    @Override
    public GeneralCaseExpression clone(boolean resolved) {
        int size = whenClauses.size();
        List<WhenClauseExpression> newWhenClauses = new ArrayList<WhenClauseExpression>(size);

        for (int i = 0; i < size; i++) {
            newWhenClauses.add(whenClauses.get(i).clone(resolved));
        }

        if (defaultExpr == null) {
            return new GeneralCaseExpression(whenClauses, null);
        } else {
            return new GeneralCaseExpression(newWhenClauses, defaultExpr.clone(resolved));
        }
    }

    public List<WhenClauseExpression> getWhenClauses() {
        return whenClauses;
    }

    public void setWhenClauses(List<WhenClauseExpression> whenClauses) {
        this.whenClauses = whenClauses;
    }

    public Expression getDefaultExpr() {
        return defaultExpr;
    }

    public void setDefaultExpr(Expression defaultExpr) {
        this.defaultExpr = defaultExpr;
    }

    @Override
    public void accept(Expression.Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T accept(ResultVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + (this.whenClauses != null ? this.whenClauses.hashCode() : 0);
        hash = 47 * hash + (this.defaultExpr != null ? this.defaultExpr.hashCode() : 0);
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
        final GeneralCaseExpression other = (GeneralCaseExpression) obj;
        if (this.whenClauses != other.whenClauses && (this.whenClauses == null || !this.whenClauses.equals(other.whenClauses))) {
            return false;
        }
        if (this.defaultExpr != other.defaultExpr && (this.defaultExpr == null || !this.defaultExpr.equals(other.defaultExpr))) {
            return false;
        }
        return true;
    }

}
