/*
 * Copyright 2014 - 2023 Blazebit.
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

package com.blazebit.persistence.impl.util;

import com.blazebit.persistence.spi.AttributeAccessor;
import com.blazebit.persistence.spi.ExtendedManagedType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Christian Beikov
 * @since 1.4.1
 */
public class CompositeAttributeAccessor<X, Y> implements AttributeAccessor<X, Y> {

    private final AttributeAccessor[] accessors;

    private CompositeAttributeAccessor(AttributeAccessor[] accessors) {
        this.accessors = accessors;
    }

    public static <X, Y> AttributeAccessor<X, Y> of(ExtendedManagedType<?> type, String path) {
        int index = path.indexOf('.');
        AttributeAccessor<?, ?> lastAccessor = type.getAttribute(path).getAccessor();
        if (index == -1) {
            return (AttributeAccessor<X, Y>) lastAccessor;
        }

        List<AttributeAccessor> accessors = new ArrayList<>();
        do {
            accessors.add(type.getAttribute(path.substring(0, index)).getAccessor());
        } while ((index = path.indexOf('.', index + 1)) != -1);

        accessors.add(lastAccessor);
        return new CompositeAttributeAccessor<>(accessors.toArray(new AttributeAccessor[accessors.size()]));
    }

    @Override
    public Y get(X entity) {
        Object result = entity;
        for (int i = 0; i < accessors.length; i++) {
            result = accessors[i].get(result);
        }

        return (Y) result;
    }

    @Override
    public Y getNullSafe(X entity) {
        Object result = entity;
        for (int i = 0; i < accessors.length; i++) {
            if (result == null) {
                return null;
            }
            result = accessors[i].get(result);
        }

        return (Y) result;
    }

    @Override
    public void set(X entity, Y value) {
        Object result = entity;
        int endIndex = accessors.length - 1;
        for (int i = 0; i < endIndex; i++) {
            result = accessors[i].get(result);
        }
        accessors[endIndex].set(result, value);
    }
}
