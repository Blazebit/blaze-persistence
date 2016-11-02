/*
 * Copyright 2014 - 2016 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blazebit.persistence.impl.expression.modifier;

import com.blazebit.persistence.impl.predicate.BinaryExpressionPredicate;
import com.blazebit.persistence.impl.predicate.Predicate;

/**
 * Created
 * by Moritz Becker (moritz.becker@gmx.at)
 * on 22.09.2016.
 */
public abstract class BinaryExpressionPredicateModifier<SELF extends BinaryExpressionPredicateModifier<SELF>> extends AbstractExpressionModifier<SELF, BinaryExpressionPredicate, Predicate> {

    public BinaryExpressionPredicateModifier() {
    }

    public BinaryExpressionPredicateModifier(BinaryExpressionPredicate target) {
        super(target);
    }

    public BinaryExpressionPredicateModifier(SELF original) {
        super(original);
    }

    public static class BinaryExpressionPredicateLeftModifier extends BinaryExpressionPredicateModifier<BinaryExpressionPredicateLeftModifier> {

        public BinaryExpressionPredicateLeftModifier() {
        }

        public BinaryExpressionPredicateLeftModifier(BinaryExpressionPredicate target) {
            super(target);
        }

        public BinaryExpressionPredicateLeftModifier(BinaryExpressionPredicateLeftModifier original) {
            super(original);
        }

        @Override
        public void set(Predicate predicate) {
            target.setLeft(predicate);
        }

        @Override
        public Object clone() {
            return new BinaryExpressionPredicateLeftModifier(this);
        }
    }

    public static class BinaryExpressionPredicateRightModifier extends BinaryExpressionPredicateModifier<BinaryExpressionPredicateRightModifier> {

        public BinaryExpressionPredicateRightModifier() {
        }

        public BinaryExpressionPredicateRightModifier(BinaryExpressionPredicate target) {
            super(target);
        }

        public BinaryExpressionPredicateRightModifier(BinaryExpressionPredicateRightModifier original) {
            super(original);
        }

        @Override
        public void set(Predicate predicate) {
            target.setLeft(predicate);
        }

        @Override
        public Object clone() {
            return new BinaryExpressionPredicateRightModifier(this);
        }
    }
}
