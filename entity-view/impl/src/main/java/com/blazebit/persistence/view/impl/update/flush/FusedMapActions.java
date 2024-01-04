/*
 * Copyright 2014 - 2024 Blazebit.
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

import com.blazebit.persistence.view.impl.collection.MapAction;
import com.blazebit.persistence.view.impl.entity.ViewToEntityMapper;
import com.blazebit.persistence.view.spi.type.DirtyTracker;
import com.blazebit.persistence.view.impl.update.UpdateContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
public class FusedMapActions {

    private final ViewToEntityMapper keyViewToEntityMapper;
    private final Map<Object, Object> removed;
    private final Map<Object, Object> added;
    private final Map<Object, Object> replaces;
    private final int removeCount;
    private final int removeValueCount;

    public FusedMapActions(ViewToEntityMapper keyViewToEntityMapper, List<? extends MapAction<?>> collectionActions) {
        this.keyViewToEntityMapper = keyViewToEntityMapper;
        Map<Object, Object> removed = new HashMap<>();
        Map<Object, Object> added = new HashMap<>();
        Map<Object, Object> replaces = new HashMap<>();
        int removeCount = 0;
        int removeValueCount = 0;
        for (MapAction<?> action : collectionActions) {
            Collection<Object> addedKeys = action.getAddedKeys();
            Collection<Object> removedKeys = action.getRemovedKeys();
            Collection<Object> addedElements = action.getAddedElements();
            Collection<Object> removedElements = action.getRemovedElements();

            // Keys and elements have a matching order
            Iterator<Object> addedKeysIter = addedKeys.iterator();
            if (addedKeysIter.hasNext()) {
                Iterator<Object> addedElementsIter = addedElements.iterator();
                Iterator<Object> removedElementsIter = removedElements.iterator();
                while (addedKeysIter.hasNext()) {
                    Object key = addedKeysIter.next();
                    Object value = addedElementsIter.next();
                    Object oldValue = removed.remove(key);
                    Object removedValue = removedElementsIter.hasNext() ? removedElementsIter.next() : null;
                    // Only record adds if the values wasn't removed before
                    if (oldValue == null) {
                        if (removedValue == null) {
                            added.put(key, value);
                        } else {
                            if (removedValue != value || value instanceof DirtyTracker && ((DirtyTracker) value).$$_isDirty() && added.get(key) != value) {
                                removed.put(new RemoveWrapper(key), removedValue);
                                replaces.put(key, value);
                                if (removedValue != value) {
                                    removeValueCount--;
                                }
                            }
                        }
                    } else {
                        removeCount--;
                        removeValueCount--;
                    }
                }
            } else {
                Iterator<Object> removedKeysIter = removedKeys.iterator();
                Iterator<Object> removedElementsIter = removedElements.iterator();
                while (removedKeysIter.hasNext()) {
                    Object key = removedKeysIter.next();
                    Object value = removedElementsIter.next();
                    Object removedValue = added.remove(key);
                    // Only record removes if the values weren't added before
                    if (removedValue == null) {
                        removed.put(key, value);
                        removeCount++;
                        if (value != null) {
                            removeValueCount++;
                        }
                    }
                }
            }
        }

        this.removed = removed;
        this.added = added;
        this.replaces = replaces;
        this.removeCount = removeCount;
        this.removeValueCount = removeValueCount;
    }

    public int operationCount() {
        return removeCount + added.size() + replaces.size();
    }

    public int getRemoveCount() {
        return removeCount;
    }

    public int getRemoveValueCount() {
        return removeValueCount;
    }

    public int getAddCount() {
        return added.size();
    }

    public int getUpdateCount() {
        return replaces.size();
    }

    public Map<Object, Object> getAdded() {
        return added;
    }

    public Map<Object, Object> getRemoved() {
        return removed;
    }

    public Collection<Object> getRemovedKeys(UpdateContext context) {
        if (keyViewToEntityMapper == null) {
            List<Object> entityReferences = new ArrayList<>(removed.size());
            for (Object o : removed.keySet()) {
                if (o instanceof RemoveWrapper) {
                    o = ((RemoveWrapper) o).object;
                }
                entityReferences.add(o);
            }
            return entityReferences;
        } else {
            List<Object> entityReferences = new ArrayList<>(removed.size());
            for (Object o : removed.keySet()) {
                if (o instanceof RemoveWrapper) {
                    o = ((RemoveWrapper) o).object;
                }
                entityReferences.add(o);
            }
            keyViewToEntityMapper.applyAll(context, entityReferences);
            return entityReferences;
        }
    }

    public Map<Object, Object> getReplaces() {
        return replaces;
    }

    /**
     *
     * @author Christian Beikov
     * @since 1.3.0
     */
    public static class RemoveWrapper {
        private final Object object;

        public RemoveWrapper(Object object) {
            this.object = object;
        }

        public Object getObject() {
            return object;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o instanceof RemoveWrapper) {
                RemoveWrapper that = (RemoveWrapper) o;
                return getObject().equals(that.getObject());
            } else {
                if (object == o) {
                    return true;
                }
                return object.equals(o);
            }
        }

        @Override
        public int hashCode() {
            return getObject().hashCode();
        }
    }
}
