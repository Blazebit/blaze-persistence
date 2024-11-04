/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.builder.expression;

import com.blazebit.persistence.parser.expression.Expression;

/**
 *
 * @author Moritz Becker
 * @since 1.0.0
 */
public interface ExpressionBuilder {

    public Expression getExpression();
}
