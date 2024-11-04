/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser.expression;

import com.blazebit.persistence.parser.SimpleQueryGenerator;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public abstract class AbstractExpression implements Expression {

    @Override
    public abstract Expression copy(ExpressionCopyContext copyContext);

    @Override
    public String toString() {
        return SimpleQueryGenerator.toString(this);
    }
}
