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

package com.blazebit.persistence.criteria.impl.expression;

import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;

import javax.persistence.criteria.Expression;
import java.util.Collections;
import java.util.List;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractSimplePredicate extends AbstractPredicate {

    private static final long serialVersionUID = 1L;
    private static final List<Expression<Boolean>> NO_EXPRESSIONS = Collections.emptyList();

    public AbstractSimplePredicate(BlazeCriteriaBuilderImpl criteriaBuilder, boolean negated) {
        super(criteriaBuilder, negated);
    }

    public BooleanOperator getOperator() {
        return BooleanOperator.AND;
    }

    public final List<Expression<Boolean>> getExpressions() {
        return NO_EXPRESSIONS;
    }
}
