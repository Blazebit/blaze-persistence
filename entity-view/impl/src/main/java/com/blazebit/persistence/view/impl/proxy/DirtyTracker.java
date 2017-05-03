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

package com.blazebit.persistence.view.impl.proxy;

@SuppressWarnings("checkstyle:methodname")
public interface DirtyTracker {

    /**
     * Whether a setter was invoked that could possibly alter the state.
     *
     * @return True if dirty, otherwise false
     */
    public boolean $$_isDirty();

    public boolean $$_isDirty(int attributeIndex);

    public <T> boolean $$_copyDirty(T[] source, T[] target);

    /**
     * Sets the dirty state of the object.
     *
     * @param dirty true for dirty, false otherwise
     */
    public void $$_setDirty(long[] dirty);

    public long[] $$_resetDirty();

    public long[] $$_getDirty();

    public long $$_getSimpleDirty();

    public void $$_markDirty(int attributeIndex);

    /**
     * Sets the mutable parent of the object.
     *
     * @param parent The new parent object
     * @param parentIndex The new attribute index of this object in the parent object
     * @throws IllegalStateException If a parent is already set
     */
    public void $$_setParent(DirtyTracker parent, int parentIndex);

    /**
     * Unsets the parent of the object.
     */
    public void $$_unsetParent();

    /**
     * The parent mutable state trackable view object that is notified of changes if there is any, or null.
     *
     * @return The parent object
     */
    public DirtyTracker $$_getParent();

    /**
     * The attribute index of this object in the parent's mutable state trackable view object that is notified of changes if there is any, or null.
     *
     * @return The attribute index of this object in the parent object
     */
    public int $$_getParentIndex();

}
