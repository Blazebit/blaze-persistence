package com.blazebit.persistence.impl.expression.modifier;

import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.predicate.InPredicate;

/**
 * Created
 * by Moritz Becker (moritz.becker@gmx.at)
 * on 22.09.2016.
 */
public class InPredicateLeftModifier extends AbstractExpressionModifier<InPredicateLeftModifier, InPredicate, Expression> {

    public InPredicateLeftModifier() {
    }

    public InPredicateLeftModifier(InPredicate target) {
        super(target);
    }

    public InPredicateLeftModifier(InPredicateLeftModifier original) {
        super(original);
    }

    @Override
    public void set(Expression expression) {
        target.setLeft(expression);
    }

    @Override
    public Object clone() {
        return new InPredicateLeftModifier(this);
    }
}
