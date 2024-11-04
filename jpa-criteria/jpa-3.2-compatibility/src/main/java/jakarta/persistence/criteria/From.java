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
//     Linda DeMichiel - 2.1
//     Linda DeMichiel - 2.0


package jakarta.persistence.criteria;

import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.CollectionAttribute;
import jakarta.persistence.metamodel.ListAttribute;
import jakarta.persistence.metamodel.MapAttribute;
import jakarta.persistence.metamodel.SetAttribute;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a bound type, usually an entity that appears in
 * the from clause, but may also be an embeddable belonging to
 * an entity in the from clause. 
 * <p> Serves as a factory for {@link Join}s of associations,
 * embeddables, and collections belonging to the type, and for
 * {@link Path}s of attributes belonging to the type.
 *
 * @param <Z>  the source type
 * @param <X>  the target type
 *
 * @since 2.0
 */
@SuppressWarnings("hiding")
public interface From<Z, X> extends Path<X>, FetchParent<Z, X> {

    /**
     * Return the joins that have been made from this bound type.
     * Returns empty set if no joins have been made from this
     * bound type.
     * Modifications to the set do not affect the query.
     * @return joins made from this type
     */
    Set<Join<X, ?>> getJoins();
	
    /**
     * Whether the {@link From} object has been obtained as a result
     * of correlation (use of a {@link Subquery#correlate} method).
     * @return boolean indicating whether the object has been
     *         obtained through correlation
     */
    boolean isCorrelated();

    /**
     * Returns the parent {@link From} object from which the correlated
     * {@link From} object has been obtained through correlation (use
     * of {@link Subquery#correlate} method).
     * @return  the parent of the correlated {@code From} object
     * @throws IllegalStateException if the {@code From} object has
     *         not been obtained through correlation
     */
    From<Z, X> getCorrelationParent();

    /**
     * Create and add an inner join to the given entity.
     * @param entityClass  the target entity class
     * @return the resulting join
     * @since 3.2
     */
    <Y> Join<X, Y> join(Class<Y> entityClass);

    /**
     * Create and add a join to the given entity.
     * @param entityClass  the target entity class
     * @param joinType  join type
     * @return the resulting join
     * @since 3.2
     */
    <Y> Join<X, Y> join(Class<Y> entityClass, JoinType joinType);

    /**
     * Create and add an inner join to the given entity.
     * @param entity  metamodel entity representing the join target
     * @return the resulting join
     * @since 3.2
     */
    <Y> Join<X, Y> join(EntityType<Y> entity);

    /**
     * Create and add a join to the given entity.
     * @param entity  metamodel entity representing the join target
     * @param joinType  join type
     * @return the resulting join
     * @since 3.2
     */
    <Y> Join<X, Y> join(EntityType<Y> entity, JoinType joinType);

    /**
     * Create an inner join to the specified single-valued 
     * attribute.
     * @param attribute  target of the join
     * @return the resulting join
     */
    <Y> Join<X, Y> join(SingularAttribute<? super X, Y> attribute);

    /**
     * Create a join to the specified single-valued attribute 
     * using the given join type.
     * @param attribute  target of the join
     * @param jt  join type 
     * @return the resulting join
     */
    <Y> Join<X, Y> join(SingularAttribute<? super X, Y> attribute, JoinType jt);

    /**
     * Create an inner join to the specified {@link Collection}-valued
     * attribute.
     * @param collection  target of the join
     * @return the resulting join
     */
    <Y> CollectionJoin<X, Y> join(CollectionAttribute<? super X, Y> collection);

    /**
     * Create an inner join to the specified {@link Set}-valued
     * attribute.
     * @param set  target of the join
     * @return the resulting join
     */
    <Y> SetJoin<X, Y> join(SetAttribute<? super X, Y> set);

    /**
     * Create an inner join to the specified
     * {@link List}-valued attribute.
     * @param list  target of the join
     * @return the resulting join
     */
    <Y> ListJoin<X, Y> join(ListAttribute<? super X, Y> list);

    /**
     * Create an inner join to the specified {@link Map}-valued
     * attribute.
     * @param map  target of the join
     * @return the resulting join
     */
    <K, V> MapJoin<X, K, V> join(MapAttribute<? super X, K, V> map);

    /**
     * Create a join to the specified {@link Collection}-valued
     * attribute using the given join type.
     * @param collection  target of the join
     * @param jt  join type 
     * @return the resulting join
     */
    <Y> CollectionJoin<X, Y> join(CollectionAttribute<? super X, Y> collection, JoinType jt);

    /**
     * Create a join to the specified {@link Set}-valued attribute
     * using the given join type.
     * @param set  target of the join
     * @param jt  join type 
     * @return the resulting join
     */
    <Y> SetJoin<X, Y> join(SetAttribute<? super X, Y> set, JoinType jt);

    /**
     * Create a join to the specified {@link List}-valued attribute
     * using the given join type.
     * @param list  target of the join
     * @param jt  join type 
     * @return the resulting join
     */
    <Y> ListJoin<X, Y> join(ListAttribute<? super X, Y> list, JoinType jt);

    /**
     * Create a join to the specified {@link Map}-valued attribute
     * using the given join type.
     * @param map  target of the join
     * @param jt  join type 
     * @return the resulting join
     */
    <K, V> MapJoin<X, K, V> join(MapAttribute<? super X, K, V> map, JoinType jt);


    //String-based:

    /**
     * Create an inner join to the specified attribute.
     * @param attributeName  name of the attribute for the
     *                       target of the join
     * @return the resulting join
     * @throws IllegalArgumentException if attribute of the given
     *         name does not exist
     */
    <X, Y> Join<X, Y> join(String attributeName);	

    /**
     * Create an inner join to the specified {@link Collection}-valued
     * attribute.
     * @param attributeName  name of the attribute for the
     *                       target of the join
     * @return the resulting join
     * @throws IllegalArgumentException if attribute of the given
     *         name does not exist
     */
    <X, Y> CollectionJoin<X, Y> joinCollection(String attributeName);	

    /**
     * Create an inner join to the specified {@link Set}-valued
     * attribute.
     * @param attributeName  name of the attribute for the
     *                       target of the join
     * @return the resulting join
     * @throws IllegalArgumentException if attribute of the given
     *         name does not exist
     */
    <X, Y> SetJoin<X, Y> joinSet(String attributeName);	

    /**
     * Create an inner join to the specified {@link List}-valued
     * attribute.
     * @param attributeName  name of the attribute for the
     *                       target of the join
     * @return the resulting join
     * @throws IllegalArgumentException if attribute of the given
     *         name does not exist
     */
    <X, Y> ListJoin<X, Y> joinList(String attributeName);		
    
    /**
     * Create an inner join to the specified {@link Map}-valued
     * attribute.
     * @param attributeName  name of the attribute for the
     *                       target of the join
     * @return the resulting join
     * @throws IllegalArgumentException if attribute of the given
     *         name does not exist
     */
    <X, K, V> MapJoin<X, K, V> joinMap(String attributeName);	

    /**
     * Create a join to the specified attribute using the given
     * join type.
     * @param attributeName  name of the attribute for the
     *                       target of the join
     * @param jt  join type
     * @return the resulting join
     * @throws IllegalArgumentException if attribute of the given
     *         name does not exist
     */
    <X, Y> Join<X, Y> join(String attributeName, JoinType jt);	
    
    /**
     * Create a join to the specified {@link Collection}-valued
     * attribute using the given join type.
     * @param attributeName  name of the attribute for the 
     *                       target of the join
     * @param jt  join type
     * @return the resulting join
     * @throws IllegalArgumentException if attribute of the given
     *         name does not exist
     */
    <X, Y> CollectionJoin<X, Y> joinCollection(String attributeName, JoinType jt);	

    /**
     * Create a join to the specified {@link Set}-valued attribute
     * using the given join type.
     * @param attributeName  name of the attribute for the
     *                       target of the join
     * @param jt  join type
     * @return the resulting join
     * @throws IllegalArgumentException if attribute of the given
     *         name does not exist
     */
    <X, Y> SetJoin<X, Y> joinSet(String attributeName, JoinType jt);	

    /**
     * Create a join to the specified {@link List}-valued attribute
     * using the given join type.
     * @param attributeName  name of the attribute for the
     *                       target of the join
     * @param jt  join type
     * @return the resulting join
     * @throws IllegalArgumentException if attribute of the given
     *         name does not exist
     */
    <X, Y> ListJoin<X, Y> joinList(String attributeName, JoinType jt);	

    /**
     * Create a join to the specified {@link Map}-valued attribute
     * using the given join type.
     * @param attributeName  name of the attribute for the
     *                       target of the join
     * @param jt  join type
     * @return the resulting join
     * @throws IllegalArgumentException if attribute of the given
     *        name does not exist
     */
    <X, K, V> MapJoin<X, K, V> joinMap(String attributeName, JoinType jt);	
}
