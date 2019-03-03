/*
 * Copyright 2014 - 2019 Blazebit.
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

package com.blazebit.persistence.querydsl

import com.blazebit.persistence.*
import com.blazebit.persistence.spi.ServiceProvider
import com.mysema.query.jpa.JPQLSerializer
import com.mysema.query.types.*
import com.mysema.query.types.Path
import com.mysema.query.types.expr.*
import javax.persistence.EntityManager
import kotlin.math.exp
import kotlin.reflect.KClass

/**
 * Creates a new criteria builder with the given result class. The result class will be used as default from class.
 * The alias will be used as default alias for the from class.
 *
 * @param entityManager The entity manager to use for the criteria builder
 * @param entityPath Source of the query
 * @param <T> The type of the result class
 * @return A new criteria builder
 */
fun <T : Any> CriteriaBuilderFactory.create(entityManager : EntityManager, entityPath : EntityPath<T>) : CriteriaBuilder<T> {
    val alias = entityPath.metadata.name
    return this.create(entityManager, entityPath.type as Class<T>, alias)
}

/**
 * Create an inner join with the given target.
 *
 * @param path The path to join
 * @param alias The alias for the joined element
 * @return The query builder for chaining calls
 */
fun <T, A, P> T.innerJoin(path : CollectionExpression<*, P>, alias : Path<P>): A where T : FromBuilder<A>, T : ServiceProvider {
    val aliasName = alias.metadata.name
    val expression = parseExpressionAndBindParameters(path)
    return innerJoin(expression, aliasName)
}

/**
 * Create an inner join with the given target.
 *
 * @param path The path to join
 * @param alias The alias for the joined element
 * @return The query builder for chaining calls
 */
fun <T, A, P> T.innerJoin(path : MapExpression<*, P>, alias : Path<P>): A where T : FromBuilder<A>, T : ServiceProvider {
    val aliasName = alias.metadata.name
    val expression = parseExpressionAndBindParameters(path)
    return innerJoin(expression, aliasName)
}

/**
 * Create an inner join with the given target.
 *
 * @param path The path to join
 * @param alias The alias for the joined element
 * @return The query builder for chaining calls
 */
fun <T, A, P> T.innerJoinFetch(path : CollectionExpression<*, P>, alias : Path<P>): A where T : FullQueryBuilder<*, A> {
    val aliasName = alias.metadata.name
    val expression = parseExpressionAndBindParameters(path)
    return innerJoinFetch(expression, aliasName)
}

/**
 * Create an inner join with the given target.
 *
 * @param path The path to join
 * @param alias The alias for the joined element
 * @return The query builder for chaining calls
 */
fun <T, A, P> T.innerJoinFetch(path : MapExpression<*, P>, alias : Path<P>): A where T : FullQueryBuilder<*, A> {
    val aliasName = alias.metadata.name
    val expression = parseExpressionAndBindParameters(path)
    return innerJoinFetch(expression, aliasName)
}

/**
 * Create an inner join with the given target.
 *
 * @param path The path to join
 * @return The query builder for chaining calls
 */
fun <T, A, P> T.innerJoin(path : EntityPath<P>): A where T : FromBuilder<A>, T : ServiceProvider {
    return innerJoin(path, path)
}

/**
 * Create an inner join with the given target.
 *
 * @param path The path to join
 * @param alias The alias for the joined element
 * @return The query builder for chaining calls
 */
fun <T, A, P> T.innerJoin(path : EntityPath<P>, alias : Path<P>): A where T : FromBuilder<A>, T : ServiceProvider {
    val entityType = path.type
    val aliasName = alias.metadata.name

    return if (! path.isRoot()) {
        val expression = parseExpressionAndBindParameters(path)
        innerJoin(expression, aliasName)
    } else {
        innerJoinOn(entityType, aliasName).onTrue()
    }
}

/**
 * Create a left join with the given target.
 *
 * @param path The path to join
 * @param alias The alias for the joined element
 * @return The query builder for chaining calls
 */
fun <T, A, P> T.leftJoin(path : CollectionExpression<*, P>, alias : Path<P>): A where T : FromBuilder<A>, T : ServiceProvider {
    val aliasName = alias.metadata.name
    val expression = parseExpressionAndBindParameters(path)
    return leftJoin(expression, aliasName)
}

/**
 * Create a left join with the given target.
 *
 * @param path The path to join
 * @param alias The alias for the joined element
 * @return The query builder for chaining calls
 */
fun <T, A, P> T.leftJoin(path : MapExpression<*, P>, alias : Path<P>): A where T : FromBuilder<A>, T : ServiceProvider {
    val aliasName = alias.metadata.name
    val expression = parseExpressionAndBindParameters(path)
    return leftJoin(expression, aliasName)
}

/**
 * Create a left join with the given target.
 *
 * @param path The path to join
 * @param alias The alias for the joined element
 * @return The query builder for chaining calls
 */
fun <T, A, P> T.leftJoinFetch(path : CollectionExpression<*, P>, alias : Path<P>): A where T : FullQueryBuilder<*, A> {
    val aliasName = alias.metadata.name
    val expression = parseExpressionAndBindParameters(path)
    return leftJoinFetch(expression, aliasName)
}

/**
 * Create a left join with the given target.
 *
 * @param path The path to join
 * @param alias The alias for the joined element
 * @return The query builder for chaining calls
 */
fun <T, A, P> T.leftJoinFetch(path : MapExpression<*, P>, alias : Path<P>): A where T : FullQueryBuilder<*, A> {
    val aliasName = alias.metadata.name
    val expression = parseExpressionAndBindParameters(path)
    return leftJoinFetch(expression, aliasName)
}

/**
 * Create a left join with the given target.
 *
 * @param path The path to join
 * @return The query builder for chaining calls
 */
fun <T, A, P> T.leftJoin(path : EntityPath<P>): A where T : FromBuilder<A>, T : ServiceProvider {
    return leftJoin(path, path)
}
/**
 * Create a left join with the given target.
 *
 * @param path The path to join
 * @param alias The alias for the joined element
 * @return The query builder for chaining calls
 */
fun <T, A, P> T.leftJoin(path : EntityPath<P>, alias : Path<P>): A where T : FromBuilder<A>, T : ServiceProvider {
    val entityType = path.type

    val aliasName = alias.metadata.name

    return if (! path.isRoot()) {
        val expression = parseExpressionAndBindParameters(path)
        leftJoin(expression, aliasName)
    } else {
        leftJoinOn(entityType, aliasName).onTrue()
    }
}

private fun <T> JoinOnBuilder<T>.onTrue() : T {
    return on("true").eqExpression("true").end()
}

/**
 * Create a right join with the given target.
 *
 * @param path The path to join
 * @param alias The alias for the joined element
 * @return The query builder for chaining calls
 */
fun <T, A, P> T.rightJoin(path : CollectionExpression<*, P>, alias : Path<P>): A where T : FromBuilder<A>, T : ServiceProvider {
    val aliasName = alias.metadata.name
    val expression = parseExpressionAndBindParameters(path)
    return rightJoin(expression, aliasName)
}


/**
 * Create a right join with the given target.
 *
 * @param path The path to join
 * @param alias The alias for the joined element
 * @return The query builder for chaining calls
 */
fun <T, A, P> T.rightJoin(path : MapExpression<*, P>, alias : Path<P>): A where T : FromBuilder<A>, T : ServiceProvider {
    val aliasName = alias.metadata.name
    val expression = parseExpressionAndBindParameters(path)
    return rightJoin(expression, aliasName)
}


/**
 * Create a right join with the given target.
 *
 * @param path The path to join
 * @param alias The alias for the joined element
 * @return The query builder for chaining calls
 */
fun <T, A, P> T.rightJoinFetch(path : CollectionExpression<*, P>, alias : Path<P>): A where T : FullQueryBuilder<*, A> {
    val aliasName = alias.metadata.name
    val expression = parseExpressionAndBindParameters(path)
    return rightJoinFetch(expression, aliasName)
}

/**
 * Create a right join with the given target.
 *
 * @param path The path to join
 * @param alias The alias for the joined element
 * @return The query builder for chaining calls
 */
fun <T, A, P> T.rightJoinFetch(path : MapExpression<*, P>, alias : Path<P>): A where T : FullQueryBuilder<*, A> {
    val aliasName = alias.metadata.name
    val expression = parseExpressionAndBindParameters(path)
    return rightJoinFetch(expression, aliasName)
}

/**
 * Create a right join with the given target.
 *
 * @param path The path to join
 * @return The query builder for chaining calls
 */
fun <T, A, P> T.rightJoin(path : EntityPath<P>): A where T : FromBuilder<A>, T : ServiceProvider {
    return rightJoin(path, path)
}

/**
 * Create a right join with the given target.
 *
 * @param path The path to join
 * @param alias The alias for the joined element
 * @return The query builder for chaining calls
 */
fun <T, A, P> T.rightJoin(path : EntityPath<P>, alias : Path<P>): A where T : FromBuilder<A>, T: ServiceProvider {
    val entityType = path.type
    val aliasName = alias.metadata.name

    return if (! path.isRoot()) {
        val expression = parseExpressionAndBindParameters(path)
        rightJoin(expression, aliasName)
    } else {
        rightJoinOn(entityType, aliasName).onTrue()
    }
}

/**
 * Create a right join with the given target.
 *
 * @param path The path to join
 * @param alias The alias for the joined element
 * @return The restriction builder for the on-clause
 */
fun <T, A, P> T.rightJoinOn(path : CollectionExpression<*, P>, alias : Path<P>): JoinOnBuilder<A> where T : FromBuilder<A>, T : ServiceProvider {
    val aliasName = alias.metadata.name
    val expression = parseExpressionAndBindParameters(path)
    return rightJoinOn(expression, aliasName)
}

/**
 * Create a right join with the given target.
 *
 * @param path The path to join
 * @param alias The alias for the joined element
 * @return The restriction builder for the on-clause
 */
fun <T, A, P> T.rightJoinOn(path : MapExpression<*, P>, alias : Path<P>): JoinOnBuilder<A> where T : FromBuilder<A>, T : ServiceProvider {
    val aliasName = alias.metadata.name
    val expression = parseExpressionAndBindParameters(path)
    return rightJoinOn(expression, aliasName)
}


/**
 * Create a right join with the given target.
 *
 * @param path The path to join
 * @return The restriction builder for the on-clause
 */
fun <T, A, P> T.rightJoinOn(path : EntityPath<P>): JoinOnBuilder<A> where T : FromBuilder<A>, T : ServiceProvider {
    return rightJoinOn(path, path)
}

/**
 * Create a right join with the given target.
 *
 * @param path The path to join
 * @param alias The alias for the joined element
 * @return The restriction builder for the on-clause
 */
fun <T, A, P> T.rightJoinOn(path : EntityPath<P>, alias : Path<P>): JoinOnBuilder<A> where T : FromBuilder<A>, T : ServiceProvider {
    val entityType = path.type
    val aliasName = alias.metadata.name

    return if (! path.isRoot()) {
        val expression = parseExpressionAndBindParameters(path)
        rightJoinOn(expression, aliasName)
    } else {
        rightJoinOn(entityType, aliasName)
    }
}

/**
 * Create an inner join with the given target.
 *
 * @param path The path to join
 * @param alias The alias for the joined element
 * @return The restriction builder for the on-clause
 */
fun <T, A, P> T.innerJoinOn(path : CollectionExpression<*, P>, alias : Path<P>): JoinOnBuilder<A>  where T : FromBuilder<A>, T: ServiceProvider {
    val aliasName = alias.metadata.name
    val expression = parseExpressionAndBindParameters(path)
    return innerJoinOn(expression, aliasName)
}

/**
 * Create an inner join with the given target.
 *
 * @param path The path to join
 * @param alias The alias for the joined element
 * @return The restriction builder for the on-clause
 */
fun <T, A, P> T.innerJoinOn(path : MapExpression<*, P>, alias : Path<P>): JoinOnBuilder<A>  where T : FromBuilder<A>, T: ServiceProvider {
    val aliasName = alias.metadata.name
    val expression = parseExpressionAndBindParameters(path)
    return innerJoinOn(expression, aliasName)
}

/**
 * Create an inner join with the given target.
 *
 * @param path The path to join
 * @return The restriction builder for the on-clause
 */
fun <T, A, P> T.innerJoinOn(path : EntityPath<P>): JoinOnBuilder<A>  where T : FromBuilder<A>, T : ServiceProvider {
    return innerJoinOn(path, path)
}

/**
 * Create an inner join with the given target.
 *
 * @param path The path to join
 * @param alias The alias for the joined element
 * @return The restriction builder for the on-clause
 */
fun <T, A, P> T.innerJoinOn(path : EntityPath<P>, alias : Path<P>): JoinOnBuilder<A>  where T : FromBuilder<A>, T : ServiceProvider {
    val entityType = path.type
    val aliasName = alias.metadata.name

    return if (! path.isRoot()) {
        val expression = parseExpressionAndBindParameters(path)
        innerJoinOn(expression, aliasName)
    } else {
        innerJoinOn(entityType, aliasName)
    }
}

private fun EntityPath<*>.isRoot() : Boolean {
    return this.metadata.parent == null
}

/**
 * Create a left join with the given target.
 *
 * @param path The path to join
 * @param alias The alias for the joined element
 * @return The restriction builder for the on-clause
 */
fun <T, A, P> T.leftJoinOn(path : CollectionExpression<*, P>, alias : Path<P>): JoinOnBuilder<A> where T : FromBuilder<A>, T : ServiceProvider {
    val aliasName = alias.metadata.name
    val expression = parseExpressionAndBindParameters(path)
    return leftJoinOn(expression, aliasName)
}

/**
 * Create a left join with the given target.
 *
 * @param path The path to join
 * @param alias The alias for the joined element
 * @return The restriction builder for the on-clause
 */
fun <T, A, P> T.leftJoinOn(path : MapExpression<*, P>, alias : Path<P>): JoinOnBuilder<A> where T : FromBuilder<A>, T : ServiceProvider {
    val aliasName = alias.metadata.name
    val expression = parseExpressionAndBindParameters(path)
    return leftJoinOn(expression, aliasName)
}

/**
 * Create a left join with the given target.
 *
 * @param path The path to join
 * @return The restriction builder for the on-clause
 */
fun <T, A, P> T.leftJoinOn(path : EntityPath<P>): JoinOnBuilder<A> where T : FromBuilder<A>, T : ServiceProvider {
    return leftJoinOn(path, path)
}

/**
 * Create a left join with the given target.
 *
 * @param path The path to join
 * @param alias The alias for the joined element
 * @return The restriction builder for the on-clause
 */
fun <T, A, P> T.leftJoinOn(path : EntityPath<P>, alias : Path<P>): JoinOnBuilder<A> where T : FromBuilder<A>, T : ServiceProvider {
    val entityType = path.type
    val aliasName = alias.metadata.name

    return if (! path.isRoot()) {
        val expression = parseExpressionAndBindParameters(path)
        leftJoinOn(expression, aliasName)
    } else {
        leftJoinOn(entityType, aliasName)
    }
}

fun <T> T.orderBy(expression: Expression<*>, ascending : Boolean, nullFirst : Boolean) : T where T : OrderByBuilder<T>, T : ServiceProvider {
    var exp = parseExpressionAndBindParameters(expression)
    return orderBy(exp, ascending, nullFirst)
}

fun <T> T.orderByAsc(expression: Expression<*>, nullFirst : Boolean) : T where T : OrderByBuilder<T>, T : ServiceProvider {
    var exp = parseExpressionAndBindParameters(expression)
    return orderByAsc(exp, nullFirst)
}

fun <T> T.orderByAsc(vararg expressions: Expression<*>) : T where T : OrderByBuilder<T>, T : ServiceProvider {
    return expressions.fold(this) { cb, expression ->
        cb.orderByAsc(parseExpressionAndBindParameters(expression))}
}

fun <T> T.orderByDesc(expression: Expression<*>, nullFirst : Boolean) : T where T : OrderByBuilder<T>, T : ServiceProvider {
    var exp = parseExpressionAndBindParameters(expression)
    return orderByDesc(exp, nullFirst)
}

fun <T> T.orderByDesc(vararg expressions: Expression<*>) : T where T : OrderByBuilder<T>, T : ServiceProvider {
    return expressions.fold(this) { cb, expression ->
        cb.orderByDesc(parseExpressionAndBindParameters(expression))}
}

fun <T> T.orderByAsc(vararg specifiers: OrderSpecifier<*>) : T where T : OrderByBuilder<T>, T : ServiceProvider {
    return specifiers.fold(this) { cb, specifier ->
        cb.orderBy(specifier.target, specifier.isAscending, specifier.nullHandling == OrderSpecifier.NullHandling.NullsFirst)}
}

fun <T> T.groupBy(vararg expressions: Expression<*>) : T where T : GroupByBuilder<T>, T : ServiceProvider {
    return expressions.fold(this) { cb, expression ->
        cb.groupBy(parseExpressionAndBindParameters(expression))}
}


fun <T : CTEBuilder<T>> CTEBuilder<T>.with(cteClass : KClass<*>) : FullSelectCTECriteriaBuilder<T> {
    return with(cteClass.java)
}

fun <T : CTEBuilder<T>> CTEBuilder<T>.with(entityPath: EntityPath<*>) : FullSelectCTECriteriaBuilder<T> {
    return with(entityPath.type)
}

fun <T> JoinOnBuilder<T>.on(predicate: Predicate) : T {
    var jpqlQueryFragment = parseExpressionAndBindParameters(predicate)
    return this.setOnExpression(jpqlQueryFragment)
}

fun <T> JoinOnBuilder<T>.on(predicate: Expression<*>) : RestrictionBuilder<JoinOnBuilder<T>> {
    var jpqlQueryFragment = parseExpressionAndBindParameters(predicate)
    return this.on(jpqlQueryFragment)
}

fun <T> FullSelectCTECriteriaBuilder<T>.bind(path : Path<*>) : SelectBuilder<FullSelectCTECriteriaBuilder<T>> {
    var jpqlQueryFragment = parseExpressionAndBindParameters(path)
    val alias = generateSequence(path) { elem -> elem.metadata.parent }.last().metadata.name
    val unqualifiedExpression = jpqlQueryFragment.substring(alias.length + 1)
    return bind(unqualifiedExpression)
}

/**
 * Adds a select clause with the given expression to the query.
 *
 * @param expression The expression for the select clause
 * @return The query builder for chaining calls
 */
fun <T, A> T.select(expression: Expression<*>): A where T : SelectBuilder<A>, T : ServiceProvider {
    var jpqlQueryFragment = parseExpressionAndBindParameters(expression)
    return select(jpqlQueryFragment)
}

/**
 * Adds a select clause with the given expression and alias to the query.
 *
 * @param expression The expression for the select clause
 * @param alias The alias for the expression
 * @return The query builder for chaining calls
 */
fun <T, A> T.select(expression: Expression<*>, alias: String): A where T : SelectBuilder<A>, T : ServiceProvider {
    var jpqlQueryFragment = parseExpressionAndBindParameters(expression)
    return select(jpqlQueryFragment, alias)
}

/**
 * Sets the given expression as expression for the where clause.
 *
 * @param predicate The where predicate
 * @return The builder
 */
fun <T, A> T.where(predicate : Predicate) : A where T : WhereBuilder<A>, T : ServiceProvider {
    var jpqlQueryFragment = parseExpressionAndBindParameters(predicate)
    return setWhereExpression(jpqlQueryFragment)
}

/**
 * Sets the given expression as expression for the having clause.
 *
 * @param predicate The having predicate
 * @return The builder
 */
fun <T, A> T.having(predicate : Predicate) : A where T : HavingBuilder<A>, T : ServiceProvider {
    var jpqlQueryFragment = parseExpressionAndBindParameters(predicate)
    return setHavingExpression(jpqlQueryFragment)
}

/**
 * Set the sources of this query
 *
 * @param entityPath The entity which should be queried
 * @return The query builder for chaining calls
 */
fun <T : FromBaseBuilder<T>> T.from(entityPath : EntityPath<*>) : T {
    val alias = entityPath.metadata.name
    return this.from(entityPath.type, alias)
}

/**
 * Set the sources of this query, but explicitly queries the data before any side effects happen because of CTEs.
 *
 * @param entityPath The entity which should be queried
 * @return The query builder for chaining calls
 */
fun <T : FromBaseBuilder<T>> T.fromOld(entityPath : EntityPath<*>) : T {
    val alias = entityPath.metadata.name
    return this.fromOld(entityPath.type, alias)
}

/**
 * Set the sources of this query, but explicitly queries the data after any side effects happen because of CTEs.
 *
 * @param entityPath The entity which should be queried
 * @return The query builder for chaining calls
 */
fun <T : FromBaseBuilder<T>> T.fromNew(entityPath : EntityPath<*>) : T {
    val alias = entityPath.metadata.name
    return this.fromNew(entityPath.type, alias)
}

/**
 * Add a VALUES clause for values of the given value class to the from clause.
 * This introduces a parameter named like the given alias.
 *
 * @param entityPath The entity which should be queried
 * @param valueCount The number of values to use for the values clause
 * @return The query builder for chaining calls
 */
fun <T : FromBaseBuilder<T>> T.fromValues(entityPath : EntityPath<*>, valueCount : Int) : T {
    val alias = entityPath.metadata.name
    return this.fromValues(entityPath.type, alias, valueCount)
}

/**
 * Add a VALUES clause for values of the type as determined by the given entity attribute to the from clause.
 * This introduces a parameter named like the given alias.
 *
 * @param entityPath The entity which should be queried
 * @param attributeName The attribute name within the entity class which to use for determining the values type
 * @param valueCount The number of values to use for the values clause
 * @return The query builder for chaining calls
 */
fun <T : FromBaseBuilder<T>> T.fromValues(entityPath : EntityPath<*>, attributeName : String, valueCount : Int) : T {
    val alias = entityPath.metadata.name
    return this.fromValues(entityPath.type, alias, attributeName, valueCount)
}

/**
 * Add a VALUES clause for values of the given value class to the from clause.
 * This introduces a parameter named like the given alias.
 *
 * @param entityPath The entity which should be queried
 * @param valueCount The number of values to use for the values clause
 * @return The query builder for chaining calls
 */
fun <T : FromBaseBuilder<T>> T.fromIdentifiableValues(entityPath : EntityPath<*>, valueCount : Int) : T {
    val alias = entityPath.metadata.name
    return this.fromIdentifiableValues(entityPath.type, alias, valueCount)
}


/**
 * Add a VALUES clause for values of the type as determined by the given entity attribute to the from clause.
 *
 * @param entityPath The entity which should be queried
 * @param values The values to use for the values clause
 * @return The query builder for chaining calls
 */
fun <T : FromBaseBuilder<T>, A> T.fromValues(entityPath : Path<A>, values : Collection<A>) : T {
    val alias = entityPath.metadata.name
    return this.fromValues(entityPath.type as Class<A>, alias, values)
}

/**
 * Add a VALUES clause for values of the type as determined by the given entity attribute to the from clause.
 *
 * @param entityPath The entity which should be queried
 * @param attributeName The attribute name within the entity class which to use for determining the values type
 * @param values The values to use for the values clause
 * @return The query builder for chaining calls
 */
fun <T : FromBaseBuilder<T>, A> T.fromValues(entityPath : Path<A>, attributeName: String, values : Collection<A>) : T {
    val alias = entityPath.metadata.name
    return this.fromValues(entityPath.type, alias, attributeName, values)
}


/**
 * Add a VALUES clause for values of the type as determined by the given entity attribute to the from clause.
 *
 * @param entityPath The entity which should be queried
 * @param values The values to use for the values clause
 * @return The query builder for chaining calls
 */
fun <T : FromBaseBuilder<T>, A> T.fromIdentifiableValues(entityPath : EntityPath<A>, values : Collection<A>) : T {
    val alias = entityPath.metadata.name
    return this.fromIdentifiableValues(entityPath.type as Class<A>, alias, values)
}

fun <X> NumberExpression<X>.greatest(expression: NumberExpression<X>) : NumberExpression<X> where X: Comparable<*>, X : Number {
    return NumberOperation.create<X>(this.type, Operators.GREATEST, this, expression)
}


fun <X> NumberExpression<X>.greatest(value: X) : NumberExpression<X> where X: Comparable<*>, X : Number {
    return NumberOperation.create<X>(this.type, Operators.GREATEST, this, ConstantImpl.create<X>(value))
}

fun <X> NumberExpression<X>.least(expression: NumberExpression<X>) : NumberExpression<X> where X: Comparable<*>, X : Number {
    return NumberOperation.create<X>(this.type, Operators.LEAST, this, expression)
}


fun <X> NumberExpression<X>.least(value: X) : NumberExpression<X> where X: Comparable<*>, X : Number {
    return NumberOperation.create<X>(this.type, Operators.LEAST, this, ConstantImpl.create<X>(value))
}


fun <X> NumberExpression<X>.round(decimals: Int) : NumberExpression<X> where X: Comparable<*>, X : Number {
    return NumberOperation.create<X>(this.type, Ops.MathOps.ROUND2, this, ConstantImpl.create(decimals))
}

fun <A> A.where(path: Path<*>) : RestrictionBuilder<A> where A : BaseWhereBuilder<A>, A : ServiceProvider {
    val jpqlQueryFragment = parseExpressionAndBindParameters(path)
    return this.where(jpqlQueryFragment)
}


fun <A> A.having(path: Path<*>) : RestrictionBuilder<A> where A : BaseHavingBuilder<A>, A : ServiceProvider {
    val jpqlQueryFragment = parseExpressionAndBindParameters(path)
    return this.having(jpqlQueryFragment)
}

/**
 * Adds an implicit join fetch to the query.
 *
 * @param paths The paths to join fetch
 * @return The query builder for chaining calls
 */
fun <A> A.fetch(vararg paths: Path<*>) : A where A : FetchBuilder<A>, A : ServiceProvider {
    return paths.fold(this) { cb, expression ->
        cb.fetch(parseExpressionAndBindParameters(expression))}
}

/**
 * Finishes the EQ predicate and adds it to the parent predicate container represented by the type T.
 * The predicate checks if the left hand side is equal to the given expression.
 *
 * @param expression The expression on the right hand side
 * @return The parent predicate container builder
 */
fun <A : RestrictionBuilder<T>, T> A.eqExpression(expression: Expression<*>) : T {
    val jpqlQueryFragment = parseExpressionAndBindParameters(expression)
    return this.eqExpression(jpqlQueryFragment)
}

/**
 * Finishes the NEQ predicate and adds it to the parent predicate container represented by the type T.
 * The predicate checks if the left hand side is equal to the given expression.
 *
 * @param expression The expression on the right hand side
 * @return The parent predicate container builder
 */
fun <A : RestrictionBuilder<T>, T> A.notEqExpression(expression: Expression<*>) : T {
    val jpqlQueryFragment = parseExpressionAndBindParameters(expression)
    return this.notEqExpression(jpqlQueryFragment)
}

/**
 * Starts a builder for a between predicate with lower bound expression.
 *
 * @param expression The between start expression
 * @return The BetweenBuilder
 */
fun <A : RestrictionBuilder<T>, T> A.betweenExpression(expression: Expression<*>) : BetweenBuilder<T> {
    val jpqlQueryFragment = parseExpressionAndBindParameters(expression)
    return this.betweenExpression(jpqlQueryFragment)
}

/**
 * Starts a builder for a not between predicate with lower bound expression.
 *
 * @param expression The between start expression
 * @return The BetweenBuilder
 */
fun <A : RestrictionBuilder<T>, T> A.notBetweenExpression(expression: Expression<*>) : BetweenBuilder<T> {
    val jpqlQueryFragment = parseExpressionAndBindParameters(expression)
    return this.notBetweenExpression(jpqlQueryFragment)
}

/**
 * Constructs a between predicate with an expression as upper bound.
 *
 * @param expression The upper bound expression
 * @return The parent predicate container builder
 */
fun <A> BetweenBuilder<A>.andExpression(expression: Expression<*>) : A {
    val jpqlQueryFragment = parseExpressionAndBindParameters(expression)
    return this.andExpression(jpqlQueryFragment)
}

/**
 * Constructs a between predicate with an expression as upper bound.
 *
 * @param expression The upper bound expression
 * @return The parent predicate container builder
 */
fun <A> BinaryPredicateBuilder<A>.andExpression(expression: Expression<*>) : A {
    val jpqlQueryFragment = parseExpressionAndBindParameters(expression)
    return this.expression(jpqlQueryFragment)
}


private fun ServiceProvider.parseExpressionAndBindParameters(expression : Expression<*>) : String {
    val em = getService(EntityManager::class.java)
    val parameterManager = getService(ConstantRegistry::class.java)
    val ser = JPQLSerializer(BlazePersistJPQLTemplates.INSTANCE, em)
    expression.accept(ser, null)

    return ser.constantToLabel.entries.fold(ser.toString()) {jpqlQueryFragment, (constant, label) ->
        val parameterName = parameterManager.addConstant(constant)
        jpqlQueryFragment.replace("?$label", ":$parameterName")
    }
}
