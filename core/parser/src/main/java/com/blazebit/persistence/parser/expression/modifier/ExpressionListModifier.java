/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser.expression.modifier;

import com.blazebit.persistence.parser.expression.Expression;

import java.util.List;

/**
 *
 * @author Moritz Becker
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ExpressionListModifier implements ExpressionModifier {

    protected final List<Expression> target;
    protected final int modificationIndex;

    public ExpressionListModifier(List<? extends Expression> target, int modificationIndex) {
        this.target = (List<Expression>) target;
        this.modificationIndex = modificationIndex;
    }

    public List<Expression> getTarget() {
        return target;
    }

    @Override
    public void set(Expression expression) {
        target.set(modificationIndex, expression);
    }

    @Override
    public Expression get() {
        return target.get(modificationIndex);
    }

}
