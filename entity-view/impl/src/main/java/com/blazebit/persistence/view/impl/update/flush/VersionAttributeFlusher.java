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

package com.blazebit.persistence.view.impl.update.flush;

import com.blazebit.persistence.view.OptimisticLockException;
import com.blazebit.persistence.view.impl.accessor.AttributeAccessor;
import com.blazebit.persistence.view.impl.proxy.MutableStateTrackable;
import com.blazebit.persistence.view.impl.update.UpdateContext;
import com.blazebit.persistence.view.spi.type.VersionBasicUserType;

import javax.persistence.Query;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class VersionAttributeFlusher<E, V> extends BasicAttributeFlusher<E, V> {

    private final boolean jpaVersion;

    public VersionAttributeFlusher(String attributeName, String mapping, VersionBasicUserType<Object> userType, String updateFragment, String parameterName, AttributeAccessor entityAttributeAccessor, AttributeAccessor viewAttributeAccessor, boolean jpaVersion) {
        super(attributeName, mapping, true, false, true, false, false, false, null, new TypeDescriptor(
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                null,
                null,
                userType,
                null,
                null,
                null
        ), updateFragment, parameterName, entityAttributeAccessor, viewAttributeAccessor, null, null, null);
        this.jpaVersion = jpaVersion;
    }

    public final V nextValue(V value) {
        return ((VersionBasicUserType<V>) elementDescriptor.getBasicUserType()).nextValue(value);
    }

    @Override
    public void flushQuery(UpdateContext context, String parameterPrefix, Query query, Object view, V value, UnmappedOwnerAwareDeleter ownerAwareDeleter) {
        if (query != null) {
            String parameter;
            if (parameterPrefix == null) {
                parameter = parameterName;
            } else {
                parameter = parameterPrefix + parameterName;
            }
            V nextValue = nextValue(value);
            query.setParameter(parameter, nextValue);
            ((MutableStateTrackable) view).$$_setVersion(nextValue);
        }
    }

    public void flushQueryInitialVersion(UpdateContext context, String parameterPrefix, Query query, Object view, V value) {
        if (query != null) {
            String parameter;
            if (parameterPrefix == null) {
                parameter = parameterName;
            } else {
                parameter = parameterPrefix + parameterName;
            }
            query.setParameter(parameter, value);
        }
    }

    @Override
    public boolean flushEntity(UpdateContext context, E entity, Object view, V value, Runnable postReplaceListener) {
        Object entityValue = entityAttributeAccessor.getValue(entity);
        if (value != entityValue && !elementDescriptor.getBasicUserType().isDeepEqual(value, entityValue)) {
            throw new OptimisticLockException(entity, view);
        }
        V nextValue = nextValue(value);
        // When the attribute is a JPA version we don't update the value
        // Not quite sure this is completely correct since I suppose there could be multiple version increases
        // But since the increase only happens at flush time, we can't read the next value yet
        if (!jpaVersion) {
            entityAttributeAccessor.setValue(entity, nextValue);
        }
        ((MutableStateTrackable) view).$$_setVersion(nextValue);
        return true;
    }

    @Override
    public List<PostFlushDeleter> remove(UpdateContext context, E entity, Object view, V value) {
        if (entity != null) {
            Object entityValue = entityAttributeAccessor.getValue(entity);
            if (value != entityValue && !elementDescriptor.getBasicUserType().isDeepEqual(value, entityValue)) {
                throw new OptimisticLockException(entity, view);
            }
        }
        return Collections.emptyList();
    }
}
