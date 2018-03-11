/*
 * Copyright 2014 - 2018 Blazebit.
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

package com.blazebit.persistence.deltaspike.data.base.builder.part;

/**
 * Implementation is similar to {@link org.apache.deltaspike.data.impl.builder.QueryOperator} but altered
 * LikeIgnoreCase to use upper on both sides.
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public enum QueryOperator {

    LessThan("LessThan", "{0} < {1}"),
    LessThanEquals("LessThanEquals", "{0} <= {1}"),
    GreaterThan("GreaterThan", "{0} > {1}"),
    GreaterThanEquals("GreaterThanEquals", "{0} >= {1}"),
    Like("Like", "{0} like {1}"),
    LikeIgnoreCase("LikeIgnoreCase", "upper({0}) like upper({1})", true),
    NotEqual("NotEqual", "{0} <> {1}"),
    NotEqualIgnoreCase("NotEqualIgnoreCase", "upper({0}) <> upper({1})"),
    Equal("Equal", "{0} = {1}"),
    EqualIgnoreCase("EqualIgnoreCase", "upper({0}) = upper({1})"),
    IgnoreCase("IgnoreCase", "upper({0}) = upper({1})"),
    Between("Between", "{0} between {1} and {2}", 2),
    IsNotNull("IsNotNull", "{0} IS NOT NULL", 0),
    IsNull("IsNull", "{0} IS NULL", 0);

    private final String expression;
    private final String jpql;
    private final int paramNum;
    private final boolean caseInsensitive;

    private QueryOperator(String expression, String jpql) {
        this(expression, jpql, 1);
    }

    private QueryOperator(String expression, String jpql, boolean caseInsensitive) {
        this(expression, jpql, 1, caseInsensitive);
    }

    private QueryOperator(String expression, String jpql, int paramNum) {
        this(expression, jpql, paramNum, false);
    }

    private QueryOperator(String expression, String jpql, int paramNum, boolean caseInsensitive) {
        this.expression = expression;
        this.jpql = jpql;
        this.paramNum = paramNum;
        this.caseInsensitive = caseInsensitive;
    }

    public String getExpression() {
        return expression;
    }

    public String getJpql() {
        return jpql;
    }

    public int getParamNum() {
        return paramNum;
    }

    public boolean isCaseInsensitive() {
        return caseInsensitive;
    }
}