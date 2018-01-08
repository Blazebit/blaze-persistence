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

import com.blazebit.persistence.CaseWhenStarterBuilder;
import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.JoinOnBuilder;
import com.blazebit.persistence.MultipleSubqueryInitiator;
import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.SimpleCaseWhenStarterBuilder;
import com.blazebit.persistence.SubqueryBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.impl.builder.expression.CaseWhenBuilderImpl;
import com.blazebit.persistence.impl.builder.expression.ExpressionBuilder;
import com.blazebit.persistence.impl.builder.expression.ExpressionBuilderEndedListener;
import com.blazebit.persistence.impl.builder.expression.SimpleCaseWhenBuilderImpl;
import com.blazebit.persistence.impl.builder.predicate.CaseExpressionBuilderListener;
import com.blazebit.persistence.impl.builder.predicate.JoinOnBuilderImpl;
import com.blazebit.persistence.impl.builder.predicate.LeftHandsideSubqueryPredicateBuilderListener;
import com.blazebit.persistence.impl.builder.predicate.RestrictionBuilderImpl;
import com.blazebit.persistence.impl.builder.predicate.RightHandsideSubqueryPredicateBuilder;
import com.blazebit.persistence.impl.builder.predicate.RootPredicate;
import com.blazebit.persistence.impl.builder.predicate.SuperExpressionLeftHandsideSubqueryPredicateBuilder;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.impl.expression.modifier.ExpressionModifier;
import com.blazebit.persistence.impl.predicate.ExistsPredicate;
import com.blazebit.persistence.impl.predicate.Predicate;
import com.blazebit.persistence.impl.transform.ExpressionModifierVisitor;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author Moritz Becker
 * @since 1.0
 */
public abstract class PredicateManager<T> extends AbstractManager<ExpressionModifier> {

    protected final ExpressionFactory expressionFactory;
    protected final RootPredicate rootPredicate;
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private final SubqueryBuilderListenerImpl<RestrictionBuilder<T>> leftSubqueryPredicateBuilderListener = new LeftHandsideSubqueryPredicateBuilderListener();
    private SubqueryBuilderListenerImpl<T> rightSubqueryPredicateBuilderListener;
    private SubqueryBuilderListenerImpl<RestrictionBuilder<T>> superExprLeftSubqueryPredicateBuilderListener;
    private CaseExpressionBuilderListener caseExpressionBuilderListener;
    private MultipleSubqueryInitiator<?> currentMultipleSubqueryInitiator;

    PredicateManager(ResolvingQueryGenerator queryGenerator, ParameterManager parameterManager, SubqueryInitiatorFactory subqueryInitFactory, ExpressionFactory expressionFactory) {
        super(queryGenerator, parameterManager, subqueryInitFactory);
        this.rootPredicate = new RootPredicate(parameterManager, getClauseType());
        this.expressionFactory = expressionFactory;
    }

    void applyFrom(PredicateManager predicateManager) {
        rootPredicate.getPredicate().getChildren().addAll(subqueryInitFactory.reattachSubqueries(predicateManager.rootPredicate.getPredicate().clone(true)).getChildren());
    }

    @SuppressWarnings("unchecked")
    RestrictionBuilder<T> restrict(AbstractCommonQueryBuilder<?, ?, ?, ?, ?> builder, Expression expr) {
        return rootPredicate.startBuilder(new RestrictionBuilderImpl<T>((T) builder, rootPredicate, expr, subqueryInitFactory, expressionFactory, parameterManager, getClauseType()));
    }

    CaseWhenStarterBuilder<RestrictionBuilder<T>> restrictCase(AbstractCommonQueryBuilder<?, ?, ?, ?, ?> builder) {
        @SuppressWarnings("unchecked")
        RestrictionBuilder<T> restrictionBuilder = rootPredicate.startBuilder(new RestrictionBuilderImpl<T>((T) builder, rootPredicate, subqueryInitFactory, expressionFactory, parameterManager, getClauseType()));
        caseExpressionBuilderListener = new CaseExpressionBuilderListener((RestrictionBuilderImpl<T>) restrictionBuilder);
        return caseExpressionBuilderListener.startBuilder(new CaseWhenBuilderImpl<RestrictionBuilder<T>>(restrictionBuilder, caseExpressionBuilderListener, subqueryInitFactory, expressionFactory, parameterManager, getClauseType()));
    }

    void restrictExpression(AbstractCommonQueryBuilder<?, ?, ?, ?, ?> builder, Predicate predicate) {
        rootPredicate.verifyBuilderEnded();
        parameterManager.collectParameterRegistrations(predicate, getClauseType());

        List<Predicate> children = rootPredicate.getPredicate().getChildren();
        children.clear();
        children.add(predicate);
    }

    SimpleCaseWhenStarterBuilder<RestrictionBuilder<T>> restrictSimpleCase(AbstractCommonQueryBuilder<?, ?, ?, ?, ?> builder, Expression caseOperand) {
        @SuppressWarnings("unchecked")
        RestrictionBuilder<T> restrictionBuilder = rootPredicate.startBuilder(new RestrictionBuilderImpl<T>((T) builder, rootPredicate, subqueryInitFactory, expressionFactory, parameterManager, getClauseType()));
        caseExpressionBuilderListener = new CaseExpressionBuilderListener((RestrictionBuilderImpl<T>) restrictionBuilder);
        return caseExpressionBuilderListener.startBuilder(new SimpleCaseWhenBuilderImpl<RestrictionBuilder<T>>(restrictionBuilder, caseExpressionBuilderListener, expressionFactory, caseOperand));
    }

    SubqueryInitiator<RestrictionBuilder<T>> restrict(AbstractCommonQueryBuilder<?, ?, ?, ?, ?> builder) {
        @SuppressWarnings("unchecked")
        RestrictionBuilder<T> restrictionBuilder = (RestrictionBuilder<T>) rootPredicate.startBuilder(new RestrictionBuilderImpl<T>((T) builder, rootPredicate, subqueryInitFactory, expressionFactory, parameterManager, getClauseType()));
        return subqueryInitFactory.createSubqueryInitiator(restrictionBuilder, leftSubqueryPredicateBuilderListener, false);
    }

    SubqueryBuilder<RestrictionBuilder<T>> restrict(AbstractCommonQueryBuilder<?, ?, ?, ?, ?> builder, FullQueryBuilder<?, ?> criteriaBuilder) {
        @SuppressWarnings("unchecked")
        RestrictionBuilder<T> restrictionBuilder = (RestrictionBuilder<T>) rootPredicate.startBuilder(new RestrictionBuilderImpl<T>((T) builder, rootPredicate, subqueryInitFactory, expressionFactory, parameterManager, getClauseType()));
        return subqueryInitFactory.createSubqueryBuilder(restrictionBuilder, leftSubqueryPredicateBuilderListener, false, criteriaBuilder);
    }

    MultipleSubqueryInitiator<RestrictionBuilder<T>> restrictSubqueries(AbstractCommonQueryBuilder<?, ?, ?, ?, ?> builder, String expression) {
        Expression expr = expressionFactory.createSimpleExpression(expression, true);
        @SuppressWarnings("unchecked")
        RestrictionBuilderImpl<T> restrictionBuilder = rootPredicate.startBuilder(new RestrictionBuilderImpl<T>((T) builder, rootPredicate, subqueryInitFactory, expressionFactory, parameterManager, getClauseType()));
        // We don't need a listener or marker here, because the resulting restriction builder can only be ended, when the initiator is ended
        MultipleSubqueryInitiator<RestrictionBuilder<T>> initiator = new MultipleSubqueryInitiatorImpl<RestrictionBuilder<T>>(restrictionBuilder, expr, new RestrictionBuilderExpressionBuilderListener(restrictionBuilder), subqueryInitFactory);
        return initiator;
    }

    <X> MultipleSubqueryInitiator<X> restrictExpressionSubqueries(X builder, Predicate predicate) {
        rootPredicate.verifyBuilderEnded();
        parameterManager.collectParameterRegistrations(predicate, getClauseType());

        MultipleSubqueryInitiator<X> initiator = new MultipleSubqueryInitiatorImpl<X>(builder, predicate, new ExpressionBuilderEndedListener() {
            
            @Override
            public void onBuilderEnded(ExpressionBuilder builder) {
                List<Predicate> children = rootPredicate.getPredicate().getChildren();
                children.clear();
                children.add((Predicate) builder.getExpression());
                currentMultipleSubqueryInitiator = null;
            }
            
        }, subqueryInitFactory);
        currentMultipleSubqueryInitiator = initiator;
        return initiator;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    SubqueryInitiator<RestrictionBuilder<T>> restrict(AbstractCommonQueryBuilder<?, ?, ?, ?, ?> builder, String subqueryAlias, String expression) {
        Expression expr = expressionFactory.createSimpleExpression(expression, true);
        superExprLeftSubqueryPredicateBuilderListener = new SuperExpressionLeftHandsideSubqueryPredicateBuilder(subqueryAlias, expr);
        RestrictionBuilder<T> restrictionBuilder = (RestrictionBuilder<T>) rootPredicate.startBuilder(new RestrictionBuilderImpl<T>((T) builder, rootPredicate, subqueryInitFactory, expressionFactory, parameterManager, getClauseType()));
        return subqueryInitFactory.createSubqueryInitiator(restrictionBuilder, superExprLeftSubqueryPredicateBuilderListener, false);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    SubqueryBuilder<RestrictionBuilder<T>> restrict(AbstractCommonQueryBuilder<?, ?, ?, ?, ?> builder, String subqueryAlias, String expression, FullQueryBuilder<?, ?> criteriaBuilder) {
        Expression expr = expressionFactory.createSimpleExpression(expression, true);
        superExprLeftSubqueryPredicateBuilderListener = new SuperExpressionLeftHandsideSubqueryPredicateBuilder(subqueryAlias, expr);
        RestrictionBuilder<T> restrictionBuilder = (RestrictionBuilder<T>) rootPredicate.startBuilder(new RestrictionBuilderImpl<T>((T) builder, rootPredicate, subqueryInitFactory, expressionFactory, parameterManager, getClauseType()));
        return subqueryInitFactory.createSubqueryBuilder(restrictionBuilder, superExprLeftSubqueryPredicateBuilderListener, false, criteriaBuilder);
    }

    SubqueryInitiator<T> restrictExists(T result) {
        rightSubqueryPredicateBuilderListener = rootPredicate.startBuilder(new RightHandsideSubqueryPredicateBuilder<T>(rootPredicate, new ExistsPredicate()));
        return subqueryInitFactory.createSubqueryInitiator(result, rightSubqueryPredicateBuilderListener, true);
    }

    SubqueryInitiator<T> restrictNotExists(T result) {
        rightSubqueryPredicateBuilderListener = rootPredicate.startBuilder(new RightHandsideSubqueryPredicateBuilder<T>(rootPredicate, new ExistsPredicate(true)));
        return subqueryInitFactory.createSubqueryInitiator(result, rightSubqueryPredicateBuilderListener, true);
    }

    SubqueryBuilder<T> restrictExists(T result, FullQueryBuilder<?, ?> criteriaBuilder) {
        rightSubqueryPredicateBuilderListener = rootPredicate.startBuilder(new RightHandsideSubqueryPredicateBuilder<T>(rootPredicate, new ExistsPredicate()));
        return subqueryInitFactory.createSubqueryBuilder(result, rightSubqueryPredicateBuilderListener, true, criteriaBuilder);
    }

    SubqueryBuilder<T> restrictNotExists(T result, FullQueryBuilder<?, ?> criteriaBuilder) {
        rightSubqueryPredicateBuilderListener = rootPredicate.startBuilder(new RightHandsideSubqueryPredicateBuilder<T>(rootPredicate, new ExistsPredicate(true)));
        return subqueryInitFactory.createSubqueryBuilder(result, rightSubqueryPredicateBuilderListener, true, criteriaBuilder);
    }

    @Override
    public void apply(ExpressionModifierVisitor<? super ExpressionModifier> visitor) {
        visitor.visit(rootPredicate, getClauseType());
    }

    void verifyBuilderEnded() {
        rootPredicate.verifyBuilderEnded();
        leftSubqueryPredicateBuilderListener.verifySubqueryBuilderEnded();
        if (rightSubqueryPredicateBuilderListener != null) {
            rightSubqueryPredicateBuilderListener.verifySubqueryBuilderEnded();
        }
        if (superExprLeftSubqueryPredicateBuilderListener != null) {
            superExprLeftSubqueryPredicateBuilderListener.verifySubqueryBuilderEnded();
        }
        if (caseExpressionBuilderListener != null) {
            caseExpressionBuilderListener.verifyBuilderEnded();
        }
        if (currentMultipleSubqueryInitiator != null) {
            throw new BuilderChainingException("A builder was not ended properly.");
        }
    }

    void acceptVisitor(Expression.Visitor v) {
        rootPredicate.getPredicate().accept(v);
    }

    <X> X acceptVisitor(Expression.ResultVisitor<X> v) {
        return rootPredicate.getPredicate().accept(v);
    }

    boolean hasPredicates() {
        return rootPredicate.getPredicate().getChildren().size() > 0;
    }

    void buildClause(StringBuilder sb) {
        buildClause(sb, Collections.<String>emptyList());
    }

    void buildClause(StringBuilder sb, List<String> additionalConjuncts) {
        if (!hasPredicates() && additionalConjuncts.isEmpty()) {
            return;
        }

        queryGenerator.setClauseType(getClauseType());
        queryGenerator.setQueryBuffer(sb);
        sb.append(' ').append(getClauseName()).append(' ');
        int oldLength = sb.length();
        applyPredicate(queryGenerator);
        queryGenerator.setClauseType(null);
        int size = additionalConjuncts.size();
        if (sb.length() != oldLength && size > 0) {
            sb.append(" AND ");
        }

        for (int i = 0; i < size; i++) {
            if (i != 0) {
                sb.append(" AND ");
            }
            sb.append(additionalConjuncts.get(i));
        }
    }

    void buildClausePredicate(StringBuilder sb) {
        buildClausePredicate(sb, Collections.<String>emptyList());
    }

    void buildClausePredicate(StringBuilder sb, List<String> additionalConjuncts) {
        queryGenerator.setClauseType(getClauseType());
        queryGenerator.setQueryBuffer(sb);
        int oldLength = sb.length();
        applyPredicate(queryGenerator);
        queryGenerator.setClauseType(null);
        int size = additionalConjuncts.size();
        if (sb.length() != oldLength && size > 0) {
            sb.append(" AND ");
        }

        for (int i = 0; i < size; i++) {
            if (i != 0) {
                sb.append(" AND ");
            }
            sb.append(additionalConjuncts.get(i));
        }
    }

    protected abstract String getClauseName();

    void applyPredicate(ResolvingQueryGenerator queryGenerator) {
        SimpleQueryGenerator.BooleanLiteralRenderingContext oldBooleanLiteralRenderingContext = queryGenerator.setBooleanLiteralRenderingContext(SimpleQueryGenerator.BooleanLiteralRenderingContext.PREDICATE);
        queryGenerator.generate(rootPredicate.getPredicate());
        queryGenerator.setBooleanLiteralRenderingContext(oldBooleanLiteralRenderingContext);
    }

    public JoinOnBuilder<?> startOnBuilder(AbstractCommonQueryBuilder<?, ?, ?, ?, ?> builder) {
        return rootPredicate.startBuilder(new JoinOnBuilderImpl<Object>(builder, rootPredicate, parameterManager, expressionFactory, subqueryInitFactory));
    }

    // TODO: needs equals-hashCode implementation

}
