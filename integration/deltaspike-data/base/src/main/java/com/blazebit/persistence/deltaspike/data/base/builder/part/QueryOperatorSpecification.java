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