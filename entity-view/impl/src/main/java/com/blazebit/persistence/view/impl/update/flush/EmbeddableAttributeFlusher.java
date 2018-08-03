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

import com.blazebit.persistence.view.impl.accessor.AttributeAccessor;
import com.blazebit.persistence.view.impl.change.DirtyChecker;
import com.blazebit.persistence.view.impl.entity.EmbeddableUpdaterBasedViewToEntityMapper;
import com.blazebit.persistence.view.impl.update.UpdateContext;
import com.blazebit.persistence.view.impl.entity.ViewToEntityMapper;
import com.blazebit.persistence.view.impl.proxy.DirtyStateTrackable;
import com.blazebit.persistence.view.impl.proxy.MutableStateTrackable;

import javax.persistence.Query;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class EmbeddableAttributeFlusher<E, V> extends EmbeddableAttributeFetchGraphNode<EmbeddableAttributeFlusher<E, V>, DirtyAttributeFlusher<?, E, V>> implements DirtyAttributeFlusher<EmbeddableAttributeFlusher<E, V>, E, V> {

    private final boolean optimisticLockProtected;
    private final String updateFragment;
    private final String parameterName;
    private final boolean passThrough;
    private final boolean supportsQueryFlush;
    private final AttributeAccessor entityAttributeAccessor;
    private final AttributeAccessor viewAttributeAccessor;
    private final EmbeddableUpdaterBasedViewToEntityMapper viewToEntityMapper;

    public EmbeddableAttributeFlusher(String attributeName, String updateFragment, String parameterName, boolean optimisticLockProtected, boolean passThrough, boolean supportsQueryFlush, AttributeAccessor entityAttributeAccessor, AttributeAccessor viewAttributeAccessor, EmbeddableUpdaterBasedViewToEntityMapper viewToEntityMapper) {
        // TODO: QUERY flushing in FULL mode currently won't work with multiple flat view subtypes for an attribute. So be careful here..
        super(attributeName, (DirtyAttributeFlusher<?, E, V>) viewToEntityMapper.getFullGraphNode());
        this.updateFragment = updateFragment;
        this.parameterName = parameterName;
        this.optimisticLockProtected = optimisticLockProtected;
        this.passThrough = passThrough;
        this.supportsQueryFlush = supportsQueryFlush;
        this.entityAttributeAccessor = entityAttributeAccessor;
        this.viewAttributeAccessor = viewAttributeAccessor;
        this.viewToEntityMapper = viewToEntityMapper;
    }

    private EmbeddableAttributeFlusher(EmbeddableAttributeFlusher<E, V> original, DirtyAttributeFlusher<?, E, V> nestedFlusher) {
        super(original.attributeName, nestedFlusher);
        this.updateFragment = original.updateFragment;
        this.parameterName = original.parameterName;
        this.optimisticLockProtected = original.optimisticLockProtected;
        this.passThrough = original.passThrough;
        this.supportsQueryFlush = original.supportsQueryFlush;
        this.entityAttributeAccessor = original.entityAttributeAccessor;
        this.viewAttributeAccessor = original.viewAttributeAccessor;
        this.viewToEntityMapper = original.viewToEntityMapper;
    }

    public ViewToEntityMapper getViewToEntityMapper() {
        return viewToEntityMapper;
    }

    public String getMapping() {
        return updateFragment;
    }

    @Override
    public V cloneDeep(Object view, V oldValue, V newValue) {
        return newValue;
    }

    @Override
    public Object getNewInitialValue(UpdateContext context, V clonedValue, V currentValue) {
        return currentValue;
    }

    @Override
    public boolean supportsQueryFlush() {
        return nestedGraphNode == null || nestedGraphNode.supportsQueryFlush();
    }

    @Override
    public boolean loadForEntityFlush() {
        return true;
    }

    @Override
    public void appendUpdateQueryFragment(UpdateContext context, StringBuilder sb, String mappingPrefix, String parameterPrefix, String separator) {
        String mapping;
        String parameter;
        if (mappingPrefix == null) {
            mapping = updateFragment;
            parameter = parameterName;
        } else {
            mapping = mappingPrefix + updateFragment;
            parameter = parameterPrefix + parameterName;
        }
        if (supportsQueryFlush) {
            sb.append(mapping);
            sb.append(" = :");
            sb.append(parameter);
        } else {
            nestedGraphNode.appendUpdateQueryFragment(context, sb, mapping, parameter, separator);
        }
    }

    @Override
    public void flushQuery(UpdateContext context, String parameterPrefix, Query query, Object view, V value, UnmappedOwnerAwareDeleter ownerAwareDeleter) {
        try {
            String parameter;
            if (parameterPrefix == null) {
                parameter = parameterName;
            } else {
                parameter = parameterPrefix + parameterName;
            }
            if (supportsQueryFlush) {
                query.setParameter(parameter, viewToEntityMapper.applyToEntity(context, null, value));
            } else {
                nestedGraphNode.flushQuery(context, parameter, query, view, value, ownerAwareDeleter);
            }
        } finally {
            if (value instanceof MutableStateTrackable) {
                MutableStateTrackable updatableProxy = (MutableStateTrackable) value;
                context.getInitialStateResetter().addPersistedView(updatableProxy);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean flushEntity(UpdateContext context, E entity, Object view, V value, Runnable postReplaceListener) {
        E embeddableValue = null;
        if (entity != null) {
            embeddableValue = (E) entityAttributeAccessor.getValue(entity);
        }
        if (value == null) {
            if (entity != null) {
                entityAttributeAccessor.setValue(entity, null);
            }
            return embeddableValue != null;
        }
        if (value instanceof MutableStateTrackable) {
            if (embeddableValue == null) {
                embeddableValue = (E) viewToEntityMapper.createEmbeddable(context);
                if (entity != null) {
                    entityAttributeAccessor.setValue(entity, embeddableValue);
                }
            }
            return nestedGraphNode.flushEntity(context, embeddableValue, value, value, postReplaceListener);
        } else {
            if (entity != null) {
                entityAttributeAccessor.setValue(entity, viewToEntityMapper.applyToEntity(context, embeddableValue, value));
            }
            return false;
        }
    }

    @Override
    public List<PostFlushDeleter> remove(UpdateContext context, E entity, Object view, V value) {
        // No-op
        return Collections.emptyList();
    }

    @Override
    public void remove(UpdateContext context, Object id) {
        // No-op
    }

    @Override
    public void removeFromEntity(UpdateContext context, E entity) {
        // No-op
    }

    @Override
    public List<PostFlushDeleter> removeByOwnerId(UpdateContext context, Object id) {
        // No-op
        return Collections.emptyList();
    }

    @Override
    public boolean requiresDeleteCascadeAfterRemove() {
        return false;
    }

    @Override
    public boolean isViewOnlyDeleteCascaded() {
        return false;
    }

    @Override
    public boolean isPassThrough() {
        return passThrough;
    }

    @Override
    public String getElementIdAttributeName() {
        return null;
    }

    @Override
    public AttributeAccessor getViewAttributeAccessor() {
        return viewAttributeAccessor;
    }

    @Override
    public boolean isOptimisticLockProtected() {
        return optimisticLockProtected;
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
        return viewToEntityMapper.getUpdater(current).getDirtyChecker().getDirtyKind((DirtyStateTrackable) initial, (DirtyStateTrackable) current);
    }

    @Override
    public DirtyAttributeFlusher<EmbeddableAttributeFlusher<E, V>, E, V> getDirtyFlusher(UpdateContext context, Object view, Object initial, Object current) {
        if (isPassThrough()) {
            return null;
        }

        if (initial != current && (initial == null || !initial.equals(current))) {
            return this;
        }

        if (current instanceof MutableStateTrackable) {
            MutableStateTrackable mutableStateTrackable = (MutableStateTrackable) current;
            if (!mutableStateTrackable.$$_isDirty()) {
                return null;
            }
            DirtyAttributeFlusher<?, E, V> flusher = (DirtyAttributeFlusher<?, E, V>) viewToEntityMapper.getNestedDirtyFlusher(context, mutableStateTrackable, this);
            if (flusher != null) {
                return new EmbeddableAttributeFlusher<>(this, flusher);
            }
        }

        return null;
    }

}
