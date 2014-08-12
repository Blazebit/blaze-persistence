/*
 * Copyright 2014 Blazebit.
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
package com.blazebit.persistence;

import java.util.List;

/**
 * The builder interface for predicates.
 * The left hand side of the predicate is already known to the builder and the methods of this builder either terminate the building process or start a {@link SubqueryInitiator}.
 *
 * @param <T> The builder type that is returned on terminal operations
 * @author Christian Beikov
 * @since 1.0
 */
public interface RestrictionBuilder<T> {

    /**
     * Finishes the BETWEEN predicate and adds it to the parent predicate container represented by the type {@linkplain T}.
     * The predicate checks if the left hand side is between start and end.
     *
     * @param start The between start value
     * @param end   The between end value
     * @return The parent predicate container builder
     */
    public T between(Object start, Object end);

    /**
     * Like {@link RestrictionBuilder#between(java.lang.Object, java.lang.Object) } but the result is wrapped in a NOT predicate.
     *
     * @param start The between start value
     * @param end   The between end value
     * @return The parent predicate container builder
     */
    public T notBetween(Object start, Object end);

    /**
     * Starts a {@link QuantifiableBinaryPredicateBuilder} for the EQ predicate that can be used to apply quantors.
     *
     * @return The quantifiable binary predicate builder
     */
    public QuantifiableBinaryPredicateBuilder<T> eq();
    
    /**
     * Starts a {@link QuantifiableSubqueryInitiator} for the EQ predicate that can be used to apply quantors.
     * All occurrences of <code>subqueryAlias</code> in <code>expression</code> will be replaced by the subquery.
     * When the subquery builder and the restriction builder for the right hand side are finished, the predicate is added to the
     * parent predicate container represented by the type {@linkplain T}.
     *
     * @param subqueryAlias The alias for the subquery which will be replaced by the actual subquery
     * @param expression    The expression which will be used as left hand side of a predicate
     * @return The quantifiable subquery initiator
     */
    public SubqueryInitiator<T> eq(String subqueryAlias, String expression);

    /**
     * Finishes the EQ predicate and adds it to the parent predicate container represented by the type {@linkplain T}.
     * The predicate checks if the left hand side is equal to the given value.
     *
     * @param value The value on the right hand side
     * @return The parent predicate container builder
     */
    public T eq(Object value);

    /**
     * Finishes the EQ predicate and adds it to the parent predicate container represented by the type {@linkplain T}.
     * The predicate checks if the left hand side is equal to the given expression.
     *
     * @param expression The expression on the right hand side
     * @return The parent predicate container builder
     */
    public T eqExpression(String expression);

    /**
     * Like {@link RestrictionBuilder#eq() } but the result is wrapped in a NOT predicate.
     *
     * @return The quantifiable binary predicate builder
     */
    public QuantifiableBinaryPredicateBuilder<T> notEq();
    
    /**
     * Like {@link RestrictionBuilder#eq(java.lang.String,java.lang.String) } but the result is wrapped in a NOT predicate.
     *
     * @param subqueryAlias The alias for the subquery which will be replaced by the actual subquery
     * @param expression    The expression which will be used as left hand side of a predicate
     * @return The quantifiable binary predicate builder
     */
    public SubqueryInitiator<T> notEq(String subqueryAlias, String expression);

    /**
     * Like {@link RestrictionBuilder#eq(java.lang.Object) } but the result is wrapped in a NOT predicate.
     *
     * @param value The value on the right hand side
     * @return The parent predicate container builder
     */
    public T notEq(Object value);

    /**
     * Like {@link RestrictionBuilder#eq(java.lang.String) } but the result is wrapped in a NOT predicate.
     *
     * @param expression The expression on the right hand side
     * @return The quantifiable binary predicate builder
     */
    public T notEqExpression(String expression);

    /**
     * Starts a {@link QuantifiableBinaryPredicateBuilder} for the GT predicate that can be used to apply quantors.
     *
     * @return The quantifiable binary predicate builder
     */
    public QuantifiableBinaryPredicateBuilder<T> gt();
    
    /**
     * Starts a {@link QuantifiableSubqueryInitiator} for the GT predicate that can be used to apply quantors.
     * All occurrences of <code>subqueryAlias</code> in <code>expression</code> will be replaced by the subquery.
     * When the subquery builder and the restriction builder for the right hand side are finished, the predicate is added to the
     * parent predicate container represented by the type {@linkplain T}.
     *
     * @param subqueryAlias The alias for the subquery which will be replaced by the actual subquery
     * @param expression    The expression which will be used as left hand side of a predicate
     * @return The quantifiable subquery initiator
     */
    public SubqueryInitiator<T> gt(String subqueryAlias, String expression);

    /**
     * Finishes the GT predicate and adds it to the parent predicate container represented by the type {@linkplain T}.
     * The predicate checks if the left hand side is greater than the given value.
     *
     * @param value The value on the right hand side
     * @return The parent predicate container builder
     */
    public T gt(Object value);

    /**
     * Finishes the GT predicate and adds it to the parent predicate container represented by the type {@linkplain T}.
     * The predicate checks if the left hand side is greater than the given expression.
     *
     * @param expression The expression on the right hand side
     * @return The parent predicate container builder
     */
    public T gtExpression(String expression);

    /**
     * Starts a {@link QuantifiableBinaryPredicateBuilder} for the GE predicate that can be used to apply quantors.
     *
     * @return The quantifiable binary predicate builder
     */
    public QuantifiableBinaryPredicateBuilder<T> ge();
    
    /**
     * Starts a {@link QuantifiableSubqueryInitiator} for the GE predicate that can be used to apply quantors.
     * All occurrences of <code>subqueryAlias</code> in <code>expression</code> will be replaced by the subquery.
     * When the subquery builder and the restriction builder for the right hand side are finished, the predicate is added to the
     * parent predicate container represented by the type {@linkplain T}.
     *
     * @param subqueryAlias The alias for the subquery which will be replaced by the actual subquery
     * @param expression    The expression which will be used as left hand side of a predicate
     * @return The quantifiable subquery initiator
     */
    public SubqueryInitiator<T> ge(String subqueryAlias, String expression);

    /**
     * Finishes the GE predicate and adds it to the parent predicate container represented by the type {@linkplain T}.
     * The predicate checks if the left hand side is greater or equal to the given value.
     *
     * @param value The value on the right hand side
     * @return The parent predicate container builder
     */
    public T ge(Object value);

    /**
     * Finishes the GE predicate and adds it to the parent predicate container represented by the type {@linkplain T}.
     * The predicate checks if the left hand side is greater or equal to the given expression.
     *
     * @param expression The expression on the right hand side
     * @return The parent predicate container builder
     */
    public T geExpression(String expression);

    /**
     * Starts a {@link QuantifiableBinaryPredicateBuilder} for the LT predicate that can be used to apply quantors.
     *
     * @return The quantifiable binary predicate builder
     */
    public QuantifiableBinaryPredicateBuilder<T> lt();
    
    /**
     * Starts a {@link QuantifiableSubqueryInitiator} for the LT predicate that can be used to apply quantors.
     * All occurrences of <code>subqueryAlias</code> in <code>expression</code> will be replaced by the subquery.
     * When the subquery builder and the restriction builder for the right hand side are finished, the predicate is added to the
     * parent predicate container represented by the type {@linkplain T}.
     *
     * @param subqueryAlias The alias for the subquery which will be replaced by the actual subquery
     * @param expression    The expression which will be used as left hand side of a predicate
     * @return The quantifiable subquery initiator
     */
    public SubqueryInitiator<T> lt(String subqueryAlias, String expression);

    /**
     * Finishes the LT predicate and adds it to the parent predicate container represented by the type {@linkplain T}.
     * The predicate checks if the left hand side is less than the given value.
     *
     * @param value The value on the right hand side
     * @return The parent predicate container builder
     */
    public T lt(Object value);

    /**
     * Finishes the LT predicate and adds it to the parent predicate container represented by the type {@linkplain T}.
     * The predicate checks if the left hand side is less than the given expression.
     *
     * @param expression The expression on the right hand side
     * @return The parent predicate container builder
     */
    public T ltExpression(String expression);

    /**
     * Starts a {@link QuantifiableBinaryPredicateBuilder} for the LE predicate that can be used to apply quantors.
     *
     * @return The quantifiable binary predicate builder
     */
    public QuantifiableBinaryPredicateBuilder<T> le();
    
    /**
     * Starts a {@link QuantifiableSubqueryInitiator} for the LE predicate that can be used to apply quantors.
     * All occurrences of <code>subqueryAlias</code> in <code>expression</code> will be replaced by the subquery.
     * When the subquery builder and the restriction builder for the right hand side are finished, the predicate is added to the
     * parent predicate container represented by the type {@linkplain T}.
     *
     * @param subqueryAlias The alias for the subquery which will be replaced by the actual subquery
     * @param expression    The expression which will be used as left hand side of a predicate
     * @return The quantifiable subquery initiator
     */
    public SubqueryInitiator<T> le(String subqueryAlias, String expression);

    /**
     * Finishes the LE predicate and adds it to the parent predicate container represented by the type {@linkplain T}.
     * The predicate checks if the left hand side is less or equal to the given value.
     *
     * @param value The value on the right hand side
     * @return The parent predicate container builder
     */
    public T le(Object value);

    /**
     * Finishes the LE predicate and adds it to the parent predicate container represented by the type {@linkplain T}.
     * The predicate checks if the left hand side is less or equal to the given expression.
     *
     * @param expression The expression on the right hand side
     * @return The parent predicate container builder
     */
    public T leExpression(String expression);

    /**
     * Starts a {@link SubqueryInitiator} for the right hand side of the IN predicate.
     * When the builder finishes, the predicate is added to the parent predicate container represented by the type {@linkplain T}.
     *
     * @return The subquery initiator for building a subquery
     */
    public SubqueryInitiator<T> in();

    /**
     * Like {@link RestrictionBuilder#in() } but the result is wrapped in a NOT predicate.
     *
     * @return The subquery initiator for building a subquery
     */
    public SubqueryInitiator<T> notIn();

    /**
     * Finishes the IN predicate and adds it to the parent predicate container represented by the type {@linkplain T}.
     * The predicate checks if the left hand side is in the list of given values.
     *
     * @param values The values on the right hand side
     * @return The parent predicate container builder
     */
    public T in(List<?> values);

    /**
     * Like {@link RestrictionBuilder#in(java.util.List) } but the values will be wrapped in a {@link List}.
     *
     * @param values The values on the right hand side
     * @return The parent predicate container builder
     */
    public T in(Object... values);

    /**
     * Like {@link RestrictionBuilder#in(java.util.List) } but the result is wrapped in a NOT predicate.
     *
     * @param values The values on the right hand side
     * @return The parent predicate container builder
     */
    public T notIn(List<?> values);

    /**
     * Like {@link RestrictionBuilder#notIn(java.util.List) } but the values will be wrapped in a {@link List}.
     *
     * @param values The values on the right hand side
     * @return The parent predicate container builder
     */
    public T notIn(Object... values);

    /**
     * Finishes the IS NULL predicate and adds it to the parent predicate container represented by the type {@linkplain T}.
     * The predicate checks if the left hand side is null.
     *
     * @return The parent predicate container builder
     */
    public T isNull();

    /**
     * Like {@link RestrictionBuilder#isNull() } but the result is wrapped in a NOT predicate.
     *
     * @return The parent predicate container builder
     */
    public T isNotNull();

    /**
     * Finishes the IS EMPTY predicate and adds it to the parent predicate container represented by the type {@linkplain T}.
     * The predicate checks if the left hand side is empty.
     *
     * @return The parent predicate container builder
     */
    public T isEmpty();

    /**
     * Like {@link RestrictionBuilder#isEmpty() } but the result is wrapped in a NOT predicate.
     *
     * @return The parent predicate container builder
     */
    public T isNotEmpty();

    /**
     * Finishes the MEMBER OF predicate and adds it to the parent predicate container represented by the type {@linkplain T}.
     * The predicate checks if the left hand side is of the given expression.
     *
     * @param expression The expression on the right hand side
     * @return The parent predicate container builder
     */
    public T isMemberOf(String expression);

    /**
     * Like {@link RestrictionBuilder#isMemberOf(java.lang.String) } but the result is wrapped in a NOT predicate.
     *
     * @param expression The expression on the right hand side
     * @return The parent predicate container builder
     */
    public T isNotMemberOf(String expression);

    /**
     * Like {@link RestrictionBuilder#like(java.lang.String, boolean) } but with caseSensitive set to true.
     *
     * @param value The value on the right hand side
     * @return The parent predicate container builder
     */
    public T like(String value);

    /**
     * Like {@link RestrictionBuilder#like(java.lang.String, boolean, java.lang.Character) } but with escapeCharacter set to null.
     *
     * @param value         The value on the right hand side
     * @param caseSensitive Whether the like predicate should be case sensitive or not
     * @return The parent predicate container builder
     */
    public T like(String value, boolean caseSensitive);

    /**
     * Finishes the LIKE predicate and adds it to the parent predicate container represented by the type {@linkplain T}.
     * The predicate checks either case sensitive or unsensitive if the left hand side is like the given value. The escape character is used to escape the placeholders.
     *
     * @param value           The value on the right hand side
     * @param caseSensitive   Whether the like predicate should be case sensitive or not
     * @param escapeCharacter The escape character for placeholders
     * @return The parent predicate container builder
     */
    public T like(String value, boolean caseSensitive, Character escapeCharacter);

    /**
     * Like {@link RestrictionBuilder#likeExpression(java.lang.String, boolean) } but with caseSensitive set to true.
     *
     * @param expression The expression on the right hand side
     * @return The parent predicate container builder
     */
    public T likeExpression(String expression);

    /**
     * Like {@link RestrictionBuilder#likeExpression(java.lang.String, boolean, java.lang.Character) } but with escapeCharacter set to null.
     *
     * @param expression    The expression on the right hand side
     * @param caseSensitive Whether the like predicate should be case sensitive or not
     * @return The parent predicate container builder
     */
    public T likeExpression(String expression, boolean caseSensitive);

    /**
     * Finishes the LIKE predicate and adds it to the parent predicate container represented by the type {@linkplain T}.
     * The predicate checks either case sensitive or unsensitive if the left hand side is like the given expression. The escape character is used to escape the placeholders.
     *
     * @param expression      The expression on the right hand side
     * @param caseSensitive   Whether the like predicate should be case sensitive or not
     * @param escapeCharacter The escape character for placeholders
     * @return The parent predicate container builder
     */
    public T likeExpression(String expression, boolean caseSensitive, Character escapeCharacter);

    /**
     * Like {@link RestrictionBuilder#like(java.lang.String) } but the result is wrapped in a NOT predicate.
     *
     * @param value The value on the right hand side
     * @return The parent predicate container builder
     */
    public T notLike(String value);

    /**
     * Like {@link RestrictionBuilder#like(java.lang.String, boolean) } but the result is wrapped in a NOT predicate.
     *
     * @param value         The value on the right hand side
     * @param caseSensitive Whether the like predicate should be case sensitive or not
     * @return The parent predicate container builder
     */
    public T notLike(String value, boolean caseSensitive);

    /**
     * Like {@link RestrictionBuilder#like(java.lang.String, boolean, java.lang.Character) } but the result is wrapped in a NOT predicate.
     *
     * @param value           The value on the right hand side
     * @param caseSensitive   Whether the like predicate should be case sensitive or not
     * @param escapeCharacter The escape character for placeholders
     * @return The parent predicate container builder
     */
    public T notLike(String value, boolean caseSensitive, Character escapeCharacter);

    /**
     * Like {@link RestrictionBuilder#likeExpression(java.lang.String) } but the result is wrapped in a NOT predicate.
     *
     * @param expression The expression on the right hand side
     * @return The parent predicate container builder
     */
    public T notLikeExpression(String expression);

    /**
     * Like {@link RestrictionBuilder#likeExpression(java.lang.String, boolean) } but the result is wrapped in a NOT predicate.
     *
     * @param expression    The expression on the right hand side
     * @param caseSensitive Whether the like predicate should be case sensitive or not
     * @return The parent predicate container builder
     */
    public T notLikeExpression(String expression, boolean caseSensitive);

    /**
     * Like {@link RestrictionBuilder#likeExpression(java.lang.String, boolean, java.lang.Character) } but the result is wrapped in a NOT predicate.
     *
     * @param expression      The expression on the right hand side
     * @param caseSensitive   Whether the like predicate should be case sensitive or not
     * @param escapeCharacter The escape character for placeholders
     * @return The parent predicate container builder
     */
    public T notLikeExpression(String expression, boolean caseSensitive, Character escapeCharacter);
}
