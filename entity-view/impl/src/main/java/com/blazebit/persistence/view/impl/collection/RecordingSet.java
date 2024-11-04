/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.collection;

import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class RecordingSet<C extends Set<E>, E> extends RecordingCollection<C, E> implements Set<E> {

    protected RecordingSet(C delegate, Set<Class<?>> allowedSubtypes, Set<Class<?>> parentRequiringSubtypes, Set<Class<?>> parentRequiringCreateSubtypes, boolean updatable, boolean optimize, boolean hashBased, boolean ordered, boolean strictCascadingCheck) {
        super(delegate, false, ordered, allowedSubtypes, parentRequiringSubtypes, parentRequiringCreateSubtypes, updatable, optimize, hashBased, strictCascadingCheck);
    }

    public RecordingSet(C delegate, boolean ordered, Set<Class<?>> allowedSubtypes, Set<Class<?>> parentRequiringSubtypes, Set<Class<?>> parentRequiringCreateSubtypes, boolean updatable, boolean optimize, boolean strictCascadingCheck) {
        super(delegate, false, ordered, allowedSubtypes, parentRequiringSubtypes, parentRequiringCreateSubtypes, updatable, optimize, true, strictCascadingCheck);
    }

    @Override
    protected boolean allowDuplicates() {
        return false;
    }

}
