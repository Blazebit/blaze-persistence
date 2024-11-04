/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser.expression;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public interface PathElementExpression extends Expression {

    public PathElementExpression copy(ExpressionCopyContext copyContext);
}
