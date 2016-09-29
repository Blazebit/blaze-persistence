package com.blazebit.persistence.impl.expression.modifier;

import com.blazebit.persistence.impl.expression.Expression;

/**
 * Created
 * by Moritz Becker (moritz.becker@gmx.at)
 * on 22.09.2016.
 */
public interface ExpressionModifier<E extends Expression> {

    void set(E expression);

    Object clone();
}
