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

/**
 * The builder interface for a select new select clause.
 *
 * @param <T> The query builder that is returned on terminal operations
 * @author Christian Beikov
 * @since 1.0
 */
public interface SelectObjectBuilder<T extends FullQueryBuilder<?, T>> {

    /**
     * Like {@link SelectObjectBuilder#withSubquery(java.lang.String)} but without an alias.
     *
     * @return A starting point for the subquery specification
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
     * Finishes the select object builder.
     *
     * @return The query builder
     */
    public T end();
}
