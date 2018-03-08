/*
 * Copyright 2014 - 2018 Blazebit.
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

package com.blazebit.persistence.view.impl.metamodel;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class ParametersKey implements Comparable<ParametersKey> {

    private final Class<?>[] parameterTypes;

    public ParametersKey(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    @Override
    public int compareTo(ParametersKey o) {
        int cmp = Integer.compare(parameterTypes.length, o.parameterTypes.length);
        if (cmp != 0) {
            return cmp;
        }
        for (int i = 0; i < parameterTypes.length; i++) {
            cmp = parameterTypes[i].getName().compareTo(o.parameterTypes[i].getName());
            if (cmp != 0) {
                return cmp;
            }
        }
        return 0;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        for (int i = 0; i < parameterTypes.length; i++) {
            hash = 31 * hash + parameterTypes[i].getName().hashCode();
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ParametersKey other = (ParametersKey) obj;
        for (int i = 0; i < parameterTypes.length; i++) {
            if (!parameterTypes[i].getName().equals(other.parameterTypes[i].getName())) {
                return false;
            }
        }
        return true;
    }
}
