/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
