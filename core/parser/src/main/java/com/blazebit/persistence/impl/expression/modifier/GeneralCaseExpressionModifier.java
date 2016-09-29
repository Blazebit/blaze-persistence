package com.blazebit.persistence.impl.expression.modifier;

import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.GeneralCaseExpression;

/**
 * Created
 * by Moritz Becker (moritz.becker@gmx.at)
 * on 22.09.2016.
 */
public class GeneralCaseExpressionModifier extends AbstractExpressionModifier<GeneralCaseExpressionModifier, GeneralCaseExpression, Expression> {

    public GeneralCaseExpressionModifier() {
    }

    public GeneralCaseExpressionModifier(GeneralCaseExpression target) {
        super(target);
    }

    public GeneralCaseExpressionModifier(GeneralCaseExpressionModifier original) {
        super(original);
    }

    @Override
    public void set(Expression expression) {
        target.setDefaultExpr(expression);
    }

    @Override
    public Object clone() {
        return new GeneralCaseExpressionModifier(this);
    }
}
