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

import com.blazebit.persistence.parser.PathTargetResolvingExpressionVisitor;
import com.blazebit.persistence.view.AttributeFilterProvider;
import com.blazebit.persistence.view.InverseRemoveStrategy;
import com.blazebit.persistence.view.LockMode;
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

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.SingularAttribute;
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
    private final Map<String, AttributeFilterMapping<X, ?>> filterMappings;

    protected AbstractMethodAttribute(ManagedViewTypeImplementor<X> viewType, MethodAttributeMapping mapping, int attributeIndex, MetamodelBuildingContext context, EmbeddableOwner embeddableMapping) {
        super(viewType, mapping, context, embeddableMapping);
        this.attributeIndex = attributeIndex;
        this.name = mapping.getName();
        this.javaMethod = mapping.getMethod();

        if (mapping.getAttributeFilterProviders() == null) {
            this.filterMappings = Collections.emptyMap();
        } else {
            Map<String, AttributeFilterMapping<X, ?>> filterMappings = new HashMap<>();
            for (Map.Entry<String, Class<? extends AttributeFilterProvider<?>>> entry : mapping.getAttributeFilterProviders().entrySet()) {
                filterMappings.put(entry.getKey(), new AttributeFilterMappingImpl<>(this, entry.getKey(), (Class<? extends AttributeFilterProvider<Object>>) entry.getValue()));
            }

            this.filterMappings = Collections.unmodifiableMap(filterMappings);
        }
    }

    public Set<ManagedViewType<?>> getViewTypes() {
        Type<?> elementType = getElementType();
        if (elementType instanceof ManagedViewType<?>) {
            Set<Type<?>> updateCascadeAllowedSubtypes = getUpdateCascadeAllowedSubtypes();
            Set<Type<?>> persistCascadeAllowedSubtypes = getPersistCascadeAllowedSubtypes();
            Set<Type<?>> readOnlyAllowedSubtypes = getReadOnlyAllowedSubtypes();
            Set<ManagedViewType<?>> viewTypes = new HashSet<>(persistCascadeAllowedSubtypes.size() + updateCascadeAllowedSubtypes.size() + readOnlyAllowedSubtypes.size() + 1);
            viewTypes.add((ManagedViewType<?>) elementType);
            viewTypes.addAll((Collection<? extends ManagedViewType<?>>) (Collection<?>) persistCascadeAllowedSubtypes);
            viewTypes.addAll((Collection<? extends ManagedViewType<?>>) (Collection<?>) updateCascadeAllowedSubtypes);
            viewTypes.addAll((Collection<? extends ManagedViewType<?>>) (Collection<?>) readOnlyAllowedSubtypes);
            return viewTypes;
        }

        return Collections.emptySet();
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
            return SortedSetFactory.INSTANCE;
        } else if (Set.class.isAssignableFrom(pluralContainerType)) {
            return SetFactory.INSTANCE;
        } else {
            return ListFactory.INSTANCE;
        }
    }

    protected final PluralObjectFactory<? extends Map<?, ?>> createMapFactory(MetamodelBuildingContext context) {
        Class<?> pluralContainerType = getPluralContainerType(context);
        if (pluralContainerType == null) {
            return null;
        }
        if (SortedMap.class.isAssignableFrom(pluralContainerType)) {
            return SortedMapFactory.INSTANCE;
        } else {
            return MapFactory.INSTANCE;
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
            if (mappingExpression != null) {
                UpdatableExpressionVisitor visitor = new UpdatableExpressionVisitor(context.getEntityMetamodel(), getDeclaringType().getEntityClass(), isUpdatable());
                try {
                    mappingExpression.accept(visitor);
                    Map<Attribute<?, ?>, javax.persistence.metamodel.Type<?>> possibleTargets = visitor.getPossibleTargets();

                    if (possibleTargets.size() > 1) {
                        context.addError("Multiple possible target type for the mapping in the " + getLocation() + ": " + possibleTargets);
                    }
                    return possibleTargets.values().iterator().next().getJavaType();
                } catch (IllegalArgumentException ex) {
                    context.addError("There is an error for the " + getLocation() + ": " + ex.getMessage());
                }
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

    @Override
    public void checkAttribute(ManagedType<?> managedType, MetamodelBuildingContext context) {
        super.checkAttribute(managedType, context);
        if (isUpdatable() && declaringType.isUpdatable()) {
            String mappedBy = getMappedBy();
            if (mappedBy != null && getInverseRemoveStrategy() == InverseRemoveStrategy.SET_NULL) {
                Type<?> elementType = getElementType();
                ManagedType<?> elementJpaType;
                if (elementType instanceof ManagedViewTypeImplementor<?>) {
                    elementJpaType = ((ManagedViewTypeImplementor<?>) elementType).getJpaManagedType();
                } else {
                    elementJpaType = ((BasicTypeImpl<?>) elementType).getManagedType();
                }
                Map<String, String> writableMappedByMappings = getWritableMappedByMappings();
                if (writableMappedByMappings == null) {
                    Attribute<?, ?> attribute = elementJpaType.getAttribute(mappedBy);
                    // SET_NULL for plural attributes i.e. @ManyToMany is like removing just the join table entry
                    // So we just care about singular attributes here
                    if (attribute instanceof SingularAttribute<?, ?>) {
                        if (!((SingularAttribute<?, ?>) attribute).isOptional()) {
                            context.addError("Illegal use of the remove strategy SET_NULL for non-nullable mapped by attribute '" + mappedBy + "' at " + getLocation() + " Use a different strategy via @MappingInverse(removeStrategy = InverseRemoveStrategy...)");
                        }
                    }
                } else {
                    PathTargetResolvingExpressionVisitor visitor = new PathTargetResolvingExpressionVisitor(context.getEntityMetamodel(), elementJpaType, null);
                    for (String value : writableMappedByMappings.values()) {
                        visitor.reset(elementJpaType);
                        context.getTypeValidationExpressionFactory().createPathExpression(value).accept(visitor);
                        Map<Attribute<?, ?>, javax.persistence.metamodel.Type<?>> possibleTargets = visitor.getPossibleTargets();
                        if (possibleTargets.size() > 1) {
                            context.addError("Multiple possible target type for the mapping in the " + getLocation() + ": " + possibleTargets);
                        }
                        Attribute<?, ?> attribute = possibleTargets.keySet().iterator().next();
                        if (attribute instanceof SingularAttribute<?, ?>) {
                            if (!((SingularAttribute<?, ?>) attribute).isOptional()) {
                                context.addError("Illegal use of the remove strategy SET_NULL for non-nullable mapped by attribute '" + mappedBy + "' because writable mapping '" + value + "' is non-optional at " + getLocation() + " Use a different strategy via @MappingInverse(removeStrategy = InverseRemoveStrategy...)");
                            }
                        }
                    }

                }
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

    @Override
    public boolean hasDirtyStateIndex() {
        return getDirtyStateIndex() != -1;
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

    public boolean isUpdatableOnly() {
        return hasDirtyStateIndex() && !isPersistCascaded() && !isUpdateCascaded();
    }

    public abstract Map<String, String> getWritableMappedByMappings();

    protected final Set<Class<?>> createAllowedSubtypesSet() {
        Set<Type<?>> readOnlyAllowedSubtypes = getReadOnlyAllowedSubtypes();
        Set<Type<?>> persistAllowedSubtypes = getPersistCascadeAllowedSubtypes();
        Set<Type<?>> updateAllowedSubtypes = getUpdateCascadeAllowedSubtypes();
        Set<Class<?>> allowedSubtypes = new HashSet<>(readOnlyAllowedSubtypes.size() + persistAllowedSubtypes.size() + updateAllowedSubtypes.size());
        for (Type<?> t : readOnlyAllowedSubtypes) {
            allowedSubtypes.add(t.getJavaType());
        }
        for (Type<?> t : persistAllowedSubtypes) {
            allowedSubtypes.add(t.getJavaType());
        }
        for (Type<?> t : updateAllowedSubtypes) {
            allowedSubtypes.add(t.getJavaType());
        }
        return Collections.unmodifiableSet(allowedSubtypes);
    }

    protected final Set<Class<?>> createParentRequiringUpdateSubtypesSet() {
        Set<Type<?>> readOnlyAllowedSubtypes = getReadOnlyAllowedSubtypes();
        Set<Type<?>> persistAllowedSubtypes = getPersistCascadeAllowedSubtypes();
        Set<Type<?>> updateAllowedSubtypes = getUpdateCascadeAllowedSubtypes();
        Set<Class<?>> allowedSubtypes = new HashSet<>(readOnlyAllowedSubtypes.size());
        for (Type<?> t : readOnlyAllowedSubtypes) {
            if (t instanceof ManagedViewTypeImplementor<?>) {
                ManagedViewTypeImplementor<?> viewType = (ManagedViewTypeImplementor<?>) t;
                if (viewType.isUpdatable() && !updateAllowedSubtypes.contains(t) || viewType.isCreatable() && !persistAllowedSubtypes.contains(t)) {
                    allowedSubtypes.add(t.getJavaType());
                }
            }
        }
        return Collections.unmodifiableSet(allowedSubtypes);
    }

    protected final Set<Class<?>> createParentRequiringCreateSubtypesSet() {
        Set<Type<?>> readOnlyAllowedSubtypes = getReadOnlyAllowedSubtypes();
        Set<Type<?>> persistAllowedSubtypes = getPersistCascadeAllowedSubtypes();
        Set<Type<?>> updateAllowedSubtypes = getUpdateCascadeAllowedSubtypes();
        Set<Class<?>> allowedSubtypes = new HashSet<>(readOnlyAllowedSubtypes.size());
        for (Type<?> t : readOnlyAllowedSubtypes) {
            if (t instanceof ManagedViewTypeImplementor<?>) {
                ManagedViewTypeImplementor<?> viewType = (ManagedViewTypeImplementor<?>) t;
                if (viewType.isUpdatable() && !updateAllowedSubtypes.contains(t) || viewType.isCreatable() && !persistAllowedSubtypes.contains(t)) {
                    allowedSubtypes.add(t.getJavaType());
                }
            }
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
    public AttributeFilterMapping<X, ?> getFilter(String filterName) {
        return filterMappings.get(filterName);
    }

    @Override
    public Set<AttributeFilterMapping<X, ?>> getFilters() {
        return new SetView<AttributeFilterMapping<X, ?>>(filterMappings.values());
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

            if (ReflectionUtils.getResolvedMethodParameterTypes(viewType, m)[0] != ReflectionUtils.getResolvedMethodReturnType(viewType, getter)) {
                context.addError("The setter '" + m.getName() + "' of the class '" + viewType.getName()
                    + "' must accept an argument of the same type as it's corresponding getter returns!");
                return null;
            }

            for (Annotation annotation : m.getAnnotations()) {
                if (annotation.annotationType().getPackage().getName().startsWith("com.blazebit.persistence.view")) {
                    context.addError("The setter '" + m.getName() + "' of the class '" + viewType.getName()
                            + "' has entity view annotations, but these annotations must be on the getter only!");
                    break;
                }
            }

            return null;
        } else if (!ReflectionUtils.isGetter(m)) {
            context.addError("The given method '" + m.getName() + "' from the entity view '" + viewType.getName()
                + "' is no bean style getter or setter!");
            return null;
        } else {
            attributeName = getAttributeName(m);
            Method setter = ReflectionUtils.getSetter(viewType, attributeName);

            if (setter != null && ReflectionUtils.getResolvedMethodParameterTypes(viewType, setter)[0] != ReflectionUtils.getResolvedMethodReturnType(viewType, m)) {
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
}
