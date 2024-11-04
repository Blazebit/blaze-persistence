/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
