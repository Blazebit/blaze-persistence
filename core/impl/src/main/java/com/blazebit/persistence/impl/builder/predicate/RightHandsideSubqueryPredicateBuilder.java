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

import com.blazebit.persistence.impl.SubqueryBuilderListenerImpl;
import com.blazebit.persistence.impl.SubqueryInternalBuilder;
import com.blazebit.persistence.parser.expression.SubqueryExpression;
import com.blazebit.persistence.parser.predicate.ExistsPredicate;
import com.blazebit.persistence.parser.predicate.Predicate;
import com.blazebit.persistence.parser.predicate.PredicateBuilder;
import com.blazebit.persistence.parser.predicate.UnaryExpressionPredicate;

/**
 *
 * @author Moritz Becker
 * @since 1.0.0
 */
public class RightHandsideSubqueryPredicateBuilder<T> extends SubqueryBuilderListenerImpl<T> implements PredicateBuilder {

    private final Predicate predicate;
    private final PredicateBuilderEndedListener listener;

    public RightHandsideSubqueryPredicateBuilder(PredicateBuilderEndedListener listener, Predicate predicate) {
        this.predicate = predicate;
        this.listener = listener;
    }

    @Override
    public void onBuilderEnded(SubqueryInternalBuilder<T> builder) {
        super.onBuilderEnded(builder);
        // set the finished subquery builder on the previously created predicate
        if (predicate instanceof ExistsPredicate && builder.getMaxResults() != Integer.MAX_VALUE) {
            // Since we render the limit in the subquery as wrapping function, there currently does not seem to be a possibility to support this in JPQL grammars
            throw new IllegalArgumentException("Limiting a subquery in an exists predicate is currently unsupported!");
        }

        if (predicate instanceof UnaryExpressionPredicate) {
            ((UnaryExpressionPredicate) predicate).setExpression(new SubqueryExpression(builder));
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
