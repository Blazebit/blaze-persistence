/*
 * Copyright 2014 - 2022 Blazebit.
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

package com.blazebit.persistence.impl.function;

/**
 *
 * @author Jan-Willem Gmelig Meyling
 * @author Sayra Ranjha
 * @since 1.4.0
 */
public final class Order {

    private final String expression;
    private final boolean ascending;
    private final boolean nullsFirst;

    public Order(String expression, Boolean ascending, Boolean nullsFirst) {
        this.expression = expression;

        if (Boolean.FALSE.equals(ascending)) {
            this.ascending = false;
            // Default NULLS FIRST
            if (nullsFirst == null) {
                this.nullsFirst = true;
            } else {
                this.nullsFirst = nullsFirst;
            }
        } else {
            this.ascending = true;
            // Default NULLS LAST
            if (nullsFirst == null) {
                this.nullsFirst = false;
            } else {
                this.nullsFirst = nullsFirst;
            }
        }
    }

    public String getExpression() {
        return expression;
    }

    public boolean isAscending() {
        return ascending;
    }

    public boolean isNullsFirst() {
        return nullsFirst;
    }
}
