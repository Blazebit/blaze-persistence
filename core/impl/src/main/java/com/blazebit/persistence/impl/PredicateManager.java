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
package com.blazebit.persistence.impl;

import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.impl.expression.Expressions;
import com.blazebit.persistence.impl.predicate.AndPredicate;
import com.blazebit.persistence.impl.predicate.BetweenPredicate;
import com.blazebit.persistence.impl.predicate.GePredicate;
import com.blazebit.persistence.impl.predicate.InSubqueryPredicate;
import com.blazebit.persistence.impl.predicate.LikePredicate;
import com.blazebit.persistence.impl.predicate.Predicate;
import com.blazebit.persistence.impl.predicate.PredicateBuilder;

/**
 *
 * @author ccbem
 */
public abstract class PredicateManager<U> extends AbstractManager {

    final RootPredicate rootPredicate;

    PredicateManager(QueryGenerator queryGenerator, ParameterManager parameterManager) {
        super(queryGenerator, parameterManager);
        this.rootPredicate = new RootPredicate();
    }

    RootPredicate getRootPredicate() {
        return rootPredicate;
    }

    RestrictionBuilder<U> restrict(AbstractBaseQueryBuilder<?, ?> builder, String expression) {
        return rootPredicate.startBuilder(new RestrictionBuilderImpl<U>((U) builder, rootPredicate, Expressions.createSimpleExpression(expression)));
    }

    void applyTransformer(ArrayExpressionTransformer transformer) {
        // carry out transformations
        rootPredicate.predicate.accept(new ArrayTransformationVisitor(transformer));
    }

    void verifyBuilderEnded() {
        rootPredicate.verifyBuilderEnded();
    }

    void acceptVisitor(Predicate.Visitor v) {
        rootPredicate.predicate.accept(v);
    }

    String buildClause() {
        StringBuilder sb = new StringBuilder();
        queryGenerator.setQueryBuffer(sb);
        applyPredicate(queryGenerator, sb);
        return sb.toString();
    }

    protected abstract String getClauseName();

    void applyPredicate(QueryGenerator queryGenerator, StringBuilder sb) {
        if (rootPredicate.predicate.getChildren().isEmpty()) {
            return;
        }
        sb.append(' ').append(getClauseName()).append(' ');
        rootPredicate.predicate.accept(queryGenerator);
    }

    class RootPredicate extends AbstractBuilderEndedListener {

        final AndPredicate predicate;

        public RootPredicate() {
            this.predicate = new AndPredicate();
        }

        @Override
        public void onBuilderEnded(PredicateBuilder builder) {
            super.onBuilderEnded(builder);
            Predicate pred = builder.getPredicate();

            // register parameter expressions
            registerParameterExpressions(pred);
            
            predicate.getChildren()
                    .add(pred);
        }
    }

    private static class ArrayTransformationVisitor extends VisitorAdapter {

        private final ArrayExpressionTransformer transformer;

        public ArrayTransformationVisitor(ArrayExpressionTransformer transformer) {
            this.transformer = transformer;
        }

        @Override
        public void visit(BetweenPredicate predicate) {
            predicate.setStart(transformer.transform(predicate.getStart()));
            predicate.setLeft(transformer.transform(predicate.getLeft()));
            predicate.setEnd(transformer.transform(predicate.getEnd()));
        }

        @Override
        public void visit(GePredicate predicate) {
            predicate.setLeft(transformer.transform(predicate.getLeft()));
            predicate.setRight(transformer.transform(predicate.getRight()));
        }

        @Override
        public void visit(LikePredicate predicate) {
            predicate.setLeft(transformer.transform(predicate.getLeft()));
            predicate.setRight(transformer.transform(predicate.getRight()));
        }
    }
}
