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

package com.blazebit.persistence.view.impl.update.flush;

import com.blazebit.persistence.view.InverseRemoveStrategy;
import com.blazebit.persistence.view.OptimisticLockException;
import com.blazebit.persistence.view.impl.accessor.AttributeAccessor;
import com.blazebit.persistence.view.impl.accessor.InitialValueAttributeAccessor;
import com.blazebit.persistence.view.impl.change.DirtyChecker;
import com.blazebit.persistence.view.impl.entity.ViewToEntityMapper;
import com.blazebit.persistence.view.impl.proxy.DirtyStateTrackable;
import com.blazebit.persistence.view.impl.proxy.MutableStateTrackable;
import com.blazebit.persistence.view.impl.update.EntityViewUpdater;
import com.blazebit.persistence.view.impl.update.UpdateContext;
import com.blazebit.persistence.view.impl.update.UpdateQueryFactory;
import com.blazebit.persistence.view.spi.type.EntityViewProxy;
import com.blazebit.persistence.view.spi.type.TypeConverter;

import javax.persistence.Query;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class SubviewAttributeFlusher<E, V> extends AttributeFetchGraphNode<SubviewAttributeFlusher<E, V>> implements DirtyAttributeFlusher<SubviewAttributeFlusher<E, V>, E, V> {

    private final boolean optimisticLockProtected;
    private final boolean updatable;
    private final boolean cascadeDelete;
    private final boolean orphanRemoval;
    private final boolean viewOnlyDeleteCascaded;
    private final String[] elementIdAttributePaths;
    private final String parameterName;
    private final boolean passThrough;
    private final TypeConverter<Object, Object> converter;
    private final AttributeAccessor entityAttributeAccessor;
    private final InitialValueAttributeAccessor viewAttributeAccessor;
    private final AttributeAccessor subviewIdAccessor;
    private final ViewToEntityMapper viewToEntityMapper;
    private final InverseFlusher<E> inverseFlusher;
    private final InverseCollectionElementAttributeFlusher.Strategy inverseRemoveStrategy;
    private final V value;
    private final boolean update;
    private final boolean supportElementIdQueryFlush;
    private final ViewFlushOperation flushOperation;
    private final DirtyAttributeFlusher<?, E, V> nestedFlusher;

    @SuppressWarnings("unchecked")
    public SubviewAttributeFlusher(String attributeName, String mapping, boolean optimisticLockProtected, boolean updatable, boolean cascadeDelete, boolean orphanRemoval, boolean viewOnlyDeleteCascaded, TypeConverter<?, ?> converter, boolean fetch, String[] elementIdAttributePaths, String parameterName, boolean passThrough,
                                   boolean isEmbedded, AttributeAccessor entityAttributeAccessor, InitialValueAttributeAccessor viewAttributeAccessor, AttributeAccessor subviewIdAccessor, ViewToEntityMapper viewToEntityMapper, InverseFlusher<E> inverseFlusher, InverseRemoveStrategy inverseRemoveStrategy) {
        super(attributeName, mapping, fetch, viewToEntityMapper.getFullGraphNode());
        this.optimisticLockProtected = optimisticLockProtected;
        this.updatable = updatable;
        this.cascadeDelete = cascadeDelete;
        this.orphanRemoval = orphanRemoval;
        this.viewOnlyDeleteCascaded = viewOnlyDeleteCascaded;
        this.converter = (TypeConverter<Object, Object>) converter;
        this.elementIdAttributePaths = elementIdAttributePaths;
        this.inverseFlusher = inverseFlusher;
        this.inverseRemoveStrategy = InverseCollectionElementAttributeFlusher.Strategy.of(inverseRemoveStrategy);
        this.parameterName = parameterName;
        this.passThrough = passThrough;
        this.entityAttributeAccessor = entityAttributeAccessor;
        this.viewAttributeAccessor = viewAttributeAccessor;
        this.subviewIdAccessor = subviewIdAccessor;
        this.viewToEntityMapper = viewToEntityMapper;
        this.value = null;
        this.update = updatable && entityAttributeAccessor != null;
        // When the subview is embedded through a flat view, it seems hibernate is unable to set the association in an update query
        // like `UPDATE Entity e SET e.embeddable.association = :param` as it will report an unexpected AND AST node, so we need to flush element ids
        // The effect is, that we can't support natural ids in such a scenario
        this.supportElementIdQueryFlush = isEmbedded || viewToEntityMapper.getIdFlusher() == null;
        this.flushOperation = null;
        this.nestedFlusher = null;
    }

    private SubviewAttributeFlusher(SubviewAttributeFlusher original, boolean fetch, V value, boolean update, DirtyAttributeFlusher<?, E, V> nestedFlusher) {
        super(original.attributeName, original.mapping, fetch, nestedFlusher);
        this.optimisticLockProtected = original.optimisticLockProtected;
        this.updatable = original.updatable;
        this.cascadeDelete = original.cascadeDelete;
        this.orphanRemoval = original.orphanRemoval;
        this.viewOnlyDeleteCascaded = original.viewOnlyDeleteCascaded;
        this.converter = original.converter;
        this.elementIdAttributePaths = original.elementIdAttributePaths;
        this.parameterName = original.parameterName;
        this.passThrough = original.passThrough;
        this.entityAttributeAccessor = original.entityAttributeAccessor;
        this.viewAttributeAccessor = original.viewAttributeAccessor;
        this.subviewIdAccessor = original.subviewIdAccessor;
        this.viewToEntityMapper = original.viewToEntityMapper;
        this.inverseFlusher = original.inverseFlusher;
        this.inverseRemoveStrategy = original.inverseRemoveStrategy;
        this.supportElementIdQueryFlush = original.supportElementIdQueryFlush;
        this.value = value;
        this.update = update;
        this.flushOperation = nestedFlusher == null ? ViewFlushOperation.NONE : ViewFlushOperation.CASCADE;
        this.nestedFlusher = nestedFlusher;
    }

    @Override
    protected FetchGraphNode<?> getNestedGraphNode() {
        return nestedFlusher != null ? nestedFlusher : viewToEntityMapper.getFullGraphNode();
    }

    private DirtyAttributeFlusher<?, Object, Object> getElementIdFlusher() {
        return (DirtyAttributeFlusher<?, Object, Object>) viewToEntityMapper.getIdFlusher();
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    private static enum ViewFlushOperation {
        NONE,
        CASCADE;
    }

    @Override
    public V cloneDeep(Object view, V oldValue, V newValue) {
        if (updatable) {
            return newValue;
        } else {
            return oldValue;
        }
    }

    @Override
    public Object getNewInitialValue(UpdateContext context, V clonedValue, V currentValue) {
        return currentValue;
    }

    @Override
    public boolean appendUpdateQueryFragment(UpdateContext context, StringBuilder sb, String mappingPrefix, String parameterPrefix, String separator) {
        if (update && (updatable || isPassThrough()) && inverseFlusher == null) {
            if (supportElementIdQueryFlush) {
                if (mappingPrefix == null) {
                    return getElementIdFlusher().appendUpdateQueryFragment(context, sb, mapping + ".", parameterName + "_", separator);
                } else {
                    return getElementIdFlusher().appendUpdateQueryFragment(context, sb, mappingPrefix + mapping + ".", parameterPrefix + parameterName + "_", separator);
                }
            } else {
                if (mappingPrefix == null) {
                    sb.append(mapping);
                    sb.append(" = :");
                    sb.append(parameterName);
                } else {
                    sb.append(mappingPrefix).append(mapping);
                    sb.append(" = :");
                    sb.append(parameterPrefix).append(parameterName);
                }
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean supportsQueryFlush() {
        return true;
    }

    @Override
    public boolean loadForEntityFlush() {
        return true;
    }

    @Override
    public Query flushQuery(UpdateContext context, String parameterPrefix, UpdateQueryFactory queryFactory, Query query, Object ownerView, Object view, V value, UnmappedOwnerAwareDeleter ownerAwareDeleter) {
        V finalValue;
        if (flushOperation == null) {
            finalValue = value;
        } else {
            finalValue = this.value;
        }
        finalValue = getConvertedValue(finalValue);
        boolean doUpdate = updatable || isPassThrough();
        // Orphan removal is only valid for entity types
        if (doUpdate && orphanRemoval) {
            Object oldValue = viewAttributeAccessor.getInitialValue(view);
            if (oldValue != null && !Objects.equals(oldValue, finalValue)) {
                context.getOrphanRemovalDeleters().add(new PostFlushViewToEntityMapperDeleter(viewToEntityMapper, oldValue));
            }
        }
        if (doUpdate && inverseFlusher != null) {
            Object oldValue = viewAttributeAccessor.getInitialValue(view);
            if (oldValue != null && !Objects.equals(oldValue, finalValue)) {
                if (inverseRemoveStrategy == InverseCollectionElementAttributeFlusher.Strategy.SET_NULL) {
                    inverseFlusher.flushQuerySetElement(context, oldValue, view, null, null, null);
                } else {
                    inverseFlusher.removeElement(context, null, oldValue);
                }
            }
        }
        if (flushOperation != null) {
            if (flushOperation == ViewFlushOperation.CASCADE) {
                if (update && inverseFlusher != null) {
                    if (finalValue != null) {
                        inverseFlusher.flushQuerySetElement(context, finalValue, view, view, null, (DirtyAttributeFlusher<?, E, Object>) (DirtyAttributeFlusher<?, ?, ?>) nestedFlusher);
                    }
                } else {
                    int orphanRemovalStartIndex = context.getOrphanRemovalDeleters().size();
                    try {
                        Query q = nestedFlusher.flushQuery(context, parameterPrefix, viewToEntityMapper, null, finalValue, null, finalValue, ownerAwareDeleter);
                        if (q != null) {
                            int updated = q.executeUpdate();

                            if (updated != 1) {
                                throw new OptimisticLockException(null, finalValue);
                            }
                        }
                    } finally {
                        if (finalValue instanceof MutableStateTrackable) {
                            context.getInitialStateResetter().addUpdatedView((MutableStateTrackable) finalValue);
                        }
                    }
                    context.removeOrphans(orphanRemovalStartIndex);
                }
            } else if (update && inverseFlusher != null && finalValue != null) {
                inverseFlusher.flushQuerySetElement(context, finalValue, view, view, null, null);
            }

            Object v = viewToEntityMapper.applyToEntity(context, null, finalValue);
            if (query != null && update) {
                if (inverseFlusher == null) {
                    Object realValue = v == null ? null : subviewIdAccessor.getValue(finalValue);
                    if (supportElementIdQueryFlush) {
                        if (parameterPrefix == null) {
                            getElementIdFlusher().flushQuery(context, parameterName + "_", queryFactory, query, finalValue, null, realValue, ownerAwareDeleter);
                        } else {
                            getElementIdFlusher().flushQuery(context, parameterPrefix + parameterName + "_", queryFactory, query, finalValue, null, realValue, ownerAwareDeleter);
                        }
                    } else {
                        if (parameterPrefix == null) {
                            query.setParameter(parameterName, v);
                        } else {
                            query.setParameter(parameterPrefix + parameterName, v);
                        }
                    }
                }
            }

            // If the view is creatable, the CompositeAttributeFlusher re-maps the view object and puts the new object to the mutable state array
            Object newValue = viewAttributeAccessor.getMutableStateValue(view);
            if (finalValue != newValue) {
                viewAttributeAccessor.setValue(view, newValue);
            }
            return query;
        }
        if (updatable || isPassThrough()) {
            if (nestedFlusher != null && nestedFlusher != viewToEntityMapper.getFullGraphNode()) {
                if (update && inverseFlusher != null) {
                    if (finalValue != null) {
                        inverseFlusher.flushQuerySetElement(context, finalValue, view, view, null, (DirtyAttributeFlusher<?, E, Object>) (DirtyAttributeFlusher<?, ?, ?>) nestedFlusher);
                    }
                } else {
                    int orphanRemovalStartIndex = context.getOrphanRemovalDeleters().size();
                    try {
                        Query q = nestedFlusher.flushQuery(context, parameterPrefix, viewToEntityMapper, null, value, null, value, ownerAwareDeleter);
                        if (q != null) {
                            int updated = q.executeUpdate();

                            if (updated != 1) {
                                throw new OptimisticLockException(null, value);
                            }
                        }
                    } finally {
                        if (value instanceof MutableStateTrackable) {
                            context.getInitialStateResetter().addUpdatedView((MutableStateTrackable) value);
                        }
                    }
                    context.removeOrphans(orphanRemovalStartIndex);
                }
            }  else if (update && inverseFlusher != null && finalValue != null) {
                inverseFlusher.flushQuerySetElement(context, finalValue, view, view, null, null);
            }
            Object v = viewToEntityMapper.applyToEntity(context, null, value);
            if (query != null && update) {
                if (inverseFlusher == null) {
                    Object realValue = v == null ? null : subviewIdAccessor.getValue(value);
                    if (supportElementIdQueryFlush) {
                        if (parameterPrefix == null) {
                            getElementIdFlusher().flushQuery(context, parameterName + "_", queryFactory, query, value, null, realValue, ownerAwareDeleter);
                        } else {
                            getElementIdFlusher().flushQuery(context, parameterPrefix + parameterName + "_", queryFactory, query, value, null, realValue, ownerAwareDeleter);
                        }
                    } else {
                        if (parameterPrefix == null) {
                            query.setParameter(parameterName, v);
                        } else {
                            query.setParameter(parameterPrefix + parameterName, v);
                        }
                    }
                }
            }
            // If the view is creatable, the CompositeAttributeFlusher re-maps the view object and puts the new object to the mutable state array
            Object newValue = viewAttributeAccessor.getMutableStateValue(view);
            if (value != newValue) {
                viewAttributeAccessor.setValue(view, newValue);
            }
        } else {
            V realValue;
            if (view == null) {
                realValue = value;
            } else {
                realValue = (V) viewAttributeAccessor.getValue(view);
            }
            if (nestedFlusher != null && nestedFlusher != viewToEntityMapper.getFullGraphNode()) {
                int orphanRemovalStartIndex = context.getOrphanRemovalDeleters().size();
                try {
                    Query q = nestedFlusher.flushQuery(context, parameterPrefix, viewToEntityMapper, null, realValue, null, realValue, ownerAwareDeleter);
                    if (q != null) {
                        int updated = q.executeUpdate();

                        if (updated != 1) {
                            throw new OptimisticLockException(null, realValue);
                        }
                    }
                } finally {
                    if (realValue instanceof MutableStateTrackable) {
                        context.getInitialStateResetter().addUpdatedView((MutableStateTrackable) realValue);
                    }
                }
                context.removeOrphans(orphanRemovalStartIndex);
            } else {
                if (realValue != null && (value == realValue || viewIdEqual(value, realValue))) {
                    viewToEntityMapper.applyToEntity(context, null, realValue);
                }
            }
            if (view != null && value != realValue && viewIdEqual(value, realValue)) {
                viewAttributeAccessor.setValue(view, realValue);
            }
        }
        return query;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean flushEntity(UpdateContext context, E entity, Object ownerView, Object view, V value, Runnable postReplaceListener) {
        V finalValue;
        if (flushOperation != null) {
            finalValue = this.value;
        } else {
            finalValue = value;
        }
        finalValue = getConvertedValue(finalValue);
        boolean doUpdate = updatable || isPassThrough();
        // Orphan removal is only valid for entity types
        if (doUpdate && orphanRemoval) {
            Object oldValue = viewAttributeAccessor.getInitialValue(view);
            if (oldValue != null && !Objects.equals(oldValue, finalValue)) {
                viewToEntityMapper.remove(context, oldValue);
                context.getOrphanRemovalDeleters().add(new PostFlushViewToEntityMapperDeleter(viewToEntityMapper, oldValue));
            }
        }
        if (doUpdate && inverseFlusher != null) {
            Object oldValue = viewAttributeAccessor.getInitialValue(view);
            if (oldValue != null && !Objects.equals(oldValue, finalValue)) {
                if (inverseRemoveStrategy == InverseCollectionElementAttributeFlusher.Strategy.SET_NULL) {
                    inverseFlusher.flushEntitySetElement(context, oldValue, entity, null, null);
                } else {
                    inverseFlusher.removeElement(context, entity, oldValue);
                }
            }
        }
        if (flushOperation != null) {
            if (flushOperation == ViewFlushOperation.CASCADE) {
                if (update && inverseFlusher != null) {
                    inverseFlusher.flushEntitySetElement(context, finalValue, entity, entity, (DirtyAttributeFlusher<?, E, Object>) (DirtyAttributeFlusher<?, ?, ?>) nestedFlusher);
                } else {
                    try {
                        nestedFlusher.flushEntity(context, null, finalValue, null, finalValue, null);
                    } finally {
                        if (finalValue instanceof MutableStateTrackable) {
                            context.getInitialStateResetter().addUpdatedView((MutableStateTrackable) finalValue);
                        }
                    }
                }
            } else if (update && inverseFlusher != null) {
                if (finalValue instanceof MutableStateTrackable) {
                    inverseFlusher.flushEntitySetElement(context, finalValue, entity, entity, (DirtyAttributeFlusher<?, E, Object>) (DirtyAttributeFlusher<?, ?, ?>) viewToEntityMapper.getNestedDirtyFlusher(context, (MutableStateTrackable) finalValue, null));
                } else {
                    inverseFlusher.flushEntitySetElement(context, finalValue, entity, entity, null);
                }
            }

            Object v = viewToEntityMapper.applyToEntity(context, null, finalValue);
            if (update) {
                entityAttributeAccessor.setValue(entity, v);
            }
            // If the view is creatable, the CompositeAttributeFlusher re-maps the view object and puts the new object to the mutable state array
            Object newValue = viewAttributeAccessor.getMutableStateValue(view);
            if (finalValue != newValue) {
                viewAttributeAccessor.setValue(view, newValue);
            }
            return true;
        }
        boolean wasDirty = false;
        if (updatable || isPassThrough()) {
            if (nestedFlusher != null && nestedFlusher != viewToEntityMapper.getFullGraphNode()) {
                if (update && inverseFlusher != null) {
                    inverseFlusher.flushEntitySetElement(context, finalValue, entity, entity, (DirtyAttributeFlusher<?, E, Object>) (DirtyAttributeFlusher<?, ?, ?>) nestedFlusher);
                    wasDirty |= true;
                } else {
                    try {
                        wasDirty |= nestedFlusher.flushEntity(context, null, finalValue, null, finalValue, null);
                    } finally {
                        if (finalValue instanceof MutableStateTrackable) {
                            context.getInitialStateResetter().addUpdatedView((MutableStateTrackable) finalValue);
                        }
                    }
                }
            } else if (update && inverseFlusher != null) {
                inverseFlusher.flushEntitySetElement(context, finalValue, entity, entity, null);
            }
            Object v = viewToEntityMapper.flushToEntity(context, null, finalValue);
            if (v == null) {
                // Special case when a view references itself
                if (finalValue == view) {
                    v = entity;
                } else {
                    v = viewToEntityMapper.loadEntity(context, finalValue);
                }
            } else {
                wasDirty = true;
            }
            if (update) {
                if (!wasDirty) {
                    Object oldVal = entity == null ? null : entityAttributeAccessor.getValue(entity);
                    Object oldId = oldVal == null ? null : viewToEntityMapper.getEntityIdAccessor().getValue(oldVal);
                    Object newId = v == null ? null : viewToEntityMapper.getEntityIdAccessor().getValue(v);
                    wasDirty = oldVal == null && v != null || !Objects.equals(oldId, newId);
                }
                if (wasDirty) {
                    entityAttributeAccessor.setValue(entity, v);
                }
            } else if (updatable) {
                // If this is a correlated subview, we still signal a version update if the reference was changed
                wasDirty |= !Objects.equals(viewAttributeAccessor.getInitialValue(view), finalValue);
            }
            // If the view is creatable, the CompositeAttributeFlusher re-maps the view object and puts the new object to the mutable state array
            Object newValue = viewAttributeAccessor.getMutableStateValue(view);
            if (finalValue != newValue) {
                viewAttributeAccessor.setValue(view, newValue);
            }
        } else {
            V realValue = (V) viewAttributeAccessor.getValue(view);
            if (nestedFlusher != null && nestedFlusher != viewToEntityMapper.getFullGraphNode()) {
                try {
                    wasDirty |= nestedFlusher.flushEntity(context, null, realValue, null, realValue, null);
                } finally {
                    if (realValue instanceof MutableStateTrackable) {
                        context.getInitialStateResetter().addUpdatedView((MutableStateTrackable) realValue);
                    }
                }
            } else {
                // We don't care if the underlying real value is different from the initial value if we don't have an entity attribute accessor
                // That represents a correlated attribute so we will only do cascading
                if (realValue != null && (entityAttributeAccessor == null || (finalValue == realValue || viewIdEqual(finalValue, realValue)) && jpaAndViewIdEqual(entityAttributeAccessor.getValue(entity), realValue))) {
                    Object v = viewToEntityMapper.flushToEntity(context, null, realValue);
                    if (v != null) {
                        wasDirty = true;
                    }
                }
            }
            if (view != null && finalValue != realValue && viewIdEqual(finalValue, realValue)) {
                viewAttributeAccessor.setValue(view, realValue);
            }
        }
        return wasDirty;
    }

    @SuppressWarnings("unchecked")
    protected final V getConvertedValue(V value) {
        if (converter != null) {
            return (V) converter.convertToUnderlyingType(value);
        }
        return value;
    }

    @Override
    public List<PostFlushDeleter> remove(UpdateContext context, E entity, Object view, V value) {
        if (cascadeDelete) {
            V valueToDelete;
            if (view instanceof DirtyStateTrackable) {
                valueToDelete = (V) viewAttributeAccessor.getInitialValue(view);
            } else {
                valueToDelete = value;
            }
            if (valueToDelete != null) {
                V convertedValue = getConvertedValue(valueToDelete);
                viewToEntityMapper.remove(context, convertedValue);
            }
        }
        return Collections.emptyList();
    }

    @Override
    public void remove(UpdateContext context, Object id) {
        viewToEntityMapper.removeById(context, id);
    }

    @Override
    public List<PostFlushDeleter> removeByOwnerId(UpdateContext context, Object id) {
//        inverseFlusher.removeByOwnerId(context, id);
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void removeFromEntity(UpdateContext context, E entity) {
        if (cascadeDelete) {
            V valueToDelete = (V) entityAttributeAccessor.getValue(entity);
            if (valueToDelete != null) {
                viewToEntityMapper.removeById(context, viewToEntityMapper.getEntityIdAccessor().getValue(valueToDelete));
            }
        }
    }

    @Override
    public boolean requiresDeleteCascadeAfterRemove() {
        // First the owner of the attribute must be deleted, otherwise we might get an FK violation
        return true;
    }

    @Override
    public boolean isViewOnlyDeleteCascaded() {
        return viewOnlyDeleteCascaded;
    }

    @Override
    public boolean isPassThrough() {
        return passThrough;
    }

    @Override
    public String getElementIdAttributeName() {
        // Return the first component representing the actual embedded id
        // If we want to support id class entities as well, we need to adapt our callers as well and return the array
        return elementIdAttributePaths[0];
    }

    @Override
    public AttributeAccessor getViewAttributeAccessor() {
        return viewAttributeAccessor;
    }

    @Override
    public boolean isOptimisticLockProtected() {
        // TODO: the nested graph node could have a different lock owner, we have to handle that
        // If the lock owner isn't INHERIT/AUTO, we should return false
        if (flushOperation != null) {
            if (update && optimisticLockProtected) {
                return true;
            } else if (flushOperation == ViewFlushOperation.CASCADE) {
                return nestedFlusher != null && nestedFlusher.isOptimisticLockProtected();
            }

            return false;
        }
        return optimisticLockProtected || nestedFlusher != null && nestedFlusher.isOptimisticLockProtected();
    }

    @Override
    public boolean requiresFlushAfterPersist(V value) {
        if (inverseFlusher != null) {
            return flushOperation != null && update || flushOperation == null;
        }
        return false;
    }

    @Override
    public boolean requiresDeferredFlush(V value) {
        return value instanceof EntityViewProxy && ((EntityViewProxy) value).$$_isNew() && !viewToEntityMapper.cascades(value);
    }

    @Override
    public <X> DirtyChecker<X>[] getNestedCheckers(V current) {
        return viewToEntityMapper.getUpdater(current).getDirtyChecker().getNestedCheckers((DirtyStateTrackable) current);
    }

    @Override
    public DirtyKind getDirtyKind(V initial, V current) {
        if (current == null) {
            if (initial == null) {
                return DirtyKind.NONE;
            }
            return DirtyKind.UPDATED;
        }
        if (initial == null) {
            return DirtyKind.UPDATED;
        }

        if (initial instanceof DirtyStateTrackable && current instanceof DirtyStateTrackable) {
            EntityViewUpdater updater = viewToEntityMapper.getUpdater(current);
            if (updater != null) {
                return updater.getDirtyChecker().getDirtyKind((DirtyStateTrackable) initial, (DirtyStateTrackable) current);
            }
        }

        return Objects.equals(initial, current) ? DirtyKind.NONE : DirtyKind.UPDATED;
    }

    @Override
    @SuppressWarnings("unchecked")
    public DirtyAttributeFlusher<SubviewAttributeFlusher<E, V>, E, V> getDirtyFlusher(UpdateContext context, Object view, Object initial, Object current, List<Runnable> preFlushListeners) {
        if (isPassThrough()) {
            return null;
        }

        if (updatable) {
            boolean needsUpdate = update && !viewIdEqual(initial, current);
            // If the reference changed, we don't need to load the old reference
            if (initial != current && needsUpdate) {
                return new SubviewAttributeFlusher<>(this, false, (V) current, needsUpdate, null);
            }

            // If the initial and current reference are null, no need to do anything further
            if (initial == null) {
                return null;
            }

            // Otherwise generate or get a dirty flusher that fits our needs
            if (current instanceof MutableStateTrackable) {
                DirtyAttributeFlusher<?, E, V> flusher = (DirtyAttributeFlusher<?, E, V>) viewToEntityMapper.getNestedDirtyFlusher(context, (MutableStateTrackable) current, this);
                if (flusher != null) {
                    return new SubviewAttributeFlusher<>(this, true, (V) current, needsUpdate, flusher);
                }
            }

            return null;
        } else {
            V newValue = (V) viewAttributeAccessor.getValue(view);

            if (current == newValue || viewIdEqual(initial, newValue)) {
                DirtyAttributeFlusher<?, E, V> flusher = (DirtyAttributeFlusher<?, E, V>) viewToEntityMapper.getNestedDirtyFlusher(context, (MutableStateTrackable) newValue, this);
                if (flusher != null) {
                    return new SubviewAttributeFlusher<>(this, true, newValue, false, flusher);
                } else {
                    return null;
                }
            } else {
                // If the value changed, but the attribute is marked as non-updatable, we are done here
                return null;
            }
        }
    }

    private boolean jpaAndViewIdEqual(Object entity, V view) {
        if (entity == null || view == null) {
            return false;
        }

        Object v1 = viewToEntityMapper.getEntityIdAccessor().getValue(entity);
        Object v2 = subviewIdAccessor.getValue(view);

        if (v1 == v2) {
            return true;
        }

        if (v1 == null || v2 == null) {
            return false;
        }

        return v1.equals(v2);
    }

    private boolean viewIdEqual(Object initial, Object current) {
        if (initial == null || current == null) {
            return false;
        }

        Object v1 = subviewIdAccessor.getValue(initial);
        Object v2 = subviewIdAccessor.getValue(current);

        if (v1 == v2) {
            return true;
        }

        if (v1 == null || v2 == null) {
            return false;
        }

        return v1.equals(v2);
    }
}
