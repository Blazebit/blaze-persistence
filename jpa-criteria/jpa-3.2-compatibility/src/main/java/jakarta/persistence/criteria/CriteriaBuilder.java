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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jakarta.persistence.Tuple;

/**
 * Used to construct criteria queries, compound selections, 
 * expressions, predicates, orderings.
 *
 * <p> Note that {@link Predicate} is used instead of
 * {@code Expression<Boolean>} in this API in
 * order to work around the fact that Java generics are not
 * compatible with varags.
 *
 * @since 2.0
 */
public interface CriteriaBuilder {

    /**
     * Create a {@link CriteriaQuery} object.
     * @return criteria query object
     */
    CriteriaQuery<Object> createQuery();

    /**
     * Create a {@link CriteriaQuery} object with the given
     * result type.
     * @param resultClass  type of the query result
     * @return criteria query object
     */
    <T> CriteriaQuery<T> createQuery(Class<T> resultClass);

    /**
     * Create a {@link CriteriaQuery} object that returns a
     * tuple of objects as its result.
     * @return criteria query object
     */
    CriteriaQuery<Tuple> createTupleQuery();

    // methods to construct queries for bulk updates and deletes:

    /**
     * Create a {@link CriteriaUpdate} query object to perform a
     * bulk update operation.
     * @param targetEntity  target type for update operation
     * @return the query object
     * @since 2.1
     */
    <T> CriteriaUpdate<T> createCriteriaUpdate(Class<T> targetEntity);

    /**
     * Create a {@link CriteriaDelete} query object to perform a
     * bulk delete operation.
     * @param targetEntity  target type for delete operation
     * @return the query object
     * @since 2.1
     */
    <T> CriteriaDelete<T> createCriteriaDelete(Class<T> targetEntity);


    // selection construction methods:
	
    /**
     * Create a selection item corresponding to a constructor.
     * This method is used to specify a constructor that is
     * applied to the results of the query execution. If the
     * constructor is for an entity class, the resulting entities
     * will be in the new state after the query is executed.
     * @param resultClass  class whose instance is to be constructed
     * @param selections  arguments to the constructor
     * @return compound selection item
     * @throws IllegalArgumentException if an argument is a 
     *         tuple- or array-valued selection item
     */
    <Y> CompoundSelection<Y> construct(Class<Y> resultClass, Selection<?>... selections);

    /**
     * Create a tuple-valued selection item.
     * @param selections  selection items
     * @return tuple-valued compound selection
     * @throws IllegalArgumentException if an argument is a 
     *         tuple- or array-valued selection item
     */
    CompoundSelection<Tuple> tuple(Selection<?>... selections);

    /**
     * Create a tuple-valued selection item.
     * @param selections  list of selection items
     * @return tuple-valued compound selection
     * @throws IllegalArgumentException if an argument is a
     *         tuple- or array-valued selection item
     * @since 3.2
     */
    CompoundSelection<Tuple> tuple(List<Selection<?>> selections);

    /**
     * Create an array-valued selection item.
     * @param selections  selection items
     * @return array-valued compound selection
     * @throws IllegalArgumentException if an argument is a
     *         tuple- or array-valued selection item
     */
    CompoundSelection<Object[]> array(Selection<?>... selections);

    /**
     * Create an array-valued selection item.
     * @param selections  list of selection items
     * @return array-valued compound selection
     * @throws IllegalArgumentException if an argument is a
     *         tuple- or array-valued selection item
     * @since 3.2
     */
    CompoundSelection<Object[]> array(List<Selection<?>> selections);


    //ordering:
	
    /**
     * Create an ordering by the ascending value of the expression.
     * @param expression  expression used to define the ordering
     * @return ascending ordering corresponding to the expression
     */
    Order asc(Expression<?> expression);

    /**
     * Create an ordering by the descending value of the expression.
     * @param expression  expression used to define the ordering
     * @return descending ordering corresponding to the expression
     */
    Order desc(Expression<?> expression);

    /**
     * Create an ordering by the ascending value of the expression.
     * @param expression  expression used to define the ordering
     * @param nullPrecedence  the precedence of null values
     * @return ascending ordering corresponding to the expression
     * @since 3.2
     */
    Order asc(Expression<?> expression, Nulls nullPrecedence);

    /**
     * Create an ordering by the descending value of the expression.
     * @param expression  expression used to define the ordering
     * @param nullPrecedence  the precedence of null values
     * @return descending ordering corresponding to the expression
     * @since 3.2
     */
    Order desc(Expression<?> expression, Nulls nullPrecedence);


    //aggregate functions:
	
    /**
     * Create an aggregate expression applying the avg operation.
     * @param x  expression representing input value to avg operation
     * @return avg expression
     */
    <N extends Number> Expression<Double> avg(Expression<N> x);

    /**
     * Create an aggregate expression applying the sum operation.
     * @param x  expression representing input value to sum operation
     * @return sum expression
     */
    <N extends Number> Expression<N> sum(Expression<N> x);

    /**
     * Create an aggregate expression applying the sum operation to an
     * Integer-valued expression, returning a Long result.
     * @param x  expression representing input value to sum operation
     * @return sum expression
     */
    Expression<Long> sumAsLong(Expression<Integer> x);

    /**
     * Create an aggregate expression applying the sum operation to a
     * Float-valued expression, returning a Double result.
     * @param x  expression representing input value to sum operation
     * @return sum expression
     */
    Expression<Double> sumAsDouble(Expression<Float> x);
    
    /**
     * Create an aggregate expression applying the numerical max 
     * operation.
     * @param x  expression representing input value to max operation
     * @return max expression
     */
    <N extends Number> Expression<N> max(Expression<N> x);
    
    /**
     * Create an aggregate expression applying the numerical min 
     * operation.
     * @param x  expression representing input value to min operation
     * @return min expression
     */
    <N extends Number> Expression<N> min(Expression<N> x);

    /**
     * Create an aggregate expression for finding the greatest of
     * the values (strings, dates, etc).
     * @param x  expression representing input value to greatest
     *           operation
     * @return greatest expression
     */
    <X extends Comparable<? super X>> Expression<X> greatest(Expression<X> x);
    
    /**
     * Create an aggregate expression for finding the least of
     * the values (strings, dates, etc).
     * @param x  expression representing input value to least
     *           operation
     * @return least expression
     */
    <X extends Comparable<? super X>> Expression<X> least(Expression<X> x);

    /**
     * Create an aggregate expression applying the count operation.
     * @param x  expression representing input value to count 
     *           operation
     * @return count expression
     */
    Expression<Long> count(Expression<?> x);

    /**
     * Create an aggregate expression applying the count distinct 
     * operation.
     * @param x  expression representing input value to 
     *        count distinct operation
     * @return count distinct expression
     */
    Expression<Long> countDistinct(Expression<?> x);
	


    //subqueries:
	
    /**
     * Create a predicate testing the existence of a subquery result.
     * @param subquery  subquery whose result is to be tested
     * @return exists predicate
     */
    Predicate exists(Subquery<?> subquery);
	
    /**
     * Create an all expression over the subquery results.
     * @param subquery  subquery
     * @return all expression
     */
    <Y> Expression<Y> all(Subquery<Y> subquery);
	
    /**
     * Create a some expression over the subquery results.
     * This expression is equivalent to an {@code any} expression.
     * @param subquery  subquery
     * @return some expression
     */
    <Y> Expression<Y> some(Subquery<Y> subquery);
	
    /**
     * Create an any expression over the subquery results. 
     * This expression is equivalent to a {@code some} expression.
     * @param subquery  subquery
     * @return any expression
     */
    <Y> Expression<Y> any(Subquery<Y> subquery);


    //boolean functions:
	
    /**
     * Create a conjunction of the given boolean expressions.
     * @param x  boolean expression
     * @param y  boolean expression
     * @return and predicate
     */
    Predicate and(Expression<Boolean> x, Expression<Boolean> y);
    
    /**
     * Create a conjunction of the given restriction predicates.
     * A conjunction of zero predicates is true.
     * @param restrictions  zero or more restriction predicates
     * @return and predicate
     */
    Predicate and(Predicate... restrictions);

    /**
     * Create a conjunction of the given restriction predicates.
     * A conjunction of zero predicates is true.
     * @param restrictions  a list of zero or more restriction predicates
     * @return and predicate
     * @since 3.2
     */
    Predicate and(List<Predicate> restrictions);

    /**
     * Create a disjunction of the given boolean expressions.
     * @param x  boolean expression
     * @param y  boolean expression
     * @return or predicate
     */
    Predicate or(Expression<Boolean> x, Expression<Boolean> y);

    /**
     * Create a disjunction of the given restriction predicates.
     * A disjunction of zero predicates is false.
     * @param restrictions  zero or more restriction predicates
     * @return or predicate
     */
    Predicate or(Predicate... restrictions);

    /**
     * Create a disjunction of the given restriction predicates.
     * A disjunction of zero predicates is false.
     * @param restrictions  a list of zero or more restriction predicates
     * @return or predicate
     * @since 3.2
     */
    Predicate or(List<Predicate> restrictions);

    /**
     * Create a negation of the given restriction. 
     * @param restriction  restriction expression
     * @return not predicate
     */
    Predicate not(Expression<Boolean> restriction);
	
    /**
     * Create a conjunction (with zero conjuncts).
     * A conjunction with zero conjuncts is true.
     * @return and predicate
     */
    Predicate conjunction();

    /**
     * Create a disjunction (with zero disjuncts).
     * A disjunction with zero disjuncts is false.
     * @return or predicate
     */
    Predicate disjunction();

	
    //turn Expression<Boolean> into a Predicate
    //useful for use with varargs methods

    /**
     * Create a predicate testing for a true value.
     * @param x  expression to be tested
     * @return predicate
     */
    Predicate isTrue(Expression<Boolean> x);

    /**
     * Create a predicate testing for a false value.
     * @param x  expression to be tested
     * @return predicate
     */
    Predicate isFalse(Expression<Boolean> x);

	
    //null tests:

    /**
     * Create a predicate to test whether the expression is null.
     * @param x expression
     * @return is-null predicate
     */
    Predicate isNull(Expression<?> x);

    /**
     * Create a predicate to test whether the expression is not null.
     * @param x expression
     * @return is-not-null predicate
     */
    Predicate isNotNull(Expression<?> x);

    //equality:
	
    /**
     * Create a predicate for testing the arguments for equality.
     * @param x  expression
     * @param y  expression
     * @return equality predicate
     */
    Predicate equal(Expression<?> x, Expression<?> y);
	
    /**
     * Create a predicate for testing the arguments for equality.
     * @param x  expression
     * @param y  object
     * @return equality predicate
     */
    Predicate equal(Expression<?> x, Object y);

    /**
     * Create a predicate for testing the arguments for inequality.
     * @param x  expression
     * @param y  expression
     * @return inequality predicate
     */
    Predicate notEqual(Expression<?> x, Expression<?> y);
	
    /**
     * Create a predicate for testing the arguments for inequality.
     * @param x  expression
     * @param y  object
     * @return inequality predicate
     */
    Predicate notEqual(Expression<?> x, Object y);

	
    //comparisons for generic (non-numeric) operands:

    /**
     * Create a predicate for testing whether the first argument is 
     * greater than the second.
     * @param x  expression
     * @param y  expression
     * @return greater-than predicate
     */
    <Y extends Comparable<? super Y>> Predicate greaterThan(Expression<? extends Y> x, Expression<? extends Y> y);
	
    /**
     * Create a predicate for testing whether the first argument is 
     * greater than the second.
     * @param x  expression
     * @param y  value
     * @return greater-than predicate
     */
    <Y extends Comparable<? super Y>> Predicate greaterThan(Expression<? extends Y> x, Y y);
    
    /**
     * Create a predicate for testing whether the first argument is 
     * greater than or equal to the second.
     * @param x  expression
     * @param y  expression
     * @return greater-than-or-equal predicate
     */
    <Y extends Comparable<? super Y>> Predicate greaterThanOrEqualTo(Expression<? extends Y> x, Expression<? extends Y> y);

    /**
     * Create a predicate for testing whether the first argument is 
     * greater than or equal to the second.
     * @param x  expression
     * @param y  value
     * @return greater-than-or-equal predicate
     */
    <Y extends Comparable<? super Y>> Predicate greaterThanOrEqualTo(Expression<? extends Y> x, Y y);

    /**
     * Create a predicate for testing whether the first argument is 
     * less than the second.
     * @param x  expression
     * @param y  expression
     * @return less-than predicate
     */
    <Y extends Comparable<? super Y>> Predicate lessThan(Expression<? extends Y> x, Expression<? extends Y> y);

    /**
     * Create a predicate for testing whether the first argument is 
     * less than the second.
     * @param x  expression
     * @param y  value
     * @return less-than predicate
     */
    <Y extends Comparable<? super Y>> Predicate lessThan(Expression<? extends Y> x, Y y);
	
    /**
     * Create a predicate for testing whether the first argument is 
     * less than or equal to the second.
     * @param x  expression
     * @param y  expression
     * @return less-than-or-equal predicate
     */
    <Y extends Comparable<? super Y>> Predicate lessThanOrEqualTo(Expression<? extends Y> x, Expression<? extends Y> y);

    /**
     * Create a predicate for testing whether the first argument is 
     * less than or equal to the second.
     * @param x  expression
     * @param y  value
     * @return less-than-or-equal predicate
     */
    <Y extends Comparable<? super Y>> Predicate lessThanOrEqualTo(Expression<? extends Y> x, Y y);

    /**
     * Create a predicate for testing whether the first argument is 
     * between the second and third arguments in value.
     * @param v  expression 
     * @param x  expression
     * @param y  expression
     * @return between predicate
     */
    <Y extends Comparable<? super Y>> Predicate between(Expression<? extends Y> v, Expression<? extends Y> x, Expression<? extends Y> y);

    /**
     * Create a predicate for testing whether the first argument is 
     * between the second and third arguments in value.
     * @param v  expression 
     * @param x  value
     * @param y  value
     * @return between predicate
     */
    <Y extends Comparable<? super Y>> Predicate between(Expression<? extends Y> v, Y x, Y y);
	

    //comparisons for numeric operands:
	
    /**
     * Create a predicate for testing whether the first argument is 
     * greater than the second.
     * @param x  expression
     * @param y  expression
     * @return greater-than predicate
     */
    Predicate gt(Expression<? extends Number> x, Expression<? extends Number> y);

    /**
     * Create a predicate for testing whether the first argument is 
     * greater than the second.
     * @param x  expression
     * @param y  value
     * @return greater-than predicate
     */
    Predicate gt(Expression<? extends Number> x, Number y);

    /**
     * Create a predicate for testing whether the first argument is 
     * greater than or equal to the second.
     * @param x  expression
     * @param y  expression
     * @return greater-than-or-equal predicate
     */
    Predicate ge(Expression<? extends Number> x, Expression<? extends Number> y);

    /**
     * Create a predicate for testing whether the first argument is 
     * greater than or equal to the second.
     * @param x  expression
     * @param y  value
     * @return greater-than-or-equal predicate
     */	
    Predicate ge(Expression<? extends Number> x, Number y);

    /**
     * Create a predicate for testing whether the first argument is 
     * less than the second.
     * @param x  expression
     * @param y  expression
     * @return less-than predicate
     */
    Predicate lt(Expression<? extends Number> x, Expression<? extends Number> y);

    /**
     * Create a predicate for testing whether the first argument is 
     * less than the second.
     * @param x  expression
     * @param y  value
     * @return less-than predicate
     */
    Predicate lt(Expression<? extends Number> x, Number y);

    /**
     * Create a predicate for testing whether the first argument is 
     * less than or equal to the second.
     * @param x  expression
     * @param y  expression
     * @return less-than-or-equal predicate
     */
    Predicate le(Expression<? extends Number> x, Expression<? extends Number> y);

    /**
     * Create a predicate for testing whether the first argument is 
     * less than or equal to the second.
     * @param x  expression
     * @param y  value
     * @return less-than-or-equal predicate
     */
    Predicate le(Expression<? extends Number> x, Number y);
	

    //numerical operations:

    /**
     * Create an expression that returns the sign of its
     * argument, that is, {@code 1} if its argument is
     * positive, {@code -1} if its argument is negative,
     * or {@code 0} if its argument is exactly zero.
     * @param x expression
     * @return sign
     */
    Expression<Integer> sign(Expression<? extends Number> x);
	
    /**
     * Create an expression that returns the arithmetic negation
     * of its argument.
     * @param x expression
     * @return arithmetic negation
     */
    <N extends Number> Expression<N> neg(Expression<N> x);

    /**
     * Create an expression that returns the absolute value
     * of its argument.
     * @param x expression
     * @return absolute value
     */
    <N extends Number> Expression<N> abs(Expression<N> x);

    /**
     * Create an expression that returns the ceiling of its
     * argument, that is, the smallest integer greater than
     * or equal to its argument.
     * @param x expression
     * @return ceiling
     */
    <N extends Number> Expression<N> ceiling(Expression<N> x);

    /**
     * Create an expression that returns the floor of its
     * argument, that is, the largest integer smaller than
     * or equal to its argument.
     * @param x expression
     * @return floor
     */
    <N extends Number> Expression<N> floor(Expression<N> x);
    /**
     * Create an expression that returns the sum
     * of its arguments.
     * @param x expression
     * @param y expression
     * @return sum
     */
    <N extends Number> Expression<N> sum(Expression<? extends N> x, Expression<? extends N> y);
	
    /**
     * Create an expression that returns the sum
     * of its arguments.
     * @param x expression
     * @param y value
     * @return sum
     */
    <N extends Number> Expression<N> sum(Expression<? extends N> x, N y);

    /**
     * Create an expression that returns the sum
     * of its arguments.
     * @param x value
     * @param y expression
     * @return sum
     */
    <N extends Number> Expression<N> sum(N x, Expression<? extends N> y);

    /**
     * Create an expression that returns the product
     * of its arguments.
     * @param x expression
     * @param y expression
     * @return product
     */
    <N extends Number> Expression<N> prod(Expression<? extends N> x, Expression<? extends N> y);

    /**
     * Create an expression that returns the product
     * of its arguments.
     * @param x expression
     * @param y value
     * @return product
     */
    <N extends Number> Expression<N> prod(Expression<? extends N> x, N y);

    /**
     * Create an expression that returns the product
     * of its arguments.
     * @param x value
     * @param y expression
     * @return product
     */
    <N extends Number> Expression<N> prod(N x, Expression<? extends N> y);

    /**
     * Create an expression that returns the difference
     * between its arguments.
     * @param x expression
     * @param y expression
     * @return difference
     */
    <N extends Number> Expression<N> diff(Expression<? extends N> x, Expression<? extends N> y);

    /**
     * Create an expression that returns the difference
     * between its arguments.
     * @param x expression
     * @param y value
     * @return difference
     */
    <N extends Number> Expression<N> diff(Expression<? extends N> x, N y);

    /**
     * Create an expression that returns the difference
     * between its arguments.
     * @param x value
     * @param y expression
     * @return difference
     */
    <N extends Number> Expression<N> diff(N x, Expression<? extends N> y);
	
    /**
     * Create an expression that returns the quotient
     * of its arguments.
     * @param x expression
     * @param y expression
     * @return quotient
     */
    Expression<Number> quot(Expression<? extends Number> x, Expression<? extends Number> y);

    /**
     * Create an expression that returns the quotient
     * of its arguments.
     * @param x expression
     * @param y value
     * @return quotient
     */
    Expression<Number> quot(Expression<? extends Number> x, Number y);

    /**
     * Create an expression that returns the quotient
     * of its arguments.
     * @param x value
     * @param y expression
     * @return quotient
     */
    Expression<Number> quot(Number x, Expression<? extends Number> y);
	
    /**
     * Create an expression that returns the modulus
     * (remainder under integer division) of its
     * arguments.
     * @param x expression
     * @param y expression
     * @return modulus
     */
    Expression<Integer> mod(Expression<Integer> x, Expression<Integer> y);
	
    /**
     * Create an expression that returns the modulus
     * (remainder under integer division) of its
     * arguments.
     * @param x expression
     * @param y value
     * @return modulus
     */
    Expression<Integer> mod(Expression<Integer> x, Integer y);

    /**
     * Create an expression that returns the modulus
     * (remainder under integer division) of its
     * arguments.
     * @param x value
     * @param y expression
     * @return modulus
     */
    Expression<Integer> mod(Integer x, Expression<Integer> y);

    /**
     * Create an expression that returns the square root
     * of its argument.
     * @param x expression
     * @return square root
     */	
    Expression<Double> sqrt(Expression<? extends Number> x);

    /**
     * Create an expression that returns the exponential
     * of its argument, that is, Euler's number <i>e</i>
     * raised to the power of its argument.
     * @param x expression
     * @return exponential
     */
    Expression<Double> exp(Expression<? extends Number> x);

    /**
     * Create an expression that returns the natural logarithm
     * of its argument.
     * @param x expression
     * @return natural logarithm
     */
    Expression<Double> ln(Expression<? extends Number> x);

    /**
     * Create an expression that returns the first argument
     * raised to the power of its second argument.
     * @param x base
     * @param y exponent
     * @return the base raised to the power of the exponent
     */
    Expression<Double> power(Expression<? extends Number> x, Expression<? extends Number> y);

    /**
     * Create an expression that returns the first argument
     * raised to the power of its second argument.
     * @param x base
     * @param y exponent
     * @return the base raised to the power of the exponent
     */
    Expression<Double> power(Expression<? extends Number> x, Number y);

    /**
     * Create an expression that returns the first argument
     * rounded to the number of decimal places given by the
     * second argument.
     * @param x base
     * @param n number of decimal places
     * @return the rounded value
     */
    <T extends Number> Expression<T> round(Expression<T> x, Integer n);


    //typecasts:
    
    /**
     * Typecast.  Returns same expression object.
     * @param number  numeric expression
     * @return {@literal Expression<Long>}
     */
    Expression<Long> toLong(Expression<? extends Number> number);

    /**
     * Typecast.  Returns same expression object.
     * @param number  numeric expression
     * @return {@literal Expression<Integer>}
     */
    Expression<Integer> toInteger(Expression<? extends Number> number);

    /**
     * Typecast. Returns same expression object.
     * @param number  numeric expression
     * @return {@literal Expression<Float>}
     */
    Expression<Float> toFloat(Expression<? extends Number> number);

    /**
     * Typecast.  Returns same expression object.
     * @param number  numeric expression
     * @return {@literal Expression<Double>}
     */
    Expression<Double> toDouble(Expression<? extends Number> number);

    /**
     * Typecast.  Returns same expression object.
     * @param number  numeric expression
     * @return {@literal Expression<BigDecimal>}
     */
    Expression<BigDecimal> toBigDecimal(Expression<? extends Number> number);

    /**
     * Typecast.  Returns same expression object.
     * @param number  numeric expression
     * @return {@literal Expression<BigInteger>}
     */
    Expression<BigInteger> toBigInteger(Expression<? extends Number> number);
	
    /**
     * Typecast.  Returns same expression object.
     * @param character expression
     * @return {@literal Expression<String>}
     */
    Expression<String> toString(Expression<Character> character);

	
    //literals:

    /**
     * Create an expression for a literal.
     * @param value  value represented by the expression
     * @return expression literal
     * @throws IllegalArgumentException if value is null
     */
    <T> Expression<T> literal(T value);

    /**
     * Create an expression for a null literal with the given type.
     * @param resultClass  type of the null literal
     * @return null expression literal
     */
    <T> Expression<T> nullLiteral(Class<T> resultClass);

    //parameters:

    /**
     * Create a parameter expression.
     * @param paramClass parameter class
     * @return parameter expression
     */
    <T> ParameterExpression<T> parameter(Class<T> paramClass);

    /**
     * Create a parameter expression with the given name.
     * @param paramClass parameter class
     * @param name  name that can be used to refer to 
     *              the parameter
     * @return parameter expression
     */
    <T> ParameterExpression<T> parameter(Class<T> paramClass, String name);


    //collection operations:
	
    /**
     * Create a predicate that tests whether a collection is empty.
     * @param collection expression
     * @return is-empty predicate
     */
    <C extends Collection<?>> Predicate isEmpty(Expression<C> collection);

    /**
     * Create a predicate that tests whether a collection is
     * not empty.
     * @param collection expression
     * @return is-not-empty predicate
     */
    <C extends Collection<?>> Predicate isNotEmpty(Expression<C> collection);

    /**
     * Create an expression that tests the size of a collection.
     * @param collection expression
     * @return size expression
     */ 
    <C extends Collection<?>> Expression<Integer> size(Expression<C> collection);
	
    /**
     * Create an expression that tests the size of a collection.
     * @param collection collection
     * @return size expression
     */ 
    <C extends Collection<?>> Expression<Integer> size(C collection);
	
    /**
     * Create a predicate that tests whether an element is
     * a member of a collection.
     * If the collection is empty, the predicate will be false.
     * @param elem element expression
     * @param collection expression
     * @return is-member predicate
     */
    <E, C extends Collection<E>> Predicate isMember(Expression<E> elem, Expression<C> collection);

    /**
     * Create a predicate that tests whether an element is
     * a member of a collection.
     * If the collection is empty, the predicate will be false.
     * @param elem element
     * @param collection expression
     * @return is-member predicate
     */
    <E, C extends Collection<E>> Predicate isMember(E elem, Expression<C> collection);

    /**
     * Create a predicate that tests whether an element is
     * not a member of a collection.
     * If the collection is empty, the predicate will be true.
     * @param elem element expression
     * @param collection expression
     * @return is-not-member predicate
     */
    <E, C extends Collection<E>> Predicate isNotMember(Expression<E> elem, Expression<C> collection);
	
    /**
     * Create a predicate that tests whether an element is
     * not a member of a collection.
     * If the collection is empty, the predicate will be true.
     * @param elem element
     * @param collection expression
     * @return is-not-member predicate
     */
    <E, C extends Collection<E>> Predicate isNotMember(E elem, Expression<C> collection);


    //get the values and keys collections of the Map, which may then
    //be passed to size(), isMember(), isEmpty(), etc

    /**
     * Create an expression that returns the values of a map.
     * @param map  map
     * @return collection expression
     */
    <V, M extends Map<?, V>> Expression<Collection<V>> values(M map);

    /**
     * Create an expression that returns the keys of a map.
     * @param map  map
     * @return set expression
     */
    <K, M extends Map<K, ?>> Expression<Set<K>> keys(M map);

	
    //string functions:
	
    /**
     * Create a predicate for testing whether the expression
     * satisfies the given pattern.
     * @param x  string expression
     * @param pattern  string expression
     * @return like predicate
     */
    Predicate like(Expression<String> x, Expression<String> pattern);
	
    /**
     * Create a predicate for testing whether the expression
     * satisfies the given pattern.
     * @param x  string expression
     * @param pattern  string 
     * @return like predicate
     */
    Predicate like(Expression<String> x, String pattern);
	
    /**
     * Create a predicate for testing whether the expression
     * satisfies the given pattern.
     * @param x  string expression
     * @param pattern  string expression
     * @param escapeChar  escape character expression
     * @return like predicate
     */
    Predicate like(Expression<String> x, Expression<String> pattern, Expression<Character> escapeChar);
	
    /**
     * Create a predicate for testing whether the expression
     * satisfies the given pattern.
     * @param x  string expression
     * @param pattern  string expression
     * @param escapeChar  escape character
     * @return like predicate
     */
    Predicate like(Expression<String> x, Expression<String> pattern, char escapeChar);
	
    /**
     * Create a predicate for testing whether the expression
     * satisfies the given pattern.
     * @param x  string expression
     * @param pattern  string 
     * @param escapeChar  escape character expression
     * @return like predicate
     */
    Predicate like(Expression<String> x, String pattern, Expression<Character> escapeChar);

    /**
     * Create a predicate for testing whether the expression
     * satisfies the given pattern.
     * @param x  string expression
     * @param pattern  string 
     * @param escapeChar  escape character
     * @return like predicate
     */
    Predicate like(Expression<String> x, String pattern, char escapeChar);
	
    /**
     * Create a predicate for testing whether the expression
     * does not satisfy the given pattern.
     * @param x  string expression
     * @param pattern  string expression
     * @return not-like predicate
     */
    Predicate notLike(Expression<String> x, Expression<String> pattern);
	
    /**
     * Create a predicate for testing whether the expression
     * does not satisfy the given pattern.
     * @param x  string expression
     * @param pattern  string 
     * @return not-like predicate
     */
    Predicate notLike(Expression<String> x, String pattern);

    /**
     * Create a predicate for testing whether the expression
     * does not satisfy the given pattern.
     * @param x  string expression
     * @param pattern  string expression
     * @param escapeChar  escape character expression
     * @return not-like predicate
     */
    Predicate notLike(Expression<String> x, Expression<String> pattern, Expression<Character> escapeChar);

    /**
     * Create a predicate for testing whether the expression
     * does not satisfy the given pattern.
     * @param x  string expression
     * @param pattern  string expression
     * @param escapeChar  escape character
     * @return not-like predicate
     */
    Predicate notLike(Expression<String> x, Expression<String> pattern, char escapeChar);

    /**
     * Create a predicate for testing whether the expression
     * does not satisfy the given pattern.
     * @param x  string expression
     * @param pattern  string 
     * @param escapeChar  escape character expression
     * @return not-like predicate
     */
    Predicate notLike(Expression<String> x, String pattern, Expression<Character> escapeChar);
	
   /**
     * Create a predicate for testing whether the expression
     * does not satisfy the given pattern.
     * @param x  string expression
     * @param pattern  string 
     * @param escapeChar  escape character
     * @return not-like predicate
     */
    Predicate notLike(Expression<String> x, String pattern, char escapeChar);

    /**
     * Create an expression for string concatenation.
     * If the given list of expressions is empty, returns
     * an expression equivalent to {@code literal("")}.
     * @param expressions  string expressions
     * @return expression corresponding to concatenation
     */
    Expression<String> concat(List<Expression<String>> expressions);

    /**
     * Create an expression for string concatenation.
     * @param x  string expression
     * @param y  string expression
     * @return expression corresponding to concatenation
     */
    Expression<String> concat(Expression<String> x, Expression<String> y);
	
    /**
     * Create an expression for string concatenation.
     * @param x  string expression
     * @param y  string 
     * @return expression corresponding to concatenation
     */
    Expression<String> concat(Expression<String> x, String y);

    /**
     * Create an expression for string concatenation.
     * @param x  string 
     * @param y  string expression
     * @return expression corresponding to concatenation
     */
    Expression<String> concat(String x, Expression<String> y);
	
    /**
     * Create an expression for substring extraction.
     * Extracts a substring starting at the specified position
     * through to end of the string.
     * First position is 1.
     * @param x  string expression
     * @param from  start position expression 
     * @return expression corresponding to substring extraction
     */
    Expression<String> substring(Expression<String> x, Expression<Integer> from);
	
    /**
     * Create an expression for substring extraction.
     * Extracts a substring starting at the specified position
     * through to end of the string.
     * First position is 1.
     * @param x  string expression
     * @param from  start position 
     * @return expression corresponding to substring extraction
     */
    Expression<String> substring(Expression<String> x, int from);

    /**
     * Create an expression for substring extraction.
     * Extracts a substring of given length starting at the
     * specified position.
     * First position is 1.
     * @param x  string expression
     * @param from  start position expression 
     * @param len  length expression
     * @return expression corresponding to substring extraction
     */
    Expression<String> substring(Expression<String> x, Expression<Integer> from, Expression<Integer> len);
	
    /**
     * Create an expression for substring extraction.
     * Extracts a substring of given length starting at the
     * specified position.
     * First position is 1.
     * @param x  string expression
     * @param from  start position 
     * @param len  length
     * @return expression corresponding to substring extraction
     */
    Expression<String> substring(Expression<String> x, int from, int len);
	
    /**
     * Used to specify how strings are trimmed.
     */
    enum Trimspec {

        /**
         * Trim from leading end.
         */
        LEADING,
 
        /**
         * Trim from trailing end.
         */
        TRAILING, 

        /**
         * Trim from both ends.
         */
        BOTH 
    }
	
    /**
     * Create expression to trim blanks from both ends of
     * a string.
     * @param x  expression for string to trim
     * @return trim expression
     */
    Expression<String> trim(Expression<String> x);
	
    /**
     * Create expression to trim blanks from a string.
     * @param ts  trim specification
     * @param x  expression for string to trim
     * @return trim expression
     */
    Expression<String> trim(Trimspec ts, Expression<String> x);

    /**
     * Create expression to trim character from both ends of
     * a string.
     * @param t  expression for character to be trimmed
     * @param x  expression for string to trim
     * @return trim expression
     */
    Expression<String> trim(Expression<Character> t, Expression<String> x);

    /**
     * Create expression to trim character from a string.
     * @param ts  trim specification
     * @param t  expression for character to be trimmed
     * @param x  expression for string to trim
     * @return trim expression
     */
    Expression<String> trim(Trimspec ts, Expression<Character> t, Expression<String> x);
	
    /**
     * Create expression to trim character from both ends of
     * a string.
     * @param t  character to be trimmed
     * @param x  expression for string to trim
     * @return trim expression
     */
    Expression<String> trim(char t, Expression<String> x);
	
    /**
     * Create expression to trim character from a string.
     * @param ts  trim specification
     * @param t  character to be trimmed
     * @param x  expression for string to trim
     * @return trim expression
     */
    Expression<String> trim(Trimspec ts, char t, Expression<String> x);
	
    /**
     * Create expression for converting a string to lowercase.
     * @param x  string expression
     * @return expression to convert to lowercase
     */
    Expression<String> lower(Expression<String> x);
	
    /**
     * Create expression for converting a string to uppercase.
     * @param x  string expression
     * @return expression to convert to uppercase
     */
    Expression<String> upper(Expression<String> x);
	
    /**
     * Create expression to return length of a string.
     * @param x  string expression
     * @return length expression
     */
    Expression<Integer> length(Expression<String> x);

    /**
     * Create an expression for the leftmost substring of a string,
     * @param x  string expression
     * @param len  length of the substring to return
     * @return expression for the leftmost substring
     */
    Expression<String> left(Expression<String> x, int len);

    /**
     * Create an expression for the rightmost substring of a string,
     * @param x  string expression
     * @param len  length of the substring to return
     * @return expression for the rightmost substring
     */
    Expression<String> right(Expression<String> x, int len);

    /**
     * Create an expression for the leftmost substring of a string,
     * @param x  string expression
     * @param len  length of the substring to return
     * @return expression for the leftmost substring
     */
    Expression<String> left(Expression<String> x, Expression<Integer> len);

    /**
     * Create an expression for the rightmost substring of a string,
     * @param x  string expression
     * @param len  length of the substring to return
     * @return expression for the rightmost substring
     */
    Expression<String> right(Expression<String> x, Expression<Integer> len);

    /**
     * Create an expression replacing every occurrence of a substring
     * within a string.
     * @param x  string expression
     * @param substring  the literal substring to replace
     * @param replacement  the replacement string
     * @return expression for the resulting string
     */
    Expression<String> replace(Expression<String> x, Expression<String> substring, Expression<String> replacement);

    /**
     * Create an expression replacing every occurrence of a substring
     * within a string.
     * @param x  string expression
     * @param substring  the literal substring to replace
     * @param replacement  the replacement string
     * @return expression for the resulting string
     */
    Expression<String> replace(Expression<String> x, String substring, Expression<String> replacement);

    /**
     * Create an expression replacing every occurrence of a substring
     * within a string.
     * @param x  string expression
     * @param substring  the literal substring to replace
     * @param replacement  the replacement string
     * @return expression for the resulting string
     */
    Expression<String> replace(Expression<String> x, Expression<String> substring, String replacement);

    /**
     * Create an expression replacing every occurrence of a substring
     * within a string.
     * @param x  string expression
     * @param substring  the literal substring to replace
     * @param replacement  the replacement string
     * @return expression for the resulting string
     */
    Expression<String> replace(Expression<String> x, String substring, String replacement);


    /**
     * Create expression to locate the position of one string
     * within another, returning position of first character
     * if found.
     * The first position in a string is denoted by 1.  If the
     * string to be located is not found, 0 is returned.
     * <p><strong>Warning:</strong> the order of the parameters
     * of this method is reversed compared to the corresponding
     * function in JPQL.
     * @param x  expression for string to be searched
     * @param pattern  expression for string to be located
     * @return expression corresponding to position
     */
    Expression<Integer> locate(Expression<String> x, Expression<String> pattern);
	
    /**
     * Create expression to locate the position of one string
     * within another, returning position of first character
     * if found.
     * The first position in a string is denoted by 1.  If the
     * string to be located is not found, 0 is returned.
     * <p><strong>Warning:</strong> the order of the parameters
     * of this method is reversed compared to the corresponding
     * function in JPQL.
     * @param x  expression for string to be searched
     * @param pattern  string to be located
     * @return expression corresponding to position
     */
    Expression<Integer> locate(Expression<String> x, String pattern);

    /**
     * Create expression to locate the position of one string
     * within another, returning position of first character
     * if found.
     * The first position in a string is denoted by 1.  If the
     * string to be located is not found, 0 is returned.
     * <p><strong>Warning:</strong> the order of the first two
     * parameters of this method is reversed compared to the
     * corresponding function in JPQL.
     * @param x  expression for string to be searched
     * @param pattern  expression for string to be located
     * @param from  expression for position at which to start search
     * @return expression corresponding to position
     */
    Expression<Integer> locate(Expression<String> x, Expression<String> pattern, Expression<Integer> from);

    /**
     * Create expression to locate the position of one string
     * within another, returning position of first character
     * if found.
     * The first position in a string is denoted by 1.  If the
     * string to be located is not found, 0 is returned.
     * <p><strong>Warning:</strong> the order of the first two
     * parameters of this method is reversed compared to the
     * corresponding function in JPQL.
     * @param x  expression for string to be searched
     * @param pattern  string to be located
     * @param from  position at which to start search
     * @return expression corresponding to position
     */	
    Expression<Integer> locate(Expression<String> x, String pattern, int from);
	

    // Date/time/timestamp functions:

    /**
     * Create expression to return current date.
     * @return expression for current date
     */
    Expression<java.sql.Date> currentDate();

    /**
     * Create expression to return current timestamp.
     * @return expression for current timestamp
     */	
    Expression<java.sql.Timestamp> currentTimestamp();

    /**
     * Create expression to return current time.
     * @return expression for current time
     */	
    Expression<java.sql.Time> currentTime();

    /**
     * Create expression to return current local date.
     * @return expression for current date
     */
    Expression<java.time.LocalDate> localDate();

    /**
     * Create expression to return current local datetime.
     * @return expression for current timestamp
     */
    Expression<java.time.LocalDateTime> localDateTime();

    /**
     * Create expression to return current local time.
     * @return expression for current time
     */
    Expression<java.time.LocalTime> localTime();

    /**
     * Create an expression that returns the value of a
     * field extracted from a date, time, or datetime.
     * @param field a temporal field type
     * @param temporal a date, time, or datetime
     * @return expression for the value of the extracted field
     * @since 3.2
     */
    <N,T extends Temporal> Expression<N> extract(TemporalField<N,T> field, Expression<T> temporal);
	

    //in builders:
	
    /**
     * Interface used to build in predicates.
     */
    interface In<T> extends Predicate {

         /**
          * Return the expression to be tested against the
          * list of values.
          * @return expression
          */
         Expression<T> getExpression();
	
         /**
          * Add to list of values to be tested against.
          * @param value value
          * @return in predicate
          */
         In<T> value(T value);

         /**
          * Add to list of values to be tested against.
          * @param value expression
          * @return in predicate
          */
         In<T> value(Expression<? extends T> value);
     }
	
    /**
     * Create predicate to test whether given expression
     * is contained in a list of values.
     * @param  expression to be tested against list of values
     * @return  in predicate
     */
    <T> In<T> in(Expression<? extends T> expression);
	

    // coalesce, nullif:

    /**
     * Create an expression that returns null if all its arguments
     * evaluate to null, and the value of the first non-null argument
     * otherwise.
     * @param x expression
     * @param y expression
     * @return coalesce expression
     */
    <Y> Expression<Y> coalesce(Expression<? extends Y> x, Expression<? extends Y> y);

    /**
     * Create an expression that returns null if all its arguments
     * evaluate to null, and the value of the first non-null argument
     * otherwise.
     * @param x expression
     * @param y value
     * @return coalesce expression
     */
    <Y> Expression<Y> coalesce(Expression<? extends Y> x, Y y);
    
    /**
     * Create an expression that tests whether its argument are
     * equal, returning null if they are and the value of the
     * first expression if they are not.
     * @param x expression
     * @param y expression
     * @return nullif expression
     */
    <Y> Expression<Y> nullif(Expression<Y> x, Expression<?> y);

    /**
     * Create an expression that tests whether its argument are
     * equal, returning null if they are and the value of the
     * first expression if they are not.
     * @param x expression
     * @param y value
     * @return nullif expression 
     */
    <Y> Expression<Y> nullif(Expression<Y> x, Y y);


    // coalesce builder:

    /**
     * Interface used to build coalesce expressions.  
     *  
     * A coalesce expression is equivalent to a case expression
     * that returns null if all its arguments evaluate to null,
     * and the value of its first non-null argument otherwise.
     */
    interface Coalesce<T> extends Expression<T> {

         /**
          * Add an argument to the coalesce expression.
          * @param value  value
          * @return coalesce expression
          */
         Coalesce<T> value(T value);

         /**
          * Add an argument to the coalesce expression.
          * @param value expression
          * @return coalesce expression
          */
         Coalesce<T> value(Expression<? extends T> value);
	}
	
    /**
     * Create a coalesce expression.
     * @return coalesce expression
     */
    <T> Coalesce<T> coalesce();


    //case builders:

    /**
     * Interface used to build simple case expressions.
     * Case conditions are evaluated in the order in which
     * they are specified.
     */
    interface SimpleCase<C,R> extends Expression<R> {

		/**
		 * Return the expression to be tested against the
		 * conditions.
		 * @return expression
		 */
		Expression<C> getExpression();

		/**
		 * Add a when/then clause to the case expression.
		 * @param condition  "when" condition
		 * @param result  "then" result value
		 * @return simple case expression
		 */
		SimpleCase<C, R> when(C condition, R result);

		/**
		 * Add a when/then clause to the case expression.
		 * @param condition  "when" condition
		 * @param result  "then" result expression
		 * @return simple case expression
		 */
		SimpleCase<C, R> when(C condition, Expression<? extends R> result);

		/**
		 * Add a when/then clause to the case expression.
		 * @param condition  "when" condition
		 * @param result  "then" result value
		 * @return simple case expression
		 */
		SimpleCase<C, R> when(Expression<? extends C> condition, R result);

		/**
		 * Add a when/then clause to the case expression.
		 * @param condition  "when" condition
		 * @param result  "then" result expression
		 * @return simple case expression
		 */
		SimpleCase<C, R> when(Expression<? extends C> condition, Expression<? extends R> result);

		/**
		 * Add an "else" clause to the case expression.
		 * @param result  "else" result
		 * @return expression
		 */
		Expression<R> otherwise(R result);

		/**
		 * Add an "else" clause to the case expression.
		 * @param result  "else" result expression
		 * @return expression
		 */
		Expression<R> otherwise(Expression<? extends R> result);
	}
	
    /**
     * Create a simple case expression.
     * @param expression  to be tested against the case conditions
     * @return simple case expression
     */
    <C, R> SimpleCase<C,R> selectCase(Expression<? extends C> expression);


    /**
     * Interface used to build general case expressions.
     * Case conditions are evaluated in the order in which
     * they are specified.
     */
    interface Case<R> extends Expression<R> {

		/**
		 * Add a when/then clause to the case expression.
		 * @param condition  "when" condition
		 * @param result  "then" result value
		 * @return general case expression
		 */
		Case<R> when(Expression<Boolean> condition, R result);

		/**
		 * Add a when/then clause to the case expression.
		 * @param condition  "when" condition
		 * @param result  "then" result expression
		 * @return general case expression
		 */
		Case<R> when(Expression<Boolean> condition, Expression<? extends R> result);

		/**
		 * Add an "else" clause to the case expression.
		 * @param result  "else" result
		 * @return expression
		 */
		Expression<R> otherwise(R result);

		/**
		 * Add an "else" clause to the case expression.
		 * @param result  "else" result expression
		 * @return expression
		 */
		Expression<R> otherwise(Expression<? extends R> result);
	}
	
    /**
     * Create a general case expression.
     * @return general case expression
     */
    <R> Case<R> selectCase();

    /**
     * Create an expression for the execution of a database
     * function.
     * @param name  function name
     * @param type  expected result type
     * @param args  function arguments
     * @return expression
     */
   <T> Expression<T> function(String name, Class<T> type,
Expression<?>... args);


    // methods for downcasting:

    /**
     * Downcast Join object to the specified type.
     * @param join  Join object
     * @param type type to be downcast to
     * @return  Join object of the specified type
     * @since 2.1
     */
    <X, T, V extends T> Join<X, V> treat(Join<X, T> join, Class<V> type);

    /**
     * Downcast CollectionJoin object to the specified type.
     * @param join  CollectionJoin object
     * @param type type to be downcast to
     * @return  CollectionJoin object of the specified type
     * @since 2.1
     */
    <X, T, E extends T> CollectionJoin<X, E> treat(CollectionJoin<X, T> join, Class<E> type);

    /**
     * Downcast SetJoin object to the specified type.
     * @param join  SetJoin object
     * @param type type to be downcast to
     * @return  SetJoin object of the specified type
     * @since 2.1
     */
    <X, T, E extends T> SetJoin<X, E> treat(SetJoin<X, T> join, Class<E> type);

    /**
     * Downcast ListJoin object to the specified type.
     * @param join  ListJoin object
     * @param type type to be downcast to
     * @return  ListJoin object of the specified type
     * @since 2.1
     */
    <X, T, E extends T> ListJoin<X, E> treat(ListJoin<X, T> join, Class<E> type);

    /**
     * Downcast MapJoin object to the specified type.
     * @param join  MapJoin object
     * @param type type to be downcast to
     * @return  MapJoin object of the specified type
     * @since 2.1
     */
    <X, K, T, V extends T> MapJoin<X, K, V> treat(MapJoin<X, K, T> join, Class<V> type);


    /**
     * Downcast Path object to the specified type.
     * @param path  path
     * @param type type to be downcast to
     * @return  Path object of the specified type
     * @since 2.1
     */
    <X, T extends X> Path<T> treat(Path<X> path, Class<T> type);

    /**
     * Downcast Root object to the specified type.
     * @param root  root
     * @param type type to be downcast to
     * @return  Root object of the specified type
     * @since 2.1
     */
    <X, T extends X> Root<T> treat(Root<X> root, Class<T> type);

    /**
     * Create a query which is the union of the given queries.
     * @return a new criteria query which returns the union of
     *         the results of the given queries
     * @since 3.2
     */
    <T> CriteriaSelect<T> union(CriteriaSelect<? extends T> left, CriteriaSelect<? extends T> right);

    /**
     * Create a query which is the union of the given queries,
     * without elimination of duplicate results.
     * @return a new criteria query which returns the union of
     *         the results of the given queries
     * @since 3.2
     */
    <T> CriteriaSelect<T> unionAll(CriteriaSelect<? extends T> left, CriteriaSelect<? extends T> right);

    /**
     * Create a query which is the intersection of the given queries.
     * @return a new criteria query which returns the intersection of
     *         the results of the given queries
     * @since 3.2
     */
    <T> CriteriaSelect<T> intersect(CriteriaSelect<? super T> left, CriteriaSelect<? super T> right);

    /**
     * Create a query which is the intersection of the given queries,
     * without elimination of duplicate results.
     * @return a new criteria query which returns the intersection of
     *         the results of the given queries
     * @since 3.2
     */
    <T> CriteriaSelect<T> intersectAll(CriteriaSelect<? super T> left, CriteriaSelect<? super T> right);

    /**
     * Create a query by (setwise) subtraction of the second query
     * from the first query.
     * @return a new criteria query which returns the result of
     *         subtracting the results of the second query from the
     *         results of the first query
     * @since 3.2
     */
    <T> CriteriaSelect<T> except(CriteriaSelect<T> left, CriteriaSelect<?> right);

    /**
     * Create a query by (setwise) subtraction of the second query
     * from the first query, without elimination of duplicate results.
     * @return a new criteria query which returns the result of
     *         subtracting the results of the second query from the
     *         results of the first query
     * @since 3.2
     */
    <T> CriteriaSelect<T> exceptAll(CriteriaSelect<T> left, CriteriaSelect<?> right);
}




