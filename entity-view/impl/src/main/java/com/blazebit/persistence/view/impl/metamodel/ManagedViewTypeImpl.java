/*
 * Copyright 2014 - 2019 Blazebit.
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

import com.blazebit.persistence.parser.util.JpaMetamodelUtils;
import com.blazebit.persistence.spi.ExtendedAttribute;
import com.blazebit.persistence.spi.ExtendedManagedType;
import com.blazebit.persistence.view.CTEProvider;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.LockMode;
import com.blazebit.persistence.view.ViewTransition;
import com.blazebit.persistence.view.impl.PrefixingQueryGenerator;
import com.blazebit.persistence.view.impl.SimpleCTEProviderFactory;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.MappingConstructor;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.spi.type.TypeConverter;
import com.blazebit.reflection.ReflectionUtils;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.bytecode.Bytecode;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.Descriptor;

import javax.persistence.EntityManager;
import javax.persistence.PrePersist;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.BasicType;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.SingularAttribute;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public abstract class ManagedViewTypeImpl<X> implements ManagedViewTypeImplementor<X> {

    private final Class<X> javaType;
    private final ManagedType<?> jpaManagedType;
    private final Method postCreateMethod;
    private final Method postConvertMethod;
    private final Method prePersistMethod;
    private final Method postPersistMethod;
    private final Method preUpdateMethod;
    private final Method postUpdateMethod;
    private final Method preRemoveMethod;
    private final Method postRemoveMethod;
    private final Method postRollbackMethod;
    private final Method postCommitMethod;
    private final Set<ViewTransition> postRollbackTransitions;
    private final Set<ViewTransition> postCommitTransitions;
    private final List<Method> specialMethods;
    private final boolean creatable;
    private final boolean updatable;
    private final boolean validatePersistability;
    private final LockMode lockMode;
    private final Set<String> excludedEntityAttributes;
    private final FlushMode flushMode;
    private final FlushStrategy flushStrategy;
    private final int defaultBatchSize;
    private final Map<String, AbstractMethodAttribute<? super X, ?>> attributes;
    private final NavigableMap<String, AbstractMethodAttribute<? super X, ?>> recursiveAttributes;
    private final NavigableMap<String, AbstractMethodAttribute<? super X, ?>> recursiveSubviewAttributes;
    private final Set<AbstractMethodAttribute<? super X, ?>> updateMappableAttributes;
    private final AbstractMethodAttribute<? super X, ?>[] mutableAttributes;
    private final Map<ParametersKey, MappingConstructorImpl<X>> constructors;
    private final Map<String, MappingConstructorImpl<X>> constructorIndex;
    private final String inheritanceMapping;
    private final InheritanceSubtypeConfiguration<X> defaultInheritanceSubtypeConfiguration;
    private final InheritanceSubtypeConfiguration<X> overallInheritanceSubtypeConfiguration;
    private final Map<Map<ManagedViewTypeImplementor<? extends X>, String>, InheritanceSubtypeConfiguration<X>> inheritanceSubtypeConfigurations;
    private final boolean hasJoinFetchedCollections;
    private final Set<CTEProvider> cteProviders = new LinkedHashSet<>();

    @SuppressWarnings("unchecked")
    public ManagedViewTypeImpl(ViewMapping viewMapping, ManagedType<?> managedType, final MetamodelBuildingContext context, EmbeddableOwner embeddableMapping) {
        context.addManagedViewType(viewMapping, embeddableMapping, this);
        this.javaType = (Class<X>) viewMapping.getEntityViewClass();
        this.jpaManagedType = managedType;
        this.postCreateMethod = viewMapping.getPostCreateMethod();
        this.postConvertMethod = viewMapping.getPostConvertMethod();
        this.prePersistMethod = viewMapping.getPrePersistMethod();
        this.postPersistMethod = viewMapping.getPostPersistMethod();
        this.preUpdateMethod = viewMapping.getPreUpdateMethod();
        this.postUpdateMethod = viewMapping.getPostUpdateMethod();
        this.preRemoveMethod = viewMapping.getPreRemoveMethod();
        this.postRemoveMethod = viewMapping.getPostRemoveMethod();
        this.postRollbackMethod = viewMapping.getPostRollbackMethod();
        this.postCommitMethod = viewMapping.getPostCommitMethod();
        EnumSet<ViewTransition> postRollbackTransitions = EnumSet.noneOf(ViewTransition.class);
        if (viewMapping.getPostRollbackTransitions() != null) {
            Collections.addAll(postRollbackTransitions, viewMapping.getPostRollbackTransitions());
        }
        this.postRollbackTransitions = Collections.unmodifiableSet(postRollbackTransitions);
        EnumSet<ViewTransition> postCommitTransitions = EnumSet.noneOf(ViewTransition.class);
        if (viewMapping.getPostCommitTransitions() != null) {
            Collections.addAll(postCommitTransitions, viewMapping.getPostCommitTransitions());
        }
        this.postCommitTransitions = Collections.unmodifiableSet(postCommitTransitions);
        this.specialMethods = viewMapping.getSpecialMethods();

        validateMethods(context);

        this.updatable = viewMapping.isUpdatable();
        this.flushMode = context.getFlushMode(javaType, viewMapping.getFlushMode());
        this.flushStrategy = context.getFlushStrategy(javaType, viewMapping.getFlushStrategy());
        this.lockMode = viewMapping.getResolvedLockMode();

        ExtendedManagedType<?> extendedManagedType = context.getEntityMetamodel().getManagedType(ExtendedManagedType.class, jpaManagedType);
        boolean embeddable = !(jpaManagedType instanceof EntityType<?>);

        if (viewMapping.isCreatable(context)) {
            this.creatable = true;
            this.validatePersistability = viewMapping.isValidatePersistability();
            if (validatePersistability) {
                this.excludedEntityAttributes = Collections.unmodifiableSet(new HashSet<>(viewMapping.getExcludedAttributes()));
            } else {
                this.excludedEntityAttributes = Collections.emptySet();
            }
        } else if (updatable && embeddable) {
            // If the entity view is for an embeddable, we also interpret it as creatable if it is marked as updatable
            this.creatable = true;
            this.validatePersistability = true;
            this.excludedEntityAttributes = Collections.emptySet();
        } else {
            this.creatable = false;
            this.validatePersistability = false;
            this.excludedEntityAttributes = Collections.emptySet();
        }

        if (!javaType.isInterface() && !Modifier.isAbstract(javaType.getModifiers())) {
            context.addError("Only interfaces or abstract classes are allowed as entity views. '" + javaType.getName() + "' is neither of those.");
        }

        Integer batchSize = viewMapping.getDefaultBatchSize();
        if (batchSize == null || batchSize == -1) {
            this.defaultBatchSize = -1;
        } else if (batchSize < 1) {
            context.addError("Illegal batch fetch size defined at '" + javaType.getName() + "'! Use a value greater than 0 or -1!");
            this.defaultBatchSize = Integer.MIN_VALUE;
        } else {
            this.defaultBatchSize = batchSize;
        }

        // We use a tree map to get a deterministic attribute order
        Map<String, AbstractMethodAttribute<? super X, ?>> attributes = new TreeMap<>();
        Set<AbstractMethodAttribute<? super X, ?>> updateMappableAttributes = new LinkedHashSet<>(attributes.size());
        List<AbstractMethodAttribute<? super X, ?>> mutableAttributes = new ArrayList<>(attributes.size());
        boolean hasJoinFetchedCollections = false;

        // Initialize attribute type and the dirty state index of attributes
        int index = viewMapping.getIdAttribute() == null ? 0 : 1;
        int dirtyStateIndex = 0;
        Set<String> requiredUpdatableAttributes;
        Set<String> mappedColumns;
        if (creatable && validatePersistability) {
            requiredUpdatableAttributes = new HashSet<>();
            mappedColumns = new HashSet<>();
            OUTER: for (Map.Entry<String, ? extends ExtendedAttribute<?, ?>> entry : extendedManagedType.getOwnedSingularAttributes().entrySet()) {
                ExtendedAttribute<?, ?> extendedAttribute = entry.getValue();
                SingularAttribute<?, ?> attribute = (SingularAttribute<?, ?>) extendedAttribute.getAttribute();
                if (!attribute.isVersion() && !attribute.isOptional() && !extendedAttribute.getElementClass().isPrimitive()) {
                    // The attribute could be the id attribute of an owned *ToOne association
                    if ((attribute.getType() instanceof BasicType<?> || attribute.getType() instanceof EmbeddableType<?>) && extendedAttribute.getAttributePath().size() > 1) {
                        List<Attribute<?, ?>> attributePath = extendedAttribute.getAttributePath();
                        // So we check the *ToOne attribute instead
                        for (int i = attributePath.size() - 2; i >= 0; i--) {
                            SingularAttribute<?, ?> superAttribute = (SingularAttribute<?, ?>) attributePath.get(i);
                            if (superAttribute.getType() instanceof EntityType<?>) {
                                // If it is optional, the attribute isn't optional
                                // Otherwise break to add it to the required set
                                if (superAttribute.isOptional()) {
                                    continue OUTER;
                                } else {
                                    break;
                                }
                            }
                        }
                    }
                    requiredUpdatableAttributes.add(entry.getKey());
                }
            }
        } else {
            requiredUpdatableAttributes = Collections.emptySet();
            mappedColumns = Collections.emptySet();
        }

        for (String excludedEntityAttribute : excludedEntityAttributes) {
            removeRequiredUpdatableAttribute(requiredUpdatableAttributes, mappedColumns, extendedManagedType, excludedEntityAttribute);
        }

        for (MethodAttributeMapping mapping : viewMapping.getMethodAttributes().values()) {
            AbstractMethodAttribute<? super X, ?> attribute;
            if (mapping.isId() || mapping.isVersion()) {
                // The id and the version always have -1 as dirty state index because they can't be dirty in the traditional sense
                // Id can only be set on "new" objects and shouldn't be mutable, version acts as optimistic concurrency version
                if (mapping.isId()) {
                    attribute = mapping.getMethodAttribute(this, 0, -1, context, embeddableMapping);
                    index--;
                } else {
                    attribute = mapping.getMethodAttribute(this, index, -1, context, embeddableMapping);
                }
                if (!requiredUpdatableAttributes.isEmpty()) {
                    removeRequiredUpdatableAttribute(requiredUpdatableAttributes, mappedColumns, extendedManagedType, attribute);
                }
            } else {
                // Note that the dirty state index is only a "suggested" index, but the implementation can choose not to use it
                attribute = mapping.getMethodAttribute(this, index, dirtyStateIndex, context, embeddableMapping);
                if (attribute.getDirtyStateIndex() != -1) {
                    mutableAttributes.add(attribute);
                    dirtyStateIndex++;
                }
            }

            if (!requiredUpdatableAttributes.isEmpty() && attribute.isUpdatable() && attribute.getUpdateMappableAttribute() != null) {
                removeRequiredUpdatableAttribute(requiredUpdatableAttributes, mappedColumns, extendedManagedType, attribute);
            }

            hasJoinFetchedCollections = hasJoinFetchedCollections || attribute.hasJoinFetchedCollections();
            attributes.put(mapping.getName(), attribute);
            index++;
        }

        this.attributes = Collections.unmodifiableMap(attributes);

        for (AbstractMethodAttribute<? super X, ?> attribute : attributes.values()) {
            if (attribute.isUpdatable() || attribute.isUpdateMappable()) {
                updateMappableAttributes.add(attribute);
            }
        }

        this.mutableAttributes = mutableAttributes.toArray(new AbstractMethodAttribute[mutableAttributes.size()]);
        this.updateMappableAttributes = Collections.unmodifiableSet(updateMappableAttributes);
        Map<Map<ManagedViewTypeImplementor<? extends X>, String>, InheritanceSubtypeConfiguration<X>> inheritanceSubtypeConfigurations = new HashMap<>();
        Map<ViewMapping, String> overallInheritanceSubtypeMappings = new HashMap<>();

        for (InheritanceViewMapping inheritanceViewMapping : viewMapping.getInheritanceViewMappings()) {
            overallInheritanceSubtypeMappings.putAll(inheritanceViewMapping.getInheritanceSubtypeMappings());
        }

        this.overallInheritanceSubtypeConfiguration = new InheritanceSubtypeConfiguration<>(this, viewMapping, -1, new InheritanceViewMapping(overallInheritanceSubtypeMappings), context, embeddableMapping);
        this.defaultInheritanceSubtypeConfiguration = new InheritanceSubtypeConfiguration<>(this, viewMapping, 0, viewMapping.getDefaultInheritanceViewMapping(), context, embeddableMapping, overallInheritanceSubtypeConfiguration);

        inheritanceSubtypeConfigurations.put(defaultInheritanceSubtypeConfiguration.inheritanceSubtypeConfiguration, defaultInheritanceSubtypeConfiguration);
        int configurationIndex = 1;
        for (InheritanceViewMapping inheritanceViewMapping : viewMapping.getInheritanceViewMappings()) {
            // Skip the default as it is handled a few lines before
            if (inheritanceViewMapping != viewMapping.getDefaultInheritanceViewMapping()) {
                InheritanceSubtypeConfiguration<X> subtypeConfiguration = new InheritanceSubtypeConfiguration<>(this, viewMapping, configurationIndex, inheritanceViewMapping, context, embeddableMapping, overallInheritanceSubtypeConfiguration);
                inheritanceSubtypeConfigurations.put(subtypeConfiguration.inheritanceSubtypeConfiguration, subtypeConfiguration);
                configurationIndex++;
            }
        }

        this.inheritanceSubtypeConfigurations = Collections.unmodifiableMap(inheritanceSubtypeConfigurations);

        final NavigableMap<String, AbstractMethodAttribute<? super X, ?>> recursiveAttributes = new TreeMap<>();
        final NavigableMap<String, AbstractMethodAttribute<? super X, ?>> recursiveSubviewAttributes = new TreeMap<>();
        for (Map.Entry<AttributeKey, ConstrainedAttribute<AbstractMethodAttribute<? super X, ?>>> entry : defaultInheritanceSubtypeConfiguration.getAttributesClosure().entrySet()) {
            final AbstractMethodAttribute<? super X, ?> attribute = entry.getValue().getAttribute();
            if (attribute.getKeyType() instanceof ManagedViewTypeImplementor<?>) {
                final ManagedViewTypeImplementor<Object> keyType = (ManagedViewTypeImplementor<Object>) attribute.getKeyType();
                // TODO: introduce a special/synthetic singular key attribute that has a different mapping and type?
                // recursiveSubviewAttributes.put("KEY(" + attribute.getName() + ")", attribute.key());
                context.onViewTypeFinished(keyType, new KeyTypeSubviewAttributeCollector<>(attribute, recursiveAttributes, recursiveSubviewAttributes, keyType, context));
            }
            if (attribute.getElementType() instanceof ManagedViewTypeImplementor<?>) {
                final ManagedViewTypeImplementor<Object> elementType = (ManagedViewTypeImplementor<Object>) attribute.getElementType();
                recursiveSubviewAttributes.put(attribute.getName(), attribute);
                context.onViewTypeFinished(elementType, new ElementTypeSubviewAttributeCollector<>(attribute, recursiveAttributes, recursiveSubviewAttributes, elementType, context));
            } else {
                recursiveAttributes.put(attribute.getName(), attribute);
            }
        }
        this.recursiveAttributes = recursiveAttributes;
        this.recursiveSubviewAttributes = recursiveSubviewAttributes;

        Map<ParametersKey, MappingConstructorImpl<X>> constructors = new HashMap<>();
        Map<String, MappingConstructorImpl<X>> constructorIndex = new TreeMap<>();

        for (Map.Entry<ParametersKey, ConstructorMapping> entry : viewMapping.getConstructorMappings().entrySet()) {
            ConstructorMapping constructor = entry.getValue();
            String constructorName = constructor.getName();
            // We do this just to get to the next step with the validation
            if (constructorIndex.containsKey(constructorName)) {
                constructorName += constructorIndex.size();
            }
            MappingConstructorImpl<X> mappingConstructor = new MappingConstructorImpl<X>(this, constructorName, constructor, context, embeddableMapping);
            constructors.put(entry.getKey(), mappingConstructor);
            constructorIndex.put(constructorName, mappingConstructor);
        }

        this.constructors = Collections.unmodifiableMap(constructors);
        this.constructorIndex = Collections.unmodifiableMap(constructorIndex);
        this.inheritanceMapping = viewMapping.determineInheritanceMapping(context);
        this.hasJoinFetchedCollections = hasJoinFetchedCollections;

        if (viewMapping.getInheritanceSupertypes().isEmpty()) {
            if (inheritanceMapping != null) {
                context.addError("Entity view type '" + javaType.getName() + "' has a @EntityViewInheritanceMapping but is never used as subtype which is not allowed!");
            }
        } else {
            if (inheritanceMapping == null || inheritanceMapping.isEmpty()) {
                List<Class<?>> classes = new ArrayList<>();
                for (ViewMapping mapping : viewMapping.getInheritanceSupertypes()) {
                    classes.add(mapping.getEntityViewClass());
                }
                context.addError("Entity view type '" + javaType.getName() + "' has no @EntityViewInheritanceMapping but is used as inheritance subtype in: " + classes);
            }
        }

        if (!requiredUpdatableAttributes.isEmpty()) {
            // If we get here, we start a bytecode analysis for attributes set in the default constructors
            removeIfSetByDefault(extendedManagedType, requiredUpdatableAttributes);

            // Before failing, remove all attribute for which we covered all columns already
            for (Iterator<String> iterator = requiredUpdatableAttributes.iterator(); iterator.hasNext(); ) {
                ExtendedAttribute<?, ?> extendedAttribute = extendedManagedType.getAttributes().get(iterator.next());
                if (extendedAttribute == null || mappedColumns.containsAll(Arrays.asList(extendedAttribute.getColumnNames()))) {
                    iterator.remove();
                }
            }
            if (!requiredUpdatableAttributes.isEmpty()) {
                // A version attribute defined on a mapped super class isn't reported as version attribute apparently
                if (jpaManagedType instanceof IdentifiableType<?>) {
                    if (((IdentifiableType<Object>) jpaManagedType).hasVersionAttribute()) {
                        for (Iterator<String> iterator = requiredUpdatableAttributes.iterator(); iterator.hasNext(); ) {
                            ExtendedAttribute<?, ?> extendedAttribute = extendedManagedType.getAttributes().get(iterator.next());
                            try {
                                SingularAttribute<? super Object, ?> version = ((IdentifiableType<Object>) jpaManagedType).getVersion(extendedAttribute.getElementClass());
                                if (extendedAttribute.getAttributePathString().equals(version.getName())) {
                                    iterator.remove();
                                }
                            } catch (IllegalArgumentException ex) {
                                // Ignore
                            }
                        }
                    }
                }
                if (!requiredUpdatableAttributes.isEmpty()) {
                    context.addError("Entity view type '" + javaType.getName() + "' might not be persistable because it is missing updatable attribute definitions for non-optional entity attributes: " + requiredUpdatableAttributes + ". Add attributes, disable validation or exclude attributes if you know values are set via the entity constructor!");
                }
            }
        }

        context.onViewTypeFinished(this, new CTEProviderCollector(this, context, viewMapping));
    }

    private void removeIfSetByDefault(ExtendedManagedType<?> extendedManagedType, Set<String> requiredUpdatableAttributes) {
        try {
            Map<String, String> fieldNameToAttribute = new HashMap<>(requiredUpdatableAttributes.size());
            Map<String, String> setterNameToAttribute = new HashMap<>(requiredUpdatableAttributes.size());
            Map<String, String> fields = new HashMap<>(requiredUpdatableAttributes.size());
            Map<String, String> setters = new HashMap<>(requiredUpdatableAttributes.size());
            Map<String, String> getters = new HashMap<>(requiredUpdatableAttributes.size());
            Class<?> javaType = jpaManagedType.getJavaType();
            ClassPool pool = new ClassPool(true);
            CtClass ctClass = pool.get(javaType.getName());

            for (String attribute : requiredUpdatableAttributes) {
                ExtendedAttribute<?, ?> extendedAttribute = extendedManagedType.getAttribute(attribute);
                Attribute<?, ?> attr = extendedAttribute.getAttributePath().get(0);
                Class<?> type = JpaMetamodelUtils.resolveFieldClass(jpaManagedType.getJavaType(), attr);
                Member javaMember = attr.getJavaMember();
                if (javaMember instanceof Method) {
                    Method getter = null;
                    String suffix = null;
                    if (javaMember.getName().startsWith("get")) {
                        getter = ReflectionUtils.getMethod(javaType, javaMember.getName());
                        suffix = javaMember.getName().substring(3);
                    } else if (javaMember.getName().startsWith("is")) {
                        getter = ReflectionUtils.getMethod(javaType, javaMember.getName());
                        suffix = javaMember.getName().substring(2);
                    } else if (javaMember.getName().startsWith("set")) {
                        suffix = javaMember.getName().substring(3);
                        getter = ReflectionUtils.getMethod(javaType, "get" + suffix);
                        if (getter == null && ((Method) javaMember).getParameterTypes().length == 1 && ((Method) javaMember).getParameterTypes()[0] == boolean.class) {
                            getter = ReflectionUtils.getMethod(javaType, "is" + suffix);
                        }
                    }

                    Method setter = suffix != null ? ReflectionUtils.getMethod(javaType, "set" + suffix, type) : null;
                    String fieldName;
                    if (getter != null && (fieldName = getSimpleGetterFieldName(ctClass.getDeclaredMethod(getter.getName()))) != null) {
                        if (setter == null) {
                            getters.put(attribute, getter.getName());
                            fields.put(attribute, fieldName);
                            fieldNameToAttribute.put(fieldName, attribute);
                        } else if (fieldName.equals(getSimpleSetterFieldName(ctClass.getDeclaredMethod(setter.getName())))) {
                            getters.put(attribute, getter.getName());
                            setters.put(attribute, setter.getName());
                            setterNameToAttribute.put(attribute, setter.getName());
                            fields.put(attribute, fieldName);
                            fieldNameToAttribute.put(fieldName, attribute);
                        }
                    } else if (setter != null && (fieldName = getSimpleSetterFieldName(ctClass.getDeclaredMethod(setter.getName()))) != null) {
                        setters.put(attribute, setter.getName());
                        setterNameToAttribute.put(attribute, setter.getName());
                        fields.put(attribute, fieldName);
                        fieldNameToAttribute.put(fieldName, attribute);
                    }
                } else {
                    fields.put(attribute, javaMember.getName());
                    fieldNameToAttribute.put(javaMember.getName(), attribute);
                    String suffix = Character.toUpperCase(javaMember.getName().charAt(0)) + javaMember.getName().substring(1);
                    Method getter = ReflectionUtils.getMethod(javaType, "get" + suffix);
                    if (getter == null && ((Field) javaMember).getType() == boolean.class) {
                        getter = ReflectionUtils.getMethod(javaType, "is" + suffix);
                    }
                    Method setter = ReflectionUtils.getMethod(javaType, "set" + suffix, type);

                    if (getter != null && javaMember.getName().equals(getSimpleGetterFieldName(ctClass.getDeclaredMethod(getter.getName())))) {
                        if (setter == null) {
                            getters.put(attribute, getter.getName());
                        } else if (javaMember.getName().equals(getSimpleSetterFieldName(ctClass.getDeclaredMethod(setter.getName())))) {
                            getters.put(attribute, getter.getName());
                            setters.put(attribute, setter.getName());
                            setterNameToAttribute.put(attribute, setter.getName());
                        }
                    }
                    if (setter != null && javaMember.getName().equals(getSimpleSetterFieldName(ctClass.getDeclaredMethod(setter.getName())))) {
                        setters.put(attribute, setter.getName());
                        setterNameToAttribute.put(attribute, setter.getName());
                    }
                }
            }

            CtClass c = ctClass;
            CtClass[] constructorParams = new CtClass[0];
            List<CtClass> superClasses = new ArrayList<>(2);
            superClasses.add(c);
            while (c.getSuperclass() != null) {
                superClasses.add(c = c.getSuperclass());
            }
            c = ctClass;
            do {
                CtConstructor entityConstructor = c.getDeclaredConstructor(constructorParams);
                while (!entityConstructor.callsSuper() && entityConstructor.getDeclaringClass().getSuperclass() != null) {
                    entityConstructor = c.getDeclaredConstructor(findCalledConstructor(entityConstructor));
                }
                constructorParams = removeAssignedAttributes(superClasses, entityConstructor, fieldNameToAttribute, setterNameToAttribute, requiredUpdatableAttributes);
                if (!requiredUpdatableAttributes.isEmpty()) {
                    for (CtMethod declaredMethod : c.getDeclaredMethods()) {
                        if (declaredMethod.hasAnnotation(PrePersist.class)) {
                            removeAssignedAttributes(superClasses, declaredMethod, fieldNameToAttribute, setterNameToAttribute, requiredUpdatableAttributes);
                        }
                    }
                }
            } while (!requiredUpdatableAttributes.isEmpty() && (c = c.getSuperclass()) != null);
        } catch (Exception ex) {
            Logger.getLogger(ManagedViewTypeImpl.class.getName()).log(Level.WARNING, "Bytecode analysis failed. Please report this issue!", ex);
        }
    }

    private static String getSimpleGetterFieldName(CtMethod method) throws Exception {
        String fieldName = null;
        CodeIterator ci = method.getMethodInfo().getCodeAttribute().iterator();
        while (ci.hasNext()) {
            int index = ci.next();
            int op = ci.byteAt(index);
            switch (op) {
                //CHECKSTYLE:OFF: FallThrough
                case Bytecode.GETFIELD:
                    ConstPool cp = method.getMethodInfo().getConstPool();
                    int cpIndex = ci.u16bitAt(index + 1);
                    if (cp.getFieldrefClass(ci.u16bitAt(index + 1)) != cp.getThisClassInfo()) {
                        break;
                    }
                    fieldName = cp.getFieldrefName(cpIndex);
                case Bytecode.ALOAD:
                case Bytecode.ALOAD_0:
                case Bytecode.CHECKCAST:
                case Bytecode.RET:
                case Bytecode.RETURN:
                case Bytecode.ARETURN:
                case Bytecode.DRETURN:
                case Bytecode.FRETURN:
                case Bytecode.IRETURN:
                case Bytecode.LRETURN:
                    continue;
                default:
                    break;
                //CHECKSTYLE:ON: FallThrough
            }
            return null;
        }

        return fieldName;
    }

    private static String getSimpleSetterFieldName(CtMethod method) throws Exception {
        String fieldName = null;
        CodeIterator ci = method.getMethodInfo().getCodeAttribute().iterator();
        while (ci.hasNext()) {
            int index = ci.next();
            int op = ci.byteAt(index);
            switch (op) {
                //CHECKSTYLE:OFF: FallThrough
                case Bytecode.PUTFIELD:
                    ConstPool cp = method.getMethodInfo().getConstPool();
                    int cpIndex = ci.u16bitAt(index + 1);
                    if (cp.getFieldrefClass(cpIndex) != cp.getThisClassInfo()) {
                        break;
                    }
                    fieldName = cp.getFieldrefName(cpIndex);
                case Bytecode.ALOAD:
                case Bytecode.ALOAD_0:
                case Bytecode.ALOAD_1:
                case Bytecode.DLOAD_1:
                case Bytecode.FLOAD_1:
                case Bytecode.ILOAD_1:
                case Bytecode.LLOAD_1:
                case Bytecode.CHECKCAST:
                case Bytecode.RET:
                case Bytecode.RETURN:
                    continue;
                default:
                    break;
                //CHECKSTYLE:ON: FallThrough
            }
            return null;
        }

        return fieldName;
    }

    private static CtClass[] removeAssignedAttributes(List<CtClass> superClasses, CtBehavior method, Map<String, String> fieldNameToAttribute, Map<String, String> setterNameToAttribute, Set<String> requiredUpdatableAttributes) throws Exception {
        CtClass[] params = new CtClass[0];
        ConstPool cp = method.getMethodInfo().getConstPool();
        CodeIterator ci = method.getMethodInfo().getCodeAttribute().iterator();
        while (ci.hasNext()) {
            int index = ci.next();
            int op = ci.byteAt(index);
            String methodName;
            String methodClassName;
            boolean isInterfaceMethod;
            int methodCpIdx;
            switch (op) {
                //CHECKSTYLE:OFF: FallThrough
                case Bytecode.PUTFIELD:
                    int cpIndex = ci.u16bitAt(index + 1);
                    if (cp.getFieldrefClass(cpIndex) != cp.getThisClassInfo()) {
                        String fieldrefClassName = cp.getFieldrefClassName(cpIndex);
                        boolean fromSuper = false;
                        for (CtClass ctClass : superClasses) {
                            if (fieldrefClassName.equals(ctClass.getName())) {
                                fromSuper = true;
                                break;
                            }
                        }

                        if (!fromSuper) {
                            continue;
                        }
                    }
                    String attribute = fieldNameToAttribute.get(cp.getFieldrefName(cpIndex));
                    if (attribute != null) {
                        requiredUpdatableAttributes.remove(attribute);
                        if (requiredUpdatableAttributes.isEmpty()) {
                            return null;
                        }
                    }
                    continue;
                case Bytecode.INVOKEINTERFACE:
                    methodCpIdx = ci.u16bitAt(index + 1);
                    methodClassName = cp.getInterfaceMethodrefClassName(methodCpIdx);
                    methodName = cp.getInterfaceMethodrefName(methodCpIdx);
                    isInterfaceMethod = true;
                    break;
                case Bytecode.INVOKEVIRTUAL:
                    methodCpIdx = ci.u16bitAt(index + 1);
                    methodClassName = cp.getMethodrefClassName(methodCpIdx);
                    methodName = cp.getMethodrefName(methodCpIdx);
                    isInterfaceMethod = false;
                    break;
                case Bytecode.INVOKESPECIAL:
                    methodCpIdx = ci.u16bitAt(index + 1);
                    methodClassName = cp.getMethodrefClassName(methodCpIdx);
                    methodName = cp.getMethodrefName(methodCpIdx);
                    isInterfaceMethod = false;
                    if ("<init>".equals(methodName)) {
                        if (method.getDeclaringClass().getSuperclass().getName().equals(methodClassName)) {
                            params = Descriptor.getParameterTypes(cp.getMethodrefType(methodCpIdx), method.getDeclaringClass().getClassPool());
                        }
                        continue;
                    }
                    break;
                default:
                    continue;
                //CHECKSTYLE:ON: FallThrough
            }

            String attributeName = setterNameToAttribute.get(methodName);
            if (attributeName != null) {
                boolean fromSuper = false;
                for (CtClass ctClass : superClasses) {
                    if (methodClassName.equals(ctClass.getName())) {
                        fromSuper = true;
                        break;
                    }
                }

                if (fromSuper) {
                    requiredUpdatableAttributes.remove(attributeName);
                    if (requiredUpdatableAttributes.isEmpty()) {
                        return null;
                    }
                } else if (isInterfaceMethod) {
                    List<CtClass> interfaces = new ArrayList<>(superClasses);
                    while (!interfaces.isEmpty()) {
                        CtClass ctClass = interfaces.remove(interfaces.size() - 1);
                        if (methodClassName.equals(ctClass.getName())) {
                            fromSuper = true;
                            break;
                        } else {
                            for (CtClass ctClassInterface : ctClass.getInterfaces()) {
                                interfaces.add(ctClassInterface);
                            }
                        }
                    }

                    if (fromSuper) {
                        requiredUpdatableAttributes.remove(attributeName);
                        if (requiredUpdatableAttributes.isEmpty()) {
                            return null;
                        }
                    }
                }
            }
        }
        return params;
    }

    private static CtClass[] findCalledConstructor(CtBehavior method) throws Exception {
        ConstPool cp = method.getMethodInfo().getConstPool();
        CodeIterator ci = method.getMethodInfo().getCodeAttribute().iterator();
        while (ci.hasNext()) {
            int index = ci.next();
            if (ci.byteAt(index) == Bytecode.INVOKESPECIAL) {
                int methodCpIdx = ci.u16bitAt(index + 1);
                if (cp.getMethodrefClass(methodCpIdx) == cp.getThisClassInfo() && "<init>".equals(cp.getMethodrefName(methodCpIdx))) {
                    return Descriptor.getParameterTypes(cp.getMethodrefType(methodCpIdx), method.getDeclaringClass().getClassPool());
                }
            }
        }
        return new CtClass[0];
    }

    private static void removeRequiredUpdatableAttribute(Set<String> requiredUpdatableAttributes, Set<String> mappedColumns, ExtendedManagedType<?> extendedManagedType, AbstractMethodAttribute<?, ?> attribute) {
        removeRequiredUpdatableAttribute(requiredUpdatableAttributes, mappedColumns, extendedManagedType, attribute.getMapping());
    }

    private static void removeRequiredUpdatableAttribute(Set<String> requiredUpdatableAttributes, Set<String> mappedColumns, ExtendedManagedType<?> extendedManagedType, String mapping) {
        requiredUpdatableAttributes.remove(mapping);
        ExtendedAttribute<?, ?> extendedAttribute = extendedManagedType.getAttributes().get(mapping);
        if (!requiredUpdatableAttributes.isEmpty() && extendedAttribute != null) {
            mappedColumns.addAll(Arrays.asList(extendedAttribute.getColumnNames()));
            for (ExtendedAttribute<?, ?> columnEquivalentAttribute : extendedAttribute.getColumnEquivalentAttributes()) {
                requiredUpdatableAttributes.remove(columnEquivalentAttribute.getAttributePathString());
                if (requiredUpdatableAttributes.isEmpty()) {
                    return;
                }
            }
            if (extendedAttribute.getAttribute() instanceof SingularAttribute<?, ?>) {
                SingularAttribute<?, ?> singularAttribute = (SingularAttribute<?, ?>) extendedAttribute.getAttribute();
                if (singularAttribute.getType() instanceof EmbeddableType<?>) {
                    for (String embeddedPropertyName : JpaMetamodelUtils.getEmbeddedPropertyNames((EmbeddableType<?>) singularAttribute.getType())) {
                        removeRequiredUpdatableAttribute(requiredUpdatableAttributes, mappedColumns, extendedManagedType, mapping + "." + embeddedPropertyName);
                    }
                }
            }
        }
    }

    private void validateMethods(final MetamodelBuildingContext context) {
        Set<Class<?>> superTypes = null;
        List<Class<?>> allowedParameterTypes = null;
        List<Class<?>> managerTypes = Arrays.asList(EntityViewManager.class, EntityManager.class);
        if (postCreateMethod != null) {
            Class<?>[] parameterTypes = postCreateMethod.getParameterTypes();
            if (!void.class.equals(postCreateMethod.getReturnType()) || parameterTypes.length > 1 || parameterTypes.length == 1 && !EntityViewManager.class.equals(parameterTypes[0])) {
                context.addError("Invalid signature for post create method at '" + javaType.getName() + "." + postCreateMethod.getName() + "'! A method annotated with @PostCreate must return void and accept no or a single EntityViewManager argument!");
            }
        }
        if (postConvertMethod != null) {
            Class<?>[] parameterTypes = postConvertMethod.getParameterTypes();
            if (!void.class.equals(postConvertMethod.getReturnType()) || parameterTypes.length > 2 || !Arrays.asList(EntityViewManager.class, Object.class).containsAll(Arrays.asList(parameterTypes))) {
                context.addError("Invalid signature for post convert method at '" + javaType.getName() + "." + postConvertMethod.getName() + "'! A method annotated with @PostConvert must return void and accept at most 2 arguments, an EntityViewManager and the source entity view argument of type Object!");
            }
        }
        if (prePersistMethod != null) {
            superTypes = jpaManagedSuperTypes(superTypes);
            allowedParameterTypes = allowedParameterTypes(allowedParameterTypes, superTypes);
            Class<?>[] parameterTypes = prePersistMethod.getParameterTypes();
            if (!void.class.equals(prePersistMethod.getReturnType()) || parameterTypes.length > 3 || !allowedParameterTypes.containsAll(Arrays.asList(parameterTypes))) {
                context.addError("Invalid signature for pre persist method at '" + javaType.getName() + "." + prePersistMethod.getName() + "'! A method annotated with @PrePersist must return void and accept at most 3 arguments, an EntityViewManager, an EntityManager and one of the compatible entity types: " + superTypes);
            }
        }
        if (postPersistMethod != null) {
            superTypes = jpaManagedSuperTypes(superTypes);
            allowedParameterTypes = allowedParameterTypes(allowedParameterTypes, superTypes);
            Class<?>[] parameterTypes = postPersistMethod.getParameterTypes();
            if (!void.class.equals(postPersistMethod.getReturnType()) || parameterTypes.length > 3 || !allowedParameterTypes.containsAll(Arrays.asList(parameterTypes))) {
                context.addError("Invalid signature for post persist method at '" + javaType.getName() + "." + postPersistMethod.getName() + "'! A method annotated with @PostPersist must return void and accept at most 3 arguments, an EntityViewManager, an EntityManager and one of the compatible entity types: " + superTypes);
            }
        }
        if (preUpdateMethod != null) {
            Class<?>[] parameterTypes = preUpdateMethod.getParameterTypes();
            if (!void.class.equals(preUpdateMethod.getReturnType()) || parameterTypes.length > 2 || !managerTypes.containsAll(Arrays.asList(parameterTypes))) {
                context.addError("Invalid signature for pre update method at '" + javaType.getName() + "." + preUpdateMethod.getName() + "'! A method annotated with @PreUpdate must return void and accept at most 2 arguments, an EntityViewManager and an EntityManager!");
            }
        }
        if (postUpdateMethod != null) {
            Class<?>[] parameterTypes = postUpdateMethod.getParameterTypes();
            if (!void.class.equals(postUpdateMethod.getReturnType()) || parameterTypes.length > 2 || !managerTypes.containsAll(Arrays.asList(parameterTypes))) {
                context.addError("Invalid signature for post update method at '" + javaType.getName() + "." + postUpdateMethod.getName() + "'! A method annotated with @PostUpdate must return void and accept at most 2 arguments, an EntityViewManager and an EntityManager!");
            }
        }
        if (preRemoveMethod != null) {
            Class<?>[] parameterTypes = preRemoveMethod.getParameterTypes();
            if (!void.class.equals(preRemoveMethod.getReturnType()) && !boolean.class.equals(preRemoveMethod.getReturnType()) || parameterTypes.length > 2 || !managerTypes.containsAll(Arrays.asList(parameterTypes))) {
                context.addError("Invalid signature for pre remove method at '" + javaType.getName() + "." + preRemoveMethod.getName() + "'! A method annotated with @PreRemove must return void or boolean and accept at most 2 arguments, an EntityViewManager and an EntityManager!");
            }
        }
        if (postRemoveMethod != null) {
            Class<?>[] parameterTypes = postRemoveMethod.getParameterTypes();
            if (!void.class.equals(postRemoveMethod.getReturnType()) || parameterTypes.length > 2 || !managerTypes.containsAll(Arrays.asList(parameterTypes))) {
                context.addError("Invalid signature for post remove method at '" + javaType.getName() + "." + postRemoveMethod.getName() + "'! A method annotated with @PostRemove must return void and accept at most 2 arguments, an EntityViewManager and an EntityManager!");
            }
        }
        if (postCommitMethod != null) {
            Class<?>[] parameterTypes = postCommitMethod.getParameterTypes();
            if (!void.class.equals(postCommitMethod.getReturnType()) || parameterTypes.length > 2 || !managerTypes.containsAll(Arrays.asList(parameterTypes))) {
                context.addError("Invalid signature for post commit method at '" + javaType.getName() + "." + postCommitMethod.getName() + "'! A method annotated with @PostCommit must return void and accept at most 2 arguments, an EntityViewManager and an EntityManager!");
            }
        }
        if (postRollbackMethod != null) {
            Class<?>[] parameterTypes = postRollbackMethod.getParameterTypes();
            if (!void.class.equals(postRollbackMethod.getReturnType()) || parameterTypes.length > 2 || !managerTypes.containsAll(Arrays.asList(parameterTypes))) {
                context.addError("Invalid signature for post rollback method at '" + javaType.getName() + "." + postRollbackMethod.getName() + "'! A method annotated with @PostRollback must return void and accept at most 2 arguments, an EntityViewManager and an EntityManager!");
            }
        }
    }

    private Set<Class<?>> jpaManagedSuperTypes(Set<Class<?>> superTypes) {
        if (superTypes == null) {
            superTypes = ReflectionUtils.getSuperTypes(jpaManagedType.getJavaType());
        }
        return superTypes;
    }

    private List<Class<?>> allowedParameterTypes(List<Class<?>> allowedParameterTypes, Set<Class<?>> superTypes) {
        if (allowedParameterTypes == null) {
            allowedParameterTypes = new ArrayList<>(superTypes.size() + 2);
            allowedParameterTypes.add(EntityViewManager.class);
            allowedParameterTypes.add(EntityManager.class);
            allowedParameterTypes.addAll(superTypes);
        }
        return allowedParameterTypes;
    }

    @Override
    public void checkAttributes(MetamodelBuildingContext context) {
        // Ensure that a plural entity attribute is not used multiple times in different plural entity view attributes
        // If it were used multiple times, the second collection would not receive all expected elements, because both are based on the same join
        // and the first collection will already cause a "fold" of the results for materializing the collection in the entity view
        // We could theoretically try to defer the "fold" action, but the current model makes this pretty hard. The obvious workaround is to map a plural subview attribute
        // and put all mappings into that. This will guarantee that the "fold" action only happens after all properties have been processed
        Map<String, List<String>> collectionMappings = new HashMap<>();
        Map<String, List<String>> collectionMappingSingulars = new HashMap<>();

        for (AbstractMethodAttribute<? super X, ?> attribute : attributes.values()) {
            attribute.checkAttribute(jpaManagedType, context);

            for (Map.Entry<String, Boolean> entry : attribute.getCollectionJoinMappings(jpaManagedType, context).entrySet()) {
                if (entry.getValue()) {
                    List<String> locations = collectionMappingSingulars.get(entry.getKey());
                    if (locations == null) {
                        locations = new ArrayList<>(2);
                        collectionMappingSingulars.put(entry.getKey(), locations);
                    }

                    locations.add("Attribute '" + attribute.getName() + "' in entity view '" + javaType.getName() + "'");
                } else {
                    List<String> locations = collectionMappings.get(entry.getKey());
                    if (locations == null) {
                        locations = new ArrayList<>(2);
                        collectionMappings.put(entry.getKey(), locations);
                    }

                    locations.add("Attribute '" + attribute.getName() + "' in entity view '" + javaType.getName() + "'");
                }
            }
        }

        if (!constructorIndex.isEmpty()) {
            for (MappingConstructorImpl<X> constructor : constructorIndex.values()) {
                Map<String, List<String>> constructorCollectionMappings = new HashMap<>();

                for (Map.Entry<String, List<String>> entry : collectionMappings.entrySet()) {
                    constructorCollectionMappings.put(entry.getKey(), new ArrayList<>(entry.getValue()));
                }

                constructor.checkParameters(jpaManagedType, constructorCollectionMappings, collectionMappingSingulars, context);
                reportCollectionMappingErrors(context, collectionMappings, collectionMappingSingulars);
            }
        } else {
            reportCollectionMappingErrors(context, collectionMappings, collectionMappingSingulars);
        }
    }

    private static void reportCollectionMappingErrors(MetamodelBuildingContext context, Map<String, List<String>> collectionMappings, Map<String, List<String>> collectionMappingSingulars) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, List<String>> locationsEntry : collectionMappings.entrySet()) {
            List<String> locations = locationsEntry.getValue();
            List<String> singularLocations = null;
            if (locations.size() > 1 || (singularLocations = collectionMappingSingulars.get(locationsEntry.getKey())) != null) {
                sb.setLength(0);
                sb.append("Invalid multiple JOIN fetch usages of the plural mapping '" + locationsEntry.getKey() + "'. Consider mapping the plural attribute only once as a subview or use a different fetch strategy. Problematic uses");

                for (String location : locations) {
                    sb.append("\n - ");
                    sb.append(location);
                }
                if (singularLocations != null) {
                    for (String location : singularLocations) {
                        sb.append("\n - ");
                        sb.append(location);
                    }
                }
                context.addError(sb.toString());
            }
        }
    }

    @Override
    public void checkNestedAttributes(List<AbstractAttribute<?, ?>> parents, MetamodelBuildingContext context) {
        for (AbstractMethodAttribute<? super X, ?> attribute : attributes.values()) {
            attribute.checkNestedAttribute(parents, jpaManagedType, context);
        }

        if (!constructorIndex.isEmpty()) {
            for (MappingConstructorImpl<X> constructor : constructorIndex.values()) {
                constructor.checkNestedParameters(parents, jpaManagedType, context);
            }
        }
    }

    protected abstract boolean hasId();

    @Override
    public boolean isUpdatable() {
        return updatable;
    }

    @Override
    public LockMode getLockMode() {
        return lockMode;
    }

    @Override
    public boolean isCreatable() {
        return creatable;
    }

    @Override
    public Method getPostCreateMethod() {
        return postCreateMethod;
    }

    @Override
    public Method getPostConvertMethod() {
        return postConvertMethod;
    }

    @Override
    public Method getPrePersistMethod() {
        return prePersistMethod;
    }

    @Override
    public Method getPostPersistMethod() {
        return postPersistMethod;
    }

    @Override
    public Method getPreUpdateMethod() {
        return preUpdateMethod;
    }

    @Override
    public Method getPostUpdateMethod() {
        return postUpdateMethod;
    }

    @Override
    public Method getPreRemoveMethod() {
        return preRemoveMethod;
    }

    @Override
    public Method getPostRemoveMethod() {
        return postRemoveMethod;
    }

    @Override
    public Method getPostRollbackMethod() {
        return postRollbackMethod;
    }

    @Override
    public Method getPostCommitMethod() {
        return postCommitMethod;
    }

    @Override
    public Set<ViewTransition> getPostRollbackTransitions() {
        return postRollbackTransitions;
    }

    @Override
    public Set<ViewTransition> getPostCommitTransitions() {
        return postCommitTransitions;
    }

    @Override
    public List<Method> getSpecialMethods() {
        return specialMethods;
    }

    @Override
    public FlushMode getFlushMode() {
        return flushMode;
    }

    @Override
    public FlushStrategy getFlushStrategy() {
        return flushStrategy;
    }

    @Override
    public boolean isPersistabilityValidationEnabled() {
        return validatePersistability;
    }

    @Override
    public Set<String> getPersistabilityValidationExcludedEntityAttributes() {
        return excludedEntityAttributes;
    }

    @Override
    public Class<X> getJavaType() {
        return javaType;
    }

    @Override
    public Type getConvertedType() {
        return null;
    }

    @Override
    public TypeConverter<?, X> getConverter() {
        return null;
    }

    @Override
    public Class<?> getEntityClass() {
        return jpaManagedType.getJavaType();
    }

    @Override
    public ManagedType<?> getJpaManagedType() {
        return jpaManagedType;
    }

    @Override
    public int getDefaultBatchSize() {
        return defaultBatchSize;
    }

    @Override
    public Set<MethodAttribute<? super X, ?>> getAttributes() {
        return new SetView<MethodAttribute<? super X, ?>>(attributes.values());
    }

    @Override
    public Set<AbstractMethodAttribute<? super X, ?>> getUpdateMappableAttributes() {
        return updateMappableAttributes;
    }

    @Override
    public MethodAttribute<? super X, ?> getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public Set<MappingConstructor<X>> getConstructors() {
        return new SetView<MappingConstructor<X>>(constructorIndex.values());
    }

    @Override
    public MappingConstructor<X> getConstructor(Class<?>... parameterTypes) {
        return constructors.get(new ParametersKey(parameterTypes));
    }

    @Override
    public Set<String> getConstructorNames() {
        return constructorIndex.keySet();
    }

    @Override
    public MappingConstructorImpl<X> getConstructor(String name) {
        if (name == null) {
            return null;
        }
        return constructorIndex.get(name);
    }

    @Override
    public NavigableMap<String, AbstractMethodAttribute<? super X, ?>> getRecursiveAttributes() {
        return recursiveAttributes;
    }

    @Override
    public NavigableMap<String, AbstractMethodAttribute<? super X, ?>> getRecursiveSubviewAttributes() {
        return recursiveSubviewAttributes;
    }

    @Override
    public String getInheritanceMapping() {
        return inheritanceMapping;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<ManagedViewType<? extends X>> getInheritanceSubtypes() {
        return (Set<ManagedViewType<? extends X>>) (Set<?>) defaultInheritanceSubtypeConfiguration.inheritanceSubtypes;
    }

    @Override
    public Map<ManagedViewTypeImplementor<? extends X>, String> getInheritanceSubtypeConfiguration() {
        return defaultInheritanceSubtypeConfiguration.inheritanceSubtypeConfiguration;
    }

    @Override
    public boolean hasEmptyConstructor() {
        if (javaType.isInterface() || constructors.isEmpty()) {
            return true;
        }
        for (MappingConstructorImpl<?> c : constructors.values()) {
            if (c.getParameterAttributes().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasJoinFetchedCollections() {
        return hasJoinFetchedCollections;
    }

    @Override
    public boolean hasSubtypes() {
        return defaultInheritanceSubtypeConfiguration.inheritanceSubtypes.size() > 1 || !defaultInheritanceSubtypeConfiguration.inheritanceSubtypes.contains(this);
    }

    @Override
    public int getSubtypeIndex(ManagedViewTypeImplementor<? super X> inheritanceBase) {
        int subtypeIndex = 0;
        for (ManagedViewType<?> subtype : inheritanceBase.getOverallInheritanceSubtypeConfiguration().getInheritanceSubtypes()) {
            if (subtype == this) {
                break;
            }
            subtypeIndex++;
        }
        return subtypeIndex;
    }

    @Override
    public InheritanceSubtypeConfiguration<X> getInheritanceSubtypeConfiguration(Map<ManagedViewTypeImplementor<? extends X>, String> inheritanceSubtypeMapping) {
        if (inheritanceSubtypeMapping == null || inheritanceSubtypeMapping.isEmpty() || defaultInheritanceSubtypeConfiguration.getInheritanceSubtypeConfiguration() == inheritanceSubtypeMapping) {
            return defaultInheritanceSubtypeConfiguration;
        }
        return inheritanceSubtypeConfigurations.get(inheritanceSubtypeMapping);
    }

    @Override
    public InheritanceSubtypeConfiguration<X> getOverallInheritanceSubtypeConfiguration() {
        return overallInheritanceSubtypeConfiguration;
    }

    @Override
    public InheritanceSubtypeConfiguration<X> getDefaultInheritanceSubtypeConfiguration() {
        return defaultInheritanceSubtypeConfiguration;
    }

    @Override
    public Map<Map<ManagedViewTypeImplementor<? extends X>, String>, InheritanceSubtypeConfiguration<X>> getInheritanceSubtypeConfigurations() {
        return inheritanceSubtypeConfigurations;
    }

    @Override
    public AbstractMethodAttribute<?, ?> getMutableAttribute(int i) {
        return mutableAttributes[i];
    }

    @Override
    public int getMutableAttributeCount() {
        return mutableAttributes.length;
    }

    public String getTypeConstraintMapping() {
        if (jpaManagedType instanceof EntityType<?>) {
            return "TYPE(this) = " + ((EntityType<?>) jpaManagedType).getName();
        }
        return null;
    }

    @Override
    public Set<CTEProvider> getCteProviders() {
        return cteProviders;
    }

    /**
     *
     * @author Giovanni Lovato
     * @since 1.4.0
     */
    private static final class CTEProviderCollector implements Runnable {
        private final ManagedViewTypeImpl<?> viewType;
        private final MetamodelBuildingContext context;
        private final ViewMapping viewMapping;

        private CTEProviderCollector(ManagedViewTypeImpl<?> viewType, MetamodelBuildingContext context, ViewMapping viewMapping) {
            this.viewType = viewType;
            this.viewMapping = viewMapping;
            this.context = context;
        }

        @Override
        public void run() {
            Set<Class<? extends CTEProvider>> rootProviders = viewMapping.getCteProviders();
            if (rootProviders != null) {
                Map<Class<?>, CTEProvider> providers = context.getCteProviders();
                for (Class<? extends CTEProvider> clazz : rootProviders) {
                    CTEProvider provider = providers.get(clazz);
                    if (provider == null) {
                        provider = new SimpleCTEProviderFactory(clazz).create();
                        providers.put(clazz, provider);
                    }
                    this.viewType.cteProviders.add(provider);
                }
            }
            for (AbstractMethodAttribute<?, ?> attribute : this.viewType.recursiveSubviewAttributes.values()) {
                com.blazebit.persistence.view.metamodel.Type<?> elementType = attribute.getElementType();
                if (elementType instanceof ManagedViewTypeImpl) {
                    ManagedViewType<?> viewType = (ManagedViewType<?>) attribute.getElementType();
                    this.viewType.cteProviders.addAll(viewType.getCteProviders());
                }
            }
        }
    }

    /**
     *
     * @author Christian Beikov
     * @since 1.4.0
     */
    private static final class ElementTypeSubviewAttributeCollector<X> implements Runnable {
        private final AbstractMethodAttribute<? super X, ?> attribute;
        private final NavigableMap<String, AbstractMethodAttribute<? super X, ?>> recursiveAttributes;
        private final NavigableMap<String, AbstractMethodAttribute<? super X, ?>> recursiveSubviewAttributes;
        private final ManagedViewTypeImplementor<Object> elementType;
        private final MetamodelBuildingContext context;

        private ElementTypeSubviewAttributeCollector(AbstractMethodAttribute<? super X, ?> attribute,
                NavigableMap<String, AbstractMethodAttribute<? super X, ?>> recursiveAttributes,
                NavigableMap<String, AbstractMethodAttribute<? super X, ?>> recursiveSubviewAttributes,
                ManagedViewTypeImplementor<Object> elementType, MetamodelBuildingContext context) {
            this.attribute = attribute;
            this.recursiveAttributes = recursiveAttributes;
            this.recursiveSubviewAttributes = recursiveSubviewAttributes;
            this.elementType = elementType;
            this.context = context;
        }

        @Override
        public void run() {
            if (elementType.getRecursiveSubviewAttributes() != null) {
                for (Map.Entry<String, AbstractMethodAttribute<? super Object, ?>> subEntry : elementType.getRecursiveSubviewAttributes().entrySet()) {
                    recursiveSubviewAttributes.put(PrefixingQueryGenerator.prefix(context.getExpressionFactory(), subEntry.getKey(), attribute.getName()), subEntry.getValue());
                }
            }
            // Cyclic models can run into this condition, but it's ok because we only allow cycles at non-cascading attributes
            if (elementType.getRecursiveAttributes() != null) {
                for (Map.Entry<String, AbstractMethodAttribute<? super Object, ?>> subEntry : elementType.getRecursiveAttributes().entrySet()) {
                    recursiveAttributes.put(PrefixingQueryGenerator.prefix(context.getExpressionFactory(), subEntry.getKey(), attribute.getName()), subEntry.getValue());
                }
            }
        }
    }

    /**
     *
     * @author Christian Beikov
     * @since 1.4.0
     */
    private static final class KeyTypeSubviewAttributeCollector<X> implements Runnable {
        private final AbstractMethodAttribute<? super X, ?> attribute;
        private final NavigableMap<String, AbstractMethodAttribute<? super X, ?>> recursiveAttributes;
        private final NavigableMap<String, AbstractMethodAttribute<? super X, ?>> recursiveSubviewAttributes;
        private final ManagedViewTypeImplementor<Object> keyType;
        private final MetamodelBuildingContext context;

        private KeyTypeSubviewAttributeCollector(AbstractMethodAttribute<? super X, ?> attribute,
                NavigableMap<String, AbstractMethodAttribute<? super X, ?>> recursiveAttributes,
                NavigableMap<String, AbstractMethodAttribute<? super X, ?>> recursiveSubviewAttributes,
                ManagedViewTypeImplementor<Object> keyType, MetamodelBuildingContext context) {
            this.attribute = attribute;
            this.recursiveAttributes = recursiveAttributes;
            this.recursiveSubviewAttributes = recursiveSubviewAttributes;
            this.keyType = keyType;
            this.context = context;
        }

        @Override
        public void run() {
            if (keyType.getRecursiveSubviewAttributes() != null) {
                for (Map.Entry<String, AbstractMethodAttribute<? super Object, ?>> subEntry : keyType.getRecursiveSubviewAttributes().entrySet()) {
                    recursiveSubviewAttributes.put("KEY(" + PrefixingQueryGenerator.prefix(context.getExpressionFactory(), subEntry.getKey(), attribute.getName()) + ")", subEntry.getValue());
                }
            }
            if (keyType.getRecursiveAttributes() != null) {
                for (Map.Entry<String, AbstractMethodAttribute<? super Object, ?>> subEntry : keyType.getRecursiveAttributes().entrySet()) {
                    recursiveAttributes.put("KEY(" + PrefixingQueryGenerator.prefix(context.getExpressionFactory(), subEntry.getKey(), attribute.getName()) + ")", subEntry.getValue());
                }
            }
        }
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    public static final class AttributeKey {
        final int subtypeIndex;
        final String attributeName;

        public AttributeKey(int subtypeIndex, String attributeName) {
            this.subtypeIndex = subtypeIndex;
            this.attributeName = attributeName;
        }

        public int getSubtypeIndex() {
            return subtypeIndex;
        }

        public String getAttributeName() {
            return attributeName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof AttributeKey)) {
                return false;
            }

            AttributeKey that = (AttributeKey) o;

            if (subtypeIndex != -1 && that.subtypeIndex != -1 && subtypeIndex != that.subtypeIndex) {
                return false;
            }
            return attributeName.equals(that.attributeName);
        }

        @Override
        public int hashCode() {
            return attributeName.hashCode();
        }
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    public static class InheritanceSubtypeConfiguration<X> {
        private final ManagedViewTypeImpl<X> baseType;
        private final int configurationIndex;
        private final Map<ManagedViewTypeImplementor<? extends X>, String> inheritanceSubtypeConfiguration;
        private final Set<ManagedViewTypeImplementor<? extends X>> inheritanceSubtypes;
        private final String inheritanceDiscriminatorMapping;
        private final Map<AttributeKey, ConstrainedAttribute<AbstractMethodAttribute<? super X, ?>>> attributesClosure;
        private final Map<ManagedViewTypeImplementor<? extends X>, int[]> overallPositionAssignments;

        public InheritanceSubtypeConfiguration(ManagedViewTypeImpl<X> baseType, ViewMapping baseTypeViewMapping, int configurationIndex, InheritanceViewMapping inheritanceViewMapping, MetamodelBuildingContext context, EmbeddableOwner embeddableMapping) {
            this(baseType, baseTypeViewMapping, configurationIndex, inheritanceViewMapping, context, embeddableMapping, null);
        }

        public InheritanceSubtypeConfiguration(ManagedViewTypeImpl<X> baseType, ViewMapping baseTypeViewMapping, int configurationIndex, InheritanceViewMapping inheritanceViewMapping, MetamodelBuildingContext context, EmbeddableOwner embeddableMapping, InheritanceSubtypeConfiguration<X> overallConfiguration) {
            this.baseType = baseType;
            this.configurationIndex = configurationIndex;
            ManagedViewTypeImpl<? extends X>[] orderedInheritanceSubtypes = createOrderedSubtypes(inheritanceViewMapping, context, embeddableMapping);
            this.inheritanceSubtypeConfiguration = createInheritanceSubtypeConfiguration(inheritanceViewMapping, context, embeddableMapping);
            this.inheritanceSubtypes = Collections.unmodifiableSet(inheritanceSubtypeConfiguration.keySet());
            this.inheritanceDiscriminatorMapping = createInheritanceDiscriminatorMapping(orderedInheritanceSubtypes);
            this.attributesClosure = createSubtypeAttributesClosure(orderedInheritanceSubtypes);
            Map<ManagedViewTypeImplementor<? extends X>, int[]> positionAssignments = new HashMap<>();

            // Map the attribute names to overall positions
            Map<AttributeKey, ConstrainedAttribute<AbstractMethodAttribute<? super X, ?>>> overallAttributesClosure;
            if (overallConfiguration == null) {
                overallAttributesClosure = attributesClosure;
            } else {
                overallAttributesClosure = overallConfiguration.attributesClosure;
            }

            Map<String, Integer> overallPositionMap = new HashMap<>(overallAttributesClosure.size());
            int index = 0;
            String idName = null;
            if (baseType instanceof ViewTypeImpl<?>) {
                idName = baseTypeViewMapping.getIdAttribute().getName();
                overallPositionMap.put(idName, index);
                index++;
            }
            for (AttributeKey attributeKey : overallAttributesClosure.keySet()) {
                if (!attributeKey.attributeName.equals(idName)) {
                    overallPositionMap.put(attributeKey.attributeName, index);
                    index++;
                }
            }

            // Then create position assignments for all subtypes
            for (ManagedViewTypeImpl<? extends X> subtype : orderedInheritanceSubtypes) {
                // positionAssignment is a Map<TargetOverallConstructorPosition, StaticCreateFactoryParameterPosition>
                int[] positionAssignment = new int[overallPositionMap.size()];
                Arrays.fill(positionAssignment, -1);

                index = 0;
                if (idName != null) {
                    positionAssignment[index] = index;
                    index++;
                }
                // The attribute closure is in the order that we define the static create factory methods
                for (AttributeKey attributeKey : attributesClosure.keySet()) {
                    if (!attributeKey.attributeName.equals(idName)) {
                        // Only consider passing through attributes that exist in a subtype
                        if (subtype.getAttribute(attributeKey.getAttributeName()) != null) {
                            Integer position = overallPositionMap.get(attributeKey.attributeName);
                            if (position != null) {
                                positionAssignment[position] = index;
                            }
                        }
                        index++;
                    }
                }

                positionAssignments.put(subtype, positionAssignment);
            }
            this.overallPositionAssignments = Collections.unmodifiableMap(positionAssignments);
        }

        public ManagedViewTypeImplementor<X> getBaseType() {
            return baseType;
        }

        public int getConfigurationIndex() {
            return configurationIndex;
        }

        public Set<ManagedViewTypeImplementor<? extends X>> getInheritanceSubtypes() {
            return inheritanceSubtypes;
        }

        public Map<ManagedViewTypeImplementor<? extends X>, String> getInheritanceSubtypeConfiguration() {
            return inheritanceSubtypeConfiguration;
        }

        public String getInheritanceDiscriminatorMapping() {
            return inheritanceDiscriminatorMapping;
        }

        public Map<AttributeKey, ConstrainedAttribute<AbstractMethodAttribute<? super X, ?>>> getAttributesClosure() {
            return attributesClosure;
        }

        public int[] getOverallPositionAssignment(ManagedViewTypeImplementor<? extends X> subtype) {
            return overallPositionAssignments.get(subtype);
        }

        @SuppressWarnings("unchecked")
        private ManagedViewTypeImpl<? extends X>[] createOrderedSubtypes(InheritanceViewMapping inheritanceViewMapping, MetamodelBuildingContext context, EmbeddableOwner embeddableMapping) {
            ManagedViewTypeImpl<? extends X>[] orderedSubtypes = new ManagedViewTypeImpl[inheritanceViewMapping.getInheritanceSubtypeMappings().size()];
            int i = 0;
            for (ViewMapping mapping : inheritanceViewMapping.getInheritanceSubtypeMappings().keySet()) {
                if (mapping.getEntityViewClass() == baseType.javaType) {
                    orderedSubtypes[i++] = baseType;
                } else {
                    orderedSubtypes[i++] = (ManagedViewTypeImpl<X>) context.getManagedViewType(mapping, embeddableMapping);
                }
            }

            return orderedSubtypes;
        }

        @SuppressWarnings("unchecked")
        private Map<ManagedViewTypeImplementor<? extends X>, String> createInheritanceSubtypeConfiguration(InheritanceViewMapping inheritanceViewMapping, MetamodelBuildingContext context, EmbeddableOwner embeddableMapping) {
            Map<ManagedViewTypeImplementor<? extends X>, String> configuration = new LinkedHashMap<>(inheritanceViewMapping.getInheritanceSubtypeMappings().size());

            for (Map.Entry<ViewMapping, String> mappingEntry : inheritanceViewMapping.getInheritanceSubtypeMappings().entrySet()) {
                String mapping = mappingEntry.getValue();
                if (mapping == null) {
                    mapping = mappingEntry.getKey().determineInheritanceMapping(context);
                    // An empty inheritance mapping signals that a subtype should actually be considered. If it was null it wouldn't be considered
                    if (mapping == null) {
                        mapping = "";
                    }
                }
                if (mappingEntry.getKey().getEntityViewClass() == baseType.javaType) {
                    configuration.put(baseType, mapping);
                } else {
                    configuration.put((ManagedViewTypeImplementor<? extends X>) context.getManagedViewType(mappingEntry.getKey(), embeddableMapping), mapping);
                }
            }

            return configuration;
        }

        public boolean hasSubtypes() {
            return inheritanceDiscriminatorMapping != null;
        }

        @SuppressWarnings("unchecked")
        private String createInheritanceDiscriminatorMapping(ManagedViewTypeImpl<? extends X>[] subtypes) {
            if (subtypes.length == 1 && subtypes[0] == baseType) {
                return null;
            }

            SortedMap<ManagedViewTypeImplementor<? extends X>, Integer> concreteToGeneralViewToDiscriminatorMap = new TreeMap<>(new Comparator<ManagedViewTypeImplementor<? extends X>>() {
                @Override
                public int compare(ManagedViewTypeImplementor<? extends X> o1, ManagedViewTypeImplementor<? extends X> o2) {
                    Class<? extends X> j1 = o1.getJavaType();
                    Class<? extends X> j2 = o2.getJavaType();
                    if (j1 == j2) {
                        return 0;
                    }
                    if (j1.isAssignableFrom(j2)) {
                        return 1;
                    }
                    if (j2.isAssignableFrom(j1)) {
                        return -1;
                    }

                    return j1.getName().compareTo(j2.getName());
                }
            });

            int subtypeIndex = 0;
            for (ManagedViewTypeImplementor<? extends X> subtype : subtypes) {
                concreteToGeneralViewToDiscriminatorMap.put(subtype, subtypeIndex++);
            }

            // Build the discriminator mapping expression
            StringBuilder sb = new StringBuilder();
            sb.append("CASE");
            for (Map.Entry<ManagedViewTypeImplementor<? extends X>, Integer> entry : concreteToGeneralViewToDiscriminatorMap.entrySet()) {
                ManagedViewTypeImplementor<?> subtype = entry.getKey();
                subtypeIndex = entry.getValue();
                if (subtype == baseType) {
                    continue;
                }
                String inheritanceMapping = inheritanceSubtypeConfiguration.get(subtype);

                if (inheritanceMapping != null && !inheritanceMapping.isEmpty()) {
                    sb.append(" WHEN ");
                    sb.append(inheritanceMapping);

                    for (int i = subtypeIndex - 1; i >= 0; i--) {
                        inheritanceMapping = inheritanceSubtypeConfiguration.get(subtypes[i]);
                        if (subtypes[i].javaType.isAssignableFrom(subtype.getJavaType()) && inheritanceMapping != null && !inheritanceMapping.isEmpty()) {
                            // We only need to add the super type condition if the entity type is the same or the condition is not a type constraint
                            if (subtypes[i].getJpaManagedType() == subtype.getJpaManagedType() || !inheritanceMapping.equals(subtypes[i].getTypeConstraintMapping())) {
                                sb.append(" AND ").append(inheritanceMapping);
                            }
                        }
                    }

                    sb.append(" THEN ");
                    sb.append(subtypeIndex);
                }
            }

            // We only consider mappings that are non-null
            String defaultMapping = inheritanceSubtypeConfiguration.get(baseType);
            if (defaultMapping != null) {
                if (defaultMapping.isEmpty()) {
                    sb.append(" ELSE 0");
                } else {
                    sb.append(" WHEN ").append(defaultMapping).append(" THEN 0");
                }
            }

            sb.append(" END");
            return sb.toString();
        }

        @SuppressWarnings("unchecked")
        private Map<AttributeKey, ConstrainedAttribute<AbstractMethodAttribute<? super X, ?>>> createSubtypeAttributesClosure(ManagedViewTypeImpl<? extends X>[] subtypes) {
            int subtypeIndex = 0;
            // Collect all attributes from all subtypes in separate maps
            Map<AttributeKey, ConstrainedAttribute<AbstractMethodAttribute<? super X, ?>>> subtypesAttributesClosure = new LinkedHashMap<>();
            Set<Integer> subtypeIndexes = new TreeSet<>();
            for (int i = 0; i < subtypes.length; i++) {
                subtypeIndexes.add(i);
            }
            int[] subtypeIndexArray = new int[subtypeIndexes.size()];
            {
                int j = 0;
                for (Integer idx : subtypeIndexes) {
                    subtypeIndexArray[j++] = idx;
                }
            }

            for (AbstractMethodAttribute<? super X, ?> attribute : baseType.attributes.values()) {
                subtypesAttributesClosure.put(new AttributeKey(0, attribute.getName()), new ConstrainedAttribute<AbstractMethodAttribute<? super X, ?>>(null, subtypeIndexArray, attribute));
            }

            if (subtypes.length > 0) {
                // Go through the subtype attributes and put them in the attribute closure maps
                for (int i = 0; i < subtypes.length; i++) {
                    ManagedViewTypeImpl<? extends X> subtype = subtypes[i];
                    subtypeIndexes.clear();
                    for (int j = i; j < subtypes.length; j++) {
                        if (subtype.getJavaType().isAssignableFrom(subtypes[j].getJavaType())) {
                            subtypeIndexes.add(j);
                        }
                    }
                    subtypeIndexArray = new int[subtypeIndexes.size()];
                    {
                        int j = 0;
                        for (Integer idx : subtypeIndexes) {
                            subtypeIndexArray[j++] = idx;
                        }
                    }

                    for (AbstractMethodAttribute<? super X, ?> attribute : (Collection<AbstractMethodAttribute<? super X, ?>>) (Collection<?>) subtype.attributes.values()) {
                        // Try to find the attribute on some of the super types to see if it is specialized
                        ConstrainedAttribute<AbstractMethodAttribute<? super X, ?>> superTypeAttribute = findAttribute(subtypesAttributesClosure, subtypes, subtypeIndex, attribute.getName());
                        if (superTypeAttribute != null) {
                            if (attribute.getJavaMethod().equals(superTypeAttribute.getAttribute().getJavaMethod())) {
                                superTypeAttribute.addSubAttribute(subtype, attribute);
                            } else {
                                // This attribute was overridden/specialized in a subtype
                                superTypeAttribute.addSelectionConstraint(inheritanceSubtypeConfiguration.get(subtype), subtypeIndexArray, attribute);
                            }
                        } else {
                            subtypesAttributesClosure.put(new AttributeKey(subtypeIndex, attribute.getName()), new ConstrainedAttribute<AbstractMethodAttribute<? super X, ?>>(inheritanceSubtypeConfiguration.get(subtype), subtypeIndexArray, attribute));
                        }
                    }
                    subtypeIndex++;
                }
            }

            return subtypesAttributesClosure;
        }

        @SuppressWarnings("unchecked")
        private ConstrainedAttribute<AbstractMethodAttribute<? super X, ?>> findAttribute(Map<AttributeKey, ConstrainedAttribute<AbstractMethodAttribute<? super X, ?>>> subtypesAttributesClosure, ManagedViewTypeImpl<?>[] subtypes, int subtypeIndex, String name) {
            for (int i = subtypeIndex; i >= 0; i--) {
                // Must be a proper subtype and contain an attribute with that name
                if (subtypes[i].javaType.isAssignableFrom(subtypes[i].javaType) && subtypes[i].getAttribute(name) != null) {
                    // Only then we will try to find the constrained attribute
                    ConstrainedAttribute<?> attribute = subtypesAttributesClosure.get(new AttributeKey(i, name));
                    if (attribute != null) {
                        return (ConstrainedAttribute<AbstractMethodAttribute<? super X, ?>>) attribute;
                    }
                }
            }

            return null;
        }
    }
}
