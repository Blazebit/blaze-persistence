/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl;

/**
 * @author Christian
 * @since 1.4.0
 */
public final class EntityViewListenerClassKey {
    private final Class<?> entityViewClass;
    private final Class<?> entityClass;
    private final Class<?> entityViewListenerKind;
    private final Class<?> entityViewListenerClass;

    public EntityViewListenerClassKey(Class<?> entityViewClass, Class<?> entityClass, Class<?> entityViewListenerKind, Class<?> entityViewListenerClass) {
        this.entityViewClass = entityViewClass;
        this.entityClass = entityClass;
        this.entityViewListenerKind = entityViewListenerKind;
        this.entityViewListenerClass = entityViewListenerClass;
    }

    public Class<?> getEntityViewClass() {
        return entityViewClass;
    }

    public Class<?> getEntityClass() {
        return entityClass;
    }

    public Class<?> getEntityViewListenerKind() {
        return entityViewListenerKind;
    }

    public Class<?> getEntityViewListenerClass() {
        return entityViewListenerClass;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EntityViewListenerClassKey)) {
            return false;
        }

        EntityViewListenerClassKey that = (EntityViewListenerClassKey) o;

        if (!entityViewClass.equals(that.entityViewClass)) {
            return false;
        }
        if (!entityClass.equals(that.entityClass)) {
            return false;
        }
        if (!entityViewListenerKind.equals(that.entityViewListenerKind)) {
            return false;
        }
        return entityViewListenerClass.equals(that.entityViewListenerClass);
    }

    @Override
    public int hashCode() {
        int result = entityViewClass.hashCode();
        result = 31 * result + entityClass.hashCode();
        result = 31 * result + entityViewListenerKind.hashCode();
        result = 31 * result + entityViewListenerClass.hashCode();
        return result;
    }
}
