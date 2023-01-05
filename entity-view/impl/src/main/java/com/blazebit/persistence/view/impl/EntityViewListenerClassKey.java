/*
 * Copyright 2014 - 2023 Blazebit.
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
