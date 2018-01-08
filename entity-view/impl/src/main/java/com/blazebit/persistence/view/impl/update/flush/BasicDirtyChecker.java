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

package com.blazebit.persistence.view.impl.update.flush;

import com.blazebit.persistence.view.impl.change.DirtyChecker;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class BasicDirtyChecker<V> implements DirtyChecker<V> {

    protected final TypeDescriptor elementDescriptor;

    public BasicDirtyChecker(TypeDescriptor elementDescriptor) {
        this.elementDescriptor = elementDescriptor;
    }

    @Override
    public <X> DirtyChecker<X>[] getNestedCheckers(V current) {
        return null;
    }

    @Override
    public DirtyKind getDirtyKind(V initial, V current) {
        if (current == null) {
            if (initial == null) {
                return DirtyKind.NONE;
            }
            return DirtyKind.UPDATED;
        }
        if (initial == null) {
            return DirtyKind.UPDATED;
        }

        if (elementDescriptor.shouldFlushMutations()) {
            if (elementDescriptor.getBasicUserType().supportsDirtyChecking()) {
                String[] dirtyProperties = elementDescriptor.getBasicUserType().getDirtyProperties(current);
                if (dirtyProperties == null) {
                    // When nothing is dirty, no need to persist or merge
                    if (elementDescriptor.getBasicUserType().isEqual(initial, current)) {
                        // If current and initial have the same identity and current is not dirty, no need to flush at all
                        return DirtyKind.NONE;
                    } else {
                        return DirtyKind.UPDATED;
                    }
                } else {
                    return DirtyKind.UPDATED;
                }
            } else {
                if (elementDescriptor.getBasicUserType().isDeepEqual(initial, current)) {
                    return DirtyKind.NONE;
                } else {
                    return DirtyKind.UPDATED;
                }
            }
        } else {
            // Immutable or non-cascading type
            if (elementDescriptor.getBasicUserType().isEqual(initial, current)) {
                return DirtyKind.NONE;
            } else {
                return DirtyKind.UPDATED;
            }
        }
    }
}
