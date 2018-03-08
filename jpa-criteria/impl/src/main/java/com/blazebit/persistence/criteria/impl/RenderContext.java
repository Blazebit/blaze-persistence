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

package com.blazebit.persistence.criteria.impl;

import com.blazebit.persistence.SubqueryInitiator;

import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Selection;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface RenderContext {

    public StringBuilder getBuffer();

    public SubqueryInitiator<?> getSubqueryInitiator();

    public ClauseType getClauseType();

    public void apply(Selection<?> selection);

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    public static enum ClauseType {
        SET,
        SELECT,
        FROM,
        ON,
        WHERE,
        GROUP_BY,
        HAVING,
        ORDER_BY
    }

    public String generateAlias(Class<?> entityClass);

    public String generateSubqueryAlias(InternalQuery<?> query);

    public String registerExplicitParameter(ParameterExpression<?> criteriaQueryParameter);

    public String registerLiteralParameterBinding(Object literal, Class javaType);
}
