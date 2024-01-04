/*
 * Copyright 2014 - 2024 Blazebit.
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
import com.blazebit.persistence.PredicateBuilder;
import com.blazebit.persistence.PredicateOrBuilder;
import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.SimpleCaseWhenStarterBuilder;
import com.blazebit.persistence.SubqueryBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.WhereBuilder;
import com.blazebit.persistence.WhereOrBuilder;
import com.blazebit.persistence.impl.builder.expression.CaseWhenBuilderImpl;
import com.blazebit.persistence.impl.builder.expression.ExpressionBuilder;
import com.blazebit.persistence.impl.builder.expression.ExpressionBuilderEndedListener;
import com.blazebit.persistence.impl.builder.expression.SimpleCaseWhenBuilderImpl;
import com.blazebit.persistence.impl.builder.predicate.CaseExpressionBuilderListener;
import com.blazebit.persistence.impl.builder.predicate.LeftHandsideSubqueryPredicateBuilderListener;
import com.blazebit.persistence.impl.builder.predicate.PredicateOrBuilderImpl;
import com.blazebit.persistence.impl.builder.predicate.RestrictionBuilderImpl;
import com.blazebit.persistence.impl.builder.predicate.RightHandsideSubqueryPredicateBuilder;
import com.blazebit.persistence.impl.builder.predicate.RootPredicate;
import com.blazebit.persistence.impl.builder.predicate.SuperExpressionLeftHandsideSubqueryPredicateBuilder;
import com.blazebit.persistence.parser.SimpleQueryGenerator;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.ExpressionCopyContext;
import com.blazebit.persistence.parser.expression.ExpressionFactory;
import com.blazebit.persistence.parser.expression.modifier.ExpressionModifier;
import com.blazebit.persistence.parser.predicate.CompoundPredicate;
import com.blazebit.persistence.parser.predicate.ExistsPredicate;
import com.blazebit.persistence.parser.predicate.Predicate;
import com.blazebit.persistence.impl.transform.ExpressionModifierVisitor;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author Moritz Becker
 * @since 1.0.0
 */
public abstract class PredicateManager<T extends WhereBuilder<T>> extends AbstractManager<ExpressionModifier> implements PredicateBuilder, WhereBuilder<T> {

    protected final ExpressionFactory expressionFactory;
    protected final RootPredicate rootPredicate;
    private final SubqueryBuilderListenerImpl<?> leftSubqueryPredicateBuilderListener = new LeftHandsideSubqueryPredicateBuilderListener<>();
    private SubqueryBuilderListenerImpl<?> rightSubqueryPredicateBuilderListener;
    private SubqueryBuilderListenerImpl<?> superExprLeftSubqueryPredicateBuilderListener;
    private CaseExpressionBuilderListener caseExpressionBuilderListener;
    private MultipleSubqueryInitiator<?> currentMultipleSubqueryInitiator;

    PredicateManager(ResolvingQueryGenerator queryGenerator, ParameterManager parameterManager, SubqueryInitiatorFactory subqueryInitFactory, ExpressionFactory expressionFactory) {
        super(queryGenerator, parameterManager, subqueryInitFactory);
        this.rootPredicate = new RootPredicate(parameterManager, getClauseType(), subqueryInitFactory.getQueryBuilder());
        this.expressionFactory = expressionFactory;
    }

    void applyFrom(PredicateManager predicateManager, ExpressionCopyContext copyContext) {
        rootPredicate.getPredicate().getChildren().addAll(subqueryInitFactory.reattachSubqueries(predicateManager.rootPredicate.getPredicate().copy(copyContext), getClauseType()).getChildren());
    }

    <X> RestrictionBuilder<X> restrict(X builder, Expression expr) {
        return rootPredicate.startBuilder(new RestrictionBuilderImpl<X>(builder, rootPredicate, expr, subqueryInitFactory, expressionFactory, parameterManager, getClauseType()));
    }

    <X> CaseWhenStarterBuilder<RestrictionBuilder<X>> restrictCase(X builder) {
        RestrictionBuilderImpl<X> restrictionBuilder = rootPredicate.startBuilder(new RestrictionBuilderImpl<X>(builder, rootPredicate, subqueryInitFactory, expressionFactory, parameterManager, getClauseType()));
        caseExpressionBuilderListener = new CaseExpressionBuilderListener(restrictionBuilder);
        return caseExpressionBuilderListener.startBuilder(new CaseWhenBuilderImpl<RestrictionBuilder<X>>(restrictionBuilder, caseExpressionBuilderListener, subqueryInitFactory, expressionFactory, parameterManager, getClauseType()));
    }

    void restrictExpression(Predicate predicate) {
        rootPredicate.verifyBuilderEnded();
        parameterManager.collectParameterRegistrations(predicate, getClauseType(), subqueryInitFactory.getQueryBuilder());
        List<Predicate> children = rootPredicate.getPredicate().getChildren();
        if (predicate instanceof CompoundPredicate) {
            CompoundPredicate compoundPredicate = (CompoundPredicate) predicate;
            if (compoundPredicate.getOperator() == CompoundPredicate.BooleanOperator.AND ^ compoundPredicate.isNegated()) {
                children.addAll(compoundPredicate.getChildren());
            } else {
                children.add(predicate);
            }
        } else {
            children.add(predicate);
        }
    }

    void restrictSetExpression(Predicate predicate) {
        rootPredicate.verifyBuilderEnded();
        parameterManager.collectParameterRegistrations(predicate, getClauseType(), subqueryInitFactory.getQueryBuilder());

        List<Predicate> children = rootPredicate.getPredicate().getChildren();
        children.clear();
        if (predicate instanceof CompoundPredicate) {
            CompoundPredicate compoundPredicate = (CompoundPredicate) predicate;
            if (compoundPredicate.getOperator() == CompoundPredicate.BooleanOperator.AND ^ compoundPredicate.isNegated()) {
                children.addAll(compoundPredicate.getChildren());
            } else {
                children.add(predicate);
            }
        } else {
            children.add(predicate);
        }
    }

    <X> SimpleCaseWhenStarterBuilder<RestrictionBuilder<X>> restrictSimpleCase(X builder, Expression caseOperand) {
        RestrictionBuilderImpl<X> restrictionBuilder = rootPredicate.startBuilder(new RestrictionBuilderImpl<X>(builder, rootPredicate, subqueryInitFactory, expressionFactory, parameterManager, getClauseType()));
        caseExpressionBuilderListener = new CaseExpressionBuilderListener(restrictionBuilder);
        return caseExpressionBuilderListener.startBuilder(new SimpleCaseWhenBuilderImpl<RestrictionBuilder<X>>(restrictionBuilder, caseExpressionBuilderListener, expressionFactory, caseOperand, subqueryInitFactory, parameterManager, getClauseType()));
    }

    <X> SubqueryInitiator<RestrictionBuilder<X>> restrict(X builder) {
        RestrictionBuilder<X> restrictionBuilder = rootPredicate.startBuilder(new RestrictionBuilderImpl<X>(builder, rootPredicate, subqueryInitFactory, expressionFactory, parameterManager, getClauseType()));
        //noinspection unchecked
        return subqueryInitFactory.createSubqueryInitiator(restrictionBuilder, (SubqueryBuilderListener<RestrictionBuilder<X>>) leftSubqueryPredicateBuilderListener, false, getClauseType());
    }

    <X> SubqueryBuilder<RestrictionBuilder<X>> restrict(X builder, FullQueryBuilder<?, ?> criteriaBuilder) {
        RestrictionBuilder<X> restrictionBuilder = rootPredicate.startBuilder(new RestrictionBuilderImpl<X>(builder, rootPredicate, subqueryInitFactory, expressionFactory, parameterManager, getClauseType()));
        //noinspection unchecked
        return subqueryInitFactory.createSubqueryBuilder(restrictionBuilder, (SubqueryBuilderListener<RestrictionBuilder<X>>) leftSubqueryPredicateBuilderListener, false, criteriaBuilder, getClauseType());
    }

    <X> MultipleSubqueryInitiator<RestrictionBuilder<X>> restrictSubqueries(X builder, String expression) {
        Expression expr = expressionFactory.createSimpleExpression(expression, true);
        RestrictionBuilderImpl<X> restrictionBuilder = rootPredicate.startBuilder(new RestrictionBuilderImpl<X>(builder, rootPredicate, subqueryInitFactory, expressionFactory, parameterManager, getClauseType()));
        // We don't need a listener or marker here, because the resulting restriction builder can only be ended, when the initiator is ended
        MultipleSubqueryInitiator<RestrictionBuilder<X>> initiator = new MultipleSubqueryInitiatorImpl<RestrictionBuilder<X>>(restrictionBuilder, expr, new RestrictionBuilderExpressionBuilderListener(restrictionBuilder), subqueryInitFactory, getClauseType());
        return initiator;
    }

    <X> MultipleSubqueryInitiator<X> restrictExpressionSubqueries(X result, Predicate predicate) {
        rootPredicate.verifyBuilderEnded();
        parameterManager.collectParameterRegistrations(predicate, getClauseType(), subqueryInitFactory.getQueryBuilder());

        MultipleSubqueryInitiator<X> initiator = new MultipleSubqueryInitiatorImpl<X>(result, predicate, new ExpressionBuilderEndedListener() {
            
            @Override
            public void onBuilderEnded(ExpressionBuilder builder) {
                rootPredicate.getPredicate().getChildren().add((Predicate) builder.getExpression());
                currentMultipleSubqueryInitiator = null;
            }
            
        }, subqueryInitFactory, getClauseType());
        currentMultipleSubqueryInitiator = initiator;
        return initiator;
    }

    <X> MultipleSubqueryInitiator<X> restrictSetExpressionSubqueries(X result, Predicate predicate, final ExpressionBuilderEndedListener listener) {
        rootPredicate.verifyBuilderEnded();
        parameterManager.collectParameterRegistrations(predicate, getClauseType(), subqueryInitFactory.getQueryBuilder());

        MultipleSubqueryInitiator<X> initiator = new MultipleSubqueryInitiatorImpl<X>(result, predicate, new ExpressionBuilderEndedListener() {

            @Override
            public void onBuilderEnded(ExpressionBuilder builder) {
                List<Predicate> children = rootPredicate.getPredicate().getChildren();
                children.clear();
                children.add((Predicate) builder.getExpression());
                currentMultipleSubqueryInitiator = null;
                if (listener != null) {
                    listener.onBuilderEnded(builder);
                }
            }

        }, subqueryInitFactory, getClauseType());
        currentMultipleSubqueryInitiator = initiator;
        return initiator;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    <X> SubqueryInitiator<RestrictionBuilder<X>> restrict(X builder, String subqueryAlias, String expression) {
        Expression expr = expressionFactory.createSimpleExpression(expression, true);
        superExprLeftSubqueryPredicateBuilderListener = new SuperExpressionLeftHandsideSubqueryPredicateBuilder(subqueryAlias, expr);
        RestrictionBuilder<X> restrictionBuilder = rootPredicate.startBuilder(new RestrictionBuilderImpl<X>(builder, rootPredicate, subqueryInitFactory, expressionFactory, parameterManager, getClauseType()));
        return subqueryInitFactory.createSubqueryInitiator(restrictionBuilder, (SubqueryBuilderListener<RestrictionBuilder<X>>) superExprLeftSubqueryPredicateBuilderListener, false, getClauseType());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    <X> SubqueryBuilder<RestrictionBuilder<X>> restrict(X builder, String subqueryAlias, String expression, FullQueryBuilder<?, ?> criteriaBuilder) {
        Expression expr = expressionFactory.createSimpleExpression(expression, true);
        superExprLeftSubqueryPredicateBuilderListener = new SuperExpressionLeftHandsideSubqueryPredicateBuilder(subqueryAlias, expr);
        RestrictionBuilder<X> restrictionBuilder = rootPredicate.startBuilder(new RestrictionBuilderImpl<X>(builder, rootPredicate, subqueryInitFactory, expressionFactory, parameterManager, getClauseType()));
        return subqueryInitFactory.createSubqueryBuilder(restrictionBuilder, (SubqueryBuilderListener<RestrictionBuilder<X>>) superExprLeftSubqueryPredicateBuilderListener, false, criteriaBuilder, getClauseType());
    }

    <X> SubqueryInitiator<X> restrictExists(X result) {
        rightSubqueryPredicateBuilderListener = rootPredicate.startBuilder(new RightHandsideSubqueryPredicateBuilder<X>(rootPredicate, new ExistsPredicate()));
        //noinspection unchecked
        return subqueryInitFactory.createSubqueryInitiator(result, (SubqueryBuilderListener<X>) rightSubqueryPredicateBuilderListener, true, getClauseType());
    }

    <X> SubqueryInitiator<X> restrictNotExists(X result) {
        rightSubqueryPredicateBuilderListener = rootPredicate.startBuilder(new RightHandsideSubqueryPredicateBuilder<X>(rootPredicate, new ExistsPredicate(true)));
        //noinspection unchecked
        return subqueryInitFactory.createSubqueryInitiator(result, (SubqueryBuilderListener<X>) rightSubqueryPredicateBuilderListener, true, getClauseType());
    }

    <X> SubqueryBuilder<X> restrictExists(X result, FullQueryBuilder<?, ?> criteriaBuilder) {
        rightSubqueryPredicateBuilderListener = rootPredicate.startBuilder(new RightHandsideSubqueryPredicateBuilder<X>(rootPredicate, new ExistsPredicate()));
        //noinspection unchecked
        return subqueryInitFactory.createSubqueryBuilder(result, (SubqueryBuilderListener<X>) rightSubqueryPredicateBuilderListener, true, criteriaBuilder, getClauseType());
    }

    <X> SubqueryBuilder<X> restrictNotExists(X result, FullQueryBuilder<?, ?> criteriaBuilder) {
        rightSubqueryPredicateBuilderListener = rootPredicate.startBuilder(new RightHandsideSubqueryPredicateBuilder<X>(rootPredicate, new ExistsPredicate(true)));
        //noinspection unchecked
        return subqueryInitFactory.createSubqueryBuilder(result, (SubqueryBuilderListener<X>) rightSubqueryPredicateBuilderListener, true, criteriaBuilder, getClauseType());
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
        buildClause(sb, Collections.<String>emptyList(), Collections.<String>emptyList());
    }

    void buildClause(StringBuilder sb, List<String> additionalConjuncts, List<String> optionalConjuncts) {
        if (!hasPredicates() && additionalConjuncts.isEmpty()) {
            return;
        }

        int initialLength = sb.length();
        sb.append(' ').append(getClauseName()).append(' ');
        int oldLength = sb.length();
        buildClausePredicate(sb, additionalConjuncts, optionalConjuncts);
        if (sb.length() == oldLength) {
            sb.setLength(initialLength);
        }
    }

    void buildClausePredicate(StringBuilder sb, List<String> additionalConjuncts, List<String> optionalConjuncts) {
        int size = additionalConjuncts.size();
        boolean hasPredicates = size > 0;
        for (int i = 0; i < size; i++) {
            sb.append(additionalConjuncts.get(i));
            sb.append(" AND ");
        }

        queryGenerator.setClauseType(getClauseType());
        queryGenerator.setQueryBuffer(sb);
        int oldLength = sb.length();
        applyPredicate(queryGenerator);
        queryGenerator.setClauseType(null);
        if (sb.length() == oldLength) {
            if (size > 0) {
                sb.setLength(sb.length() - " AND ".length());
            }
        } else {
            hasPredicates = true;
        }

        if (hasPredicates) {
            for (int i = 0; i < optionalConjuncts.size(); i++) {
                sb.append(" AND ");
                sb.append(optionalConjuncts.get(i));
            }
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

    @Override
    public SubqueryInitiator<RestrictionBuilder<PredicateBuilder>> subquery() {
        return restrict((PredicateBuilder) this);
    }

    @Override
    public SubqueryInitiator<RestrictionBuilder<PredicateBuilder>> subquery(String subqueryAlias, String expression) {
        return restrict((PredicateBuilder) this, subqueryAlias, expression);
    }

    @Override
    public MultipleSubqueryInitiator<RestrictionBuilder<PredicateBuilder>> subqueries(String expression) {
        return restrictSubqueries((PredicateBuilder) this, expression);
    }

    @Override
    public SubqueryBuilder<RestrictionBuilder<PredicateBuilder>> subquery(FullQueryBuilder<?, ?> criteriaBuilder) {
        return restrict((PredicateBuilder) this, criteriaBuilder);
    }

    @Override
    public SubqueryBuilder<RestrictionBuilder<PredicateBuilder>> subquery(String subqueryAlias, String expression, FullQueryBuilder<?, ?> criteriaBuilder) {
        return restrict((PredicateBuilder) this, subqueryAlias, expression, criteriaBuilder);
    }

    @Override
    public PredicateBuilder withExpression(String expression) {
        Predicate predicate = expressionFactory.createBooleanExpression(expression, false);
        restrictExpression(predicate);
        return this;
    }

    @Override
    public MultipleSubqueryInitiator<PredicateBuilder> withExpressionSubqueries(String expression) {
        Predicate predicate = expressionFactory.createBooleanExpression(expression, false);
        return restrictExpressionSubqueries((PredicateBuilder) this, predicate);
    }

    @Override
    public RestrictionBuilder<PredicateBuilder> expression(String expression) {
        Expression expr = expressionFactory.createSimpleExpression(expression, false);
        return restrict((PredicateBuilder) this, expr);
    }

    @Override
    public CaseWhenStarterBuilder<RestrictionBuilder<PredicateBuilder>> selectCase() {
        return restrictCase((PredicateBuilder) this);
    }

    @Override
    public SimpleCaseWhenStarterBuilder<RestrictionBuilder<PredicateBuilder>> selectCase(String expression) {
        Expression expr = expressionFactory.createSimpleExpression(expression, false);
        return restrictSimpleCase((PredicateBuilder) this, expr);
    }

    @Override
    public SubqueryInitiator<PredicateBuilder> exists() {
        return restrictExists((PredicateBuilder) this);
    }

    @Override
    public SubqueryInitiator<PredicateBuilder> notExists() {
        return restrictNotExists((PredicateBuilder) this);
    }

    @Override
    public SubqueryBuilder<PredicateBuilder> exists(FullQueryBuilder<?, ?> criteriaBuilder) {
        return restrictExists((PredicateBuilder) this, criteriaBuilder);
    }

    @Override
    public SubqueryBuilder<PredicateBuilder> notExists(FullQueryBuilder<?, ?> criteriaBuilder) {
        return restrictNotExists((PredicateBuilder) this, criteriaBuilder);
    }

    @Override
    public PredicateOrBuilder<PredicateBuilder> or() {
        return rootPredicate.startBuilder(new PredicateOrBuilderImpl<PredicateBuilder>(this, rootPredicate, getClauseType(), subqueryInitFactory, expressionFactory, parameterManager));
    }

    @Override
    public PredicateBuilder setExpression(String expression) {
        Predicate predicate = expressionFactory.createBooleanExpression(expression, false);
        restrictSetExpression(predicate);
        return this;
    }

    @Override
    public MultipleSubqueryInitiator<PredicateBuilder> setExpressionSubqueries(String expression) {
        Predicate predicate = expressionFactory.createBooleanExpression(expression, false);
        return restrictSetExpressionSubqueries((PredicateBuilder) this, predicate, null);
    }

    public void verifyEnded() {
        rootPredicate.verifyBuilderEnded();
        if (currentMultipleSubqueryInitiator != null) {
            throw new BuilderChainingException("A builder was not ended properly.");
        }
    }

    // todo: the following WhereBuilder implementation is just a temporary hack
    //  with https://github.com/Blazebit/blaze-persistence/issues/1596 we will remove this

    @Override
    public SubqueryInitiator<RestrictionBuilder<T>> whereSubquery() {
        return (SubqueryInitiator) subquery();
    }

    @Override
    public SubqueryInitiator<RestrictionBuilder<T>> whereSubquery(String subqueryAlias, String expression) {
        return (SubqueryInitiator) subquery(subqueryAlias, expression);
    }

    @Override
    public MultipleSubqueryInitiator<RestrictionBuilder<T>> whereSubqueries(String expression) {
        return (MultipleSubqueryInitiator) subqueries(expression);
    }

    @Override
    public SubqueryBuilder<RestrictionBuilder<T>> whereSubquery(FullQueryBuilder<?, ?> criteriaBuilder) {
        return (SubqueryBuilder) subquery(criteriaBuilder);
    }

    @Override
    public SubqueryBuilder<RestrictionBuilder<T>> whereSubquery(String subqueryAlias, String expression, FullQueryBuilder<?, ?> criteriaBuilder) {
        return (SubqueryBuilder) subquery(subqueryAlias, expression, criteriaBuilder);
    }

    @Override
    public T whereExpression(String expression) {
        return (T) withExpression(expression);
    }

    @Override
    public MultipleSubqueryInitiator<T> whereExpressionSubqueries(String expression) {
        return (MultipleSubqueryInitiator) withExpressionSubqueries(expression);
    }

    @Override
    public RestrictionBuilder<T> where(String expression) {
        return (RestrictionBuilder) expression(expression);
    }

    @Override
    public CaseWhenStarterBuilder<RestrictionBuilder<T>> whereCase() {
        return (CaseWhenStarterBuilder) selectCase();
    }

    @Override
    public SimpleCaseWhenStarterBuilder<RestrictionBuilder<T>> whereSimpleCase(String expression) {
        return (SimpleCaseWhenStarterBuilder) selectCase(expression);
    }

    @Override
    public SubqueryInitiator<T> whereExists() {
        return (SubqueryInitiator) exists();
    }

    @Override
    public SubqueryInitiator<T> whereNotExists() {
        return (SubqueryInitiator) notExists();
    }

    @Override
    public SubqueryBuilder<T> whereExists(FullQueryBuilder<?, ?> criteriaBuilder) {
        return (SubqueryBuilder) exists(criteriaBuilder);
    }

    @Override
    public SubqueryBuilder<T> whereNotExists(FullQueryBuilder<?, ?> criteriaBuilder) {
        return (SubqueryBuilder) notExists(criteriaBuilder);
    }

    @Override
    public WhereOrBuilder<T> whereOr() {
        return (WhereOrBuilder) or();
    }

    @Override
    public T setWhereExpression(String expression) {
        setExpression(expression);
        return (T) this;
    }

    @Override
    public MultipleSubqueryInitiator<T> setWhereExpressionSubqueries(String expression) {
        return (MultipleSubqueryInitiator) setExpressionSubqueries(expression);
    }

    @Override
    public PredicateBuilder where() {
        return this;
    }


    // TODO: needs equals-hashCode implementation

}
