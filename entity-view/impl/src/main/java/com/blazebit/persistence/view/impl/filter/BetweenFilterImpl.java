/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.filter;

import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.WhereBuilder;
import com.blazebit.persistence.view.SubqueryProvider;
import com.blazebit.persistence.view.filter.BetweenFilter;
import com.blazebit.persistence.view.filter.Range;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class BetweenFilterImpl<FilterValue> extends BetweenFilter<FilterValue> {

    private final Range<FilterValue> range;

    public BetweenFilterImpl(Object value) {
        if (value == null) {
            throw new NullPointerException("value");
        }
        if (value instanceof Range<?>) {
            this.range = (Range<FilterValue>) value;
        } else if (value instanceof Object[]) {
            Object[] objects = (Object[]) value;
            if (objects.length != 2) {
                throw new IllegalArgumentException("Range filter needs a Range or Object[2] but got: " + value);
            }
            this.range = Range.between((FilterValue) objects[0], (FilterValue) objects[1]);
        } else {
            throw new IllegalArgumentException("Range filter needs a Range or Object[2] but got: " + value);
        }
    }

    @Override
    public <T extends WhereBuilder<T>> T apply(T whereBuilder, String attributeExpression) {
        RestrictionBuilder<T> restrictionBuilder = whereBuilder.where(attributeExpression);
        if (range.getLowerBound() == null) {
            if (range.isInclusive()) {
                return restrictionBuilder.le(range.getUpperBound());
            } else {
                return restrictionBuilder.lt(range.getUpperBound());
            }
        } else if (range.getUpperBound() == null) {
            if (range.isInclusive()) {
                return restrictionBuilder.ge(range.getLowerBound());
            } else {
                return restrictionBuilder.gt(range.getLowerBound());
            }
        } else {
            if (range.isInclusive()) {
                return restrictionBuilder.between(range.getLowerBound()).and(range.getUpperBound());
            } else {
                return restrictionBuilder.gt(range.getLowerBound()).where(attributeExpression).lt(range.getUpperBound());
            }
        }
    }

    @Override
    public <T extends WhereBuilder<T>> T apply(T whereBuilder, String subqueryAlias, String subqueryExpresion, SubqueryProvider provider) {
        RestrictionBuilder<T> restrictionBuilder;
        if (subqueryAlias == null) {
            restrictionBuilder = provider.createSubquery(whereBuilder.whereSubquery());
        } else {
            restrictionBuilder = provider.createSubquery(whereBuilder.whereSubquery(subqueryAlias, subqueryExpresion));
        }
        if (range.getLowerBound() == null) {
            if (range.isInclusive()) {
                return restrictionBuilder.le(range.getUpperBound());
            } else {
                return restrictionBuilder.lt(range.getUpperBound());
            }
        } else if (range.getUpperBound() == null) {
            if (range.isInclusive()) {
                return restrictionBuilder.ge(range.getLowerBound());
            } else {
                return restrictionBuilder.gt(range.getLowerBound());
            }
        } else {
            if (range.isInclusive()) {
                return restrictionBuilder.between(range.getLowerBound()).and(range.getUpperBound());
            } else {
                restrictionBuilder.gt(range.getLowerBound());
                if (subqueryAlias == null) {
                    return provider.createSubquery(whereBuilder.whereSubquery()).lt(range.getUpperBound());
                } else {
                    return provider.createSubquery(whereBuilder.whereSubquery(subqueryAlias, subqueryExpresion)).lt(range.getUpperBound());
                }
            }
        }
    }
}
