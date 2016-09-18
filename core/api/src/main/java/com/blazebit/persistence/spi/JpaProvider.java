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
package com.blazebit.persistence.spi;

import javax.persistence.metamodel.Attribute;

/**
 * TODO: documentation
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface JpaProvider {

    public boolean supportsJpa21();

    public boolean supportsEntityJoin();

    public boolean supportsInsertStatement();

    public boolean needsBracketsForListParamter();

    /**
     * Returns whether key restricted left joins should be rewritten to subquery joins.
     * This is part of the workaround for https://hibernate.atlassian.net/browse/HHH-9329 which makes key restricted left joins wrong.
     *
     * @return true if joins should be rewritten to subquery joins, false otherwise
     */
    public boolean needsJoinSubqueryRewrite();

    public String getBooleanExpression(boolean value);

    public String getBooleanConditionalExpression(boolean value);

    public String getNullExpression();

    public String getOnClause();

    /**
     * Normally returns <code>VALUE</code>, but since Hibernate does weird things when using that, it returns <code>null</code>.
     * Returning null results in omitting <code>VALUE</code> in the final query that is passed to the JPA provider.
     *
     * @return The value function
     */
    public String getCollectionValueFunction();

    public Class<?> getDefaultQueryResultType();

    public String getCustomFunctionInvocation(String functionName, int argumentCount);

    public String escapeCharacter(char character);

    public boolean supportsNullPrecedenceExpression();

    public void renderNullPrecedence(StringBuilder sb, String expression, String resolvedExpression, String order, String nulls);

    public boolean supportsRootTreat();

    public boolean supportsTreatJoin();

    public boolean supportsRootTreatJoin();

    public boolean supportsSubtypePropertyResolving();

    public boolean supportsCountStar();

    public boolean isJoinTable(Attribute<?, ?> attribute);
}
