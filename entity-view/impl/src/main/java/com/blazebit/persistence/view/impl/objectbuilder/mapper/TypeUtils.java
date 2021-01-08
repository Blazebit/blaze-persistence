/*
 * Copyright 2014 - 2021 Blazebit.
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

package com.blazebit.persistence.view.impl.objectbuilder.mapper;

import com.blazebit.persistence.view.metamodel.BasicType;
import com.blazebit.persistence.view.metamodel.Type;
import com.blazebit.persistence.view.spi.type.BasicUserTypeStringSupport;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public final class TypeUtils {

    private TypeUtils() {
    }

    public static BasicUserTypeStringSupport<Object> forType(Type<?> type) {
        if (type == null || type.getMappingType() != Type.MappingType.BASIC) {
            return null;
        }
        return (BasicUserTypeStringSupport<Object>) ((BasicType<?>) type).getUserType();
    }
}
