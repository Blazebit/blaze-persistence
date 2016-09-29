package com.blazebit.persistence.impl.plan;

import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface SelectQueryPlan<T> {

    public List<T> getResultList();

    public T getSingleResult();

}
