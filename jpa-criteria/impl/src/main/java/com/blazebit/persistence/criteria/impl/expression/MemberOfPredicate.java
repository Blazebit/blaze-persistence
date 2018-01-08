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
import com.blazebit.persistence.criteria.impl.path.PluralAttributePath;

import javax.persistence.criteria.Expression;
import java.util.Collection;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class MemberOfPredicate<E, C extends Collection<E>> extends AbstractSimplePredicate {

    private static final long serialVersionUID = 1L;

    private final Expression<E> elementExpression;
    private final PluralAttributePath<C> collectionPath;

    public MemberOfPredicate(BlazeCriteriaBuilderImpl criteriaBuilder, boolean negated, Expression<E> elementExpression, PluralAttributePath<C> collectionPath) {
        super(criteriaBuilder, negated);
        this.elementExpression = elementExpression;
        this.collectionPath = collectionPath;
    }

    public MemberOfPredicate(BlazeCriteriaBuilderImpl criteriaBuilder, boolean negated, E element, PluralAttributePath<C> collectionPath) {
        this(criteriaBuilder, negated, new LiteralExpression<E>(criteriaBuilder, element), collectionPath);
    }

    @Override
    public AbstractPredicate copyNegated() {
        return new MemberOfPredicate<E, C>(criteriaBuilder, !isNegated(), elementExpression, collectionPath);
    }

    @Override
    public void visitParameters(ParameterVisitor visitor) {
        visitor.visit(elementExpression);
        visitor.visit(collectionPath);
    }

    @Override
    public void render(RenderContext context) {
        context.apply(elementExpression);

        if (isNegated()) {
            context.getBuffer().append(" NOT");
        }

        context.getBuffer().append(" MEMBER OF ");
        context.apply(collectionPath);
    }

}
