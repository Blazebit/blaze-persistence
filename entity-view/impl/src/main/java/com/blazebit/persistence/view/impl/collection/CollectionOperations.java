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

package com.blazebit.persistence.view.impl.collection;

import java.util.Collection;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public final class CollectionOperations {
    private final List<? extends CollectionAction<? extends Collection<?>>> actions;
    private final CollectionRemoveAllAction removeAction;
    private final CollectionAddAllAction addAction;
    private final CollectionRetainAllAction retainAction;
    private final int removeIndex;

    public CollectionOperations(List<? extends CollectionAction<? extends Collection<?>>> actions) {
        // We ensure there will always be at most a single remove and add action after a non-add/remove operation
        CollectionRemoveAllAction removeAction = null;
        CollectionAddAllAction addAction = null;
        CollectionRetainAllAction retainAction = null;
        int removeIndex = actions.size();
        if (!actions.isEmpty()) {
            CollectionAction<?> a = actions.get(actions.size() - 1);
            if (a instanceof CollectionRemoveAllAction) {
                removeAction = (CollectionRemoveAllAction) a;
                removeIndex = actions.size() - 1;
            } else if (a instanceof CollectionAddAllAction) {
                addAction = (CollectionAddAllAction) a;
                removeIndex = actions.size() - 1;
                if (actions.size() > 1) {
                    a = actions.get(actions.size() - 2);
                    if (a instanceof CollectionRemoveAllAction) {
                        removeAction = (CollectionRemoveAllAction) a;
                        removeIndex = actions.size() - 2;
                    }
                }
            } else if (a instanceof CollectionRetainAllAction) {
                retainAction = (CollectionRetainAllAction) a;
                removeIndex = actions.size() - 1;
                if (actions.size() > 1) {
                    a = actions.get(actions.size() - 2);
                    if (a instanceof CollectionAddAllAction) {
                        addAction = (CollectionAddAllAction) a;
                        removeIndex = actions.size() - 2;
                        if (actions.size() > 2) {
                            a = actions.get(actions.size() - 3);
                            if (a instanceof CollectionRemoveAllAction) {
                                removeAction = (CollectionRemoveAllAction) a;
                                removeIndex = actions.size() - 3;
                            }
                        }
                    }
                }
            }
        }

        // We ensure there will always be at most a single remove and add action after a non-add/remove operation
        this.actions = actions;
        this.removeAction = removeAction;
        this.addAction = addAction;
        this.retainAction = retainAction;
        this.removeIndex = removeIndex;
    }

    public boolean addElements(Collection<Object> addedElements) {
        if (!addedElements.isEmpty()) {
            Collection<Object> objectsToAdd = addedElements;
            // A retain action always needs to add all added elements since it's the last operation
            if (retainAction != null) {
                retainAction.onAddObjects(objectsToAdd);
            }
            // Elide removed elements for newly added elements
            if (removeAction != null) {
                objectsToAdd = removeAction.onAddObjects(objectsToAdd);
            }
            if (!objectsToAdd.isEmpty()) {
                // Merge newly added elements into existing add action
                if (addAction != null) {
                    addAction.onAddObjects(objectsToAdd);
                } else {
                    return true;
                }
            }
        }

        return false;
    }

    public int removeElements(Collection<Object> removedElements) {
        if (!removedElements.isEmpty()) {
            Collection<Object> objectsToRemove = removedElements;
            // Elide added elements for newly removed elements
            if (addAction != null) {
                objectsToRemove = addAction.onRemoveObjects(objectsToRemove);
            }
            // A retain operation only needs to remove objects that didn't elide
            if (retainAction != null) {
                retainAction.onRemoveObjects(objectsToRemove);
            }
            if (!objectsToRemove.isEmpty()) {
                // Merge newly removed elements into existing remove action
                if (removeAction != null) {
                    removeAction.onRemoveObjects(objectsToRemove);
                } else {
                    return removeIndex;
                }
            }
        }

        return -1;
    }

    public void removeEmpty() {
        if (addAction != null && addAction.isEmpty()) {
            actions.remove(actions.size() - (retainAction == null ? 1 : 2));
        }
        if (retainAction != null && retainAction.isEmpty()) {
            actions.remove(actions.size() - 1);
        }
        if (removeAction != null && removeAction.isEmpty()) {
            actions.remove(removeIndex);
        }
    }
}
