/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser.expression;

import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.6.9
 */
public class ExpressionCopyContextForQuery implements ExpressionCopyContext {

    private final ExpressionCopyContext parent;
    private final Map<String, Expression> aliasedExpressions;

    public ExpressionCopyContextForQuery(ExpressionCopyContext parent, Map<String, Expression> aliasedExpressions) {
        this.parent = parent;
        this.aliasedExpressions = aliasedExpressions;
    }

    @Override
    public String getNewParameterName(String oldParameterName) {
        return parent.getNewParameterName(oldParameterName);
    }

    @Override
    public Expression getExpressionForAlias(String alias) {
        return aliasedExpressions.get(alias);
    }

    @Override
    public boolean isCopyResolved() {
        return false;
    }
}
