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

import com.blazebit.persistence.view.impl.accessor.AttributeAccessor;
import com.blazebit.persistence.view.impl.entity.EntityLoaderFetchGraphNode;
import com.blazebit.persistence.view.impl.update.UpdateContext;
import com.blazebit.persistence.view.spi.type.TypeConverter;

import javax.persistence.Query;
import java.util.List;

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
    private final String updateFragment;
    private final AttributeAccessor viewAttributeAccessor;
    private final V value;
    private final boolean update;
    private final BasicFlushOperation flushOperation;

    public BasicAttributeFlusher(String attributeName, String mapping, boolean supportsQueryFlush, boolean optimisticLockProtected, boolean updatable, TypeDescriptor elementDescriptor, String updateFragment, String parameterName, AttributeAccessor entityAttributeAccessor, AttributeAccessor viewAttributeAccessor) {
        super(elementDescriptor);
        this.attributeName = attributeName;
        this.mapping = mapping;
        this.optimisticLockProtected = optimisticLockProtected;
        this.fetch = elementDescriptor.shouldJpaMerge();
        this.supportsQueryFlush = supportsQueryFlush;
        this.fetchGraphNode = elementDescriptor.getEntityToEntityMapper() == null ? null : elementDescriptor.getEntityToEntityMapper().getFullGraphNode();
        this.updatable = updatable;
        this.updateFragment = updateFragment;
        this.parameterName = parameterName;
        this.entityAttributeAccessor = entityAttributeAccessor;
        this.viewAttributeAccessor = viewAttributeAccessor;
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
        this.updateFragment = original.updateFragment;
        this.parameterName = original.parameterName;
        this.entityAttributeAccessor = original.entityAttributeAccessor;
        this.viewAttributeAccessor = original.viewAttributeAccessor;
        this.value = value;
        this.update = update;
        this.flushOperation = flushOperation;
    }

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
    public String getAttributeName() {
        return attributeName;
    }

    @Override
    public void appendUpdateQueryFragment(UpdateContext context, StringBuilder sb, String mappingPrefix, String parameterPrefix) {
        // It must be updatable and the value must have changed
        if ((updatable || isPassThrough()) && (flushOperation == null || update)) {
            if (updateFragment != null) {
                if (mappingPrefix == null) {
                    sb.append(updateFragment);
                    sb.append(" = :");
                    sb.append(parameterName);
                } else {
                    sb.append(mappingPrefix).append(updateFragment);
                    sb.append(" = :");
                    sb.append(parameterPrefix).append(parameterName);
                }
            }
        }
    }

    @Override
    public void appendFetchJoinQueryFragment(String base, StringBuilder sb) {
        if (fetch) {
            String newBase = base + "_" + attributeName;
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
    public void flushQuery(UpdateContext context, String parameterPrefix, Query query, Object view, V value) {
        value = getConvertedValue(value);
        value = persistOrMerge(context, null, view, value);
        if (query != null && (updatable || isPassThrough()) && (flushOperation == null || update)) {
            String parameter;
            if (parameterPrefix == null) {
                parameter = parameterName;
            } else {
                parameter = parameterPrefix + parameterName;
            }
            query.setParameter(parameter, value);
        }
    }

    private V persistOrMerge(UpdateContext context, E entity, Object view, V value) {
        if (flushOperation != null) {
            V finalValue = this.value;
            if (flushOperation == BasicFlushOperation.PERSIST) {
                context.getEntityManager().persist(finalValue);
            } else if (flushOperation == BasicFlushOperation.MERGE) {
                if (fetchGraphNode != null) {
                    Object id = fetchGraphNode.getEntityId(context, finalValue);
                    Object loadedEntity = fetchGraphNode.toEntity(context, id);
                }
                finalValue = context.getEntityManager().merge(finalValue);
                if (updatable && finalValue != this.value) {
                    viewAttributeAccessor.setValue(context, view, finalValue);
                }
            }
            return finalValue;
        }
        if (elementDescriptor.shouldJpaPersistOrMerge()) {
            boolean shouldJpaPersistOrMerge;
            V realValue;
            if (updatable) {
                realValue = value;
                shouldJpaPersistOrMerge = realValue != null;
            } else {
                realValue = (V) viewAttributeAccessor.getValue(context, view);
                shouldJpaPersistOrMerge = realValue != null
                        && (value == realValue || idEqual(value, realValue))
                        && (entity == null || idEqual(entityAttributeAccessor.getValue(context, entity), realValue));
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
                        viewAttributeAccessor.setValue(context, view, newValue);
                    }
                    return newValue;
                }
            }
        } else if (elementDescriptor.shouldFlushMutations()) {
            return (V) viewAttributeAccessor.getValue(context, view);
        }

        return value;
    }

    @SuppressWarnings("unchecked")
    protected final V getConvertedValue(V value) {
        TypeConverter<Object, Object> converter = elementDescriptor.getConverter();
        if (converter != null) {
            return (V) converter.convertToEntityType(value);
        }
        return value;
    }

    @Override
    public boolean flushEntity(UpdateContext context, E entity, Object view, V value) {
        value = getConvertedValue(value);
        value = persistOrMerge(context, entity, view, value);
        if (updatable || isPassThrough()) {
            entityAttributeAccessor.setValue(context, entity, value);
            return true;
        }

        return false;
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
                Object newValue = viewAttributeAccessor.getValue(context, view);

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
