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

import com.blazebit.persistence.parser.util.JpaMetamodelUtils;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.LockMode;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.spi.EntityViewAttributeMapping;
import com.blazebit.persistence.view.spi.EntityViewConstructorMapping;
import com.blazebit.persistence.view.spi.type.EntityViewProxy;

import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.SingularAttribute;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ViewMappingImpl implements ViewMapping {

    private final MetamodelBootContext context;
    private final Class<?> entityViewClass;
    // Basic configs
    private Class<?> entityClass;
    private String name;

    // Other configs
    private Integer defaultBatchSize;

    // Updatable entity view configs
    private boolean updatable;
    private FlushMode flushMode;
    private FlushStrategy flushStrategy;

    // Creatable entity view configs
    private Method postCreateMethod;
    private List<Method> specialMethods = new ArrayList<>();
    private boolean creatable;
    private boolean validatePersistability;
    private final Set<String> excludedAttributes = new TreeSet<>();

    // We use a tree map to get a deterministic attribute order
    private final Map<String, MethodAttributeMapping> attributes = new TreeMap<>();
    // Deterministic order of constructors
    private final Map<ParametersKey, ConstructorMapping> constructors = new TreeMap<>();
    private final Map<String, ConstructorMapping> constructorNameMap = new TreeMap<>();

    private MethodAttributeMapping idAttribute;
    private MethodAttributeMapping versionAttribute;
    private String lockOwner;
    private LockMode lockMode;
    private LockMode resolvedLockMode;

    private String inheritanceMapping;
    private boolean inheritanceSubtypesResolved;
    private Set<Class<?>> inheritanceSubtypeClasses = new LinkedHashSet<>();
    private final Set<ViewMapping> inheritanceSubtypes = new LinkedHashSet<>();
    private final Set<ViewMapping> inheritanceSupertypes;
    private InheritanceViewMapping defaultInheritanceViewMapping;
    private final Set<InheritanceViewMapping> inheritanceViewMappings;

    private boolean initialized;
    private boolean finished;
    private final List<Runnable> finishListeners = new ArrayList<>();
    private ManagedViewTypeImplementor<?> viewType;

    public ViewMappingImpl(Class<?> entityViewClass, Class<?> entityClass, String name, MetamodelBootContext context) {
        this.entityViewClass = entityViewClass;
        this.entityClass = entityClass;
        this.name = name;
        this.context = context;
        this.inheritanceSupertypes = new HashSet<>();
        // Make the order of the inheritanceViewMappings deterministic, otherwise clustering won't work
        this.inheritanceViewMappings = new TreeSet<>();
    }

    @Override
    public InheritanceViewMapping getDefaultInheritanceViewMapping() {
        return defaultInheritanceViewMapping;
    }

    @Override
    public Class<?> getEntityViewClass() {
        return entityViewClass;
    }

    @Override
    public Class<?> getEntityClass() {
        return entityClass;
    }

    @Override
    public void setEntityClass(Class<?> entityClass) {
        this.entityClass = entityClass;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Integer getDefaultBatchSize() {
        return defaultBatchSize;
    }

    @Override
    public void setDefaultBatchSize(Integer defaultBatchSize) {
        this.defaultBatchSize = defaultBatchSize;
    }

    @Override
    public boolean isUpdatable() {
        return updatable;
    }

    @Override
    public void setUpdatable(boolean updatable) {
        this.updatable = updatable;
    }

    @Override
    public FlushMode getFlushMode() {
        return flushMode;
    }

    @Override
    public void setFlushMode(FlushMode flushMode) {
        this.flushMode = flushMode;
    }

    @Override
    public FlushStrategy getFlushStrategy() {
        return flushStrategy;
    }

    @Override
    public void setFlushStrategy(FlushStrategy flushStrategy) {
        this.flushStrategy = flushStrategy;
    }

    @Override
    public boolean isCreatable() {
        return creatable;
    }

    @Override
    public void setCreatable(boolean creatable) {
        this.creatable = creatable;
    }

    @Override
    public Method getPostCreateMethod() {
        return postCreateMethod;
    }

    @Override
    public void setPostCreateMethod(Method postCreateMethod) {
        this.postCreateMethod = postCreateMethod;
    }

    @Override
    public List<Method> getSpecialMethods() {
        return specialMethods;
    }

    public void setSpecialMethods(List<Method> specialMethods) {
        this.specialMethods = specialMethods;
    }

    @Override
    public boolean isValidatePersistability() {
        return validatePersistability;
    }

    @Override
    public void setValidatePersistability(boolean validatePersistability) {
        this.validatePersistability = validatePersistability;
    }

    @Override
    public Set<String> getExcludedAttributes() {
        return excludedAttributes;
    }

    @Override
    public MethodAttributeMapping getIdAttribute() {
        return idAttribute;
    }

    @Override
    public void setIdAttribute(EntityViewAttributeMapping idAttribute) {
        setIdAttributeMapping((MethodAttributeMapping) idAttribute);
    }

    @Override
    public void setIdAttributeMapping(MethodAttributeMapping idAttribute) {
        this.idAttribute = idAttribute;
    }

    @Override
    public MethodAttributeMapping getVersionAttribute() {
        return versionAttribute;
    }

    @Override
    public void setVersionAttribute(EntityViewAttributeMapping versionAttribute) {
        setVersionAttributeMapping((MethodAttributeMapping) versionAttribute);
    }

    @Override
    public void setVersionAttributeMapping(MethodAttributeMapping versionAttribute) {
        this.versionAttribute = versionAttribute;
    }

    @Override
    public LockMode getLockMode() {
        return lockMode == null ? LockMode.AUTO : lockMode;
    }

    @Override
    public void setLockMode(LockMode lockMode) {
        this.lockMode = lockMode;
        if (lockMode != LockMode.AUTO) {
            this.resolvedLockMode = lockMode;
        } else {
            this.resolvedLockMode = null;
        }
    }

    @Override
    public LockMode getResolvedLockMode() {
        return resolvedLockMode;
    }

    @Override
    public String getLockOwner() {
        return lockOwner;
    }

    @Override
    public void setLockOwner(String lockOwner) {
        this.lockOwner = lockOwner;
    }

    @Override
    public Map<String, EntityViewAttributeMapping> getAttributes() {
        return Collections.<String, EntityViewAttributeMapping>unmodifiableMap(attributes);
    }

    @Override
    public Map<String, MethodAttributeMapping> getMethodAttributes() {
        return attributes;
    }

    @Override
    public Map<String, EntityViewConstructorMapping> getConstructors() {
        return Collections.<String, EntityViewConstructorMapping>unmodifiableMap(constructorNameMap);
    }

    @Override
    public void addConstructor(ConstructorMapping constructorMapping) {
        if (constructorNameMap.put(constructorMapping.getName(), constructorMapping) != null) {
            context.addError("Constructor with duplicate view constructor name '" + constructorMapping.getName() + "' found: " + constructorMapping.getConstructor());
        }
        constructors.put(new ParametersKey(constructorMapping.getConstructor().getParameterTypes()), constructorMapping);
    }

    @Override
    public Map<ParametersKey, ConstructorMapping> getConstructorMappings() {
        return constructors;
    }

    @Override
    public String determineInheritanceMapping(MetamodelBuildingContext context) {
        if (inheritanceMapping == null && !inheritanceSupertypes.isEmpty()) {
            // Check all super type inheritance mappings. If we encounter that a super type uses
            // an entity class that is not a proper super type of our entity class, we can't infer a type inheritance mapping
            for (ViewMapping supertypeMapping : inheritanceSupertypes) {
                Class<?> supertypeEntityClass = supertypeMapping.getEntityClass();
                if (!supertypeEntityClass.isAssignableFrom(entityClass) || supertypeEntityClass == entityClass) {
                    return inheritanceMapping;
                }
            }

            // If we get here, we know that our entity class type is a proper subtype of all super type inheritance mappings
            return "TYPE(this) = " + context.getEntityMetamodel().entity(entityClass).getName();
        }

        return inheritanceMapping;
    }

    @Override
    public void setInheritanceMapping(String inheritanceMapping) {
        this.inheritanceMapping = inheritanceMapping;
    }

    @Override
    public boolean isInheritanceSubtypesResolved() {
        return inheritanceSubtypesResolved;
    }

    @Override
    public void setInheritanceSubtypesResolved(boolean inheritanceSubtypesResolved) {
        this.inheritanceSubtypesResolved = inheritanceSubtypesResolved;
    }

    @Override
    public Set<Class<?>> getInheritanceSubtypeClasses() {
        return inheritanceSubtypeClasses;
    }

    @Override
    public Set<ViewMapping> getInheritanceSubtypes() {
        return inheritanceSubtypes;
    }

    @Override
    public Set<ViewMapping> getInheritanceSupertypes() {
        return inheritanceSupertypes;
    }

    @Override
    public Set<InheritanceViewMapping> getInheritanceViewMappings() {
        return inheritanceViewMappings;
    }

    @Override
    public void onInitializeViewMappingsFinished(Runnable finishListener) {
        if (finished) {
            finishListener.run();
        } else {
            finishListeners.add(finishListener);
        }
    }

    @Override
    public void initializeViewMappings(MetamodelBuildingContext context, Runnable finishListener) {
        // Only initialize a view mapping once
        if (initialized) {
            if (finishListener != null) {
                onInitializeViewMappingsFinished(finishListener);
            }
            return;
        }
        // Mark as initialized early to avoid stack overflows in potential circular models
        initialized = true;

        if (!inheritanceSubtypesResolved) {
            inheritanceSubtypeClasses = context.findSubtypes(entityViewClass);
            inheritanceSubtypeClasses.remove(entityViewClass);
            inheritanceSubtypesResolved = true;
        }

        inheritanceViewMappings.add(defaultInheritanceViewMapping = new InheritanceViewMapping(this, inheritanceSubtypes));
        initializeSubtypes(entityViewClass, context, inheritanceSubtypeClasses);

        for (MethodAttributeMapping attributeMapping : attributes.values()) {
            attributeMapping.initializeViewMappings(context);
        }
        for (ConstructorMapping constructorMapping : getConstructorMappings().values()) {
            constructorMapping.initializeViewMappings(context);
        }

        finished = true;
        if (finishListener != null) {
            finishListener.run();
        }
        for (Runnable listener : finishListeners) {
            listener.run();
        }
        finishListeners.clear();
    }

    @Override
    public boolean validateDependencies(MetamodelBuildingContext context, Set<Class<?>> dependencies, AttributeMapping originatingAttributeMapping, Class<?> excludeEntityViewClass, boolean reportError) {
        if (originatingAttributeMapping != null && containsAny(context, dependencies, entityViewClass, excludeEntityViewClass)) {
            if (reportError) {
                originatingAttributeMapping.circularDependencyError(dependencies);
            }
            return true;
        }

        boolean error = false;
        dependencies.add(entityViewClass);

        for (InheritanceViewMapping inheritanceViewMapping : inheritanceViewMappings) {
            for (ViewMapping subtypeMapping : inheritanceViewMapping.getInheritanceSubtypeMappings().keySet()) {
                if (subtypeMapping != this) {
                    if (subtypeMapping.validateDependencies(context, dependencies, originatingAttributeMapping, entityViewClass, reportError)) {
                        error = true;
                        if (!reportError) {
                            return true;
                        }
                    }
                }
            }
        }

        // We keep the inheritance subtype classes in the dependencies for the attribute validation
        dependencies.addAll(inheritanceSubtypeClasses);

        for (MethodAttributeMapping attributeMapping : attributes.values()) {
            if (attributeMapping.validateDependencies(context, dependencies, reportError)) {
                error = true;
                if (!reportError) {
                    return true;
                }
            }
        }
        for (ConstructorMapping constructorMapping : getConstructorMappings().values()) {
            if (constructorMapping.validateDependencies(context, dependencies, reportError)) {
                error = true;
                if (!reportError) {
                    return true;
                }
            }
        }

        dependencies.removeAll(inheritanceSubtypeClasses);
        dependencies.remove(entityViewClass);

        return error;
    }

    private boolean containsAny(MetamodelBuildingContext context, Set<Class<?>> dependencies, Class<?> entityViewClass, Class<?> excludeEntityViewClass) {
        if (dependencies.contains(entityViewClass)) {
            return true;
        }
        for (Class<?> clazz : context.findSupertypes(entityViewClass)) {
            if (clazz != excludeEntityViewClass && dependencies.contains(clazz)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public ManagedViewTypeImplementor<?> getManagedViewType(MetamodelBuildingContext context) {
        if (viewType == null) {
            if (entityClass == null) {
                context.addError("The persistence unit metamodel doesn't contain the entity class configured in view mapping for entity view class: " + entityViewClass.getName());
            }
            ManagedType<?> managedType = context.getEntityMetamodel().getManagedType(entityClass);
            if (managedType == null) {
                context.addError("The entity class '" + entityClass.getName() + "' used for the entity view '" + entityViewClass.getName() + "' could not be found in the persistence unit!");
                return null;
            }
            if (!(managedType instanceof IdentifiableType<?>)) {
                if (idAttribute != null) {
                    context.addError("Invalid id attribute mapping for embeddable entity type '" + entityClass.getName() + "' at " + idAttribute.getErrorLocation() + " for managed view type '" + entityViewClass.getName() + "'!");
                }
                if (versionAttribute != null) {
                    context.addError("Invalid version attribute mapping for embeddable entity type '" + entityClass.getName() + "' at " + versionAttribute.getErrorLocation() + " for managed view type '" + entityViewClass.getName() + "'!");
                }
            } else {
                // If the identifiable type has an id attribute and is creatable or updatable
                // We try to infer the lock mode and other attributes if necessary
                if (idAttribute != null && (isCreatable() || isUpdatable())) {
                    if (resolvedLockMode == null) {
                        SingularAttribute<?, ?> versionAttribute = JpaMetamodelUtils.getVersionAttribute((IdentifiableType<?>) managedType);
                        if (versionAttribute == null) {
                            // If there is no lock mode defined, we default to the AUTO mode which will inherit lock modes of an owner
                            resolvedLockMode = LockMode.AUTO;
                        } else {
                            // If there is a version attribute, we default to optimistic locking
                            resolvedLockMode = LockMode.OPTIMISTIC;
                        }
                    }
                } else {
                    // Can't have a lock mode if neither creatable or updatable
                    resolvedLockMode = LockMode.NONE;
                }

                // If an optimistic locking mode was chosen, the backing entity needs a version attribute
                if (resolvedLockMode == LockMode.OPTIMISTIC) {
                    SingularAttribute<?, ?> versionAttribute = JpaMetamodelUtils.getVersionAttribute((IdentifiableType<?>) managedType);
                    if (versionAttribute == null) {
                        context.addError("No version attribute could be found for entity type '" + managedType.getJavaType().getName() + "', but since OPTIMISTIC locking was specified for the entity view type '" + entityViewClass.getName() + "' it is required!");
                    } else {
                        // Note that we also have to link or create the entity attribute for the JPA version attribute
                        MethodAttributeMapping versionAttributeMapping = null;
                        for (MethodAttributeMapping attributeMapping : attributes.values()) {
                            String mappingPath = getMappingPath(attributeMapping);
                            if (versionAttribute.getName().equals(mappingPath)) {
                                versionAttributeMapping = attributeMapping;
                                break;
                            }
                        }

                        // If there is no mapping for the attribute, we will create a synthetic attribute
                        if (versionAttributeMapping == null) {
                            try {
                                versionAttributeMapping = new MethodAttributeMapping(
                                        this,
                                        new MappingLiteral(versionAttribute.getName()),
                                        this.context,
                                        "$$_version",
                                        EntityViewProxy.class.getMethod("$$_getVersion"),
                                        false,
                                        versionAttribute.getJavaType(),
                                        null,
                                        null,
                                        versionAttribute.getJavaType(),
                                        null,
                                        null,
                                        null,
                                        null,
                                        null
                                );
                                attributes.put("$$_version", versionAttributeMapping);
                            } catch (NoSuchMethodException ex) {
                                throw new RuntimeException(ex);
                            }
                        }

                        this.versionAttribute = versionAttributeMapping;
                    }
                }
            }

            if (idAttribute != null) {
                return viewType = new ViewTypeImpl<Object>(this, managedType, context);
            } else {
                return viewType = new FlatViewTypeImpl<Object>(this, managedType, context);
            }
        }

        return viewType;
    }

    private String getMappingPath(MethodAttributeMapping attributeMapping) {
        if (attributeMapping.getMapping() instanceof Mapping) {
            return AbstractAttribute.stripThisFromMapping(((Mapping) attributeMapping.getMapping()).value());
        }

        return null;
    }

    private void initializeSubtypes(Class<?> entityViewClass, MetamodelBuildingContext context, Set<Class<?>> subtypeClasses) {
        for (Class<?> subtypeClass : subtypeClasses) {
            if (entityViewClass == subtypeClass) {
                context.addError("Entity view type '" + entityViewClass.getName() + "' declared itself in @EntityViewInheritance as subtype which is not allowed!");
                continue;
            }
            if (!entityViewClass.isAssignableFrom(subtypeClass)) {
                context.addError("Entity view subtype '" + subtypeClass.getName() + "' was explicitly declared as subtype in '" + entityViewClass.getName() + "' but isn't a Java subtype!");
                continue;
            }

            final ViewMapping subtypeMapping = context.getViewMapping(subtypeClass);
            if (subtypeMapping == null) {
                unknownSubtype(entityViewClass, subtypeClass);
            } else {
                subtypeMapping.initializeViewMappings(context, new Runnable() {
                    @Override
                    public void run() {
                        inheritanceSubtypes.add(subtypeMapping);
                        inheritanceSubtypes.addAll(subtypeMapping.getInheritanceSubtypes());
                        subtypeMapping.getInheritanceSupertypes().add(ViewMappingImpl.this);
                        defaultInheritanceViewMapping.getInheritanceSubtypeMappings().put(subtypeMapping, null);
                        for (ViewMapping inheritanceSubtypeMapping : subtypeMapping.getInheritanceSubtypes()) {
                            inheritanceSubtypeMapping.getInheritanceSupertypes().add(ViewMappingImpl.this);
                            defaultInheritanceViewMapping.getInheritanceSubtypeMappings().put(inheritanceSubtypeMapping, null);
                        }
                    }
                });

            }
        }
    }

    public void unknownSubtype(Class<?> entityViewClass, Class<?> subviewClass) {
        context.addError("An unknown or unregistered entity view subtype type '" + subviewClass.getName() + "' is used for the entity view: " + entityViewClass.getName() + "!");
    }

    @Override
    public int compareTo(ViewMapping o) {
        return getEntityViewClass().getName().compareTo(o.getEntityViewClass().getName());
    }
}
