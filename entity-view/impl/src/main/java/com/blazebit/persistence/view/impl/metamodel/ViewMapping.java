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

import com.blazebit.persistence.impl.util.JpaMetamodelUtils;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.LockMode;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.impl.proxy.EntityViewProxy;
import com.blazebit.persistence.view.spi.EntityViewAttributeMapping;
import com.blazebit.persistence.view.spi.EntityViewConstructorMapping;
import com.blazebit.persistence.view.spi.EntityViewMapping;

import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.SingularAttribute;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
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
public class ViewMapping implements Comparable<ViewMapping>, EntityViewMapping {

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
    private boolean creatable;
    private boolean validatePersistability;
    private final Set<String> excludedAttributes = new TreeSet<>();

    // We use a tree map to get a deterministic attribute order
    private final Map<String, MethodAttributeMapping> attributes = new TreeMap<>();
    // Deterministic order of constructors
    private final Map<ParametersKey, ConstructorMapping> constructors = new TreeMap<>();
    private final Map<String, ConstructorMapping> constructorNameMap = new HashMap<>();

    private MethodAttributeMapping idAttribute;
    private MethodAttributeMapping versionAttribute;
    private String lockOwner;
    private LockMode lockMode;
    private LockMode resolvedLockMode;

    private String inheritanceMapping;
    private boolean inheritanceSubtypesResolved;
    private final Set<Class<?>> inheritanceSubtypeClasses = new LinkedHashSet<>();
    private final Set<ViewMapping> inheritanceSubtypes = new LinkedHashSet<>();
    private final Set<ViewMapping> inheritanceSupertypes;
    private InheritanceViewMapping defaultInheritanceViewMapping;
    private final Set<InheritanceViewMapping> inheritanceViewMappings;

    private ManagedViewTypeImpl<?> viewType;

    public ViewMapping(Class<?> entityViewClass, Class<?> entityClass, String name, MetamodelBootContext context) {
        this.entityViewClass = entityViewClass;
        this.entityClass = entityClass;
        this.name = name;
        this.context = context;
        this.inheritanceSupertypes = new HashSet<>();
        this.inheritanceViewMappings = new HashSet<>();
    }

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

    public Integer getDefaultBatchSize() {
        return defaultBatchSize;
    }

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
    public Method getPostCreateMethod() {
        return postCreateMethod;
    }

    @Override
    public void setPostCreateMethod(Method postCreateMethod) {
        this.postCreateMethod = postCreateMethod;
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
    public boolean isValidatePersistability() {
        return validatePersistability;
    }

    @Override
    public void setValidatePersistability(boolean validatePersistability) {
        this.validatePersistability = validatePersistability;
    }

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

    public Map<String, MethodAttributeMapping> getMethodAttributes() {
        return attributes;
    }

    @Override
    public Map<String, EntityViewConstructorMapping> getConstructors() {
        return Collections.<String, EntityViewConstructorMapping>unmodifiableMap(constructorNameMap);
    }

    public void addConstructor(ConstructorMapping constructorMapping) {
        if (constructorNameMap.put(constructorMapping.getName(), constructorMapping) != null) {
            context.addError("Constructor with duplicate view constructor name '" + constructorMapping.getName() + "' found: " + constructorMapping.getConstructor());
        }
        constructors.put(new ParametersKey(constructorMapping.getConstructor().getParameterTypes()), constructorMapping);
    }

    public Map<ParametersKey, ConstructorMapping> getConstructorMappings() {
        return constructors;
    }

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

    public void setInheritanceMapping(String inheritanceMapping) {
        this.inheritanceMapping = inheritanceMapping;
    }

    public boolean isInheritanceSubtypesResolved() {
        return inheritanceSubtypesResolved;
    }

    public void setInheritanceSubtypesResolved(boolean inheritanceSubtypesResolved) {
        this.inheritanceSubtypesResolved = inheritanceSubtypesResolved;
    }

    public Set<Class<?>> getInheritanceSubtypeClasses() {
        return inheritanceSubtypeClasses;
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

    public void initializeViewMappings(MetamodelBuildingContext context, Set<Class<?>> dependencies, AttributeMapping originatingAttributeMapping) {
        Set<ViewMapping> inheritanceSubtypes;
        Set<Class<?>> subtypeClasses;

        if (inheritanceSubtypesResolved) {
            inheritanceSubtypes = new LinkedHashSet<>();
            subtypeClasses = initializeSubtypes(entityViewClass, context, dependencies, originatingAttributeMapping, inheritanceSubtypes, inheritanceSubtypeClasses, true);
        } else {
            inheritanceSubtypes = new TreeSet<>();
            subtypeClasses = initializeSubtypes(entityViewClass, context, dependencies, originatingAttributeMapping, inheritanceSubtypes, context.findSubtypes(entityViewClass), false);
        }

        for (ViewMapping subtype : inheritanceSubtypes) {
            this.inheritanceSubtypes.add(subtype);
            subtype.getInheritanceSupertypes().add(this);
        }
        for (MethodAttributeMapping attributeMapping : attributes.values()) {
            attributeMapping.initializeViewMappings(context, dependencies);
        }
        for (ConstructorMapping constructorMapping : getConstructorMappings().values()) {
            constructorMapping.initializeViewMappings(context, dependencies);
        }
        // Cleanup dependencies after constructing the view type
        dependencies.removeAll(subtypeClasses);

        inheritanceViewMappings.add(defaultInheritanceViewMapping = new InheritanceViewMapping(this, inheritanceSubtypes));
    }

    public ManagedViewTypeImpl<?> getManagedViewType(MetamodelBuildingContext context) {
        if (viewType == null) {
            if (entityClass == null) {
                context.addError("No entity class configured in view mapping for entity view class: " + entityViewClass.getName());
            }
            ManagedType<?> managedType = context.getEntityMetamodel().getManagedType(entityClass);
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
                return viewType = new ViewTypeImpl<Object>(this, context);
            } else {
                return viewType = new FlatViewTypeImpl<Object>(this, context);
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

    private Set<Class<?>> initializeSubtypes(Class<?> entityViewClass, MetamodelBuildingContext context, Set<Class<?>> dependencies, AttributeMapping originatingAttributeMapping, Set<ViewMapping> inheritanceSubtypes, Set<Class<?>> subtypeClasses, boolean explicit) {
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

            ViewMapping subtypeMapping = context.getViewMappings().get(subtypeClass);
            if (subtypeMapping == null) {
                unknownSubtype(entityViewClass, subtypeClass);
            } else {
                subtypeMapping.initializeViewMappings(context, dependencies, originatingAttributeMapping);
                inheritanceSubtypes.add(subtypeMapping);
                inheritanceSubtypes.addAll(subtypeMapping.getInheritanceSubtypes());
            }
        }

        return subtypeClasses;
    }

    public void unknownSubtype(Class<?> entityViewClass, Class<?> subviewClass) {
        context.addError("An unknown or unregistered entity view subtype type '" + subviewClass.getName() + "' is used for the entity view: " + entityViewClass.getName() + "!");
    }

    @Override
    public int compareTo(ViewMapping o) {
        return getEntityViewClass().getName().compareTo(o.getEntityViewClass().getName());
    }
}
