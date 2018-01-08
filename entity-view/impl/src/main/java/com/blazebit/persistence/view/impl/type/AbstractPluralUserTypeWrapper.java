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

import com.blazebit.persistence.view.spi.type.BasicUserType;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractPluralUserTypeWrapper<C, V> implements BasicUserType<C> {

    protected final BasicUserType<V> elementUserType;

    public AbstractPluralUserTypeWrapper(BasicUserType<V> elementUserType) {
        this.elementUserType = elementUserType;
    }

    @Override
    public boolean isMutable() {
        throw new UnsupportedOperationException("plural attribute");
    }

    @Override
    public boolean supportsDirtyChecking() {
        throw new UnsupportedOperationException("plural attribute");
    }

    @Override
    public boolean supportsDirtyTracking() {
        return false;
    }

    @Override
    public boolean supportsDeepEqualChecking() {
        throw new UnsupportedOperationException("plural attribute");
    }

    @Override
    public boolean supportsDeepCloning() {
        throw new UnsupportedOperationException("plural attribute");
    }

    public boolean isEqual(C object1, C object2) {
        throw new UnsupportedOperationException("plural attribute");
    }

    public boolean isDeepEqual(C object1, C object2) {
        throw new UnsupportedOperationException("plural attribute");
    }

    public int hashCode(C object) {
        throw new UnsupportedOperationException("plural attribute");
    }

    @Override
    public boolean shouldPersist(C entity) {
        throw new UnsupportedOperationException("plural attribute");
    }

    @Override
    public String[] getDirtyProperties(C object) {
        throw new UnsupportedOperationException("plural attribute");
    }
}
