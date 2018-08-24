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
