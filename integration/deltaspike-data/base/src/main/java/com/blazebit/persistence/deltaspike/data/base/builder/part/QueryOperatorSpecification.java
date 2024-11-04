/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.deltaspike.data.base.builder.part;

import com.blazebit.persistence.deltaspike.data.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class QueryOperatorSpecification<T> implements Specification<T> {

    private final String property;
    private final QueryOperator operator;
    private final String param1;
    private final String param2;

    public QueryOperatorSpecification(String property, QueryOperator operator, String param1, String param2) {
        this.property = property;
        this.operator = operator;
        this.param1 = param1;
        this.param2 = param2;
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        Expression leftExpr = root.get(property);
        Expression paramExpr1 = this.param1 == null ? null : cb.parameter(leftExpr.getJavaType(), this.param1);
        Expression paramExpr2 = this.param2 == null ? null : cb.parameter(leftExpr.getJavaType(), this.param2);
        switch (operator) {
            case Equal:
                return cb.equal(leftExpr, paramExpr1);
            case Like:
                return cb.like(leftExpr, paramExpr1);
            case IsNull:
                return cb.isNull(leftExpr);
            case Between:
                return cb.between(leftExpr, paramExpr1, paramExpr2);
            case LessThan:
                return cb.lessThan(leftExpr, paramExpr1);
            case NotEqual:
                return cb.notEqual(leftExpr, paramExpr1);
            case IsNotNull:
                return cb.isNotNull(leftExpr);
            case IgnoreCase:
            case EqualIgnoreCase:
                return cb.equal(cb.upper(leftExpr), cb.upper(paramExpr1));
            case GreaterThan:
                return cb.greaterThan(leftExpr, paramExpr1);
            case LessThanEquals:
                return cb.lessThanOrEqualTo(leftExpr, paramExpr1);
            case LikeIgnoreCase:
                return cb.like(cb.upper(leftExpr), cb.upper(paramExpr1));
            case GreaterThanEquals:
                return cb.greaterThanOrEqualTo(leftExpr, paramExpr1);
            case NotEqualIgnoreCase:
                return cb.notEqual(cb.upper(leftExpr), cb.upper(paramExpr1));
            default:
                throw new IllegalArgumentException("Unsupported operator: " + operator);
        }
    }
}