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

import com.blazebit.persistence.spi.JpaProvider;
import com.blazebit.persistence.view.spi.type.BasicUserType;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class EntityBasicUserType implements BasicUserType<Object> {

    private final JpaProvider jpaProvider;

    public EntityBasicUserType(JpaProvider jpaProvider) {
        this.jpaProvider = jpaProvider;
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public boolean supportsDirtyChecking() {
        return false;
    }

    @Override
    public boolean supportsDirtyTracking() {
        return false;
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
    public boolean isEqual(Object initial, Object current) {
        return jpaProvider.getIdentifier(initial).equals(jpaProvider.getIdentifier(current));
    }

    @Override
    public boolean isDeepEqual(Object initial, Object current) {
        return false;
    }

    @Override
    public int hashCode(Object object) {
        return jpaProvider.getIdentifier(object).hashCode();
    }

    @Override
    public boolean shouldPersist(Object entity) {
        return entity != null && jpaProvider.getIdentifier(entity) == null;
    }

    @Override
    public String[] getDirtyProperties(Object entity) {
        return DIRTY_MARKER;
    }

    @Override
    public Object deepClone(Object object) {
        return object;
    }
}
