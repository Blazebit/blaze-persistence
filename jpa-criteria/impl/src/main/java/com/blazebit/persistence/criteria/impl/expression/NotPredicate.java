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
import com.blazebit.persistence.criteria.impl.RenderContext;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class NotPredicate extends AbstractSimplePredicate {

    private final AbstractPredicate predicate;

    public NotPredicate(BlazeCriteriaBuilderImpl criteriaBuilder, AbstractPredicate predicate) {
        super(criteriaBuilder, false);
        this.predicate = predicate;
    }

    @Override
    public AbstractPredicate copyNegated() {
        return new NotPredicate(criteriaBuilder, this);
    }

    @Override
    public void render(RenderContext context) {
        final StringBuilder buffer = context.getBuffer();
        boolean requiresParanthesis = predicate instanceof CompoundPredicate;
        buffer.append("NOT ");
        if (requiresParanthesis) {
            buffer.append("(");
            context.apply(predicate);
            buffer.append(")");
        } else {
            context.apply(predicate);
        }
    }
}
