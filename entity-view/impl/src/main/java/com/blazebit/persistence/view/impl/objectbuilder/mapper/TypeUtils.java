/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
