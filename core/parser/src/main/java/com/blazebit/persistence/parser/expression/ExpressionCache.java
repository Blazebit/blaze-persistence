/*
 * Copyright 2014 - 2020 Blazebit.
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

package com.blazebit.persistence.parser.expression;

import java.util.Objects;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface ExpressionCache<T> {

    public T get(String cacheName, Key key);

    public T putIfAbsent(String cacheName, Key key, T value);

    /**
     *
     * @author Moritz Becker
     * @since 1.4.0
     */
    class Key {
        private static final byte ALLOW_OUTER_MASK = 1;
        private static final byte ALLOW_QUANTIFIED_PREDICATES_MASK = (1 << 1);
        private static final byte ALLOW_OBJECT_EXPRESSION_MASK = (1 << 2);

        private final String expression;
        private final byte flags;

        public Key(String expression, boolean allowOuter, boolean allowQuantifiedPredicates, boolean allowObjectExpression) {
            this.expression = expression;
            byte flags = 0;
            if (allowOuter) {
                flags |= ALLOW_OUTER_MASK;
            }
            if (allowOuter) {
                flags |= ALLOW_QUANTIFIED_PREDICATES_MASK;
            }
            if (allowObjectExpression) {
                flags |= ALLOW_OBJECT_EXPRESSION_MASK;
            }
            this.flags = flags;
        }

        public boolean isAllowOuter() {
            return (flags & ALLOW_OUTER_MASK) != 0;
        }

        public boolean isAllowQuantifiedPredicates() {
            return (flags & ALLOW_QUANTIFIED_PREDICATES_MASK) != 0;
        }

        public boolean isAllowObjectExpression() {
            return (flags & ALLOW_OBJECT_EXPRESSION_MASK) != 0;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Key key = (Key) o;
            return flags == key.flags &&
                    expression.equals(key.expression);
        }

        @Override
        public int hashCode() {
            return Objects.hash(expression, flags);
        }
    }

}
