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
 * The builder interface for a select new select clause.
 *
 * @param <T> The query builder that is returned on terminal operations
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface SelectObjectBuilder<T extends FullQueryBuilder<?, T>> {

    /**
     * Like {@link SelectObjectBuilder#withSubquery(java.lang.String)} but without an alias.
     *
     * @return The subquery initiator for building a subquery
     */
    public SubqueryInitiator<SelectObjectBuilder<T>> withSubquery();

    /**
     * Starts a subquery builder which allows the result of the specified subquery
     * to be passed as argument to the select new clause.
     *
     * @param alias The alias for the subquery
     * @return A starting point for the subquery specification
     */
    public SubqueryInitiator<SelectObjectBuilder<T>> withSubquery(String alias);

    /**
     * Starts a {@link SubqueryInitiator} for a new argument for the select new clause with the given select alias.
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
     * @since 1.2.0
     */
    public SubqueryInitiator<SelectObjectBuilder<T>> withSubquery(String subqueryAlias, String expression, String selectAlias);

    /**
     * Like {@link SelectBuilder#selectSubquery(java.lang.String,java.lang.String,java.lang.String)} but without a select alias.
     *
     * @param subqueryAlias The alias for the subquery which will be replaced by the actual subquery
     * @param expression The expression which will be added as select item
     * @return The subquery initiator for building a subquery
     * @since 1.2.0
     */
    public SubqueryInitiator<SelectObjectBuilder<T>> withSubquery(String subqueryAlias, String expression);

    /**
     * Starts a {@link MultipleSubqueryInitiator} for a new argument for the select new clause with the given select alias.
     * 
     * <p>
     * All occurrences of subsequently defined <code>subqueryAlias</code>es in <code>expression</code> will be replaced by the respective subquery.
     * When the builder finishes, the select item is added to the parent container represented by the type <code>X</code>.
     * </p>
     * 
     * @param expression The expression which will be added as select item.
     *            This expression contains the {@code subqueryAlias} to define the insertion points for the subquery.
     * @param selectAlias The select alias for the expression
     * @return The subquery initiator for building multiple subqueries for their respective subqueryAliases
     * @since 1.2.0
     */
    public MultipleSubqueryInitiator<SelectObjectBuilder<T>> withSubqueries(String expression, String selectAlias);

    /**
     * Like {@link SelectBuilder#selectSubqueries(java.lang.String,java.lang.String)} but without a select alias.
     *
     * @param expression The expression which will be added as select item
     * @return The subquery initiator for building multiple subqueries for their respective subqueryAliases
     * @since 1.2.0
     */
    public MultipleSubqueryInitiator<SelectObjectBuilder<T>> withSubqueries(String expression);

    /**
     * Like {@link SelectObjectBuilder#withSubquery(java.lang.String, FullQueryBuilder)} but without an alias.
     *
     * @param criteriaBuilder The criteria builder to base the subquery on
     * @return The subquery builder for building a subquery
     * @since 1.2.0
     */
    public SubqueryBuilder<SelectObjectBuilder<T>> withSubquery(FullQueryBuilder<?, ?> criteriaBuilder);

    /**
     * Starts a {@link SubqueryBuilder} based on the given criteria builder which allows the result of the specified subquery
     * to be passed as argument to the select new clause.
     *
     * @param alias The alias for the subquery
     * @param criteriaBuilder The criteria builder to base the subquery on
     * @return The subquery builder for building a subquery
     * @since 1.2.0
     */
    public SubqueryBuilder<SelectObjectBuilder<T>> withSubquery(String alias, FullQueryBuilder<?, ?> criteriaBuilder);

    /**
     * Starts a {@link SubqueryBuilder} based on the given criteria builder for a new argument for the select new clause with the given select alias.
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
    public SubqueryBuilder<SelectObjectBuilder<T>> withSubquery(String subqueryAlias, String expression, String selectAlias, FullQueryBuilder<?, ?> criteriaBuilder);

    /**
     * Like {@link SelectBuilder#selectSubquery(java.lang.String,java.lang.String,java.lang.String,FullQueryBuilder)} but without a select alias.
     *
     * @param subqueryAlias The alias for the subquery which will be replaced by the actual subquery
     * @param expression The expression which will be added as select item
     * @param criteriaBuilder The criteria builder to base the subquery on
     * @return The subquery builder for building a subquery
     * @since 1.2.0
     */
    public SubqueryBuilder<SelectObjectBuilder<T>> withSubquery(String subqueryAlias, String expression, FullQueryBuilder<?, ?> criteriaBuilder);

    /**
     * Like {@link SelectObjectBuilder#with(java.lang.String, java.lang.String)} but without an alias.
     *
     * @param expression The expression to add
     * @return A starting point for the subquery specification
     */
    public SelectObjectBuilder<T> with(String expression);

    /**
     * Adds the given expression to the arguments for the select new select clause.
     *
     * @param expression The expression to add
     * @param alias The alias for the expression
     * @return This select object builder
     * @throws IllegalStateException Is thrown when the argument position is already taken
     */
    public SelectObjectBuilder<T> with(String expression, String alias);

    /**
     * Like {@link SelectObjectBuilder#with(int, java.lang.String, java.lang.String)} but without an alias.
     *
     * @param position The position at which the expression should be added
     * @param expression The expression to add
     * @return A starting point for the subquery specification
     */
    public SelectObjectBuilder<T> with(int position, String expression);

    /**
     * Adds the given expression at the given position to the arguments for the select new select clause.
     *
     * @param position The position at which the expression should be added
     * @param expression The expression to add
     * @param alias The alias for the expression
     * @return This select object builder
     * @throws IllegalStateException Is thrown when the argument position is already taken
     */
    public SelectObjectBuilder<T> with(int position, String expression, String alias);

    /**
     * Like {@link SelectObjectBuilder#withSubquery(int,java.lang.String)} but without an alias.
     *
     * @param position The position at which the expression should be added
     * @return The subquery initiator for building a subquery
     * @since 1.2.0
     */
    public SubqueryInitiator<SelectObjectBuilder<T>> withSubquery(int position);

    /**
     * Like {@link SelectObjectBuilder#withSubquery(java.lang.String)} but adds the resulting subquery expression to the given position.
     *
     * @param position The position at which the expression should be added
     * @param alias The alias for the subquery
     * @return The subquery initiator for building a subquery
     * @since 1.2.0
     */
    public SubqueryInitiator<SelectObjectBuilder<T>> withSubquery(int position, String alias);

    /**
     * Like {@link SelectObjectBuilder#withSubquery(java.lang.String,java.lang.String,java.lang.String)} but adds the resulting subquery expression to the given position.
     *
     * @param position The position at which the expression should be added
     * @param subqueryAlias The alias for the subquery which will be replaced by the actual subquery
     * @param expression The expression which will be added as select item.
     * @param selectAlias The select alias for the expression
     * @return The subquery initiator for building a subquery
     * @since 1.2.0
     */
    public SubqueryInitiator<SelectObjectBuilder<T>> withSubquery(int position, String subqueryAlias, String expression, String selectAlias);

    /**
     * Like {@link SelectObjectBuilder#withSubquery(java.lang.String,java.lang.String,java.lang.String)} but adds the resulting subquery expression to the given position.
     *
     * @param position The position at which the expression should be added
     * @param subqueryAlias The alias for the subquery which will be replaced by the actual subquery
     * @param expression The expression which will be added as select item.
     * @return The subquery initiator for building a subquery
     * @since 1.2.0
     */
    public SubqueryInitiator<SelectObjectBuilder<T>> withSubquery(int position, String subqueryAlias, String expression);

    /**
     * Like {@link SelectObjectBuilder#withSubqueries(java.lang.String,java.lang.String)} but adds the resulting expression to the given position.
     *
     * @param position The position at which the expression should be added
     * @param expression The expression which will be added as select item.
     * @param selectAlias The select alias for the expression
     * @return The subquery initiator for building multiple subqueries for their respective subqueryAliases
     * @since 1.2.0
     */
    public MultipleSubqueryInitiator<SelectObjectBuilder<T>> withSubqueries(int position, String expression, String selectAlias);

    /**
     * Like {@link SelectObjectBuilder#withSubqueries(int,java.lang.String,java.lang.String)} but without an alias.
     *
     * @param position The position at which the expression should be added
     * @param expression The expression which will be added as select item.
     * @return The subquery initiator for building multiple subqueries for their respective subqueryAliases
     * @since 1.2.0
     */
    public MultipleSubqueryInitiator<SelectObjectBuilder<T>> withSubqueries(int position, String expression);

    /**
     * Like {@link SelectObjectBuilder#withSubquery(int,java.lang.String,FullQueryBuilder)} but without an alias.
     *
     * @param position The position at which the expression should be added
     * @param criteriaBuilder The criteria builder to base the subquery on
     * @return The subquery builder for building a subquery
     * @since 1.2.0
     */
    public SubqueryBuilder<SelectObjectBuilder<T>> withSubquery(int position, FullQueryBuilder<?, ?> criteriaBuilder);

    /**
     * Like {@link SelectObjectBuilder#withSubquery(java.lang.String,FullQueryBuilder)} but adds the resulting subquery expression to the given position.
     *
     * @param position The position at which the expression should be added
     * @param alias The alias for the subquery
     * @param criteriaBuilder The criteria builder to base the subquery on
     * @return The subquery builder for building a subquery
     * @since 1.2.0
     */
    public SubqueryBuilder<SelectObjectBuilder<T>> withSubquery(int position, String alias, FullQueryBuilder<?, ?> criteriaBuilder);

    /**
     * Like {@link SelectObjectBuilder#withSubquery(java.lang.String,java.lang.String,java.lang.String,FullQueryBuilder)} but adds the resulting subquery expression to the given position.
     *
     * @param position The position at which the expression should be added
     * @param subqueryAlias The alias for the subquery which will be replaced by the actual subquery
     * @param expression The expression which will be added as select item.
     * @param selectAlias The select alias for the expression
     * @param criteriaBuilder The criteria builder to base the subquery on
     * @return The subquery builder for building a subquery
     * @since 1.2.0
     */
    public SubqueryBuilder<SelectObjectBuilder<T>> withSubquery(int position, String subqueryAlias, String expression, String selectAlias, FullQueryBuilder<?, ?> criteriaBuilder);

    /**
     * Like {@link SelectObjectBuilder#withSubquery(java.lang.String,java.lang.String,java.lang.String,FullQueryBuilder)} but adds the resulting subquery expression to the given position.
     *
     * @param position The position at which the expression should be added
     * @param subqueryAlias The alias for the subquery which will be replaced by the actual subquery
     * @param expression The expression which will be added as select item.
     * @param criteriaBuilder The criteria builder to base the subquery on
     * @return The subquery builder for building a subquery
     * @since 1.2.0
     */
    public SubqueryBuilder<SelectObjectBuilder<T>> withSubquery(int position, String subqueryAlias, String expression, FullQueryBuilder<?, ?> criteriaBuilder);

    /**
     * Finishes the select object builder.
     *
     * @return The query builder
     */
    public T end();
}
