/*
 * Copyright 2014 - 2022 Blazebit.
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

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.Map;

/**
 * A contract for a converter to convert between an entity view model type and an underlying type.
 *
 * @author Christian Beikov
 * @since 1.6.0
 */
public interface TypeConverter {

    /**
     * Add the converter registrations for this converter.
     *
     * @param converters The map to register to
     */
    public void addRegistrations(Map<String, Map<String, TypeConverter>> converters);

    /**
     * Extract the underlying type from the declared type.
     * The owning class is the concrete entity view class which contains a field or method of the declared type.
     *
     * @param owningClass The class owning the declared type
     * @param declaredType The declared type as present in the entity view model
     * @param context The annotation processor context
     * @return The actual underlying type
     */
    public String getUnderlyingType(DeclaredType owningClass, TypeMirror declaredType, Context context);
}
