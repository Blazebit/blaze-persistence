package com.blazebit.persistence.impl.expression.modifier;

import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.predicate.UnaryExpressionPredicate;

/**
 * Created
 * by Moritz Becker (moritz.becker@gmx.at)
 * on 22.09.2016.
 */
public class UnaryExpressionPredicateModifier extends AbstractExpressionModifier<UnaryExpressionPredicateModifier, UnaryExpressionPredicate, Expression> {
    public UnaryExpressionPredicateModifier() {
    }

    public UnaryExpressionPredicateModifier(UnaryExpressionPredicate target) {
        super(target);
    }

    public UnaryExpressionPredicateModifier(UnaryExpressionPredicateModifier original) {
        super(original);
    }

    @Override
    public void set(Expression expression) {
        target.setExpression(expression);
    }

    @Override
    public Object clone() {
        return new UnaryExpressionPredicateModifier(this);
    }
}
