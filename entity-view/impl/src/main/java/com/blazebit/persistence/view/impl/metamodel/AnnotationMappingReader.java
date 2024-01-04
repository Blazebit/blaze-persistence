/*
 * Copyright 2014 - 2024 Blazebit.
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
import com.blazebit.persistence.view.CorrelationProvider;
import com.blazebit.persistence.view.CreatableEntityView;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.EntityViewInheritance;
import com.blazebit.persistence.view.EntityViewInheritanceMapping;
import com.blazebit.persistence.view.EntityViewListener;
import com.blazebit.persistence.view.EntityViewListeners;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewRoot;
import com.blazebit.persistence.view.EntityViewRoots;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.LockMode;
import com.blazebit.persistence.view.LockOwner;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.MappingCorrelated;
import com.blazebit.persistence.view.MappingCorrelatedSimple;
import com.blazebit.persistence.view.MappingParameter;
import com.blazebit.persistence.view.MappingSubquery;
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
import com.blazebit.persistence.view.Self;
import com.blazebit.persistence.view.UpdatableEntityView;
import com.blazebit.persistence.view.ViewConstructor;
import com.blazebit.persistence.view.ViewFilter;
import com.blazebit.persistence.view.ViewFilterProvider;
import com.blazebit.persistence.view.ViewFilters;
import com.blazebit.persistence.view.With;
import com.blazebit.persistence.view.impl.EntityViewListenerFactory;
import com.blazebit.persistence.view.impl.metamodel.analysis.AssignmentAnalyzer;
import com.blazebit.persistence.view.impl.metamodel.analysis.Frame;
import com.blazebit.persistence.view.spi.EntityViewRootMapping;
import com.blazebit.reflection.ReflectionUtils;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtPrimitiveType;
import javassist.LoaderClassPath;
import javassist.bytecode.Bytecode;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.Descriptor;
import javassist.bytecode.analysis.Type;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
                context.addEntityViewListener(
                    resolveEntityViewClass(null, entityViewListenerClass, factory),
                    resolveEntityClass(null, entityViewListenerClass, factory), factory
                );
            } else {
                for (EntityViewListener viewListener : entityViewListeners.value()) {
                    context.addEntityViewListener(
                        resolveEntityViewClass(viewListener, entityViewListenerClass, factory),
                        resolveEntityClass(viewListener, entityViewListenerClass, factory), factory
                    );
                }
            }
        } else {
            context.addEntityViewListener(
                    resolveEntityViewClass(entityViewListener, entityViewListenerClass, factory),
                    resolveEntityClass(entityViewListener, entityViewListenerClass, factory),
                    factory
            );
        }
    }

    private Class<?> resolveEntityViewClass(EntityViewListener annotation, Class<?> entityViewListenerClass, EntityViewListenerFactory<?> factory) {
        Class<?> entityViewClass = resolveEntityViewClassFromTypeParameters(entityViewListenerClass, factory);
        if (annotation == null || annotation.entityView() == Object.class) {
            return entityViewClass;
        } else {
            if (!entityViewClass.isAssignableFrom(annotation.entityView())) {
                context.addError("The entity view type parameter for listener class " + entityViewListenerClass + " must " +
                        "be at least as general as the value of the entityView property in the EntityViewListener annotation  " + annotation.entityView());
            }
            return annotation.entityView();
        }
    }

    private Class<?> resolveEntityClass(EntityViewListener annotation, Class<?> entityViewListenerClass, EntityViewListenerFactory<?> factory) {
        Class<?> entityClass = resolveEntityClassFromTypeParameters(entityViewListenerClass, factory);
        if (annotation == null || annotation.entity() == Object.class) {
            return entityClass;
        } else {
            if (!entityClass.isAssignableFrom(annotation.entity())) {
                context.addError("The entity type parameter for listener class " + entityViewListenerClass + " must " +
                        "be at least as general as the value of the entity property in the EntityViewListener annotation  " + annotation.entity());
            }
            return annotation.entity();
        }
    }

    private Class<?> resolveEntityViewClassFromTypeParameters(Class<?> entityViewListenerClass, EntityViewListenerFactory<?> factory) {
        TypeVariable<? extends Class<?>>[] typeParameters = factory.getListenerKind().getTypeParameters();
        if (typeParameters.length > 0) {
            return ReflectionUtils.resolveTypeVariable(entityViewListenerClass, typeParameters[0]);
        } else {
            return Object.class;
        }
    }

    private Class<?> resolveEntityClassFromTypeParameters(Class<?> entityViewListenerClass, EntityViewListenerFactory<?> factory) {
        TypeVariable<? extends Class<?>>[] typeParameters = factory.getListenerKind().getTypeParameters();
        if (typeParameters.length > 1) {
            return ReflectionUtils.resolveTypeVariable(entityViewListenerClass, typeParameters[1]);
        } else {
            return Object.class;
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
        boolean isAbstract = entityViewClass.isInterface() || Modifier.isAbstract(entityViewClass.getModifiers());

        BatchFetch batchFetch = AnnotationUtils.findAnnotation(entityViewClass, BatchFetch.class);
        if (batchFetch != null) {
            viewMapping.setDefaultBatchSize(batchFetch.size());
        }

        Set<Class<? extends CTEProvider>> cteProviders = new LinkedHashSet<>();
        Map<String, Class<? extends ViewFilterProvider>> viewFilterProviders = new HashMap<>();
        Map<String, EntityViewRootMapping> viewRootMappings = new LinkedHashMap<>();
        for (Annotation a : AnnotationUtils.getAllAnnotations(entityViewClass)) {
            if (a instanceof With) {
                cteProviders.addAll(Arrays.asList(((With) a).value()));
            } else if (a instanceof ViewFilter) {
                ViewFilter viewFilter = (ViewFilter) a;
                addFilterMapping(viewFilter.name(), viewFilter.value(), viewFilterProviders, entityViewClass, context);
            } else if (a instanceof ViewFilters) {
                ViewFilters viewFilters = (ViewFilters) a;
                for (ViewFilter viewFilter : viewFilters.value()) {
                    addFilterMapping(viewFilter.name(), viewFilter.value(), viewFilterProviders, entityViewClass, context);
                }
            } else if (a instanceof EntityViewRoot) {
                EntityViewRoot entityViewRoot = (EntityViewRoot) a;
                addEntityViewRootMapping(entityViewRoot, viewRootMappings, entityViewClass, context);
            } else if (a instanceof EntityViewRoots) {
                EntityViewRoots entityViewRoots = (EntityViewRoots) a;
                for (EntityViewRoot entityViewRoot : entityViewRoots.value()) {
                    addEntityViewRootMapping(entityViewRoot, viewRootMappings, entityViewClass, context);
                }
            }
        }
        viewMapping.setCteProviders(cteProviders);
        viewMapping.setViewFilterProviders(viewFilterProviders);
        viewMapping.setEntityViewRoots(viewRootMappings.isEmpty() ? Collections.<EntityViewRootMapping>emptySet() : new LinkedHashSet<>(viewRootMappings.values()));

        UpdatableEntityView updatableEntityView = AnnotationUtils.findAnnotation(entityViewClass, UpdatableEntityView.class);

        if (updatableEntityView != null) {
            if (isAbstract) {
                viewMapping.setUpdatable(true);
                viewMapping.setFlushMode(updatableEntityView.mode());
                viewMapping.setFlushStrategy(updatableEntityView.strategy());
                viewMapping.setLockMode(updatableEntityView.lockMode());
            } else {
                context.addError("Only abstract class entity views can be updatable! Remove the @UpdatableEntityView annotation from the entity view class '" + entityViewClass.getName() + "' or make it abstract.");
            }
        }

        CreatableEntityView creatableEntityView = AnnotationUtils.findAnnotation(entityViewClass, CreatableEntityView.class);

        if (creatableEntityView != null) {
            if (isAbstract) {
                viewMapping.setCreatable(true);
                viewMapping.setValidatePersistability(creatableEntityView.validatePersistability());
                viewMapping.getExcludedAttributes().addAll(Arrays.asList(creatableEntityView.excludedEntityAttributes()));
            } else {
                context.addError("Only abstract class entity views can be creatable! Remove the @CreatableEntityView annotation from the entity view class '" + entityViewClass.getName() + "' or make it abstract.");
            }
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

        if (isAbstract) {
            viewMapping.setInheritanceMapping(inheritanceMapping);

            if (inheritanceAnnotation == null) {
                viewMapping.setInheritanceSubtypesResolved(true);
            } else if (inheritanceAnnotation.value().length > 0) {
                viewMapping.getInheritanceSubtypeClasses().addAll(Arrays.asList(inheritanceAnnotation.value()));
                viewMapping.setInheritanceSubtypesResolved(true);
            }
        } else if (inheritanceMapping != null || inheritanceAnnotation != null) {
            context.addError("Only abstract class entity views can use inheritance mappings! Remove the inheritance annotations from the entity view class '" + entityViewClass.getName() + "' or make it abstract.");
        } else {
            viewMapping.setInheritanceSubtypesResolved(true);
        }

        // Attributes
        if (isAbstract) {
            readAbstractClassMappings(entityViewClass, viewMapping);
        } else {
            readClassMappings(entityViewClass, viewMapping);
        }

        return viewMapping;
    }

    private void readAbstractClassMappings(Class<?> entityViewClass, ViewMapping viewMapping) {
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
                                Annotation mapping = getMapping(attributeName, method, true);
                                MethodAttributeMapping attribute = methodAttributeMappingReader.readMethodAttributeMapping(viewMapping, mapping, attributeName, method, method, -1);
                                attributes.put(attributeName, attribute);

                                if (attribute.isId()) {
                                    idAttribute = attribute.handleReplacement(idAttribute);
                                }
                            }
                        }
                    } else if (!concreteMethods.contains(methodName) && method.getReturnType() != EntityViewManager.class && method.getReturnType() != void.class) {
                        // Check if the attribute definition is conflicting
                        String attributeName = AbstractMethodAttribute.getAttributeName(method);
                        Annotation mapping = getMapping(attributeName, method, true);
                        MethodAttributeMapping originalAttribute = attributes.get(attributeName);

                        // We ignore methods that only have implicit mappings
                        if (mapping instanceof MappingLiteral) {
                            // Unless the declaring class of the method is no interface and the already visited method is an interface
                            if (c.isInterface() || originalAttribute == null || !originalAttribute.getMethod().getDeclaringClass().isInterface()) {
                                continue;
                            }
                        }

                        MethodAttributeMapping attribute = methodAttributeMappingReader.readMethodAttributeMapping(viewMapping, mapping, attributeName, method, method, -1);
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
                AnnotatedElement annotatedElement = new MapAnnotatedElement(parameterAnnotations[i]);
                Annotation mapping = getMapping(annotatedElement, constructor, i, context);
                if (mapping != null) {
                    ParameterAttributeMapping parameter = parameterAttributeMappingReader.readParameterAttributeMapping(viewMapping, mapping, constructorMapping, i, annotatedElement);
                    parameters.add(parameter);
                }
            }

            viewMapping.addConstructor(constructorMapping);
        }
    }

    private void readClassMappings(Class<?> entityViewClass, ViewMapping viewMapping) {
        Constructor<?>[] declaredConstructors = entityViewClass.getDeclaredConstructors();
        Constructor<?> canonicalConstructor = null;
        if (declaredConstructors.length == 1) {
            canonicalConstructor = declaredConstructors[0];
        } else {
            // TODO: somehow detect the canonical constructor for Java 14 records
            for (Constructor<?> constructor : declaredConstructors) {
                ViewConstructor viewConstructor = constructor.getAnnotation(ViewConstructor.class);
                if (viewConstructor != null && (viewConstructor.value().isEmpty() || "init".equals(viewConstructor.value()))) {
                    if (canonicalConstructor == null) {
                        canonicalConstructor = constructor;
                    } else {
                        context.addError("Multiple constructors in entity view class '" + entityViewClass.getName() + "' are annotated with @ViewConstructor(\"init\") but exactly one canonical constructor is required!");
                        return;
                    }
                }
            }
        }

        if (canonicalConstructor == null) {
            context.addError("Could not find canonical constructor for entity view class '" + entityViewClass.getName() + "'. Please annotate it with @ViewConstructor(\"init\")!");
            return;
        }

        Map<String, AttributeInfo> fieldsToAccessors = findAccessors(entityViewClass);
        if (fieldsToAccessors == null) {
            return;
        }
        AttributeInfo[] parameterToAccessorMapping = analyzeParameterToAccessorMapping(entityViewClass, canonicalConstructor, fieldsToAccessors);
        boolean ok = true;
        for (int i = 0; i < parameterToAccessorMapping.length; i++) {
            if (parameterToAccessorMapping[i] == null) {
                context.addError("Could not determine the attribute for the parameter at index " + i + " of the canonical constructor: " + canonicalConstructor);
                ok = false;
            } else if (parameterToAccessorMapping[i].getter == null) {
                context.addError("Could not determine the accessor for the attribute for the parameter at index " + i + " of the canonical constructor: " + canonicalConstructor);
                ok = false;
            }
        }
        if (!ok) {
            return;
        }

        Map<String, MethodAttributeMapping> attributes = viewMapping.getMethodAttributes();
        Annotation[][] canonicalParameterAnnotations = canonicalConstructor.getParameterAnnotations();
        MethodAttributeMapping idAttribute = null;

        for (int i = 0; i < parameterToAccessorMapping.length; i++) {
            AttributeInfo attributeInfo = parameterToAccessorMapping[i];
            if (attributeInfo != null) {
                AnnotatedElement annotatedElement = new MapAnnotatedElement(canonicalParameterAnnotations[i]);
                Annotation mapping = getMapping(attributeInfo.attributeName, annotatedElement, false);
                if (mapping == null) {
                    mapping = getMapping(attributeInfo.attributeName, attributeInfo.field, true);
                }
                MethodAttributeMapping attribute = methodAttributeMappingReader.readMethodAttributeMapping(viewMapping, mapping, attributeInfo.attributeName, attributeInfo.getter, annotatedElement, i);
                attributes.put(attributeInfo.attributeName, attribute);

                if (attribute.isId()) {
                    idAttribute = attribute.handleReplacement(idAttribute);
                }
            }
        }

        // TODO: We need to ensure that all attributes from the canonical constructor are always bound
//        for (Constructor<?> constructor : declaredConstructors) {
//            if (constructor != canonicalConstructor) {
//                int parameterCount = constructor.getParameterTypes().length;
//                List<ParameterAttributeMapping> parameters = new ArrayList<>(parameterCount);
//                String constructorName = extractConstructorName(constructor);
//                ConstructorMapping constructorMapping = new ConstructorMapping(viewMapping, constructorName, constructor, parameters, context);
//                Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();
//                for (int i = 0; i < parameterCount; i++) {
//                    AnnotatedElement annotatedElement = new MapAnnotatedElement(parameterAnnotations[i]);
//                    Annotation mapping = getMapping(annotatedElement, constructor, i, context);
//                    if (mapping != null) {
//                        ParameterAttributeMapping parameter = parameterAttributeMappingReader.readParameterAttributeMapping(viewMapping, mapping, constructorMapping, i, annotatedElement);
//                        parameters.add(parameter);
//                    }
//                }
//
//                viewMapping.addConstructor(constructorMapping);
//            }
//        }

        viewMapping.setIdAttributeMapping(idAttribute);
    }

    public static Annotation getMapping(String attributeName, AnnotatedElement annotatedElement, boolean implicitMapping) {
        Mapping mapping = annotatedElement.getAnnotation(Mapping.class);

        if (mapping == null) {
            IdMapping idMapping = annotatedElement.getAnnotation(IdMapping.class);

            if (idMapping != null) {
                if (idMapping.value().isEmpty()) {
                    idMapping = new IdMappingLiteral(attributeName);
                }

                return idMapping;
            }

            MappingParameter mappingParameter = annotatedElement.getAnnotation(MappingParameter.class);

            if (mappingParameter != null) {
                return mappingParameter;
            }

            MappingSubquery mappingSubquery = annotatedElement.getAnnotation(MappingSubquery.class);

            if (mappingSubquery != null) {
                return mappingSubquery;
            }

            MappingCorrelated mappingCorrelated = annotatedElement.getAnnotation(MappingCorrelated.class);

            if (mappingCorrelated != null) {
                return mappingCorrelated;
            }

            MappingCorrelatedSimple mappingCorrelatedSimple = annotatedElement.getAnnotation(MappingCorrelatedSimple.class);

            if (mappingCorrelatedSimple != null) {
                return mappingCorrelatedSimple;
            }

            if (implicitMapping) {
                mapping = new MappingLiteral(attributeName);
            } else {
                return null;
            }
        }

        if (mapping.value().isEmpty()) {
            mapping = new MappingLiteral(attributeName, mapping);
        }

        return mapping;
    }

    public static Annotation getMapping(AnnotatedElement annotatedElement, Constructor<?> constructor, int index, MetamodelBootContext context) {
        if (annotatedElement.isAnnotationPresent(IdMapping.class)) {
            context.addError("The @IdMapping annotation is disallowed for entity view constructors for the " + ParameterAttributeMapping.getLocation(constructor, index));
            return null;
        }

        Mapping mapping = annotatedElement.getAnnotation(Mapping.class);

        if (mapping != null) {
            return mapping;
        }

        MappingParameter mappingParameter = annotatedElement.getAnnotation(MappingParameter.class);

        if (mappingParameter != null) {
            return mappingParameter;
        }

        MappingSubquery mappingSubquery = annotatedElement.getAnnotation(MappingSubquery.class);

        if (mappingSubquery != null) {
            return mappingSubquery;
        }

        MappingCorrelated mappingCorrelated = annotatedElement.getAnnotation(MappingCorrelated.class);

        if (mappingCorrelated != null) {
            return mappingCorrelated;
        }

        MappingCorrelatedSimple mappingCorrelatedSimple = annotatedElement.getAnnotation(MappingCorrelatedSimple.class);

        if (mappingCorrelatedSimple != null) {
            return mappingCorrelatedSimple;
        }

        Self self = annotatedElement.getAnnotation(Self.class);

        if (self != null) {
            return self;
        }

        context.addError("No entity view mapping annotation given for the " + ParameterAttributeMapping.getLocation(constructor, index));
        return null;
    }

    private AttributeInfo[] analyzeParameterToAccessorMapping(Class<?> entityViewClass, Constructor<?> canonicalConstructor, Map<String, AttributeInfo> fieldsToAccessors) {
        Map<String, AttributeInfo> setterToAccessors = new HashMap<>();
        for (AttributeInfo attributeInfo : fieldsToAccessors.values()) {
            if (attributeInfo.setter != null) {
                setterToAccessors.put(attributeInfo.setter.getName(), attributeInfo);
            }
        }

        Class<?>[] parameterTypes = canonicalConstructor.getParameterTypes();
        AttributeInfo[] parameterToAccessorMapping = new AttributeInfo[parameterTypes.length];
        try {
            ClassPool pool = new ClassPool(true);
            pool.appendClassPath(new LoaderClassPath(entityViewClass.getClassLoader()));
            CtClass ctClass = pool.get(entityViewClass.getName());
            CtClass[] constructorParams = new CtClass[parameterTypes.length];
            int parameterStackSlots = parameterTypes.length;
            for (int i = 0; i < parameterTypes.length; i++) {
                CtClass parameterTypeClass = pool.get(parameterTypes[i].getName());
                constructorParams[i] = parameterTypeClass;
                if (parameterTypeClass instanceof CtPrimitiveType && ((CtPrimitiveType) parameterTypeClass).getDataSize() == 2) {
                    parameterStackSlots++;
                }
            }

            CtConstructor constructor = ctClass.getDeclaredConstructor(constructorParams);

            ConstPool cp = constructor.getMethodInfo().getConstPool();
            CodeIterator ci = constructor.getMethodInfo().getCodeAttribute().iterator();
            AssignmentAnalyzer analyzer = new AssignmentAnalyzer();
            Frame[] frames = analyzer.analyze(ctClass, constructor.getMethodInfo2());
            while (ci.hasNext()) {
                int index = ci.next();
                int op = ci.byteAt(index);
                String methodName;
                int methodCpIdx;
                switch (op) {
                    //CHECKSTYLE:OFF: FallThrough
                    case Bytecode.PUTFIELD: {
                        Frame frame = frames[index];
                        Integer targetOrigin;
                        Integer sourceOrigin;
                        if (frame.peek() == Type.TOP) {
                            targetOrigin = frame.getStackOrigin(frame.getTopIndex() - 2);
                            sourceOrigin = frame.getStackOrigin(frame.getTopIndex() - 1);
                        } else {
                            targetOrigin = frame.getStackOrigin(frame.getTopIndex() - 1);
                            sourceOrigin = frame.getStackOrigin(frame.getTopIndex());
                        }
                        // The targetOrigin must be 0 i.e. the this pointer since we only care about PUTFIELD that happen to our object
                        // The source origin must have an index between 1 and N where N is the number of parameters. Higher indexes are local vars
                        if (targetOrigin != null && sourceOrigin != null && targetOrigin == 0 && sourceOrigin <= parameterStackSlots) {
                            int cpIndex = ci.u16bitAt(index + 1);
                            AttributeInfo attributeInfo = fieldsToAccessors.get(cp.getFieldrefName(cpIndex));
                            if (attributeInfo != null && attributeInfo.className.equals(cp.getFieldrefClassName(cpIndex))) {
                                parameterToAccessorMapping[stackSlotToParameterIndex(sourceOrigin, constructorParams)] = attributeInfo;
                            }
                        }
                        continue;
                    }
                    case Bytecode.INVOKEINTERFACE:
                    case Bytecode.INVOKEVIRTUAL:
                    case Bytecode.INVOKESPECIAL: {
                        methodCpIdx = ci.u16bitAt(index + 1);
                        methodName = cp.getMethodrefName(methodCpIdx);

                        Frame frame = frames[index];
                        if ("<init>".equals(methodName)) {
                            Class<?> superclass = entityViewClass.getSuperclass();
                            if (superclass != Object.class) {
                                CtClass[] superParameterTypes = Descriptor.getParameterTypes(cp.getMethodrefType(methodCpIdx), pool);
                                if (superParameterTypes.length != 0) {
                                    Class[] superParamTypes = new Class[superParameterTypes.length];
                                    for (int i = 0; i < superParameterTypes.length; i++) {
                                        if (superParameterTypes[i].isPrimitive()) {
                                            superParamTypes[i] = ReflectionUtils.getClass(superParameterTypes[i].getName());
                                        } else {
                                            superParamTypes[i] = entityViewClass.getClassLoader().loadClass(superParameterTypes[i].getName());
                                        }
                                    }
                                    Constructor<?> superConstructor = superclass.getDeclaredConstructor(superParamTypes);
                                    AttributeInfo[] superAttributes = analyzeParameterToAccessorMapping(superclass, superConstructor, fieldsToAccessors);
                                    // We assign the attributes from right to left and remap indexes from the super constructor to our parameter order
                                    int offset = frame.getTopIndex();
                                    for (int i = superAttributes.length - 1; i >= 0; i--) {
                                        AttributeInfo superAttribute = superAttributes[i];
                                        Integer origin;
                                        if (frame.getStack(offset) == Type.TOP) {
                                            offset--;
                                        }
                                        if (superAttribute != null && (origin = frame.getStackOrigin(offset)) != null) {
                                            parameterToAccessorMapping[origin - 1] = superAttribute;
                                        }
                                        offset--;
                                    }
                                }
                            }
                        } else {
                            Integer targetOrigin;
                            Integer sourceOrigin;
                            // Stack size must be at least 2 i.e. contain the this pointer and the argument for the setter
                            if (frame.peek() == Type.TOP) {
                                targetOrigin = frame.getTopIndex() > 1 ? frame.getStackOrigin(frame.getTopIndex() - 2) : null;
                                sourceOrigin = frame.getStackOrigin(frame.getTopIndex() - 1);
                            } else {
                                targetOrigin = frame.getTopIndex() > 0 ? frame.getStackOrigin(frame.getTopIndex() - 1) : null;
                                sourceOrigin = frame.getStackOrigin(frame.getTopIndex());
                            }
                            if (targetOrigin != null && sourceOrigin != null && targetOrigin == 0 && sourceOrigin <= parameterToAccessorMapping.length) {
                                AttributeInfo attributeInfo = setterToAccessors.get(methodName);
                                // We must check if this is the correct setter that is invoked
                                if (attributeInfo != null && cp.getMethodrefType(methodCpIdx).equals(Descriptor.ofMethod(CtClass.voidType, new CtClass[]{ pool.get(attributeInfo.field.getType().getName()) }))) {
                                    parameterToAccessorMapping[sourceOrigin - 1] = attributeInfo;
                                }
                            }
                        }
                        break;
                    }
                    default:
                        break;
                    //CHECKSTYLE:ON: FallThrough
                }
            }

        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            sw.append("Could not analyze the parameter to field mapping of the canonical constructor of '").append(entityViewClass.getName()).append("' with the parameter types ")
                    .append(Arrays.toString(parameterTypes)).append(" because of the following exception:\n");
            ex.printStackTrace(new PrintWriter(sw));
            context.addError(sw.toString());
        }
        return parameterToAccessorMapping;
    }

    private static int stackSlotToParameterIndex(int stackSlot, CtClass[] methodParameterTypes) {
        for (int i = 0; i < methodParameterTypes.length; i++) {
            if (stackSlot == 1) {
                return i;
            }
            CtClass parameterTypeClass = methodParameterTypes[i];
            if (parameterTypeClass instanceof CtPrimitiveType && ((CtPrimitiveType) parameterTypeClass).getDataSize() == 2) {
                stackSlot -= 2;
            } else {
                stackSlot--;
            }
        }

        return -1;
    }

    private Map<String, AttributeInfo> findAccessors(Class<?> entityViewClass) {
        Map<String, AttributeInfo> accessors = new HashMap<>();
        Class<?> currentClass = entityViewClass;
        do {
            // First we scan through all fields and register them
            for (Field declaredField : currentClass.getDeclaredFields()) {
                if (accessors.put(declaredField.getName(), new AttributeInfo(currentClass.getName(), declaredField.getName(), declaredField, Modifier.isFinal(declaredField.getModifiers()), ReflectionUtils.getResolvedFieldType(entityViewClass, declaredField))) != null) {
                    context.addError("The entity view class '" + entityViewClass.getName() + "' defines the field '" + declaredField.getName() + "' multiple times in the class hierarchy which is disallowed for non-abstract entity views!");
                    return null;
                }
            }
            // Then we scan through all methods and try to find matching fields
            for (Method declaredMethod : currentClass.getDeclaredMethods()) {
                if (!Modifier.isAbstract(declaredMethod.getModifiers())) {
                    Class<?>[] parameterTypes = declaredMethod.getParameterTypes();
                    String methodName = declaredMethod.getName();
                    String fieldName;
                    if (parameterTypes.length == 0) {
                        if (methodName.startsWith("get") && methodName.length() > 3 && Character.isUpperCase(methodName.charAt(3))) {
                            fieldName = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
                        } else if (methodName.startsWith("is") && methodName.length() > 2 && Character.isUpperCase(methodName.charAt(2)) && (boolean.class == declaredMethod.getReturnType() || Boolean.class == declaredMethod.getReturnType())) {
                            fieldName = Character.toLowerCase(methodName.charAt(2)) + methodName.substring(3);
                        } else {
                            fieldName = methodName;
                        }
                        AttributeInfo attributeInfo = accessors.get(fieldName);
                        if (attributeInfo == null || !declaredMethod.getReturnType().isAssignableFrom(attributeInfo.type)) {
                            continue;
                        }
                        attributeInfo.getter = declaredMethod;
                    } else if (parameterTypes.length == 1 && void.class == declaredMethod.getReturnType()) {
                        if (methodName.startsWith("set") && methodName.length() > 3 && Character.isUpperCase(methodName.charAt(3))) {
                            fieldName = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
                        } else {
                            fieldName = methodName;
                        }
                        AttributeInfo attributeInfo = accessors.get(fieldName);
                        if (attributeInfo == null || attributeInfo.isFinal || !parameterTypes[0].isAssignableFrom(attributeInfo.type)) {
                            continue;
                        }
                        attributeInfo.setter = declaredMethod;
                    }
                }
            }
            currentClass = currentClass.getSuperclass();
        } while (currentClass != Object.class);

        return accessors;
    }

    /**
     * @author Christian Beikov
     * @since 1.5.0
     */
    private static final class AttributeInfo {
        final String className;
        final String attributeName;
        final Field field;
        final boolean isFinal;
        final Class<?> type;
        Method getter;
        Method setter;

        public AttributeInfo(String className, String attributeName, Field field, boolean isFinal, Class<?> type) {
            this.className = className;
            this.attributeName = attributeName;
            this.field = field;
            this.isFinal = isFinal;
            this.type = type;
        }
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

    private void addEntityViewRootMapping(EntityViewRoot entityViewRoot, Map<String, EntityViewRootMapping> viewRootMappings, Class<?> entityViewClass, MetamodelBootContext context) {
        boolean errorOccurred = false;
        String viewRootName = entityViewRoot.name();
        if (viewRootName != null && viewRootName.isEmpty()) {
            errorOccurred = true;
            context.addError("Illegal empty name for the entity view root at the class '" + entityViewClass.getName() + "'!");
        } else if (viewRootMappings.containsKey(viewRootName)) {
            errorOccurred = true;
            context.addError("Illegal duplicate entity view root name '" + viewRootName + "' at the class '" + entityViewClass.getName() + "'!");
        }
        Class<?> entityClass = entityViewRoot.entity();
        String joinExpression = entityViewRoot.expression();
        Class<? extends CorrelationProvider> correlationProvider = entityViewRoot.correlator();
        String conditionExpression = entityViewRoot.condition();
        if (entityClass == void.class) {
            entityClass = null;
        }
        if (joinExpression != null && joinExpression.isEmpty()) {
            joinExpression = null;
        }
        if (correlationProvider == CorrelationProvider.class) {
            correlationProvider = null;
        }
        if (conditionExpression != null && conditionExpression.isEmpty()) {
            conditionExpression = null;
        }

        if (!errorOccurred) {
            viewRootMappings.put(viewRootName, new EntityViewRootMappingImpl(viewRootName, entityClass, joinExpression, correlationProvider, conditionExpression, entityViewRoot.joinType(), entityViewRoot.fetches(), Arrays.asList(entityViewRoot.order()), entityViewRoot.limit(), entityViewRoot.offset()));
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
