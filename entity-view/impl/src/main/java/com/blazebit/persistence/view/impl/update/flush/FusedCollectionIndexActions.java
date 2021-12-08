/*
 * Copyright 2014 - 2021 Blazebit.
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

import com.blazebit.persistence.view.impl.collection.ListAction;
import com.blazebit.persistence.view.impl.update.UpdateContext;
import com.blazebit.persistence.view.spi.type.DirtyTracker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
public class FusedCollectionIndexActions implements FusedCollectionActions {

    private final List<RemoveRangeOperation> removeRangeOperations;
    private final List<IndexTranslateOperation> indexTranslateOperations;
    private final List<ReplaceOperation> replaceOperations;
    private final List<Object> appendedObjects;
    private final int appendIndex;
    private final int removeCount;
    private final int addCount;
    private final int updateCount;

    public FusedCollectionIndexActions(List<? extends ListAction<?>> collectionActions) {
        List<IndexTranslateOperation> translateOperations = new ArrayList<>();
        List<ReplaceOperation> replaceOperations = new ArrayList<>();
        List<Object> appendedObjects = new ArrayList<>();
        int appendIndex = Integer.MAX_VALUE; // The index after which only appends happen
        for (ListAction<?> collectionAction : collectionActions) {
            List<Map.Entry<Object, Integer>> insertedObjectMap = collectionAction.getInsertedObjectEntries();
            List<Map.Entry<Object, Integer>> appendedObjectMap = collectionAction.getAppendedObjectEntries();
            List<Map.Entry<Object, Integer>> removedObjectMap = collectionAction.getRemovedObjectEntries();
            List<Map.Entry<Object, Integer>> trimmedObjectMap = collectionAction.getTrimmedObjectEntries();

            for (Map.Entry<Object, Integer> entry : removedObjectMap) {
                if (appendIndex < entry.getValue()) {
                    int indexToRemove = entry.getValue() - appendIndex;
                    appendedObjects.remove(indexToRemove);
                    if (appendedObjects.isEmpty()) {
                        appendIndex = Integer.MAX_VALUE;
                    }
                } else {
                    int index = applyIndexTranslations(translateOperations, -entry.getValue());
                    RemoveOperation removeOperation = new RemoveOperation(index, entry.getKey());
                    addTranslateOperation(translateOperations, index, Integer.MAX_VALUE, -1, removeOperation, null);
                }
            }
            for (Map.Entry<Object, Integer> entry : trimmedObjectMap) {
                int index = applyIndexTranslations(translateOperations, -entry.getValue());
                appendIndex = Math.min(index, appendIndex);
                RemoveOperation removeOperation = new RemoveOperation(index, entry.getKey());
                addTranslateOperation(translateOperations, index, Integer.MAX_VALUE, -1, removeOperation, null);
            }
            for (Map.Entry<Object, Integer> entry : insertedObjectMap) {
                int index = entry.getValue();
                if (appendIndex < index && appendIndex + appendedObjects.size() == index) {
                    // Inserting into the last index is like appending
                    appendedObjectMap = new ArrayList<>(appendedObjectMap);
                    appendedObjectMap.add(entry);
                } else {
                    ReplaceOperation replaceOperation = new ReplaceOperation(index, entry.getKey());
                    if (addTranslateOperation(translateOperations, index, Integer.MAX_VALUE, 1, null, replaceOperation)) {
                        replaceOperations.add(replaceOperation);
                    }
                }
            }
            OUTER: for (Map.Entry<Object, Integer> entry : appendedObjectMap) {
                int index = entry.getValue();
                for (int i = 0; i < translateOperations.size(); i++) {
                    IndexTranslateOperation translateOperation = translateOperations.get(i);
                    // We look at translate operations that removes the index range within which the append is
                    if (translateOperation.offset == -1 && translateOperation.startIndex <= index && index <= translateOperation.endIndex) {
                        Iterator<RemoveOperation> iterator = translateOperation.removeOperations.iterator();
                        while (iterator.hasNext()) {
                            RemoveOperation removeOperation = iterator.next();
                            // Find the remove operation for the current append index
                            if (index == removeOperation.index) {
                                // If we readd an object we removed before
                                if (removeOperation.removedObject == entry.getKey()) {
                                    // Drop the remove operation
                                    iterator.remove();
                                    // Remove the translate if possible
                                    if (translateOperation.removeOperations.isEmpty()) {
                                        translateOperations.remove(i);
                                    }
                                    // Also skip adding the append
                                    continue OUTER;
                                }
                                break;
                            }
                        }
                        break;
                    }
                }
                appendIndex = Math.min(index, appendIndex);
                appendedObjects.add(entry.getKey());
            }
        }

        SortedSet<RemoveOperation> removeOperations = new TreeSet<>();
        if (appendIndex != Integer.MAX_VALUE) {
            for (int i = 0; i < translateOperations.size(); i++) {
                IndexTranslateOperation indexTranslateOperation = translateOperations.get(i);
                removeOperations.addAll(indexTranslateOperation.removeOperations);
                if (indexTranslateOperation.endIndex == Integer.MAX_VALUE) {
                    // If we have a translate operation without removes that starts after the append index, we can ignore it
                    // Also, we can ignore translate operations that remove a tail fully i.e. do not leave holes
                    if (indexTranslateOperation.startIndex > appendIndex && indexTranslateOperation.removeOperations.isEmpty() || indexTranslateOperation.startIndex + indexTranslateOperation.removeOperations.size() == appendIndex + 1) {
                        translateOperations.remove(i);
                        i--;
                    } else {
                        indexTranslateOperation.endIndex = appendIndex;
                    }
                }
            }
        } else {
            for (int i = 0; i < translateOperations.size(); i++) {
                IndexTranslateOperation indexTranslateOperation = translateOperations.get(i);
                removeOperations.addAll(indexTranslateOperation.removeOperations);
            }
        }

        List<RemoveRangeOperation> removeRangeOperations = new ArrayList<>();
        Iterator<RemoveOperation> iterator = removeOperations.iterator();
        RemoveRangeOperation lastRangeOp = null;
        while (iterator.hasNext()) {
            RemoveOperation removeOp = iterator.next();
            if (lastRangeOp == null || lastRangeOp.endIndex != removeOp.index) {
                List<Object> removedObjects = new ArrayList<>();
                removedObjects.add(removeOp.removedObject);
                lastRangeOp = new RemoveRangeOperation(removeOp.index, removeOp.index + 1, removedObjects);
                removeRangeOperations.add(lastRangeOp);
            } else {
                lastRangeOp.endIndex++;
                lastRangeOp.removedObjects.add(removeOp.removedObject);
            }
        }

        int updateCount = translateOperations.size();

        int addCount = appendedObjects.size();
        for (int i = 0; i < replaceOperations.size(); i++) {
            ReplaceOperation replaceOperation = replaceOperations.get(i);
            if (replaceOperation.oldObject == null) {
                addCount++;
            } else {
                updateCount++;
            }
        }

        this.removeRangeOperations = removeRangeOperations;
        this.indexTranslateOperations = translateOperations;
        this.replaceOperations = replaceOperations;
        this.appendedObjects = appendedObjects;
        this.appendIndex = appendIndex;
        this.removeCount = removeRangeOperations.size();
        this.addCount = addCount;
        this.updateCount = updateCount;
    }

    private static boolean addTranslateOperation(List<IndexTranslateOperation> translateOperations, int startIndex, int endIndex, int offset, RemoveOperation removeOperation, ReplaceOperation replaceOperation) {
        if (translateOperations.isEmpty()) {
            translateOperations.add(new IndexTranslateOperation(startIndex, endIndex, offset, removeOperation == null ? new ArrayList<RemoveOperation>() : new ArrayList<>(Collections.singletonList(removeOperation))));
        } else {
            for (int i = 0; i < translateOperations.size(); i++) {
                IndexTranslateOperation indexTranslateOperation = translateOperations.get(i);
                if (indexTranslateOperation.startIndex <= startIndex && indexTranslateOperation.endIndex >= endIndex) {
                    int indexDiff = Math.abs(indexTranslateOperation.startIndex - startIndex);
                    // TODO: Currently we only handle adds following removes, to handle the other way round we need the replace operation in the translate operation
                    if (indexDiff == 0 && indexTranslateOperation.offset + offset == 0 && indexTranslateOperation.removeOperations.size() == 1 && replaceOperation != null) {
                        // A remove followed an insert or the other way round allow to remove the translation operation
                        translateOperations.remove(i);
                        replaceOperation.oldObject = indexTranslateOperation.removeOperations.get(0).removedObject;
                        Object value = replaceOperation.newObject;
                        return replaceOperation.oldObject != value || value instanceof DirtyTracker && ((DirtyTracker) value).$$_isDirty();
                    } else if (indexDiff == 1 && indexTranslateOperation.endIndex == endIndex) {
                        // Neighbouring index translations with the same endIndex are merged
                        List<RemoveOperation> newList;
                        if (removeOperation == null) {
                            newList = indexTranslateOperation.removeOperations;
                        } else {
                            newList = new ArrayList<>(indexTranslateOperation.removeOperations.size() + 1);
                            newList.addAll(indexTranslateOperation.removeOperations);
                            newList.add(removeOperation);
                        }
                        translateOperations.set(i, new IndexTranslateOperation(Math.min(indexTranslateOperation.startIndex, startIndex), endIndex, indexTranslateOperation.offset + offset, newList));
                        return true;
                    } else {
                        translateOperations.set(i, new IndexTranslateOperation(indexTranslateOperation.startIndex, startIndex, indexTranslateOperation.offset, indexTranslateOperation.removeOperations));
                        translateOperations.add(i + 1, new IndexTranslateOperation(startIndex, endIndex, offset, removeOperation == null ? new ArrayList<RemoveOperation>() : new ArrayList<>(Collections.singletonList(removeOperation))));
                        return true;
                    }
                }
            }

            translateOperations.add(new IndexTranslateOperation(startIndex, endIndex, offset, removeOperation == null ? new ArrayList<RemoveOperation>() : new ArrayList<>(Collections.singletonList(removeOperation))));
        }
        return true;
    }

    private static int applyIndexTranslations(List<IndexTranslateOperation> indexTranslateOperations, int index) {
        int absIndex = Math.abs(index);
        for (int i = 0; i < indexTranslateOperations.size(); i++) {
            IndexTranslateOperation indexTranslateOperation = indexTranslateOperations.get(i);
            if (absIndex >= indexTranslateOperation.startIndex && absIndex <= indexTranslateOperation.endIndex) {
                index += indexTranslateOperation.offset;
                absIndex = Math.abs(index);
            }
        }

        return absIndex;
    }

    @Override
    public int operationCount() {
        return addCount + removeCount + updateCount;
    }

    @Override
    public int getRemoveCount() {
        return removeCount;
    }

    @Override
    public int getAddCount() {
        return addCount;
    }

    @Override
    public int getUpdateCount() {
        return updateCount;
    }

    @Override
    public Collection<Object> getRemoved() {
        List<Object> objects = new ArrayList<>(removeCount);
        for (int i = 0; i < removeRangeOperations.size(); i++) {
            RemoveRangeOperation removeRangeOperation = removeRangeOperations.get(i);
            objects.addAll(removeRangeOperation.removedObjects);
        }
        return objects;
    }

    @Override
    public Collection<Object> getRemoved(UpdateContext context) {
        List<Integer> indexes = new ArrayList<>(removeCount);
        for (int i = 0; i < removeRangeOperations.size(); i++) {
            RemoveRangeOperation removeRangeOperation = removeRangeOperations.get(i);
            for (int j = removeRangeOperation.startIndex; j < removeRangeOperation.endIndex; j++) {
                indexes.add(j);
            }
        }
        return (Collection<Object>) (Collection) indexes;
    }

    public List<IndexTranslateOperation> getTranslations() {
        return indexTranslateOperations;
    }

    @Override
    public Collection<Object> getAdded() {
        return appendedObjects;
    }

    @Override
    public Collection<Object> getAdded(UpdateContext context) {
        return appendedObjects;
    }

    public int getAppendIndex() {
        return appendIndex;
    }

    public List<ReplaceOperation> getReplaces() {
        return replaceOperations;
    }

    /**
     *
     * @author Christian Beikov
     * @since 1.3.0
     */
    private static class RemoveOperation implements Comparable<RemoveOperation> {
        private final int index;
        private final Object removedObject;

        public RemoveOperation(int index, Object removedObject) {
            this.index = index;
            this.removedObject = removedObject;
        }

        @Override
        public int compareTo(RemoveOperation o) {
            return Integer.compare(index, o.index);
        }
    }

    /**
     *
     * @author Christian Beikov
     * @since 1.3.0
     */
    private static class RemoveRangeOperation {
        private int startIndex;
        private int endIndex;
        private final List<Object> removedObjects;

        public RemoveRangeOperation(int startIndex, int endIndex, List<Object> removedObjects) {
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.removedObjects = removedObjects;
        }
    }

    /**
     *
     * @author Christian Beikov
     * @since 1.3.0
     */
    public static class IndexTranslateOperation {
        private int startIndex;
        private int endIndex;
        private int offset;
        private final List<RemoveOperation> removeOperations;

        public IndexTranslateOperation(int startIndex, int endIndex, int offset, List<RemoveOperation> removeOperations) {
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.offset = offset;
            this.removeOperations = removeOperations;
        }

        public int getStartIndex() {
            return startIndex;
        }

        public int getEndIndex() {
            return endIndex;
        }

        public int getOffset() {
            return offset;
        }
    }

    /**
     *
     * @author Christian Beikov
     * @since 1.3.0
     */
    public static class ReplaceOperation {
        private final int index;
        private final Object newObject;
        private Object oldObject;

        public ReplaceOperation(int index, Object newObject) {
            this.index = index;
            this.newObject = newObject;
        }

        public int getIndex() {
            return index;
        }

        public Object getNewObject() {
            return newObject;
        }
    }
}
