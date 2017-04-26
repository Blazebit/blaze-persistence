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

import com.blazebit.annotation.AnnotationUtils;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.EntityViewInheritance;
import com.blazebit.persistence.view.EntityViewInheritanceMapping;
import com.blazebit.reflection.ReflectionUtils;

import javax.persistence.metamodel.ManagedType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ViewMapping implements Comparable<ViewMapping> {

    private final Class<?> entityViewClass;
    private final EntityView mapping;
    private final MetamodelBuildingContext context;
    private final MethodAttributeMapping idAttribute;
    private final Map<String, MethodAttributeMapping> attributes;
    private final Map<ParametersKey, ConstructorMapping> constructors;
    private final String inheritanceMapping;
    private final Set<ViewMapping> inheritanceSubtypes;
    private final Set<ViewMapping> inheritanceSupertypes;
    private final InheritanceViewMapping defaultInheritanceViewMapping;
    private final Set<InheritanceViewMapping> inheritanceViewMappings;
    private ManagedViewTypeImpl<?> viewType;

    public ViewMapping(Class<?> entityViewClass, EntityView mapping, MetamodelBuildingContext context, MethodAttributeMapping idAttribute, Map<String, MethodAttributeMapping> attributes, Map<ParametersKey, ConstructorMapping> constructors, String inheritanceMapping, Set<ViewMapping> inheritanceSubtypes) {
        this.entityViewClass = entityViewClass;
        this.mapping = mapping;
        this.context = context;
        this.idAttribute = idAttribute;
        this.attributes = attributes;
        this.constructors = constructors;
        this.inheritanceMapping = inheritanceMapping;
        this.inheritanceSubtypes = inheritanceSubtypes;
        this.inheritanceSupertypes = new HashSet<>();
        this.inheritanceViewMappings = new HashSet<>();
        inheritanceViewMappings.add(defaultInheritanceViewMapping = new InheritanceViewMapping(this, inheritanceSubtypes));
    }

    public InheritanceViewMapping getDefaultInheritanceViewMapping() {
        return defaultInheritanceViewMapping;
    }

    public Class<?> getEntityViewClass() {
        return entityViewClass;
    }

    public EntityView getMapping() {
        return mapping;
    }

    public MethodAttributeMapping getIdAttribute() {
        return idAttribute;
    }

    public Map<String, MethodAttributeMapping> getAttributes() {
        return attributes;
    }

    public Map<ParametersKey, ConstructorMapping> getConstructors() {
        return constructors;
    }

    public String getInheritanceMapping() {
        if (inheritanceMapping == null && !inheritanceSupertypes.isEmpty()) {
            Class<?> entityClass = mapping.value();
            // Check all super type inheritance mappings. If we encounter that a super type uses
            // an entity class that is not a proper super type of our entity class, we can't infer a type inheritance mapping
            for (ViewMapping supertypeMapping : inheritanceSupertypes) {
                Class<?> supertypeEntityClass = supertypeMapping.getMapping().value();
                if (!supertypeEntityClass.isAssignableFrom(entityClass) || supertypeEntityClass == entityClass) {
                    return inheritanceMapping;
                }
            }

            // If we get here, we know that our entity class type is a proper subtype of all super type inheritance mappings
            return "TYPE(this) = " + context.getEntityMetamodel().entity(entityClass).getName();
        }

        return inheritanceMapping;
    }

    public Set<ViewMapping> getInheritanceSubtypes() {
        return inheritanceSubtypes;
    }

    public Set<ViewMapping> getInheritanceSupertypes() {
        return inheritanceSupertypes;
    }

    public Set<InheritanceViewMapping> getInheritanceViewMappings() {
        return inheritanceViewMappings;
    }

    public ManagedViewTypeImpl<?> getManagedViewType() {
        if (viewType == null) {
            if (idAttribute != null) {
                return viewType = new ViewTypeImpl<Object>(this, context);
            } else {
                return viewType = new FlatViewTypeImpl<Object>(this, context);
            }
        }

        return viewType;
    }

    public static ViewMapping initializeViewMappings(Class<?> entityViewRootClass, Class<?> entityViewClass, MetamodelBuildingContext context, Map<Class<?>, ViewMapping> viewMappings, Set<Class<?>> dependencies, AttributeMapping originatingAttributeMapping) {
        ViewMapping existingMapping = viewMappings.get(entityViewClass);
        if (existingMapping != null) {
            return existingMapping;
        }

        EntityView entityView = AnnotationUtils.findAnnotation(entityViewClass, EntityView.class);
        Class<?> entityClass = entityView.value();
        ManagedType<?> managedType = context.getEntityMetamodel().managedType(entityClass);

        // Inheritance
        EntityViewInheritance inheritanceAnnotation = entityViewClass.getAnnotation(EntityViewInheritance.class);
        EntityViewInheritanceMapping inheritanceMappingAnnotation = entityViewClass.getAnnotation(EntityViewInheritanceMapping.class);
        String inheritanceMapping;

        if (inheritanceMappingAnnotation != null) {
            inheritanceMapping = inheritanceMappingAnnotation.value();
        } else {
            inheritanceMapping = null;
        }

        Set<ViewMapping> inheritanceSubtypes;
        Set<Class<?>> subtypeClasses;

        if (inheritanceAnnotation == null) {
            inheritanceSubtypes = Collections.emptySet();
            subtypeClasses = Collections.emptySet();
        } else if (inheritanceAnnotation.value().length == 0) {
            inheritanceSubtypes = new TreeSet<>();
            subtypeClasses = initializeSubtypes(entityViewRootClass, entityViewClass, context, viewMappings, dependencies, originatingAttributeMapping, inheritanceSubtypes, context.findSubtypes(entityViewClass), false);
        } else {
            inheritanceSubtypes = new LinkedHashSet<>();
            subtypeClasses = initializeSubtypes(entityViewRootClass, entityViewClass, context, viewMappings, dependencies, originatingAttributeMapping, inheritanceSubtypes, new HashSet<>(Arrays.asList(inheritanceAnnotation.value())), true);
        }

        // Attributes
        MethodAttributeMapping idAttribute = null;
        // We use a tree map to get a deterministic attribute order
        Map<String, MethodAttributeMapping> attributes = new TreeMap<>();

        // Deterministic order of methods for #203
        Method[] methods = entityViewClass.getMethods();
        Set<String> handledMethods = new HashSet<String>(methods.length);
        Set<String> concreteMethods = new HashSet<String>(methods.length);
        // mark concrete methods as handled
        for (Method method : methods) {
            if (!Modifier.isAbstract(method.getModifiers()) && !method.isBridge()) {
                handledMethods.add(method.getName());
                concreteMethods.add(method.getName());
            }
        }
        for (Class<?> c : ReflectionUtils.getSuperTypes(entityViewClass)) {
            for (Method method : c.getDeclaredMethods()) {
                if (Modifier.isPublic(method.getModifiers()) && Modifier.isAbstract(method.getModifiers()) && !method.isBridge()) {
                    final String methodName = method.getName();
                    if (handledMethods.add(methodName)) {
                        String attributeName = AbstractMethodAttribute.extractAttributeName(entityViewClass, method, context);

                        if (attributeName != null && !attributes.containsKey(attributeName)) {
                            Annotation mapping = AbstractMethodAttribute.getMapping(attributeName, method, context);
                            if (mapping != null) {
                                MethodAttributeMapping attribute = new MethodAttributeMapping(entityViewClass, managedType, mapping, context, attributeName, method);
                                attributes.put(attributeName, attribute);
                                if (attribute.isId()) {
                                    idAttribute = attribute.handleReplacement(idAttribute);
                                }
                            }
                        }
                    } else if (!concreteMethods.contains(methodName)) {
                        // Check if the attribute definition is conflicting
                        String attributeName = AbstractMethodAttribute.extractAttributeName(entityViewClass, method, context);
                        Annotation mapping = AbstractMethodAttribute.getMapping(attributeName, method, context);

                        // We ignore methods that only have implicit mappings
                        if (mapping instanceof MappingLiteral) {
                            continue;
                        }

                        MethodAttributeMapping originalAttribute = attributes.get(attributeName);
                        MethodAttributeMapping attribute = new MethodAttributeMapping(entityViewClass, managedType, mapping, context, attributeName, method);
                        MethodAttributeMapping newAttribute = attribute.handleReplacement(originalAttribute);

                        if (newAttribute != originalAttribute) {
                            attributes.put(attributeName, newAttribute);
                            if (newAttribute.isId()) {
                                idAttribute = newAttribute.handleReplacement(idAttribute);
                            }
                        }
                    }
                }
            }
        }

        // Deterministic order of constructors
        Map<ParametersKey, ConstructorMapping> constructors = new TreeMap<>();

        for (Constructor<?> constructor : entityViewClass.getDeclaredConstructors()) {
            String constructorName = MappingConstructorImpl.extractConstructorName(constructor);
            ConstructorMapping constructorMapping = new ConstructorMapping(entityViewClass, managedType, constructorName, constructor, context);
            constructors.put(new ParametersKey(constructor.getParameterTypes()), constructorMapping);
        }

        ViewMapping viewMapping = new ViewMapping(entityViewClass, entityView, context, idAttribute, attributes, constructors, inheritanceMapping, inheritanceSubtypes);
        for (ViewMapping subtype : inheritanceSubtypes) {
            subtype.inheritanceSupertypes.add(viewMapping);
        }
        viewMappings.put(entityViewClass, viewMapping);
        for (MethodAttributeMapping attributeMapping : attributes.values()) {
            attributeMapping.initializeViewMappings(entityViewRootClass, viewMappings, dependencies);
        }
        for (ConstructorMapping constructorMapping : constructors.values()) {
            constructorMapping.initializeViewMappings(entityViewRootClass, viewMappings, dependencies);
        }

        // Cleanup dependencies after constructing the view type
        dependencies.removeAll(subtypeClasses);

        return viewMapping;
    }

    private static Set<Class<?>> initializeSubtypes(Class<?> entityViewRootClass, Class<?> entityViewClass, MetamodelBuildingContext context, Map<Class<?>, ViewMapping> viewMappings, Set<Class<?>> dependencies, AttributeMapping originatingAttributeMapping, Set<ViewMapping> inheritanceSubtypes, Set<Class<?>> subtypeClasses, boolean explicit) {
        for (Class<?> subtypeClass : subtypeClasses) {
            if (!dependencies.add(subtypeClass) && subtypeClass != entityViewClass) {
                originatingAttributeMapping.circularDependencyError(dependencies);
                return subtypeClasses;
            }
        }
        for (Class<?> subtypeClass : subtypeClasses) {
            if (entityViewClass == subtypeClass) {
                if (explicit) {
                    context.addError("Entity view type '" + entityViewClass.getName() + "' declared itself in @EntityViewInheritance as subtype which is not allowed!");
                }
                continue;
            }
            if (explicit) {
                if (!entityViewClass.isAssignableFrom(subtypeClass)) {
                    context.addError("Entity view subtype '" + subtypeClass.getName() + "' was explicitly declared as subtype in '" + entityViewClass.getName() + "' but isn't a Java subtype!");
                }
            }

            ViewMapping subtypeMapping = ViewMapping.initializeViewMappings(entityViewRootClass, subtypeClass, context, viewMappings, dependencies, originatingAttributeMapping);
            inheritanceSubtypes.add(subtypeMapping);
            inheritanceSubtypes.addAll(subtypeMapping.getInheritanceSubtypes());
        }

        return subtypeClasses;
    }

    @Override
    public int compareTo(ViewMapping o) {
        return getEntityViewClass().getName().compareTo(o.getEntityViewClass().getName());
    }
}
