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

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;
import java.util.List;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractPredicate extends AbstractExpression<Boolean> implements Predicate {

    private static final long serialVersionUID = 1L;

    private final boolean negated;

    protected AbstractPredicate(BlazeCriteriaBuilderImpl criteriaBuilder, boolean negated) {
        super(criteriaBuilder, Boolean.class);
        this.negated = negated;
    }

    public boolean isNegated() {
        return negated;
    }

    @Override
    public Predicate not() {
        return criteriaBuilder.negate(this);
    }

    public abstract AbstractPredicate copyNegated();

    @Override
    public final boolean isCompoundSelection() {
        return false;
    }

    @Override
    public final List<Selection<?>> getCompoundSelectionItems() {
        throw new IllegalStateException("Not a compound selection");
    }
}
