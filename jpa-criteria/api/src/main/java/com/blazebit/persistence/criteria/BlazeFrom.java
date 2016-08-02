package com.blazebit.persistence.criteria;

import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import java.util.Set;

public interface BlazeFrom<Z, X> extends From<Z, X>, BlazeFetchParent<Z, X> {

    /**
     * Returns all joins including fetches since fetches are just joins with the fetch flag set to true.
     *
     * @return all joins
     */
    Set<BlazeJoin<X, ?>> getBlazeJoins();
    
    /* Aliased joins */

    <Y> BlazeJoin<X, Y> join(SingularAttribute<? super X, Y> attribute, String alias);

    <Y> BlazeJoin<X, Y> join(SingularAttribute<? super X, Y> attribute, String alias, JoinType jt);

    <Y> BlazeCollectionJoin<X, Y> join(CollectionAttribute<? super X, Y> collection, String alias);

    <Y> BlazeSetJoin<X, Y> join(SetAttribute<? super X, Y> set, String alias);

    <Y> BlazeListJoin<X, Y> join(ListAttribute<? super X, Y> list, String alias);

    <K, V> BlazeMapJoin<X, K, V> join(MapAttribute<? super X, K, V> map, String alias);

    <Y> BlazeCollectionJoin<X, Y> join(CollectionAttribute<? super X, Y> collection, String alias, JoinType jt);

    <Y> BlazeSetJoin<X, Y> join(SetAttribute<? super X, Y> set, String alias, JoinType jt);

    <Y> BlazeListJoin<X, Y> join(ListAttribute<? super X, Y> list, String alias, JoinType jt);

    <K, V> BlazeMapJoin<X, K, V> join(MapAttribute<? super X, K, V> map, String alias, JoinType jt);

    <X, Y> BlazeJoin<X, Y> join(String attributeName, String alias);   

    <X, Y> BlazeCollectionJoin<X, Y> joinCollection(String attributeName, String alias);   

    <X, Y> BlazeSetJoin<X, Y> joinSet(String attributeName, String alias); 

    <X, Y> BlazeListJoin<X, Y> joinList(String attributeName, String alias);

    <X, K, V> BlazeMapJoin<X, K, V> joinMap(String attributeName, String alias);   

    <X, Y> BlazeJoin<X, Y> join(String attributeName, String alias, JoinType jt);

    <X, Y> BlazeCollectionJoin<X, Y> joinCollection(String attributeName, String alias, JoinType jt);  

    <X, Y> BlazeSetJoin<X, Y> joinSet(String attributeName, String alias, JoinType jt);    

    <X, Y> BlazeListJoin<X, Y> joinList(String attributeName, String alias, JoinType jt);  

    <X, K, V> BlazeMapJoin<X, K, V> joinMap(String attributeName, String alias, JoinType jt);  

    /* Covariant overrides */

    BlazeFrom<Z, X> getCorrelationParent();

    <Y> BlazeJoin<X, Y> join(SingularAttribute<? super X, Y> attribute);

    <Y> BlazeJoin<X, Y> join(SingularAttribute<? super X, Y> attribute, JoinType jt);

    <Y> BlazeCollectionJoin<X, Y> join(CollectionAttribute<? super X, Y> collection);

    <Y> BlazeSetJoin<X, Y> join(SetAttribute<? super X, Y> set);

    <Y> BlazeListJoin<X, Y> join(ListAttribute<? super X, Y> list);

    <K, V> BlazeMapJoin<X, K, V> join(MapAttribute<? super X, K, V> map);

    <Y> BlazeCollectionJoin<X, Y> join(CollectionAttribute<? super X, Y> collection, JoinType jt);

    <Y> BlazeSetJoin<X, Y> join(SetAttribute<? super X, Y> set, JoinType jt);

    <Y> BlazeListJoin<X, Y> join(ListAttribute<? super X, Y> list, JoinType jt);

    <K, V> BlazeMapJoin<X, K, V> join(MapAttribute<? super X, K, V> map, JoinType jt);

    <X, Y> BlazeJoin<X, Y> join(String attributeName);   

    <X, Y> BlazeCollectionJoin<X, Y> joinCollection(String attributeName);   

    <X, Y> BlazeSetJoin<X, Y> joinSet(String attributeName); 

    <X, Y> BlazeListJoin<X, Y> joinList(String attributeName);

    <X, K, V> BlazeMapJoin<X, K, V> joinMap(String attributeName);   

    <X, Y> BlazeJoin<X, Y> join(String attributeName, JoinType jt);

    <X, Y> BlazeCollectionJoin<X, Y> joinCollection(String attributeName, JoinType jt);  

    <X, Y> BlazeSetJoin<X, Y> joinSet(String attributeName, JoinType jt);    

    <X, Y> BlazeListJoin<X, Y> joinList(String attributeName, JoinType jt);  

    <X, K, V> BlazeMapJoin<X, K, V> joinMap(String attributeName, JoinType jt);  
}
