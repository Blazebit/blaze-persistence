/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.type;

import com.blazebit.persistence.spi.JpaProvider;
import com.blazebit.persistence.view.spi.type.BasicUserType;

import java.util.Objects;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class EntityBasicUserType implements BasicUserType<Object> {

    private final JpaProvider jpaProvider;

    public EntityBasicUserType(JpaProvider jpaProvider) {
        this.jpaProvider = jpaProvider;
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public boolean supportsDirtyChecking() {
        return false;
    }

    @Override
    public boolean supportsDirtyTracking() {
        return false;
    }

    @Override
    public boolean supportsDeepEqualChecking() {
        return false;
    }

    @Override
    public boolean supportsDeepCloning() {
        return false;
    }

    @Override
    public boolean isEqual(Object initial, Object current) {
        return Objects.equals(jpaProvider.getIdentifier(initial), jpaProvider.getIdentifier(current));
    }

    @Override
    public boolean isDeepEqual(Object initial, Object current) {
        return false;
    }

    @Override
    public int hashCode(Object object) {
        return Objects.hashCode(jpaProvider.getIdentifier(object));
    }

    @Override
    public boolean shouldPersist(Object entity) {
        return entity != null && jpaProvider.getIdentifier(entity) == null;
    }

    @Override
    public String[] getDirtyProperties(Object entity) {
        return DIRTY_MARKER;
    }

    @Override
    public Object deepClone(Object object) {
        return object;
    }

    @Override
    public Object fromString(CharSequence sequence) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toStringExpression(String expression) {
        throw new UnsupportedOperationException();
    }
}
