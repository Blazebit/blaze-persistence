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

import com.blazebit.annotation.AnnotationUtils;
import com.blazebit.persistence.view.BatchFetch;
import com.blazebit.persistence.view.CTEProvider;
import com.blazebit.persistence.view.CreatableEntityView;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.EntityViewInheritance;
import com.blazebit.persistence.view.EntityViewInheritanceMapping;
import com.blazebit.persistence.view.EntityViewListener;
import com.blazebit.persistence.view.EntityViewListeners;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.LockMode;
import com.blazebit.persistence.view.LockOwner;
import com.blazebit.persistence.view.PostCommit;
import com.blazebit.persistence.view.PostConvert;
import com.blazebit.persistence.view.PostCreate;
import com.blazebit.persistence.view.PostLoad;
import com.blazebit.persistence.view.PostPersist;
import com.blazebit.persistence.view.PostRemove;
import com.blazebit.persistence.view.PostRollback;
import com.blazebit.persistence.view.PostUpdate;
import com.blazebit.persistence.view.PrePersist;
import com.blazebit.persistence.view.PreRemove;
import com.blazebit.persistence.view.PreUpdate;
import com.blazebit.persistence.view.UpdatableEntityView;
import com.blazebit.persistence.view.ViewConstructor;
import com.blazebit.persistence.view.ViewFilter;
import com.blazebit.persistence.view.ViewFilterProvider;
import com.blazebit.persistence.view.ViewFilters;
import com.blazebit.persistence.view.With;
import com.blazebit.persistence.view.impl.EntityViewListenerFactory;
import com.blazebit.reflection.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class AnnotationMappingReader implements MappingReader {

    private static final Map<Class<?>, LifecycleEntry> LIFECYCLE_ENTRY_MAP;
    private static final LifecycleEntry[] LIFECYCLE_ENTRIES;

    static {
        LifecycleEntry[] lifecycleEntries = new LifecycleEntry[11];
        lifecycleEntries[0] = new LifecycleEntry(0, "post create", PostCreate.class);
        lifecycleEntries[1] = new LifecycleEntry(1, "pre persist", PrePersist.class);
        lifecycleEntries[2] = new LifecycleEntry(2, "post persist", PostPersist.class);
        lifecycleEntries[3] = new LifecycleEntry(3, "pre update", PreUpdate.class);
        lifecycleEntries[4] = new LifecycleEntry(4, "post update", PostUpdate.class);
        lifecycleEntries[5] = new LifecycleEntry(5, "pre remove", PreRemove.class);
        lifecycleEntries[6] = new LifecycleEntry(6, "post remove", PostRemove.class);
        lifecycleEntries[7] = new LifecycleEntry(7, "post rollback", PostRollback.class);
        lifecycleEntries[8] = new LifecycleEntry(8, "post commit", PostCommit.class);
        lifecycleEntries[9] = new LifecycleEntry(9, "post create", PostConvert.class);
        lifecycleEntries[10] = new LifecycleEntry(10, "post load", PostLoad.class);
        LIFECYCLE_ENTRIES = lifecycleEntries;

        Map<Class<?>, LifecycleEntry> lifecycleEntryMap = new HashMap<>(lifecycleEntries.length);
        for (LifecycleEntry lifecycleEntry : lifecycleEntries) {
            lifecycleEntryMap.put(lifecycleEntry.annotation, lifecycleEntry);
        }
        LIFECYCLE_ENTRY_MAP = lifecycleEntryMap;
    }

    private final MetamodelBootContext context;
    private final AnnotationMethodAttributeMappingReader methodAttributeMappingReader;
    private final AnnotationParameterAttributeMappingReader parameterAttributeMappingReader;

    public AnnotationMappingReader(MetamodelBootContext context) {
        this.context = context;
        this.methodAttributeMappingReader = new AnnotationMethodAttributeMappingReader(context);
        this.parameterAttributeMappingReader = new AnnotationParameterAttributeMappingReader(context);
    }

    @Override
    public MetamodelBootContext getContext() {
        return context;
    }

    @Override
    public void readViewListenerMapping(Class<?> entityViewListenerClass, EntityViewListenerFactory<?> factory) {
        EntityViewListener entityViewListener = AnnotationUtils.findAnnotation(entityViewListenerClass, EntityViewListener.class);
        if (entityViewListener == null) {
            EntityViewListeners entityViewListeners = AnnotationUtils.findAnnotation(entityViewListenerClass, EntityViewListeners.class);
            if (entityViewListeners == null) {
                TypeVariable<? extends Class<?>>[] typeParameters = factory.getListenerKind().getTypeParameters();
                if (typeParameters.length > 0) {
                    Class<?>[] typeArguments = new Class<?>[typeParameters.length];
                    for (int i = 0; i < typeParameters.length; i++) {
                        typeArguments[i] = ReflectionUtils.resolveTypeVariable(entityViewListenerClass, typeParameters[i]);
                    }
                    if (typeArguments.length > 1) {
                        context.addEntityViewListener(typeArguments[0], typeArguments[1], factory);
                    } else {
                        context.addEntityViewListener(typeArguments[0], Object.class, factory);
                    }
                }
            } else {
                for (EntityViewListener viewListener : entityViewListeners.value()) {
                    context.addEntityViewListener(viewListener.entityView(), viewListener.entity(), factory);
                }
            }
        } else {
            context.addEntityViewListener(entityViewListener.entityView(), entityViewListener.entity(), factory);
        }
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

        ViewMapping viewMapping = new ViewMappingImpl(entityViewClass, entityClass, context);
        context.addViewMapping(entityViewClass, viewMapping);

        BatchFetch batchFetch = AnnotationUtils.findAnnotation(entityViewClass, BatchFetch.class);
        if (batchFetch != null) {
            viewMapping.setDefaultBatchSize(batchFetch.size());
        }

        Set<Class<? extends CTEProvider>> cteProviders = new LinkedHashSet<>();
        Map<String, Class<? extends ViewFilterProvider>> viewFilterProviders = new HashMap<>();
        for (Annotation a : AnnotationUtils.getAllAnnotations(entityViewClass)) {
            if (a instanceof With) {
                cteProviders.addAll(Arrays.asList(((With) a).value()));
            } else if (a instanceof ViewFilter) {
                ViewFilter viewFilter = (ViewFilter) a;
                addFilterMapping(viewFilter.name(), viewFilter.value(), viewFilterProviders, entityViewClass, context);
            } else if (a instanceof ViewFilters) {
                ViewFilters viewFilters = (ViewFilters) a;
                for (ViewFilter viewFilter : viewFilters.value()) {
                    viewFilterProviders.put(viewFilter.name(), viewFilter.value());
                }
            }
        }
        viewMapping.setCteProviders(cteProviders);
        viewMapping.setViewFilterProviders(viewFilterProviders);

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

        if (updatableEntityView != null || creatableEntityView != null) {
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

        List<Method> specialMethods = new ArrayList<>();
        // Deterministic order of methods for #203
        Method[] methods = entityViewClass.getMethods();
        Set<String> handledMethods = new HashSet<>(methods.length);
        Set<String> concreteMethods = new HashSet<>(methods.length);
        Method[] lifecycleMethods = visitAndCollectLifecycleMethods(entityViewClass, methods, handledMethods, concreteMethods);
        Method[] lifecycleMethodCandidates = new Method[lifecycleMethods.length];

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
                    } else if (!concreteMethods.contains(methodName) && method.getReturnType() != EntityViewManager.class) {
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

                if (!Modifier.isAbstract(method.getModifiers()) && !method.isBridge()) {
                    for (int i = 0; i < lifecycleMethods.length; i++) {
                        if (lifecycleMethods[i] == null && AnnotationUtils.findAnnotation(method, LIFECYCLE_ENTRIES[i].annotation) != null) {
                            if (lifecycleMethodCandidates[i] != null) {
                                if (lifecycleMethodCandidates[i].getDeclaringClass().isAssignableFrom(method.getDeclaringClass())) {
                                    lifecycleMethodCandidates[i] = method;
                                } else if (!method.getDeclaringClass().isAssignableFrom(lifecycleMethodCandidates[i].getDeclaringClass())) {
                                    context.addError(
                                            "Multiple " + LIFECYCLE_ENTRIES[i].name + " methods found in super types of entity view '" + entityViewClass.getName() + "':" +
                                                    "\n\t" + lifecycleMethodCandidates[i].getDeclaringClass().getName() + "." + lifecycleMethodCandidates[i].getName() +
                                                    "\n\t" + method.getDeclaringClass().getName() + "." + method.getName()
                                    );
                                }
                            } else {
                                lifecycleMethodCandidates[i] = method;
                            }
                        }
                    }
                }
            }
        }

        for (int i = 0; i < lifecycleMethods.length; i++) {
            if (lifecycleMethods[i] == null) {
                lifecycleMethods[i] = lifecycleMethodCandidates[i];
            }
        }

        viewMapping.setPostCreateMethod(lifecycleMethods[LIFECYCLE_ENTRY_MAP.get(PostCreate.class).index]);
        viewMapping.setPostConvertMethod(lifecycleMethods[LIFECYCLE_ENTRY_MAP.get(PostConvert.class).index]);
        viewMapping.setPostLoadMethod(lifecycleMethods[LIFECYCLE_ENTRY_MAP.get(PostLoad.class).index]);
        viewMapping.setPrePersistMethod(lifecycleMethods[LIFECYCLE_ENTRY_MAP.get(PrePersist.class).index]);
        viewMapping.setPostPersistMethod(lifecycleMethods[LIFECYCLE_ENTRY_MAP.get(PostPersist.class).index]);
        viewMapping.setPreUpdateMethod(lifecycleMethods[LIFECYCLE_ENTRY_MAP.get(PreUpdate.class).index]);
        viewMapping.setPostUpdateMethod(lifecycleMethods[LIFECYCLE_ENTRY_MAP.get(PostUpdate.class).index]);
        viewMapping.setPreRemoveMethod(lifecycleMethods[LIFECYCLE_ENTRY_MAP.get(PreRemove.class).index]);
        viewMapping.setPostRemoveMethod(lifecycleMethods[LIFECYCLE_ENTRY_MAP.get(PostRemove.class).index]);
        Method postRollback = lifecycleMethods[LIFECYCLE_ENTRY_MAP.get(PostRollback.class).index];
        if (postRollback != null) {
            viewMapping.setPostRollbackMethod(postRollback);
            PostRollback annotation = AnnotationUtils.findAnnotation(postRollback, PostRollback.class);
            viewMapping.setPostRollbackTransitions(annotation.transitions());
        }
        Method postCommit = lifecycleMethods[LIFECYCLE_ENTRY_MAP.get(PostCommit.class).index];
        if (postCommit != null) {
            viewMapping.setPostCommitMethod(postCommit);
            PostCommit annotation = AnnotationUtils.findAnnotation(postCommit, PostCommit.class);
            viewMapping.setPostCommitTransitions(annotation.transitions());
        }
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

    private void addFilterMapping(String filterName, Class<? extends ViewFilterProvider> viewFilterProvider, Map<String, Class<? extends ViewFilterProvider>> viewFilters, Class<?> entityViewClass, MetamodelBootContext context) {
        boolean errorOccurred = false;

        if (filterName != null && filterName.isEmpty()) {
            errorOccurred = true;
            context.addError("Illegal empty name for the filter mapping at the class '" + entityViewClass.getName() + "' with filter class '"
                    + viewFilterProvider.getName() + "'!");
        } else if (viewFilters.containsKey(filterName)) {
            errorOccurred = true;
            context.addError("Illegal duplicate filter name mapping '" + filterName + "' at the class '" + entityViewClass.getName() + "'!");
        }

        if (!errorOccurred) {
            viewFilters.put(filterName, viewFilterProvider);
        }
    }

    private Method[] visitAndCollectLifecycleMethods(Class<?> entityViewClass, Method[] methods, Set<String> handledMethods, Set<String> concreteMethods) {
        Method[] foundMethods = new Method[LIFECYCLE_ENTRY_MAP.size()];
        for (Method method : methods) {
            if (!Modifier.isAbstract(method.getModifiers()) && !method.isBridge()) {
                // mark concrete methods as handled
                handledMethods.add(method.getName());
                concreteMethods.add(method.getName());
                if (!entityViewClass.isInterface()) {
                    for (Annotation annotation : AnnotationUtils.getAllAnnotations(method)) {
                        LifecycleEntry lifecycleEntry = LIFECYCLE_ENTRY_MAP.get(annotation.annotationType());
                        if (lifecycleEntry != null) {
                            if (foundMethods[lifecycleEntry.index] != null) {
                                context.addError(
                                        "Multiple " + lifecycleEntry.name + " methods found:" +
                                                "\n\t" + foundMethods[lifecycleEntry.index].getDeclaringClass().getName() + "." + foundMethods[lifecycleEntry.index].getName() +
                                                "\n\t" + method.getDeclaringClass().getName() + "." + method.getName()
                                );
                            } else {
                                foundMethods[lifecycleEntry.index] = method;
                            }
                        }
                    }
                }
            }
        }
        return foundMethods;
    }

    public static String extractConstructorName(Constructor<?> c) {
        ViewConstructor viewConstructor = c.getAnnotation(ViewConstructor.class);

        if (viewConstructor == null) {
            return "init";
        }

        return viewConstructor.value();
    }

    /**
     * @author Christian Beikov
     * @since 1.4.0
     */
    private static class LifecycleEntry {
        private final int index;
        private final String name;
        private final Class<? extends Annotation> annotation;

        public LifecycleEntry(int index, String name, Class<? extends Annotation> annotation) {
            this.index = index;
            this.name = name;
            this.annotation = annotation;
        }
    }
}
