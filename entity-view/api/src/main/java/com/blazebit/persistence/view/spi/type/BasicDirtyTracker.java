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

package com.blazebit.persistence.view.spi.type;

/**
 * A dirty tracker records the fact that an object was possibly altered
 * and allows to query this information.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@SuppressWarnings("checkstyle:methodname")
public interface BasicDirtyTracker {

    /**
     * Whether a mutating action was invoked that could have possibly altered the state.
     *
     * @return True if maybe dirty, otherwise false
     */
    public boolean $$_isDirty();

    /**
     * Marks the attribute with the given index as dirty.
     *
     * @param attributeIndex The attribute index
     */
    public void $$_markDirty(int attributeIndex);

    /**
     * Unmarks this object as dirty usually done after flushing.
     */
    public void $$_unmarkDirty();

    /**
     * Sets the mutable parent of the object.
     *
     * @param parent The new parent object
     * @param parentIndex The new attribute index of this object in the parent object
     * @throws IllegalStateException If a parent is already set
     */
    public void $$_setParent(BasicDirtyTracker parent, int parentIndex);

    /**
     * Whether this dirty tracked object has a parent dirty tracker.
     *
     * @return true if this dirty tracked object has a parent dirty tracker, false otherwise
     */
    public boolean $$_hasParent();

    /**
     * Unsets the parent of the dirty tracked object.
     */
    public void $$_unsetParent();

}
