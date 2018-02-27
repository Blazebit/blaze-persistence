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

package com.blazebit.persistence.impl.transform;

import com.blazebit.persistence.impl.ClauseType;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.InplaceModificationResultVisitorAdapter;
import com.blazebit.persistence.parser.predicate.EqPredicate;
import com.blazebit.persistence.parser.predicate.InPredicate;
import com.blazebit.persistence.parser.predicate.IsEmptyPredicate;
import com.blazebit.persistence.parser.predicate.IsNullPredicate;
import com.blazebit.persistence.parser.predicate.MemberOfPredicate;

/**
 * This visitor keeps track of whether joins are required which might change depending on the predicates.
 * It is also keeps the clause for which it is executed.
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.2.0
 */
public class ClauseAndJoinAwareResultVisitor extends InplaceModificationResultVisitorAdapter {

    protected ClauseType fromClause;
    // By default we require joins
    protected boolean joinRequired = true;

    public Expression visit(ClauseType clauseType, Expression expression) {
        this.fromClause = clauseType;
        try {
            return expression.accept(this);
        } finally {
            this.fromClause = null;
        }
    }

    @Override
    public Expression visit(EqPredicate predicate) {
        boolean original = joinRequired;
        joinRequired = false;
        super.visit(predicate);
        joinRequired = original;
        return predicate;
    }

    @Override
    public Expression visit(InPredicate predicate) {
        boolean original = joinRequired;
        joinRequired = false;
        super.visit(predicate);
        joinRequired = original;
        return predicate;
    }

    @Override
    public Expression visit(MemberOfPredicate predicate) {
        boolean original = joinRequired;
        joinRequired = false;
        super.visit(predicate);
        joinRequired = original;
        return predicate;
    }

    @Override
    public Expression visit(IsEmptyPredicate predicate) {
        boolean original = joinRequired;
        joinRequired = false;
        super.visit(predicate);
        joinRequired = original;
        return predicate;
    }

    @Override
    public Expression visit(IsNullPredicate predicate) {
        boolean original = joinRequired;
        joinRequired = false;
        super.visit(predicate);
        joinRequired = original;
        return predicate;
    }

}
