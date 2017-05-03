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

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public final class EntityIdAttributeAccessor implements AttributeAccessor {

    public static final AttributeAccessor INSTANCE = new EntityIdAttributeAccessor();

    private EntityIdAttributeAccessor() {
    }

    @Override
    public Object getValue(UpdateContext context, Object entity) {
        return context.getEntityManager().getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(entity);
    }

    @Override
    public Object getOrCreateValue(UpdateContext context, Object entity) {
        throw new UnsupportedOperationException("Read only!");
    }

    @Override
    public void setValue(UpdateContext context, Object entity, Object value) {
        throw new UnsupportedOperationException("Read only!");
    }
}
