/*
 * Copyright 2014 - 2017 Blazebit.
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

package com.blazebit.persistence.view.impl.objectbuilder.transformator;

import java.util.HashMap;
import java.util.Map;

public class UpdatableViewMap {

    private final Map<UpdatableViewKey, Object> objectMap = new HashMap<>();

    public Object get(UpdatableViewKey key) {
        return objectMap.get(key);
    }

    public void put(UpdatableViewKey key, Object object) {
        objectMap.put(key, object);
    }

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