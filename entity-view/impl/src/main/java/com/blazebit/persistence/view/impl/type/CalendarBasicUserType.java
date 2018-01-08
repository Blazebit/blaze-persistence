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
import com.blazebit.persistence.view.spi.type.VersionBasicUserType;

import java.util.Calendar;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CalendarBasicUserType implements BasicUserType<Calendar>, VersionBasicUserType<Calendar> {

    public static final BasicUserType<?> INSTANCE = new CalendarBasicUserType();

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
        return true;
    }

    @Override
    public boolean supportsDeepCloning() {
        return true;
    }

    @Override
    public boolean isEqual(Calendar initial, Calendar current) {
        return initial.equals(current);
    }

    @Override
    public boolean isDeepEqual(Calendar initial, Calendar current) {
        return initial.equals(current);
    }

    @Override
    public int hashCode(Calendar object) {
        return object.hashCode();
    }

    @Override
    public boolean shouldPersist(Calendar entity) {
        return false;
    }

    @Override
    public String[] getDirtyProperties(Calendar entity) {
        return DIRTY_MARKER;
    }

    @Override
    public Calendar deepClone(Calendar object) {
        return (Calendar) object.clone();
    }

    @Override
    public Calendar nextValue(Calendar current) {
        return Calendar.getInstance();
    }
}
