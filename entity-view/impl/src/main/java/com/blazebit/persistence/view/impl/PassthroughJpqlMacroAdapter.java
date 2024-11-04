/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl;

import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.FunctionExpression;
import com.blazebit.persistence.parser.expression.MacroFunction;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public class PassthroughJpqlMacroAdapter implements MacroFunction {

    private final String name;

    public PassthroughJpqlMacroAdapter(String name) {
        this.name = name;
    }

    @Override
    public Expression apply(List<Expression> expressions) {
        return new FunctionExpression(name, expressions);
    }

    @Override
    public Object[] getState() {
        return EMPTY;
    }

    @Override
    public boolean supportsCaching() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PassthroughJpqlMacroAdapter)) {
            return false;
        }

        PassthroughJpqlMacroAdapter that = (PassthroughJpqlMacroAdapter) o;

        return Arrays.equals(getState(), that.getState());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(getState());
    }
}
