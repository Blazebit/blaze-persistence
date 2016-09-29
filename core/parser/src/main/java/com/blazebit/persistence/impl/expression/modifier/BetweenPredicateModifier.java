package com.blazebit.persistence.impl.expression.modifier;

import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.predicate.BetweenPredicate;

/**
 * Created
 * by Moritz Becker (moritz.becker@gmx.at)
 * on 22.09.2016.
 */
public abstract class BetweenPredicateModifier<SELF extends BetweenPredicateModifier<SELF>> extends AbstractExpressionModifier<SELF, BetweenPredicate, Expression> {

    public BetweenPredicateModifier() {
    }

    public BetweenPredicateModifier(BetweenPredicate target) {
        super(target);
    }

    public BetweenPredicateModifier(SELF original) {
        super(original);
    }

    public static class BetweenPredicateLeftModifier extends BetweenPredicateModifier<BetweenPredicateLeftModifier> {
        public BetweenPredicateLeftModifier() {
        }

        public BetweenPredicateLeftModifier(BetweenPredicate target) {
            super(target);
        }

        public BetweenPredicateLeftModifier(BetweenPredicateLeftModifier original) {
            super(original);
        }

        @Override
        public void set(Expression expression) {
            target.setLeft(expression);
        }

        @Override
        public Object clone() {
            return new BetweenPredicateLeftModifier(this);
        }
    }

    public static class BetweenPredicateStartModifier extends BetweenPredicateModifier<BetweenPredicateStartModifier> {
        public BetweenPredicateStartModifier() {
        }

        public BetweenPredicateStartModifier(BetweenPredicate target) {
            super(target);
        }

        public BetweenPredicateStartModifier(BetweenPredicateStartModifier original) {
            super(original);
        }

        @Override
        public void set(Expression expression) {
            target.setStart(expression);
        }

        @Override
        public Object clone() {
            return new BetweenPredicateStartModifier(this);
        }
    }

    public static class BetweenPredicateEndModifier extends BetweenPredicateModifier<BetweenPredicateEndModifier> {
        public BetweenPredicateEndModifier() {
        }

        public BetweenPredicateEndModifier(BetweenPredicate target) {
            super(target);
        }

        public BetweenPredicateEndModifier(BetweenPredicateEndModifier original) {
            super(original);
        }

        @Override
        public void set(Expression expression) {
            target.setEnd(expression);
        }

        @Override
        public Object clone() {
            return new BetweenPredicateEndModifier(this);
        }
    }
}
