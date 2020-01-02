/*
 * Copyright 2014 - 2020 Blazebit.
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

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public final class PassthroughAttributeAccessor implements AttributeAccessor {

    public static final AttributeAccessor INSTANCE = new PassthroughAttributeAccessor();

    private PassthroughAttributeAccessor() {
    }

    @Override
    public void setValue(Object object, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getOrCreateValue(Object object) {
        return object;
    }

    @Override
    public Object getValue(Object object) {
        return object;
    }
}
