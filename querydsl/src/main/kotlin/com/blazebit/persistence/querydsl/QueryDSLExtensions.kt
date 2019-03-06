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
import com.mysema.query.types.path.CollectionPath
import javax.persistence.EntityManager
import javax.persistence.Tuple
import javax.persistence.TypedQuery
import kotlin.reflect.KClass

//<editor-fold desc="Criteria Builder Functions">

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

fun <T : Any> CriteriaBuilderFactory.delete(entityManager : EntityManager, entityPath : EntityPath<T>) : DeleteCriteriaBuilder<T> {
    val alias = entityPath.metadata.name
    return this.delete(entityManager, entityPath.type as Class<T>, alias)
}

fun <T : Any> CriteriaBuilderFactory.update(entityManager : EntityManager, entityPath : EntityPath<T>) : UpdateCriteriaBuilder<T> {
    val alias = entityPath.metadata.name
    return this.update(entityManager, entityPath.type as Class<T>, alias)
}

//</editor-fold>

//<editor-fold desc="Joins">

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

fun <T> JoinOnBuilder<T>.on(predicate: Predicate) : T {
    var jpqlQueryFragment = parseExpressionAndBindParameters(predicate)
    return this.setOnExpression(jpqlQueryFragment)
}

fun <T : BaseJoinOnBuilder<T>> BaseJoinOnBuilder<T>.on(predicate: Expression<*>) : RestrictionBuilder<T> {
    var jpqlQueryFragment = parseExpressionAndBindParameters(predicate)
    return this.on(jpqlQueryFragment)
}

//</editor-fold>

//<editor-fold desc="Order By">

fun <T> T.orderBy(expression: Expression<*>, ascending : Boolean, nullFirst : Boolean) : T where T : OrderByBuilder<T>, T : ServiceProvider {
    val exp = parseExpressionAndBindParameters(expression)
    return orderBy(exp, ascending, nullFirst)
}

fun <T> T.orderBy(vararg specifiers: OrderSpecifier<*>) : T where T : OrderByBuilder<T>, T : ServiceProvider {
    return specifiers.fold(this) { cb, specifier ->
        cb.orderBy(specifier.target, specifier.isAscending, specifier.nullHandling == OrderSpecifier.NullHandling.NullsFirst)}
}

//</editor-fold>

//<editor-fold desc="Group By">

fun <T> T.groupBy(vararg expressions: Expression<*>) : T where T : GroupByBuilder<T>, T : ServiceProvider {
    return expressions.fold(this) { cb, expression ->
        cb.groupBy(parseExpressionAndBindParameters(expression))}
}

//</editor-fold>

//<editor-fold desc="CTE">

/**
 * Creates a builder for a CTE with the given CTE type.
 *
 * @param cteClass The type of the CTE
 * @return The CTE builder
 */
fun <T : CTEBuilder<T>> CTEBuilder<T>.with(cteClass : KClass<*>) : FullSelectCTECriteriaBuilder<T> {
    return with(cteClass.java)
}

/**
 * Creates a builder for a CTE with a nested set operation builder.
 * Doing this is like starting a nested query that will be connected via a set operation.
 *
 * @param entityPath The type of the CTE
 * @return The CTE set operation builder
 */
fun <T : CTEBuilder<T>> CTEBuilder<T>.withStartSet(entityPath: EntityPath<*>) : StartOngoingSetOperationCTECriteriaBuilder<T, LeafOngoingFinalSetOperationCTECriteriaBuilder<T>> {
    return withStartSet(entityPath.type)
}

/**
 * Creates a builder for a recursive CTE with the given CTE type.
 *
 * @param entityPath The type of the CTE
 * @return The recursive CTE builder
 */
fun <T : CTEBuilder<T>> CTEBuilder<T>.withRecursive(entityPath: EntityPath<*>) : SelectRecursiveCTECriteriaBuilder<T> {
    return withRecursive(entityPath.type)
}

/**
 * Creates a builder for a CTE with the given CTE type.
 *
 * @param entityPath The type of the CTE
 * @return The CTE builder
 */
fun <T : CTEBuilder<T>> CTEBuilder<T>.with(entityPath: EntityPath<*>) : FullSelectCTECriteriaBuilder<T> {
    return with(entityPath.type)
}

/**
 * Creates a builder for a modification CTE with the given CTE type.
 *
 * @param entityPath The type of the CTE
 * @return A factory to create a modification query that returns/binds attributes to the CTE.
 */
fun <T : CTEBuilder<T>> CTEBuilder<T>.withReturning(entityPath: EntityPath<*>) : ReturningModificationCriteriaBuilderFactory<T> {
    return withReturning(entityPath.type)
}

fun <T : SelectBaseCTECriteriaBuilder<X>, X> T.bind(path : Path<*>) : SelectBuilder<X> {
    return bind(path.getPathString(1))
}

//</editor-fold>

//<editor-fold desc="Manipulation queries">

fun <T : BaseInsertCriteriaBuilder<*, T>?> BaseInsertCriteriaBuilder<*, T>.bind(path : Path<*>) : SelectBuilder<T> {
    return bind(path.getPathString(1))
}

fun <T, X> T.executeWithReturning(vararg attributes : Path<*>) : ReturningResult<Tuple>
        where T : ModificationCriteriaBuilder<X>, T : ServiceProvider {
    val toTypedArray = attributes.map { expr -> expr.getPathString(1) }.toTypedArray()
    return this.executeWithReturning(*toTypedArray)
}


fun <T, X, V> T.executeWithReturning(attribute : Path<V>) : ReturningResult<V>
        where T : ModificationCriteriaBuilder<X>, T : ServiceProvider {
    return this.executeWithReturning(attribute.getPathString(1), attribute.type as Class<V>)
}

fun <T, X, B : BaseUpdateCriteriaBuilder<T, X>, V> B.set(path : Path<V>, value : V) : X {
    return set(path.getPathString(1), value)
}

fun <T, X, B : BaseUpdateCriteriaBuilder<T, X>, V> B.setExpression(path : Path<V>, expression : Expression<V>) : X {
    return setExpression(path.getPathString(1), parseExpressionAndBindParameters(expression))
}


//</editor-fold>

//<editor-fold desc="Insert">

fun <T, X> T.getWithReturningQuery(vararg attributes : Path<*>) : TypedQuery<ReturningResult<Tuple>>
        where T : ModificationCriteriaBuilder<X>, T : ServiceProvider {
    val toTypedArray = attributes.map { expr -> parseExpressionAndBindParameters(expr) }.toTypedArray()
    return this.getWithReturningQuery(*toTypedArray)
}


fun <T, X, V> T.getWithReturningQuery(attribute : Path<V>) : TypedQuery<ReturningResult<V>>
        where T : ModificationCriteriaBuilder<X>, T : ServiceProvider {
    return this.getWithReturningQuery(parseExpressionAndBindParameters(attribute), attribute.type as Class<V>)
}


fun <T, X> T.returning(cteAttribute : Path<*>, modificationAttribute : Path<*>) : X
        where T : ReturningBuilder<X>, T : ServiceProvider {
    val cteAttributeExpr = cteAttribute.getPathString(1)
    val modificationAttributeExpr = modificationAttribute.getPathString(1)
    return this.returning(cteAttributeExpr, modificationAttributeExpr)
}

fun <X, Y, T> Y.delete(path : Path<T>) : ReturningDeleteCriteriaBuilder<T, X>
        where Y : ReturningModificationCriteriaBuilderFactory<X>, Y : ServiceProvider {
    val alias = path.metadata.name
    val type = path.type as Class<T>
    return delete(type, alias)
}


fun <X, Y, T> Y.deleteCollection(path : CollectionPath<*, T>) : ReturningDeleteCriteriaBuilder<T, X>
        where Y : ReturningModificationCriteriaBuilderFactory<X>, Y : ServiceProvider {
    val collectionName = path.getMetadata().name
    val entityAlias = path.getMetadata().parent!!.metadata.name
    val type = path.getMetadata().parent!!.type as Class<T>
    return deleteCollection(type, entityAlias, collectionName)
}

fun <X, Y, T> Y.update(path : Path<T>) : ReturningUpdateCriteriaBuilder<T, X>
        where Y : ReturningModificationCriteriaBuilderFactory<X>, Y : ServiceProvider {
    val alias = path.metadata.name
    val type = path.type as Class<T>
    return update(type, alias)
}


fun <X, Y, T> Y.updateCollection(path : CollectionPath<*, T>) : ReturningUpdateCriteriaBuilder<T, X>
        where Y : ReturningModificationCriteriaBuilderFactory<X>, Y : ServiceProvider {
    val collectionName = path.getMetadata().name
    val entityAlias = path.getMetadata().parent!!.metadata.name
    val type = path.getMetadata().parent!!.type as Class<T>
    return updateCollection(type, entityAlias, collectionName)
}


fun <X, Y, T> Y.insert(path : Path<T>) : ReturningInsertCriteriaBuilder<T, X>
        where Y : ReturningModificationCriteriaBuilderFactory<X>, Y : ServiceProvider {
    val type = path.type as Class<T>
    return insert(type)
}


fun <X, Y, T> Y.insertCollection(path : CollectionPath<*, T>) : ReturningInsertCriteriaBuilder<T, X>
        where Y : ReturningModificationCriteriaBuilderFactory<X>, Y : ServiceProvider {
    val collectionName = path.getMetadata().name
    val type = path.getMetadata().parent!!.type as Class<T>
    return insertCollection(type, collectionName)
}


//</editor-fold>

//<editor-fold desc="Case Restrictions">


fun <T, B> B.and(expression : Expression<*>) : RestrictionBuilder<CaseWhenAndBuilder<T>> where B : CaseWhenAndBuilder<T>, B : ServiceProvider {
    val jpqlFragment = parseExpressionAndBindParameters(expression)
    return and(jpqlFragment)
}

fun <T : CaseWhenBuilder<*>?, B> B.and(expression : Expression<*>) : RestrictionBuilder<CaseWhenAndThenBuilder<T>> where B : CaseWhenAndThenBuilder<T>, B : ServiceProvider {
    val jpqlFragment = parseExpressionAndBindParameters(expression)
    return and(jpqlFragment)
}

fun <T, B> B.otherwiseExpression(expression : Expression<*>) : T where B : CaseWhenBuilder<T>, B : ServiceProvider {
    val jpqlFragment = parseExpressionAndBindParameters(expression)
    return otherwiseExpression(jpqlFragment)
}


fun <T, B> B.or(expression : Expression<*>) : RestrictionBuilder<CaseWhenOrBuilder<T>> where B : CaseWhenOrBuilder<T>, B : ServiceProvider {
    val jpqlFragment = parseExpressionAndBindParameters(expression)
    return or(jpqlFragment)
}

fun <T : CaseWhenBuilder<*>?, B> B.or(expression : Expression<*>) : RestrictionBuilder<CaseWhenOrThenBuilder<T>> where B : CaseWhenOrThenBuilder<T>, B : ServiceProvider {
    val jpqlFragment = parseExpressionAndBindParameters(expression)
    return or(jpqlFragment)
}

fun <T, B> B.thenExpression(expression : Expression<*>) : T where B : CaseWhenThenBuilder<T>, B : ServiceProvider {
    val jpqlFragment = parseExpressionAndBindParameters(expression)
    return thenExpression(jpqlFragment)
}

fun <T, B> B.`when`(expression : Expression<*>) : RestrictionBuilder<CaseWhenThenBuilder<CaseWhenBuilder<T>>>
        where B : CaseWhenStarterBuilder<T>, B : ServiceProvider {
    val jpqlFragment = parseExpressionAndBindParameters(expression)
    return `when`(jpqlFragment)
}

fun <A, V> A.whereSimpleCase(path: Path<V>) : ExtendedSimpleCaseWhenStarterBuilder<RestrictionBuilder<A>, V> where A : BaseWhereBuilder<A>, A : ServiceProvider {
    val jpqlQueryFragment = parseExpressionAndBindParameters(path)
    return ExtendedSimpleCaseWhenStarterBuilder<RestrictionBuilder<A>, V>(
            this.whereSimpleCase(jpqlQueryFragment), this)
}


/**
 * Adds a select clause with the given expression to the query.
 *
 * @param expression The expression for the select clause
 * @return The query builder for chaining calls
 */
fun <T, A, V> T.selectSimpleCase(expression: Expression<V>): ExtendedSimpleCaseWhenStarterBuilder<A, V> where T : SelectBuilder<A>, T : ServiceProvider {
    var jpqlQueryFragment = parseExpressionAndBindParameters(expression)
    return ExtendedSimpleCaseWhenStarterBuilder(selectSimpleCase(jpqlQueryFragment), this)
}

class ExtendedSimpleCaseWhenStarterBuilder<T, V>(private val delegate: SimpleCaseWhenStarterBuilder<T>, private val serviceProvider: ServiceProvider) :
        SimpleCaseWhenStarterBuilder<T> by delegate,
        ServiceProvider by serviceProvider {


    fun `when`(expression : Expression<V>, thenExpression : Expression<V>) : ExtendedSimpleCaseWhenBuilder<T, V> {
        val jpqlFragment = parseExpressionAndBindParameters(expression)
        val jpqlFragmentThen = parseExpressionAndBindParameters(thenExpression)
        return ExtendedSimpleCaseWhenBuilder(`when`(jpqlFragment, jpqlFragmentThen), this)
    }

    fun `when`(expression : Expression<V>, value : V) : ExtendedSimpleCaseWhenBuilder<T, V> {
        return `when`(expression, ConstantImpl.create(value))
    }

}

class ExtendedSimpleCaseWhenBuilder<T, V>(private val delegate: SimpleCaseWhenBuilder<T>, private val serviceProvider: ServiceProvider) :
        SimpleCaseWhenBuilder<T> by delegate,
        ServiceProvider by serviceProvider {


    fun `when`(expression : Expression<V>, thenExpression : Expression<V>) : ExtendedSimpleCaseWhenBuilder<T, V> {
        val jpqlFragment = parseExpressionAndBindParameters(expression)
        val jpqlFragmentThen = parseExpressionAndBindParameters(thenExpression)
        return ExtendedSimpleCaseWhenBuilder(`when`(jpqlFragment, jpqlFragmentThen), this)
    }

    fun `when`(expression : Expression<V>, value : V) : ExtendedSimpleCaseWhenBuilder<T, V> {
        return `when`(expression, ConstantImpl.create(value))
    }

    fun `otherwise`(thenExpression : Expression<V>) : T {
        val jpqlFragmentThen = parseExpressionAndBindParameters(thenExpression)
        return otherwise(jpqlFragmentThen)
    }

    fun `otherwiseValue`(value : V) : T {
        return otherwise(ConstantImpl.create(value))
    }

}


//</editor-fold>

//<editor-fold desc="Key set pagination">


/**
 * Uses the given value as reference value for keyset pagination for the given expression.
 * Normally the expression is one of the order by expressions used in the query.
 *
 * @param expression The order by expression for which a value should be provided
 * @param value The reference value from which the keyset pagination can start from
 * @return This keyset builder
 */
fun <T, X, V> T.with(expression: Expression<V>, value : V) : KeysetBuilder<X> where T : ServiceProvider, T : KeysetBuilder<X> {
    var jpqlQueryFragment = parseExpressionAndBindParameters(expression)
    return with(jpqlQueryFragment, value)
}


//</editor-fold>

//<editor-fold desc="Selects">

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

//</editor-fold>

//<editor-fold desc="Where">


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


fun <A> A.where(path: Path<*>) : RestrictionBuilder<A> where A : BaseWhereBuilder<A>, A : ServiceProvider {
    val jpqlQueryFragment = parseExpressionAndBindParameters(path)
    return this.where(jpqlQueryFragment)
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
fun <A> BinaryPredicateBuilder<A>.expression(expression: Expression<*>) : A {
    val jpqlQueryFragment = parseExpressionAndBindParameters(expression)
    return this.expression(jpqlQueryFragment)
}


//</editor-fold>

//<editor-fold desc="Having">

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

fun <A> A.having(path: Path<*>) : RestrictionBuilder<A> where A : BaseHavingBuilder<A>, A : ServiceProvider {
    val jpqlQueryFragment = parseExpressionAndBindParameters(path)
    return this.having(jpqlQueryFragment)
}

//</editor-fold>

//<editor-fold desc="From Clause">

/**
 * Set the sources of this query
 *
 * @param entityPath The entity which should be queried
 * @return The query builder for chaining calls
 */
fun <T : FromBaseBuilder<T>> FromBaseBuilder<T>.from(entityPath : EntityPath<*>) : T {
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


//</editor-fold>

//<editor-fold desc="Number Expression Helpers">


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

//</editor-fold>

//<editor-fold desc="Fetch">

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

//</editor-fold>

//<editor-fold desc="Helper functions">

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

private fun Path<*>.getPathString(drop : Int = 0) : String {
    return generateSequence(this.metadata) { pathMetadata -> pathMetadata.parent?.metadata }
            .map { pathMetadata -> pathMetadata.name }
            .toMutableList()
            .dropLast(drop)
            .reversed()
            .joinToString(".")
}

private fun <T> JoinOnBuilder<T>.onTrue() : T {
    return on("true").eqExpression("true").end()
}

private fun EntityPath<*>.isRoot() : Boolean {
    return this.metadata.parent == null
}

//</editor-fold>
