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

package com.blazebit.persistence.impl.builder.predicate;

import com.blazebit.persistence.CaseWhenAndThenBuilder;
import com.blazebit.persistence.CaseWhenBuilder;
import com.blazebit.persistence.CaseWhenOrThenBuilder;
import com.blazebit.persistence.CaseWhenThenBuilder;
import com.blazebit.persistence.EscapeBuilder;
import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.LikeBuilder;
import com.blazebit.persistence.MultipleSubqueryInitiator;
import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.SimpleCaseWhenBuilder;
import com.blazebit.persistence.SubqueryBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.impl.ClauseType;
import com.blazebit.persistence.impl.MultipleSubqueryInitiatorImpl;
import com.blazebit.persistence.impl.ParameterManager;
import com.blazebit.persistence.impl.SubqueryAndExpressionBuilderListener;
import com.blazebit.persistence.impl.SubqueryInitiatorFactory;
import com.blazebit.persistence.impl.builder.expression.CaseWhenBuilderImpl;
import com.blazebit.persistence.impl.builder.expression.EscapeBuilderImpl;
import com.blazebit.persistence.impl.builder.expression.ExpressionBuilder;
import com.blazebit.persistence.impl.builder.expression.ExpressionBuilderEndedListener;
import com.blazebit.persistence.impl.builder.expression.SimpleCaseWhenBuilderImpl;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.ExpressionFactory;
import com.blazebit.persistence.parser.predicate.LikePredicate;
import com.blazebit.persistence.parser.predicate.PredicateBuilder;

/**
 *
 * @author Moritz Becker
 * @since 1.0.0
 */
public class LikeBuilderImpl<T> extends SubqueryAndExpressionBuilderListener<T> implements LikeBuilder<T>, PredicateBuilder {

    private final Expression leftExpression;
    private final ExpressionFactory expressionFactory;
    private final ParameterManager parameterManager;
    private final ClauseType clauseType;
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

    public LikeBuilderImpl(T result, PredicateBuilderEndedListener listener, Expression leftExpression, ExpressionFactory expressionFactory, ParameterManager parameterManager, SubqueryInitiatorFactory subqueryInitFactory, ClauseType clauseType, boolean negated, boolean caseSensitive) {
        this.result = result;
        this.listener = listener;
        this.leftExpression = leftExpression;
        this.expressionFactory = expressionFactory;
        this.parameterManager = parameterManager;
        this.clauseType = clauseType;
        this.subqueryInitFactory = subqueryInitFactory;
        this.negated = negated;
        this.caseSensitive = caseSensitive;
    }

    @Override
    public EscapeBuilder<T> value(Object value) {
        if (value == null) {
            throw new NullPointerException("value");
        }
        patternExpression = parameterManager.addParameterExpression(value, clauseType, subqueryInitFactory.getQueryBuilder());
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
    public MultipleSubqueryInitiator<EscapeBuilder<T>> subqueries(String expression) {
        Expression expr = expressionFactory.createStringExpression(expression);
        ExpressionToEscapeBuilderEndedListener listener = new ExpressionToEscapeBuilderEndedListener();
        return new MultipleSubqueryInitiatorImpl<EscapeBuilder<T>>(new EscapeBuilderImpl<T>(listener, result), expr, listener, subqueryInitFactory, clauseType);
    }

    /**
     * @author Christian Beikov
     * @since 1.0.0
     */
    private class ExpressionToEscapeBuilderEndedListener extends EscapeBuilderImpl.EscapeBuilderImplEndedListener implements ExpressionBuilderEndedListener {

        private Expression patternExpression;
        
        @Override
        public void onBuilderEnded(ExpressionBuilder builder) {
            patternExpression = builder.getExpression();
        }
        
        @Override
        public void onBuilderEnded(EscapeBuilderImpl<?> builder) {
            super.onBuilderEnded(builder);
            likePredicate = new LikePredicate(leftExpression, patternExpression, caseSensitive, builder.getEscapeCharacter(), negated);
            listener.onBuilderEnded(LikeBuilderImpl.this);
        }
        
    }

    @Override
    public RestrictionBuilder<CaseWhenThenBuilder<CaseWhenBuilder<EscapeBuilder<T>>>> caseWhen(String expression) {
        EscapeBuilder<T> escapeBuilder = escapeBuilderEndedListener.startBuilder(new EscapeBuilderImpl<T>(escapeBuilderEndedListener, result));
        return startBuilder(new CaseWhenBuilderImpl<EscapeBuilder<T>>(escapeBuilder, this, subqueryInitFactory, expressionFactory, parameterManager, clauseType)).when(expression);
    }

    @Override
    public SubqueryInitiator<RestrictionBuilder<CaseWhenThenBuilder<CaseWhenBuilder<EscapeBuilder<T>>>>> caseWhenSubquery() {
        EscapeBuilder<T> escapeBuilder = escapeBuilderEndedListener.startBuilder(new EscapeBuilderImpl<T>(escapeBuilderEndedListener, result));
        return startBuilder(new CaseWhenBuilderImpl<EscapeBuilder<T>>(escapeBuilder, this, subqueryInitFactory, expressionFactory, parameterManager, clauseType)).whenSubquery();
    }

    @Override
    public SubqueryInitiator<RestrictionBuilder<CaseWhenThenBuilder<CaseWhenBuilder<EscapeBuilder<T>>>>> caseWhenSubquery(String subqueryAlias, String expression) {
        EscapeBuilder<T> escapeBuilder = escapeBuilderEndedListener.startBuilder(new EscapeBuilderImpl<T>(escapeBuilderEndedListener, result));
        return startBuilder(new CaseWhenBuilderImpl<EscapeBuilder<T>>(escapeBuilder, this, subqueryInitFactory, expressionFactory, parameterManager, clauseType)).whenSubquery(subqueryAlias, expression);
    }

    @Override
    public MultipleSubqueryInitiator<RestrictionBuilder<CaseWhenThenBuilder<CaseWhenBuilder<EscapeBuilder<T>>>>> caseWhenSubqueries(String expression) {
        EscapeBuilder<T> escapeBuilder = escapeBuilderEndedListener.startBuilder(new EscapeBuilderImpl<T>(escapeBuilderEndedListener, result));
        return startBuilder(new CaseWhenBuilderImpl<EscapeBuilder<T>>(escapeBuilder, this, subqueryInitFactory, expressionFactory, parameterManager, clauseType)).whenSubqueries(expression);
    }

    @Override
    public SubqueryBuilder<RestrictionBuilder<CaseWhenThenBuilder<CaseWhenBuilder<EscapeBuilder<T>>>>> caseWhenSubquery(FullQueryBuilder<?, ?> criteriaBuilder) {
        EscapeBuilder<T> escapeBuilder = escapeBuilderEndedListener.startBuilder(new EscapeBuilderImpl<T>(escapeBuilderEndedListener, result));
        return startBuilder(new CaseWhenBuilderImpl<EscapeBuilder<T>>(escapeBuilder, this, subqueryInitFactory, expressionFactory, parameterManager, clauseType)).whenSubquery(criteriaBuilder);
    }

    @Override
    public SubqueryBuilder<RestrictionBuilder<CaseWhenThenBuilder<CaseWhenBuilder<EscapeBuilder<T>>>>> caseWhenSubquery(String subqueryAlias, String expression, FullQueryBuilder<?, ?> criteriaBuilder) {
        EscapeBuilder<T> escapeBuilder = escapeBuilderEndedListener.startBuilder(new EscapeBuilderImpl<T>(escapeBuilderEndedListener, result));
        return startBuilder(new CaseWhenBuilderImpl<EscapeBuilder<T>>(escapeBuilder, this, subqueryInitFactory, expressionFactory, parameterManager, clauseType)).whenSubquery(subqueryAlias, expression, criteriaBuilder);
    }

    @Override
    public SubqueryInitiator<CaseWhenThenBuilder<CaseWhenBuilder<EscapeBuilder<T>>>> caseWhenExists() {
        EscapeBuilder<T> escapeBuilder = escapeBuilderEndedListener.startBuilder(new EscapeBuilderImpl<T>(escapeBuilderEndedListener, result));
        return startBuilder(new CaseWhenBuilderImpl<EscapeBuilder<T>>(escapeBuilder, this, subqueryInitFactory, expressionFactory, parameterManager, clauseType)).whenExists();
    }

    @Override
    public SubqueryInitiator<CaseWhenThenBuilder<CaseWhenBuilder<EscapeBuilder<T>>>> caseWhenNotExists() {
        EscapeBuilder<T> escapeBuilder = escapeBuilderEndedListener.startBuilder(new EscapeBuilderImpl<T>(escapeBuilderEndedListener, result));
        return startBuilder(new CaseWhenBuilderImpl<EscapeBuilder<T>>(escapeBuilder, this, subqueryInitFactory, expressionFactory, parameterManager, clauseType)).whenNotExists();
    }

    @Override
    public SubqueryBuilder<CaseWhenThenBuilder<CaseWhenBuilder<EscapeBuilder<T>>>> caseWhenExists(FullQueryBuilder<?, ?> criteriaBuilder) {
        EscapeBuilder<T> escapeBuilder = escapeBuilderEndedListener.startBuilder(new EscapeBuilderImpl<T>(escapeBuilderEndedListener, result));
        return startBuilder(new CaseWhenBuilderImpl<EscapeBuilder<T>>(escapeBuilder, this, subqueryInitFactory, expressionFactory, parameterManager, clauseType)).whenExists(criteriaBuilder);
    }

    @Override
    public SubqueryBuilder<CaseWhenThenBuilder<CaseWhenBuilder<EscapeBuilder<T>>>> caseWhenNotExists(FullQueryBuilder<?, ?> criteriaBuilder) {
        EscapeBuilder<T> escapeBuilder = escapeBuilderEndedListener.startBuilder(new EscapeBuilderImpl<T>(escapeBuilderEndedListener, result));
        return startBuilder(new CaseWhenBuilderImpl<EscapeBuilder<T>>(escapeBuilder, this, subqueryInitFactory, expressionFactory, parameterManager, clauseType)).whenNotExists(criteriaBuilder);
    }

    @Override
    public CaseWhenAndThenBuilder<CaseWhenBuilder<EscapeBuilder<T>>> caseWhenAnd() {
        EscapeBuilder<T> escapeBuilder = escapeBuilderEndedListener.startBuilder(new EscapeBuilderImpl<T>(escapeBuilderEndedListener, result));
        return startBuilder(new CaseWhenBuilderImpl<EscapeBuilder<T>>(escapeBuilder, this, subqueryInitFactory, expressionFactory, parameterManager, clauseType)).whenAnd();
    }

    @Override
    public CaseWhenOrThenBuilder<CaseWhenBuilder<EscapeBuilder<T>>> caseWhenOr() {
        EscapeBuilder<T> escapeBuilder = escapeBuilderEndedListener.startBuilder(new EscapeBuilderImpl<T>(escapeBuilderEndedListener, result));
        return startBuilder(new CaseWhenBuilderImpl<EscapeBuilder<T>>(escapeBuilder, this, subqueryInitFactory, expressionFactory, parameterManager, clauseType)).whenOr();
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
