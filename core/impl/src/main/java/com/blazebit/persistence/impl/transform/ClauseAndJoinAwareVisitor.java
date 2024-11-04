/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.transform;

import com.blazebit.persistence.impl.ClauseType;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.VisitorAdapter;
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
public class ClauseAndJoinAwareVisitor extends VisitorAdapter {

    protected ClauseType fromClause;
    // By default we require joins
    protected boolean joinRequired = true;

    public void visit(ClauseType clauseType, Expression expression) {
        this.fromClause = clauseType;
        try {
            expression.accept(this);
        } finally {
            this.fromClause = null;
        }
    }

    @Override
    public void visit(EqPredicate predicate) {
        boolean original = joinRequired;
        joinRequired = false;
        super.visit(predicate);
        joinRequired = original;
    }

    @Override
    public void visit(InPredicate predicate) {
        boolean original = joinRequired;
        joinRequired = false;
        super.visit(predicate);
        joinRequired = original;
    }

    @Override
    public void visit(MemberOfPredicate predicate) {
        boolean original = joinRequired;
        joinRequired = false;
        super.visit(predicate);
        joinRequired = original;
    }

    @Override
    public void visit(IsEmptyPredicate predicate) {
        boolean original = joinRequired;
        joinRequired = false;
        super.visit(predicate);
        joinRequired = original;
    }

    @Override
    public void visit(IsNullPredicate predicate) {
        boolean original = joinRequired;
        joinRequired = false;
        super.visit(predicate);
        joinRequired = original;
    }
}
