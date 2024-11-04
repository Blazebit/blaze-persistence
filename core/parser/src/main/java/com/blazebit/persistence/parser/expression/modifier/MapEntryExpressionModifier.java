/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser.expression.modifier;

import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.MapEntryExpression;
import com.blazebit.persistence.parser.expression.PathExpression;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class MapEntryExpressionModifier extends AbstractExpressionModifier<MapEntryExpressionModifier, MapEntryExpression> {

    public MapEntryExpressionModifier(MapEntryExpression target) {
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
