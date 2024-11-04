/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser.expression.modifier;

import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.MapKeyExpression;
import com.blazebit.persistence.parser.expression.PathExpression;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class MapKeyExpressionModifier extends AbstractExpressionModifier<MapKeyExpressionModifier, MapKeyExpression> {

    public MapKeyExpressionModifier(MapKeyExpression target) {
        super(target);
    }

    @Override
    public void set(Expression expression) {
        target.setPath((PathExpression) expression);
    }

    @Override
    public Expression get() {
        return target.getPath();
    }

}
