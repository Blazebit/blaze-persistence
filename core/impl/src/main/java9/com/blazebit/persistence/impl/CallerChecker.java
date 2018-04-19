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

package com.blazebit.persistence.impl;

import java.util.Set;

/**
 * A contract that checks if the caller of our caller is trusted. On Java 9 this will actually check modules.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
class CallerChecker {

    private CallerChecker() {
    }

    public static boolean isCallerTrusted() {
        return StackWalker.getInstance(Set.of(), 2).walk(stackFrames -> {
            return stackFrames.limit(2).allMatch(s -> s.getClassName().startsWith("com.blazebit.persistence."));
        });
    }
}
