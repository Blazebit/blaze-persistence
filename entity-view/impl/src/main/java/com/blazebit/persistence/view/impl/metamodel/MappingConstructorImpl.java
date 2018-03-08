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

import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.MappingConstructor;
import com.blazebit.persistence.view.metamodel.ParameterAttribute;

import javax.persistence.metamodel.ManagedType;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class MappingConstructorImpl<X> implements MappingConstructor<X> {

    private final String name;
    private final ManagedViewTypeImplementor<X> declaringType;
    private final Constructor<X> javaConstructor;
    private final List<AbstractParameterAttribute<? super X, ?>> parameters;
    private final InheritanceSubtypeConstructorConfiguration<X> defaultInheritanceParametersAttributesClosureConfiguration;
    private final InheritanceSubtypeConstructorConfiguration<X> overallInheritanceParametersAttributesClosureConfiguration;
    private final Map<Map<ManagedViewTypeImplementor<? extends X>, String>, InheritanceSubtypeConstructorConfiguration<X>> inheritanceSubtypeParameterAttributesClosureConfigurations;
    private final boolean hasJoinFetchedCollections;

    @SuppressWarnings("unchecked")
    public MappingConstructorImpl(ManagedViewTypeImplementor<X> viewType, String name, ConstructorMapping mapping, MetamodelBuildingContext context) {
        this.name = name;
        this.declaringType = viewType;
        this.javaConstructor = (Constructor<X>) mapping.getConstructor();

        List<ParameterAttributeMapping> parameterMappings = mapping.getParameterMappings();
        int parameterCount = parameterMappings.size();
        List<AbstractParameterAttribute<? super X, ?>> parameters = new ArrayList<AbstractParameterAttribute<? super X, ?>>(parameterCount);
        List<AbstractParameterAttribute<? super X, ?>> overallParameters = new ArrayList<AbstractParameterAttribute<? super X, ?>>(parameterCount + 10);
        boolean hasJoinFetchedCollections = false;

        for (int i = 0; i < parameterCount; i++) {
            AbstractParameterAttribute<? super X, ?> parameter = parameterMappings.get(i).getParameterAttribute(this, context);
            hasJoinFetchedCollections = hasJoinFetchedCollections || parameter.hasJoinFetchedCollections();
            parameters.add(parameter);
            overallParameters.add(parameter);
        }

        this.parameters = Collections.unmodifiableList(parameters);

        // Collect the overall parameters from constructors of subtypes with the same name
        for (ManagedViewType<? extends X> subtype : viewType.getOverallInheritanceSubtypeConfiguration().getInheritanceSubtypes()) {
            if (subtype == declaringType) {
                continue;
            }

            MappingConstructorImpl<? extends X> constructor = (MappingConstructorImpl<? extends X>) subtype.getConstructor(name);

            if (constructor == null) {
                continue;
            }

            overallParameters.addAll((List<AbstractParameterAttribute<? super X, ?>>) (List<?>) constructor.getParameterAttributes());
        }

        this.overallInheritanceParametersAttributesClosureConfiguration = new InheritanceSubtypeConstructorConfiguration<>(overallParameters);
        this.defaultInheritanceParametersAttributesClosureConfiguration = new InheritanceSubtypeConstructorConfiguration<>(createParameterAttributesClosure(viewType.getDefaultInheritanceSubtypeConfiguration().getInheritanceSubtypeConfiguration(), context), overallInheritanceParametersAttributesClosureConfiguration);

        Map<Map<ManagedViewTypeImplementor<? extends X>, String>, InheritanceSubtypeConstructorConfiguration<X>> inheritanceSubtypeParameterAttributesClosureConfigurations = new HashMap<>();

        for (Map<ManagedViewTypeImplementor<? extends X>, String> subtypes : viewType.getInheritanceSubtypeConfigurations().keySet()) {
            // Reuse the default closure already built
            if (subtypes == viewType.getDefaultInheritanceSubtypeConfiguration()) {
                inheritanceSubtypeParameterAttributesClosureConfigurations.put(subtypes, defaultInheritanceParametersAttributesClosureConfiguration);
            } else {
                inheritanceSubtypeParameterAttributesClosureConfigurations.put(subtypes, new InheritanceSubtypeConstructorConfiguration<>(createParameterAttributesClosure(subtypes, context), overallInheritanceParametersAttributesClosureConfiguration));
            }
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

        return parametersAttributeClosure;
    }

    public InheritanceSubtypeConstructorConfiguration<X> getSubtypeConstructorConfiguration(Map<ManagedViewTypeImplementor<? extends X>, String> inheritanceSubtypeMappings) {
        if (inheritanceSubtypeMappings == null || inheritanceSubtypeMappings.isEmpty() || declaringType.getDefaultInheritanceSubtypeConfiguration().getInheritanceSubtypeConfiguration() == inheritanceSubtypeMappings) {
            return defaultInheritanceParametersAttributesClosureConfiguration;
        }

        return inheritanceSubtypeParameterAttributesClosureConfigurations.get(inheritanceSubtypeMappings);
    }

    public InheritanceSubtypeConstructorConfiguration<X> getDefaultInheritanceParametersAttributesClosureConfiguration() {
        return defaultInheritanceParametersAttributesClosureConfiguration;
    }

    public InheritanceSubtypeConstructorConfiguration<X> getOverallInheritanceParametersAttributesClosureConfiguration() {
        return overallInheritanceParametersAttributesClosureConfiguration;
    }

    public Map<Map<ManagedViewTypeImplementor<? extends X>, String>, InheritanceSubtypeConstructorConfiguration<X>> getInheritanceSubtypeParameterAttributesClosureConfigurations() {
        return inheritanceSubtypeParameterAttributesClosureConfigurations;
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    public static class InheritanceSubtypeConstructorConfiguration<X> {
        private final List<AbstractParameterAttribute<? super X, ?>> parameterAttributesClosure;
        private final int[] overallPositionAssignment;

        public InheritanceSubtypeConstructorConfiguration(List<AbstractParameterAttribute<? super X, ?>> parameterAttributesClosure) {
            this(parameterAttributesClosure, null);
        }

        public InheritanceSubtypeConstructorConfiguration(List<AbstractParameterAttribute<? super X, ?>> parameterAttributesClosure, InheritanceSubtypeConstructorConfiguration<X> overallConfiguration) {
            this.parameterAttributesClosure = Collections.unmodifiableList(parameterAttributesClosure);
            int[] positionAssignment;
            if (overallConfiguration == null) {
                positionAssignment = new int[parameterAttributesClosure.size()];
                int index = 0;
                for (AbstractParameterAttribute<? super X, ?> parameter : parameterAttributesClosure) {
                    positionAssignment[index] = index;
                    index++;
                }
            } else {
                Map<AbstractParameterAttribute<? super X, ?>, Integer> positionMap = new IdentityHashMap<>(parameterAttributesClosure.size());
                int index = 0;
                for (AbstractParameterAttribute<? super X, ?> parameter : parameterAttributesClosure) {
                    positionMap.put(parameter, index);
                    index++;
                }

                positionAssignment = new int[overallConfiguration.parameterAttributesClosure.size()];
                index = 0;
                for (AbstractParameterAttribute<? super X, ?> parameter : overallConfiguration.parameterAttributesClosure) {
                    Integer position = positionMap.get(parameter);
                    positionAssignment[index] = position == null ? -1 : position;
                    index++;
                }

            }
            this.overallPositionAssignment = positionAssignment;
        }

        public List<AbstractParameterAttribute<? super X, ?>> getParameterAttributesClosure() {
            return parameterAttributesClosure;
        }

        public int[] getOverallPositionAssignment() {
            return overallPositionAssignment;
        }
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
