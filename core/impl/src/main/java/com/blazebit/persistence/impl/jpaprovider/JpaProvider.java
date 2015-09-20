/*
 * Copyright 2015 Blazebit.
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
package com.blazebit.persistence.impl.jpaprovider;

import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public interface JpaProvider {

    public boolean supportsJpa21();

    public boolean needsBracketsForListParamter();

    public String getBooleanExpression(boolean value);

    public String getBooleanConditionalExpression(boolean value);

    public String getOnClause();

    public String getCollectionValueFunction();

    public Class<?> getDefaultQueryResultType();

    public String getCustomFunctionInvocation(String functionName, int argumentCount);

    public String escapeCharacter(char character);

    public boolean supportsNullPrecedenceExpression();

    public String renderNullPrecedence(String expression, String resolvedExpression, String order, String nulls);
}
