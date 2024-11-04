/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser.expression;

import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface MacroFunction {

    Object[] EMPTY = {};

    public Expression apply(List<Expression> expressions);

    // Equals and hashCode must be based on this state
    public Object[] getState();

    public boolean supportsCaching();
}
