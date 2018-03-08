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

package com.blazebit.persistence.view.testsuite.basic.model;

import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.view.AttributeFilter;
import com.blazebit.persistence.view.AttributeFilterProvider;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.testsuite.entity.Person;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@EntityView(Person.class)
public interface CustomRootPersonView extends IdHolderView<Long> {

    @AttributeFilter(NotEqualFilter.class)
    public String getName();

    public static class NotEqualFilter extends AttributeFilterProvider {

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
