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

package com.blazebit.persistence.view.impl.type;

import com.blazebit.persistence.view.spi.BasicUserType;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ImmutableBasicUserType<X> implements BasicUserType<X> {

    public static final BasicUserType<?> INSTANCE = new ImmutableBasicUserType<>();

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public boolean supportsDirtyChecking() {
        return false;
    }

    @Override
    public boolean supportsDeepEqualChecking() {
        return true;
    }

    @Override
    public boolean supportsDeepCloning() {
        return true;
    }

    @Override
    public boolean isEqual(X initial, X current) {
        return initial.equals(current);
    }

    @Override
    public boolean isDeepEqual(X initial, X current) {
        return initial.equals(current);
    }

    @Override
    public int hashCode(X object) {
        return object.hashCode();
    }

    @Override
    public boolean shouldPersist(Object entity) {
        return false;
    }

    @Override
    public String[] getDirtyProperties(Object entity) {
        return null;
    }

    @Override
    public X deepClone(X object) {
        return object;
    }
}
