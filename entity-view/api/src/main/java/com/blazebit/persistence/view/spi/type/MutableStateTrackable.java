/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.spi.type;

import java.util.List;

/**
 * A dirty tracker that exposes the mutable state.
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
@SuppressWarnings("checkstyle:methodname")
public interface MutableStateTrackable extends EntityViewProxy, DirtyTracker {

    /**
     * Returns the mutable state as array. Never null, contains the current object state of all mutable attributes.
     * The order is the same as the metamodel attribute order of updatable attributes.
     *
     * @return The mutable state as array
     */
    public Object[] $$_getMutableState();

    /**
     * Returns the parent object at which this object is registered.
     *
     * @return the parent object at which this object is registered
     */
    public DirtyTracker $$_getParent();

    /**
     * Returns an interleaved list of read only parent objects and parent indexes.
     *
     * @return An interleaved list of read only parent objects and parent indexes
     */
    public List<Object> $$_getReadOnlyParents();

    /**
     * Adds the given parent at the given parent index to the interleaved read only parents list.
     *
     * @param readOnlyParent The read only parent
     * @param parentIndex The parent index
     */
    public void $$_addReadOnlyParent(DirtyTracker readOnlyParent, int parentIndex);

    /**
     * Removes the given parent from the given parent index from the interleaved read only parents list.
     *
     * @param readOnlyParent The read only parent
     * @param parentIndex The parent index
     */
    public void $$_removeReadOnlyParent(DirtyTracker readOnlyParent, int parentIndex);

    /**
     * Returns the attribute index at which this object is registered on the parent.
     *
     * @return the attribute index at which this object is registered on the parent
     */
    public int $$_getParentIndex();

    /**
     * Sets whether the object should be new.
     *
     * @param isNew Whether the object should be new
     */
    public void $$_setIsNew(boolean isNew);

    /**
     * Sets the id of the object to the given value.
     *
     * @param id The new id
     */
    public void $$_setId(Object id);

    /**
     * Sets the version of the object to the given value.
     *
     * @param version The new version
     */
    public void $$_setVersion(Object version);

}
