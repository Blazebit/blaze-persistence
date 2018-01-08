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

import javax.persistence.metamodel.ManagedType;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.0
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
    private final Set<Type<?>> persistSubtypes;
    private final Set<Type<?>> updateSubtypes;
    private final Set<Class<?>> allowedSubtypes;
    private final Map<ManagedViewType<? extends Y>, String> inheritanceSubtypes;

    @SuppressWarnings("unchecked")
    public AbstractMethodSingularAttribute(ManagedViewTypeImplementor<X> viewType, MethodAttributeMapping mapping, MetamodelBuildingContext context, int attributeIndex, int dirtyStateIndex) {
        super(viewType, mapping, attributeIndex, context);
        this.type = (Type<Y>) mapping.getType(context);
        if (mapping.isVersion()) {
            if (!(type instanceof BasicType<?>) || !(((BasicType) type).getUserType() instanceof VersionBasicUserType<?>)) {
                context.addError("Illegal non-version capable type '" + type + "' used for @Version attribute on the " + mapping.getErrorLocation() + "!");
            }
        }

        if (mapping.getUpdatable() == null) {
            // Id and version are never updatable
            if (mapping.isId() || mapping.isVersion()) {
                this.updatable = false;
            } else {
                this.updatable = determineUpdatable(type, context, false);
            }
        } else {
            this.updatable = mapping.getUpdatable();
            if (updatable) {
                if (mapping.isId()) {
                    context.addError("Illegal @UpdatableMapping along with @IdMapping on the " + mapping.getErrorLocation() + "!");
                } else if (mapping.isVersion()) {
                    context.addError("Illegal @UpdatableMapping along with @Version on the " + mapping.getErrorLocation() + "!");
                }
            }
        }
        if (mapping.getUpdatable() == null || mapping.getCascadeTypes().contains(CascadeType.AUTO)) {
            if (!declaringType.isUpdatable() && !declaringType.isCreatable()) {
                this.persistSubtypes = Collections.emptySet();
                this.updateSubtypes = Collections.emptySet();
            } else {
                this.persistSubtypes = determinePersistSubtypeSet(type, mapping.getCascadeSubtypes(context), mapping.getCascadePersistSubtypes(context), context);
                this.updateSubtypes = determineUpdateSubtypeSet(type, mapping.getCascadeSubtypes(context), mapping.getCascadeUpdateSubtypes(context), context);
            }
            this.persistCascaded = !persistSubtypes.isEmpty();
            this.updateCascaded = !updateSubtypes.isEmpty();
        } else {
            if (!declaringType.isUpdatable() && !declaringType.isCreatable()) {
                context.addError("Illegal occurrences of @UpdatableMapping for non-updatable and non-creatable view type '" + declaringType.getJavaType().getName() + "' on the " + mapping.getErrorLocation() + "!");
                this.persistCascaded = false;
                this.updateCascaded = false;
                this.persistSubtypes = Collections.emptySet();
                this.updateSubtypes = Collections.emptySet();
            } else {
                this.persistCascaded = mapping.getCascadeTypes().contains(CascadeType.PERSIST);
                this.updateCascaded = mapping.getCascadeTypes().contains(CascadeType.UPDATE);
                this.persistSubtypes = determinePersistSubtypeSet(type, mapping.getCascadeSubtypes(context), mapping.getCascadePersistSubtypes(context), context);
                this.updateSubtypes = determineUpdateSubtypeSet(type, mapping.getCascadeSubtypes(context), mapping.getCascadeUpdateSubtypes(context), context);

                if ((isUpdatable() != persistCascaded || isUpdatable() != updateCascaded)) {
                    if (type instanceof BasicType<?> && context.getEntityMetamodel().getEntity(type.getJavaType()) == null
                            || type instanceof FlatViewType<?>) {
                        context.addError("Cascading configuration for basic, embeddable or flat view type attributes is not allowed. Invalid definition found on the " + mapping.getErrorLocation() + "!");
                    }
                }
                if (!isUpdatable() && persistCascaded) {
                    context.addError("Persist cascading for non-updatable attributes is not allowed. Invalid definition found on the " + mapping.getErrorLocation() + "!");
                }
            }
        }
        this.allowedSubtypes = createAllowedSubtypesSet();
        // TODO: maybe allow to override mutability?
        this.mutable = determineMutable(type, context);
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
                this.inverseRemoveStrategy = mapping.getInverseRemoveStrategy();
                this.writableMappedByMapping = mapping.determineWritableMappedByMappings(managedType, mappedBy, context);
            }
        }
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
