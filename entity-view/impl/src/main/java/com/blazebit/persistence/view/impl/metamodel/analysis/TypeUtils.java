/*
 * Copyright 2014 - 2021 Blazebit.
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

package com.blazebit.persistence.view.impl.metamodel.analysis;

import javassist.bytecode.analysis.Type;

import java.lang.reflect.Method;

/**
 * Utility to access {@link Type#popChanged()} reflectively which is not visible unfortunately.
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
class TypeUtils {

    private static final Method POP_CHANGED;

    static {
        try {
            Method popChanged = Type.class.getDeclaredMethod("popChanged");
            popChanged.setAccessible(true);
            POP_CHANGED = popChanged;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private TypeUtils() {
    }

    public static boolean popChanged(Type t) {
        try {
            return (boolean) POP_CHANGED.invoke(t);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
