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

package com.blazebit.persistence.criteria.impl.expression.function;

import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.ParameterVisitor;
import com.blazebit.persistence.criteria.impl.RenderContext;
import com.blazebit.persistence.criteria.impl.expression.LiteralExpression;

import javax.persistence.criteria.CriteriaBuilder.Trimspec;
import javax.persistence.criteria.Expression;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class TrimFunction extends AbstractFunctionExpression<String> {

    public static final String NAME = "TRIM";
    public static final Trimspec DEFAULT_TRIMSPEC = Trimspec.BOTH;
    public static final char DEFAULT_TRIM_CHAR = ' ';

    private static final long serialVersionUID = 1L;

    private final Trimspec trimspec;
    private final Expression<Character> trimCharacter;
    private final Expression<String> trimSource;

    public TrimFunction(BlazeCriteriaBuilderImpl criteriaBuilder, Trimspec trimspec, Expression<Character> trimCharacter, Expression<String> trimSource) {
        super(criteriaBuilder, String.class, NAME);
        this.trimspec = trimspec;
        this.trimCharacter = trimCharacter;
        this.trimSource = trimSource;
    }

    public TrimFunction(BlazeCriteriaBuilderImpl criteriaBuilder, Trimspec trimspec, char trimCharacter, Expression<String> trimSource) {
        super(criteriaBuilder, String.class, NAME);
        this.trimspec = trimspec;
        this.trimCharacter = new LiteralExpression<Character>(criteriaBuilder, trimCharacter);
        this.trimSource = trimSource;
    }

    public TrimFunction(BlazeCriteriaBuilderImpl criteriaBuilder, Expression<String> trimSource) {
        this(criteriaBuilder, DEFAULT_TRIMSPEC, DEFAULT_TRIM_CHAR, trimSource);
    }

    public TrimFunction(BlazeCriteriaBuilderImpl criteriaBuilder, Expression<Character> trimCharacter, Expression<String> trimSource) {
        this(criteriaBuilder, DEFAULT_TRIMSPEC, trimCharacter, trimSource);
    }

    public TrimFunction(BlazeCriteriaBuilderImpl criteriaBuilder, char trimCharacter, Expression<String> trimSource) {
        this(criteriaBuilder, DEFAULT_TRIMSPEC, trimCharacter, trimSource);
    }

    public TrimFunction(BlazeCriteriaBuilderImpl criteriaBuilder, Trimspec trimspec, Expression<String> trimSource) {
        this(criteriaBuilder, trimspec, DEFAULT_TRIM_CHAR, trimSource);
    }

    @Override
    public void visitParameters(ParameterVisitor visitor) {
        visitor.visit(trimCharacter);
        visitor.visit(trimSource);
    }

    @Override
    public void render(RenderContext context) {
        final StringBuilder buffer = context.getBuffer();
        buffer.append("TRIM(");
        buffer.append(trimspec.name());
        buffer.append(' ');

        if (trimCharacter.getClass().isAssignableFrom(LiteralExpression.class)) {
            // Use character as literal if possible because some databases don't support parameters for the character
            buffer.append(((LiteralExpression<Character>) trimCharacter).getLiteral());
        } else {
            context.apply(trimCharacter);
        }

        buffer.append(" FROM ");
        context.apply(trimSource);
        buffer.append(')');
    }
}
