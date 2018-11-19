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

package com.blazebit.persistence.view.impl.proxy;

import com.blazebit.persistence.view.spi.type.EntityViewProxy;

import java.util.List;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
@SuppressWarnings("checkstyle:methodname")
public interface MutableStateTrackable extends EntityViewProxy, DirtyTracker {

    /**
     * Never null, contains the current object state of all mutable attributes.
     * The order is the same as the metamodel attribute order of updatable attributes.
     *
     * @return
     */
    public Object[] $$_getMutableState();

    public DirtyTracker $$_getParent();

    /**
     * Returns an interleaved list of read only parent objects and parent indexes.
     *
     * @return An interleaved list of read only parent objects and parent indexes
     */
    public List<Object> $$_getReadOnlyParents();

    public void $$_addReadOnlyParent(DirtyTracker readOnlyParent, int parentIndex);

    public void $$_removeReadOnlyParent(DirtyTracker readOnlyParent, int parentIndex);

    public int $$_getParentIndex();

    public void $$_setIsNew(boolean isNew);

    public void $$_setId(Object id);

    public void $$_setVersion(Object version);

}
