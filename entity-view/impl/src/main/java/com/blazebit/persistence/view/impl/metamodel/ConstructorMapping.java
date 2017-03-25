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

package com.blazebit.persistence.view.impl.metamodel;

import javax.persistence.metamodel.ManagedType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ConstructorMapping {

    private final Class<?> entityViewClass;
    private final ManagedType<?> managedType;
    private final String constructorName;
    private final Constructor<?> constructor;
    private final MetamodelBuildingContext context;
    private final List<ParameterAttributeMapping> parameterAttributes;

    public ConstructorMapping(Class<?> entityViewClass, ManagedType<?> managedType, String constructorName, Constructor<?> constructor, MetamodelBuildingContext context) {
        this.entityViewClass = entityViewClass;
        this.managedType = managedType;
        this.constructorName = constructorName;
        this.constructor = constructor;
        this.context = context;

        if (constructor.getExceptionTypes().length != 0) {
            context.addError("The constructor '" + constructor.toString() + "' of the class '" + constructor.getDeclaringClass().getName()
                    + "' may not throw an exception!");
        }

        int parameterCount = constructor.getParameterTypes().length;
        List<ParameterAttributeMapping> parameters = new ArrayList<>(parameterCount);
        for (int i = 0; i < parameterCount; i++) {
            Annotation mapping = AbstractParameterAttribute.getMapping(constructor, i, context);
            if (mapping != null) {
                ParameterAttributeMapping parameter = new ParameterAttributeMapping(entityViewClass, managedType, mapping, context, constructor, i);
                parameters.add(parameter);
            }
        }

        this.parameterAttributes = Collections.unmodifiableList(parameters);
    }

    public Class<?> getEntityViewClass() {
        return entityViewClass;
    }

    public String getConstructorName() {
        return constructorName;
    }

    public Constructor<?> getConstructor() {
        return constructor;
    }

    public List<ParameterAttributeMapping> getParameterAttributes() {
        return parameterAttributes;
    }

    public void initializeViewMappings(Class<?> entityViewRootClass, Map<Class<?>, ViewMapping> viewMappings, Set<Class<?>> dependencies) {
        for (ParameterAttributeMapping attributeMapping : parameterAttributes) {
            attributeMapping.initializeViewMappings(entityViewRootClass, viewMappings, dependencies);
        }
    }
}
