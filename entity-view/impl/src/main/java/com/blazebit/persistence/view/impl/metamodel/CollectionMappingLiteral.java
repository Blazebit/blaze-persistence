/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.metamodel;

import com.blazebit.persistence.view.CollectionMapping;

import java.lang.annotation.Annotation;
import java.util.Comparator;

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
public class CollectionMappingLiteral implements CollectionMapping {

    @SuppressWarnings("rawtypes")
    private final Class<? extends Comparator> comparator;
    private final boolean ordered;
    private final boolean ignoreIndex;
    private final boolean forceUnique;

    @SuppressWarnings("rawtypes")
    public CollectionMappingLiteral(Class<? extends Comparator> comparator, boolean ordered, boolean ignoreIndex, boolean forceUnique) {
        this.comparator = comparator;
        this.ordered = ordered;
        this.ignoreIndex = ignoreIndex;
        this.forceUnique = forceUnique;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Class<? extends Comparator> comparator() {
        return comparator;
    }

    @Override
    public boolean ordered() {
        return ordered;
    }

    @Override
    public boolean ignoreIndex() {
        return ignoreIndex;
    }

    @Override
    public boolean forceUnique() {
        return forceUnique;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return CollectionMapping.class;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CollectionMapping)) {
            return false;
        }

        CollectionMapping that = (CollectionMapping) o;

        if (ordered != that.ordered()) {
            return false;
        }
        if (ignoreIndex != that.ignoreIndex()) {
            return false;
        }
        if (forceUnique != that.forceUnique()) {
            return false;
        }
        return comparator != null ? comparator.equals(that.comparator()) : that.comparator() == null;
    }

    @Override
    public int hashCode() {
        int result = comparator != null ? comparator.hashCode() : 0;
        result = 31 * result + (ordered ? 1 : 0);
        result = 31 * result + (ignoreIndex ? 1 : 0);
        result = 31 * result + (forceUnique ? 1 : 0);
        return result;
    }
}
