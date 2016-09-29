package com.blazebit.persistence.impl.expression.modifier;

import com.blazebit.persistence.impl.expression.Expression;

/**
 * Created
 * by Moritz Becker (moritz.becker@gmx.at)
 * on 22.09.2016.
 */
public abstract class AbstractExpressionModifier<SELF extends AbstractExpressionModifier<SELF, T, E>, T extends  Expression, E extends Expression> implements ExpressionModifier<E> {

    protected T target;

    public AbstractExpressionModifier() {
    }

    public AbstractExpressionModifier(T target) {
        this.target = target;
    }

    public AbstractExpressionModifier(SELF original) {
        this.target = original.target;
    }

    public T getTarget() {
        return target;
    }

    public void setTarget(T target) {
        this.target = target;
    }

    @Override
    public abstract Object clone();
}
