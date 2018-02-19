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

package com.blazebit.persistence.view.impl.update.flush;

import com.blazebit.persistence.view.OptimisticLockException;
import com.blazebit.persistence.view.impl.accessor.AttributeAccessor;
import com.blazebit.persistence.view.impl.accessor.InitialValueAttributeAccessor;
import com.blazebit.persistence.view.impl.change.DirtyChecker;
import com.blazebit.persistence.view.impl.entity.ViewToEntityMapper;
import com.blazebit.persistence.view.impl.proxy.DirtyStateTrackable;
import com.blazebit.persistence.view.impl.proxy.MutableStateTrackable;
import com.blazebit.persistence.view.impl.update.EntityViewUpdater;
import com.blazebit.persistence.view.impl.update.UpdateContext;
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
    private final String elementIdAttributePath;
    private final String parameterName;
    private final boolean passThrough;
    private final TypeConverter<Object, Object> converter;
    private final AttributeAccessor entityAttributeAccessor;
    private final InitialValueAttributeAccessor viewAttributeAccessor;
    private final AttributeAccessor subviewIdAccessor;
    private final ViewToEntityMapper viewToEntityMapper;
    private final V value;
    private final boolean update;
    private final ViewFlushOperation flushOperation;
    private final DirtyAttributeFlusher<?, E, V> nestedFlusher;

    @SuppressWarnings("unchecked")
    public SubviewAttributeFlusher(String attributeName, String mapping, boolean optimisticLockProtected, boolean updatable, boolean cascadeDelete, boolean orphanRemoval, boolean viewOnlyDeleteCascaded, TypeConverter<?, ?> converter, boolean fetch, String elementIdAttributePath, String parameterName, boolean passThrough,
                                   AttributeAccessor entityAttributeAccessor, InitialValueAttributeAccessor viewAttributeAccessor, AttributeAccessor subviewIdAccessor, ViewToEntityMapper viewToEntityMapper) {
        super(attributeName, mapping, fetch, viewToEntityMapper.getFullGraphNode());
        this.optimisticLockProtected = optimisticLockProtected;
        this.updatable = updatable;
        this.cascadeDelete = cascadeDelete;
        this.orphanRemoval = orphanRemoval;
        this.viewOnlyDeleteCascaded = viewOnlyDeleteCascaded;
        this.converter = (TypeConverter<Object, Object>) converter;
        this.elementIdAttributePath = elementIdAttributePath;
        this.parameterName = parameterName;
        this.passThrough = passThrough;
        this.entityAttributeAccessor = entityAttributeAccessor;
        this.viewAttributeAccessor = viewAttributeAccessor;
        this.subviewIdAccessor = subviewIdAccessor;
        this.viewToEntityMapper = viewToEntityMapper;
        this.value = null;
        this.update = updatable;
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
        this.elementIdAttributePath = original.elementIdAttributePath;
        this.parameterName = original.parameterName;
        this.passThrough = original.passThrough;
        this.entityAttributeAccessor = original.entityAttributeAccessor;
        this.viewAttributeAccessor = original.viewAttributeAccessor;
        this.subviewIdAccessor = original.subviewIdAccessor;
        this.viewToEntityMapper = original.viewToEntityMapper;
        this.value = value;
        this.update = update;
        this.flushOperation = nestedFlusher == null ? ViewFlushOperation.NONE : ViewFlushOperation.CASCADE;
        this.nestedFlusher = nestedFlusher;
    }

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
    public void appendUpdateQueryFragment(UpdateContext context, StringBuilder sb, String mappingPrefix, String parameterPrefix) {
        if (update && (updatable || isPassThrough())) {
            String mapping;
            String parameter;
            if (mappingPrefix == null) {
                mapping = elementIdAttributePath;
                parameter = parameterName;
            } else {
                mapping = mappingPrefix + elementIdAttributePath;
                parameter = parameterPrefix + parameterName;
            }
            sb.append(mapping);
            sb.append(" = :");
            sb.append(parameter);
        }
    }

    @Override
    public boolean supportsQueryFlush() {
        return true;
    }

    @Override
    public void flushQuery(UpdateContext context, String parameterPrefix, Query query, Object view, V value) {
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
            Object oldValue = viewAttributeAccessor.getValue(view);
            if (!Objects.equals(oldValue, finalValue)) {
                viewToEntityMapper.remove(context, oldValue);
            }
        }
        if (flushOperation != null) {
            if (flushOperation == ViewFlushOperation.CASCADE) {
                Query q = viewToEntityMapper.createUpdateQuery(context, finalValue, nestedFlusher);
                nestedFlusher.flushQuery(context, parameterPrefix, q, null, finalValue);
                if (q != null) {
                    int updated = q.executeUpdate();

                    if (updated != 1) {
                        throw new OptimisticLockException(null, finalValue);
                    }
                }
            }

            Object v = viewToEntityMapper.applyToEntity(context, null, finalValue);
            if (query != null && update) {
                Object realValue = v == null ? null : subviewIdAccessor.getValue(finalValue);
                String parameter;
                if (parameterPrefix == null) {
                    parameter = parameterName;
                } else {
                    parameter = parameterPrefix + parameterName;
                }
                query.setParameter(parameter, realValue);
            }

            // If the view is creatable, the CompositeAttributeFlusher re-maps the view object and puts the new object to the mutable state array
            Object newValue = viewAttributeAccessor.getMutableStateValue(view);
            if (finalValue != newValue) {
                viewAttributeAccessor.setValue(view, newValue);
            }
            return;
        }
        if (updatable || isPassThrough()) {
            if (nestedFlusher != null && nestedFlusher != viewToEntityMapper.getFullGraphNode()) {
                Query q = viewToEntityMapper.createUpdateQuery(context, value, nestedFlusher);
                nestedFlusher.flushQuery(context, parameterPrefix, q, null, value);
                if (q != null) {
                    int updated = q.executeUpdate();

                    if (updated != 1) {
                        throw new OptimisticLockException(null, value);
                    }
                }
            }
            Object v = viewToEntityMapper.applyToEntity(context, null, value);
            if (query != null && update) {
                Object realValue = v == null ? null : subviewIdAccessor.getValue(value);
                String parameter;
                if (parameterPrefix == null) {
                    parameter = parameterName;
                } else {
                    parameter = parameterPrefix + parameterName;
                }
                query.setParameter(parameter, realValue);
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
                Query q = viewToEntityMapper.createUpdateQuery(context, realValue, nestedFlusher);
                nestedFlusher.flushQuery(context, parameterPrefix, q, null, realValue);
                if (q != null) {
                    int updated = q.executeUpdate();

                    if (updated != 1) {
                        throw new OptimisticLockException(null, realValue);
                    }
                }
            } else {
                if (realValue != null && (value == realValue || viewIdEqual(value, realValue))) {
                    viewToEntityMapper.applyToEntity(context, null, realValue);
                }
            }
            if (view != null && value != realValue && viewIdEqual(value, realValue)) {
                viewAttributeAccessor.setValue(view, realValue);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean flushEntity(UpdateContext context, E entity, Object view, V value, Runnable postReplaceListener) {
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
            Object oldValue = viewAttributeAccessor.getValue(view);
            if (!Objects.equals(oldValue, finalValue)) {
                viewToEntityMapper.remove(context, oldValue);
            }
        }
        if (flushOperation != null) {
            if (flushOperation == ViewFlushOperation.CASCADE) {
                nestedFlusher.flushEntity(context, null, null, finalValue, null);
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
        if (updatable || isPassThrough()) {
            if (nestedFlusher != null && nestedFlusher != viewToEntityMapper.getFullGraphNode()) {
                nestedFlusher.flushEntity(context, null, null, finalValue, null);
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
        } else {
            V realValue = (V) viewAttributeAccessor.getValue(view);
            if (nestedFlusher != null && nestedFlusher != viewToEntityMapper.getFullGraphNode()) {
                nestedFlusher.flushEntity(context, null, null, realValue, null);
            } else {
                if (realValue != null && (finalValue == realValue || viewIdEqual(finalValue, realValue)) && jpaAndViewIdEqual(entityAttributeAccessor.getValue(entity), realValue)) {
                    viewToEntityMapper.applyToEntity(context, null, realValue);
                }
            }
            if (view != null && finalValue != realValue && viewIdEqual(finalValue, realValue)) {
                viewAttributeAccessor.setValue(view, realValue);
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    protected final V getConvertedValue(V value) {
        if (converter != null) {
            return (V) converter.convertToUnderlyingType(value);
        }
        return value;
    }

    @Override
    public List<PostRemoveDeleter> remove(UpdateContext context, E entity, Object view, V value) {
        if (cascadeDelete) {
            V valueToDelete;
            if (view instanceof DirtyStateTrackable) {
                valueToDelete = (V) viewAttributeAccessor.getInitialValue(view);
            } else {
                valueToDelete = value;
            }
            if (valueToDelete != null) {
                V convertedValue = getConvertedValue(valueToDelete);
                context.getInitialStateResetter().addRemovedView((EntityViewProxy) convertedValue);
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
    public List<PostRemoveDeleter> removeByOwnerId(UpdateContext context, Object id) {
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
        return elementIdAttributePath;
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
        return false;
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

        EntityViewUpdater updater = viewToEntityMapper.getUpdater(current);
        if (updater != null) {
            return updater.getDirtyChecker().getDirtyKind((DirtyStateTrackable) initial, (DirtyStateTrackable) current);
        }

        return Objects.equals(initial, current) ? DirtyKind.NONE : DirtyKind.UPDATED;
    }

    @Override
    @SuppressWarnings("unchecked")
    public DirtyAttributeFlusher<SubviewAttributeFlusher<E, V>, E, V> getDirtyFlusher(UpdateContext context, Object view, Object initial, Object current) {
        if (isPassThrough()) {
            return null;
        }

        if (updatable) {
            boolean needsUpdate = !viewIdEqual(initial, current);
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
