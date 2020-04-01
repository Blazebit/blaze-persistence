/*
 * Copyright 2014 - 2020 Blazebit.
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

package com.blazebit.persistence.view.spi.type;

/**
 * A dirty tracker that gives detailed dirty information about attributes.
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
@SuppressWarnings("checkstyle:methodname")
public interface DirtyTracker extends BasicDirtyTracker {

    /**
     * Returns true if the attribute at the given index is dirty, false otherwise.
     *
     * @param attributeIndex The attribute index
     * @return true if the attribute at the given index is dirty, false otherwise
     */
    public boolean $$_isDirty(int attributeIndex);

    /**
     * Copies elements from the source to the target array for the dirty attribute indexes.
     *
     * @param source The source array
     * @param target The target array
     * @param <T> The array element type
     * @return true if it was dirty, false otherwise
     */
    public <T> boolean $$_copyDirty(T[] source, T[] target);

    /**
     * Sets the dirty state of the object.
     *
     * @param dirty the dirty bit masks as long array
     */
    public void $$_setDirty(long[] dirty);

    /**
     * Returns and resets the dirty bit masks as long array.
     *
     * @return the dirty bit masks as long array
     */
    public long[] $$_resetDirty();

    /**
     * Returns the dirty bit masks as long array.
     *
     * @return the dirty bit masks as long array
     */
    public long[] $$_getDirty();

    /**
     * Returns the dirty bit mask as long.
     *
     * @return the dirty bit mask as long
     */
    public long $$_getSimpleDirty();

    /**
     * Replaces the attribute at the given index with the given new object if it matches the old object.
     *
     * @param oldObject The old object
     * @param attributeIndex The attribute index
     * @param newObject The new object
     */
    public void $$_replaceAttribute(Object oldObject, int attributeIndex, Object newObject);

}
