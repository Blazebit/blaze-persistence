/*
 * Copyright 2014 - 2020 Blazebit.
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
import java.util.Collection;
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
    private final Map<Map<ManagedViewType<? extends X>, String>, InheritanceSubtypeConstructorConfiguration<X>> inheritanceSubtypeParameterAttributesClosureConfigurations;
    private final boolean hasJoinFetchedCollections;
    private final boolean hasEntityAttributes;

    @SuppressWarnings("unchecked")
    public MappingConstructorImpl(ManagedViewTypeImplementor<X> viewType, String name, ConstructorMapping mapping, MetamodelBuildingContext context, EmbeddableOwner embeddableMapping) {
        this.name = name;
        this.declaringType = viewType;
        this.javaConstructor = (Constructor<X>) mapping.getConstructor();

        List<ParameterAttributeMapping> parameterMappings = mapping.getParameterMappings();
        int parameterCount = parameterMappings.size();
        List<AbstractParameterAttribute<? super X, ?>> parameters = new ArrayList<AbstractParameterAttribute<? super X, ?>>(parameterCount);
        List<AbstractParameterAttribute<? super X, ?>> overallParameters = new ArrayList<AbstractParameterAttribute<? super X, ?>>(parameterCount + 10);
        boolean hasJoinFetchedCollections = false;
        boolean hasEntityAttributes = false;

        for (int i = 0; i < parameterCount; i++) {
            AbstractParameterAttribute<? super X, ?> parameter = parameterMappings.get(i).getParameterAttribute(this, context, embeddableMapping);
            hasJoinFetchedCollections = hasJoinFetchedCollections || parameter.hasJoinFetchedCollections();
            hasEntityAttributes = hasEntityAttributes || parameter.hasJpaManagedAttributes();
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

        Map<ManagedViewType<? extends X>, String> defaultInheritanceSubtypeConfiguration = viewType.getDefaultInheritanceSubtypeConfiguration().getInheritanceSubtypeConfiguration();
        this.overallInheritanceParametersAttributesClosureConfiguration = new InheritanceSubtypeConstructorConfiguration<>(this, viewType.getOverallInheritanceSubtypeConfiguration().getInheritanceSubtypeConfiguration().keySet(), overallParameters);
        this.defaultInheritanceParametersAttributesClosureConfiguration = new InheritanceSubtypeConstructorConfiguration<>(this, defaultInheritanceSubtypeConfiguration.keySet(), createParameterAttributesClosure(defaultInheritanceSubtypeConfiguration, context), overallInheritanceParametersAttributesClosureConfiguration);

        Map<Map<ManagedViewType<? extends X>, String>, InheritanceSubtypeConstructorConfiguration<X>> inheritanceSubtypeParameterAttributesClosureConfigurations = new HashMap<>();

        for (Map<ManagedViewType<? extends X>, String> subtypes : viewType.getInheritanceSubtypeConfigurations().keySet()) {
            // Reuse the default closure already built
            if (subtypes == viewType.getDefaultInheritanceSubtypeConfiguration()) {
                inheritanceSubtypeParameterAttributesClosureConfigurations.put(subtypes, defaultInheritanceParametersAttributesClosureConfiguration);
            } else {
                inheritanceSubtypeParameterAttributesClosureConfigurations.put(subtypes, new InheritanceSubtypeConstructorConfiguration<>(this, subtypes.keySet(), createParameterAttributesClosure(subtypes, context), overallInheritanceParametersAttributesClosureConfiguration));
            }
        }

        this.inheritanceSubtypeParameterAttributesClosureConfigurations = Collections.unmodifiableMap(inheritanceSubtypeParameterAttributesClosureConfigurations);
        this.hasJoinFetchedCollections = hasJoinFetchedCollections;
        this.hasEntityAttributes = hasEntityAttributes;
    }

    @SuppressWarnings("unchecked")
    private List<AbstractParameterAttribute<? super X, ?>> createParameterAttributesClosure(Map<ManagedViewType<? extends X>, String> subtypes, MetamodelBuildingContext context) {
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

    public InheritanceSubtypeConstructorConfiguration<X> getSubtypeConstructorConfiguration(Map<ManagedViewType<? extends X>, String> inheritanceSubtypeMappings) {
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

    public Map<Map<ManagedViewType<? extends X>, String>, InheritanceSubtypeConstructorConfiguration<X>> getInheritanceSubtypeParameterAttributesClosureConfigurations() {
        return inheritanceSubtypeParameterAttributesClosureConfigurations;
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    public static class InheritanceSubtypeConstructorConfiguration<X> {
        private final List<AbstractParameterAttribute<? super X, ?>> parameterAttributesClosure;
        private final Map<ManagedViewTypeImplementor<? extends X>, int[]> positionAssignments;

        public InheritanceSubtypeConstructorConfiguration(MappingConstructorImpl<X> constructor, Collection<ManagedViewType<? extends X>> managedViewTypes, List<AbstractParameterAttribute<? super X, ?>> parameterAttributesClosure) {
            this(constructor, managedViewTypes, parameterAttributesClosure, null);
        }

        public InheritanceSubtypeConstructorConfiguration(MappingConstructorImpl<X> constructor, Collection<ManagedViewType<? extends X>> managedViewTypes, List<AbstractParameterAttribute<? super X, ?>> parameterAttributesClosure, InheritanceSubtypeConstructorConfiguration<X> overallConfiguration) {
            this.parameterAttributesClosure = Collections.unmodifiableList(parameterAttributesClosure);
            List<AbstractParameterAttribute<? super X, ?>> parameterAttributesClosureToUse;
            if (overallConfiguration == null) {
                parameterAttributesClosureToUse = parameterAttributesClosure;
            } else {
                parameterAttributesClosureToUse = overallConfiguration.parameterAttributesClosure;
            }
            Map<AbstractParameterAttribute<? super X, ?>, Integer> overallPositionMap = new IdentityHashMap<>(parameterAttributesClosureToUse.size());
            int index = 0;
            for (AbstractParameterAttribute<? super X, ?> parameter : parameterAttributesClosureToUse) {
                overallPositionMap.put(parameter, index);
                index++;
            }

            Map<ManagedViewTypeImplementor<? extends X>, int[]> positionAssignments = new HashMap<>();

            // Then create position assignments for all subtypes
            for (ManagedViewType<? extends X> subtype : managedViewTypes) {
                MappingConstructor<? extends X> subtypeConstructor;
                if (constructor.getDeclaringType() == subtype) {
                    subtypeConstructor = constructor;
                } else {
                    subtypeConstructor = subtype.getConstructor(constructor.getName());
                }
                int[] positionAssignment = new int[subtypeConstructor.getParameterAttributes().size()];

                index = 0;
                for (ParameterAttribute<?, ?> parameter : subtypeConstructor.getParameterAttributes()) {
                    positionAssignment[index] = overallPositionMap.get(parameter);
                    index++;
                }
                positionAssignments.put((ManagedViewTypeImplementor<? extends X>) subtype, positionAssignment);
            }

            this.positionAssignments = Collections.unmodifiableMap(positionAssignments);
        }

        public List<AbstractParameterAttribute<? super X, ?>> getParameterAttributesClosure() {
            return parameterAttributesClosure;
        }

        public int[] getOverallPositionAssignment(ManagedViewTypeImplementor<? extends X> subtype) {
            return positionAssignments.get(subtype);
        }
    }

    public void checkParameters(ManagedType<?> managedType, Map<String, List<String>> collectionMappings, Map<String, List<String>> collectionMappingSingulars, MetamodelBuildingContext context) {
        for (AbstractParameterAttribute<? super X, ?> parameter : parameters) {
            parameter.checkAttribute(managedType, context);

            for (Map.Entry<String, Boolean> entry : parameter.getCollectionJoinMappings(managedType, context).entrySet()) {
                if (entry.getValue()) {
                    List<String> locations = collectionMappingSingulars.get(entry.getKey());
                    if (locations == null) {
                        locations = new ArrayList<>(2);
                        collectionMappingSingulars.put(entry.getKey(), locations);
                    }

                    locations.add("Parameter with the index '" + parameter.getIndex() + "' of the constructor '" + parameter.getDeclaringConstructor().getJavaConstructor() + "'");
                } else {
                    List<String> locations = collectionMappings.get(entry.getKey());
                    if (locations == null) {
                        locations = new ArrayList<>(2);
                        collectionMappings.put(entry.getKey(), locations);
                    }

                    locations.add("Parameter with the index '" + parameter.getIndex() + "' of the constructor '" + parameter.getDeclaringConstructor().getJavaConstructor() + "'");
                }
            }
        }
    }

    public void checkNestedParameters(List<AbstractAttribute<?, ?>> parents, ManagedType<?> managedType, MetamodelBuildingContext context, boolean hasMultisetParent) {
        for (AbstractParameterAttribute<? super X, ?> parameter : parameters) {
            parameter.checkNestedAttribute(parents, managedType, context, hasMultisetParent);
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

    public boolean hasEntityAttributes() {
        return hasEntityAttributes;
    }
}
