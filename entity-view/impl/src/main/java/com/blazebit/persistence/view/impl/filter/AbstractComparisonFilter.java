/*
 * Copyright 2014 Blazebit.
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

import com.blazebit.persistence.Filterable;
import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.view.Filter;
import com.blazebit.persistence.view.SubqueryProvider;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public abstract class AbstractComparisonFilter implements Filter {
    
    protected final Object value;
    protected final SubqueryProvider subqueryProvider;

    public AbstractComparisonFilter(Class<?> expectedType, Object value) {
        if (value == null) {
            throw new NullPointerException("value");
        }

        if (value instanceof SubqueryProvider) {
            this.value = null;
            this.subqueryProvider = (SubqueryProvider) value;
        } else if (expectedType.isInstance(value)) {
            this.value = value;
            this.subqueryProvider = null;
        } else {
            this.value = FilterUtils.parseValue(expectedType, value);
            this.subqueryProvider = null;
        }
    }

    @Override
    public <T> T apply(RestrictionBuilder<T> rb) {
        if (subqueryProvider == null) {
            return applyRestriction(rb);
        } else {
            return subqueryProvider.createSubquery(applySubquery(rb));
        }
    }
    
    protected abstract <T> T applyRestriction(RestrictionBuilder<T> rb);
    
    protected abstract <T> SubqueryInitiator<T> applySubquery(RestrictionBuilder<T> rb);
}
