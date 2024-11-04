/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.modifier.ExpressionModifier;
import com.blazebit.persistence.impl.transform.ExpressionModifierVisitor;

import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author Moritz Becker
 * @since 1.0.0
 */
public abstract class AbstractManager<T extends ExpressionModifier> {

    protected final ResolvingQueryGenerator queryGenerator;
    protected final ParameterManager parameterManager;
    protected final SubqueryInitiatorFactory subqueryInitFactory;

    protected AbstractManager(ResolvingQueryGenerator queryGenerator, ParameterManager parameterManager, SubqueryInitiatorFactory subqueryInitFactory) {
        this.queryGenerator = queryGenerator;
        this.parameterManager = parameterManager;
        this.subqueryInitFactory = subqueryInitFactory;
    }

    protected void registerParameterExpressions(Expression expression) {
        parameterManager.collectParameterRegistrations(expression, getClauseType(), subqueryInitFactory.getQueryBuilder());
    }

    protected void unregisterParameterExpressions(Expression expression) {
        parameterManager.collectParameterUnregistrations(expression, getClauseType(), subqueryInitFactory.getQueryBuilder());
    }

    protected void build(StringBuilder sb, Set<String> clauses) {
        Iterator<String> iter = clauses.iterator();
        if (iter.hasNext()) {
            sb.append(iter.next());
        }
        while (iter.hasNext()) {
            sb.append(", ");
            sb.append(iter.next());
        }
    }

    public abstract void apply(ExpressionModifierVisitor<? super T> visitor);

    public abstract ClauseType getClauseType();

}
