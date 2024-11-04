/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.basic.model;

import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.view.AttributeFilter;
import com.blazebit.persistence.view.AttributeFilterProvider;
import com.blazebit.persistence.view.AttributeFilters;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.filter.BetweenFilter;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@EntityView(Person.class)
public interface CustomRootPersonView extends IdHolderView<Long> {

    @AttributeFilters({
        @AttributeFilter(NotEqualFilter.class),
        @AttributeFilter(name = "betweenFilter", value = BetweenFilter.class)
    })
    public String getName();

    public static class NotEqualFilter<FilterValue> extends AttributeFilterProvider<FilterValue> {

        private final Object value;

        public NotEqualFilter(Class<?> expectedType, Object value) {
            this.value = value;
        }

        @Override
        protected <T> T apply(RestrictionBuilder<T> restrictionBuilder) {
            return restrictionBuilder.notEq(value);
        }

    }
}
