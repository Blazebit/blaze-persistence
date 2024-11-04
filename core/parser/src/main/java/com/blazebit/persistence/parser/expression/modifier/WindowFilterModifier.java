/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser.expression.modifier;

import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.WindowDefinition;
import com.blazebit.persistence.parser.predicate.Predicate;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public class WindowFilterModifier implements ExpressionModifier {

    protected final WindowDefinition windowDefinition;

    public WindowFilterModifier(WindowDefinition windowDefinition) {
        this.windowDefinition = windowDefinition;
    }

    @Override
    public void set(Expression expression) {
        windowDefinition.setFilterPredicate((Predicate) expression);
    }

    @Override
    public Expression get() {
        return windowDefinition.getFilterPredicate();
    }

}
