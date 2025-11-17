/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria.impl.expression;

import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.ParameterVisitor;
import com.blazebit.persistence.criteria.impl.RenderContext;

import jakarta.persistence.criteria.Subquery;
import java.io.Serializable;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class QuantifiableSubqueryExpression<Y> extends AbstractExpression<Y> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    public static enum Quantor {
        ALL {
            String getOperator() {
                return "ALL ";
            }
        },
        SOME {
            String getOperator() {
                return "SOME ";
            }
        },
        ANY {
            String getOperator() {
                return "ANY ";
            }
        };

        abstract String getOperator();
    }

    private final Subquery<Y> subquery;
    private final Quantor quantor;

    public QuantifiableSubqueryExpression(BlazeCriteriaBuilderImpl criteriaBuilder, Class<Y> javaType, Subquery<Y> subquery, Quantor modifier) {
        super(criteriaBuilder, javaType);
        this.subquery = subquery;
        this.quantor = modifier;
    }

    @Override
    public void visitParameters(ParameterVisitor visitor) {
        // no-op
    }

    @Override
    public void render(RenderContext context) {
        context.getBuffer().append(quantor.getOperator());
        context.apply(subquery);
    }
}
