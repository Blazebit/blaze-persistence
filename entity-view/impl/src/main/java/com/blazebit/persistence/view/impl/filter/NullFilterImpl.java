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

package com.blazebit.persistence.view.impl.filter;

import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.view.SubqueryProvider;
import com.blazebit.persistence.view.filter.NullFilter;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class NullFilterImpl extends NullFilter {

    private final boolean value;

    public NullFilterImpl(Object value) {
        if (value == null) {
            throw new NullPointerException("value");
        }

        if (value instanceof Boolean) {
            this.value = (Boolean) value;
        } else if (value instanceof SubqueryProvider) {
            throw new IllegalArgumentException("Subqueries are not allowed for the NullFilter");
        } else {
            this.value = Boolean.parseBoolean(value.toString());
        }
    }

    @Override
    public <T> T apply(RestrictionBuilder<T> rb) {
        if (value) {
            return rb.isNull();
        } else {
            return rb.isNotNull();
        }
    }
}
