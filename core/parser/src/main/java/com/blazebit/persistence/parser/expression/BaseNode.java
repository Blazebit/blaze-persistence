/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser.expression;

/**
 * TODO: documentation
 * 
 * @author Christian Beikov
 * @since 1.2.0
 *
 */
public interface BaseNode {

    public Expression createExpression(String field);

    public PathExpression createPathExpression(String field);

}
