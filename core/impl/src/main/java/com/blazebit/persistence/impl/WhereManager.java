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

import com.blazebit.persistence.BaseQueryBuilder;
import com.blazebit.persistence.SubqueryBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.WhereOrBuilder;
import com.blazebit.persistence.impl.predicate.ExistsPredicate;
import com.blazebit.persistence.impl.predicate.NotPredicate;
import com.blazebit.persistence.impl.predicate.Predicate;
import com.blazebit.persistence.impl.predicate.SubqueryPredicate;

/**
 *
 * @author ccbem
 */
public class WhereManager<U> extends PredicateManager<U> {

    WhereManager(QueryGenerator queryGenerator, ParameterManager parameterManager, SubqueryInitiatorFactory subqueryInitFactory) {
        super(queryGenerator, parameterManager, subqueryInitFactory);
    }

    SubqueryBuilderListenerImpl subqueryBuilderListener = new SubqueryBuilderListenerImpl();

    class SubqueryBuilderListenerImpl extends BuilderEndedListenerImpl {

        private Predicate predicate;

        public void setPredicate(Predicate predicate) {
            this.predicate = predicate;
        }

        @Override
        public void onBuilderEnded(SubqueryBuilder<?> builder) {
            super.onBuilderEnded(builder);
            // set the finished subquery builder on the previously created predicate
            Predicate pred;
            if (predicate instanceof NotPredicate) {
                // unwrap not predicate
                pred = ((NotPredicate) predicate).getPredicate();
            } else {
                pred = predicate;
            }

            if (pred instanceof SubqueryPredicate) {
                ((SubqueryPredicate) pred).setSubqueryBuilder(builder);
            } else {
                throw new IllegalStateException("SubqueryBuilder ended but predicate was not a SubqueryPredicate");
            }
            rootPredicate.predicate.getChildren().add(predicate);
        }
    }

    @Override
    protected String getClauseName() {
        return "WHERE";
    }

    WhereOrBuilder<U> whereOr(AbstractBaseQueryBuilder<?, ?> builder) {
        return rootPredicate.startBuilder(new WhereOrBuilderImpl<U>((U) builder, rootPredicate, subqueryInitFactory));
    }

    @Override
    void verifyBuilderEnded() {
        super.verifyBuilderEnded();
        subqueryBuilderListener.verifyBuilderEnded();
    }

    SubqueryInitiator<U> whereExists(U result) {
        verifyBuilderEnded();
        subqueryBuilderListener.setPredicate(new ExistsPredicate());
        return subqueryInitFactory.createSubqueryInitiator(result, subqueryBuilderListener);
    }
    
    SubqueryInitiator<U> whereNotExists(U result) {
        verifyBuilderEnded();
        subqueryBuilderListener.setPredicate(new NotPredicate(new ExistsPredicate()));
        return subqueryInitFactory.createSubqueryInitiator(result, subqueryBuilderListener);
    }

    String buildClause(boolean generateRequiredMapKeyFiltersOnly) {
        queryGenerator.setGenerateRequiredMapKeyFiltersOnly(generateRequiredMapKeyFiltersOnly);
        String clause = super.buildClause();
        queryGenerator.setGenerateRequiredMapKeyFiltersOnly(false);
        return clause;
    }

}
