package com.blazebit.persistence.impl.expression.modifier;

import com.blazebit.persistence.impl.expression.ArithmeticExpression;
import com.blazebit.persistence.impl.expression.Expression;

/**
 * Created
 * by Moritz Becker (moritz.becker@gmx.at)
 * on 22.09.2016.
 */
public abstract class ArithmeticExpressionModifier<SELF extends ArithmeticExpressionModifier<SELF>> extends AbstractExpressionModifier<SELF, ArithmeticExpression, Expression> {

    public ArithmeticExpressionModifier() {
    }

    public ArithmeticExpressionModifier(ArithmeticExpression target) {
        super(target);
    }

    public ArithmeticExpressionModifier(SELF original) {
        super(original);
    }

    public static class ArithmeticLeftExpressionModifier extends ArithmeticExpressionModifier<ArithmeticLeftExpressionModifier> {

        public ArithmeticLeftExpressionModifier() {
        }

        public ArithmeticLeftExpressionModifier(ArithmeticExpression target) {
            super(target);
        }

        public ArithmeticLeftExpressionModifier(ArithmeticLeftExpressionModifier original) {
            super(original);
        }

        @Override
        public void set(Expression expression) {
            target.setLeft(expression);
        }

        @Override
        public Object clone() {
            return new ArithmeticLeftExpressionModifier(this);
        }
    }

    public static class ArithmeticRightExpressionModifier extends ArithmeticExpressionModifier<ArithmeticRightExpressionModifier> {

        public ArithmeticRightExpressionModifier() {
        }

        public ArithmeticRightExpressionModifier(ArithmeticExpression target) {
            super(target);
        }

        public ArithmeticRightExpressionModifier(ArithmeticRightExpressionModifier original) {
            super(original);
        }

        @Override
        public void set(Expression expression) {
            target.setRight(expression);
        }

        @Override
        public Object clone() {
            return new ArithmeticRightExpressionModifier(this);
        }
    }

}
