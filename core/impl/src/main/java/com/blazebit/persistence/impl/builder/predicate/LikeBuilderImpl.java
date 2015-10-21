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
package com.blazebit.persistence.impl.builder.predicate;

import com.blazebit.persistence.CaseWhenAndThenBuilder;
import com.blazebit.persistence.CaseWhenBuilder;
import com.blazebit.persistence.CaseWhenOrThenBuilder;
import com.blazebit.persistence.CaseWhenThenBuilder;
import com.blazebit.persistence.EscapeBuilder;
import com.blazebit.persistence.LikeBuilder;
import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.SimpleCaseWhenBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.impl.ParameterManager;
import com.blazebit.persistence.impl.SubqueryAndExpressionBuilderListener;
import com.blazebit.persistence.impl.SubqueryInitiatorFactory;
import com.blazebit.persistence.impl.builder.expression.CaseWhenBuilderImpl;
import com.blazebit.persistence.impl.builder.expression.EscapeBuilderImpl;
import com.blazebit.persistence.impl.builder.expression.SimpleCaseWhenBuilderImpl;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.impl.predicate.LikePredicate;
import com.blazebit.persistence.impl.predicate.PredicateBuilder;

/**
 *
 * @author Moritz Becker
 */
public class LikeBuilderImpl<T> extends SubqueryAndExpressionBuilderListener<T> implements LikeBuilder<T>, PredicateBuilder {

    private final Expression leftExpression;
    private final ExpressionFactory expressionFactory;
    private final ParameterManager parameterManager;
    private final SubqueryInitiatorFactory subqueryInitFactory;
    private final T result;
    private final PredicateBuilderEndedListener listener;
    private final EscapeBuilderImpl.EscapeBuilderImplEndedListener escapeBuilderEndedListener = new EscapeBuilderImpl.EscapeBuilderImplEndedListener() {

        @Override
        public void onBuilderEnded(EscapeBuilderImpl<?> builder) {
            super.onBuilderEnded(builder);
            likePredicate = new LikePredicate(leftExpression, patternExpression, caseSensitive, builder.getEscapeCharacter(), negated);
            listener.onBuilderEnded(LikeBuilderImpl.this);
        }
    };

    private boolean negated;
    private boolean caseSensitive;
    private LikePredicate likePredicate;
    private Expression patternExpression;

    public LikeBuilderImpl(T result, PredicateBuilderEndedListener listener, Expression leftExpression, ExpressionFactory expressionFactory, ParameterManager parameterManager, SubqueryInitiatorFactory subqueryInitFactory, boolean negated, boolean caseSensitive) {
        this.result = result;
        this.listener = listener;
        this.leftExpression = leftExpression;
        this.expressionFactory = expressionFactory;
        this.parameterManager = parameterManager;
        this.subqueryInitFactory = subqueryInitFactory;
        this.negated = negated;
        this.caseSensitive = caseSensitive;
    }

    @Override
    public EscapeBuilder<T> value(Object value) {
        if (value == null) {
            throw new NullPointerException("value");
        }
        patternExpression = parameterManager.addParameterExpression(value);
        return escapeBuilderEndedListener.startBuilder(new EscapeBuilderImpl<T>(escapeBuilderEndedListener, result));
    }

    @Override
    public EscapeBuilder<T> expression(String expression) {
        if (expression == null) {
            throw new NullPointerException("expression");
        }
        patternExpression = expressionFactory.createStringExpression(expression);
        return escapeBuilderEndedListener.startBuilder(new EscapeBuilderImpl<T>(escapeBuilderEndedListener, result));
    }

    @Override
    public RestrictionBuilder<CaseWhenThenBuilder<CaseWhenBuilder<EscapeBuilder<T>>>> caseWhen(String expression) {
        EscapeBuilder<T> escapeBuilder = escapeBuilderEndedListener.startBuilder(new EscapeBuilderImpl<T>(escapeBuilderEndedListener, result));
        return startBuilder(new CaseWhenBuilderImpl<EscapeBuilder<T>>(escapeBuilder, this, subqueryInitFactory, expressionFactory, parameterManager)).when(expression);
    }

    @Override
    public SubqueryInitiator<RestrictionBuilder<CaseWhenThenBuilder<CaseWhenBuilder<EscapeBuilder<T>>>>> caseWhenSubquery() {
        EscapeBuilder<T> escapeBuilder = escapeBuilderEndedListener.startBuilder(new EscapeBuilderImpl<T>(escapeBuilderEndedListener, result));
        return startBuilder(new CaseWhenBuilderImpl<EscapeBuilder<T>>(escapeBuilder, this, subqueryInitFactory, expressionFactory, parameterManager)).whenSubquery();
    }

    @Override
    public SubqueryInitiator<RestrictionBuilder<CaseWhenThenBuilder<CaseWhenBuilder<EscapeBuilder<T>>>>> caseWhenSubquery(String subqueryAlias, String expression) {
        EscapeBuilder<T> escapeBuilder = escapeBuilderEndedListener.startBuilder(new EscapeBuilderImpl<T>(escapeBuilderEndedListener, result));
        return startBuilder(new CaseWhenBuilderImpl<EscapeBuilder<T>>(escapeBuilder, this, subqueryInitFactory, expressionFactory, parameterManager)).whenSubquery(subqueryAlias, expression);
    }

    @Override
    public SubqueryInitiator<CaseWhenThenBuilder<CaseWhenBuilder<EscapeBuilder<T>>>> caseWhenExists() {
        EscapeBuilder<T> escapeBuilder = escapeBuilderEndedListener.startBuilder(new EscapeBuilderImpl<T>(escapeBuilderEndedListener, result));
        return startBuilder(new CaseWhenBuilderImpl<EscapeBuilder<T>>(escapeBuilder, this, subqueryInitFactory, expressionFactory, parameterManager)).whenExists();
    }

    @Override
    public SubqueryInitiator<CaseWhenThenBuilder<CaseWhenBuilder<EscapeBuilder<T>>>> caseWhenNotExists() {
        EscapeBuilder<T> escapeBuilder = escapeBuilderEndedListener.startBuilder(new EscapeBuilderImpl<T>(escapeBuilderEndedListener, result));
        return startBuilder(new CaseWhenBuilderImpl<EscapeBuilder<T>>(escapeBuilder, this, subqueryInitFactory, expressionFactory, parameterManager)).whenNotExists();
    }

    @Override
    public CaseWhenAndThenBuilder<CaseWhenBuilder<EscapeBuilder<T>>> caseWhenAnd() {
        EscapeBuilder<T> escapeBuilder = escapeBuilderEndedListener.startBuilder(new EscapeBuilderImpl<T>(escapeBuilderEndedListener, result));
        return startBuilder(new CaseWhenBuilderImpl<EscapeBuilder<T>>(escapeBuilder, this, subqueryInitFactory, expressionFactory, parameterManager)).whenAnd();
    }

    @Override
    public CaseWhenOrThenBuilder<CaseWhenBuilder<EscapeBuilder<T>>> caseWhenOr() {
        EscapeBuilder<T> escapeBuilder = escapeBuilderEndedListener.startBuilder(new EscapeBuilderImpl<T>(escapeBuilderEndedListener, result));
        return startBuilder(new CaseWhenBuilderImpl<EscapeBuilder<T>>(escapeBuilder, this, subqueryInitFactory, expressionFactory, parameterManager)).whenOr();
    }

    @Override
    public SimpleCaseWhenBuilder<EscapeBuilder<T>> simpleCase(String caseOperand) {
        EscapeBuilder<T> escapeBuilder = escapeBuilderEndedListener.startBuilder(new EscapeBuilderImpl<T>(escapeBuilderEndedListener, result));
        return startBuilder(new SimpleCaseWhenBuilderImpl<EscapeBuilder<T>>(escapeBuilder, this, expressionFactory, expressionFactory.createCaseOperandExpression(caseOperand)));
    }

    @Override
    public LikePredicate getPredicate() {
        return likePredicate;
    }

    @Override
    protected void verifyBuilderEnded() {
        super.verifyBuilderEnded();
        escapeBuilderEndedListener.verifyBuilderEnded();
    }

}
