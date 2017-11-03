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

import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.MappingConstructor;
import com.blazebit.persistence.view.metamodel.ParameterAttribute;

import javax.persistence.metamodel.ManagedType;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class MappingConstructorImpl<X> implements MappingConstructor<X> {

    private final String name;
    private final ManagedViewTypeImplementor<X> declaringType;
    private final Constructor<X> javaConstructor;
    private final List<AbstractParameterAttribute<? super X, ?>> parameters;
    private final List<AbstractParameterAttribute<? super X, ?>> defaultInheritanceParametersAttributesClosureConfiguration;
    private final Map<Map<ManagedViewTypeImplementor<? extends X>, String>, List<AbstractParameterAttribute<? super X, ?>>> inheritanceSubtypeParameterAttributesClosureConfigurations;
    private final boolean hasJoinFetchedCollections;

    @SuppressWarnings("unchecked")
    public MappingConstructorImpl(ManagedViewTypeImplementor<X> viewType, String name, ConstructorMapping mapping, MetamodelBuildingContext context) {
        this.name = name;
        this.declaringType = viewType;
        this.javaConstructor = (Constructor<X>) mapping.getConstructor();

        List<ParameterAttributeMapping> parameterMappings = mapping.getParameterMappings();
        int parameterCount = parameterMappings.size();
        List<AbstractParameterAttribute<? super X, ?>> parameters = new ArrayList<AbstractParameterAttribute<? super X, ?>>(parameterCount);
        boolean hasJoinFetchedCollections = false;

        for (int i = 0; i < parameterCount; i++) {
            AbstractParameterAttribute<? super X, ?> parameter = parameterMappings.get(i).getParameterAttribute(this, context);
            hasJoinFetchedCollections = hasJoinFetchedCollections || parameter.hasJoinFetchedCollections();
            parameters.add(parameter);
        }

        this.parameters = Collections.unmodifiableList(parameters);
        this.defaultInheritanceParametersAttributesClosureConfiguration = createParameterAttributesClosure(viewType.getDefaultInheritanceSubtypeConfiguration().getInheritanceSubtypeConfiguration(), context);

        Map<Map<ManagedViewTypeImplementor<? extends X>, String>, List<AbstractParameterAttribute<? super X, ?>>> inheritanceSubtypeParameterAttributesClosureConfigurations = new HashMap<>();

        for (Map<ManagedViewTypeImplementor<? extends X>, String> subtypes : viewType.getInheritanceSubtypeConfigurations().keySet()) {
            inheritanceSubtypeParameterAttributesClosureConfigurations.put(subtypes, createParameterAttributesClosure(subtypes, context));
        }

        this.inheritanceSubtypeParameterAttributesClosureConfigurations = Collections.unmodifiableMap(inheritanceSubtypeParameterAttributesClosureConfigurations);
        this.hasJoinFetchedCollections = hasJoinFetchedCollections;
    }

    @SuppressWarnings("unchecked")
    private List<AbstractParameterAttribute<? super X, ?>> createParameterAttributesClosure(Map<ManagedViewTypeImplementor<? extends X>, String> subtypes, MetamodelBuildingContext context) {
        List<AbstractParameterAttribute<? super X, ?>> parametersAttributeClosure = new ArrayList<AbstractParameterAttribute<? super X, ?>>(parameters.size());
        parametersAttributeClosure.addAll(parameters);

        // Go through the subtype parameter attributes of the same named constructor and put them in the attribute closure list
        for (ManagedViewType<? extends X> subtype : subtypes.keySet()) {
            if (subtype == declaringType) {
                continue;
            }

            MappingConstructorImpl<? extends X> constructor = (MappingConstructorImpl<? extends X>) subtype.getConstructor(name);

            if (constructor == null) {
                context.addError("Could not find required mapping constructor with name '" + name + "' in inheritance subtype '" + subtype.getJavaType().getName() + "'!");
                continue;
            }

            parametersAttributeClosure.addAll((List<AbstractParameterAttribute<? super X, ?>>) (List<?>) constructor.getParameterAttributes());
        }

        return Collections.unmodifiableList(parametersAttributeClosure);
    }

    public List<AbstractParameterAttribute<? super X, ?>> getSubtypeParameterAttributesClosure(Map<ManagedViewTypeImplementor<? extends X>, String> inheritanceSubtypeMappings) {
        if (inheritanceSubtypeMappings == null || inheritanceSubtypeMappings.isEmpty() || declaringType.getDefaultInheritanceSubtypeConfiguration().getInheritanceSubtypeConfiguration() == inheritanceSubtypeMappings) {
            return defaultInheritanceParametersAttributesClosureConfiguration;
        }

        return inheritanceSubtypeParameterAttributesClosureConfigurations.get(inheritanceSubtypeMappings);
    }

    public List<AbstractParameterAttribute<? super X, ?>> getDefaultInheritanceParametersAttributesClosureConfiguration() {
        return defaultInheritanceParametersAttributesClosureConfiguration;
    }

    public Map<Map<ManagedViewTypeImplementor<? extends X>, String>, List<AbstractParameterAttribute<? super X, ?>>> getInheritanceSubtypeParameterAttributesClosureConfigurations() {
        return inheritanceSubtypeParameterAttributesClosureConfigurations;
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

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ManagedViewTypeImplementor<X> getDeclaringType() {
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
