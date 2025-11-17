/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.metamodel;

import com.blazebit.persistence.view.CascadeType;
import com.blazebit.persistence.view.InverseRemoveStrategy;
import com.blazebit.persistence.view.metamodel.BasicType;
import com.blazebit.persistence.view.metamodel.FlatViewType;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.PluralAttribute;
import com.blazebit.persistence.view.metamodel.Type;
import com.blazebit.persistence.view.metamodel.ViewType;

import jakarta.persistence.metamodel.ManagedType;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public abstract class AbstractMethodPluralAttribute<X, C, Y> extends AbstractMethodAttribute<X, C> implements PluralAttribute<X, C, Y> {

    private static final Logger LOG = Logger.getLogger(AbstractMethodPluralAttribute.class.getName());

    private final Type<Y> elementType;
    private final ElementCollectionType elementCollectionType;
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
    private final Set<Type<?>> readOnlySubtypes;
    private final Set<Type<?>> persistSubtypes;
    private final Set<Type<?>> updateSubtypes;
    private final Set<Class<?>> allowedSubtypes;
    private final Set<Class<?>> parentRequiringUpdateSubtypes;
    private final Set<Class<?>> parentRequiringCreateSubtypes;
    private final Map<ManagedViewType<? extends Y>, String> elementInheritanceSubtypes;
    private final boolean sorted;
    private final boolean ordered;
    private final Class<Comparator<Object>> comparatorClass;
    private final Comparator<Object> comparator;

    @SuppressWarnings("unchecked")
    public AbstractMethodPluralAttribute(ManagedViewTypeImplementor<X> viewType, MethodAttributeMapping mapping, MetamodelBuildingContext context, int attributeIndex, int dirtyStateIndex, EmbeddableOwner embeddableMapping) {
        super(viewType, mapping, attributeIndex, context, embeddableMapping);
        // Id and version can't be plural attributes
        if (mapping.isId()) {
            context.addError("Attribute annotated with @IdMapping must use a singular type. Plural type found at attribute on the " + mapping.getErrorLocation() + "!");
        }
        if (mapping.isVersion()) {
            context.addError("Attribute annotated with @Version must use a singular type. Plural type found at attribute on the " + mapping.getErrorLocation() + "!");
        }
        this.elementCollectionType = mapping.getElementCollectionType();
        this.elementType = (Type<Y>) mapping.getElementType(context, embeddableMapping);

        // The declaring type must be mutable, otherwise attributes can't be considered updatable
        if (mapping.getUpdatable() == null) {
            if (declaringType.isUpdatable() || declaringType.isCreatable()) {
                this.updatable = determineUpdatable(elementType);
            } else {
                this.updatable = false;
            }
        } else {
            this.updatable = mapping.getUpdatable();
            if (updatable && !declaringType.isUpdatable() && !declaringType.isCreatable()) {
                // Note that although orphanRemoval and delete cascading makes sense for read only views, we don't want to mix up concepts for now..
                context.addError("Illegal occurrences of @UpdatableMapping for non-updatable and non-creatable view type '" + declaringType.getJavaType().getName() + "' on the " + mapping.getErrorLocation() + "!");
            }
        }
        boolean definesDeleteCascading = mapping.getCascadeTypes().contains(CascadeType.DELETE);
        boolean allowsDeleteCascading = updatable || mapping.getCascadeTypes().contains(CascadeType.AUTO);

        this.readOnlySubtypes = (Set<Type<?>>) (Set) mapping.getReadOnlySubtypes(context, embeddableMapping);

        if (updatable) {
            Set<Type<?>> types = determinePersistSubtypeSet(elementType, mapping.getCascadeSubtypes(context, embeddableMapping), mapping.getCascadePersistSubtypes(context, embeddableMapping), context);
            this.persistCascaded = mapping.getCascadeTypes().contains(CascadeType.PERSIST)
                    || mapping.getCascadeTypes().contains(CascadeType.AUTO) && !types.isEmpty();
            if (persistCascaded) {
                this.persistSubtypes = types;
            } else {
                this.persistSubtypes = Collections.emptySet();
            }
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
            Set<Type<?>> updateCascadeAllowedSubtypes = determineUpdateSubtypeSet(elementType, mapping.getCascadeSubtypes(context, embeddableMapping), mapping.getCascadeUpdateSubtypes(context, embeddableMapping), context);
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

        this.mutable = determineMutable(elementType);

        if (!mapping.getCascadeTypes().contains(CascadeType.AUTO)) {
            if (elementType instanceof BasicType<?> && context.getEntityMetamodel().getEntity(elementType.getJavaType()) == null
                    || elementType instanceof FlatViewType<?>) {
                context.addError("Cascading configuration for basic, embeddable or flat view type attributes is not allowed. Invalid definition found on the " + mapping.getErrorLocation() + "!");
            }
        }
        if (!updatable && mapping.getCascadeTypes().contains(CascadeType.PERSIST)) {
            context.addError("Persist cascading for non-updatable attributes is not allowed. Invalid definition found on the " + mapping.getErrorLocation() + "!");
        }

        this.allowedSubtypes = createAllowedSubtypesSet();
        // We treat elements of correlated collections specially
        this.parentRequiringUpdateSubtypes = isCorrelated() ? Collections.<Class<?>>emptySet() : createParentRequiringUpdateSubtypesSet();
        this.parentRequiringCreateSubtypes = isCorrelated() ? Collections.<Class<?>>emptySet() : createParentRequiringCreateSubtypesSet();
        this.optimisticLockProtected = determineOptimisticLockProtected(mapping, context, mutable);
        this.elementInheritanceSubtypes = (Map<ManagedViewType<? extends Y>, String>) (Map<?, ?>) mapping.getElementInheritanceSubtypes(context, embeddableMapping);
        this.dirtyStateIndex = determineDirtyStateIndex(dirtyStateIndex);
        if (this.dirtyStateIndex == -1) {
            this.mappedBy = null;
            this.inverseRemoveStrategy = null;
            this.writableMappedByMapping = null;
        } else {
            ManagedType<?> managedType;
            String mappingPath;
            if (embeddableMapping == null) {
                mappingPath = this.mapping;
                managedType = context.getEntityMetamodel().getManagedType(declaringType.getEntityClass());
            } else {
                mappingPath = embeddableMapping.getEmbeddableMapping() + "." + this.mapping;
                managedType = context.getEntityMetamodel().getManagedType(embeddableMapping.getEntityClass());
            }
            this.mappedBy = mapping.determineMappedBy(managedType, mappingPath, context, embeddableMapping);
            if (this.mappedBy == null) {
                this.inverseRemoveStrategy = null;
                this.writableMappedByMapping = null;
            } else {
                this.inverseRemoveStrategy = mapping.getInverseRemoveStrategy() == null ? InverseRemoveStrategy.SET_NULL : mapping.getInverseRemoveStrategy();
                this.writableMappedByMapping = mapping.determineWritableMappedByMappings(managedType, mappedBy, context);
            }
        }

        if (this.inverseRemoveStrategy == null && mapping.getInverseRemoveStrategy() != null && this.dirtyStateIndex != -1) {
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
            String mappingExpression = getMapping();
            if (mappingExpression != null) {
                if (elementType instanceof BasicType<?> && context.getEntityMetamodel().getEntity(elementType.getJavaType()) != null || elementType instanceof ViewType<?>) {
                    // Checking this only makes sense for entities, as embeddables are orphan removed/delete cascaded anyway
                    boolean jpaOrphanRemoval = context.getJpaProvider().isOrphanRemoval(declaringType.getJpaManagedType(), mappingExpression);
                    if (jpaOrphanRemoval && !orphanRemoval) {
                        context.addError("Orphan removal configuration via @UpdatableMapping must be defined if entity attribute defines orphan removal. Invalid definition found on the  " + mapping.getErrorLocation() + "!");
                    }
                    boolean jpaDeleteCascaded = context.getJpaProvider().isDeleteCascaded(declaringType.getJpaManagedType(), mappingExpression);
                    if (jpaDeleteCascaded && !deleteCascaded) {
                        context.addError("Delete cascading configuration via @UpdatableMapping must be defined if entity attribute defines delete cascading. Invalid definition found on the  " + mapping.getErrorLocation() + "!");
                    }
                }
            }
        }

        if (context.isStrictCascadingCheck() && elementType.getMappingType() != Type.MappingType.BASIC && (updateCascaded || persistCascaded) && (declaringType.isUpdatable() || declaringType.isCreatable())) {
            Method setter = getSetterMethod();
            if (setter != null) {
                String message = "The setter '" + setter.getName() + "' for the type " + declaringType.getJavaType().getName() + " will always produce an exception due to enabled strict cascading checks and should be removed! " +
                        "Use @UpdatableMapping on the getter instead to make this attribute updatable!";
                if (context.isErrorOnInvalidPluralSetter()) {
                    context.addError(message);
                } else {
                    LOG.warning(message);
                }
            }
        }

        this.sorted = mapping.isSorted();
        
        this.ordered = mapping.getContainerBehavior() == AttributeMapping.ContainerBehavior.ORDERED;
        this.comparatorClass = (Class<Comparator<Object>>) mapping.getComparatorClass();
        this.comparator = MetamodelUtils.getComparator(comparatorClass);
    }

    private boolean determineUpdatable(Type<?> elementType) {
        // Subquery and Parameter mappings are never considered updatable
        if (getMappingType() != MappingType.BASIC && getMappingType() != MappingType.CORRELATED) {
            return false;
        }
        // Note that the same logic is implemented for subtype discovery in MethodAttributeMapping.initializeViewMappings()
        // For a plural attribute being considered updatable, there must be a setter or the view type must be creatable
        if (elementType instanceof ManagedViewType<?>) {
            ManagedViewType<?> t = (ManagedViewType<?>) elementType;
            return getSetterMethod() != null || t.isCreatable();
        }

        // We exclude entity types from this since there is no clear intent
        return getSetterMethod() != null;
    }

    @Override
    protected boolean isDisallowOwnedUpdatableSubview() {
        return false;
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
    public boolean isDeleteCascaded() {
        return deleteCascaded;
    }

    @Override
    public boolean isOrphanRemoval() {
        return orphanRemoval;
    }

    @Override
    public Set<Type<?>> getReadOnlyAllowedSubtypes() {
        return readOnlySubtypes;
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
    public Set<Class<?>> getParentRequiringUpdateSubtypes() {
        return parentRequiringUpdateSubtypes;
    }

    @Override
    public Set<Class<?>> getParentRequiringCreateSubtypes() {
        return parentRequiringCreateSubtypes;
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
    public ElementCollectionType getElementCollectionType() {
        return elementCollectionType;
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
