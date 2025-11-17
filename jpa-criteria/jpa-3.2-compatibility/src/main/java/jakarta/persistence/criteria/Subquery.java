/*
 * Copyright (c) 2008, 2023 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0,
 * or the Eclipse Distribution License v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause
 */

// Contributors:
//     Gavin King      - 3.2
//     Linda DeMichiel - 2.1
//     Linda DeMichiel - 2.0


package jakarta.persistence.criteria;

import java.util.List;
import java.util.Set;

/**
 * The {@code Subquery} interface defines functionality that is
 * specific to subqueries.
 *
 * <p>A subquery has an expression as its selection item.
 *
 * @param <T> the type of the selection item.
 *
 * @since 2.0
 */
public interface Subquery<T> extends AbstractQuery<T>, Expression<T> {
	
    /**
     * Specify the item that is to be returned as the subquery 
     * result.
     * Replaces the previously specified selection, if any.
     * @param expression  expression specifying the item that
     *        is to be returned as the subquery result
     * @return the modified subquery
     */
    Subquery<T> select(Expression<T> expression);
	
    /**
     * Modify the subquery to restrict the result according
     * to the specified boolean expression.
     * Replaces the previously added restriction(s), if any.
     * This method only overrides the return type of the 
     * corresponding {@code AbstractQuery} method.
     * @param restriction  a simple or compound boolean expression
     * @return the modified subquery
     */
    Subquery<T> where(Expression<Boolean> restriction);

    /**
     * Modify the subquery to restrict the result according 
     * to the conjunction of the specified restriction predicates.
     * Replaces the previously added restriction(s), if any.
     * If no restrictions are specified, any previously added
     * restrictions are simply removed.
     * This method only overrides the return type of the 
     * corresponding {@code AbstractQuery} method.
     * @param restrictions  zero or more restriction predicates
     * @return the modified subquery
     */
    Subquery<T> where(Predicate... restrictions);

    /**
     * Modify the query to restrict the query result according
     * to the conjunction of the specified restriction predicates.
     * Replaces the previously added restriction(s), if any.
     * If no restrictions are specified, any previously added
     * restrictions are simply removed.
     * This method only overrides the return type of the
     * corresponding {@code AbstractQuery} method.
     * @param restrictions  a list of zero or more restriction predicates
     * @return the modified query
     * @since 3.2
     */
    Subquery<T> where(List<Predicate> restrictions);

    /**
     * Specify the expressions that are used to form groups over
     * the subquery results.
     * Replaces the previous specified grouping expressions, if any.
     * If no grouping expressions are specified, any previously 
     * added grouping expressions are simply removed.
     * This method only overrides the return type of the 
     * corresponding {@code AbstractQuery} method.
     * @param grouping  zero or more grouping expressions
     * @return the modified subquery
     */
    Subquery<T> groupBy(Expression<?>... grouping);

    /**
     * Specify the expressions that are used to form groups over
     * the subquery results.
     * Replaces the previous specified grouping expressions, if any.
     * If no grouping expressions are specified, any previously 
     * added grouping expressions are simply removed.
     * This method only overrides the return type of the 
     * corresponding {@code AbstractQuery} method.
     * @param grouping  list of zero or more grouping expressions
     * @return the modified subquery
     */
    Subquery<T> groupBy(List<Expression<?>> grouping);

    /**
     * Specify a restriction over the groups of the subquery.
     * Replaces the previous having restriction(s), if any.
     * This method only overrides the return type of the 
     * corresponding {@code AbstractQuery} method.
     * @param restriction  a simple or compound boolean expression
     * @return the modified subquery
     */
    Subquery<T> having(Expression<Boolean> restriction);

    /**
     * Specify restrictions over the groups of the subquery
     * according the conjunction of the specified restriction 
     * predicates.
     * Replaces the previously added having restriction(s), if any.
     * If no restrictions are specified, any previously added
     * restrictions are simply removed.
     * This method only overrides the return type of the 
     * corresponding {@code AbstractQuery} method.
     * @param restrictions  zero or more restriction predicates
     * @return the modified subquery
     */
    Subquery<T> having(Predicate... restrictions);

    /**
     * Specify restrictions over the groups of the query
     * according the conjunction of the specified restriction
     * predicates.
     * Replaces the previously added having restriction(s), if any.
     * If no restrictions are specified, any previously added
     * restrictions are simply removed.
     * This method only overrides the return type of the
     * corresponding {@code AbstractQuery} method.
     * @param restrictions  a list of zero or more restriction predicates
     * @return the modified query
     * @since 3.2
     */
    Subquery<T> having(List<Predicate> restrictions);

    /**
     * Specify whether duplicate query results are eliminated.
     * A true value will cause duplicates to be eliminated.
     * A false value will cause duplicates to be retained.
     * If distinct has not been specified, duplicate results must
     * be retained.
     * This method only overrides the return type of the 
     * corresponding {@code AbstractQuery} method.
     * @param distinct  boolean value specifying whether duplicate
     *        results must be eliminated from the subquery result or
     *        whether they must be retained
     * @return the modified subquery.
     */
    Subquery<T> distinct(boolean distinct);
	
    /**
     * Create a subquery root correlated to a root of the 
     * enclosing query.
     * @param parentRoot  a root of the containing query
     * @return subquery root
     */
    <Y> Root<Y> correlate(Root<Y> parentRoot);

    /**
     * Create a subquery join object correlated to a join object
     * of the enclosing query.
     * @param parentJoin  join object of the containing query
     * @return subquery join
     */
    <X, Y> Join<X, Y> correlate(Join<X, Y> parentJoin);

    /**
     * Create a subquery collection join object correlated to a 
     * collection join object of the enclosing query.
     * @param parentCollection  join object of the containing query
     * @return subquery join
     */
    <X, Y> CollectionJoin<X, Y> correlate(CollectionJoin<X, Y> parentCollection);

    /**
     * Create a subquery set join object correlated to a set join
     * object of the enclosing query.
     * @param parentSet  join object of the containing query
     * @return subquery join
     */
    <X, Y> SetJoin<X, Y> correlate(SetJoin<X, Y> parentSet);

    /**
     * Create a subquery list join object correlated to a list join
     * object of the enclosing query.
     * @param parentList join object of the containing query
     * @return subquery join
     */
    <X, Y> ListJoin<X, Y> correlate(ListJoin<X, Y> parentList);

    /**
     * Create a subquery map join object correlated to a map join
     * object of the enclosing query.
     * @param parentMap join object of the containing query
     * @return subquery join
     */
    <X, K, V> MapJoin<X, K, V> correlate(MapJoin<X, K, V> parentMap);

    /**
     * Return the query of which this is a subquery.
     * This must be a CriteriaQuery or a Subquery.
     * @return the enclosing query or subquery
     */
    AbstractQuery<?> getParent();

    /**
     * Return the query of which this is a subquery.
     * This may be a CriteriaQuery, CriteriaUpdate, CriteriaDelete,
     * or a Subquery.
     * @return the enclosing query or subquery
     * @since 2.1
     */
    CommonAbstractCriteria getContainingQuery();
	
    /**
     * Return the selection expression.
     * @return the item to be returned in the subquery result
     */
    Expression<T> getSelection();

    /**
     * Return the correlated joins of the subquery.
     * Returns empty set if the subquery has no correlated
     * joins.
     * Modifications to the set do not affect the query.
     * @return the correlated joins of the subquery
     */
    Set<Join<?, ?>> getCorrelatedJoins();

}

