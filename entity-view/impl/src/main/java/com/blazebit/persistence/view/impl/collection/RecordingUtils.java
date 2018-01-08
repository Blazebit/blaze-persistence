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

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public final class RecordingUtils {

    private RecordingUtils() {
    }

    public static Collection<Object> compensateObjects(Collection<?> elements, Collection<Object> objectsToCompensate) {
        List<Object> newObjectsToRemove = null;
        // Initialize the new collection if we found an overlap
        for (Object o : objectsToCompensate) {
            if (elements.contains(o)) {
                newObjectsToRemove = new ArrayList<>(objectsToCompensate.size());
                break;
            }
        }
        // If not initialized, there's no overlap
        if (newObjectsToRemove == null) {
            return objectsToCompensate;
        }

        // Only add non-elided objects
        for (Object o : objectsToCompensate) {
            if (!elements.remove(o)) {
                newObjectsToRemove.add(o);
            }
        }

        return newObjectsToRemove;
    }

    public static List<Object> replaceElements(Collection<?> elements, Object oldElem, Object elem) {
        List<Object> newElements = null;

        int i = 0;
        for (Object element : elements) {
            if (element == oldElem) {
                if (newElements == null) {
                    newElements = new ArrayList<>(elements);
                }
                newElements.set(i, elem);
            }
            i++;
        }

        return newElements;
    }

    public static Map<Object, Object> replaceElements(Map<?, ?> elements, Object oldKey, Object oldValue, Object newKey, Object newValue) {
        Map<Object, Object> newElements = null;
        Iterator<? extends Map.Entry<?, ?>> iter = elements.entrySet().iterator();

        while (iter.hasNext()) {
            Map.Entry<?, ?> entry = iter.next();
            if (entry.getKey() == oldKey || entry.getValue() == oldValue) {
                if (newElements == null) {
                    newElements = new LinkedHashMap<>(elements);
                }
                newElements.remove(oldKey);
                newElements.put(newKey, newValue);
            }
        }

        return newElements;
    }

    public static List<Map.Entry<Object, Object>> replaceEntries(Collection<? extends Map.Entry<?, ?>> elements, Object oldKey, Object oldValue, Object newKey, Object newValue) {
        List<Map.Entry<Object, Object>> newElements = null;

        int i = 0;
        for (Map.Entry<?, ?> element : elements) {
            if (element.getKey() == oldKey || element.getValue() == oldValue) {
                if (newElements == null) {
                    newElements = new ArrayList<>((Collection<? extends Map.Entry<Object, Object>>) elements);
                }
                newElements.set(i, new AbstractMap.SimpleEntry<>(newKey, newValue));
            }
            i++;
        }

        return newElements;
    }
}
