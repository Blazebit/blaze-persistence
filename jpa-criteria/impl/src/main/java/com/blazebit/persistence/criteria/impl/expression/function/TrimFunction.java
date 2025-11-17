/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria.impl.expression.function;

import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.ParameterVisitor;
import com.blazebit.persistence.criteria.impl.RenderContext;

import jakarta.persistence.criteria.CriteriaBuilder.Trimspec;
import jakarta.persistence.criteria.Expression;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class TrimFunction extends AbstractFunctionExpression<String> {

    public static final String NAME = "TRIM";
    public static final Trimspec DEFAULT_TRIMSPEC = Trimspec.BOTH;

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

    public TrimFunction(BlazeCriteriaBuilderImpl criteriaBuilder, Expression<String> trimSource) {
        this(criteriaBuilder, DEFAULT_TRIMSPEC, null, trimSource);
    }

    public TrimFunction(BlazeCriteriaBuilderImpl criteriaBuilder, Expression<Character> trimCharacter, Expression<String> trimSource) {
        this(criteriaBuilder, DEFAULT_TRIMSPEC, trimCharacter, trimSource);
    }

    public TrimFunction(BlazeCriteriaBuilderImpl criteriaBuilder, Trimspec trimspec, Expression<String> trimSource) {
        this(criteriaBuilder, trimspec, null, trimSource);
    }

    @Override
    public void visitParameters(ParameterVisitor visitor) {
        if (trimCharacter != null) {
            visitor.visit(trimCharacter);
        }
        visitor.visit(trimSource);
    }

    @Override
    public void render(RenderContext context) {
        final StringBuilder buffer = context.getBuffer();
        buffer.append("TRIM(");
        buffer.append(trimspec.name());
        if (trimCharacter != null) {
            buffer.append(' ');
            context.apply(trimCharacter);
        }
        buffer.append(" FROM ");
        context.apply(trimSource);
        buffer.append(')');
    }
}
