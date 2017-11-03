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
import com.blazebit.persistence.view.metamodel.BasicType;
import com.blazebit.persistence.view.metamodel.FlatViewType;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.PluralAttribute;
import com.blazebit.persistence.view.metamodel.Type;

import javax.persistence.metamodel.ManagedType;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public abstract class AbstractMethodPluralAttribute<X, C, Y> extends AbstractMethodAttribute<X, C> implements PluralAttribute<X, C, Y> {

    private final Type<Y> elementType;
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
    private final Map<ManagedViewType<? extends Y>, String> elementInheritanceSubtypes;
    private final boolean sorted;
    private final boolean ordered;
    private final Class<Comparator<Object>> comparatorClass;
    private final Comparator<Object> comparator;

    @SuppressWarnings("unchecked")
    public AbstractMethodPluralAttribute(ManagedViewTypeImplementor<X> viewType, MethodAttributeMapping mapping, MetamodelBuildingContext context, int attributeIndex, int dirtyStateIndex) {
        super(viewType, mapping, attributeIndex, context);
        // Id and version can't be plural attributes
        if (mapping.isId()) {
            context.addError("Attribute annotated with @IdMapping must use a singular type. Plural type found at attribute on the " + mapping.getErrorLocation() + "!");
        }
        if (mapping.isVersion()) {
            context.addError("Attribute annotated with @Version must use a singular type. Plural type found at attribute on the " + mapping.getErrorLocation() + "!");
        }
        this.elementType = (Type<Y>) mapping.getElementType(context);

        if (mapping.getUpdatable() == null) {
            // Plural attributes are only updatable if we have a setter or they are explicitly configured to be so
            this.updatable = determineUpdatable(elementType, context, true);
        } else {
            this.updatable = mapping.getUpdatable();
        }
        if (mapping.getUpdatable() == null || mapping.getCascadeTypes().contains(CascadeType.AUTO)) {
            if (!declaringType.isUpdatable()) {
                this.persistSubtypes = Collections.emptySet();
                this.updateSubtypes = Collections.emptySet();
            } else {
                // Contrary to singular attributes, plural attributes could still cascade persist events
                this.persistSubtypes = determinePersistSubtypeSet(elementType, mapping.getCascadeSubtypes(context), mapping.getCascadePersistSubtypes(context), context);
                this.updateSubtypes = determineUpdateSubtypeSet(elementType, mapping.getCascadeSubtypes(context), mapping.getCascadeUpdateSubtypes(context), context);
            }
            this.persistCascaded = !persistSubtypes.isEmpty();
            this.updateCascaded = !updateSubtypes.isEmpty();
        } else {
            if (!declaringType.isUpdatable()) {
                context.addError("Illegal occurrences of @UpdatableMapping for non-updatable view type '" + declaringType.getJavaType().getName() + "' on the " + mapping.getErrorLocation() + "!");
                this.persistCascaded = false;
                this.updateCascaded = false;
                this.persistSubtypes = Collections.emptySet();
                this.updateSubtypes = Collections.emptySet();
            } else {
                this.persistCascaded = mapping.getCascadeTypes().contains(CascadeType.PERSIST);
                this.updateCascaded = mapping.getCascadeTypes().contains(CascadeType.UPDATE);
                this.persistSubtypes = determinePersistSubtypeSet(elementType, mapping.getCascadeSubtypes(context), mapping.getCascadePersistSubtypes(context), context);
                this.updateSubtypes = determineUpdateSubtypeSet(elementType, mapping.getCascadeSubtypes(context), mapping.getCascadeUpdateSubtypes(context), context);

                if ((isUpdatable() != persistCascaded || isUpdatable() != updateCascaded)) {
                    if (elementType instanceof BasicType<?> && context.getEntityMetamodel().getEntity(elementType.getJavaType()) == null
                            || elementType instanceof FlatViewType<?>) {
                        context.addError("Cascading configuration for basic, embeddable or flat view type attributes is not allowed. Invalid definition found on the " + mapping.getErrorLocation() + "!");
                    }
                }
                if (!isUpdatable() && persistCascaded) {
                    context.addError("Persist cascading for non-updatable attributes is not allowed. Invalid definition found on the " + mapping.getErrorLocation() + "!");
                }
            }
        }
        // TODO: maybe allow to override mutability?
        this.mutable = determineMutable(elementType, context);
        this.optimisticLockProtected = determineOptimisticLockProtected(mapping, context, mutable);
        this.elementInheritanceSubtypes = (Map<ManagedViewType<? extends Y>, String>) (Map<?, ?>) mapping.getElementInheritanceSubtypes(context);
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

        this.sorted = mapping.isSorted();
        
        this.ordered = mapping.getContainerBehavior() == AttributeMapping.ContainerBehavior.ORDERED;
        this.comparatorClass = (Class<Comparator<Object>>) mapping.getComparatorClass();
        this.comparator = MetamodelUtils.getComparator(comparatorClass);
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
    public AttributeType getAttributeType() {
        return AttributeType.PLURAL;
    }

    @Override
    public Type<Y> getElementType() {
        return elementType;
    }

    @Override
    public Map<ManagedViewType<? extends Y>, String> getElementInheritanceSubtypeMappings() {
        return elementInheritanceSubtypes;
    }

    @SuppressWarnings("unchecked")
    protected Map<ManagedViewTypeImplementor<?>, String> elementInheritanceSubtypeMappings() {
        return (Map<ManagedViewTypeImplementor<?>, String>) (Map<?, ?>) elementInheritanceSubtypes;
    }

    @Override
    public boolean isCollection() {
        return true;
    }

    @Override
    public boolean isSubview() {
        return elementType.getMappingType() != Type.MappingType.BASIC;
    }

    @Override
    public boolean isSorted() {
        return sorted;
    }

    @Override
    public boolean isOrdered() {
        return ordered;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<Comparator<?>> getComparatorClass() {
        return (Class<Comparator<?>>) (Class<?>) comparatorClass;
    }

    @Override
    public Comparator<?> getComparator() {
        return comparator;
    }

}
