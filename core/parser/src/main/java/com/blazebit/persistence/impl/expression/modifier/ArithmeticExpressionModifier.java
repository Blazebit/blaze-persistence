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
