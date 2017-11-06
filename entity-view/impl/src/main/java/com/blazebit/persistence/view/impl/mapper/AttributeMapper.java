/*
 * Copyright 2014 - 2017 Blazebit.
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

package com.blazebit.persistence.view.impl.mapper;

import com.blazebit.persistence.view.impl.accessor.AttributeAccessor;
import com.blazebit.persistence.view.impl.update.UpdateContext;

import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class AttributeMapper<S, T> implements Mapper<S, T> {

    private final AttributeAccessor[] sourceAttributes;
    private final AttributeAccessor[] targetAttributes;

    AttributeMapper(List<AttributeAccessor> source, List<AttributeAccessor> target) {
        if (source.size() != target.size()) {
            throw new IllegalArgumentException("Invalid size");
        }
        this.sourceAttributes = source.toArray(new AttributeAccessor[source.size()]);
        this.targetAttributes = target.toArray(new AttributeAccessor[target.size()]);
    }

    @Override
    public void map(UpdateContext context, S source, T target) {
        for (int i = 0; i < sourceAttributes.length; i++) {
            Object sourceValue = sourceAttributes[i].getValue(context, source);
            targetAttributes[i].setValue(context, target, sourceValue);
        }
    }
    
}
