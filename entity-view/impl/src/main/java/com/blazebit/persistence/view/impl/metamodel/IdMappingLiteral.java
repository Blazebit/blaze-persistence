/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.metamodel;

import com.blazebit.persistence.view.IdMapping;

import java.lang.annotation.Annotation;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class IdMappingLiteral implements IdMapping {

    private final String value;

    public IdMappingLiteral(String value) {
        this.value = value;
    }

    @Override
    public String value() {
        return value;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return IdMapping.class;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof IdMapping)) {
            return false;
        }

        IdMapping that = (IdMapping) o;

        return value != null ? value.equals(that.value()) : that.value() == null;
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }
}
