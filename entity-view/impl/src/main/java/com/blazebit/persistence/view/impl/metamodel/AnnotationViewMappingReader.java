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

import com.blazebit.annotation.AnnotationUtils;
import com.blazebit.persistence.view.BatchFetch;
import com.blazebit.persistence.view.CreatableEntityView;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.EntityViewInheritance;
import com.blazebit.persistence.view.EntityViewInheritanceMapping;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.LockMode;
import com.blazebit.persistence.view.LockOwner;
import com.blazebit.persistence.view.PostCreate;
import com.blazebit.persistence.view.UpdatableEntityView;
import com.blazebit.persistence.view.ViewConstructor;
import com.blazebit.reflection.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class AnnotationViewMappingReader implements ViewMappingReader {

    private final MetamodelBootContext context;
    private final AnnotationMethodAttributeMappingReader methodAttributeMappingReader;
    private final AnnotationParameterAttributeMappingReader parameterAttributeMappingReader;

    public AnnotationViewMappingReader(MetamodelBootContext context) {
        this.context = context;
        this.methodAttributeMappingReader = new AnnotationMethodAttributeMappingReader(context);
        this.parameterAttributeMappingReader = new AnnotationParameterAttributeMappingReader(context);
    }

    @Override
    public MetamodelBootContext getContext() {
        return context;
    }

    @Override
    public ViewMapping readViewMapping(Class<?> entityViewClass) {
        ViewMapping existingMapping = context.getViewMapping(entityViewClass);
        if (existingMapping != null) {
            return existingMapping;
        }

        EntityView entityView = AnnotationUtils.findAnnotation(entityViewClass, EntityView.class);
        if (entityView == null) {
            return null;
        }
        Class<?> entityClass = entityView.value();
        String entityViewName = entityView.name();

        ViewMapping viewMapping = new ViewMappingImpl(entityViewClass, entityClass, entityViewName, context);
        context.addViewMapping(entityViewClass, viewMapping);

        BatchFetch batchFetch = AnnotationUtils.findAnnotation(entityViewClass, BatchFetch.class);
        if (batchFetch != null) {
            viewMapping.setDefaultBatchSize(batchFetch.size());
        }

        UpdatableEntityView updatableEntityView = AnnotationUtils.findAnnotation(entityViewClass, UpdatableEntityView.class);

        if (updatableEntityView != null) {
            viewMapping.setUpdatable(true);
            viewMapping.setFlushMode(updatableEntityView.mode());
            viewMapping.setFlushStrategy(updatableEntityView.strategy());
            viewMapping.setLockMode(updatableEntityView.lockMode());
        }

        CreatableEntityView creatableEntityView = AnnotationUtils.findAnnotation(entityViewClass, CreatableEntityView.class);

        if (creatableEntityView != null) {
            viewMapping.setCreatable(true);
            viewMapping.setValidatePersistability(creatableEntityView.validatePersistability());
            viewMapping.getExcludedAttributes().addAll(Arrays.asList(creatableEntityView.excludedEntityAttributes()));
        }

        if (viewMapping.isCreatable() || viewMapping.isUpdatable()) {
            LockOwner lockOwner = AnnotationUtils.findAnnotation(entityViewClass, LockOwner.class);
            if (lockOwner != null) {
                viewMapping.setLockOwner(lockOwner.value());
            }
        } else {
            viewMapping.setLockMode(LockMode.NONE);
        }

        // Inheritance
        // Note that the usage of Class.getAnnotation is on purpose
        // For the full discussion see: https://github.com/Blazebit/blaze-persistence/issues/475
        EntityViewInheritance inheritanceAnnotation = entityViewClass.getAnnotation(EntityViewInheritance.class);
        EntityViewInheritanceMapping inheritanceMappingAnnotation = entityViewClass.getAnnotation(EntityViewInheritanceMapping.class);
        String inheritanceMapping;

        if (inheritanceMappingAnnotation != null) {
            inheritanceMapping = inheritanceMappingAnnotation.value();
        } else {
            inheritanceMapping = null;
        }

        viewMapping.setInheritanceMapping(inheritanceMapping);

        if (inheritanceAnnotation == null) {
            viewMapping.setInheritanceSubtypesResolved(true);
        } else if (inheritanceAnnotation.value().length > 0) {
            viewMapping.getInheritanceSubtypeClasses().addAll(Arrays.asList(inheritanceAnnotation.value()));
            viewMapping.setInheritanceSubtypesResolved(true);
        }

        // Attributes
        MethodAttributeMapping idAttribute = null;

        Map<String, MethodAttributeMapping> attributes = viewMapping.getMethodAttributes();

        Method postCreateMethod = null;
        Method postCreateMethodCandidate = null;
        List<Method> specialMethods = new ArrayList<>();
        // Deterministic order of methods for #203
        Method[] methods = entityViewClass.getMethods();
        Set<String> handledMethods = new HashSet<>(methods.length);
        Set<String> concreteMethods = new HashSet<>(methods.length);
        // mark concrete methods as handled
        for (Method method : methods) {
            if (!Modifier.isAbstract(method.getModifiers()) && !method.isBridge()) {
                handledMethods.add(method.getName());
                concreteMethods.add(method.getName());
                if (!entityViewClass.isInterface() && AnnotationUtils.findAnnotation(method, PostCreate.class) != null) {
                    if (postCreateMethod != null) {
                        context.addError(
                                "Multiple post create methods found:" +
                                "\n\t" + postCreateMethod.getDeclaringClass().getName() + "." + postCreateMethod.getName() +
                                "\n\t" + method.getDeclaringClass().getName() + "." + method.getName()
                        );
                    } else {
                        postCreateMethod = method;
                    }
                }
            }
        }

        for (Class<?> c : ReflectionUtils.getSuperTypes(entityViewClass)) {
            for (Method method : c.getDeclaredMethods()) {
                if (!Modifier.isPrivate(method.getModifiers()) && Modifier.isAbstract(method.getModifiers()) && !method.isBridge()) {
                    final String methodName = method.getName();
                    if (handledMethods.add(methodName)) {
                        // Allow abstract method to return EntityViewManager
                        if (method.getReturnType() == EntityViewManager.class) {
                            specialMethods.add(method);
                        } else {
                            String attributeName = AbstractMethodAttribute.extractAttributeName(entityViewClass, method, context);

                            if (attributeName != null && !attributes.containsKey(attributeName)) {
                                Annotation mapping = AbstractMethodAttribute.getMapping(attributeName, method, context);
                                if (mapping != null) {
                                    MethodAttributeMapping attribute = methodAttributeMappingReader.readMethodAttributeMapping(viewMapping, mapping, attributeName, method);
                                    attributes.put(attributeName, attribute);

                                    if (attribute.isId()) {
                                        idAttribute = attribute.handleReplacement(idAttribute);
                                    }
                                }
                            }
                        }
                    } else if (!concreteMethods.contains(methodName)) {
                        // Check if the attribute definition is conflicting
                        String attributeName = AbstractMethodAttribute.getAttributeName(method);
                        Annotation mapping = AbstractMethodAttribute.getMapping(attributeName, method, context);

                        // We ignore methods that only have implicit mappings
                        if (mapping instanceof MappingLiteral) {
                            continue;
                        }

                        MethodAttributeMapping originalAttribute = attributes.get(attributeName);
                        MethodAttributeMapping attribute = methodAttributeMappingReader.readMethodAttributeMapping(viewMapping, mapping, attributeName, method);
                        MethodAttributeMapping newAttribute = attribute.handleReplacement(originalAttribute);

                        if (newAttribute != originalAttribute) {
                            attributes.put(attributeName, newAttribute);
                            if (newAttribute.isId()) {
                                idAttribute = newAttribute.handleReplacement(idAttribute);
                            }
                        }
                    }
                }

                if (postCreateMethod == null && !Modifier.isAbstract(method.getModifiers()) && !method.isBridge() && AnnotationUtils.findAnnotation(method, PostCreate.class) != null) {
                    if (postCreateMethodCandidate != null) {
                        if (postCreateMethodCandidate.getDeclaringClass().isAssignableFrom(method.getDeclaringClass())) {
                            postCreateMethodCandidate = method;
                        } else if (!method.getDeclaringClass().isAssignableFrom(postCreateMethodCandidate.getDeclaringClass())) {
                            context.addError(
                                    "Multiple post create methods found in super types of entity view '" + entityViewClass.getName() + "':" +
                                    "\n\t" + postCreateMethodCandidate.getDeclaringClass().getName() + "." + postCreateMethodCandidate.getName() +
                                    "\n\t" + method.getDeclaringClass().getName() + "." + method.getName()
                            );
                        }
                    } else {
                        postCreateMethodCandidate = method;
                    }
                }
            }
        }

        if (postCreateMethod == null) {
            postCreateMethod = postCreateMethodCandidate;
        }

        viewMapping.setPostCreateMethod(postCreateMethod);
        viewMapping.setSpecialMethods(specialMethods);
        viewMapping.setIdAttributeMapping(idAttribute);

        for (Constructor<?> constructor : entityViewClass.getDeclaredConstructors()) {
            int parameterCount = constructor.getParameterTypes().length;
            List<ParameterAttributeMapping> parameters = new ArrayList<>(parameterCount);
            String constructorName = extractConstructorName(constructor);
            ConstructorMapping constructorMapping = new ConstructorMapping(viewMapping, constructorName, constructor, parameters, context);
            Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();
            for (int i = 0; i < parameterCount; i++) {
                Annotation mapping = AbstractParameterAttribute.getMapping(constructor, i, context);
                if (mapping != null) {
                    ParameterAttributeMapping parameter = parameterAttributeMappingReader.readParameterAttributeMapping(viewMapping, mapping, constructorMapping, i, parameterAnnotations[i]);
                    parameters.add(parameter);
                }
            }

            viewMapping.addConstructor(constructorMapping);
        }

        return viewMapping;
    }

    public static String extractConstructorName(Constructor<?> c) {
        ViewConstructor viewConstructor = c.getAnnotation(ViewConstructor.class);

        if (viewConstructor == null) {
            return "init";
        }

        return viewConstructor.value();
    }
}
