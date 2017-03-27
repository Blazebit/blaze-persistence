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
import com.blazebit.reflection.ReflectionUtils;

import javax.persistence.metamodel.ManagedType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ViewMapping {

    private final Class<?> entityViewClass;
    private final EntityView mapping;
    private final MetamodelBuildingContext context;
    private final MethodAttributeMapping idAttribute;
    private final Map<String, MethodAttributeMapping> attributes;
    private final Map<ParametersKey, ConstructorMapping> constructors;
    private ManagedViewTypeImpl<?> viewType;

    public ViewMapping(Class<?> entityViewClass, EntityView mapping, MetamodelBuildingContext context, MethodAttributeMapping idAttribute, Map<String, MethodAttributeMapping> attributes, Map<ParametersKey, ConstructorMapping> constructors) {
        this.entityViewClass = entityViewClass;
        this.mapping = mapping;
        this.context = context;
        this.idAttribute = idAttribute;
        this.attributes = attributes;
        this.constructors = constructors;
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

    public static ViewMapping initializeViewMappings(Class<?> entityViewRootClass, Class<?> entityViewClass, MetamodelBuildingContext context, Map<Class<?>, ViewMapping> viewMappings, Set<Class<?>> dependencies) {
        ViewMapping existingMapping = viewMappings.get(entityViewClass);
        if (existingMapping != null) {
            return existingMapping;
        }

        EntityView entityView = AnnotationUtils.findAnnotation(entityViewClass, EntityView.class);
        Class<?> entityClass = entityView.value();
        ManagedType<?> managedType = context.getEntityMetamodel().managedType(entityClass);

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

        ViewMapping viewMapping = new ViewMapping(entityViewClass, entityView, context, idAttribute, attributes, constructors);
        viewMappings.put(entityViewClass, viewMapping);
        for (MethodAttributeMapping attributeMapping : attributes.values()) {
            attributeMapping.initializeViewMappings(entityViewRootClass, viewMappings, dependencies);
        }
        for (ConstructorMapping constructorMapping : constructors.values()) {
            constructorMapping.initializeViewMappings(entityViewRootClass, viewMappings, dependencies);
        }

        return viewMapping;
    }
}
