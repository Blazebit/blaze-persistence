/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.processor.convert;

import com.blazebit.persistence.view.processor.Context;
import com.blazebit.persistence.view.processor.TypeUtils;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.6.0
 */
public class OptionalTypeConverter implements TypeConverter {

    @Override
    public void addRegistrations(Map<String, Map<String, TypeConverter>> converters) {
        Map<String, TypeConverter> map = new HashMap<>();
        map.put("java.lang.Object", new OptionalTypeConverter());
        converters.put("java.util.Optional", map);
    }

    @Override
    public String getUnderlyingType(DeclaredType owningType, TypeMirror declaredTypeMirror, Context context) {
        if (declaredTypeMirror instanceof DeclaredType) {
            List<? extends TypeMirror> typeArguments = ((DeclaredType) declaredTypeMirror).getTypeArguments();
            return TypeUtils.toTypeString(owningType, typeArguments.get(0), context);
        }
        return TypeUtils.toTypeString(owningType, declaredTypeMirror, context);
    }
}
