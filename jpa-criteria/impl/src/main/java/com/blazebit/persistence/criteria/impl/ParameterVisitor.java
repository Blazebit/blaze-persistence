/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria.impl;

import com.blazebit.persistence.criteria.impl.expression.AbstractSelection;

import jakarta.persistence.criteria.ParameterExpression;
import jakarta.persistence.criteria.Selection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public final class ParameterVisitor {

    private final Set<ParameterExpression<?>> parameters = new LinkedHashSet<ParameterExpression<?>>();

    public Set<ParameterExpression<?>> getParameters() {
        return parameters;
    }

    public void add(ParameterExpression<?> parameter) {
        parameters.add(parameter);
    }

    public void visit(Selection<?> s) {
        if (s instanceof AbstractSelection<?>) {
            ((AbstractSelection<?>) s).visitParameters(this);
        }
    }
}
