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

package com.blazebit.persistence.view.impl.metamodel;

import com.blazebit.persistence.view.spi.EntityViewConstructorMapping;
import com.blazebit.persistence.view.spi.EntityViewMapping;
import com.blazebit.persistence.view.spi.EntityViewParameterMapping;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ConstructorMapping implements EntityViewConstructorMapping {

    private final ViewMapping viewMapping;
    private final String constructorName;
    private final Constructor<?> constructor;
    private final List<ParameterAttributeMapping> parameterAttributes;

    public ConstructorMapping(ViewMapping viewMapping, String constructorName, Constructor<?> constructor, List<ParameterAttributeMapping> parameters, MetamodelBootContext context) {
        this.viewMapping = viewMapping;
        this.constructorName = constructorName;
        this.constructor = constructor;

        if (constructorName == null || constructorName.isEmpty()) {
            context.addError("The constructor '" + constructor.toString() + "' of the class '" + constructor.getDeclaringClass().getName()
                    + "' must have a non-empty name!");
        }
        if (constructor.getExceptionTypes().length != 0) {
            context.addError("The constructor '" + constructor.toString() + "' of the class '" + constructor.getDeclaringClass().getName()
                    + "' may not throw an exception!");
        }

        this.parameterAttributes = Collections.unmodifiableList(parameters);
    }

    @Override
    public String getName() {
        return constructorName;
    }

    @Override
    public Constructor<?> getConstructor() {
        return constructor;
    }

    @Override
    public EntityViewMapping getDeclaringView() {
        return viewMapping;
    }

    @Override
    public List<EntityViewParameterMapping> getParameters() {
        return Collections.<EntityViewParameterMapping>unmodifiableList(parameterAttributes);
    }

    public List<ParameterAttributeMapping> getParameterMappings() {
        return parameterAttributes;
    }

    public void initializeViewMappings(MetamodelBuildingContext context) {
        for (ParameterAttributeMapping attributeMapping : parameterAttributes) {
            attributeMapping.initializeViewMappings(context);
        }
    }

    public boolean validateDependencies(MetamodelBuildingContext context, Set<Class<?>> dependencies, boolean reportError) {
        boolean error = false;
        for (ParameterAttributeMapping attributeMapping : parameterAttributes) {
            if (attributeMapping.validateDependencies(context, dependencies, reportError)) {
                error = true;
                if (!reportError) {
                    return true;
                }
            }
        }
        return error;
    }
}
