/*
 * Copyright 2014 - 2017 Blazebit.
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
 * A JPA provider implementation provides information about which features are supported by a JPA implementation.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface JpaProvider {

    /**
     * Whether JPA 2.1 specification is supported.
     *
     * @return True if JPA 2.1 is supported, false otherwise
     */
    public boolean supportsJpa21();

    /**
     * Whether Entity Joins are supported.
     *
     * @return True if Entity Joins are supported, false otherwise
     */
    public boolean supportsEntityJoin();

    /**
     * Whether Insert statements are supported.
     *
     * @return True if Insert statements are supported, false otherwise
     */
    public boolean supportsInsertStatement();

    /**
     * Whether brackets are needed around a list parameter.
     *
     * @return True if brackets are needed, false otherwise
     */
    public boolean needsBracketsForListParamter();

    /**
     * Returns whether key restricted left joins should be rewritten to subquery joins.
     * This is part of the workaround for https://hibernate.atlassian.net/browse/HHH-9329 which makes key restricted left joins wrong.
     *
     * @return true if joins should be rewritten to subquery joins, false otherwise
     */
    public boolean needsJoinSubqueryRewrite();

    /**
     * The JPQL expression to use for the given literal boolean value.
     * This expression is used in contexts where an expression is assumed.
     *
     * @param value The boolean value
     * @return The JPQL expression to use to reflect the boolean value
     */
    public String getBooleanExpression(boolean value);

    /**
     * The JPQL conditional expression to use for the given literal boolean value.
     * This expression is used in contexts where a predicate is assumed.
     *
     * @param value The boolean value
     * @return The JPQL conditional expression to use to reflect the boolean value
     */
    public String getBooleanConditionalExpression(boolean value);

    /**
     * The JPQL expression to use for the NULL literal.
     *
     * @return The JPQL expression to use to reflect the NULL value
     */
    public String getNullExpression();

    /**
     * The name of the clause that has the same semantics as the ON clause which was introduced in JPA 2.1.
     *
     * @return The name of the ON clause
     */
    public String getOnClause();

    /**
     * Normally returns <code>VALUE</code>, but since Hibernate does weird things when using that, it returns <code>null</code>.
     * Returning null results in omitting <code>VALUE</code> in the final query that is passed to the JPA provider.
     *
     * @return The value function
     */
    public String getCollectionValueFunction();

    /**
     * Whether dereferencing a VALUE function expression is supported by the JPA provider.
     *
     * @return True if dereferencing is supported, false otherwise
     * @since 1.2.0
     */
    public boolean supportsCollectionValueDereference();

    /**
     * The default result type of a scalar query.
     *
     * @return The default result type
     */
    public Class<?> getDefaultQueryResultType();

    /**
     * Returns the start of a JPQL representation of a function invocation for the given function name and argument count.
     * Normally this returns <code>FUNCTION('<i>functionName</i>',</code> but implementations may also allow to render the function name directly.
     *
     * @param functionName The function name
     * @param argumentCount The number of the arguments that the function is invoked with
     * @return The start of the JPQL represenation for the function invocation
     */
    public String getCustomFunctionInvocation(String functionName, int argumentCount);

    /**
     * The given escape character for a LIKE predicate as string.
     *
     * @param character The escape character
     * @return The string representation
     */
    public String escapeCharacter(char character);

    /**
     * Whether the query language supports the null precedence clause.
     *
     * @return True if the null precedence clause is supported, false otherwise
     */
    public boolean supportsNullPrecedenceExpression();

    /**
     * Renders the null precedence into the given string builder.
     *
     * @param sb The builder to which the null precedence should be appended to
     * @param expression The order by expression which might be a select alias
     * @param resolvedExpression The resolved expression for a possible select alias or the expression
     * @param order The order as string (<code>ASC</code> or <code>DESC</code>)
     * @param nulls The null precedence as string (<code>NULLS FIRST</code> or <code>NULLS LAST</code>)
     */
    public void renderNullPrecedence(StringBuilder sb, String expression, String resolvedExpression, String order, String nulls);

    /**
     * Whether treating a from/root alias is supported.
     *
     * @return True if treating a from alias is supported, false otherwise
     */
    public boolean supportsRootTreat();

    /**
     * Whether a treat join is supported.
     *
     * @return True if a treat join is supported, false otherwise
     */
    public boolean supportsTreatJoin();

    /**
     * Whether a root treat in a treat join is supported.
     *
     * @return True if a root treat in a treat join is supported, false otherwise
     */
    public boolean supportsRootTreatJoin();

    /**
     * Whether properties accessed of a from node are implicitly resolved to properties of a subtype of the from node.
     *
     * @return True if subtype property resolving is supported, false otherwise
     */
    public boolean supportsSubtypePropertyResolving();

    /**
     * Whether the <code>COUNT(*)</code> syntax is supported.
     *
     * @return True if <code>COUNT(*)</code> syntax is supported, false otherwise
     */
    public boolean supportsCountStar();

    /**
     * Whether the given attribute is a collection that uses a join table.
     *
     * @param attribute The attribute to check
     * @return True if uses a join table, false otherwise
     */
    public boolean isJoinTable(Attribute<?, ?> attribute);

    /**
     * Whether the given attribute is a non-indexed and non-ordered collection a.k.a. a bag.
     *
     * @param attribute The attribute to check
     * @return True if it is a bag, false otherwise
     */
    public boolean isBag(Attribute<?, ?> attribute);

    /**
     * Indicates if the provider supports expressions like
     *
     *    doc.owner.id
     *
     * without generating an extra join.
     *
     * @return true if supported, else fales
     */
    public boolean supportsSingleValuedAssociationIdExpressions();
}
