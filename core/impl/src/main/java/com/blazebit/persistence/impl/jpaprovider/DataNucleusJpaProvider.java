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
public class DataNucleusJpaProvider implements JpaProvider {

    public DataNucleusJpaProvider(EntityManager em) {

    }

    @Override
    public boolean supportsJpa21() {
        return true;
    }

    @Override
    public boolean needsBracketsForListParamter() {
        return true;
    }

    @Override
    public String getBooleanExpression(boolean value) {
        return value ? "TRUE" : "FALSE";
    }

    @Override
    public String getBooleanConditionalExpression(boolean value) {
        return value ? "TRUE" : "FALSE";
    }

    @Override
    public String escapeCharacter(char character) {
        return Character.toString(character);
    }

    @Override
    public boolean supportsNullPrecedenceExpression() {
        return true;
    }

    @Override
    public String renderNullPrecedence(String expression, String resolvedExpression, String order, String nulls) {
        if (nulls == null) {
            return expression + " " + order;
        } else {
            return expression + " " + order + " NULLS " + nulls;
        }
    }

    @Override
    public String getOnClause() {
        return "ON";
    }

    @Override
    public String getCollectionValueFunction() {
        return "VALUE";
    }

    @Override
    public Class<?> getDefaultQueryResultType() {
        return null;
    }

    @Override
    public String getCustomFunctionInvocation(String functionName, int argumentCount) {
        return functionName + "(";
    }

}
