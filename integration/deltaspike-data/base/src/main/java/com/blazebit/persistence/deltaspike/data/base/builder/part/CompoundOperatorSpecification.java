/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.deltaspike.data.base.builder.part;

import com.blazebit.persistence.deltaspike.data.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CompoundOperatorSpecification<T> implements Specification<T> {

    private final boolean and;
    private final List<Specification<T>> specifications;

    public CompoundOperatorSpecification(boolean and, List<Specification<T>> specifications) {
        this.and = and;
        this.specifications = specifications;
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>(specifications.size());
        for (int i = 0; i < specifications.size(); i++) {
            Predicate predicate = specifications.get(i).toPredicate(root, query, cb);
            if (predicate != null) {
                predicates.add(predicate);
            }
        }
        if (and) {
            return cb.and(predicates.toArray(new Predicate[predicates.size()]));
        } else {
            return cb.or(predicates.toArray(new Predicate[predicates.size()]));
        }
    }

    public static <T> CompoundOperatorSpecification<T> and(List<Specification<T>> specifications) {
        if (specifications == null || specifications.isEmpty()) {
            return null;
        }
        return new CompoundOperatorSpecification<>(true, specifications);
    }

    public static <T> CompoundOperatorSpecification<T> or(List<Specification<T>> specifications) {
        if (specifications == null || specifications.isEmpty()) {
            return null;
        }
        return new CompoundOperatorSpecification<>(false, specifications);
    }
}