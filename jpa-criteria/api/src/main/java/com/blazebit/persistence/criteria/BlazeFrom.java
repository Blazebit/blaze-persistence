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

package com.blazebit.persistence.criteria;

import javax.persistence.criteria.From;
import javax.persistence.criteria.JoinType;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import java.util.Set;

/**
 * An extended version of {@link From}.
 *
 * @param <Z> The source type
 * @param <X> The target type
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface BlazeFrom<Z, X> extends From<Z, X>, BlazeFetchParent<Z, X>, BlazePath<X> {

    /**
     * Returns all joins including fetches since fetches are just joins with the fetch flag set to true.
     *
     * @return All joins
     */
    Set<BlazeJoin<X, ?>> getBlazeJoins();
    
    /* Aliased joins */

    /**
     * Like {@link From#join(SingularAttribute)} but allows to set the alias of the {@link BlazeJoin}.
     *
     * @param attribute The target of the join
     * @param alias     The alias for the {@link BlazeJoin}
     * @param <Y>       The join target type
     * @return The resulting join
     */
    <Y> BlazeJoin<X, Y> join(SingularAttribute<? super X, Y> attribute, String alias);

    /**
     * Like {@link From#join(SingularAttribute, JoinType)} but allows to set the alias of the {@link BlazeJoin}.
     *
     * @param attribute The target of the join
     * @param alias     The alias for the {@link BlazeJoin}
     * @param joinType  The join type
     * @param <Y>       The join target type
     * @return The resulting join
     */
    <Y> BlazeJoin<X, Y> join(SingularAttribute<? super X, Y> attribute, String alias, JoinType joinType);

    /**
     * Like {@link From#join(CollectionAttribute)} but allows to set the alias of the {@link BlazeCollectionJoin}.
     *
     * @param collection The target of the join
     * @param alias      The alias for the {@link BlazeCollectionJoin}
     * @param <Y>        The join target type
     * @return The resulting join
     */
    <Y> BlazeCollectionJoin<X, Y> join(CollectionAttribute<? super X, Y> collection, String alias);

    /**
     * Like {@link From#join(SetAttribute)} but allows to set the alias of the {@link BlazeSetJoin}.
     *
     * @param set   The target of the join
     * @param alias The alias for the {@link BlazeSetJoin}
     * @param <Y>   The join target type
     * @return The resulting join
     */
    <Y> BlazeSetJoin<X, Y> join(SetAttribute<? super X, Y> set, String alias);

    /**
     * Like {@link From#join(ListAttribute)} but allows to set the alias of the {@link BlazeListJoin}.
     *
     * @param list  The target of the join
     * @param alias The alias for the {@link BlazeListJoin}
     * @param <Y>   The join target type
     * @return The resulting join
     */
    <Y> BlazeListJoin<X, Y> join(ListAttribute<? super X, Y> list, String alias);

    /**
     * Like {@link From#join(MapAttribute)} but allows to set the alias of the {@link BlazeMapJoin}.
     *
     * @param map   The target of the join
     * @param alias The alias for the {@link BlazeMapJoin}
     * @param <K>   The join target key type
     * @param <V>   The join target value type
     * @return The resulting join
     */
    <K, V> BlazeMapJoin<X, K, V> join(MapAttribute<? super X, K, V> map, String alias);

    /**
     * Like {@link From#join(CollectionAttribute, JoinType)} but allows to set the alias of the {@link BlazeCollectionJoin}.
     *
     * @param collection The target of the join
     * @param alias      The alias for the {@link BlazeCollectionJoin}
     * @param joinType   The join type
     * @param <Y>        The join target type
     * @return The resulting join
     */
    <Y> BlazeCollectionJoin<X, Y> join(CollectionAttribute<? super X, Y> collection, String alias, JoinType joinType);

    /**
     * Like {@link From#join(SetAttribute, JoinType)} but allows to set the alias of the {@link BlazeSetJoin}.
     *
     * @param set      The target of the join
     * @param alias    The alias for the {@link BlazeSetJoin}
     * @param joinType The join type
     * @param <Y>      The join target type
     * @return The resulting join
     */
    <Y> BlazeSetJoin<X, Y> join(SetAttribute<? super X, Y> set, String alias, JoinType joinType);

    /**
     * Like {@link From#join(ListAttribute, JoinType)} but allows to set the alias of the {@link BlazeListJoin}.
     *
     * @param list     The target of the join
     * @param alias    The alias for the {@link BlazeListJoin}
     * @param joinType The join type
     * @param <Y>      The join target type
     * @return The resulting join
     */
    <Y> BlazeListJoin<X, Y> join(ListAttribute<? super X, Y> list, String alias, JoinType joinType);

    /**
     * Like {@link From#join(MapAttribute, JoinType)} but allows to set the alias of the {@link BlazeMapJoin}.
     *
     * @param map      The target of the join
     * @param alias    The alias for the {@link BlazeMapJoin}
     * @param joinType The join type
     * @param <K>      The join target key type
     * @param <V>      The join target value type
     * @return The resulting join
     */
    <K, V> BlazeMapJoin<X, K, V> join(MapAttribute<? super X, K, V> map, String alias, JoinType joinType);

    /**
     * Like {@link From#join(String)} but allows to set the alias of the {@link BlazeJoin}.
     *
     * @param attributeName The name of the attribute for the target of the join
     * @param alias         The alias for the {@link BlazeJoin}
     * @param <X>           The join source type
     * @param <Y>           The join target type
     * @return The resulting join
     */
    <X, Y> BlazeJoin<X, Y> join(String attributeName, String alias);

    /**
     * Like {@link BlazeFrom#join(EntityType)} but allows to set the alias of the {@link BlazeJoin}.
     *
     * @param entityType    The entity type to join
     * @param alias         The alias for the {@link BlazeJoin}
     * @param <Y>           The joined entity type
     * @return The resulting join
     * @since 1.3.0
     */
    <Y> BlazeJoin<X, Y> join(EntityType<Y> entityType, String alias);

    /**
     * Gets the entity type by the given entity type class and delegates to {@link BlazeFrom#join(EntityType, String)}.
     *
     * @param entityTypeClass   The entity type class to join
     * @param alias             The alias for the {@link BlazeJoin}
     * @param <Y>               The joined entity type
     * @return The resulting join
     * @since 1.3.0
     */
    <Y> BlazeJoin<X, Y> join(Class<Y> entityTypeClass, String alias);

    /**
     * Like {@link From#joinCollection(String)} but allows to set the alias of the {@link BlazeCollectionJoin}.
     *
     * @param attributeName The name of the attribute for the target of the join
     * @param alias         The alias for the {@link BlazeCollectionJoin}
     * @param <X>           The join source type
     * @param <Y>           The join target type
     * @return The resulting join
     */
    <X, Y> BlazeCollectionJoin<X, Y> joinCollection(String attributeName, String alias);

    /**
     * Like {@link From#joinSet(String)} but allows to set the alias of the {@link BlazeSetJoin}.
     *
     * @param attributeName The name of the attribute for the target of the join
     * @param alias         The alias for the {@link BlazeSetJoin}
     * @param <X>           The join source type
     * @param <Y>           The join target type
     * @return The resulting join
     */
    <X, Y> BlazeSetJoin<X, Y> joinSet(String attributeName, String alias);

    /**
     * Like {@link From#joinList(String)} but allows to set the alias of the {@link BlazeListJoin}.
     *
     * @param attributeName The name of the attribute for the target of the join
     * @param alias         The alias for the {@link BlazeListJoin}
     * @param <X>           The join source type
     * @param <Y>           The join target type
     * @return The resulting join
     */
    <X, Y> BlazeListJoin<X, Y> joinList(String attributeName, String alias);

    /**
     * Like {@link From#joinMap(String)} but allows to set the alias of the {@link BlazeMapJoin}.
     *
     * @param attributeName The name of the attribute for the target of the join
     * @param alias         The alias for the {@link BlazeMapJoin}
     * @param <X>           The join source type
     * @param <K>           The join target key type
     * @param <V>           The join target value type
     * @return The resulting join
     */
    <X, K, V> BlazeMapJoin<X, K, V> joinMap(String attributeName, String alias);

    /**
     * Like {@link From#join(String, JoinType)} but allows to set the alias of the {@link BlazeJoin}.
     *
     * @param attributeName The name of the attribute for the target of the join
     * @param alias         The alias for the {@link BlazeJoin}
     * @param joinType      The join type
     * @param <X>           The join source type
     * @param <Y>           The join target type
     * @return The resulting join
     */
    <X, Y> BlazeJoin<X, Y> join(String attributeName, String alias, JoinType joinType);

    /**
     * Like {@link BlazeFrom#join(EntityType, JoinType)} but allows to set the alias of the {@link BlazeJoin}.
     *
     * @param entityType    The entity type to join
     * @param alias         The alias for the {@link BlazeJoin}
     * @param joinType      The join type
     * @param <Y>           The joined entity type
     * @return The resulting join
     * @since 1.3.0
     */
    <Y> BlazeJoin<X, Y> join(EntityType<Y> entityType, String alias, JoinType joinType);

    /**
     * Gets the entity type by the given entity type class and delegates to {@link BlazeFrom#join(EntityType, String, JoinType)}.
     *
     * @param entityTypeClass   The entity type class to join
     * @param alias             The alias for the {@link BlazeJoin}
     * @param joinType          The join type
     * @param <Y>               The joined entity type
     * @return The resulting join
     * @since 1.3.0
     */
    <Y> BlazeJoin<X, Y> join(Class<Y> entityTypeClass, String alias, JoinType joinType);

    /**
     * Like {@link From#joinCollection(String, JoinType)} but allows to set the alias of the {@link BlazeCollectionJoin}.
     *
     * @param attributeName The name of the attribute for the target of the join
     * @param alias         The alias for the {@link BlazeCollectionJoin}
     * @param joinType      The join type
     * @param <X>           The join source type
     * @param <Y>           The join target type
     * @return The resulting join
     */
    <X, Y> BlazeCollectionJoin<X, Y> joinCollection(String attributeName, String alias, JoinType joinType);

    /**
     * Like {@link From#joinSet(String, JoinType)} but allows to set the alias of the {@link BlazeSetJoin}.
     *
     * @param attributeName The name of the attribute for the target of the join
     * @param alias         The alias for the {@link BlazeSetJoin}
     * @param joinType      The join type
     * @param <X>           The join source type
     * @param <Y>           The join target type
     * @return The resulting join
     */
    <X, Y> BlazeSetJoin<X, Y> joinSet(String attributeName, String alias, JoinType joinType);

    /**
     * Like {@link From#joinList(String, JoinType)} but allows to set the alias of the {@link BlazeListJoin}.
     *
     * @param attributeName The name of the attribute for the target of the join
     * @param alias         The alias for the {@link BlazeListJoin}
     * @param joinType      The join type
     * @param <X>           The join source type
     * @param <Y>           The join target type
     * @return The resulting join
     */
    <X, Y> BlazeListJoin<X, Y> joinList(String attributeName, String alias, JoinType joinType);

    /**
     * Like {@link From#joinMap(String, JoinType)} but allows to set the alias of the {@link BlazeMapJoin}.
     *
     * @param attributeName The name of the attribute for the target of the join
     * @param alias         The alias for the {@link BlazeMapJoin}
     * @param joinType      The join type
     * @param <X>           The join source type
     * @param <K>           The join target key type
     * @param <V>           The join target value type
     * @return The resulting join
     */
    <X, K, V> BlazeMapJoin<X, K, V> joinMap(String attributeName, String alias, JoinType joinType);

    /* Covariant overrides */

    /**
     * Like {@link From#getCorrelationParent} but returns the subtype {@link BlazeFrom} instead.
     *
     * @return The parent of the correlated From object
     */
    BlazeFrom<Z, X> getCorrelationParent();

    /**
     * Like {@link From#join(SingularAttribute)} but returns the subtype {@link BlazeJoin} instead.
     *
     * @param attribute The target of the join
     * @param <Y>       The join target type
     * @return The resulting join
     */
    <Y> BlazeJoin<X, Y> join(SingularAttribute<? super X, Y> attribute);

    /**
     * Like {@link From#join(SingularAttribute, JoinType)} but returns the subtype {@link BlazeJoin} instead.
     *
     * @param attribute The target of the join
     * @param joinType  The join type
     * @param <Y>       The join target type
     * @return The resulting join
     */
    <Y> BlazeJoin<X, Y> join(SingularAttribute<? super X, Y> attribute, JoinType joinType);

    /**
     * Like {@link From#join(CollectionAttribute)} but returns the subtype {@link BlazeCollectionJoin} instead.
     *
     * @param collection The target of the join
     * @param <Y>        The join target type
     * @return The resulting join
     */
    <Y> BlazeCollectionJoin<X, Y> join(CollectionAttribute<? super X, Y> collection);

    /**
     * Like {@link From#join(SetAttribute)} but returns the subtype {@link BlazeSetJoin} instead.
     *
     * @param set The target of the join
     * @param <Y> The join target type
     * @return The resulting join
     */
    <Y> BlazeSetJoin<X, Y> join(SetAttribute<? super X, Y> set);

    /**
     * Like {@link From#join(ListAttribute)} but returns the subtype {@link BlazeListJoin} instead.
     *
     * @param list The target of the join
     * @param <Y>  The join target type
     * @return The resulting join
     */
    <Y> BlazeListJoin<X, Y> join(ListAttribute<? super X, Y> list);

    /**
     * Like {@link From#join(MapAttribute)} but returns the subtype {@link BlazeMapJoin} instead.
     *
     * @param map The target of the join
     * @param <K> The join target key type
     * @param <V> The join target value type
     * @return The resulting join
     */
    <K, V> BlazeMapJoin<X, K, V> join(MapAttribute<? super X, K, V> map);

    /**
     * Like {@link From#join(CollectionAttribute, JoinType)} but returns the subtype {@link BlazeCollectionJoin} instead.
     *
     * @param collection The target of the join
     * @param joinType   The join type
     * @param <Y>        The join target type
     * @return The resulting join
     */
    <Y> BlazeCollectionJoin<X, Y> join(CollectionAttribute<? super X, Y> collection, JoinType joinType);

    /**
     * Like {@link From#join(SetAttribute, JoinType)} but returns the subtype {@link BlazeSetJoin} instead.
     *
     * @param set      The target of the join
     * @param joinType The join type
     * @param <Y>      The join target type
     * @return The resulting join
     */
    <Y> BlazeSetJoin<X, Y> join(SetAttribute<? super X, Y> set, JoinType joinType);

    /**
     * Like {@link From#join(ListAttribute, JoinType)} but returns the subtype {@link BlazeListJoin} instead.
     *
     * @param list     The target of the join
     * @param joinType The join type
     * @param <Y>      The join target type
     * @return The resulting join
     */
    <Y> BlazeListJoin<X, Y> join(ListAttribute<? super X, Y> list, JoinType joinType);

    /**
     * Like {@link From#join(MapAttribute, JoinType)} but returns the subtype {@link BlazeMapJoin} instead.
     *
     * @param map      The target of the join
     * @param joinType The join type
     * @param <K>      The join target key type
     * @param <V>      The join target value type
     * @return The resulting join
     */
    <K, V> BlazeMapJoin<X, K, V> join(MapAttribute<? super X, K, V> map, JoinType joinType);

    /**
     * Like {@link From#join(String)} but returns the subtype {@link BlazeJoin} instead.
     *
     * @param attributeName The name of the attribute for the target of the join
     * @param <X>           The join source type
     * @param <Y>           The join target type
     * @return The resulting join
     */
    <X, Y> BlazeJoin<X, Y> join(String attributeName);

    /**
     * Creates an inner join to the specified entity type.
     *
     * @param entityType    The entity type to join
     * @param <Y>           The joined entity type
     * @return The resulting join
     * @since 1.3.0
     */
    <Y> BlazeJoin<X, Y> join(EntityType<Y> entityType);

    /**
     * Gets the entity type by the given entity type class and delegates to {@link BlazeFrom#join(EntityType)}.
     *
     * @param entityTypeClass   The entity type class to join
     * @param <Y>               The joined entity type
     * @return The resulting join
     * @since 1.3.0
     */
    <Y> BlazeJoin<X, Y> join(Class<Y> entityTypeClass);

    /**
     * Like {@link From#joinCollection(String)} but returns the subtype {@link BlazeCollectionJoin} instead.
     *
     * @param attributeName The name of the attribute for the target of the join
     * @param <X>           The join source type
     * @param <Y>           The join target type
     * @return The resulting join
     */
    <X, Y> BlazeCollectionJoin<X, Y> joinCollection(String attributeName);

    /**
     * Like {@link From#joinSet(String)} but returns the subtype {@link BlazeSetJoin} instead.
     *
     * @param attributeName The name of the attribute for the target of the join
     * @param <X>           The join source type
     * @param <Y>           The join target type
     * @return The resulting join
     */
    <X, Y> BlazeSetJoin<X, Y> joinSet(String attributeName);

    /**
     * Like {@link From#joinList(String)} but returns the subtype {@link BlazeListJoin} instead.
     *
     * @param attributeName The name of the attribute for the target of the join
     * @param <X>           The join source type
     * @param <Y>           The join target type
     * @return The resulting join
     */
    <X, Y> BlazeListJoin<X, Y> joinList(String attributeName);

    /**
     * Like {@link From#joinMap(String)} but returns the subtype {@link BlazeMapJoin} instead.
     *
     * @param attributeName The name of the attribute for the target of the join
     * @param <X>           The join source type
     * @param <K>           The join target key type
     * @param <V>           The join target value type
     * @return The resulting join
     */
    <X, K, V> BlazeMapJoin<X, K, V> joinMap(String attributeName);

    /**
     * Like {@link From#join(String, JoinType)} but returns the subtype {@link BlazeJoin} instead.
     *
     * @param attributeName The name of the attribute for the target of the join
     * @param joinType      The join type
     * @param <X>           The join source type
     * @param <Y>           The join target type
     * @return The resulting join
     */
    <X, Y> BlazeJoin<X, Y> join(String attributeName, JoinType joinType);

    /**
     * Creates a join of the specified join type to the specified entity type.
     *
     * @param entityType    The entity type to join
     * @param joinType      The join type
     * @param <Y>           The joined entity type
     * @return The resulting join
     * @since 1.3.0
     */
    <Y> BlazeJoin<X, Y> join(EntityType<Y> entityType, JoinType joinType);

    /**
     * Gets the entity type by the given entity type class and delegates to {@link BlazeFrom#join(EntityType, JoinType)}.
     *
     * @param entityTypeClass   The entity type class to join
     * @param joinType          The join type
     * @param <Y>               The joined entity type
     * @return The resulting join
     * @since 1.3.0
     */
    <Y> BlazeJoin<X, Y> join(Class<Y> entityTypeClass, JoinType joinType);

    /**
     * Like {@link From#joinCollection(String, JoinType)} but returns the subtype {@link BlazeCollectionJoin} instead.
     *
     * @param attributeName The name of the attribute for the target of the join
     * @param joinType      The join type
     * @param <X>           The join source type
     * @param <Y>           The join target type
     * @return The resulting join
     */
    <X, Y> BlazeCollectionJoin<X, Y> joinCollection(String attributeName, JoinType joinType);

    /**
     * Like {@link From#joinSet(String, JoinType)} but returns the subtype {@link BlazeSetJoin} instead.
     *
     * @param attributeName The name of the attribute for the target of the join
     * @param joinType      The join type
     * @param <X>           The join source type
     * @param <Y>           The join target type
     * @return The resulting join
     */
    <X, Y> BlazeSetJoin<X, Y> joinSet(String attributeName, JoinType joinType);

    /**
     * Like {@link From#joinList(String, JoinType)} but returns the subtype {@link BlazeListJoin} instead.
     *
     * @param attributeName The name of the attribute for the target of the join
     * @param joinType      The join type
     * @param <X>           The join source type
     * @param <Y>           The join target type
     * @return The resulting join
     */
    <X, Y> BlazeListJoin<X, Y> joinList(String attributeName, JoinType joinType);

    /**
     * Like {@link From#joinMap(String, JoinType)} but returns the subtype {@link BlazeMapJoin} instead.
     *
     * @param attributeName The name of the attribute for the target of the join
     * @param joinType      The join type
     * @param <X>           The join source type
     * @param <K>           The join target key type
     * @param <V>           The join target value type
     * @return The resulting join
     */
    <X, K, V> BlazeMapJoin<X, K, V> joinMap(String attributeName, JoinType joinType);
}
