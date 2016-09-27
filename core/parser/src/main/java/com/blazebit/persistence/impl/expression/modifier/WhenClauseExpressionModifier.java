package com.blazebit.persistence.impl.expression.modifier;

import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.WhenClauseExpression;

/**
 * Created
 * by Moritz Becker (moritz.becker@gmx.at)
 * on 22.09.2016.
 */
public class WhenClauseExpressionModifier extends AbstractExpressionModifier<WhenClauseExpressionModifier, WhenClauseExpression, Expression> {

    public WhenClauseExpressionModifier() {
    }

    public WhenClauseExpressionModifier(WhenClauseExpression target) {
        super(target);
    }

    public WhenClauseExpressionModifier(WhenClauseExpressionModifier original) {
        super(original);
    }

    @Override
    public void set(Expression expression) {
        target.setResult(expression);
    }

    @Override
    public Object clone() {
        return new WhenClauseExpressionModifier(this);
    }
}
