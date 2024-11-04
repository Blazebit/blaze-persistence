/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.collection;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class OrderedMapInstantiator extends AbstractMapInstantiator<Map<?, ?>, RecordingMap<Map<?, ?>, ?, ?>> {

    private final Set<Class<?>> allowedSubtypes;
    private final Set<Class<?>> parentRequiringUpdateSubtypes;
    private final Set<Class<?>> parentRequiringCreateSubtypes;
    private final boolean updatable;
    private final boolean optimize;
    private final boolean strictCascadingCheck;

    public OrderedMapInstantiator(PluralObjectFactory<Map<?, ?>> collectionFactory, Set<Class<?>> allowedSubtypes, Set<Class<?>> parentRequiringUpdateSubtypes, Set<Class<?>> parentRequiringCreateSubtypes, boolean updatable, boolean optimize, boolean strictCascadingCheck) {
        super(collectionFactory);
        this.allowedSubtypes = allowedSubtypes;
        this.parentRequiringUpdateSubtypes = parentRequiringUpdateSubtypes;
        this.parentRequiringCreateSubtypes = parentRequiringCreateSubtypes;
        this.updatable = updatable;
        this.optimize = optimize;
        this.strictCascadingCheck = strictCascadingCheck;
    }

    @Override
    public Map<?, ?> createMap(int size) {
        return new LinkedHashMap<>(size);
    }

    @Override
    public RecordingMap<Map<?, ?>, ?, ?> createRecordingMap(int size) {
        return new RecordingMap(createMap(size), true, allowedSubtypes, parentRequiringUpdateSubtypes, parentRequiringCreateSubtypes, updatable, optimize, strictCascadingCheck);
    }
}
