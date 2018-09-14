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

import com.blazebit.persistence.view.CascadeType;
import com.blazebit.persistence.view.InverseRemoveStrategy;
import com.blazebit.persistence.view.impl.collection.CollectionInstantiator;
import com.blazebit.persistence.view.impl.collection.MapInstantiator;
import com.blazebit.persistence.view.metamodel.BasicType;
import com.blazebit.persistence.view.metamodel.FlatViewType;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.PluralAttribute;
import com.blazebit.persistence.view.metamodel.SingularAttribute;
import com.blazebit.persistence.view.metamodel.Type;
import com.blazebit.persistence.view.spi.type.VersionBasicUserType;
import com.blazebit.reflection.ReflectionUtils;

import javax.persistence.metamodel.ManagedType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public abstract class AbstractMethodSingularAttribute<X, Y> extends AbstractMethodAttribute<X, Y> implements SingularAttribute<X, Y> {

    private final Type<Y> type;
    private final int dirtyStateIndex;
    private final String mappedBy;
    private final Map<String, String> writableMappedByMapping;
    private final InverseRemoveStrategy inverseRemoveStrategy;
    private final boolean updatable;
    private final boolean mutable;
    private final boolean optimisticLockProtected;
    private final boolean persistCascaded;
    private final boolean updateCascaded;
    private final boolean deleteCascaded;
    private final boolean orphanRemoval;
    private final Set<Type<?>> persistSubtypes;
    private final Set<Type<?>> updateSubtypes;
    private final Set<Class<?>> allowedSubtypes;
    private final Map<ManagedViewType<? extends Y>, String> inheritanceSubtypes;

    @SuppressWarnings("unchecked")
    public AbstractMethodSingularAttribute(ManagedViewTypeImplementor<X> viewType, MethodAttributeMapping mapping, MetamodelBuildingContext context, int attributeIndex, int dirtyStateIndex) {
        super(viewType, mapping, attributeIndex, context);
        this.type = (Type<Y>) mapping.getType(context);
        if (mapping.isVersion()) {
            if (!(type instanceof BasicType<?>) || !(((BasicType<?>) type).getUserType() instanceof VersionBasicUserType<?>)) {
                context.addError("Illegal non-version capable type '" + type + "' used for @Version attribute on the " + mapping.getErrorLocation() + "!");
            }
        }

        // The declaring type must be mutable, otherwise attributes can't be considered updatable
        if (mapping.getUpdatable() == null) {
            // Id and version are never updatable
            if (mapping.isId() || mapping.isVersion() || !declaringType.isUpdatable() && !declaringType.isCreatable()) {
                this.updatable = false;
            } else {
                this.updatable = determineUpdatable(type);
            }
        } else {
            this.updatable = mapping.getUpdatable();
            if (updatable) {
                if (mapping.isId()) {
                    context.addError("Illegal @UpdatableMapping along with @IdMapping on the " + mapping.getErrorLocation() + "!");
                } else if (mapping.isVersion()) {
                    context.addError("Illegal @UpdatableMapping along with @Version on the " + mapping.getErrorLocation() + "!");
                }
                if (!declaringType.isUpdatable() && !declaringType.isCreatable()) {
                    // Note that although orphanRemoval and delete cascading makes sense for read only views, we don't want to mix up concepts for now..
                    context.addError("Illegal occurrences of @UpdatableMapping for non-updatable and non-creatable view type '" + declaringType.getJavaType().getName() + "' on the " + mapping.getErrorLocation() + "!");
                }
            }
        }
        boolean definesDeleteCascading = mapping.getCascadeTypes().contains(CascadeType.DELETE);
        boolean allowsDeleteCascading = updatable || mapping.getCascadeTypes().contains(CascadeType.AUTO);

        if (updatable) {
            this.persistSubtypes = determinePersistSubtypeSet(type, mapping.getCascadeSubtypes(context), mapping.getCascadePersistSubtypes(context), context);
            this.persistCascaded = mapping.getCascadeTypes().contains(CascadeType.PERSIST)
                    || mapping.getCascadeTypes().contains(CascadeType.AUTO) && !persistSubtypes.isEmpty();
        } else {
            this.persistCascaded = false;
            this.persistSubtypes = Collections.emptySet();
        }

        // The declaring type must be mutable, otherwise attributes can't have cascading
        if (mapping.isId() || mapping.isVersion() || !declaringType.isUpdatable() && !declaringType.isCreatable()) {
            this.updateCascaded = false;
            this.updateSubtypes = Collections.emptySet();
        } else {
            // TODO: maybe allow to override mutability?
            Set<Type<?>> updateCascadeAllowedSubtypes = determineUpdateSubtypeSet(type, mapping.getCascadeSubtypes(context), mapping.getCascadeUpdateSubtypes(context), context);
            boolean updateCascaded = mapping.getCascadeTypes().contains(CascadeType.UPDATE)
                    || mapping.getCascadeTypes().contains(CascadeType.AUTO) && !updateCascadeAllowedSubtypes.isEmpty();
            if (updateCascaded) {
                this.updateCascaded = true;
                this.updateSubtypes = updateCascadeAllowedSubtypes;
            } else {
                this.updateCascaded = false;
                this.updateSubtypes = Collections.emptySet();
            }
        }

        this.mutable = determineMutable(type);

        if (!mapping.getCascadeTypes().contains(CascadeType.AUTO)) {
            if (type instanceof BasicType<?> && context.getEntityMetamodel().getEntity(type.getJavaType()) == null
                    || type instanceof FlatViewType<?>) {
                context.addError("Cascading configuration for basic, embeddable or flat view type attributes is not allowed. Invalid definition found on the " + mapping.getErrorLocation() + "!");
            }
        }
        if (!updatable && mapping.getCascadeTypes().contains(CascadeType.PERSIST)) {
            context.addError("Persist cascading for non-updatable attributes is not allowed. Invalid definition found on the " + mapping.getErrorLocation() + "!");
        }

        this.allowedSubtypes = createAllowedSubtypesSet();
        this.optimisticLockProtected = determineOptimisticLockProtected(mapping, context, mutable);
        this.inheritanceSubtypes = (Map<ManagedViewType<? extends Y>, String>) (Map<?, ?>) mapping.getInheritanceSubtypes(context);
        this.dirtyStateIndex = determineDirtyStateIndex(dirtyStateIndex);
        if (this.dirtyStateIndex == -1) {
            this.mappedBy = null;
            this.inverseRemoveStrategy = null;
            this.writableMappedByMapping = null;
        } else {
            ManagedType<?> managedType = context.getEntityMetamodel().getManagedType(declaringType.getEntityClass());
            this.mappedBy = mapping.determineMappedBy(managedType, this.mapping, context);
            if (this.mappedBy == null) {
                this.inverseRemoveStrategy = null;
                this.writableMappedByMapping = null;
            } else {
                this.inverseRemoveStrategy = mapping.getInverseRemoveStrategy() == null ? InverseRemoveStrategy.SET_NULL : mapping.getInverseRemoveStrategy();
                this.writableMappedByMapping = mapping.determineWritableMappedByMappings(managedType, mappedBy, context);
            }
        }

        if (this.inverseRemoveStrategy == null && mapping.getInverseRemoveStrategy() != null) {
            context.addError("Found use of @MappingInverse on attribute that isn't an inverse relationship. Invalid definition found on the " + mapping.getErrorLocation() + "!");
        }

        if (Boolean.FALSE.equals(mapping.getOrphanRemoval())) {
            this.orphanRemoval = false;
        } else {
            // Determine orphan removal based on remove strategy
            this.orphanRemoval = inverseRemoveStrategy == InverseRemoveStrategy.REMOVE || Boolean.TRUE.equals(mapping.getOrphanRemoval());
        }

        // Orphan removal implies delete cascading, inverse attributes also always do delete cascading
        this.deleteCascaded = orphanRemoval || definesDeleteCascading || allowsDeleteCascading && inverseRemoveStrategy != null;

        if (updatable) {
            boolean jpaOrphanRemoval = context.getJpaProvider().isOrphanRemoval(declaringType.getJpaManagedType(), getMapping());
            if (jpaOrphanRemoval && !orphanRemoval) {
                context.addError("Orphan removal configuration via @UpdatableMapping must be defined if entity attribute defines orphan removal. Invalid definition found on the  " + mapping.getErrorLocation() + "!");
            }
            boolean jpaDeleteCascaded = context.getJpaProvider().isDeleteCascaded(declaringType.getJpaManagedType(), getMapping());
            if (jpaDeleteCascaded && !deleteCascaded) {
                context.addError("Delete cascading configuration via @UpdatableMapping must be defined if entity attribute defines delete cascading. Invalid definition found on the  " + mapping.getErrorLocation() + "!");
            }
        }
    }

    private boolean determineUpdatable(Type<?> elementType) {
        // Subquery and Parameter mappings are never considered updatable
        if (getMappingType() != MappingType.BASIC && getMappingType() != MappingType.CORRELATED) {
            return false;
        }

        Method setter = ReflectionUtils.getSetter(getDeclaringType().getJavaType(), getName());
        boolean hasSetter = setter != null && (setter.getModifiers() & Modifier.ABSTRACT) != 0;

        // For a singular attribute being considered updatable, there must be a setter
        // If the type is a flat view, it must be updatable or creatable and have a setter
        if (elementType instanceof FlatViewType<?>) {
            FlatViewType<?> t = (FlatViewType<?>) elementType;
            return t.isUpdatable() || hasSetter && t.isCreatable();
        }

        // We exclude entity types from this since there is no clear intent
        return hasSetter;
    }

    @Override
    public int getDirtyStateIndex() {
        return dirtyStateIndex;
    }

    @Override
    public Map<String, String> getWritableMappedByMappings() {
        return writableMappedByMapping;
    }

    @Override
    public String getMappedBy() {
        return mappedBy;
    }

    @Override
    public InverseRemoveStrategy getInverseRemoveStrategy() {
        return inverseRemoveStrategy;
    }

    @Override
    public boolean isCollection() {
        return false;
    }

    @Override
    public boolean isIndexed() {
        return false;
    }

    @Override
    protected boolean isForcedUnique() {
        return false;
    }

    @Override
    protected PluralAttribute.CollectionType getCollectionType() {
        throw new UnsupportedOperationException("Singular attribute");
    }

    @Override
    public CollectionInstantiator getCollectionInstantiator() {
        throw new UnsupportedOperationException("Singular attribute");
    }

    @Override
    public MapInstantiator getMapInstantiator() {
        throw new UnsupportedOperationException("Singular attribute");
    }

    @Override
    public AttributeType getAttributeType() {
        return AttributeType.SINGULAR;
    }

    @Override
    public boolean isUpdatable() {
        return updatable;
    }

    @Override
    public boolean isMutable() {
        return mutable;
    }

    @Override
    public boolean isOptimisticLockProtected() {
        return optimisticLockProtected;
    }

    @Override
    public boolean isPersistCascaded() {
        return persistCascaded;
    }

    @Override
    public boolean isUpdateCascaded() {
        return updateCascaded;
    }

    @Override
    public boolean isDeleteCascaded() {
        return deleteCascaded;
    }

    @Override
    public boolean isOrphanRemoval() {
        return orphanRemoval;
    }

    @Override
    public Set<Type<?>> getPersistCascadeAllowedSubtypes() {
        return persistSubtypes;
    }

    @Override
    public Set<Type<?>> getUpdateCascadeAllowedSubtypes() {
        return updateSubtypes;
    }

    @Override
    public Set<Class<?>> getAllowedSubtypes() {
        return allowedSubtypes;
    }

    @Override
    public Type<Y> getType() {
        return type;
    }

    @Override
    protected Type<?> getElementType() {
        return type;
    }

    @Override
    public Map<ManagedViewType<? extends Y>, String> getInheritanceSubtypeMappings() {
        return inheritanceSubtypes;
    }

    @SuppressWarnings("unchecked")
    protected Map<ManagedViewTypeImplementor<?>, String> elementInheritanceSubtypeMappings() {
        return (Map<ManagedViewTypeImplementor<?>, String>) (Map<?, ?>) inheritanceSubtypes;
    }

    protected Type<?> getKeyType() {
        return null;
    }

    protected Map<ManagedViewTypeImplementor<?>, String> keyInheritanceSubtypeMappings() {
        return null;
    }

    protected boolean isKeySubview() {
        return false;
    }

    @Override
    public boolean isSubview() {
        return type.getMappingType() != Type.MappingType.BASIC;
    }
}
