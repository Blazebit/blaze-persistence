/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser.expression;

import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.4.1
 */
public class ExpressionCopyContextMap implements ExpressionCopyContext {

    private final Map<String, String> parameterMapping;

    public ExpressionCopyContextMap(Map<String, String> parameterMapping) {
        this.parameterMapping = parameterMapping;
    }

    @Override
    public String getNewParameterName(String oldParameterName) {
        String newParameterName = parameterMapping.get(oldParameterName);
        if (newParameterName == null) {
            return oldParameterName;
        }
        return newParameterName;
    }

    @Override
    public Expression getExpressionForAlias(String alias) {
        return null;
    }

    @Override
    public boolean isCopyResolved() {
        return false;
    }
}
