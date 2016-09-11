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
package com.blazebit.persistence.impl.eclipselink;

import com.blazebit.persistence.spi.JpaProvider;

import javax.persistence.EntityManager;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class EclipseLinkJpaProvider implements JpaProvider {

    public EclipseLinkJpaProvider(EntityManager em) {

    }

    @Override
    public boolean supportsJpa21() {
        return true;
    }

    @Override
    public boolean supportsEntityJoin() {
        return true;
    }

    @Override
    public boolean supportsRelationAccessInOnClause() {
        return true;
    }

    @Override
    public boolean supportsInsertStatement() {
        return false;
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
    public String getNullExpression() {
        return "NULL";
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
    public void renderNullPrecedence(StringBuilder sb, String expression, String resolvedExpression, String order, String nulls) {
        sb.append(expression).append(' ').append(order);
        
        if (nulls != null) {
            sb.append(" NULLS ").append(nulls);
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
        return Object.class;
    }

    @Override
    public String getCustomFunctionInvocation(String functionName, int argumentCount) {
    	// Careful, PaginatedCriteriaBuilder has some dependency on the "length" of the string for rendering in the count query
        if (argumentCount == 0) {
            return "OPERATOR('" + functionName + "'";
        }

        return "OPERATOR('" + functionName + "',";
    }

    @Override
    public boolean supportsRootTreat() {
        return true;
    }

    @Override
    public boolean supportsTreatJoin() {
        return true;
    }

    @Override
    public boolean supportsRootTreatJoin() {
        return true;
    }

    @Override
    public boolean supportsSubtypePropertyResolving() {
        return false;
    }

    @Override
    public boolean supportsCountStar() {
        return false;
    }

}
