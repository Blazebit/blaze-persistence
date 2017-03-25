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

import com.blazebit.persistence.view.ViewConstructor;
import com.blazebit.persistence.view.metamodel.MappingConstructor;
import com.blazebit.persistence.view.metamodel.ParameterAttribute;

import javax.persistence.metamodel.ManagedType;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class MappingConstructorImpl<X> implements MappingConstructor<X> {

    private final String name;
    private final ManagedViewTypeImpl<X> declaringType;
    private final Constructor<X> javaConstructor;
    private final List<AbstractParameterAttribute<? super X, ?>> parameters;
    private final boolean hasJoinFetchedCollections;

    @SuppressWarnings("unchecked")
    public MappingConstructorImpl(ManagedViewTypeImpl<X> viewType, String name, ConstructorMapping mapping, MetamodelBuildingContext context) {
        this.name = name;
        this.declaringType = viewType;
        this.javaConstructor = (Constructor<X>) mapping.getConstructor();

        List<ParameterAttributeMapping> parameterMappings = mapping.getParameterAttributes();
        int parameterCount = parameterMappings.size();
        List<AbstractParameterAttribute<? super X, ?>> parameters = new ArrayList<AbstractParameterAttribute<? super X, ?>>(parameterCount);
        boolean hasJoinFetchedCollections = false;

        for (int i = 0; i < parameterCount; i++) {
            AbstractParameterAttribute<? super X, ?> parameter = parameterMappings.get(i).getParameterAttribute(this);
            hasJoinFetchedCollections = hasJoinFetchedCollections || parameter.hasJoinFetchedCollections();
            parameters.add(parameter);
        }

        this.parameters = Collections.unmodifiableList(parameters);
        this.hasJoinFetchedCollections = hasJoinFetchedCollections;
    }

    public void checkParameters(ManagedType<?> managedType, Map<String, List<String>> collectionMappings, MetamodelBuildingContext context) {
        for (AbstractParameterAttribute<? super X, ?> parameter : parameters) {
            parameter.checkAttribute(managedType, context);

            for (String mapping : parameter.getCollectionJoinMappings(managedType, context)) {
                List<String> locations = collectionMappings.get(mapping);
                if (locations == null) {
                    locations = new ArrayList<String>(2);
                    collectionMappings.put(mapping, locations);
                }
                
                locations.add("Parameter with the index '" + parameter.getIndex() + "' of the constructor '" + parameter.getDeclaringConstructor().getJavaConstructor() + "'");
            }
        }
    }

    public void checkNestedParameters(List<AbstractAttribute<?, ?>> parents, ManagedType<?> managedType, MetamodelBuildingContext context) {
        for (AbstractParameterAttribute<? super X, ?> parameter : parameters) {
            parameter.checkNestedAttribute(parents, managedType, context);
        }
    }

    public static String extractConstructorName(Constructor<?> c) {
        ViewConstructor viewConstructor = c.getAnnotation(ViewConstructor.class);

        if (viewConstructor == null) {
            return "init";
        }

        return viewConstructor.value();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ManagedViewTypeImpl<X> getDeclaringType() {
        return declaringType;
    }

    @Override
    public Constructor<X> getJavaConstructor() {
        return javaConstructor;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ParameterAttribute<? super X, ?>> getParameterAttributes() {
        return (List<ParameterAttribute<? super X, ?>>) (List<?>) parameters;
    }

    @Override
    public ParameterAttribute<? super X, ?> getParameterAttribute(int index) {
        return parameters.get(index);
    }

    public boolean hasJoinFetchedCollections() {
        return hasJoinFetchedCollections;
    }
}
