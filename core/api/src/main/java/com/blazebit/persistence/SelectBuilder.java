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

package com.blazebit.persistence;

/**
 * An interface for builders that support selecting.
 * This is related to the fact, that a query builder supports select clauses.
 *
 * @param <X> The result type
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface SelectBuilder<X> {

    /**
     * Like {@link SelectBuilder#selectCase(java.lang.String)} but without an alias.
     *
     * @return The case when builder
     */
    public CaseWhenStarterBuilder<X> selectCase();

    /**
     * Starts a {@link CaseWhenBuilder} with the given alias as select alias.
     *
     * @param alias The select alias for the case when expression
     * @return The case when builder
     */
    public CaseWhenStarterBuilder<X> selectCase(String alias);

    /**
     * Like {@link SelectBuilder#selectSimpleCase(java.lang.String, java.lang.String)} but without an alias.
     *
     * @param caseOperand The case operand
     * @return The simple case when builder
     */
    public SimpleCaseWhenStarterBuilder<X> selectSimpleCase(String caseOperand);

    /**
     * Starts a {@link SimpleCaseWhenBuilder} with the given alias as select alias.
     * The expression is the case operand which will be compared to the when expressions defined in the subsequent
     * {@linkplain SimpleCaseWhenBuilder}.
     *
     * @param caseOperand The case operand
     * @param alias The select alias for the simple case when expression
     * @return The simple case when builder
     */
    public SimpleCaseWhenStarterBuilder<X> selectSimpleCase(String caseOperand, String alias);

    /**
     * Like {@link SelectBuilder#selectSubquery(java.lang.String)} but without an alias.
     *
     * @return The subquery initiator for building a subquery
     */
    public SubqueryInitiator<X> selectSubquery();

    /**
     * Starts a {@link SubqueryInitiator} for the select item with the given alias.
     * When the builder finishes, the select item is added to the parent container represented by the type <code>X</code>.
     *
     * @param alias The select alias for the subquery
     * @return The subquery initiator for building a subquery
     */
    public SubqueryInitiator<X> selectSubquery(String alias);

    /**
     * Starts a {@link SubqueryInitiator} for a new select item with the given select alias.
     * 
     * <p>
     * All occurrences of <code>subqueryAlias</code> in <code>expression</code> will be replaced by the subquery. When the builder
     * finishes, the select item is added to the parent container represented by the type <code>X</code>.
     * </p>
     * 
     * @param subqueryAlias The alias for the subquery which will be replaced by the actual subquery
     * @param expression The expression which will be added as select item.
     *            This expression contains the {@code subqueryAlias} to define the insertion points for the subquery.
     * @param selectAlias The select alias for the expression
     * @return The subquery initiator for building a subquery
     */
    public SubqueryInitiator<X> selectSubquery(String subqueryAlias, String expression, String selectAlias);

    /**
     * Like {@link SelectBuilder#selectSubquery(java.lang.String,java.lang.String,java.lang.String)} but without a select alias.
     *
     * @param subqueryAlias The alias for the subquery which will be replaced by the actual subquery
     * @param expression The expression which will be added as select item
     * @return The subquery initiator for building a subquery
     */
    public SubqueryInitiator<X> selectSubquery(String subqueryAlias, String expression);

    /**
     * Starts a {@link MultipleSubqueryInitiator} for a new select item with the given select alias.
     * 
     * <p>
     * All occurrences of subsequently defined <code>subqueryAlias</code>es in <code>expression</code> will be replaced by the respective subquery.
     * When the builder finishes, the select item is added to the parent container represented by the type <code>X</code>.
     * </p>
     * 
     * @param expression The expression which will be added as select item.
     *            This expression contains the {@code subqueryAlias} to define the insertion points for the subquery.
     * @param selectAlias The select alias for the expression
     * @return The subquery initiator for building a subquery
     * @since 1.2.0
     */
    public MultipleSubqueryInitiator<X> selectSubqueries(String expression, String selectAlias);

    /**
     * Like {@link SelectBuilder#selectSubqueries(java.lang.String,java.lang.String)} but without a select alias.
     *
     * @param expression The expression which will be added as select item
     * @return The subquery initiator for building a subquery
     * @since 1.2.0
     */
    public MultipleSubqueryInitiator<X> selectSubqueries(String expression);

    /**
     * Like {@link SelectBuilder#selectSubquery(java.lang.String, FullQueryBuilder)} but without an alias.
     *
     * @param criteriaBuilder The criteria builder to base the subquery on
     * @return The subquery builder for building a subquery
     * @since 1.2.0
     */
    public SubqueryBuilder<X> selectSubquery(FullQueryBuilder<?, ?> criteriaBuilder);

    /**
     * Starts a {@link SubqueryBuilder} based on the given criteria builder for the select item with the given alias.
     * When the builder finishes, the select item is added to the parent container represented by the type <code>X</code>.
     *
     * @param alias The select alias for the subquery
     * @param criteriaBuilder The criteria builder to base the subquery on
     * @return The subquery builder for building a subquery
     * @since 1.2.0
     */
    public SubqueryBuilder<X> selectSubquery(String alias, FullQueryBuilder<?, ?> criteriaBuilder);

    /**
     * Starts a {@link SubqueryBuilder} based on the given criteria builder for a new select item with the given select alias.
     *
     * <p>
     * All occurrences of <code>subqueryAlias</code> in <code>expression</code> will be replaced by the subquery. When the builder
     * finishes, the select item is added to the parent container represented by the type <code>X</code>.
     * </p>
     *
     * @param subqueryAlias The alias for the subquery which will be replaced by the actual subquery
     * @param expression The expression which will be added as select item.
     *            This expression contains the {@code subqueryAlias} to define the insertion points for the subquery.
     * @param selectAlias The select alias for the expression
     * @param criteriaBuilder The criteria builder to base the subquery on
     * @return The subquery builder for building a subquery
     * @since 1.2.0
     */
    public SubqueryBuilder<X> selectSubquery(String subqueryAlias, String expression, String selectAlias, FullQueryBuilder<?, ?> criteriaBuilder);

    /**
     * Like {@link SelectBuilder#selectSubquery(java.lang.String,java.lang.String,java.lang.String, FullQueryBuilder)} but without a select alias.
     *
     * @param subqueryAlias The alias for the subquery which will be replaced by the actual subquery
     * @param expression The expression which will be added as select item
     * @param criteriaBuilder The criteria builder to base the subquery on
     * @return The subquery builder for building a subquery
     * @since 1.2.0
     */
    public SubqueryBuilder<X> selectSubquery(String subqueryAlias, String expression, FullQueryBuilder<?, ?> criteriaBuilder);
    
    /**
     * Adds a select clause with the given expression to the query.
     *
     * @param expression The expression for the select clause
     * @return The query builder for chaining calls
     */
    public X select(String expression);

    /**
     * Adds a select clause with the given expression and alias to the query.
     *
     * @param expression The expression for the select clause
     * @param alias The alias for the expression
     * @return The query builder for chaining calls
     */
    public X select(String expression, String alias);
}
