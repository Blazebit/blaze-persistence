package com.blazebit.persistence.criteria.impl;

import javax.persistence.EntityManager;

import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.criteria.BlazeCriteriaBuilder;
import com.blazebit.persistence.criteria.BlazeCriteriaQuery;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class BlazeCriteria {

    private BlazeCriteria() {
    }

    public static BlazeCriteriaBuilder get(EntityManager em, CriteriaBuilderFactory cbf) {
        return new BlazeCriteriaBuilderImpl(em, cbf);
    }

    public static <T> BlazeCriteriaQuery<T> get(EntityManager em, CriteriaBuilderFactory cbf, Class<T> clazz) {
        return new BlazeCriteriaBuilderImpl(em, cbf).createQuery(clazz);
    }
}
