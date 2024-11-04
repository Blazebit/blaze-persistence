/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.transform;

import com.blazebit.persistence.impl.ClauseType;
import com.blazebit.persistence.parser.expression.modifier.ExpressionModifier;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface ExpressionModifierVisitor<T extends ExpressionModifier> {

    public void visit(T expressionModifier, ClauseType clauseType);

}
