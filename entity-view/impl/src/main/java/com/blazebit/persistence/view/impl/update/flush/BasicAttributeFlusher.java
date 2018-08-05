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

import com.blazebit.persistence.view.InverseRemoveStrategy;
import com.blazebit.persistence.view.impl.accessor.AttributeAccessor;
import com.blazebit.persistence.view.impl.accessor.InitialValueAttributeAccessor;
import com.blazebit.persistence.view.impl.entity.EntityLoaderFetchGraphNode;
import com.blazebit.persistence.view.impl.proxy.DirtyStateTrackable;
import com.blazebit.persistence.view.impl.update.UpdateContext;
import com.blazebit.persistence.view.spi.type.TypeConverter;

import javax.persistence.Query;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class BasicAttributeFlusher<E, V> extends BasicDirtyChecker<V> implements DirtyAttributeFlusher<BasicAttributeFlusher<E, V>, E, V> {

    protected final String parameterName;
    protected final AttributeAccessor entityAttributeAccessor;

    private final String attributeName;
    private final String mapping;
    private final boolean fetch;
    private final boolean supportsQueryFlush;
    private final boolean optimisticLockProtected;
    private final EntityLoaderFetchGraphNode<?> fetchGraphNode;
    private final boolean updatable;
    private final boolean cascadeDelete;
    private final boolean orphanRemoval;
    private final boolean viewOnlyDeleteCascaded;
    private final Map.Entry<AttributeAccessor, BasicAttributeFlusher>[] componentFlushers;
    private final String updateFragment;
    private final AttributeAccessor viewAttributeAccessor;
    private final InitialValueAttributeAccessor initialValueViewAttributeAccessor;
    private final UnmappedBasicAttributeCascadeDeleter deleter;
    private final InverseFlusher<E> inverseFlusher;
    private final InverseCollectionElementAttributeFlusher.Strategy inverseRemoveStrategy;
    private final V value;
    private final boolean update;
    private final BasicFlushOperation flushOperation;

    public BasicAttributeFlusher(String attributeName, String mapping, boolean supportsQueryFlush, boolean optimisticLockProtected, boolean updatable, boolean cascadeDelete, boolean orphanRemoval, boolean viewOnlyDeleteCascaded, Map.Entry<AttributeAccessor, BasicAttributeFlusher>[] componentFlushers,
                                 TypeDescriptor elementDescriptor, String updateFragment, String parameterName, AttributeAccessor entityAttributeAccessor, AttributeAccessor viewAttributeAccessor, UnmappedBasicAttributeCascadeDeleter deleter, InverseFlusher<E> inverseFlusher, InverseRemoveStrategy inverseRemoveStrategy) {
        super(elementDescriptor);
        this.attributeName = attributeName;
        this.mapping = mapping;
        this.optimisticLockProtected = optimisticLockProtected;
        this.cascadeDelete = cascadeDelete;
        this.orphanRemoval = orphanRemoval;
        this.viewOnlyDeleteCascaded = viewOnlyDeleteCascaded;
        this.fetch = elementDescriptor.shouldJpaMerge();
        this.supportsQueryFlush = supportsQueryFlush;
        this.fetchGraphNode = elementDescriptor.getEntityToEntityMapper() == null ? null : elementDescriptor.getEntityToEntityMapper().getFullGraphNode();
        this.updatable = updatable;
        this.componentFlushers = componentFlushers;
        this.updateFragment = updateFragment;
        this.parameterName = parameterName;
        this.entityAttributeAccessor = entityAttributeAccessor;
        this.viewAttributeAccessor = viewAttributeAccessor;
        this.initialValueViewAttributeAccessor = viewAttributeAccessor instanceof InitialValueAttributeAccessor ? (InitialValueAttributeAccessor) viewAttributeAccessor : null;
        this.deleter = deleter;
        this.inverseFlusher = inverseFlusher;
        this.inverseRemoveStrategy = InverseCollectionElementAttributeFlusher.Strategy.of(inverseRemoveStrategy);
        this.value = null;
        this.update = updatable;
        this.flushOperation = null;
    }

    private BasicAttributeFlusher(BasicAttributeFlusher<E, V> original, EntityLoaderFetchGraphNode<?> fetchGraphNode, V value, boolean update, BasicFlushOperation flushOperation) {
        super(original.elementDescriptor);
        this.attributeName = original.attributeName;
        this.mapping = original.mapping;
        this.fetch = fetchGraphNode != null;
        this.supportsQueryFlush = original.supportsQueryFlush;
        this.optimisticLockProtected = original.optimisticLockProtected;
        this.fetchGraphNode = fetchGraphNode;
        this.updatable = original.updatable;
        this.componentFlushers = original.componentFlushers;
        this.cascadeDelete = original.cascadeDelete;
        this.orphanRemoval = original.orphanRemoval;
        this.viewOnlyDeleteCascaded = original.viewOnlyDeleteCascaded;
        this.updateFragment = original.updateFragment;
        this.parameterName = original.parameterName;
        this.entityAttributeAccessor = original.entityAttributeAccessor;
        this.viewAttributeAccessor = original.viewAttributeAccessor;
        this.initialValueViewAttributeAccessor = original.initialValueViewAttributeAccessor;
        this.deleter = original.deleter;
        this.inverseFlusher = original.inverseFlusher;
        this.inverseRemoveStrategy = original.inverseRemoveStrategy;
        this.value = value;
        this.update = update;
        this.flushOperation = flushOperation;
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    private static enum BasicFlushOperation {
        NONE,
        PERSIST,
        MERGE;
    }

    @Override
    public V cloneDeep(Object view, V oldValue, V newValue) {
        if (updatable) {
            return (V) elementDescriptor.getBasicUserType().deepClone(newValue);
        } else {
            return oldValue;
        }
    }

    @Override
    public Object getNewInitialValue(UpdateContext context, V clonedValue, V currentValue) {
        if (elementDescriptor.getBasicUserType().isMutable() && elementDescriptor.getBasicUserType().supportsDeepCloning() && !elementDescriptor.getBasicUserType().supportsDirtyTracking()) {
            return clonedValue;
        } else {
            return currentValue;
        }
    }

    @Override
    public String getAttributeName() {
        return attributeName;
    }

    @Override
    public void appendUpdateQueryFragment(UpdateContext context, StringBuilder sb, String mappingPrefix, String parameterPrefix, String separator) {
        // It must be updatable and the value must have changed
        if ((updatable || isPassThrough()) && (flushOperation == null || update) && inverseFlusher == null) {
            if (updateFragment != null) {
                if (componentFlushers == null) {
                    if (mappingPrefix == null) {
                        sb.append(updateFragment);
                        sb.append(" = :");
                        sb.append(parameterName);
                    } else {
                        sb.append(mappingPrefix).append(updateFragment);
                        sb.append(" = :");
                        sb.append(parameterPrefix).append(parameterName);
                    }
                } else {
                    componentFlushers[0].getValue().appendUpdateQueryFragment(context, sb, mappingPrefix, parameterPrefix, separator);
                    for (int i = 1; i < componentFlushers.length; i++) {
                        sb.append(separator);
                        componentFlushers[i].getValue().appendUpdateQueryFragment(context, sb, mappingPrefix, parameterPrefix, separator);
                    }
                }
            }
        }
    }

    @Override
    public void appendFetchJoinQueryFragment(String base, StringBuilder sb) {
        if (fetch) {
            String newBase = base.replace('.', '_') + "_" + attributeName;
            sb.append(" LEFT JOIN FETCH ")
                    .append(base)
                    .append('.')
                    .append(mapping)
                    .append(" ")
                    .append(newBase);

            if (fetchGraphNode != null) {
                fetchGraphNode.appendFetchJoinQueryFragment(base, sb);
            }
        }
    }

    @Override
    public FetchGraphNode<?> mergeWith(List<BasicAttributeFlusher<E, V>> fetchGraphNodes) {
        for (int i = 0; i < fetchGraphNodes.size(); i++) {
            BasicAttributeFlusher<E, V> flusher = fetchGraphNodes.get(i);
            if (flusher.fetchGraphNode != this.fetchGraphNode) {
                if (this.fetchGraphNode == null) {
                    return flusher;
                } else {
                    return this;
                }
            }
        }

        return this;
    }

    @Override
    public boolean supportsQueryFlush() {
        return supportsQueryFlush;
    }

    @Override
    public boolean loadForEntityFlush() {
        return true;
    }

    @Override
    public void flushQuery(UpdateContext context, String parameterPrefix, Query query, Object view, V value, UnmappedOwnerAwareDeleter ownerAwareDeleter) {
        V finalValue;
        if (flushOperation == null) {
            finalValue = value;
        } else {
            finalValue = this.value;
        }
        finalValue = getConvertedValue(finalValue);
        boolean doUpdate = query != null && (updatable || isPassThrough()) && (flushOperation == null || update);
        // Orphan removal is only valid for entity types
        if (doUpdate && orphanRemoval) {
            Object oldValue = initialValueViewAttributeAccessor.getInitialValue(view);
            if (!Objects.equals(oldValue, finalValue)) {
                context.getEntityManager().remove(oldValue);
            }
        }
        if (doUpdate && inverseFlusher != null) {
            Object oldValue = initialValueViewAttributeAccessor.getInitialValue(view);
            if (oldValue != null && !Objects.equals(oldValue, finalValue)) {
                if (inverseRemoveStrategy == InverseCollectionElementAttributeFlusher.Strategy.SET_NULL) {
                    inverseFlusher.flushQuerySetElement(context, oldValue, null, null, null);
                } else {
                    inverseFlusher.removeElement(context, null, oldValue);
                }
            }
            if (finalValue != null) {
                inverseFlusher.flushQuerySetElement(context, finalValue, view, null, null);
            }
        }
        finalValue = persistOrMerge(context, null, view, finalValue);
        if (doUpdate && inverseFlusher == null) {
            if (componentFlushers == null) {
                String parameter;
                if (parameterPrefix == null) {
                    parameter = parameterName;
                } else {
                    parameter = parameterPrefix + parameterName;
                }
                query.setParameter(parameter, finalValue);
            } else {
                for (int i = 0; i < componentFlushers.length; i++) {
                    Object val = componentFlushers[i].getKey().getValue(finalValue);
                    componentFlushers[i].getValue().flushQuery(context, parameterPrefix, query, view, val, ownerAwareDeleter);
                }
            }
        }
    }

    private V persistOrMerge(UpdateContext context, E entity, Object view, V value) {
        if (flushOperation != null) {
            if (flushOperation == BasicFlushOperation.PERSIST) {
                context.getEntityManager().persist(value);
            } else if (flushOperation == BasicFlushOperation.MERGE) {
                if (fetchGraphNode != null) {
                    Object id = fetchGraphNode.getEntityId(context, value);
                    Object loadedEntity = fetchGraphNode.toEntity(context, id);
                }
                value = context.getEntityManager().merge(value);
                if (updatable && value != this.value) {
                    viewAttributeAccessor.setValue(view, value);
                }
            }
            return value;
        }
        if (elementDescriptor.shouldJpaPersistOrMerge()) {
            boolean shouldJpaPersistOrMerge;
            V realValue;
            if (updatable) {
                realValue = value;
                shouldJpaPersistOrMerge = realValue != null;
            } else {
                realValue = (V) viewAttributeAccessor.getValue(view);
                shouldJpaPersistOrMerge = realValue != null
                        && (value == realValue || idEqual(value, realValue))
                        && (entity == null || idEqual(entityAttributeAccessor.getValue(entity), realValue));
            }

            if (shouldJpaPersistOrMerge) {
                boolean shouldPersist = elementDescriptor.getBasicUserType().shouldPersist(realValue);
                if (shouldPersist) {
                    if (elementDescriptor.shouldJpaPersist()) {
                        context.getEntityManager().persist(realValue);
                    }
                    return realValue;
                } else if (elementDescriptor.shouldJpaMerge()) {
                    if (fetchGraphNode != null) {
                        Object id = fetchGraphNode.getEntityId(context, realValue);
                        Object loadedEntity = fetchGraphNode.toEntity(context, id);
                    }
                    V newValue = context.getEntityManager().merge(realValue);
                    if (updatable && newValue != realValue) {
                        viewAttributeAccessor.setValue(view, newValue);
                    }
                    return newValue;
                }
            }
        } else if (elementDescriptor.shouldFlushMutations()) {
            return (V) viewAttributeAccessor.getValue(view);
        }

        return value;
    }

    @SuppressWarnings("unchecked")
    protected final V getConvertedValue(V value) {
        TypeConverter<Object, Object> converter = elementDescriptor.getConverter();
        if (converter != null) {
            return (V) converter.convertToUnderlyingType(value);
        }
        return value;
    }

    @Override
    public boolean flushEntity(UpdateContext context, E entity, Object view, V value, Runnable postReplaceListener) {
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
            Object oldValue = initialValueViewAttributeAccessor.getInitialValue(view);
            if (!Objects.equals(oldValue, finalValue)) {
                context.getEntityManager().remove(oldValue);
            }
        }
        if (doUpdate && inverseFlusher != null) {
            Object oldValue = initialValueViewAttributeAccessor.getInitialValue(view);
            if (oldValue != null && !Objects.equals(oldValue, finalValue)) {
                if (inverseRemoveStrategy == InverseCollectionElementAttributeFlusher.Strategy.SET_NULL) {
                    inverseFlusher.flushEntitySetElement(context, oldValue, null, null);
                } else {
                    inverseFlusher.removeElement(context, entity, oldValue);
                }
            }
            if (finalValue != null) {
                inverseFlusher.flushEntitySetElement(context, finalValue, entity, null);
            }
        }
        finalValue = persistOrMerge(context, entity, view, finalValue);
        if (doUpdate) {
            entityAttributeAccessor.setValue(entity, finalValue);
            return true;
        }

        return false;
    }

    public void flushEntityComponents(UpdateContext context, E entity, V value) {
        for (int i = 0; i < componentFlushers.length; i++) {
            Object val = componentFlushers[i].getKey().getValue(value);
            componentFlushers[i].getValue().flushEntity(context, entity, null, val, null);
        }
    }

    @Override
    public List<PostFlushDeleter> remove(UpdateContext context, E entity, Object view, V value) {
        if (cascadeDelete) {
            V valueToDelete;
            if (view instanceof DirtyStateTrackable && viewAttributeAccessor instanceof InitialValueAttributeAccessor) {
                valueToDelete = (V) ((InitialValueAttributeAccessor) viewAttributeAccessor).getInitialValue(view);
            } else {
                valueToDelete = value;
            }
            if (valueToDelete != null) {
                context.getEntityManager().remove(getConvertedValue(valueToDelete));
            }
        }
        return Collections.emptyList();
    }

    @Override
    public void removeFromEntity(UpdateContext context, E entity) {
        if (cascadeDelete) {
            V valueToDelete = (V) entityAttributeAccessor.getValue(entity);
            if (valueToDelete != null) {
                context.getEntityManager().remove(valueToDelete);
            }
        }
    }

    @Override
    public List<PostFlushDeleter> removeByOwnerId(UpdateContext context, Object ownerId) {
        if (deleter != null) {
            deleter.removeByOwnerId(context, ownerId);
        }
        return Collections.emptyList();
    }

    @Override
    public void remove(UpdateContext context, Object id) {
        if (deleter != null) {
            deleter.removeById(context, id);
        }
    }

    @Override
    public boolean requiresDeleteCascadeAfterRemove() {
        // First the owner of the attribute must be deleted, otherwise we might get an FK violation
        return cascadeDelete;
    }

    @Override
    public boolean isViewOnlyDeleteCascaded() {
        return viewOnlyDeleteCascaded;
    }

    private boolean idEqual(Object entityValue, Object newValue) {
        if (entityValue == null || newValue == null) {
            return false;
        }

        return elementDescriptor.getBasicUserType().isEqual(entityValue, newValue);
    }

    @Override
    public boolean isPassThrough() {
        return !updatable && !elementDescriptor.shouldFlushMutations();
    }

    @Override
    public String getElementIdAttributeName() {
        return attributeName;
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
        if (inverseFlusher != null) {
            return flushOperation != null && update || flushOperation == null;
        }
        return false;
    }

    @Override
    public DirtyAttributeFlusher<BasicAttributeFlusher<E, V>, E, V> getDirtyFlusher(UpdateContext context, Object view, Object initial, Object current) {
        if (updatable) {
            if (initial != current) {
                if (initial == null) {
                    return noFetchFlusher(current, true, true);
                } else if (current == null) {
                    return new BasicAttributeFlusher<>(this, null, null, true, BasicFlushOperation.NONE);
                } else {
                    if (elementDescriptor.shouldFlushMutations()) {
                        if (elementDescriptor.getBasicUserType().supportsDirtyChecking()) {
                            String[] dirtyProperties = elementDescriptor.getBasicUserType().getDirtyProperties(current);
                            if (dirtyProperties == null) {
                                // When nothing is dirty, no need to persist or merge
                                if (elementDescriptor.getBasicUserType().isEqual(initial, current)) {
                                    // If current and initial have the same identity and current is not dirty, no need to flush at all
                                    return null;
                                } else {
                                    return new BasicAttributeFlusher<>(this, null, (V) current, true, BasicFlushOperation.NONE);
                                }
                            } else if (dirtyProperties.length == 0) {
                                return fetchFlusher(fetchGraphNode, current, true, true);
                            } else {
                                return fetchFlusher(elementDescriptor.getEntityToEntityMapper().getFetchGraph(dirtyProperties), current, true, true);
                            }
                        } else {
                            if (elementDescriptor.getBasicUserType().isDeepEqual(initial, current)) {
                                return null;
                            } else {
                                return noFetchFlusher(current, true, true);
                            }
                        }
                    } else {
                        // Immutable or non-cascading type
                        if (elementDescriptor.getBasicUserType().isEqual(initial, current)) {
                            return null;
                        } else {
                            return this;
                        }
                    }
                }
            } else {
                // If it stays null, nothing needed to do
                if (initial == null) {
                    return null;
                }

                if (elementDescriptor.shouldFlushMutations()) {
                    return mutableFlusher(current, !elementDescriptor.isJpaEntity());
                } else {
                    // No need to flush anything when having an immutable or non-cascading type
                    return null;
                }
            }
        } else {
            // Not updatable
            if (elementDescriptor.shouldFlushMutations()) {
                Object newValue = viewAttributeAccessor.getValue(view);

                if (current == newValue || elementDescriptor.isIdentifiable() && idEqual(initial, newValue)) {
                    return mutableFlusher(current, false);
                } else {
                    // If the value changed, but the attribute is marked as non-updatable, we are done here
                    return null;
                }
            } else {
                // Not updatable and no cascading, this is for pass through flushers only
                return null;
            }
        }
    }

    private DirtyAttributeFlusher<BasicAttributeFlusher<E, V>, E, V> noFetchFlusher(Object current, boolean flushAttribute, boolean update) {
        return fetchFlusher(null, current, flushAttribute, update);
    }

    private DirtyAttributeFlusher<BasicAttributeFlusher<E, V>, E, V> fetchFlusher(EntityLoaderFetchGraphNode<?> fetchGraphNode, Object current, boolean flushAttribute, boolean update) {
        if (elementDescriptor.shouldJpaPersistOrMerge()) {
            if (elementDescriptor.getBasicUserType().shouldPersist(current)) {
                if (elementDescriptor.isCascadePersist()) {
                    if (elementDescriptor.shouldJpaPersist()) {
                        return new BasicAttributeFlusher<>(this, fetchGraphNode, (V) current, update, BasicFlushOperation.PERSIST);
                    } else {
                        return new BasicAttributeFlusher<>(this, fetchGraphNode, (V) current, update, BasicFlushOperation.NONE);
                    }
                } else if (flushAttribute) {
                    return new BasicAttributeFlusher<>(this, fetchGraphNode, (V) current, update, BasicFlushOperation.NONE);
                } else {
                    return null;
                }
            } else {
                if (elementDescriptor.isCascadeUpdate()) {
                    if (elementDescriptor.shouldJpaMerge()) {
                        return new BasicAttributeFlusher<>(this, fetchGraphNode, (V) current, update, BasicFlushOperation.MERGE);
                    } else {
                        return new BasicAttributeFlusher<>(this, fetchGraphNode, (V) current, update, BasicFlushOperation.NONE);
                    }
                } else if (flushAttribute) {
                    return new BasicAttributeFlusher<>(this, fetchGraphNode, (V) current, update, BasicFlushOperation.NONE);
                } else {
                    return null;
                }
            }
        } else if (flushAttribute) {
            return new BasicAttributeFlusher<>(this, fetchGraphNode, (V) current, update, BasicFlushOperation.NONE);
        } else {
            return null;
        }
    }

    protected DirtyAttributeFlusher<BasicAttributeFlusher<E, V>, E, V> mutableFlusher(Object current, boolean update) {
        if (elementDescriptor.getBasicUserType().supportsDirtyChecking()) {
            String[] dirtyProperties = elementDescriptor.getBasicUserType().getDirtyProperties(current);
            if (dirtyProperties == null) {
                // If nothing is dirty, no need to fetch or update
                return null;
            } else if (dirtyProperties.length == 0) {
                return fetchFlusher(fetchGraphNode, current, !elementDescriptor.isJpaEntity(), update);
            } else {
                return fetchFlusher(elementDescriptor.getEntityToEntityMapper().getFetchGraph(dirtyProperties), current, !elementDescriptor.isJpaEntity(), update);
            }
        } else {
            return fetchFlusher(fetchGraphNode, current, !elementDescriptor.isJpaEntity(), update);
        }
    }
}
