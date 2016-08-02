package com.blazebit.persistence.criteria;

import javax.persistence.criteria.CollectionJoin;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

public interface BlazeCollectionJoin<Z, E> extends CollectionJoin<Z, E>, BlazeJoin<Z, E> {

    /* Compatibility for JPA 2.1 */

    /**
     * Modify the join to restrict the result according to the
     * specified ON condition. Replaces the previous ON condition,
     * if any.
     * Return the join object
     * @param restriction a simple or compound boolean expression
     * @return the modified join object
     */
    public BlazeCollectionJoin<Z, E> on(Expression<Boolean> restriction);

    /**
     * Modify the join to restrict the result according to the
     * specified ON condition. Replaces the previous ON condition,
     * if any.
     * Return the join object
     * @param restrictions zero or more restriction predicates
     * @return the modified join object
     */
    public BlazeCollectionJoin<Z, E> on(Predicate... restrictions);
}
