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
import com.blazebit.persistence.parser.expression.SyntaxErrorException;
import com.blazebit.persistence.view.AttributeFilter;
import com.blazebit.persistence.view.AttributeFilters;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.LockMode;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.MappingCorrelated;
import com.blazebit.persistence.view.MappingCorrelatedSimple;
import com.blazebit.persistence.view.MappingParameter;
import com.blazebit.persistence.view.MappingSubquery;
import com.blazebit.persistence.view.impl.UpdatableExpressionVisitor;
import com.blazebit.persistence.view.impl.collection.ListFactory;
import com.blazebit.persistence.view.impl.collection.MapFactory;
import com.blazebit.persistence.view.impl.collection.PluralObjectFactory;
import com.blazebit.persistence.view.impl.collection.SetFactory;
import com.blazebit.persistence.view.impl.collection.SortedMapFactory;
import com.blazebit.persistence.view.impl.collection.SortedSetFactory;
import com.blazebit.persistence.view.metamodel.AttributeFilterMapping;
import com.blazebit.persistence.view.metamodel.BasicType;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.Type;
import com.blazebit.reflection.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public abstract class AbstractMethodAttribute<X, Y> extends AbstractAttribute<X, Y> implements MethodAttribute<X, Y> {

    private final int attributeIndex;
    private final String name;
    private final Method javaMethod;
    private final Map<String, AttributeFilterMapping> filterMappings;

    protected AbstractMethodAttribute(ManagedViewTypeImplementor<X> viewType, MethodAttributeMapping mapping, int attributeIndex, MetamodelBuildingContext context) {
        super(viewType, mapping, context);
        this.attributeIndex = attributeIndex;
        this.name = mapping.getName();
        this.javaMethod = mapping.getMethod();

        Map<String, AttributeFilterMapping> filterMappings = new HashMap<String, AttributeFilterMapping>();
        
        AttributeFilter filterMapping = AnnotationUtils.findAnnotation(javaMethod, AttributeFilter.class);
        AttributeFilters filtersMapping = AnnotationUtils.findAnnotation(javaMethod, AttributeFilters.class);
        
        if (filterMapping != null) {
            if (filtersMapping != null) {
                context.addError("Illegal occurrences of @Filter and @Filters on the " + mapping.getErrorLocation() + "!");
            } else {
                addFilterMapping(filterMapping, filterMappings, context);
            }
        } else if (filtersMapping != null) {
            for (AttributeFilter f : filtersMapping.value()) {
                addFilterMapping(f, filterMappings, context);
            }
        }

        this.filterMappings = Collections.unmodifiableMap(filterMappings);
    }

    @Override
    protected Class<?>[] getTypeArguments() {
        return ReflectionUtils.getResolvedMethodReturnTypeArguments(getDeclaringType().getJavaType(), getJavaMethod());
    }

    protected int determineDirtyStateIndex(int dirtyStateIndex) {
        if (isUpdatable() || isMutable() && (isPersistCascaded() || isUpdateCascaded())) {
            return dirtyStateIndex;
        }

        return -1;
    }

    protected Set<Type<?>> determinePersistSubtypeSet(Type<?> superType, Set<ManagedViewTypeImplementor<?>> subtypes1, Set<ManagedViewTypeImplementor<?>> subtypes2, MetamodelBuildingContext context) {
        Class<?> superTypeClass = superType.getJavaType();
        Set<Type<?>> set = new HashSet<>(subtypes1.size() + subtypes2.size());
        if (superType.getMappingType() == Type.MappingType.BASIC && context.getEntityMetamodel().getManagedType(superType.getJavaType()) != null
                || superType.getMappingType() != Type.MappingType.BASIC && ((ManagedViewType<?>) superType).isCreatable()) {
            set.add(superType);
        }
        addToPersistSubtypeSet(set, superTypeClass, subtypes1, context, false);
        addToPersistSubtypeSet(set, superTypeClass, subtypes2, context, true);
        return Collections.unmodifiableSet(set);
    }

    protected Set<Type<?>> determineUpdateSubtypeSet(Type<?> superType, Set<ManagedViewTypeImplementor<?>> subtypes1, Set<ManagedViewTypeImplementor<?>> subtypes2, MetamodelBuildingContext context) {
        Class<?> superTypeClass = superType.getJavaType();
        Set<Type<?>> set = new HashSet<>(subtypes1.size() + subtypes2.size());
        if (superType.getMappingType() == Type.MappingType.BASIC && ((BasicType<?>) superType).getUserType().isMutable()
                || superType.getMappingType() != Type.MappingType.BASIC && ((ManagedViewType<?>) superType).isUpdatable()) {
            set.add(superType);
        }
        addToUpdateSubtypeSet(set, superTypeClass, subtypes1, context, false);
        addToUpdateSubtypeSet(set, superTypeClass, subtypes2, context, true);
        return Collections.unmodifiableSet(set);
    }

    private void addToPersistSubtypeSet(Set<Type<?>> set, Class<?> superType, Set<ManagedViewTypeImplementor<?>> subtypes, MetamodelBuildingContext context, boolean failIfNotCreatable) {
        for (ManagedViewTypeImplementor<?> type : subtypes) {
            Class<?> c = type.getJavaType();
            if (c == superType) {
                continue;
            }

            if (!superType.isAssignableFrom(c)) {
                context.addError("Invalid subtype [" + c.getName() + "] in updatable mapping is not a subtype of declared attribute element type [" + superType.getName() + "] in the " + getLocation());
            }

            if (type.isCreatable()) {
                set.add(type);
            } else if (failIfNotCreatable) {
                context.addError("Invalid subtype [" + c.getName() + "] in updatable mapping is not creatable in the " + getLocation());
            }
        }
    }

    private void addToUpdateSubtypeSet(Set<Type<?>> set, Class<?> superType, Set<ManagedViewTypeImplementor<?>> subtypes, MetamodelBuildingContext context, boolean failIfNotUpdatable) {
        for (ManagedViewTypeImplementor<?> type : subtypes) {
            Class<?> c = type.getJavaType();
            if (c == superType) {
                continue;
            }

            if (!superType.isAssignableFrom(c)) {
                context.addError("Invalid subtype [" + c.getName() + "] in updatable mapping is not a subtype of declared attribute element type [" + superType.getName() + "] in the " + getLocation());
            }

            if (type.isUpdatable()) {
                set.add(type);
            } else if (failIfNotUpdatable) {
                context.addError("Invalid subtype [" + c.getName() + "] in updatable mapping is not updatable in the " + getLocation());
            }
        }
    }

    protected boolean determineMutable(Type<?> elementType) {
        if (isUpdatable()) {
            return true;
        }
        if (elementType == null) {
            return false;
        }

        // Essentially, the checks for whether the type is updatable etc. have been done during update cascade determination already
        return isUpdateCascaded();
    }

    protected final PluralObjectFactory<? extends Collection<?>> createCollectionFactory(MetamodelBuildingContext context) {
        Class<?> pluralContainerType = getPluralContainerType(context);
        if (pluralContainerType == null) {
            return null;
        }
        if (SortedSet.class.isAssignableFrom(pluralContainerType)) {
            return new SortedSetFactory();
        } else if (Set.class.isAssignableFrom(pluralContainerType)) {
            return new SetFactory();
        } else {
            return new ListFactory();
        }
    }

    protected final PluralObjectFactory<? extends Map<?, ?>> createMapFactory(MetamodelBuildingContext context) {
        Class<?> pluralContainerType = getPluralContainerType(context);
        if (pluralContainerType == null) {
            return null;
        }
        if (SortedMap.class.isAssignableFrom(pluralContainerType)) {
            return new SortedMapFactory();
        } else {
            return new MapFactory();
        }
    }

    private Class<?> getPluralContainerType(MetamodelBuildingContext context) {
        if (isMutable() && (declaringType.isUpdatable() || declaringType.isCreatable())) {
            if (mapping == null) {
                switch (getCollectionType()) {
                    case MAP:
                        return Map.class;
                    case SET:
                        return Set.class;
                    case LIST:
                        return List.class;
                    case COLLECTION:
                        return Collection.class;
                    default:
                        return null;
                }
            }
            UpdatableExpressionVisitor visitor = new UpdatableExpressionVisitor(getDeclaringType().getEntityClass());
            try {
                context.getExpressionFactory().createPathExpression(mapping).accept(visitor);
                Map<Method, Class<?>[]> possibleTargets = visitor.getPossibleTargets();

                if (possibleTargets.size() > 1) {
                    context.addError("Multiple possible target type for the mapping in the " + getLocation() + ": " + possibleTargets);
                }
                return possibleTargets.values().iterator().next()[0];
            } catch (SyntaxErrorException ex) {
                try {
                    context.getExpressionFactory().createSimpleExpression(mapping, false);
                    // The used expression is not usable for updatable mappings
                    context.addError("Invalid mapping expression '" + mapping + "' of the " + getLocation() + " for an updatable attribute. Consider annotating the attribute with @UpdatableMapping(updatable = false) or simplify the mapping expression to a simple path expression. Encountered error: " + ex.getMessage());
                } catch (SyntaxErrorException ex2) {
                    // This is a real syntax error
                    context.addError("Syntax error in mapping expression '" + mapping + "' of the " + getLocation() + ": " + ex.getMessage());
                }
            } catch (IllegalArgumentException ex) {
                context.addError("There is an error for the " + getLocation() + ": " + ex.getMessage());
            }
        }

        return null;
    }

    protected boolean determineOptimisticLockProtected(MethodAttributeMapping mapping, MetamodelBuildingContext context, boolean mutable) {
        Boolean isOptimisticLockProtected = mapping.getOptimisticLockProtected();
        if (isOptimisticLockProtected != null) {
            // The user explicitly annotated the attribute
            if (!declaringType.isUpdatable() && !declaringType.isCreatable()) {
                context.addError("The usage of @OptimisticLock is only allowed on updatable or creatable entity view types! Invalid definition on the " + mapping.getErrorLocation());
            }
            return isOptimisticLockProtected;
        } else if (!mutable) {
            // Can only be protected when it's actually mutable
            return false;
        } else {
            // At this point, the declaring type must be updatable or creatable
            // Since the attribute can only be mutable if the declaring type is
            if (declaringType instanceof ViewTypeImpl<?>) {
                ViewTypeImpl<?> owner = (ViewTypeImpl<?>) declaringType;
                // Only the lock modes AUTO and OPTIMISTIC will make this attribute protected by that lock
                if (owner.getLockMode() == LockMode.AUTO || owner.getLockMode() == LockMode.OPTIMISTIC) {
                    // NOTE: By default, we protect every mutable attribute by an optimistic lock
                    // This is different than what JPA does, which only protects "owned" attributes
                    // but entity views are different. Since entity views are designed for a specific use case
                    // it is unlikely that it contains mutable attributes that shouldn't be lock protected.
                    // Why would attributes be mutable for a use case, but not be part of the same consistency view?
                    // They wouldn't, as that doesn't make sense. We'd rather let users occasionally see an optimistic lock exception
                    // than they facing a possible data loss just because of the way they map something in the JPA entity model.
                    // Another alternative for the user is to handle the optimistic lock exception and thanks to the change model
                    // they can even implement a merge strategy to apply changes based on their own rules
                    return true;
                } else {
                    return false;
                }
            } else {
                // Flat view types don't have a lock mode of their own, but are assumed to inherit locks from parents
                return true;
            }
        }
    }

    protected static String getAttributeName(Method getterOrSetter) {
        String name = getterOrSetter.getName();
        StringBuilder sb = new StringBuilder(name.length());
        int index = name.startsWith("is") ? 2 : 3;
        char firstAttributeNameChar = name.charAt(index);
        return sb.append(Character.toLowerCase(firstAttributeNameChar))
                .append(name, index + 1, name.length())
                .toString();
    }

    private void addFilterMapping(AttributeFilter filterMapping, Map<String, AttributeFilterMapping> filterMappings, MetamodelBuildingContext context) {
        String filterName = filterMapping.name();
        boolean errorOccurred = false;
        
        if (filterMappings.containsKey(filterName)) {
            errorOccurred = true;
            context.addError("Illegal duplicate filter name mapping '" + filterName + "' at " + getLocation());
        }

        if (!errorOccurred) {
            AttributeFilterMapping attributeFilterMapping = new AttributeFilterMappingImpl(this, filterName, filterMapping.value());
            filterMappings.put(attributeFilterMapping.getName(), attributeFilterMapping);
        }
    }

    @Override
    public String getLocation() {
        return MethodAttributeMapping.getLocation(getName(), getJavaMethod());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Method getJavaMethod() {
        return javaMethod;
    }

    @Override
    public boolean needsDirtyTracker() {
        return isUpdatable() || isUpdateCascaded() && !getUpdateCascadeAllowedSubtypes().isEmpty();
    }

    public int getAttributeIndex() {
        return attributeIndex;
    }

    @SuppressWarnings("unchecked")
    public Y getValue(Object o) {
        try {
            return (Y) javaMethod.invoke(o);
        } catch (Exception e) {
            throw new IllegalArgumentException("Couldn't get value!", e);
        }
    }

    public abstract int getDirtyStateIndex();

    public abstract Map<String, String> getWritableMappedByMappings();

    protected final Set<Class<?>> createAllowedSubtypesSet() {
        Set<Type<?>> persistAllowedSubtypes = getPersistCascadeAllowedSubtypes();
        Set<Type<?>> updateAllowedSubtypes = getUpdateCascadeAllowedSubtypes();
        Set<Class<?>> allowedSubtypes = new HashSet<>(persistAllowedSubtypes.size() + updateAllowedSubtypes.size() + 1);
        // Always add the element type to the allowed subtypes
        // This is especially important as it might be a read only type
        allowedSubtypes.add(getElementType().getJavaType());
        for (Type<?> t : persistAllowedSubtypes) {
            allowedSubtypes.add(t.getJavaType());
        }
        for (Type<?> t : updateAllowedSubtypes) {
            allowedSubtypes.add(t.getJavaType());
        }
        return Collections.unmodifiableSet(allowedSubtypes);
    }

    @Override
    public boolean isOptimizeCollectionActionsEnabled() {
        // For now, we optimize collection actions only when optimistic lock protected
        return isOptimisticLockProtected();
    }

    @Override
    public MemberType getMemberType() {
        return MemberType.METHOD;
    }

    @Override
    public AttributeFilterMapping getFilter(String filterName) {
        return filterMappings.get(filterName);
    }

    @Override
    public Set<AttributeFilterMapping> getFilters() {
        return new SetView<AttributeFilterMapping>(filterMappings.values());
    }
    
    public Map<String, AttributeFilterMapping> getFilterMappings() {
        return filterMappings;
    }

    public static String extractAttributeName(Class<?> viewType, Method m, MetamodelBootContext context) {
        String attributeName;

        // We only support bean style getters
        if (ReflectionUtils.isSetter(m)) {
            attributeName = getAttributeName(m);
            Method getter = ReflectionUtils.getGetter(viewType, attributeName);

            if (getter == null) {
                context.addError("The setter '" + m.getName() + "' from the entity view '" + viewType.getName() + "' has no corresponding getter!");
                return null;
            }

            if (m.getParameterTypes()[0] != getter.getReturnType()) {
                context.addError("The setter '" + m.getName() + "' of the class '" + viewType.getName()
                    + "' must accept an argument of the same type as it's corresponding getter returns!");
                return null;
            }

            return null;
        } else if (!ReflectionUtils.isGetter(m)) {
            context.addError("The given method '" + m.getName() + "' from the entity view '" + viewType.getName()
                + "' is no bean style getter or setter!");
            return null;
        } else {
            attributeName = getAttributeName(m);
            Method setter = ReflectionUtils.getSetter(viewType, attributeName);

            if (setter != null && setter.getParameterTypes()[0] != m.getReturnType()) {
                context.addError("The getter '" + m.getName() + "' of the class '" + viewType.getName()
                    + "' must have the same return type as it's corresponding setter accepts!");
                return null;
            }
        }

        if (m.getExceptionTypes().length > 0) {
            context.addError("The given method '" + m.getName() + "' from the entity view '" + viewType.getName() + "' must not throw an exception!");
            return null;
        }

        return attributeName;
    }

    public static Annotation getMapping(String attributeName, Method m, MetamodelBootContext context) {
        Mapping mapping = AnnotationUtils.findAnnotation(m, Mapping.class);

        if (mapping == null) {
            IdMapping idMapping = AnnotationUtils.findAnnotation(m, IdMapping.class);

            if (idMapping != null) {
                if (idMapping.value().isEmpty()) {
                    idMapping = new IdMappingLiteral(getAttributeName(m));
                }

                return idMapping;
            }
            
            MappingParameter mappingParameter = AnnotationUtils.findAnnotation(m, MappingParameter.class);

            if (mappingParameter != null) {
                if (mappingParameter.value().isEmpty()) {
                    context.addError("Illegal empty mapping parameter for the " + MethodAttributeMapping.getLocation(attributeName, m));
                }

                return mappingParameter;
            }

            MappingSubquery mappingSubquery = AnnotationUtils.findAnnotation(m, MappingSubquery.class);

            if (mappingSubquery != null) {
                return mappingSubquery;
            }

            MappingCorrelated mappingCorrelated = AnnotationUtils.findAnnotation(m, MappingCorrelated.class);

            if (mappingCorrelated != null) {
                return mappingCorrelated;
            }

            MappingCorrelatedSimple mappingCorrelatedSimple = AnnotationUtils.findAnnotation(m, MappingCorrelatedSimple.class);

            if (mappingCorrelatedSimple != null) {
                return mappingCorrelatedSimple;
            }

            // Implicit mapping
            mapping = new MappingLiteral(getAttributeName(m));
        }

        if (mapping.value().isEmpty()) {
            mapping = new MappingLiteral(getAttributeName(m), mapping);
        }

        return mapping;
    }
}
