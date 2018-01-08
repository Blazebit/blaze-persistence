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
import com.blazebit.persistence.criteria.impl.ParameterVisitor;
import com.blazebit.persistence.criteria.impl.RenderContext;

import javax.persistence.criteria.Subquery;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ExistsPredicate extends AbstractSimplePredicate {

    private static final long serialVersionUID = 1L;

    private final Subquery<?> subquery;

    public ExistsPredicate(BlazeCriteriaBuilderImpl criteriaBuilder, Subquery<?> subquery) {
        this(criteriaBuilder, false, subquery);
    }

    private ExistsPredicate(BlazeCriteriaBuilderImpl criteriaBuilder, boolean negated, Subquery<?> subquery) {
        super(criteriaBuilder, negated);
        this.subquery = subquery;
    }

    @Override
    public AbstractPredicate copyNegated() {
        return new ExistsPredicate(criteriaBuilder, !isNegated(), subquery);
    }

    @Override
    public void visitParameters(ParameterVisitor visitor) {
        // no-op
    }

    @Override
    public void render(RenderContext context) {
        if (isNegated()) {
            context.getBuffer().append("NOT ");
        }

        context.getBuffer().append("EXISTS ");
        context.apply(subquery);
    }

}
