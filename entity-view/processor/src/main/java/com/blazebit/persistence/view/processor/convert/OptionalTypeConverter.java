/*
 * Copyright 2014 - 2023 Blazebit.
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
