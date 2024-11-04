/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
