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
 * @author Moritz Becker
 */
public class SimpleCaseExpression extends GeneralCaseExpression {
    private final Expression caseOperand;

    public SimpleCaseExpression(Expression caseOperand, List<WhenClauseExpression> whenClauses, Expression defaultExpr) {
        super(whenClauses, defaultExpr);
        this.caseOperand = caseOperand;
    }

    public Expression getCaseOperand() {
        return caseOperand;
    }
    
    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 89 * hash + (this.caseOperand != null ? this.caseOperand.hashCode() : 0);
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
        final SimpleCaseExpression other = (SimpleCaseExpression) obj;
        if(!super.equals(other)){
            return false;
        } else if (this.caseOperand != other.caseOperand && (this.caseOperand == null || !this.caseOperand.equals(other.caseOperand))) {
            return false;
        }
        return true;
    }

    
    
    
}
