/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser.expression;

/**
 * Super type of expressions like KEY/VALUE/ENTRY/INDEX
 * 
 * @author Christian Beikov
 * @since 1.2.0
 *
 */
public interface QualifiedExpression {

    public PathExpression getPath();

    public String getQualificationExpression();

}
