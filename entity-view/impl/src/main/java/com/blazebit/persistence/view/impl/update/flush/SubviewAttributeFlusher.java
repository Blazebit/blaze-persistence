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

package com.blazebit.persistence.view.impl.update.flush;

import com.blazebit.persistence.view.OptimisticLockException;
import com.blazebit.persistence.view.impl.accessor.AttributeAccessor;
import com.blazebit.persistence.view.impl.change.DirtyChecker;
import com.blazebit.persistence.view.impl.update.UpdateContext;
import com.blazebit.persistence.view.impl.entity.ViewToEntityMapper;
import com.blazebit.persistence.view.impl.proxy.DirtyStateTrackable;
import com.blazebit.persistence.view.impl.proxy.MutableStateTrackable;
import com.blazebit.persistence.view.impl.update.EntityViewUpdater;

import javax.persistence.Query;
import java.util.Objects;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class SubviewAttributeFlusher<E, V> extends AttributeFetchGraphNode<SubviewAttributeFlusher<E, V>, DirtyAttributeFlusher<?, E, V>> implements DirtyAttributeFlusher<SubviewAttributeFlusher<E, V>, E, V> {

    private final boolean optimisticLockProtected;
    private final boolean updatable;
    private final String updateFragment;
    private final String parameterName;
    private final boolean passThrough;
    private final AttributeAccessor entityAttributeAccessor;
    private final AttributeAccessor viewAttributeAccessor;
    private final AttributeAccessor subviewIdAccessor;
    private final ViewToEntityMapper viewToEntityMapper;
    private final V value;
    private final boolean update;
    private final ViewFlushOperation flushOperation;

    @SuppressWarnings("unchecked")
    public SubviewAttributeFlusher(String attributeName, String mapping, boolean optimisticLockProtected, boolean updatable, boolean fetch, String updateFragment, String parameterName, boolean passThrough, AttributeAccessor entityAttributeAccessor, AttributeAccessor viewAttributeAccessor, AttributeAccessor subviewIdAccessor, ViewToEntityMapper viewToEntityMapper) {
        super(attributeName, mapping, fetch, (DirtyAttributeFlusher<?, E, V>) viewToEntityMapper.getFullGraphNode());
        this.optimisticLockProtected = optimisticLockProtected;
        this.updatable = updatable;
        this.updateFragment = updateFragment;
        this.parameterName = parameterName;
        this.passThrough = passThrough;
        this.entityAttributeAccessor = entityAttributeAccessor;
        this.viewAttributeAccessor = viewAttributeAccessor;
        this.subviewIdAccessor = subviewIdAccessor;
        this.viewToEntityMapper = viewToEntityMapper;
        this.value = null;
        this.update = updatable;
        this.flushOperation = null;
    }

    private SubviewAttributeFlusher(SubviewAttributeFlusher original, boolean fetch, V value, boolean update, DirtyAttributeFlusher<?, E, V> nestedFlusher) {
        super(original.attributeName, original.mapping, fetch, nestedFlusher);
        this.optimisticLockProtected = original.optimisticLockProtected;
        this.updatable = original.updatable;
        this.updateFragment = original.updateFragment;
        this.parameterName = original.parameterName;
        this.passThrough = original.passThrough;
        this.entityAttributeAccessor = original.entityAttributeAccessor;
        this.viewAttributeAccessor = original.viewAttributeAccessor;
        this.subviewIdAccessor = original.subviewIdAccessor;
        this.viewToEntityMapper = original.viewToEntityMapper;
        this.value = value;
        this.update = update;
        this.flushOperation = nestedFlusher == null ? ViewFlushOperation.NONE : ViewFlushOperation.CASCADE;
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
                mapping = updateFragment;
                parameter = parameterName;
            } else {
                mapping = mappingPrefix + updateFragment;
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
        if (flushOperation != null) {
            if (flushOperation == ViewFlushOperation.CASCADE) {
                Query q = viewToEntityMapper.createUpdateQuery(context, this.value, nestedGraphNode);
                nestedGraphNode.flushQuery(context, parameterPrefix, q, null, this.value);
                if (q != null) {
                    int updated = q.executeUpdate();

                    if (updated != 1) {
                        throw new OptimisticLockException(null, this.value);
                    }
                }
            }

            Object v = viewToEntityMapper.applyToEntity(context, null, this.value);
            if (query != null && update) {
                Object realValue = v == null ? null : subviewIdAccessor.getValue(context, this.value);
                String parameter;
                if (parameterPrefix == null) {
                    parameter = parameterName;
                } else {
                    parameter = parameterPrefix + parameterName;
                }
                query.setParameter(parameter, realValue);
            }

            if (view != null && !updatable && this.value != value) {
                viewAttributeAccessor.setValue(context, view, this.value);
            }
            return;
        }
        if (updatable || isPassThrough()) {
            if (nestedGraphNode != null && nestedGraphNode != viewToEntityMapper.getFullGraphNode()) {
                Query q = viewToEntityMapper.createUpdateQuery(context, value, nestedGraphNode);
                nestedGraphNode.flushQuery(context, parameterPrefix, q, null, value);
                if (q != null) {
                    int updated = q.executeUpdate();

                    if (updated != 1) {
                        throw new OptimisticLockException(null, value);
                    }
                }
            }
            Object v = viewToEntityMapper.applyToEntity(context, null, value);
            if (query != null && update) {
                Object realValue = v == null ? null : subviewIdAccessor.getValue(context, value);
                String parameter;
                if (parameterPrefix == null) {
                    parameter = parameterName;
                } else {
                    parameter = parameterPrefix + parameterName;
                }
                query.setParameter(parameter, realValue);
            }
        } else {
            V realValue;
            if (view == null) {
                realValue = value;
            } else {
                realValue = (V) viewAttributeAccessor.getValue(context, view);
            }
            if (nestedGraphNode != null && nestedGraphNode != viewToEntityMapper.getFullGraphNode()) {
                Query q = viewToEntityMapper.createUpdateQuery(context, realValue, nestedGraphNode);
                nestedGraphNode.flushQuery(context, parameterPrefix, q, null, realValue);
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
                viewAttributeAccessor.setValue(context, view, realValue);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean flushEntity(UpdateContext context, E entity, Object view, V value) {
        if (flushOperation != null) {
            if (flushOperation == ViewFlushOperation.CASCADE) {
                nestedGraphNode.flushEntity(context, null, null, this.value);
            }

            Object v = viewToEntityMapper.applyToEntity(context, null, value);
            if (update) {
                entityAttributeAccessor.setValue(context, entity, v);
            }
            if (!updatable && this.value != value) {
                viewAttributeAccessor.setValue(context, view, this.value);
            }
            return true;
        }
        if (updatable || isPassThrough()) {
            if (nestedGraphNode != null && nestedGraphNode != viewToEntityMapper.getFullGraphNode()) {
                nestedGraphNode.flushEntity(context, null, null, value);
            }
            Object v = viewToEntityMapper.applyToEntity(context, null, value);
            if (update) {
                entityAttributeAccessor.setValue(context, entity, v);
            }
        } else {
            V realValue = (V) viewAttributeAccessor.getValue(context, view);
            if (nestedGraphNode != null && nestedGraphNode != viewToEntityMapper.getFullGraphNode()) {
                nestedGraphNode.flushEntity(context, null, null, realValue);
            } else {
                if (realValue != null && (value == realValue || viewIdEqual(value, realValue)) && jpaAndViewIdEqual(context, entityAttributeAccessor.getValue(context, entity), realValue)) {
                    viewToEntityMapper.applyToEntity(context, null, realValue);
                }
            }
            if (view != null && value != realValue && viewIdEqual(value, realValue)) {
                viewAttributeAccessor.setValue(context, view, realValue);
            }
        }
        return true;
    }

    @Override
    public boolean isPassThrough() {
        return passThrough;
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
                return nestedGraphNode != null && nestedGraphNode.isOptimisticLockProtected();
            }

            return false;
        }
        return optimisticLockProtected || nestedGraphNode != null && nestedGraphNode.isOptimisticLockProtected();
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
            V newValue = (V) viewAttributeAccessor.getValue(context, view);

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

    private boolean jpaAndViewIdEqual(UpdateContext context, Object entity, V view) {
        if (entity == null || view == null) {
            return false;
        }

        Object v1 = viewToEntityMapper.getEntityIdAccessor().getValue(context, entity);
        Object v2 = subviewIdAccessor.getValue(context, view);

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

        Object v1 = subviewIdAccessor.getValue(null, initial);
        Object v2 = subviewIdAccessor.getValue(null, current);

        if (v1 == v2) {
            return true;
        }

        if (v1 == null || v2 == null) {
            return false;
        }

        return v1.equals(v2);
    }
}
