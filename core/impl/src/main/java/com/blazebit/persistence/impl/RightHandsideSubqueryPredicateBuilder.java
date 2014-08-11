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

import com.blazebit.persistence.impl.expression.SubqueryExpression;
import com.blazebit.persistence.impl.predicate.NotPredicate;
import com.blazebit.persistence.impl.predicate.Predicate;
import com.blazebit.persistence.impl.predicate.PredicateBuilder;
import com.blazebit.persistence.impl.predicate.PredicateBuilderEndedListener;
import com.blazebit.persistence.impl.predicate.UnaryExpressionPredicate;

/**
 *
 * @author ccbem
 */
public class RightHandsideSubqueryPredicateBuilder<T> extends SubqueryBuilderListenerImpl<T> implements PredicateBuilder {
        private final Predicate predicate;
        private final PredicateBuilderEndedListener listener;

        public RightHandsideSubqueryPredicateBuilder(PredicateBuilderEndedListener listener, Predicate predicate) {
            this.predicate = predicate;
            this.listener = listener;
        }
        
        @Override
        public void onBuilderEnded(SubqueryBuilderImpl<T> builder) {
            super.onBuilderEnded(builder);
            // set the finished subquery builder on the previously created predicate
            Predicate pred;
            if (predicate instanceof NotPredicate) {
                // unwrap not predicate
                pred = ((NotPredicate) predicate).getPredicate();
            }else{
                pred = predicate;
            }

            if (pred instanceof UnaryExpressionPredicate) {
                ((UnaryExpressionPredicate) pred).setExpression(new SubqueryExpression(builder));
            } else {
                throw new IllegalStateException("SubqueryBuilder ended but predicate type was unexpected");
            }

            listener.onBuilderEnded(this);
        }

        @Override
        public Predicate getPredicate() {
            return predicate;
        }
    }
