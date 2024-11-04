/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.objectbuilder.transformator;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class UpdatableViewMap {

    private final Map<UpdatableViewKey, Object> objectMap = new HashMap<>();

    public Object get(UpdatableViewKey key) {
        return objectMap.get(key);
    }

    public void put(UpdatableViewKey key, Object object) {
        objectMap.put(key, object);
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    public static class UpdatableViewKey {
        private final Class<?> clazz;
        private final Object identifier;

        public UpdatableViewKey(Class<?> clazz, Object identifier) {
            this.clazz = clazz;
            this.identifier = identifier;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            UpdatableViewKey that = (UpdatableViewKey) o;

            if (!clazz.equals(that.clazz)) {
                return false;
            }
            return identifier.equals(that.identifier);
        }

        @Override
        public int hashCode() {
            int result = clazz.hashCode();
            result = 31 * result + identifier.hashCode();
            return result;
        }
    }

}