/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.objectbuilder;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
public class TupleIdDescriptor {

    private final TupleIdDescriptor parent;
    private final Set<Integer> idPositions;

    public TupleIdDescriptor() {
        this(null);
    }

    public TupleIdDescriptor(TupleIdDescriptor parent) {
        this.parent = parent;
        this.idPositions = new LinkedHashSet<>();
    }

    public int[] createIdPositions() {
        Set<Integer> realIdPositions = computeIdPositions();
        int[] positions = new int[realIdPositions.size()];
        int i = 0;
        for (Integer realIdPosition : realIdPositions) {
            positions[i++] = realIdPosition;
        }
        return positions;
    }

    private Set<Integer> computeIdPositions() {
        if (parent == null) {
            return idPositions;
        }
        Set<Integer> parentIdPositions = parent.computeIdPositions();
        Set<Integer> realIdPositions = new LinkedHashSet<>(parentIdPositions.size() + idPositions.size());
        realIdPositions.addAll(parentIdPositions);
        realIdPositions.addAll(idPositions);
        return realIdPositions;
    }

    public void addIdPosition(int idPosition) {
        idPositions.add(idPosition);
    }
}
