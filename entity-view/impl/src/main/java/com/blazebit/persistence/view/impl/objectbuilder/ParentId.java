/*
 * Copyright 2014 Blazebit.
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
package com.blazebit.persistence.view.impl.objectbuilder;

import java.util.Arrays;

/**
 *
 * @author Christian
 */
public class ParentId {

    private final Object[] parentId;

    public ParentId(int[] parentIdPositions, Object[] tuple) {
        parentId = new Object[parentIdPositions.length];
        for (int i = 0; i < parentIdPositions.length; i++) {
            parentId[i] = tuple[parentIdPositions[i]];
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Arrays.deepHashCode(this.parentId);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ParentId other = (ParentId) obj;
        if (!Arrays.deepEquals(this.parentId, other.parentId)) {
            return false;
        }
        return true;
    }
}
