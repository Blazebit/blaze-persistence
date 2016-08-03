package com.blazebit.persistence.impl.expression;

import com.blazebit.persistence.impl.predicate.Predicate;

/**
 * Created
 * by Moritz Becker (moritz.becker@gmx.at)
 * on 03.08.2016.
 */
public interface BooleanExpression extends Expression {

    @Override
    public BooleanExpression clone();

}
