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

package com.blazebit.persistence.view.impl.accessor;

import com.blazebit.persistence.view.impl.update.UpdateContext;

import java.util.List;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class NestedAttributeAccessor implements AttributeAccessor {

    private final AttributeAccessor[] accessors;

    NestedAttributeAccessor(List<AttributeAccessor> accessors) {
        this.accessors = accessors.toArray(new AttributeAccessor[accessors.size()]);
    }

    @Override
    public void setValue(UpdateContext context, Object entity, Object value) {
        if (entity == null) {
            return;
        }

        for (int i = 0; i < accessors.length - 1; i++) {
            entity = accessors[i].getOrCreateValue(context, entity);
        }

        accessors[accessors.length - 1].setValue(context, entity, value);
    }

    @Override
    public Object getOrCreateValue(UpdateContext context, Object entity) {
        if (entity == null) {
            return null;
        }

        Object value = entity;
        for (int i = 0; i < accessors.length - 1; i++) {
            value = accessors[i].getOrCreateValue(context, value);
        }

        return accessors[accessors.length - 1].getValue(context, value);
    }

    @Override
    public Object getValue(UpdateContext context, Object entity) {
        if (entity == null) {
            return null;
        }

        Object value = entity;
        for (int i = 0; i < accessors.length - 1; i++) {
            value = accessors[i].getValue(context, value);
            if (value == null) {
                return null;
            }
        }

        return accessors[accessors.length - 1].getValue(context, value);
    }
}
