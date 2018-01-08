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

package com.blazebit.persistence.view.impl.type;

import com.blazebit.persistence.view.spi.type.AbstractMutableBasicUserType;
import com.blazebit.persistence.view.spi.type.BasicUserType;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractLobBasicUserType<T> extends AbstractMutableBasicUserType<T> implements BasicUserType<T> {

    @Override
    public boolean supportsDirtyChecking() {
        return true;
    }

    @Override
    public boolean supportsDirtyTracking() {
        return true;
    }

    @Override
    public boolean supportsDeepEqualChecking() {
        return false;
    }

    @Override
    public boolean supportsDeepCloning() {
        return false;
    }

    @Override
    public String[] getDirtyProperties(T entity) {
        if (entity instanceof LobImplementor<?>) {
            if (((LobImplementor) entity).$$_isDirty()) {
                return DIRTY_MARKER;
            } else {
                return null;
            }
        }
        return DIRTY_MARKER;
    }

    @Override
    public boolean isDeepEqual(T object1, T object2) {
        return object1 == object2;
    }

    @Override
    public int hashCode(T object) {
        return System.identityHashCode(object);
    }

    @Override
    public T deepClone(T object) {
        return object;
    }
}
